package org.gotson.komga.interfaces.api.opds.v2

import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import org.gotson.komga.domain.model.BookSearchWithReadProgress
import org.gotson.komga.domain.model.KomgaUser
import org.gotson.komga.domain.model.Library
import org.gotson.komga.domain.model.Media
import org.gotson.komga.domain.model.ROLE_PAGE_STREAMING
import org.gotson.komga.domain.model.ReadList
import org.gotson.komga.domain.model.ReadStatus
import org.gotson.komga.domain.model.SeriesCollection
import org.gotson.komga.domain.model.SeriesSearchWithReadProgress
import org.gotson.komga.domain.persistence.LibraryRepository
import org.gotson.komga.domain.persistence.ReadListRepository
import org.gotson.komga.domain.persistence.ReferentialRepository
import org.gotson.komga.domain.persistence.SeriesCollectionRepository
import org.gotson.komga.infrastructure.security.KomgaPrincipal
import org.gotson.komga.infrastructure.swagger.PageAsQueryParam
import org.gotson.komga.interfaces.api.CommonBookController
import org.gotson.komga.interfaces.api.ContentRestrictionChecker
import org.gotson.komga.interfaces.api.OpdsGenerator
import org.gotson.komga.interfaces.api.dto.MEDIATYPE_OPDS_AUTHENTICATION_JSON_VALUE
import org.gotson.komga.interfaces.api.dto.MEDIATYPE_OPDS_JSON_VALUE
import org.gotson.komga.interfaces.api.dto.MEDIATYPE_OPDS_PUBLICATION_JSON_VALUE
import org.gotson.komga.interfaces.api.dto.OpdsLinkRel
import org.gotson.komga.interfaces.api.dto.WPLinkDto
import org.gotson.komga.interfaces.api.dto.WPPublicationDto
import org.gotson.komga.interfaces.api.opds.v2.dto.FacetDto
import org.gotson.komga.interfaces.api.opds.v2.dto.FeedDto
import org.gotson.komga.interfaces.api.opds.v2.dto.FeedGroupDto
import org.gotson.komga.interfaces.api.opds.v2.dto.FeedMetadataDto
import org.gotson.komga.interfaces.api.persistence.BookDtoRepository
import org.gotson.komga.interfaces.api.persistence.SeriesDtoRepository
import org.gotson.komga.interfaces.api.rest.dto.SeriesDto
import org.gotson.komga.language.toZonedDateTime
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.context.request.ServletWebRequest
import org.springframework.web.server.ResponseStatusException
import org.springframework.web.servlet.support.ServletUriComponentsBuilder
import org.springframework.web.util.UriComponentsBuilder
import java.time.ZoneId
import java.time.ZonedDateTime

private const val ROUTE_CATALOG = "catalog"
const val ROUTE_AUTH = "auth"

private const val RECOMMENDED_ITEMS_NUMBER = 5

