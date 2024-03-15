package org.gotson.komga.interfaces.api.opds.v1

import io.github.oshai.kotlinlogging.KotlinLogging
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import org.apache.commons.io.FilenameUtils
import org.gotson.komga.domain.model.BookSearchWithReadProgress
import org.gotson.komga.domain.model.Library
import org.gotson.komga.domain.model.Media
import org.gotson.komga.domain.model.MediaProfile
import org.gotson.komga.domain.model.ROLE_PAGE_STREAMING
import org.gotson.komga.domain.model.ReadList
import org.gotson.komga.domain.model.ReadStatus
import org.gotson.komga.domain.model.SeriesCollection
import org.gotson.komga.domain.model.SeriesSearchWithReadProgress
import org.gotson.komga.domain.model.ThumbnailBook
import org.gotson.komga.domain.persistence.LibraryRepository
import org.gotson.komga.domain.persistence.MediaRepository
import org.gotson.komga.domain.persistence.ReadListRepository
import org.gotson.komga.domain.persistence.ReferentialRepository
import org.gotson.komga.domain.persistence.SeriesCollectionRepository
import org.gotson.komga.domain.service.BookLifecycle
import org.gotson.komga.infrastructure.configuration.KomgaSettingsProvider
import org.gotson.komga.infrastructure.image.ImageType
import org.gotson.komga.infrastructure.security.KomgaPrincipal
import org.gotson.komga.infrastructure.swagger.PageAsQueryParam
import org.gotson.komga.interfaces.api.CommonBookController
import org.gotson.komga.interfaces.api.ContentRestrictionChecker
import org.gotson.komga.interfaces.api.dto.MEDIATYPE_OPDS_JSON_VALUE
import org.gotson.komga.interfaces.api.dto.OpdsLinkRel
import org.gotson.komga.interfaces.api.opds.v1.dto.OpdsAuthor
import org.gotson.komga.interfaces.api.opds.v1.dto.OpdsEntryAcquisition
import org.gotson.komga.interfaces.api.opds.v1.dto.OpdsEntryNavigation
import org.gotson.komga.interfaces.api.opds.v1.dto.OpdsFeed
import org.gotson.komga.interfaces.api.opds.v1.dto.OpdsFeedAcquisition
import org.gotson.komga.interfaces.api.opds.v1.dto.OpdsFeedNavigation
import org.gotson.komga.interfaces.api.opds.v1.dto.OpdsLink
import org.gotson.komga.interfaces.api.opds.v1.dto.OpdsLinkFeedNavigation
import org.gotson.komga.interfaces.api.opds.v1.dto.OpdsLinkFileAcquisition
import org.gotson.komga.interfaces.api.opds.v1.dto.OpdsLinkImage
import org.gotson.komga.interfaces.api.opds.v1.dto.OpdsLinkImageThumbnail
import org.gotson.komga.interfaces.api.opds.v1.dto.OpdsLinkPageStreaming
import org.gotson.komga.interfaces.api.opds.v1.dto.OpdsLinkSearch
import org.gotson.komga.interfaces.api.opds.v1.dto.OpenSearchDescription
import org.gotson.komga.interfaces.api.persistence.BookDtoRepository
import org.gotson.komga.interfaces.api.persistence.SeriesDtoRepository
import org.gotson.komga.interfaces.api.rest.dto.BookDto
import org.gotson.komga.interfaces.api.rest.dto.SeriesDto
import org.gotson.komga.language.toZonedDateTime
import org.springframework.beans.factory.annotation.Qualifier
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
import org.springframework.web.util.UriUtils
import java.net.URI
import java.nio.charset.StandardCharsets
import java.text.DecimalFormat
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.Optional

private val logger = KotlinLogging.logger {}

private const val ROUTE_BASE = "/opds/v1.2/"
private const val ROUTE_CATALOG = "catalog"
private const val ROUTE_ON_DECK = "ondeck"
private const val ROUTE_KEEP_READING = "keep-reading"
private const val ROUTE_SERIES_ALL = "series"
private const val ROUTE_SERIES_LATEST = "series/latest"
private const val ROUTE_BOOKS_LATEST = "books/latest"
private const val ROUTE_LIBRARIES_ALL = "libraries"
private const val ROUTE_COLLECTIONS_ALL = "collections"
private const val ROUTE_READLISTS_ALL = "readlists"
private const val ROUTE_PUBLISHERS_ALL = "publishers"
private const val ROUTE_SEARCH = "search"

