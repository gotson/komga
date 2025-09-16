package org.gotson.komga.interfaces.api.kobo

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ObjectNode
import com.fasterxml.jackson.module.kotlin.treeToValue
import com.github.benmanes.caffeine.cache.Caffeine
import io.github.oshai.kotlinlogging.KotlinLogging
import org.apache.commons.io.IOUtils
import org.apache.commons.lang3.RandomStringUtils
import org.gotson.komga.domain.model.Book
import org.gotson.komga.domain.model.BookWithMedia
import org.gotson.komga.domain.model.KomgaSyncToken
import org.gotson.komga.domain.model.MediaExtensionEpub
import org.gotson.komga.domain.model.MediaType.EPUB
import org.gotson.komga.domain.model.R2Device
import org.gotson.komga.domain.model.R2Locator
import org.gotson.komga.domain.model.R2Progression
import org.gotson.komga.domain.model.SyncPoint
import org.gotson.komga.domain.persistence.BookRepository
import org.gotson.komga.domain.persistence.MediaRepository
import org.gotson.komga.domain.persistence.ReadProgressRepository
import org.gotson.komga.domain.persistence.SyncPointRepository
import org.gotson.komga.domain.persistence.ThumbnailBookRepository
import org.gotson.komga.domain.service.BookLifecycle
import org.gotson.komga.domain.service.SyncPointLifecycle
import org.gotson.komga.infrastructure.configuration.KomgaProperties
import org.gotson.komga.infrastructure.image.ImageConverter
import org.gotson.komga.infrastructure.image.ImageType
import org.gotson.komga.infrastructure.kobo.KepubConverter
import org.gotson.komga.infrastructure.kobo.KoboHeaders.X_KOBO_DEVICEID
import org.gotson.komga.infrastructure.kobo.KoboHeaders.X_KOBO_SYNC
import org.gotson.komga.infrastructure.kobo.KoboHeaders.X_KOBO_SYNCTOKEN
import org.gotson.komga.infrastructure.kobo.KoboHeaders.X_KOBO_USERKEY
import org.gotson.komga.infrastructure.kobo.KoboProxy
import org.gotson.komga.infrastructure.kobo.KomgaSyncTokenGenerator
import org.gotson.komga.infrastructure.security.KomgaPrincipal
import org.gotson.komga.infrastructure.web.getCurrentRequest
import org.gotson.komga.infrastructure.web.getMediaTypeOrDefault
import org.gotson.komga.interfaces.api.CommonBookController
import org.gotson.komga.interfaces.api.ContentRestrictionChecker
import org.gotson.komga.interfaces.api.kobo.dto.AuthDto
import org.gotson.komga.interfaces.api.kobo.dto.BookEntitlementContainerDto
import org.gotson.komga.interfaces.api.kobo.dto.BookmarkDto
import org.gotson.komga.interfaces.api.kobo.dto.ChangedEntitlementDto
import org.gotson.komga.interfaces.api.kobo.dto.ChangedProductMetadataDto
import org.gotson.komga.interfaces.api.kobo.dto.ChangedReadingStateDto
import org.gotson.komga.interfaces.api.kobo.dto.ChangedTagDto
import org.gotson.komga.interfaces.api.kobo.dto.DeletedTagDto
import org.gotson.komga.interfaces.api.kobo.dto.DownloadUrlDto
import org.gotson.komga.interfaces.api.kobo.dto.FormatDto
import org.gotson.komga.interfaces.api.kobo.dto.KoboBookMetadataDto
import org.gotson.komga.interfaces.api.kobo.dto.NewEntitlementDto
import org.gotson.komga.interfaces.api.kobo.dto.NewTagDto
import org.gotson.komga.interfaces.api.kobo.dto.ReadingStateDto
import org.gotson.komga.interfaces.api.kobo.dto.ReadingStateStateUpdateDto
import org.gotson.komga.interfaces.api.kobo.dto.ReadingStateUpdateResultDto
import org.gotson.komga.interfaces.api.kobo.dto.RequestResultDto
import org.gotson.komga.interfaces.api.kobo.dto.ResourcesDto
import org.gotson.komga.interfaces.api.kobo.dto.ResultDto
import org.gotson.komga.interfaces.api.kobo.dto.StatisticsDto
import org.gotson.komga.interfaces.api.kobo.dto.StatusDto
import org.gotson.komga.interfaces.api.kobo.dto.StatusInfoDto
import org.gotson.komga.interfaces.api.kobo.dto.SyncResultDto
import org.gotson.komga.interfaces.api.kobo.dto.TagItemDto
import org.gotson.komga.interfaces.api.kobo.dto.TestsDto
import org.gotson.komga.interfaces.api.kobo.dto.WrappedReadingStateDto
import org.gotson.komga.interfaces.api.kobo.dto.toBookEntitlementDto
import org.gotson.komga.interfaces.api.kobo.dto.toDto
import org.gotson.komga.interfaces.api.kobo.dto.toWrappedTagDto
import org.gotson.komga.interfaces.api.kobo.persistence.KoboDtoRepository
import org.gotson.komga.language.toUTCZoned
import org.springframework.core.io.FileSystemResource
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.http.ContentDisposition
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ResponseStatusException
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody
import org.springframework.web.servlet.support.ServletUriComponentsBuilder
import org.springframework.web.util.UriBuilder
import org.springframework.web.util.UriComponentsBuilder
import java.io.FileNotFoundException
import java.io.OutputStream
import java.nio.charset.StandardCharsets
import java.nio.file.Path
import java.time.ZonedDateTime
import java.util.UUID
import java.util.concurrent.TimeUnit
import kotlin.io.path.deleteIfExists
import kotlin.io.path.exists
import kotlin.io.path.nameWithoutExtension