@RestController
@RequestMapping(value = ["/opds/v2/"], produces = [MEDIATYPE_OPDS_JSON_VALUE])
class Opds2Controller(
  private val libraryRepository: LibraryRepository,
  private val collectionRepository: SeriesCollectionRepository,
  private val readListRepository: ReadListRepository,
  private val seriesDtoRepository: SeriesDtoRepository,
  private val bookDtoRepository: BookDtoRepository,
  private val referentialRepository: ReferentialRepository,
  private val commonBookController: CommonBookController,
  private val opdsGenerator: OpdsGenerator,
  private val contentRestrictionChecker: ContentRestrictionChecker,
) {
  private fun linkStart() =
    WPLinkDto(
      title = "Home",
      rel = OpdsLinkRel.START,
      type = MEDIATYPE_OPDS_JSON_VALUE,
      href = uriBuilder(ROUTE_CATALOG).toUriString(),
    )

  private fun linkSearch() =
    WPLinkDto(
      title = "Search",
      rel = OpdsLinkRel.SEARCH,
      type = MEDIATYPE_OPDS_JSON_VALUE,
      href = uriBuilder("search").toUriString() + "{?query}",
      templated = true,
    )

  private fun uriBuilder(path: String) = ServletUriComponentsBuilder.fromCurrentContextPath().pathSegment("opds", "v2").path(path)

  private fun linkPage(
    uriBuilder: UriComponentsBuilder,
    page: Page<*>,
  ): List<WPLinkDto> {
    return listOfNotNull(
      if (!page.isFirst)
        WPLinkDto(
          rel = OpdsLinkRel.PREVIOUS,
          href = uriBuilder.cloneBuilder().queryParam("page", page.pageable.previousOrFirst().pageNumber).toUriString(),
        )
      else
        null,
      if (!page.isLast)
        WPLinkDto(
          rel = OpdsLinkRel.NEXT,
          href = uriBuilder.cloneBuilder().queryParam("page", page.pageable.next().pageNumber).toUriString(),
        )
      else
        null,
    )
  }

  private fun linkSelf(
    path: String,
    type: String? = null,
  ) = linkSelf(uriBuilder(path), type)

  private fun linkSelf(
    uriBuilder: UriComponentsBuilder,
    type: String? = null,
  ) = WPLinkDto(
    rel = OpdsLinkRel.SELF,
    href = uriBuilder.toUriString(),
    type = type,
  )

  private fun getLibrariesFeedGroup(
    principal: KomgaPrincipal,
  ): FeedGroupDto {
    val libraries =
      if (principal.user.sharedAllLibraries) {
        libraryRepository.findAll()
      } else {
        libraryRepository.findAllByIds(principal.user.sharedLibrariesIds)
      }
    return FeedGroupDto(
      metadata =
        FeedMetadataDto(
          title = "Libraries",
        ),
      links = listOf(linkSelf("libraries")),
      navigation = libraries.map { it.toWPLinkDto() },
    )
  }

  private fun getLibraryNavigation(
    user: KomgaUser,
    libraryId: String?,
  ): List<WPLinkDto> {
    val uriBuilder = uriBuilder("libraries${if (libraryId != null) "/$libraryId" else ""}")

    val collections = collectionRepository.findAll(libraryId?.let { listOf(it) }, restrictions = user.restrictions, pageable = Pageable.ofSize(1))
    val readLists = readListRepository.findAll(libraryId?.let { listOf(it) }, restrictions = user.restrictions, pageable = Pageable.ofSize(1))

    return listOfNotNull(
      WPLinkDto("Recommended", OpdsLinkRel.SUBSECTION, href = uriBuilder.toUriString(), type = MEDIATYPE_OPDS_JSON_VALUE),
      WPLinkDto("Browse", OpdsLinkRel.SUBSECTION, href = uriBuilder.cloneBuilder().pathSegment("browse").toUriString(), type = MEDIATYPE_OPDS_JSON_VALUE),
      if (collections.isEmpty)
        null
      else
        WPLinkDto("Collections", OpdsLinkRel.SUBSECTION, href = uriBuilder.cloneBuilder().pathSegment("collections").toUriString(), type = MEDIATYPE_OPDS_JSON_VALUE),
      if (readLists.isEmpty)
        null
      else
        WPLinkDto("Read lists", OpdsLinkRel.SUBSECTION, href = uriBuilder.cloneBuilder().pathSegment("readlists").toUriString(), type = MEDIATYPE_OPDS_JSON_VALUE),
    )
  }

  @GetMapping(value = [ROUTE_CATALOG, "libraries", "libraries/{id}"])
  fun getLibrariesRecommended(
    @AuthenticationPrincipal principal: KomgaPrincipal,
    @PathVariable(name = "id", required = false) libraryId: String?,
  ): FeedDto {
    val (library, authorizedLibraryIds) = checkLibraryAccess(libraryId, principal)

    val keepReading =
      bookDtoRepository.findAll(
        BookSearchWithReadProgress(
          libraryIds = authorizedLibraryIds,
          readStatus = setOf(ReadStatus.IN_PROGRESS),
          mediaStatus = setOf(Media.Status.READY),
          deleted = false,
        ),
        principal.user.id,
        PageRequest.of(0, RECOMMENDED_ITEMS_NUMBER, Sort.by(Sort.Order.desc("readProgress.readDate"))),
        principal.user.restrictions,
      ).map { opdsGenerator.toOpdsPublicationDto(it) }

    val onDeck =
      bookDtoRepository.findAllOnDeck(
        principal.user.id,
        authorizedLibraryIds,
        Pageable.ofSize(RECOMMENDED_ITEMS_NUMBER),
        principal.user.restrictions,
      ).map { opdsGenerator.toOpdsPublicationDto(it) }

    val latestBooks =
      bookDtoRepository.findAll(
        BookSearchWithReadProgress(
          libraryIds = authorizedLibraryIds,
          mediaStatus = setOf(Media.Status.READY),
          deleted = false,
        ),
        principal.user.id,
        PageRequest.of(0, RECOMMENDED_ITEMS_NUMBER, Sort.by(Sort.Order.desc("createdDate"))),
        principal.user.restrictions,
      ).map { opdsGenerator.toOpdsPublicationDto(it) }

    val latestSeries =
      seriesDtoRepository.findAll(
        SeriesSearchWithReadProgress(
          libraryIds = authorizedLibraryIds,
          deleted = false,
          oneshot = false,
        ),
        principal.user.id,
        PageRequest.of(0, RECOMMENDED_ITEMS_NUMBER, Sort.by(Sort.Order.desc("lastModified"))),
        principal.user.restrictions,
      ).map { it.toWPLinkDto() }

    val uriBuilder = uriBuilder("libraries${if (library != null) "/${library.id}" else ""}")

    return FeedDto(
      metadata =
        FeedMetadataDto(
          title = (library?.name ?: "All libraries") + " - Recommended",
          modified = library?.lastModifiedDate?.atZone(ZoneId.systemDefault()) ?: ZonedDateTime.now(),
        ),
      links =
        listOf(
          linkSelf(uriBuilder),
          linkStart(),
          linkSearch(),
        ),
      navigation = getLibraryNavigation(principal.user, libraryId),
      groups =
        listOfNotNull(
          if (library == null) getLibrariesFeedGroup(principal) else null,
          if (!keepReading.isEmpty)
            FeedGroupDto(
              FeedMetadataDto("Keep Reading", page = keepReading),
              links = listOf(WPLinkDto("Keep Reading", OpdsLinkRel.SELF, uriBuilder.cloneBuilder().pathSegment("keep-reading").toUriString(), MEDIATYPE_OPDS_JSON_VALUE)),
              publications = keepReading.content,
            )
          else
            null,
          if (!onDeck.isEmpty)
            FeedGroupDto(
              FeedMetadataDto("On Deck", page = onDeck),
              links = listOf(WPLinkDto("On Deck", OpdsLinkRel.SELF, uriBuilder.cloneBuilder().pathSegment("on-deck").toUriString(), MEDIATYPE_OPDS_JSON_VALUE)),
              publications = onDeck.content,
            )
          else
            null,
          if (!latestBooks.isEmpty)
            FeedGroupDto(
              FeedMetadataDto("Latest Books", page = latestBooks),
              links = listOf(WPLinkDto("Latest Books", OpdsLinkRel.SELF, uriBuilder.cloneBuilder().pathSegment("books", "latest").toUriString(), MEDIATYPE_OPDS_JSON_VALUE)),
              publications = latestBooks.content,
            )
          else
            null,
          if (!latestSeries.isEmpty)
            FeedGroupDto(
              FeedMetadataDto("Latest Series", page = latestSeries),
              links = listOf(WPLinkDto("Latest Series", OpdsLinkRel.SELF, uriBuilder.cloneBuilder().pathSegment("series", "latest").toUriString(), MEDIATYPE_OPDS_JSON_VALUE)),
              navigation = latestSeries.content,
            )
          else
            null,
        ),
    )
  }

  @PageAsQueryParam
  @GetMapping(value = ["libraries/keep-reading", "libraries/{id}/keep-reading"])
  fun getKeepReading(
    @AuthenticationPrincipal principal: KomgaPrincipal,
    @PathVariable(name = "id", required = false) libraryId: String?,
    @Parameter(hidden = true) page: Pageable,
  ): FeedDto {
    val (library, authorizedLibraryIds) = checkLibraryAccess(libraryId, principal)

    val entries =
      bookDtoRepository.findAll(
        BookSearchWithReadProgress(
          libraryIds = authorizedLibraryIds,
          readStatus = setOf(ReadStatus.IN_PROGRESS),
          mediaStatus = setOf(Media.Status.READY),
          deleted = false,
        ),
        principal.user.id,
        PageRequest.of(page.pageNumber, page.pageSize, Sort.by(Sort.Order.desc("readProgress.readDate"))),
        principal.user.restrictions,
      ).map { opdsGenerator.toOpdsPublicationDto(it) }

    val uriBuilder = uriBuilder("libraries${if (library != null) "/${library.id}" else ""}/keep-reading")

    return FeedDto(
      metadata =
        FeedMetadataDto(
          title = (library?.name ?: "All libraries") + " - Keep Reading",
          modified = library?.lastModifiedDate?.atZone(ZoneId.systemDefault()) ?: ZonedDateTime.now(),
          page = entries,
        ),
      links =
        listOf(
          linkSelf(uriBuilder),
          linkStart(),
          linkSearch(),
          *linkPage(uriBuilder, entries).toTypedArray(),
        ),
      publications = entries.content,
    )
  }

  @PageAsQueryParam
  @GetMapping(value = ["libraries/on-deck", "libraries/{id}/on-deck"])
  fun getOnDeck(
    @AuthenticationPrincipal principal: KomgaPrincipal,
    @PathVariable(name = "id", required = false) libraryId: String?,
    @Parameter(hidden = true) page: Pageable,
  ): FeedDto {
    val (library, authorizedLibraryIds) = checkLibraryAccess(libraryId, principal)

    val entries =
      bookDtoRepository.findAllOnDeck(
        principal.user.id,
        authorizedLibraryIds,
        page,
        principal.user.restrictions,
      ).map { opdsGenerator.toOpdsPublicationDto(it) }

    val uriBuilder = uriBuilder("libraries${if (library != null) "/${library.id}" else ""}/on-deck")

    return FeedDto(
      metadata =
        FeedMetadataDto(
          title = (library?.name ?: "All libraries") + " - On Deck",
          modified = library?.lastModifiedDate?.atZone(ZoneId.systemDefault()) ?: ZonedDateTime.now(),
          page = entries,
        ),
      links =
        listOf(
          linkSelf(uriBuilder),
          linkStart(),
          linkSearch(),
          *linkPage(uriBuilder, entries).toTypedArray(),
        ),
      publications = entries.content,
    )
  }

  @PageAsQueryParam
  @GetMapping(value = ["libraries/books/latest", "libraries/{id}/books/latest"])
  fun getLatestBooks(
    @AuthenticationPrincipal principal: KomgaPrincipal,
    @PathVariable(name = "id", required = false) libraryId: String?,
    @Parameter(hidden = true) page: Pageable,
  ): FeedDto {
    val (library, authorizedLibraryIds) = checkLibraryAccess(libraryId, principal)

    val entries =
      bookDtoRepository.findAll(
        BookSearchWithReadProgress(
          libraryIds = authorizedLibraryIds,
          mediaStatus = setOf(Media.Status.READY),
          deleted = false,
        ),
        principal.user.id,
        PageRequest.of(page.pageNumber, page.pageSize, Sort.by(Sort.Order.desc("createdDate"))),
        principal.user.restrictions,
      ).map { opdsGenerator.toOpdsPublicationDto(it) }

    val uriBuilder = uriBuilder("libraries${if (library != null) "/${library.id}" else ""}/books/latest")

    return FeedDto(
      metadata =
        FeedMetadataDto(
          title = (library?.name ?: "All libraries") + " - Latest Books",
          modified = library?.lastModifiedDate?.atZone(ZoneId.systemDefault()) ?: ZonedDateTime.now(),
          page = entries,
        ),
      links =
        listOf(
          linkSelf(uriBuilder),
          linkStart(),
          linkSearch(),
          *linkPage(uriBuilder, entries).toTypedArray(),
        ),
      publications = entries.content,
    )
  }

  @PageAsQueryParam
  @GetMapping(value = ["libraries/series/latest", "libraries/{id}/series/latest"])
  fun getLatestSeries(
    @AuthenticationPrincipal principal: KomgaPrincipal,
    @PathVariable(name = "id", required = false) libraryId: String?,
    @Parameter(hidden = true) page: Pageable,
  ): FeedDto {
    val (library, authorizedLibraryIds) = checkLibraryAccess(libraryId, principal)

    val entries =
      seriesDtoRepository.findAll(
        SeriesSearchWithReadProgress(
          libraryIds = authorizedLibraryIds,
          deleted = false,
          oneshot = false,
        ),
        principal.user.id,
        PageRequest.of(page.pageNumber, page.pageSize, Sort.by(Sort.Order.desc("lastModified"))),
        principal.user.restrictions,
      ).map { it.toWPLinkDto() }

    val uriBuilder = uriBuilder("libraries${if (library != null) "/${library.id}" else ""}/books/latest")

    return FeedDto(
      metadata =
        FeedMetadataDto(
          title = (library?.name ?: "All libraries") + " - Latest Series",
          modified = library?.lastModifiedDate?.atZone(ZoneId.systemDefault()) ?: ZonedDateTime.now(),
          page = entries,
        ),
      links =
        listOf(
          linkSelf(uriBuilder),
          linkStart(),
          linkSearch(),
          *linkPage(uriBuilder, entries).toTypedArray(),
        ),
      navigation = entries.content,
    )
  }

  @PageAsQueryParam
  @GetMapping(value = ["libraries/browse", "libraries/{id}/browse"])
  fun getLibrariesBrowse(
    @AuthenticationPrincipal principal: KomgaPrincipal,
    @PathVariable(name = "id", required = false) libraryId: String?,
    @RequestParam(name = "publisher", required = false) publishers: List<String>? = null,
    @Parameter(hidden = true) page: Pageable,
  ): FeedDto {
    val (library, authorizedLibraryIds) = checkLibraryAccess(libraryId, principal)

    val seriesSearch =
      SeriesSearchWithReadProgress(
        libraryIds = authorizedLibraryIds,
        publishers = publishers,
        deleted = false,
      )

    val pageable = PageRequest.of(page.pageNumber, page.pageSize, Sort.by(Sort.Order.asc("metadata.titleSort")))

    val entries = seriesDtoRepository.findAll(seriesSearch, principal.user.id, pageable, principal.user.restrictions).map { it.toWPLinkDto() }

    val uriBuilder = uriBuilder("libraries${if (library != null) "/${library.id}" else ""}/browse")

    val publisherLinks =
      referentialRepository.findAllPublishers(authorizedLibraryIds).map {
        WPLinkDto(
          title = it,
          href = uriBuilder.cloneBuilder().queryParam("publisher", it).toUriString(),
          type = MEDIATYPE_OPDS_JSON_VALUE,
        )
      }

    return FeedDto(
      metadata =
        FeedMetadataDto(
          title = library?.name ?: "All libraries",
          modified = library?.lastModifiedDate?.atZone(ZoneId.systemDefault()) ?: ZonedDateTime.now(),
          page = entries,
        ),
      links =
        listOf(
          linkSelf(uriBuilder),
          linkStart(),
          linkSearch(),
          *linkPage(uriBuilder, entries).toTypedArray(),
        ),
      navigation = getLibraryNavigation(principal.user, libraryId),
      groups =
        listOfNotNull(
          FeedGroupDto(FeedMetadataDto("Series"), navigation = entries.content),
          if (publisherLinks.isNotEmpty()) FeedGroupDto(FeedMetadataDto("Publisher"), navigation = publisherLinks) else null,
        ),
    )
  }

  @PageAsQueryParam
  @GetMapping(value = ["libraries/collections", "libraries/{id}/collections"])
  fun getLibrariesCollections(
    @AuthenticationPrincipal principal: KomgaPrincipal,
    @PathVariable(name = "id", required = false) libraryId: String?,
    @Parameter(hidden = true) page: Pageable,
  ): FeedDto {
    val (library, authorizedLibraryIds) = checkLibraryAccess(libraryId, principal)

    val pageable = PageRequest.of(page.pageNumber, page.pageSize, Sort.by(Sort.Order.asc("name")))
    val entries =
      collectionRepository.findAll(
        authorizedLibraryIds,
        authorizedLibraryIds,
        pageable = pageable,
      ).map { it.toWPLinkDto() }

    val uriBuilder = uriBuilder("libraries${if (library != null) "/${library.id}" else ""}/collections")

    return FeedDto(
      metadata =
        FeedMetadataDto(
          title = (library?.name ?: "All libraries") + " - Collections",
          modified = library?.lastModifiedDate?.atZone(ZoneId.systemDefault()) ?: ZonedDateTime.now(),
          page = entries,
        ),
      links =
        listOf(
          linkSelf(uriBuilder),
          linkStart(),
          linkSearch(),
          *linkPage(uriBuilder, entries).toTypedArray(),
        ),
      navigation = getLibraryNavigation(principal.user, libraryId),
      groups =
        listOfNotNull(
          FeedGroupDto(FeedMetadataDto("Collections"), navigation = entries.content),
        ),
    )
  }

  @PageAsQueryParam
  @GetMapping("collections/{id}")
  fun getOneCollection(
    @AuthenticationPrincipal principal: KomgaPrincipal,
    @PathVariable id: String,
    @Parameter(hidden = true) page: Pageable,
  ): FeedDto =
    collectionRepository.findByIdOrNull(id, principal.user.getAuthorizedLibraryIds(null))?.let { collection ->
      val sort =
        if (collection.ordered)
          Sort.by(Sort.Order.asc("collection.number"))
        else
          Sort.by(Sort.Order.asc("metadata.titleSort"))
      val pageable = PageRequest.of(page.pageNumber, page.pageSize, sort)

      val seriesSearch =
        SeriesSearchWithReadProgress(
          libraryIds = principal.user.getAuthorizedLibraryIds(null),
          deleted = false,
        )

      val entries = seriesDtoRepository.findAllByCollectionId(collection.id, seriesSearch, principal.user.id, pageable, principal.user.restrictions).map { it.toWPLinkDto() }

      val uriBuilder = uriBuilder("collections/$id")

      FeedDto(
        FeedMetadataDto(
          title = collection.name,
          modified = collection.lastModifiedDate.atZone(ZoneId.systemDefault()) ?: ZonedDateTime.now(),
          page = entries,
        ),
        links =
          listOf(
            linkSelf(uriBuilder),
            linkStart(),
            linkSearch(),
            *linkPage(uriBuilder, entries).toTypedArray(),
          ),
        navigation = entries.content,
      )
    } ?: throw ResponseStatusException(HttpStatus.NOT_FOUND)

  @PageAsQueryParam
  @GetMapping(value = ["libraries/readlists", "libraries/{id}/readlists"])
  fun getLibrariesReadLists(
    @AuthenticationPrincipal principal: KomgaPrincipal,
    @PathVariable(name = "id", required = false) libraryId: String?,
    @Parameter(hidden = true) page: Pageable,
  ): FeedDto {
    val (library, authorizedLibraryIds) = checkLibraryAccess(libraryId, principal)

    val pageable = PageRequest.of(page.pageNumber, page.pageSize, Sort.by(Sort.Order.asc("name")))
    val entries =
      readListRepository.findAll(
        authorizedLibraryIds,
        authorizedLibraryIds,
        pageable = pageable,
      ).map { it.toWPLinkDto() }

    val uriBuilder = uriBuilder("libraries${if (library != null) "/${library.id}" else ""}/readlists")

    return FeedDto(
      metadata =
        FeedMetadataDto(
          title = (library?.name ?: "All libraries") + " - Read Lists",
          modified = library?.lastModifiedDate?.atZone(ZoneId.systemDefault()) ?: ZonedDateTime.now(),
          page = entries,
        ),
      links =
        listOf(
          linkSelf(uriBuilder),
          linkStart(),
          linkSearch(),
          *linkPage(uriBuilder, entries).toTypedArray(),
        ),
      navigation = getLibraryNavigation(principal.user, libraryId),
      groups =
        listOfNotNull(
          FeedGroupDto(FeedMetadataDto("Read Lists"), navigation = entries.content),
        ),
    )
  }

  @PageAsQueryParam
  @GetMapping("readlists/{id}")
  fun getOneReadList(
    @AuthenticationPrincipal principal: KomgaPrincipal,
    @PathVariable id: String,
    @Parameter(hidden = true) page: Pageable,
  ): FeedDto =
    readListRepository.findByIdOrNull(id, principal.user.getAuthorizedLibraryIds(null))?.let { readList ->
      val sort =
        if (readList.ordered)
          Sort.by(Sort.Order.asc("readList.number"))
        else
          Sort.by(Sort.Order.asc("metadata.releaseDate"))
      val pageable = PageRequest.of(page.pageNumber, page.pageSize, sort)

      val bookSearch =
        BookSearchWithReadProgress(
          mediaStatus = setOf(Media.Status.READY),
          deleted = false,
        )

      val booksPage =
        bookDtoRepository.findAllByReadListId(
          readList.id,
          principal.user.id,
          principal.user.getAuthorizedLibraryIds(null),
          bookSearch,
          pageable,
          principal.user.restrictions,
        )

      val entries = booksPage.map { opdsGenerator.toOpdsPublicationDto(it) }

      val uriBuilder = uriBuilder("readlists/$id")

      FeedDto(
        FeedMetadataDto(
          title = readList.name,
          modified = readList.lastModifiedDate.atZone(ZoneId.systemDefault()) ?: ZonedDateTime.now(),
          page = entries,
        ),
        links =
          listOf(
            linkSelf(uriBuilder),
            linkStart(),
            linkSearch(),
            *linkPage(uriBuilder, booksPage).toTypedArray(),
          ),
        publications = entries.content,
      )
    } ?: throw ResponseStatusException(HttpStatus.NOT_FOUND)

  private fun checkLibraryAccess(
    libraryId: String?,
    principal: KomgaPrincipal,
  ): Pair<Library?, Collection<String>?> {
    val library =
      if (libraryId != null)
        libraryRepository.findByIdOrNull(libraryId)?.let { library ->
          if (!principal.user.canAccessLibrary(library)) throw ResponseStatusException(HttpStatus.FORBIDDEN)
          library
        } ?: throw ResponseStatusException(HttpStatus.NOT_FOUND)
      else
        null

    val libraryIds = principal.user.getAuthorizedLibraryIds(if (libraryId != null) listOf(libraryId) else null)
    return Pair(library, libraryIds)
  }

  @PageAsQueryParam
  @GetMapping("series/{id}")
  fun getOneSeries(
    @AuthenticationPrincipal principal: KomgaPrincipal,
    @PathVariable id: String,
    @RequestParam(name = "tag", required = false) tag: String? = null,
    @Parameter(hidden = true) page: Pageable,
  ): FeedDto =
    seriesDtoRepository.findByIdOrNull(id, "principal.user.id")?.let { series ->
      contentRestrictionChecker.checkContentRestriction(principal.user, series)

      val bookSearch =
        BookSearchWithReadProgress(
          seriesIds = listOf(id),
          mediaStatus = setOf(Media.Status.READY),
          tags = if (tag != null) listOf(tag) else null,
          deleted = false,
        )
      val pageable = PageRequest.of(page.pageNumber, page.pageSize, Sort.by(Sort.Order.asc("metadata.numberSort")))

      val entries = bookDtoRepository.findAll(bookSearch, principal.user.id, pageable, principal.user.restrictions).map { opdsGenerator.toOpdsPublicationDto(it) }

      val uriBuilder = uriBuilder("series/$id")

      val tagLinks =
        referentialRepository.findAllBookTagsBySeries(series.id, null).map {
          WPLinkDto(
            title = it,
            href = uriBuilder.cloneBuilder().queryParam("tag", it).toUriString(),
            type = MEDIATYPE_OPDS_JSON_VALUE,
            rel = if (it == tag) OpdsLinkRel.SELF else null,
          )
        }

      FeedDto(
        metadata =
          FeedMetadataDto(
            title = series.metadata.title,
            modified = series.lastModified.toZonedDateTime(),
            description = series.metadata.summary.ifBlank { series.booksMetadata.summary },
            page = entries,
          ),
        links =
          listOf(
            linkSelf(uriBuilder),
            linkStart(),
            linkSearch(),
            *linkPage(uriBuilder, entries).toTypedArray(),
          ),
        publications = entries.content,
        facets =
          listOfNotNull(
            if (tagLinks.isNotEmpty()) FacetDto(FeedMetadataDto("Tag"), tagLinks) else null,
          ),
      )
    } ?: throw ResponseStatusException(HttpStatus.NOT_FOUND)

  @GetMapping("search")
  fun getSearchResults(
    @AuthenticationPrincipal principal: KomgaPrincipal,
    @RequestParam(name = "query", required = false) query: String? = null,
  ): FeedDto {
    val pageable = PageRequest.of(0, 20, Sort.by("relevance"))

    val resultsSeries =
      seriesDtoRepository.findAll(
        SeriesSearchWithReadProgress(
          libraryIds = principal.user.getAuthorizedLibraryIds(null),
          searchTerm = query,
          oneshot = false,
          deleted = false,
        ),
        principal.user.id,
        pageable,
        principal.user.restrictions,
      ).map { it.toWPLinkDto() }

    val resultsBooks =
      bookDtoRepository.findAll(
        BookSearchWithReadProgress(
          libraryIds = principal.user.getAuthorizedLibraryIds(null),
          searchTerm = query,
          deleted = false,
        ),
        principal.user.id,
        pageable,
        principal.user.restrictions,
      ).map { opdsGenerator.toOpdsPublicationDto(it) }

    val resultsCollections =
      collectionRepository.findAll(
        principal.user.getAuthorizedLibraryIds(null),
        principal.user.getAuthorizedLibraryIds(null),
        query,
        pageable,
        principal.user.restrictions,
      ).map { it.toWPLinkDto() }

    val resultsReadLists =
      readListRepository.findAll(
        principal.user.getAuthorizedLibraryIds(null),
        principal.user.getAuthorizedLibraryIds(null),
        query,
        pageable,
        principal.user.restrictions,
      ).map { it.toWPLinkDto() }

    return FeedDto(
      metadata =
        FeedMetadataDto(
          title = "Search results",
          modified = ZonedDateTime.now(),
        ),
      links =
        listOf(
          linkStart(),
          linkSearch(),
        ),
      groups =
        listOfNotNull(
          if (!resultsSeries.isEmpty) FeedGroupDto(FeedMetadataDto("Series"), navigation = resultsSeries.content) else null,
          if (!resultsBooks.isEmpty) FeedGroupDto(FeedMetadataDto("Books"), publications = resultsBooks.content) else null,
          if (!resultsCollections.isEmpty) FeedGroupDto(FeedMetadataDto("Collections"), navigation = resultsCollections.content) else null,
          if (!resultsReadLists.isEmpty) FeedGroupDto(FeedMetadataDto("Read Lists"), navigation = resultsReadLists.content) else null,
        ),
    )
  }

  @GetMapping(value = [ROUTE_AUTH], produces = [MEDIATYPE_OPDS_AUTHENTICATION_JSON_VALUE])
  fun getAuthDocument() = opdsGenerator.generateOpdsAuthDocument()

  @ApiResponse(content = [Content(mediaType = "image/*", schema = Schema(type = "string", format = "binary"))])
  @GetMapping(
    value = ["books/{bookId}/pages/{pageNumber}"],
    produces = [MediaType.ALL_VALUE],
  )
  @PreAuthorize("hasRole('$ROLE_PAGE_STREAMING')")
  fun getBookPage(
    @AuthenticationPrincipal principal: KomgaPrincipal,
    request: ServletWebRequest,
    @PathVariable bookId: String,
    @PathVariable pageNumber: Int,
    @Parameter(
      description = "Convert the image to the provided format.",
      schema = Schema(allowableValues = ["jpeg", "png"]),
    )
    @RequestParam(value = "convert", required = false)
    convertTo: String?,
  ): ResponseEntity<ByteArray> =
    commonBookController.getBookPageInternal(bookId, pageNumber, convertTo, request, principal, null)

  @GetMapping(
    value = ["books/{bookId}/manifest"],
    produces = [MEDIATYPE_OPDS_PUBLICATION_JSON_VALUE],
  )
  fun getWebPubManifest(
    @AuthenticationPrincipal principal: KomgaPrincipal,
    @PathVariable bookId: String,
  ): WPPublicationDto =
    commonBookController.getWebPubManifestInternal(principal, bookId, opdsGenerator)

  @GetMapping(
    value = ["books/{bookId}/manifest/epub"],
    produces = [MEDIATYPE_OPDS_PUBLICATION_JSON_VALUE],
  )
  fun getWebPubManifestEpub(
    @AuthenticationPrincipal principal: KomgaPrincipal,
    @PathVariable bookId: String,
  ): WPPublicationDto =
    commonBookController.getWebPubManifestEpubInternal(principal, bookId, opdsGenerator)

  @GetMapping(
    value = ["books/{bookId}/manifest/pdf"],
    produces = [MEDIATYPE_OPDS_PUBLICATION_JSON_VALUE],
  )
  fun getWebPubManifestPdf(
    @AuthenticationPrincipal principal: KomgaPrincipal,
    @PathVariable bookId: String,
  ): WPPublicationDto =
    commonBookController.getWebPubManifestPdfInternal(principal, bookId, opdsGenerator)

  @GetMapping(
    value = ["books/{bookId}/manifest/divina"],
    produces = [MEDIATYPE_OPDS_PUBLICATION_JSON_VALUE],
  )
  fun getWebPubManifestDivina(
    @AuthenticationPrincipal principal: KomgaPrincipal,
    @PathVariable bookId: String,
  ): WPPublicationDto =
    commonBookController.getWebPubManifestDivinaInternal(principal, bookId, opdsGenerator)

  private fun Library.toWPLinkDto(): WPLinkDto =
    WPLinkDto(
      title = name,
      href = uriBuilder("libraries/$id").toUriString(),
      type = MEDIATYPE_OPDS_JSON_VALUE,
    )

  private fun SeriesDto.toWPLinkDto(): WPLinkDto =
    WPLinkDto(
      title = metadata.title,
      href = uriBuilder("series/$id").toUriString(),
      type = MEDIATYPE_OPDS_JSON_VALUE,
    )

  private fun SeriesCollection.toWPLinkDto(): WPLinkDto =
    WPLinkDto(
      title = name,
      href = uriBuilder("collections/$id").toUriString(),
      type = MEDIATYPE_OPDS_JSON_VALUE,
    )

  private fun ReadList.toWPLinkDto(): WPLinkDto =
    WPLinkDto(
      title = name,
      href = uriBuilder("readlists/$id").toUriString(),
      type = MEDIATYPE_OPDS_JSON_VALUE,
    )
}