private const val ID_ON_DECK = "ondeck"
private const val ID_KEEP_READING = "keepReading"
private const val ID_SERIES_ALL = "allSeries"
private const val ID_SERIES_LATEST = "latestSeries"
private const val ID_BOOKS_LATEST = "latestBooks"
private const val ID_LIBRARIES_ALL = "allLibraries"
private const val ID_COLLECTIONS_ALL = "allCollections"
private const val ID_READLISTS_ALL = "allReadLists"
private const val ID_PUBLISHERS_ALL = "allPublishers"

@RestController
@RequestMapping(value = [ROUTE_BASE], produces = [MediaType.APPLICATION_ATOM_XML_VALUE, MediaType.APPLICATION_XML_VALUE, MediaType.TEXT_XML_VALUE])
class OpdsController(
  private val libraryRepository: LibraryRepository,
  private val collectionRepository: SeriesCollectionRepository,
  private val readListRepository: ReadListRepository,
  private val seriesDtoRepository: SeriesDtoRepository,
  private val bookDtoRepository: BookDtoRepository,
  private val mediaRepository: MediaRepository,
  private val referentialRepository: ReferentialRepository,
  private val bookLifecycle: BookLifecycle,
  private val commonBookController: CommonBookController,
  private val komgaSettingsProvider: KomgaSettingsProvider,
  private val contentRestrictionChecker: ContentRestrictionChecker,
  @Qualifier("pdfImageType")
  private val pdfImageType: ImageType,
) {
  private val komgaAuthor = OpdsAuthor("Komga", URI("https://github.com/gotson/komga"))

  private val decimalFormat = DecimalFormat("0.#")

  private val opdsPseSupportedFormats = listOf("image/jpeg", "image/png", "image/gif")

  private fun linkStart() = OpdsLinkFeedNavigation(OpdsLinkRel.START, uriBuilder(ROUTE_CATALOG).toUriString())

  private fun uriBuilder(path: String) =
    ServletUriComponentsBuilder.fromCurrentContextPath().pathSegment("opds", "v1.2").path(path)

  private fun <T> linkPage(
    uriBuilder: UriComponentsBuilder,
    page: Page<T>,
  ): List<OpdsLink> {
    return listOfNotNull(
      if (!page.isFirst)
        OpdsLinkFeedNavigation(
          OpdsLinkRel.PREVIOUS,
          uriBuilder.cloneBuilder().queryParam("page", page.pageable.previousOrFirst().pageNumber).toUriString(),
        )
      else
        null,
      if (!page.isLast)
        OpdsLinkFeedNavigation(
          OpdsLinkRel.NEXT,
          uriBuilder.cloneBuilder().queryParam("page", page.pageable.next().pageNumber).toUriString(),
        )
      else
        null,
    )
  }

  @GetMapping(ROUTE_CATALOG)
  fun getCatalog(): OpdsFeed =
    OpdsFeedNavigation(
      id = "root",
      title = "Komga OPDS catalog",
      updated = ZonedDateTime.now(),
      author = komgaAuthor,
      links =
        listOf(
          OpdsLinkFeedNavigation(OpdsLinkRel.SELF, uriBuilder(ROUTE_CATALOG).toUriString()),
          linkStart(),
          OpdsLinkSearch(uriBuilder(ROUTE_SEARCH).toUriString()),
          OpdsLink(MEDIATYPE_OPDS_JSON_VALUE, "alternate", href = ServletUriComponentsBuilder.fromCurrentContextPath().pathSegment("opds", "v2", "catalog").toUriString()),
        ),
      entries =
        listOf(
          OpdsEntryNavigation(
            title = "Keep Reading",
            updated = ZonedDateTime.now(),
            id = ID_KEEP_READING,
            content = "Continue reading your in progress books",
            link = OpdsLinkFeedNavigation(OpdsLinkRel.SUBSECTION, uriBuilder(ROUTE_KEEP_READING).toUriString()),
          ),
          OpdsEntryNavigation(
            title = "On Deck",
            updated = ZonedDateTime.now(),
            id = ID_ON_DECK,
            content = "Browse what to read next",
            link = OpdsLinkFeedNavigation(OpdsLinkRel.SUBSECTION, uriBuilder(ROUTE_ON_DECK).toUriString()),
          ),
          OpdsEntryNavigation(
            title = "All series",
            updated = ZonedDateTime.now(),
            id = ID_SERIES_ALL,
            content = "Browse by series",
            link = OpdsLinkFeedNavigation(OpdsLinkRel.SUBSECTION, uriBuilder(ROUTE_SERIES_ALL).toUriString()),
          ),
          OpdsEntryNavigation(
            title = "Latest series",
            updated = ZonedDateTime.now(),
            id = ID_SERIES_LATEST,
            content = "Browse latest series",
            link = OpdsLinkFeedNavigation(OpdsLinkRel.SUBSECTION, uriBuilder(ROUTE_SERIES_LATEST).toUriString()),
          ),
          OpdsEntryNavigation(
            title = "Latest books",
            updated = ZonedDateTime.now(),
            id = ID_BOOKS_LATEST,
            content = "Browse latest books",
            link = OpdsLinkFeedNavigation(OpdsLinkRel.SUBSECTION, uriBuilder(ROUTE_BOOKS_LATEST).toUriString()),
          ),
          OpdsEntryNavigation(
            title = "All libraries",
            updated = ZonedDateTime.now(),
            id = ID_LIBRARIES_ALL,
            content = "Browse by library",
            link = OpdsLinkFeedNavigation(OpdsLinkRel.SUBSECTION, uriBuilder(ROUTE_LIBRARIES_ALL).toUriString()),
          ),
          OpdsEntryNavigation(
            title = "All collections",
            updated = ZonedDateTime.now(),
            id = ID_COLLECTIONS_ALL,
            content = "Browse by collection",
            link = OpdsLinkFeedNavigation(OpdsLinkRel.SUBSECTION, uriBuilder(ROUTE_COLLECTIONS_ALL).toUriString()),
          ),
          OpdsEntryNavigation(
            title = "All read lists",
            updated = ZonedDateTime.now(),
            id = ID_READLISTS_ALL,
            content = "Browse by read lists",
            link = OpdsLinkFeedNavigation(OpdsLinkRel.SUBSECTION, uriBuilder(ROUTE_READLISTS_ALL).toUriString()),
          ),
          OpdsEntryNavigation(
            title = "All publishers",
            updated = ZonedDateTime.now(),
            id = ID_PUBLISHERS_ALL,
            content = "Browse by publishers",
            link = OpdsLinkFeedNavigation(OpdsLinkRel.SUBSECTION, uriBuilder(ROUTE_PUBLISHERS_ALL).toUriString()),
          ),
        ),
    )

  @GetMapping(ROUTE_SEARCH)
  fun getSearch(): OpenSearchDescription =
    OpenSearchDescription(
      shortName = "Search",
      description = "Search for series",
      url = OpenSearchDescription.OpenSearchUrl(uriBuilder(ROUTE_SERIES_ALL).toUriString() + "?search={searchTerms}"),
    )

  @PageAsQueryParam
  @GetMapping(ROUTE_ON_DECK)
  fun getOnDeck(
    @AuthenticationPrincipal principal: KomgaPrincipal,
    @Parameter(hidden = true) page: Pageable,
  ): OpdsFeed {
    val bookPage =
      bookDtoRepository.findAllOnDeck(
        principal.user.id,
        principal.user.getAuthorizedLibraryIds(null),
        page,
        principal.user.restrictions,
      )

    val builder = uriBuilder(ROUTE_ON_DECK)

    return OpdsFeedAcquisition(
      id = ID_ON_DECK,
      title = "On Deck",
      updated = ZonedDateTime.now(),
      author = komgaAuthor,
      links =
        listOf(
          OpdsLinkFeedNavigation(OpdsLinkRel.SELF, builder.toUriString()),
          linkStart(),
          *linkPage(builder, bookPage).toTypedArray(),
        ),
      entries = bookPage.content.getEntriesWithSeriesTitle(),
    )
  }

  @PageAsQueryParam
  @GetMapping(ROUTE_KEEP_READING)
  fun getKeepReading(
    @AuthenticationPrincipal principal: KomgaPrincipal,
    @Parameter(hidden = true) page: Pageable,
  ): OpdsFeed {
    val pageable = PageRequest.of(page.pageNumber, page.pageSize, Sort.by(Sort.Order.desc("readProgress.readDate")))

    val bookSearch =
      BookSearchWithReadProgress(
        libraryIds = principal.user.getAuthorizedLibraryIds(null),
        readStatus = setOf(ReadStatus.IN_PROGRESS),
        mediaStatus = setOf(Media.Status.READY),
        deleted = false,
      )

    val bookPage =
      bookDtoRepository.findAll(
        bookSearch,
        principal.user.id,
        pageable,
        principal.user.restrictions,
      )

    val builder = uriBuilder(ROUTE_ON_DECK)

    return OpdsFeedAcquisition(
      id = ID_KEEP_READING,
      title = "Keep Reading",
      updated = ZonedDateTime.now(),
      author = komgaAuthor,
      links =
        listOf(
          OpdsLinkFeedNavigation(OpdsLinkRel.SELF, builder.toUriString()),
          linkStart(),
          *linkPage(builder, bookPage).toTypedArray(),
        ),
      entries = bookPage.content.getEntriesWithSeriesTitle(),
    )
  }

  @PageAsQueryParam
  @GetMapping(ROUTE_SERIES_ALL)
  fun getAllSeries(
    @AuthenticationPrincipal principal: KomgaPrincipal,
    @RequestParam(name = "search", required = false) searchTerm: String?,
    @RequestParam(name = "publisher", required = false) publishers: List<String>?,
    @Parameter(hidden = true) page: Pageable,
  ): OpdsFeed {
    val sort =
      if (!searchTerm.isNullOrBlank())
        Sort.by("relevance")
      else
        Sort.by(Sort.Order.asc("metadata.titleSort"))
    val pageable = PageRequest.of(page.pageNumber, page.pageSize, sort)

    val seriesSearch =
      SeriesSearchWithReadProgress(
        libraryIds = principal.user.getAuthorizedLibraryIds(null),
        searchTerm = searchTerm,
        publishers = publishers,
        deleted = false,
      )

    val seriesPage = seriesDtoRepository.findAll(seriesSearch, principal.user.id, pageable, principal.user.restrictions)

    val builder =
      uriBuilder(ROUTE_SERIES_ALL)
        .queryParamIfPresent("search", Optional.ofNullable(searchTerm))
        .queryParamIfPresent("publisher", Optional.ofNullable(publishers))

    return OpdsFeedNavigation(
      id = ID_SERIES_ALL,
      title = if (!searchTerm.isNullOrBlank()) "Series search for: $searchTerm" else "All series",
      updated = ZonedDateTime.now(),
      author = komgaAuthor,
      links =
        listOf(
          OpdsLinkFeedNavigation(OpdsLinkRel.SELF, builder.toUriString()),
          linkStart(),
          *linkPage(builder, seriesPage).toTypedArray(),
        ),
      entries = seriesPage.content.map { it.toOpdsEntry() },
    )
  }

  @PageAsQueryParam
  @GetMapping(ROUTE_SERIES_LATEST)
  fun getLatestSeries(
    @AuthenticationPrincipal principal: KomgaPrincipal,
    @Parameter(hidden = true) page: Pageable,
  ): OpdsFeed {
    val pageable = PageRequest.of(page.pageNumber, page.pageSize, Sort.by(Sort.Order.desc("lastModified")))

    val seriesSearch =
      SeriesSearchWithReadProgress(
        libraryIds = principal.user.getAuthorizedLibraryIds(null),
        deleted = false,
      )

    val seriesPage = seriesDtoRepository.findAll(seriesSearch, principal.user.id, pageable, principal.user.restrictions)

    val uriBuilder = uriBuilder(ROUTE_SERIES_LATEST)

    return OpdsFeedNavigation(
      id = ID_SERIES_LATEST,
      title = "Latest series",
      updated = ZonedDateTime.now(),
      author = komgaAuthor,
      links =
        listOf(
          OpdsLinkFeedNavigation(OpdsLinkRel.SELF, uriBuilder.toUriString()),
          linkStart(),
          *linkPage(uriBuilder, seriesPage).toTypedArray(),
        ),
      entries = seriesPage.content.map { it.toOpdsEntry() },
    )
  }

  @PageAsQueryParam
  @GetMapping(ROUTE_BOOKS_LATEST)
  fun getLatestBooks(
    @AuthenticationPrincipal principal: KomgaPrincipal,
    @Parameter(hidden = true) page: Pageable,
  ): OpdsFeed {
    val bookSearch =
      BookSearchWithReadProgress(
        libraryIds = principal.user.getAuthorizedLibraryIds(null),
        mediaStatus = setOf(Media.Status.READY),
        deleted = false,
      )
    val pageable = PageRequest.of(page.pageNumber, page.pageSize, Sort.by(Sort.Order.desc("createdDate")))

    val bookPage = bookDtoRepository.findAll(bookSearch, principal.user.id, pageable, principal.user.restrictions)

    val uriBuilder = uriBuilder(ROUTE_BOOKS_LATEST)

    return OpdsFeedAcquisition(
      id = ID_BOOKS_LATEST,
      title = "Latest books",
      updated = ZonedDateTime.now(),
      author = komgaAuthor,
      links =
        listOf(
          OpdsLinkFeedNavigation(OpdsLinkRel.SELF, uriBuilder.toUriString()),
          linkStart(),
          *linkPage(uriBuilder, bookPage).toTypedArray(),
        ),
      entries = bookPage.content.getEntriesWithSeriesTitle(),
    )
  }

  @GetMapping(ROUTE_LIBRARIES_ALL)
  fun getLibraries(
    @AuthenticationPrincipal principal: KomgaPrincipal,
  ): OpdsFeed {
    val libraries =
      if (principal.user.sharedAllLibraries) {
        libraryRepository.findAll()
      } else {
        libraryRepository.findAllByIds(principal.user.sharedLibrariesIds)
      }
    return OpdsFeedNavigation(
      id = ID_LIBRARIES_ALL,
      title = "All libraries",
      updated = ZonedDateTime.now(),
      author = komgaAuthor,
      links =
        listOf(
          OpdsLinkFeedNavigation(OpdsLinkRel.SELF, uriBuilder(ROUTE_LIBRARIES_ALL).toUriString()),
          linkStart(),
        ),
      entries = libraries.map { it.toOpdsEntry() },
    )
  }

  @PageAsQueryParam
  @GetMapping(ROUTE_COLLECTIONS_ALL)
  fun getCollections(
    @AuthenticationPrincipal principal: KomgaPrincipal,
    @Parameter(hidden = true) page: Pageable,
  ): OpdsFeed {
    val pageable = PageRequest.of(page.pageNumber, page.pageSize, Sort.by(Sort.Order.asc("name")))
    val collections = collectionRepository.findAll(principal.user.getAuthorizedLibraryIds(null), principal.user.getAuthorizedLibraryIds(null), pageable = pageable)

    val uriBuilder = uriBuilder(ROUTE_COLLECTIONS_ALL)

    return OpdsFeedNavigation(
      id = ID_COLLECTIONS_ALL,
      title = "All collections",
      updated = ZonedDateTime.now(),
      author = komgaAuthor,
      links =
        listOf(
          OpdsLinkFeedNavigation(OpdsLinkRel.SELF, uriBuilder.toUriString()),
          linkStart(),
          *linkPage(uriBuilder, collections).toTypedArray(),
        ),
      entries = collections.content.map { it.toOpdsEntry() },
    )
  }

  @PageAsQueryParam
  @GetMapping(ROUTE_READLISTS_ALL)
  fun getReadLists(
    @AuthenticationPrincipal principal: KomgaPrincipal,
    @Parameter(hidden = true) page: Pageable,
  ): OpdsFeed {
    val pageable = PageRequest.of(page.pageNumber, page.pageSize, Sort.by(Sort.Order.asc("name")))
    val readLists = readListRepository.findAll(principal.user.getAuthorizedLibraryIds(null), principal.user.getAuthorizedLibraryIds(null), pageable = pageable)

    val uriBuilder = uriBuilder(ROUTE_READLISTS_ALL)

    return OpdsFeedNavigation(
      id = ID_READLISTS_ALL,
      title = "All read lists",
      updated = ZonedDateTime.now(),
      author = komgaAuthor,
      links =
        listOf(
          OpdsLinkFeedNavigation(OpdsLinkRel.SELF, uriBuilder.toUriString()),
          linkStart(),
          *linkPage(uriBuilder, readLists).toTypedArray(),
        ),
      entries = readLists.content.map { it.toOpdsEntry() },
    )
  }

  @PageAsQueryParam
  @GetMapping(ROUTE_PUBLISHERS_ALL)
  fun getPublishers(
    @AuthenticationPrincipal principal: KomgaPrincipal,
    @Parameter(hidden = true) page: Pageable,
  ): OpdsFeed {
    val publishers = referentialRepository.findAllPublishers(principal.user.getAuthorizedLibraryIds(null), page)

    val uriBuilder = uriBuilder(ROUTE_PUBLISHERS_ALL)

    return OpdsFeedNavigation(
      id = ID_PUBLISHERS_ALL,
      title = "All publishers",
      updated = ZonedDateTime.now(),
      author = komgaAuthor,
      links =
        listOf(
          OpdsLinkFeedNavigation(OpdsLinkRel.SELF, uriBuilder.toUriString()),
          linkStart(),
          *linkPage(uriBuilder, publishers).toTypedArray(),
        ),
      entries =
        publishers.content.map { publisher ->
          OpdsEntryNavigation(
            title = publisher,
            updated = ZonedDateTime.now(),
            id = "publisher:${UriUtils.encodeQueryParam(publisher, StandardCharsets.UTF_8)}",
            content = "",
            link = OpdsLinkFeedNavigation(OpdsLinkRel.SUBSECTION, uriBuilder(ROUTE_SERIES_ALL).queryParam("publisher", publisher).toUriString()),
          )
        },
    )
  }

  @PageAsQueryParam
  @GetMapping("series/{id}")
  fun getOneSeries(
    @AuthenticationPrincipal principal: KomgaPrincipal,
    @PathVariable id: String,
    @Parameter(hidden = true) page: Pageable,
  ): OpdsFeed =
    seriesDtoRepository.findByIdOrNull(id, principal.user.id)?.let { series ->
      contentRestrictionChecker.checkContentRestriction(principal.user, series)

      val bookSearch =
        BookSearchWithReadProgress(
          seriesIds = listOf(id),
          mediaStatus = setOf(Media.Status.READY),
          deleted = false,
        )
      val pageable = PageRequest.of(page.pageNumber, page.pageSize, Sort.by(Sort.Order.asc("metadata.numberSort")))

      val entries =
        bookDtoRepository.findAll(bookSearch, principal.user.id, pageable, principal.user.restrictions)
          .map { it.toOpdsEntry(mediaRepository.findById(it.id)) }

      val uriBuilder = uriBuilder("series/$id")

      OpdsFeedAcquisition(
        id = series.id,
        title = series.metadata.title,
        updated = series.lastModified.toZonedDateTime(),
        author = komgaAuthor,
        links =
          listOf(
            OpdsLinkFeedNavigation(OpdsLinkRel.SELF, uriBuilder.toUriString()),
            linkStart(),
            *linkPage(uriBuilder, entries).toTypedArray(),
          ),
        entries = entries.content,
      )
    } ?: throw ResponseStatusException(HttpStatus.NOT_FOUND)

  @PageAsQueryParam
  @GetMapping("libraries/{id}")
  fun getOneLibrary(
    @AuthenticationPrincipal principal: KomgaPrincipal,
    @PathVariable id: String,
    @Parameter(hidden = true) page: Pageable,
  ): OpdsFeed =
    libraryRepository.findByIdOrNull(id)?.let { library ->
      if (!principal.user.canAccessLibrary(library)) throw ResponseStatusException(HttpStatus.FORBIDDEN)

      val seriesSearch =
        SeriesSearchWithReadProgress(
          libraryIds = setOf(library.id),
          deleted = false,
        )

      val pageable = PageRequest.of(page.pageNumber, page.pageSize, Sort.by(Sort.Order.asc("metadata.titleSort")))

      val entries =
        seriesDtoRepository.findAll(seriesSearch, principal.user.id, pageable, principal.user.restrictions)
          .map { it.toOpdsEntry() }

      val uriBuilder = uriBuilder("libraries/$id")

      OpdsFeedNavigation(
        id = library.id,
        title = library.name,
        updated = library.lastModifiedDate.atZone(ZoneId.systemDefault()) ?: ZonedDateTime.now(),
        author = komgaAuthor,
        links =
          listOf(
            OpdsLinkFeedNavigation(OpdsLinkRel.SELF, uriBuilder.toUriString()),
            linkStart(),
            *linkPage(uriBuilder, entries).toTypedArray(),
          ),
        entries = entries.content,
      )
    } ?: throw ResponseStatusException(HttpStatus.NOT_FOUND)

  @PageAsQueryParam
  @GetMapping("collections/{id}")
  fun getOneCollection(
    @AuthenticationPrincipal principal: KomgaPrincipal,
    @PathVariable id: String,
    @Parameter(hidden = true) page: Pageable,
  ): OpdsFeed =
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

      val entries =
        seriesDtoRepository.findAllByCollectionId(collection.id, seriesSearch, principal.user.id, pageable, principal.user.restrictions)
          .map { it.toOpdsEntry() }

      val uriBuilder = uriBuilder("collections/$id")

      OpdsFeedNavigation(
        id = collection.id,
        title = collection.name,
        updated = collection.lastModifiedDate.atZone(ZoneId.systemDefault()) ?: ZonedDateTime.now(),
        author = komgaAuthor,
        links =
          listOf(
            OpdsLinkFeedNavigation(OpdsLinkRel.SELF, uriBuilder.toUriString()),
            linkStart(),
            *linkPage(uriBuilder, entries).toTypedArray(),
          ),
        entries = entries.content,
      )
    } ?: throw ResponseStatusException(HttpStatus.NOT_FOUND)

  @PageAsQueryParam
  @GetMapping("readlists/{id}")
  fun getOneReadList(
    @AuthenticationPrincipal principal: KomgaPrincipal,
    @PathVariable id: String,
    @Parameter(hidden = true) page: Pageable,
  ): OpdsFeed =
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

      val entries =
        booksPage.map { bookDto ->
          bookDto.toOpdsEntry(mediaRepository.findById(bookDto.id)) { "${it.seriesTitle} ${it.metadata.number}: " }
        }

      val uriBuilder = uriBuilder("readlists/$id")

      OpdsFeedAcquisition(
        id = readList.id,
        title = readList.name,
        updated = readList.lastModifiedDate.atZone(ZoneId.systemDefault()) ?: ZonedDateTime.now(),
        author = komgaAuthor,
        links =
          listOf(
            OpdsLinkFeedNavigation(OpdsLinkRel.SELF, uriBuilder.toUriString()),
            linkStart(),
            *linkPage(uriBuilder, booksPage).toTypedArray(),
          ),
        entries = entries.content,
      )
    } ?: throw ResponseStatusException(HttpStatus.NOT_FOUND)

  @ApiResponse(content = [Content(schema = Schema(type = "string", format = "binary"))])
  @GetMapping(
    value = ["books/{bookId}/thumbnail/small"],
    produces = [MediaType.IMAGE_JPEG_VALUE],
  )
  fun getBookThumbnailSmall(
    @AuthenticationPrincipal principal: KomgaPrincipal,
    @PathVariable bookId: String,
  ): ByteArray {
    contentRestrictionChecker.checkContentRestriction(principal.user, bookId)
    val thumbnail = bookLifecycle.getThumbnail(bookId) ?: throw ResponseStatusException(HttpStatus.NOT_FOUND)

    return bookLifecycle.getThumbnailBytes(bookId, if (thumbnail.type == ThumbnailBook.Type.GENERATED) null else komgaSettingsProvider.thumbnailSize.maxEdge)?.bytes
      ?: throw ResponseStatusException(HttpStatus.NOT_FOUND)
  }

  @ApiResponse(content = [Content(mediaType = "image/*", schema = Schema(type = "string", format = "binary"))])
  @GetMapping("books/{bookId}/pages/{pageNumber}", produces = ["image/png", "image/gif", "image/jpeg"])
  @PreAuthorize("hasRole('$ROLE_PAGE_STREAMING')")
  fun getBookPageOpds(
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
    commonBookController.getBookPageInternal(bookId, pageNumber + 1, convertTo, request, principal, null)

  private fun SeriesDto.toOpdsEntry(prepend: Int? = null): OpdsEntryNavigation {
    val pre = prepend?.let { decimalFormat.format(it) + " - " } ?: ""
    return OpdsEntryNavigation(
      title = pre + metadata.title,
      updated = lastModified.atZone(ZoneId.of("Z")) ?: ZonedDateTime.now(),
      id = id,
      content = "",
      link = OpdsLinkFeedNavigation(OpdsLinkRel.SUBSECTION, uriBuilder("series/$id").toUriString()),
    )
  }

  private fun BookDto.toOpdsEntry(
    media: Media,
    prepend: (BookDto) -> String = { "" },
  ): OpdsEntryAcquisition {
    val mediaTypes =
      when (media.profile) {
        MediaProfile.DIVINA -> media.pages.map { it.mediaType }.distinct()
        MediaProfile.PDF -> listOf(pdfImageType.mediaType)
        MediaProfile.EPUB -> if (media.epubDivinaCompatible) media.pages.map { it.mediaType }.distinct() else emptyList()
        null -> emptyList()
      }

    val opdsLinkPageStreaming =
      if (mediaTypes.isEmpty()) {
        null
      } else if (mediaTypes.size == 1 && mediaTypes.first() in opdsPseSupportedFormats) {
        OpdsLinkPageStreaming(mediaTypes.first(), uriBuilder("books/$id/pages/").toUriString() + "{pageNumber}", media.pageCount, readProgress?.page, readProgress?.readDate)
      } else {
        OpdsLinkPageStreaming("image/jpeg", uriBuilder("books/$id/pages/").toUriString() + "{pageNumber}?convert=jpeg", media.pageCount, readProgress?.page, readProgress?.readDate)
      }

    val thumbnailMediaType =
      when (media.profile) {
        MediaProfile.PDF -> pdfImageType.mediaType
        else -> "image/jpeg"
      }

    return OpdsEntryAcquisition(
      title = "${prepend(this)}${metadata.title}",
      updated = lastModified.toZonedDateTime(),
      id = id,
      content =
        buildString {
          append("${FilenameUtils.getExtension(url).lowercase()} - $size")
          if (metadata.summary.isNotBlank()) append("\n\n${metadata.summary}")
        },
      authors = metadata.authors.map { OpdsAuthor(it.name) },
      links =
        listOfNotNull(
          OpdsLinkImageThumbnail("image/jpeg", uriBuilder("books/$id/thumbnail/small").toUriString()),
          OpdsLinkImage(thumbnailMediaType, uriBuilder("books/$id/thumbnail").toUriString()),
          OpdsLinkFileAcquisition(media.mediaType, uriBuilder("books/$id/file/${sanitize(FilenameUtils.getName(url))}").toUriString()),
          opdsLinkPageStreaming,
        ),
    )
  }

  private fun Library.toOpdsEntry(): OpdsEntryNavigation =
    OpdsEntryNavigation(
      title = name,
      updated = lastModifiedDate.atZone(ZoneId.systemDefault()) ?: ZonedDateTime.now(),
      id = id,
      content = "",
      link = OpdsLinkFeedNavigation(OpdsLinkRel.SUBSECTION, uriBuilder("libraries/$id").toUriString()),
    )

  private fun SeriesCollection.toOpdsEntry(): OpdsEntryNavigation =
    OpdsEntryNavigation(
      title = name,
      updated = lastModifiedDate.atZone(ZoneId.systemDefault()) ?: ZonedDateTime.now(),
      id = id,
      content = "",
      link = OpdsLinkFeedNavigation(OpdsLinkRel.SUBSECTION, uriBuilder("collections/$id").toUriString()),
    )

  private fun ReadList.toOpdsEntry(): OpdsEntryNavigation =
    OpdsEntryNavigation(
      title = name,
      updated = lastModifiedDate.atZone(ZoneId.systemDefault()) ?: ZonedDateTime.now(),
      id = id,
      content = "",
      link = OpdsLinkFeedNavigation(OpdsLinkRel.SUBSECTION, uriBuilder("readlists/$id").toUriString()),
    )

  private fun List<BookDto>.getEntriesWithSeriesTitle(): List<OpdsEntryAcquisition> =
    map { bookDto ->
      bookDto.toOpdsEntry(mediaRepository.findById(bookDto.id)) { "${it.seriesTitle} ${it.metadata.number}: " }
    }

  private fun sanitize(fileName: String): String =
    fileName.replace(";", "")
}