private val logger = KotlinLogging.logger {}

/**
 * The following documentation is coming from the awesome work from [Calibre-web](https://github.com/gotson/calibre-web/blob/14b578dd3a15bd371102d5b9828da830e59b4557/cps/kobo_auth.py).
 *
 * **Log-in**
 *
 * When first booting a Kobo device the user must sign into a Kobo (or affiliate) account.
 * Upon successful sign-in, the user is redirected to
 *     https://auth.kobobooks.com/CrossDomainSignIn?id=<some id>
 * which serves the following response:
 *
 * ```html
 *     <script type='text/javascript'>
 *         location.href='kobo://UserAuthenticated?userId=<redacted>&userKey<redacted>&email=<redacted>&returnUrl=https%3a%2f%2fwww.kobo.com';
 *     </script>
 * ```
 *
 * And triggers the insertion of a userKey into the device's User table.
 *
 * Together, the device's DeviceId and UserKey act as an *irrevocable* authentication
 * token to most (if not all) Kobo APIs. In fact, in most cases only the UserKey is
 * required to authorize the API call.
 *
 * Changing Kobo password *does not* invalidate user keys! This is apparently a known
 * issue for a few years now https://www.mobileread.com/forums/showpost.php?p=3476851&postcount=13
 * (although this poster hypothesised that Kobo could blacklist a DeviceId, many endpoints
 * will still grant access given the userkey.)
 *
 * **Official Kobo Store Api authorization**
 *
 * * For most of the endpoints we care about (sync, metadata, tags, etc), the userKey is
 * passed in the x-kobo-userkey header, and is sufficient to authorize the API call.
 * * Some endpoints (e.g: AnnotationService) instead make use of Bearer tokens pass through
 * an authorization header. To get a BearerToken, the device makes a POST request to the
 * /v1/auth/device endpoint with the secret UserKey and the device's DeviceId.
 * * The book download endpoint passes an auth token as a URL param instead of a header.
 *
 * **Komga implementation**
 *
 * To authenticate the user, an API key is added to the Komga URL when setting up the api_store
 * setting on the device.
 * Thus, every request from the device to the api_store will hit Komga with the
 * API key in the url (e.g: https://mylibrary.com/kobo/<api_key>/v1/library/sync).
 *
 * In addition, once authenticated a session cookie is set on response, which will
 * be sent back for the duration of the session to authorize subsequent API calls
 * and avoid having to lookup the API key in database.
 */
