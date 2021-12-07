package org.gotson.komga.interfaces.api.opds

import io.swagger.v3.oas.annotations.Parameter
import mu.KotlinLogging
import org.apache.commons.io.FilenameUtils
import org.gotson.komga.domain.model.BookSearchWithReadProgress
import org.gotson.komga.domain.model.Library
import org.gotson.komga.domain.model.Media
import org.gotson.komga.domain.model.ReadList
import org.gotson.komga.domain.model.SeriesCollection
import org.gotson.komga.domain.model.SeriesSearchWithReadProgress
import org.gotson.komga.domain.persistence.LibraryRepository
import org.gotson.komga.domain.persistence.MediaRepository
import org.gotson.komga.domain.persistence.ReadListRepository
import org.gotson.komga.domain.persistence.ReferentialRepository
import org.gotson.komga.domain.persistence.SeriesCollectionRepository
import org.gotson.komga.infrastructure.jooq.toCurrentTimeZone
import org.gotson.komga.infrastructure.security.KomgaPrincipal
import org.gotson.komga.infrastructure.swagger.PageAsQueryParam
import org.gotson.komga.interfaces.api.opds.dto.OpdsAuthor
import org.gotson.komga.interfaces.api.opds.dto.OpdsEntryAcquisition
import org.gotson.komga.interfaces.api.opds.dto.OpdsEntryNavigation
import org.gotson.komga.interfaces.api.opds.dto.OpdsFeed
import org.gotson.komga.interfaces.api.opds.dto.OpdsFeedAcquisition
import org.gotson.komga.interfaces.api.opds.dto.OpdsFeedNavigation
import org.gotson.komga.interfaces.api.opds.dto.OpdsLink
import org.gotson.komga.interfaces.api.opds.dto.OpdsLinkFeedNavigation
import org.gotson.komga.interfaces.api.opds.dto.OpdsLinkFileAcquisition
import org.gotson.komga.interfaces.api.opds.dto.OpdsLinkImage
import org.gotson.komga.interfaces.api.opds.dto.OpdsLinkImageThumbnail
import org.gotson.komga.interfaces.api.opds.dto.OpdsLinkPageStreaming
import org.gotson.komga.interfaces.api.opds.dto.OpdsLinkRel
import org.gotson.komga.interfaces.api.opds.dto.OpdsLinkSearch
import org.gotson.komga.interfaces.api.opds.dto.OpenSearchDescription
import org.gotson.komga.interfaces.api.persistence.BookDtoRepository
import org.gotson.komga.interfaces.api.persistence.SeriesDtoRepository
import org.gotson.komga.interfaces.api.rest.dto.BookDto
import org.gotson.komga.interfaces.api.rest.dto.SeriesDto
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ResponseStatusException
import org.springframework.web.util.UriComponentsBuilder
import org.springframework.web.util.UriUtils
import java.net.URI
import java.nio.charset.StandardCharsets
import java.text.DecimalFormat
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.Optional
import javax.servlet.ServletContext

private val logger = KotlinLogging.logger {}

