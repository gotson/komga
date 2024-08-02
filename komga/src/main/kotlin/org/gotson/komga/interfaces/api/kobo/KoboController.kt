package org.gotson.komga.interfaces.api.kobo

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import io.github.oshai.kotlinlogging.KotlinLogging
import org.apache.commons.lang3.RandomStringUtils
import org.gotson.komga.domain.model.KomgaSyncToken
import org.gotson.komga.domain.model.SyncPoint
import org.gotson.komga.domain.persistence.SyncPointRepository
import org.gotson.komga.domain.service.SyncPointLifecycle
import org.gotson.komga.infrastructure.configuration.KomgaProperties
import org.gotson.komga.infrastructure.kobo.KoboHeaders.X_KOBO_SYNC
import org.gotson.komga.infrastructure.kobo.KoboHeaders.X_KOBO_SYNCTOKEN
import org.gotson.komga.infrastructure.kobo.KoboHeaders.X_KOBO_USERKEY
import org.gotson.komga.infrastructure.kobo.KoboProxy
import org.gotson.komga.infrastructure.kobo.KomgaSyncTokenGenerator
import org.gotson.komga.infrastructure.security.KomgaPrincipal
import org.gotson.komga.infrastructure.web.getCurrentRequest
import org.gotson.komga.interfaces.api.CommonBookController
import org.gotson.komga.interfaces.api.kobo.dto.AuthDto
import org.gotson.komga.interfaces.api.kobo.dto.ChangedEntitlementDto
import org.gotson.komga.interfaces.api.kobo.dto.NewEntitlementDto
import org.gotson.komga.interfaces.api.kobo.dto.ResourcesDto
import org.gotson.komga.interfaces.api.kobo.dto.SyncResultDto
import org.gotson.komga.interfaces.api.kobo.dto.TestsDto
import org.gotson.komga.interfaces.api.kobo.dto.toBookEntitlementDto
import org.gotson.komga.interfaces.api.kobo.persistence.KoboDtoRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody
import org.springframework.web.servlet.support.ServletUriComponentsBuilder
import org.springframework.web.util.UriBuilder
import java.util.UUID

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
@RequestMapping(value = ["/kobo/{authToken}/"], produces = [MediaType.APPLICATION_JSON_VALUE])
class KoboController(
  private val koboProxy: KoboProxy,
  private val syncPointLifecycle: SyncPointLifecycle,
  private val syncPointRepository: SyncPointRepository,
  private val komgaSyncTokenGenerator: KomgaSyncTokenGenerator,
  private val komgaProperties: KomgaProperties,
  private val koboDtoRepository: KoboDtoRepository,
  private val mapper: ObjectMapper,
  private val commonBookController: CommonBookController,
) {
  @GetMapping("ping")
  fun ping() = "pong"

  @GetMapping("v1/initialization")
  fun initialization(): ResponseEntity<ResourcesDto> {
    val resources =
      try {
        koboProxy.proxyCurrentRequest().body?.get("Resources")
      } catch (e: Exception) {
        logger.warn { "Failed to get response from Kobo /v1/initialization, fallback to noproxy" }
        null
      } ?: koboProxy.nativeKoboResources

    return ResponseEntity.ok()
      .header("x-kobo-apitoken", "e30=")
      .body(ResourcesDto(resources))
  }

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
      accessToken = RandomStringUtils.randomAlphanumeric(24),
      refreshToken = RandomStringUtils.randomAlphanumeric(24),
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

  @GetMapping("v1/library/sync")
  fun syncLibrary(
    @AuthenticationPrincipal principal: KomgaPrincipal,
    @PathVariable authToken: String,
  ): ResponseEntity<Collection<SyncResultDto>> {
    val syncToken = komgaSyncTokenGenerator.fromRequestHeaders(getCurrentRequest()) ?: KomgaSyncToken()

    // find the ongoing sync point, else create one
    val toSyncPoint =
      getSyncPointVerified(syncToken.ongoingSyncPointId, principal.user.id)
        ?: syncPointLifecycle.createSyncPoint(principal.user, null) // TODO: for now we sync all libraries

    // find the last successful sync, if any
    val fromSyncPoint = getSyncPointVerified(syncToken.lastSuccessfulSyncPointId, principal.user.id)

    var shouldContinueSync: Boolean
    val syncResult: Collection<SyncResultDto> =
      if (fromSyncPoint != null) {
        // find books added/changed/removed and map to DTO
        var maxRemainingCount = komgaProperties.kobo.syncItemLimit

        val booksAdded =
          syncPointLifecycle.takeBooksAdded(fromSyncPoint.id, toSyncPoint.id, Pageable.ofSize(maxRemainingCount)).also {
            maxRemainingCount -= it.size
            shouldContinueSync = it.hasNext()
          }

        val booksChanged =
          if (booksAdded.isLast && maxRemainingCount > 0)
            syncPointLifecycle.takeBooksChanged(fromSyncPoint.id, toSyncPoint.id, Pageable.ofSize(maxRemainingCount)).also {
              maxRemainingCount -= it.size
              shouldContinueSync = shouldContinueSync || it.hasNext()
            }
          else
            Page.empty()

        val booksRemoved =
          if (booksChanged.isLast && maxRemainingCount > 0)
            syncPointLifecycle.takeBooksRemoved(fromSyncPoint.id, toSyncPoint.id, Pageable.ofSize(maxRemainingCount)).also {
              maxRemainingCount -= it.size
              shouldContinueSync = shouldContinueSync || it.hasNext()
            }
          else
            Page.empty()

        val metadata = koboDtoRepository.findBookMetadataByIds((booksAdded.content + booksChanged.content + booksRemoved.content).map { it.bookId }, getDownloadUrlBuilder(authToken)).associateBy { it.entitlementId }
        buildList {
          addAll(
            booksAdded.content.map {
              NewEntitlementDto(
                newEntitlement = it.toBookEntitlementDto(false),
                bookMetadata = metadata[it.bookId]!!,
              )
            },
          )
          addAll(
            booksChanged.content.map {
              ChangedEntitlementDto(
                changedEntitlement = it.toBookEntitlementDto(false),
                bookMetadata = metadata[it.bookId]!!,
              )
            },
          )
          addAll(
            booksAdded.content.map {
              ChangedEntitlementDto(
                changedEntitlement = it.toBookEntitlementDto(true),
                bookMetadata = metadata[it.bookId]!!,
              )
            },
          )
        }
      } else {
        // no starting point, sync everything
        val books = syncPointLifecycle.takeBooks(toSyncPoint.id, Pageable.ofSize(komgaProperties.kobo.syncItemLimit))
        shouldContinueSync = books.hasNext()

        val metadata = koboDtoRepository.findBookMetadataByIds(books.content.map { it.bookId }, getDownloadUrlBuilder(authToken)).associateBy { it.entitlementId }
        books.content.map {
          NewEntitlementDto(
            newEntitlement = it.toBookEntitlementDto(false),
            bookMetadata = metadata[it.bookId]!!,
          )
        }
      }

    // update synctoken to send back to Kobo
    val syncTokenUpdated =
      if (shouldContinueSync) {
        syncToken.copy(ongoingSyncPointId = toSyncPoint.id)
      } else {
        // cleanup old syncpoint if it exists
        fromSyncPoint?.let { syncPointRepository.deleteOne(it.id) }

        syncToken.copy(ongoingSyncPointId = null, lastSuccessfulSyncPointId = toSyncPoint.id)
      }

    // TODO: merge kobo store response
    // return koboProxy.proxyCurrentRequest(includeSyncToken = true)

    return ResponseEntity
      .ok()
      .headers {
        if (shouldContinueSync) it.set(X_KOBO_SYNC, "continue")
        it.set(X_KOBO_SYNCTOKEN, komgaSyncTokenGenerator.toBase64(syncTokenUpdated))
      }
      .body(syncResult)
  }

  @GetMapping(
    value = ["v1/books/{bookId}/file"],
    produces = [MediaType.APPLICATION_OCTET_STREAM_VALUE],
  )
  fun getBookFile(
    @AuthenticationPrincipal principal: KomgaPrincipal,
    @PathVariable bookId: String,
  ): ResponseEntity<StreamingResponseBody> = commonBookController.getBookFileInternal(principal, bookId)

  @RequestMapping(
    value = ["{*path}"],
    method = [RequestMethod.GET, RequestMethod.PUT, RequestMethod.POST, RequestMethod.DELETE, RequestMethod.OPTIONS, RequestMethod.HEAD, RequestMethod.PATCH],
  )
  fun catchAll(
    @RequestBody body: Any?,
  ): ResponseEntity<JsonNode> {
    return if (koboProxy.isEnabled())
      koboProxy.proxyCurrentRequest(body)
    else
      ResponseEntity.ok().body(mapper.createObjectNode())
  }

  private fun getDownloadUrlBuilder(token: String): UriBuilder =
    ServletUriComponentsBuilder.fromCurrentContextPath().pathSegment("kobo", token, "v1", "books", "{bookId}", "file")

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
}