@RestController
@RequestMapping(value = ["/kobo/{authToken}/"], produces = ["application/json; charset=utf-8"])
class KoboController(
  private val koboProxy: KoboProxy,
  private val kepubConverter: KepubConverter,
  private val syncPointLifecycle: SyncPointLifecycle,
  private val syncPointRepository: SyncPointRepository,
  private val komgaSyncTokenGenerator: KomgaSyncTokenGenerator,
  private val komgaProperties: KomgaProperties,
  private val koboDtoRepository: KoboDtoRepository,
  private val mapper: ObjectMapper,
  private val commonBookController: CommonBookController,
  private val bookLifecycle: BookLifecycle,
  private val bookRepository: BookRepository,
  private val thumbnailBookRepository: ThumbnailBookRepository,
  private val readProgressRepository: ReadProgressRepository,
  private val imageConverter: ImageConverter,
  private val mediaRepository: MediaRepository,
  private val contentRestrictionChecker: ContentRestrictionChecker,
) {
  private val cachedKepub =
    Caffeine
      .newBuilder()
      .expireAfterAccess(5, TimeUnit.MINUTES)
      .removalListener<String, Path> { _, value, _ ->
        if (value?.deleteIfExists() == true) logger.debug { "Deleted cached kepub: $value" }
      }.build<String, Path>()

  @GetMapping("ping")
  fun ping() = "pong"

  @GetMapping("v1/initialization")
  fun initialization(
    @PathVariable authToken: String,
  ): ResponseEntity<ResourcesDto> {
    val resources =
      try {
        koboProxy.proxyCurrentRequest().body?.get("Resources")
      } catch (e: Exception) {
        logger.warn { "Failed to get response from Kobo /v1/initialization, fallback to noproxy" }
        null
      } ?: koboProxy.nativeKoboResources

    with(resources as ObjectNode) {
      put("image_host", ServletUriComponentsBuilder.fromCurrentContextPath().toUriString())
      put(
        "image_url_template",
        ServletUriComponentsBuilder
          .fromCurrentContextPath()
          .pathSegment("kobo", authToken, "v1", "books", "{ImageId}", "thumbnail", "{Width}", "{Height}", "false", "image.jpg")
          .build()
          .toUriString(),
      )
      put(
        "image_url_quality_template",
        ServletUriComponentsBuilder
          .fromCurrentContextPath()
          .pathSegment("kobo", authToken, "v1", "books", "{ImageId}", "thumbnail", "{Width}", "{Height}", "{Quality}", "{IsGreyscale}", "image.jpg")
          .build()
          .toUriString(),
      )
    }

    return ResponseEntity
      .ok()
      .header("x-kobo-apitoken", "e30=")
      .body(ResourcesDto(resources))
  }

  /**
   * @return an [AuthDto]
   */
  @PostMapping("v1/auth/device")
  fun authDevice(
    @RequestBody body: JsonNode,
  ): Any {
    try {
      return koboProxy.proxyCurrentRequest(body)
    } catch (e: Exception) {
      logger.warn { "Failed to get response from Kobo /v1/auth/device, fallback to noproxy" }
    }

    /**
     * Komga does not use the /v1/auth/device API call for authentication/authorization.
     * Return dummy data to keep the device happy.
     */
    return AuthDto(
      accessToken = RandomStringUtils.secure().nextAlphanumeric(24),
      refreshToken = RandomStringUtils.secure().nextAlphanumeric(24),
      trackingId = UUID.randomUUID().toString(),
      userKey = body.get("UserKey")?.asText() ?: "",
    )
  }

  //  @RequestMapping(value = ["/v1/analytics/gettests"], method = [RequestMethod.GET, RequestMethod.POST])
  fun analyticsGetTests(
    @RequestHeader(name = X_KOBO_USERKEY, required = false) userKey: String?,
  ) = TestsDto(
    result = "Success",
    testKey = userKey ?: "",
  )

  /**
   * @return an array of [SyncResultDto]
   */
  @GetMapping("v1/library/sync")
  fun syncLibrary(
    @AuthenticationPrincipal principal: KomgaPrincipal,
    @PathVariable authToken: String,
  ): ResponseEntity<Collection<Any>> {
    val syncTokenReceived = komgaSyncTokenGenerator.fromRequestHeaders(getCurrentRequest()) ?: KomgaSyncToken()

    // find the ongoing sync point, else create one
    val toSyncPoint =
      getSyncPointVerified(syncTokenReceived.ongoingSyncPointId, principal.user.id)
        ?: syncPointLifecycle.createSyncPoint(principal.user, principal.apiKey?.id, null) // for now we sync all libraries

    // find the last successful sync, if any
    val fromSyncPoint = getSyncPointVerified(syncTokenReceived.lastSuccessfulSyncPointId, principal.user.id)

    logger.debug { "Library sync from SyncPoint $fromSyncPoint, to SyncPoint: $toSyncPoint" }

    var shouldContinueSync: Boolean
    val downloadUriBuilder = getDownloadUrlBuilder(authToken)
    val syncResultKomga: Collection<SyncResultDto> =
      if (fromSyncPoint != null) {
        // find books added/changed/removed and map to DTO
        var maxRemainingCount = komgaProperties.kobo.syncItemLimit

        val booksAdded =
          syncPointLifecycle.takeBooksAdded(fromSyncPoint.id, toSyncPoint.id, Pageable.ofSize(maxRemainingCount)).also {
            maxRemainingCount -= it.numberOfElements
            shouldContinueSync = it.hasNext()
          }

        val booksChanged =
          if (booksAdded.isLast && maxRemainingCount > 0)
            syncPointLifecycle.takeBooksChanged(fromSyncPoint.id, toSyncPoint.id, Pageable.ofSize(maxRemainingCount)).also {
              maxRemainingCount -= it.numberOfElements
              shouldContinueSync = shouldContinueSync || it.hasNext()
            }
          else
            Page.empty()

        val booksRemoved =
          if (booksChanged.isLast && maxRemainingCount > 0)
            syncPointLifecycle.takeBooksRemoved(fromSyncPoint.id, toSyncPoint.id, Pageable.ofSize(maxRemainingCount)).also {
              maxRemainingCount -= it.numberOfElements
              shouldContinueSync = shouldContinueSync || it.hasNext()
            }
          else
            Page.empty()

        val changedReadingState =
          if (booksRemoved.isLast && maxRemainingCount > 0)
            syncPointLifecycle.takeBooksReadProgressChanged(fromSyncPoint.id, toSyncPoint.id, Pageable.ofSize(maxRemainingCount)).also {
              maxRemainingCount -= it.numberOfElements
              shouldContinueSync = shouldContinueSync || it.hasNext()
            }
          else
            Page.empty()

        val readListsAdded =
          if (changedReadingState.isLast && maxRemainingCount > 0)
            syncPointLifecycle.takeReadListsAdded(fromSyncPoint.id, toSyncPoint.id, Pageable.ofSize(maxRemainingCount)).also {
              maxRemainingCount -= it.numberOfElements
              shouldContinueSync = shouldContinueSync || it.hasNext()
            }
          else
            Page.empty()

        val readListsChanged =
          if (readListsAdded.isLast && maxRemainingCount > 0)
            syncPointLifecycle.takeReadListsChanged(fromSyncPoint.id, toSyncPoint.id, Pageable.ofSize(maxRemainingCount)).also {
              maxRemainingCount -= it.numberOfElements
              shouldContinueSync = shouldContinueSync || it.hasNext()
            }
          else
            Page.empty()

        val readListsRemoved =
          if (readListsChanged.isLast && maxRemainingCount > 0)
            syncPointLifecycle.takeReadListsRemoved(fromSyncPoint.id, toSyncPoint.id, Pageable.ofSize(maxRemainingCount)).also {
              maxRemainingCount -= it.numberOfElements
              shouldContinueSync = shouldContinueSync || it.hasNext()
            }
          else
            Page.empty()

        logger.debug { "Library sync: ${booksAdded.numberOfElements} books added, ${booksChanged.numberOfElements} books changed, ${booksRemoved.numberOfElements} books removed, ${changedReadingState.numberOfElements} books with changed reading state, $readListsAdded readlists added, $readListsChanged readlists changed, $readListsRemoved removed" }

        val metadata =
          koboDtoRepository
            .findBookMetadataByIds((booksAdded.content + booksChanged.content).map { it.bookId })
            .associateBy { it.entitlementId }
            .mapValues { it.value.withDownloadUrls(downloadUriBuilder) }
        val readProgress = readProgressRepository.findAllByBookIdsAndUserId((booksAdded.content + booksChanged.content + changedReadingState.content).map { it.bookId }, principal.user.id).associateBy { it.bookId }
        val readListsBooks = syncPointRepository.findBookIdsByReadListIds(toSyncPoint.id, (readListsAdded.content + readListsChanged.content).map { it.readListId }).groupBy { it.readListId }

        buildList {
          addAll(
            booksAdded.content.map {
              NewEntitlementDto(
                BookEntitlementContainerDto(
                  bookEntitlement = it.toBookEntitlementDto(false),
                  bookMetadata = metadata[it.bookId]!!,
                  readingState = readProgress[it.bookId]?.toDto() ?: getEmptyReadProgressForBook(it.bookId, it.createdDate),
                ),
              )
            },
          )
          addAll(
            booksChanged.content.map {
              NewEntitlementDto(
                BookEntitlementContainerDto(
                  bookEntitlement = it.toBookEntitlementDto(false),
                  bookMetadata = metadata[it.bookId]!!,
                  readingState = readProgress[it.bookId]?.toDto() ?: getEmptyReadProgressForBook(it.bookId, it.createdDate),
                ),
              )
            },
          )
          addAll(
            booksChanged.content.map {
              ChangedProductMetadataDto(metadata[it.bookId]!!)
            },
          )
          addAll(
            booksRemoved.content.map {
              ChangedEntitlementDto(
                BookEntitlementContainerDto(
                  bookEntitlement = it.toBookEntitlementDto(true),
                  bookMetadata = getMetadataForRemovedBook(it.bookId),
                ),
              )
            },
          )
          addAll(
            // changed books are also passed as changed reading state because Kobo does not process ChangedEntitlement even if it contains a ReadingState
            (booksChanged.content + changedReadingState.content).mapNotNull { book ->
              readProgress[book.bookId]?.let { it ->
                ChangedReadingStateDto(
                  WrappedReadingStateDto(
                    it.toDto(),
                  ),
                )
              }
            },
          )
          addAll(
            readListsAdded.content.map {
              NewTagDto(it.toWrappedTagDto(readListsBooks[it.readListId]?.map { b -> TagItemDto(b.bookId) }))
            },
          )
          addAll(
            readListsChanged.content.map {
              ChangedTagDto(it.toWrappedTagDto(readListsBooks[it.readListId]?.map { b -> TagItemDto(b.bookId) }))
            },
          )
          addAll(
            readListsRemoved.content.map {
              DeletedTagDto(it.toWrappedTagDto())
            },
          )
        }
      } else {
        // no starting point, sync everything
        var maxRemainingCount = komgaProperties.kobo.syncItemLimit

        val books =
          syncPointLifecycle.takeBooks(toSyncPoint.id, Pageable.ofSize(maxRemainingCount)).also {
            maxRemainingCount -= it.numberOfElements
            shouldContinueSync = it.hasNext()
          }

        val readLists =
          if (books.isLast && maxRemainingCount > 0)
            syncPointLifecycle.takeReadLists(toSyncPoint.id, Pageable.ofSize(maxRemainingCount)).also {
              maxRemainingCount -= it.numberOfElements
              shouldContinueSync = shouldContinueSync || it.hasNext()
            }
          else
            Page.empty()

        logger.debug { "Library sync: ${books.numberOfElements} books, ${readLists.numberOfElements} readlists" }

        val metadata =
          koboDtoRepository
            .findBookMetadataByIds(books.content.map { it.bookId })
            .associateBy { it.entitlementId }
            .mapValues { it.value.withDownloadUrls(downloadUriBuilder) }
        val readProgress = readProgressRepository.findAllByBookIdsAndUserId(books.content.map { it.bookId }, principal.user.id).associateBy { it.bookId }
        val readListsBooks = syncPointRepository.findBookIdsByReadListIds(toSyncPoint.id, readLists.content.map { it.readListId }).groupBy { it.readListId }

        buildList {
          addAll(
            books.content.map {
              NewEntitlementDto(
                BookEntitlementContainerDto(
                  bookEntitlement = it.toBookEntitlementDto(false),
                  bookMetadata = metadata[it.bookId]!!,
                  readingState = readProgress[it.bookId]?.toDto() ?: getEmptyReadProgressForBook(it.bookId, it.createdDate),
                ),
              )
            },
          )
          addAll(
            readLists.content.map {
              NewTagDto(it.toWrappedTagDto(readListsBooks[it.readListId]?.map { b -> TagItemDto(b.bookId) }))
            },
          )
        }
      }

    // merge Kobo store sync response, we only trigger this once all Komga updates have been processed (shouldContinueSync == false)
    val (syncResultMerged, syncTokenMerged, shouldContinueSyncMerged) =
      if (!shouldContinueSync && koboProxy.isEnabled()) {
        try {
          val koboStoreResponse = koboProxy.proxyCurrentRequest(includeSyncToken = true)
          val syncResultsKobo = koboStoreResponse.body?.let { mapper.treeToValue<Collection<Any>>(it) } ?: emptyList()
          val syncTokenKobo = koboStoreResponse.headers[X_KOBO_SYNCTOKEN]?.firstOrNull()?.let { komgaSyncTokenGenerator.fromBase64(it) }
          val shouldContinueSyncKobo = koboStoreResponse.headers[X_KOBO_SYNC]?.firstOrNull()?.lowercase() == "continue"

          Triple(syncResultKomga + syncResultsKobo, syncTokenKobo ?: syncTokenReceived, shouldContinueSyncKobo)
        } catch (e: Exception) {
          logger.error(e) { "Kobo sync endpoint failure" }
          Triple(syncResultKomga, syncTokenReceived, false)
        }
      } else {
        Triple(syncResultKomga, syncTokenReceived, shouldContinueSync)
      }

    // update synctoken to send back to Kobo device
    val syncTokenUpdated =
      if (shouldContinueSyncMerged) {
        syncTokenMerged.copy(ongoingSyncPointId = toSyncPoint.id)
      } else {
        // cleanup old syncpoint if it exists
        fromSyncPoint?.let { syncPointRepository.deleteOne(it.id) }

        syncTokenMerged.copy(ongoingSyncPointId = null, lastSuccessfulSyncPointId = toSyncPoint.id)
      }

    return ResponseEntity
      .ok()
      .headers {
        if (shouldContinueSyncMerged) it.set(X_KOBO_SYNC, "continue")
        it.set(X_KOBO_SYNCTOKEN, komgaSyncTokenGenerator.toBase64(syncTokenUpdated))
      }.body(syncResultMerged)
  }

  /**
   * @return an array of [KoboBookMetadataDto]
   */
  @GetMapping("/v1/library/{bookId}/metadata")
  fun getBookMetadata(
    @PathVariable authToken: String,
    @PathVariable bookId: String,
  ): ResponseEntity<*> =
    if (!bookRepository.existsById(bookId) && koboProxy.isEnabled())
      koboProxy.proxyCurrentRequest()
    else
      ResponseEntity.ok(koboDtoRepository.findBookMetadataByIds(listOf(bookId)).map { it.withDownloadUrls(getDownloadUrlBuilder(authToken)) })

  /**
   * @return an array of [ReadingStateDto]
   */
  @GetMapping("/v1/library/{bookId}/state")
  fun getState(
    @AuthenticationPrincipal principal: KomgaPrincipal,
    @PathVariable bookId: String,
  ): ResponseEntity<*> {
    val book =
      bookRepository.findByIdOrNull(bookId)
        ?: if (koboProxy.isEnabled())
          return koboProxy.proxyCurrentRequest()
        else
          throw ResponseStatusException(HttpStatus.NOT_FOUND)

    val response = readProgressRepository.findByBookIdAndUserIdOrNull(bookId, principal.user.id)?.toDto() ?: getEmptyReadProgressForBook(book)
    return ResponseEntity.ok(listOf(response))
  }

  /**
   * @return a [RequestResultDto]
   */
  @PutMapping("/v1/library/{bookId}/state")
  fun updateState(
    @AuthenticationPrincipal principal: KomgaPrincipal,
    @PathVariable bookId: String,
    @RequestBody body: ReadingStateStateUpdateDto,
    @RequestHeader(name = X_KOBO_DEVICEID, required = false) koboDeviceId: String = "unknown",
  ): ResponseEntity<*> {
    val book =
      bookRepository.findByIdOrNull(bookId)
        ?: if (koboProxy.isEnabled())
          return koboProxy.proxyCurrentRequest(body)
        else
          throw ResponseStatusException(HttpStatus.NOT_FOUND)

    val koboUpdate = body.readingStates.firstOrNull() ?: throw ResponseStatusException(HttpStatus.BAD_REQUEST)
    if (koboUpdate.currentBookmark.location == null || koboUpdate.currentBookmark.contentSourceProgressPercent == null) throw ResponseStatusException(HttpStatus.BAD_REQUEST)

    // convert the Kobo update request to an R2Progression
    val r2Progression =
      R2Progression(
        modified = koboUpdate.lastModified,
        device =
          R2Device(
            id = principal.apiKey?.id ?: "unknown",
            name = principal.apiKey?.comment ?: "unknown",
          ),
        locator =
          if (koboUpdate.statusInfo.status == StatusDto.FINISHED) {
            // If the book is finished, Kobo sends the first resource instead of the last, so we can't trust what Kobo sent
            val epubExtension =
              mediaRepository.findExtensionByIdOrNull(book.id) as? MediaExtensionEpub
                ?: throw IllegalArgumentException("Epub extension not found")
                  .also { logger.error { "Epub extension not found for book ${book.id}. Book should be re-analyzed." } }
            epubExtension.positions.last()
          } else {
            R2Locator(
              href = koboUpdate.currentBookmark.location.source,
              // assume default, will be overwritten by the correct type when saved
              type = "application/xhtml+xml",
              koboSpan =
                if (koboUpdate.currentBookmark.location.type
                    .contentEquals("kobospan", true)
                )
                  koboUpdate.currentBookmark.location.value
                else
                  null,
              locations =
                R2Locator.Location(
                  progression = koboUpdate.currentBookmark.contentSourceProgressPercent / 100,
                  totalProgression = koboUpdate.currentBookmark.progressPercent?.div(100),
                ),
            )
          },
      )

    val response =
      try {
        bookLifecycle.markProgression(book, principal.user, r2Progression)

        RequestResultDto(
          requestResult = ResultDto.SUCCESS,
          updateResults =
            listOf(
              ReadingStateUpdateResultDto(
                entitlementId = bookId,
                currentBookmarkResult = ResultDto.SUCCESS.wrapped(),
                statisticsResult = ResultDto.IGNORED.wrapped(),
                statusInfoResult = ResultDto.SUCCESS.wrapped(),
              ),
            ),
        )
      } catch (e: Exception) {
        logger.error(e) { "Could not update progression" }
        RequestResultDto(
          requestResult = ResultDto.FAILURE,
          updateResults =
            listOf(
              ReadingStateUpdateResultDto(
                entitlementId = bookId,
                currentBookmarkResult = ResultDto.FAILURE.wrapped(),
                statisticsResult = ResultDto.FAILURE.wrapped(),
                statusInfoResult = ResultDto.FAILURE.wrapped(),
              ),
            ),
        )
      }

    return ResponseEntity.ok(response)
  }

  @GetMapping(
    value = ["v1/books/{bookId}/file/epub"],
    produces = [MediaType.APPLICATION_OCTET_STREAM_VALUE],
  )
  @PreAuthorize("hasRole('FILE_DOWNLOAD')")
  fun getBookFile(
    @AuthenticationPrincipal principal: KomgaPrincipal,
    @PathVariable bookId: String,
    @RequestParam(required = false, name = "convert_kepub") convertToKepub: Boolean = false,
  ): ResponseEntity<StreamingResponseBody> {
    if (convertToKepub) {
      bookRepository.findByIdOrNull(bookId)?.let { book ->
        contentRestrictionChecker.checkContentRestriction(principal.user, book)

        // check cache
        val cacheKey = book.computeCacheKey()
        var kepubPath = cachedKepub.getIfPresent(cacheKey)?.let { if (it.exists()) it else null }

        if (kepubPath == null) {
          // convert
          val converted =
            kepubConverter.convertEpubToKepub(BookWithMedia(book, mediaRepository.findById(bookId)))
              ?: throw ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Kepub conversion failed")
          converted.toFile().deleteOnExit()
          cachedKepub.put(cacheKey, converted)
          kepubPath = converted
        } else {
          logger.debug { "Found kepub in cache" }
        }

        try {
          with(FileSystemResource(kepubPath)) {
            if (!exists()) throw FileNotFoundException(path)
            val stream =
              StreamingResponseBody { os: OutputStream ->
                this.inputStream.use {
                  IOUtils.copyLarge(it, os, ByteArray(8192))
                  os.close()
                }
              }
            return ResponseEntity
              .ok()
              .headers(
                HttpHeaders().apply {
                  contentDisposition =
                    ContentDisposition
                      .builder("attachment")
                      .filename("${book.path.nameWithoutExtension}.kepub.epub", StandardCharsets.UTF_8)
                      .build()
                },
              ).contentType(getMediaTypeOrDefault(EPUB.type))
              .contentLength(contentLength())
              .body(stream)
          }
        } catch (ex: FileNotFoundException) {
          logger.warn(ex) { "File not found: $kepubPath" }
          throw ResponseStatusException(HttpStatus.NOT_FOUND, "File not found, it may have moved")
        }
      } ?: throw ResponseStatusException(HttpStatus.NOT_FOUND)
    } else {
      return commonBookController.getBookFileInternal(principal, bookId)
    }
  }

  private fun Book.computeCacheKey() = "$id-$fileLastModified"

  @GetMapping(
    value = [
      "v1/books/{thumbnailId}/thumbnail/{width}/{height}/{isGreyScale}/image.jpg",
      "v1/books/{thumbnailId}/thumbnail/{width}/{height}/{quality}/{isGreyScale}/image.jpg",
    ],
    produces = [MediaType.IMAGE_JPEG_VALUE],
  )
  fun getBookCover(
    @AuthenticationPrincipal principal: KomgaPrincipal,
    @PathVariable thumbnailId: String,
    @PathVariable width: String?,
    @PathVariable height: String?,
    @PathVariable quality: String?,
    @PathVariable isGreyScale: String?,
  ): ResponseEntity<Any> =
    if (!thumbnailBookRepository.existsById(thumbnailId) && koboProxy.isEnabled()) {
      ResponseEntity
        .status(HttpStatus.TEMPORARY_REDIRECT)
        .location(UriComponentsBuilder.fromUriString(koboProxy.imageHostUrl).buildAndExpand(thumbnailId, width, height).toUri())
        .build()
    } else {
      val poster = bookLifecycle.getThumbnailBytesByThumbnailId(thumbnailId) ?: throw ResponseStatusException(HttpStatus.NOT_FOUND)
      val posterBytes =
        if (poster.mediaType != ImageType.JPEG.mediaType)
          imageConverter.convertImage(poster.bytes, ImageType.JPEG.imageIOFormat)
        else
          poster.bytes
      ResponseEntity.ok(posterBytes)
    }

  @RequestMapping(
    value = ["{*path}"],
    method = [RequestMethod.GET, RequestMethod.PUT, RequestMethod.POST, RequestMethod.DELETE, RequestMethod.PATCH],
  )
  fun catchAll(
    @RequestBody body: Any?,
  ): ResponseEntity<JsonNode> =
    if (koboProxy.isEnabled())
      koboProxy.proxyCurrentRequest(body)
    else
      ResponseEntity.ok().body(mapper.createObjectNode())

  private fun getDownloadUrlBuilder(token: String): UriBuilder =
    ServletUriComponentsBuilder
      .fromCurrentContextPath()
      .pathSegment("kobo", token, "v1", "books", "{bookId}", "file", "epub")
      .query("convert_kepub={convert}")

  private fun KoboBookMetadataDto.withDownloadUrls(downloadUriBuilder: UriBuilder) =
    this.copy(
      downloadUrls =
        buildList {
          val (format, convert) =
            when {
              // for fixed layout we always send EPUB3FL, so the Kobo can display in full screen
              // no conversion to Kepub is necessary, as there is already 1 chapter per page, which is sufficient for progress tracking
              isPrePaginated -> FormatDto.EPUB3FL to false
              // provide Kepub if available, or convert if possible
              isKepub || kepubConverter.isAvailable -> FormatDto.KEPUB to !isKepub
              else -> FormatDto.EPUB3 to false
            }
          add(
            DownloadUrlDto(
              format = format,
              size = fileSize,
              url = downloadUriBuilder.build(entitlementId, convert).toURL().toString(),
            ),
          )
        },
    )

  /**
   * Retrieve a SyncPoint by ID, and verifies it belongs to the same userId
   */
  private fun getSyncPointVerified(
    syncPointId: String?,
    userId: String,
  ): SyncPoint? {
    if (syncPointId != null) {
      val syncPoint = syncPointRepository.findByIdOrNull(syncPointId)
      // verify that the SyncPoint is owned by the user
      if (syncPoint?.userId == userId) return syncPoint
    }
    return null
  }

  private fun getMetadataForRemovedBook(bookId: String) =
    KoboBookMetadataDto(
      coverImageId = bookId,
      crossRevisionId = bookId,
      entitlementId = bookId,
      revisionId = bookId,
      workId = bookId,
      title = bookId,
      isKepub = false,
      isPrePaginated = false,
      fileSize = 0,
    )

  private fun getEmptyReadProgressForBook(book: Book): ReadingStateDto {
    val createdDateUTC = book.createdDate.toUTCZoned()
    return ReadingStateDto(
      created = createdDateUTC,
      lastModified = createdDateUTC,
      priorityTimestamp = createdDateUTC,
      entitlementId = book.id,
      currentBookmark = BookmarkDto(createdDateUTC),
      statistics = StatisticsDto(createdDateUTC),
      statusInfo =
        StatusInfoDto(
          lastModified = createdDateUTC,
          status = StatusDto.READY_TO_READ,
          timesStartedReading = 0,
        ),
    )
  }

  private fun getEmptyReadProgressForBook(
    bookId: String,
    createdDate: ZonedDateTime,
  ): ReadingStateDto =
    ReadingStateDto(
      created = createdDate,
      lastModified = createdDate,
      priorityTimestamp = createdDate,
      entitlementId = bookId,
      currentBookmark = BookmarkDto(createdDate),
      statistics = StatisticsDto(createdDate),
      statusInfo =
        StatusInfoDto(
          lastModified = createdDate,
          status = StatusDto.READY_TO_READ,
          timesStartedReading = 0,
        ),
    )
}