private const val ROUTE_BASE = "/opds/v1.2/"
private const val ROUTE_CATALOG = "catalog"
private const val ROUTE_SERIES_ALL = "series"
private const val ROUTE_SERIES_LATEST = "series/latest"
private const val ROUTE_BOOKS_LATEST = "books/latest"
private const val ROUTE_LIBRARIES_ALL = "libraries"
private const val ROUTE_COLLECTIONS_ALL = "collections"
private const val ROUTE_READLISTS_ALL = "readlists"
private const val ROUTE_PUBLISHERS_ALL = "publishers"
private const val ROUTE_SEARCH = "search"

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
  servletContext: ServletContext,
  private val libraryRepository: LibraryRepository,
  private val collectionRepository: SeriesCollectionRepository,
  private val readListRepository: ReadListRepository,
  private val seriesDtoRepository: SeriesDtoRepository,
  private val bookDtoRepository: BookDtoRepository,
  private val mediaRepository: MediaRepository,
  private val referentialRepository: ReferentialRepository
) {

  private val routeBase = "${servletContext.contextPath}$ROUTE_BASE"

  private val komgaAuthor = OpdsAuthor("Komga", URI("https://github.com/gotson/komga"))

  private val decimalFormat = DecimalFormat("0.#")

  private val opdsPseSupportedFormats = listOf("image/jpeg", "image/png", "image/gif")

  private val linkStart = OpdsLinkFeedNavigation(OpdsLinkRel.START, uriBuilder(ROUTE_CATALOG).toUriString())

  private fun uriBuilder(path: String) =
    UriComponentsBuilder
      .fromPath("$routeBase$path")

  private fun <T> linkPage(uriBuilder: UriComponentsBuilder, page: Page<T>): List<OpdsLink> {
    val pageBuilder = uriBuilder.cloneBuilder()
      .queryParam("page", "{page}")
      .build()
    return listOfNotNull(
      if (!page.isFirst) OpdsLinkFeedNavigation(
        OpdsLinkRel.PREVIOUS,
        pageBuilder.expand(mapOf("page" to page.pageable.previousOrFirst().pageNumber)).toUriString()
      )
      else null,
      if (!page.isLast) OpdsLinkFeedNavigation(
        OpdsLinkRel.NEXT,
        pageBuilder.expand(mapOf("page" to page.pageable.next().pageNumber)).toUriString()
      )
      else null,
    )
  }

  @GetMapping(ROUTE_CATALOG)
  fun getCatalog(): OpdsFeed = OpdsFeedNavigation(
    id = "root",
    title = "Komga OPDS catalog",
    updated = ZonedDateTime.now(),
    author = komgaAuthor,
    links = listOf(
      OpdsLinkFeedNavigation(OpdsLinkRel.SELF, uriBuilder(ROUTE_CATALOG).toUriString()),
      linkStart,
      OpdsLinkSearch(uriBuilder(ROUTE_SEARCH).toUriString()),
    ),
    entries = listOf(
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
      )
    )
  )

  @GetMapping(ROUTE_SEARCH)
  fun getSearch(): OpenSearchDescription = OpenSearchDescription(
    shortName = "Search",
    description = "Search for series",
    url = OpenSearchDescription.OpenSearchUrl("$routeBase$ROUTE_SERIES_ALL?search={searchTerms}"),
  )

  @PageAsQueryParam
  @GetMapping(ROUTE_SERIES_ALL)
  fun getAllSeries(
    @AuthenticationPrincipal principal: KomgaPrincipal,
    @RequestParam(name = "search", required = false) searchTerm: String?,
    @RequestParam(name = "publisher", required = false) publishers: List<String>?,
    @Parameter(hidden = true) page: Pageable,
  ): OpdsFeed {
    val sort =
      if (!searchTerm.isNullOrBlank()) Sort.by("relevance")
      else Sort.by(Sort.Order.asc("metadata.titleSort"))
    val pageable = PageRequest.of(page.pageNumber, page.pageSize, sort)

    val seriesSearch = SeriesSearchWithReadProgress(
      libraryIds = principal.user.getAuthorizedLibraryIds(null),
      searchTerm = searchTerm,
      publishers = publishers,
      deleted = false,
    )

    val seriesPage = seriesDtoRepository.findAll(seriesSearch, principal.user.id, pageable)

    val builder = uriBuilder(ROUTE_SERIES_ALL)
      .queryParamIfPresent("search", Optional.ofNullable(searchTerm))

    return OpdsFeedNavigation(
      id = ID_SERIES_ALL,
      title = if (!searchTerm.isNullOrBlank()) "Series search for: $searchTerm" else "All series",
      updated = ZonedDateTime.now(),
      author = komgaAuthor,
      links = listOf(
        OpdsLinkFeedNavigation(OpdsLinkRel.SELF, builder.toUriString()),
        linkStart,
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

    val seriesSearch = SeriesSearchWithReadProgress(
      libraryIds = principal.user.getAuthorizedLibraryIds(null),
      deleted = false,
    )

    val seriesPage = seriesDtoRepository.findAll(seriesSearch, principal.user.id, pageable)

    val uriBuilder = uriBuilder(ROUTE_SERIES_LATEST)

    return OpdsFeedNavigation(
      id = ID_SERIES_LATEST,
      title = "Latest series",
      updated = ZonedDateTime.now(),
      author = komgaAuthor,
      links = listOf(
        OpdsLinkFeedNavigation(OpdsLinkRel.SELF, uriBuilder.build().toUriString()),
        linkStart,
        *linkPage(uriBuilder, seriesPage).toTypedArray(),
      ),
      entries = seriesPage.content.map { it.toOpdsEntry() },
    )
  }

  @PageAsQueryParam
  @GetMapping(ROUTE_BOOKS_LATEST)
  fun getLatestBooks(
    @AuthenticationPrincipal principal: KomgaPrincipal,
    @RequestHeader(name = HttpHeaders.USER_AGENT, required = false, defaultValue = "") userAgent: String,
    @Parameter(hidden = true) page: Pageable,
  ): OpdsFeed {
    val bookSearch = BookSearchWithReadProgress(
      libraryIds = principal.user.getAuthorizedLibraryIds(null),
      mediaStatus = setOf(Media.Status.READY),
      deleted = false,
    )
    val pageable = PageRequest.of(page.pageNumber, page.pageSize, Sort.by(Sort.Order.desc("createdDate")))

    val entries = bookDtoRepository.findAll(bookSearch, principal.user.id, pageable)
      .map { it.toOpdsEntry(mediaRepository.findById(it.id), shouldPrependBookNumbers(userAgent)) }

    val uriBuilder = uriBuilder(ROUTE_BOOKS_LATEST)

    return OpdsFeedAcquisition(
      id = ID_BOOKS_LATEST,
      title = "Latest books",
      updated = ZonedDateTime.now(),
      author = komgaAuthor,
      links = listOf(
        OpdsLinkFeedNavigation(OpdsLinkRel.SELF, uriBuilder.build().toUriString()),
        linkStart,
        *linkPage(uriBuilder, entries).toTypedArray(),
      ),
      entries = entries.content,
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
      links = listOf(
        OpdsLinkFeedNavigation(OpdsLinkRel.SELF, uriBuilder(ROUTE_LIBRARIES_ALL).toUriString()),
        linkStart,
      ),
      entries = libraries.map { it.toOpdsEntry() }
    )
  }

  @PageAsQueryParam
  @GetMapping(ROUTE_COLLECTIONS_ALL)
  fun getCollections(
    @AuthenticationPrincipal principal: KomgaPrincipal,
    @Parameter(hidden = true) page: Pageable,
  ): OpdsFeed {
    val pageable = PageRequest.of(page.pageNumber, page.pageSize, Sort.by(Sort.Order.asc("name")))
    val collections =
      if (principal.user.sharedAllLibraries) {
        collectionRepository.findAll(pageable = pageable)
      } else {
        collectionRepository.findAllByLibraryIds(principal.user.sharedLibrariesIds, principal.user.sharedLibrariesIds, pageable = pageable)
      }

    val uriBuilder = uriBuilder(ROUTE_COLLECTIONS_ALL)

    return OpdsFeedNavigation(
      id = ID_COLLECTIONS_ALL,
      title = "All collections",
      updated = ZonedDateTime.now(),
      author = komgaAuthor,
      links = listOf(
        OpdsLinkFeedNavigation(OpdsLinkRel.SELF, uriBuilder.toUriString()),
        linkStart,
        *linkPage(uriBuilder, collections).toTypedArray(),
      ),
      entries = collections.content.map { it.toOpdsEntry() }
    )
  }

  @PageAsQueryParam
  @GetMapping(ROUTE_READLISTS_ALL)
  fun getReadLists(
    @AuthenticationPrincipal principal: KomgaPrincipal,
    @Parameter(hidden = true) page: Pageable,
  ): OpdsFeed {
    val pageable = PageRequest.of(page.pageNumber, page.pageSize, Sort.by(Sort.Order.asc("name")))
    val readLists =
      if (principal.user.sharedAllLibraries) {
        readListRepository.findAll(pageable = pageable)
      } else {
        readListRepository.findAllByLibraryIds(principal.user.sharedLibrariesIds, principal.user.sharedLibrariesIds, pageable = pageable)
      }

    val uriBuilder = uriBuilder(ROUTE_READLISTS_ALL)

    return OpdsFeedNavigation(
      id = ID_READLISTS_ALL,
      title = "All read lists",
      updated = ZonedDateTime.now(),
      author = komgaAuthor,
      links = listOf(
        OpdsLinkFeedNavigation(OpdsLinkRel.SELF, uriBuilder.toUriString()),
        linkStart,
        *linkPage(uriBuilder, readLists).toTypedArray(),
      ),
      entries = readLists.content.map { it.toOpdsEntry() }
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
      links = listOf(
        OpdsLinkFeedNavigation(OpdsLinkRel.SELF, uriBuilder.toUriString()),
        linkStart,
        *linkPage(uriBuilder, publishers).toTypedArray(),
      ),
      entries = publishers.content.map { publisher ->
        OpdsEntryNavigation(
          title = publisher,
          updated = ZonedDateTime.now(),
          id = "publisher:${UriUtils.encodeQueryParam(publisher, StandardCharsets.UTF_8)}",
          content = "",
          link = OpdsLinkFeedNavigation(OpdsLinkRel.SUBSECTION, uriBuilder(ROUTE_SERIES_ALL).queryParam("publisher", publisher).toUriString()),
        )
      }
    )
  }

  @PageAsQueryParam
  @GetMapping("series/{id}")
  fun getOneSeries(
    @AuthenticationPrincipal principal: KomgaPrincipal,
    @RequestHeader(name = HttpHeaders.USER_AGENT, required = false, defaultValue = "") userAgent: String,
    @PathVariable id: String,
    @Parameter(hidden = true) page: Pageable,
  ): OpdsFeed =
    seriesDtoRepository.findByIdOrNull(id, principal.user.id)?.let { series ->
      if (!principal.user.canAccessLibrary(series.libraryId)) throw ResponseStatusException(HttpStatus.FORBIDDEN)

      val bookSearch = BookSearchWithReadProgress(
        seriesIds = listOf(id),
        mediaStatus = setOf(Media.Status.READY),
        deleted = false,
      )
      val pageable = PageRequest.of(page.pageNumber, page.pageSize, Sort.by(Sort.Order.asc("metadata.numberSort")))

      val entries = bookDtoRepository.findAll(bookSearch, principal.user.id, pageable)
        .map { it.toOpdsEntry(mediaRepository.findById(it.id), shouldPrependBookNumbers(userAgent)) }

      val uriBuilder = uriBuilder("series/$id")

      OpdsFeedAcquisition(
        id = series.id,
        title = series.metadata.title,
        updated = series.lastModified.toCurrentTimeZone().atZone(ZoneId.systemDefault()) ?: ZonedDateTime.now(),
        author = komgaAuthor,
        links = listOf(
          OpdsLinkFeedNavigation(OpdsLinkRel.SELF, uriBuilder.toUriString()),
          linkStart,
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

      val seriesSearch = SeriesSearchWithReadProgress(
        libraryIds = setOf(library.id),
        deleted = false,
      )

      val pageable = PageRequest.of(page.pageNumber, page.pageSize, Sort.by(Sort.Order.asc("metadata.titleSort")))

      val entries = seriesDtoRepository.findAll(seriesSearch, principal.user.id, pageable)
        .map { it.toOpdsEntry() }

      val uriBuilder = uriBuilder("libraries/$id")

      OpdsFeedNavigation(
        id = library.id,
        title = library.name,
        updated = library.lastModifiedDate.atZone(ZoneId.systemDefault()) ?: ZonedDateTime.now(),
        author = komgaAuthor,
        links = listOf(
          OpdsLinkFeedNavigation(OpdsLinkRel.SELF, uriBuilder.toUriString()),
          linkStart,
          *linkPage(uriBuilder, entries).toTypedArray(),
        ),
        entries = entries.content
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
        if (collection.ordered) Sort.by(Sort.Order.asc("collection.number"))
        else Sort.by(Sort.Order.asc("metadata.titleSort"))
      val pageable = PageRequest.of(page.pageNumber, page.pageSize, sort)

      val seriesSearch = SeriesSearchWithReadProgress(
        libraryIds = principal.user.getAuthorizedLibraryIds(null),
        deleted = false,
      )

      val entries = seriesDtoRepository.findAllByCollectionId(collection.id, seriesSearch, principal.user.id, pageable)
        .map { seriesDto ->
          val index = if (collection.ordered) collection.seriesIds.indexOf(seriesDto.id) + 1 else null
          seriesDto.toOpdsEntry(index)
        }

      val uriBuilder = uriBuilder("collections/$id")

      OpdsFeedNavigation(
        id = collection.id,
        title = collection.name,
        updated = collection.lastModifiedDate.atZone(ZoneId.systemDefault()) ?: ZonedDateTime.now(),
        author = komgaAuthor,
        links = listOf(
          OpdsLinkFeedNavigation(OpdsLinkRel.SELF, uriBuilder.toUriString()),
          linkStart,
          *linkPage(uriBuilder, entries).toTypedArray(),
        ),
        entries = entries.content
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
      val pageable = PageRequest.of(page.pageNumber, page.pageSize, Sort.by(Sort.Order.asc("readList.number")))

      val bookSearch = BookSearchWithReadProgress(deleted = false)

      val booksPage = bookDtoRepository.findAllByReadListId(
        readList.id,
        principal.user.id,
        principal.user.getAuthorizedLibraryIds(null),
        bookSearch,
        pageable,
      )

      val entries = booksPage.map { bookDto ->
        val index = readList.bookIds.filterValues { it == bookDto.id }.keys.first()
        bookDto.toOpdsEntry(mediaRepository.findById(bookDto.id), prependNumber = false, prepend = index + 1)
      }

      val uriBuilder = uriBuilder("readlists/$id")

      OpdsFeedAcquisition(
        id = readList.id,
        title = readList.name,
        updated = readList.lastModifiedDate.atZone(ZoneId.systemDefault()) ?: ZonedDateTime.now(),
        author = komgaAuthor,
        links = listOf(
          OpdsLinkFeedNavigation(OpdsLinkRel.SELF, uriBuilder.toUriString()),
          linkStart,
          *linkPage(uriBuilder, booksPage).toTypedArray(),
        ),
        entries = entries.content
      )
    } ?: throw ResponseStatusException(HttpStatus.NOT_FOUND)

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

  private fun BookDto.toOpdsEntry(media: Media, prependNumber: Boolean, prepend: Int? = null): OpdsEntryAcquisition {
    val mediaTypes = media.pages.map { it.mediaType }.distinct()

    val opdsLinkPageStreaming = if (mediaTypes.size == 1 && mediaTypes.first() in opdsPseSupportedFormats) {
      OpdsLinkPageStreaming(mediaTypes.first(), "${routeBase}books/$id/pages/{pageNumber}?zero_based=true", media.pages.size, readProgress?.page)
    } else {
      OpdsLinkPageStreaming("image/jpeg", "${routeBase}books/$id/pages/{pageNumber}?convert=jpeg&zero_based=true", media.pages.size, readProgress?.page)
    }

    val pre = prepend?.let { decimalFormat.format(it) + " - " } ?: ""
    return OpdsEntryAcquisition(
      title = "$pre${if (prependNumber) "${decimalFormat.format(metadata.numberSort)} - " else ""}${metadata.title}",
      updated = lastModified.toCurrentTimeZone().atZone(ZoneId.systemDefault()) ?: ZonedDateTime.now(),
      id = id,
      content = run {
        var content = "${FilenameUtils.getExtension(url).lowercase()} - $size"
        if (metadata.summary.isNotBlank())
          content += "\n\n${metadata.summary}"
        content
      },
      authors = metadata.authors.map { OpdsAuthor(it.name) },
      links = listOf(
        OpdsLinkImageThumbnail("image/jpeg", uriBuilder("books/$id/thumbnail").toUriString()),
        OpdsLinkImage(media.pages[0].mediaType, uriBuilder("books/$id/pages/1").toUriString()),
        OpdsLinkFileAcquisition(media.mediaType, uriBuilder("books/$id/file/${sanitize(FilenameUtils.getName(url))}").toUriString()),
        opdsLinkPageStreaming,
      )
    )
  }

  private fun Library.toOpdsEntry(): OpdsEntryNavigation =
    OpdsEntryNavigation(
      title = name,
      updated = lastModifiedDate.atZone(ZoneId.systemDefault()) ?: ZonedDateTime.now(),
      id = id,
      content = "",
      link = OpdsLinkFeedNavigation(OpdsLinkRel.SUBSECTION, uriBuilder("libraries/$id").toUriString())
    )

  private fun SeriesCollection.toOpdsEntry(): OpdsEntryNavigation =
    OpdsEntryNavigation(
      title = name,
      updated = lastModifiedDate.atZone(ZoneId.systemDefault()) ?: ZonedDateTime.now(),
      id = id,
      content = "",
      link = OpdsLinkFeedNavigation(OpdsLinkRel.SUBSECTION, uriBuilder("collections/$id").toUriString())
    )

  private fun ReadList.toOpdsEntry(): OpdsEntryNavigation =
    OpdsEntryNavigation(
      title = name,
      updated = lastModifiedDate.atZone(ZoneId.systemDefault()) ?: ZonedDateTime.now(),
      id = id,
      content = "",
      link = OpdsLinkFeedNavigation(OpdsLinkRel.SUBSECTION, uriBuilder("readlists/$id").toUriString())
    )

  private fun shouldPrependBookNumbers(userAgent: String) =
    userAgent.contains("chunky", ignoreCase = true)

  private fun sanitize(fileName: String): String =
    fileName.replace(";", "")
}
