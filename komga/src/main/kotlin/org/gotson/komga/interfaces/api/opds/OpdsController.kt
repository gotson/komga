package org.gotson.komga.interfaces.api.opds

import mu.KotlinLogging
import org.apache.commons.io.FilenameUtils
import org.gotson.komga.domain.model.Book
import org.gotson.komga.domain.model.BookMetadata
import org.gotson.komga.domain.model.BookSearch
import org.gotson.komga.domain.model.Library
import org.gotson.komga.domain.model.Media
import org.gotson.komga.domain.model.ReadList
import org.gotson.komga.domain.model.Series
import org.gotson.komga.domain.model.SeriesCollection
import org.gotson.komga.domain.model.SeriesMetadata
import org.gotson.komga.domain.model.SeriesSearch
import org.gotson.komga.domain.persistence.BookMetadataRepository
import org.gotson.komga.domain.persistence.BookRepository
import org.gotson.komga.domain.persistence.LibraryRepository
import org.gotson.komga.domain.persistence.MediaRepository
import org.gotson.komga.domain.persistence.ReadListRepository
import org.gotson.komga.domain.persistence.ReferentialRepository
import org.gotson.komga.domain.persistence.SeriesCollectionRepository
import org.gotson.komga.domain.persistence.SeriesMetadataRepository
import org.gotson.komga.domain.persistence.SeriesRepository
import org.gotson.komga.infrastructure.jooq.UnpagedSorted
import org.gotson.komga.infrastructure.security.KomgaPrincipal
import org.gotson.komga.interfaces.api.MARK_READ
import org.gotson.komga.interfaces.api.opds.dto.OpdsAuthor
import org.gotson.komga.interfaces.api.opds.dto.OpdsEntryAcquisition
import org.gotson.komga.interfaces.api.opds.dto.OpdsEntryNavigation
import org.gotson.komga.interfaces.api.opds.dto.OpdsFeed
import org.gotson.komga.interfaces.api.opds.dto.OpdsFeedAcquisition
import org.gotson.komga.interfaces.api.opds.dto.OpdsFeedNavigation
import org.gotson.komga.interfaces.api.opds.dto.OpdsLinkFeedNavigation
import org.gotson.komga.interfaces.api.opds.dto.OpdsLinkFileAcquisition
import org.gotson.komga.interfaces.api.opds.dto.OpdsLinkImage
import org.gotson.komga.interfaces.api.opds.dto.OpdsLinkImageThumbnail
import org.gotson.komga.interfaces.api.opds.dto.OpdsLinkPageStreaming
import org.gotson.komga.interfaces.api.opds.dto.OpdsLinkRel
import org.gotson.komga.interfaces.api.opds.dto.OpdsLinkSearch
import org.gotson.komga.interfaces.api.opds.dto.OpenSearchDescription
import org.springframework.data.domain.PageRequest
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
import org.springframework.web.util.UriUtils
import java.net.URI
import java.nio.charset.StandardCharsets
import java.text.DecimalFormat
import java.time.ZoneId
import java.time.ZonedDateTime
import javax.servlet.ServletContext
import kotlin.io.path.extension

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
  private val seriesRepository: SeriesRepository,
  private val seriesMetadataRepository: SeriesMetadataRepository,
  private val bookRepository: BookRepository,
  private val bookMetadataRepository: BookMetadataRepository,
  private val mediaRepository: MediaRepository,
  private val referentialRepository: ReferentialRepository
) {

  private val routeBase = "${servletContext.contextPath}$ROUTE_BASE"

  private val komgaAuthor = OpdsAuthor("Komga", URI("https://github.com/gotson/komga"))

  private val decimalFormat = DecimalFormat("0.#")

  private val opdsPseSupportedFormats = listOf("image/jpeg", "image/png", "image/gif")

  private fun linkStart(markRead: Boolean) = OpdsLinkFeedNavigation(OpdsLinkRel.START, "$routeBase$ROUTE_CATALOG?$MARK_READ=$markRead")

  @GetMapping(ROUTE_CATALOG)
  fun getCatalog(
    @RequestParam(name = MARK_READ, required = false) markRead: Boolean = false,
  ): OpdsFeed = OpdsFeedNavigation(
    id = "root",
    title = "Komga OPDS catalog",
    updated = ZonedDateTime.now(),
    author = komgaAuthor,
    links = listOf(
      OpdsLinkFeedNavigation(OpdsLinkRel.SELF, "$routeBase$ROUTE_CATALOG?$MARK_READ=$markRead"),
      linkStart(markRead),
      OpdsLinkSearch("$routeBase$ROUTE_SEARCH?$MARK_READ=$markRead"),
    ),
    entries = listOf(
      OpdsEntryNavigation(
        title = "All series",
        updated = ZonedDateTime.now(),
        id = ID_SERIES_ALL,
        content = "Browse by series",
        link = OpdsLinkFeedNavigation(OpdsLinkRel.SUBSECTION, "$routeBase$ROUTE_SERIES_ALL?$MARK_READ=$markRead"),
      ),
      OpdsEntryNavigation(
        title = "Latest series",
        updated = ZonedDateTime.now(),
        id = ID_SERIES_LATEST,
        content = "Browse latest series",
        link = OpdsLinkFeedNavigation(OpdsLinkRel.SUBSECTION, "$routeBase$ROUTE_SERIES_LATEST?$MARK_READ=$markRead"),
      ),
      OpdsEntryNavigation(
        title = "Latest books",
        updated = ZonedDateTime.now(),
        id = ID_BOOKS_LATEST,
        content = "Browse latest books",
        link = OpdsLinkFeedNavigation(OpdsLinkRel.SUBSECTION, "$routeBase$ROUTE_BOOKS_LATEST?$MARK_READ=$markRead"),
      ),
      OpdsEntryNavigation(
        title = "All libraries",
        updated = ZonedDateTime.now(),
        id = ID_LIBRARIES_ALL,
        content = "Browse by library",
        link = OpdsLinkFeedNavigation(OpdsLinkRel.SUBSECTION, "$routeBase$ROUTE_LIBRARIES_ALL?$MARK_READ=$markRead"),
      ),
      OpdsEntryNavigation(
        title = "All collections",
        updated = ZonedDateTime.now(),
        id = ID_COLLECTIONS_ALL,
        content = "Browse by collection",
        link = OpdsLinkFeedNavigation(OpdsLinkRel.SUBSECTION, "$routeBase$ROUTE_COLLECTIONS_ALL?$MARK_READ=$markRead"),
      ),
      OpdsEntryNavigation(
        title = "All read lists",
        updated = ZonedDateTime.now(),
        id = ID_READLISTS_ALL,
        content = "Browse by read lists",
        link = OpdsLinkFeedNavigation(OpdsLinkRel.SUBSECTION, "$routeBase$ROUTE_READLISTS_ALL?$MARK_READ=$markRead"),
      ),
      OpdsEntryNavigation(
        title = "All publishers",
        updated = ZonedDateTime.now(),
        id = ID_PUBLISHERS_ALL,
        content = "Browse by publishers",
        link = OpdsLinkFeedNavigation(OpdsLinkRel.SUBSECTION, "$routeBase$ROUTE_PUBLISHERS_ALL?$MARK_READ=$markRead"),
      )
    )
  )

  @GetMapping(ROUTE_SEARCH)
  fun getSearch(
    @RequestParam(name = MARK_READ, required = false) markRead: Boolean = false,
  ): OpenSearchDescription = OpenSearchDescription(
    shortName = "Search",
    description = "Search for series",
    url = OpenSearchDescription.OpenSearchUrl("$routeBase$ROUTE_SERIES_ALL?search={searchTerms}&$MARK_READ=$markRead")
  )

  @GetMapping(ROUTE_SERIES_ALL)
  fun getAllSeries(
    @AuthenticationPrincipal principal: KomgaPrincipal,
    @RequestParam(name = "search", required = false) searchTerm: String?,
    @RequestParam(name = "publisher", required = false) publishers: List<String>?,
    @RequestParam(name = MARK_READ, required = false) markRead: Boolean = false,
  ): OpdsFeed {
    val seriesSearch = SeriesSearch(
      libraryIds = principal.user.getAuthorizedLibraryIds(null),
      searchTerm = searchTerm,
      publishers = publishers,
      deleted = false,
    )

    val entries = seriesRepository.findAll(seriesSearch)
      .map { SeriesWithInfo(it, seriesMetadataRepository.findById(it.id)) }
      .sortedBy { it.metadata.titleSort.lowercase() }
      .map { it.toOpdsEntry(markRead) }

    return OpdsFeedNavigation(
      id = ID_SERIES_ALL,
      title = "All series",
      updated = ZonedDateTime.now(),
      author = komgaAuthor,
      links = listOf(
        OpdsLinkFeedNavigation(OpdsLinkRel.SELF, "$routeBase$ROUTE_SERIES_ALL?$MARK_READ=$markRead"),
        linkStart(markRead),
      ),
      entries = entries
    )
  }

  @GetMapping(ROUTE_SERIES_LATEST)
  fun getLatestSeries(
    @AuthenticationPrincipal principal: KomgaPrincipal,
    @RequestParam(name = MARK_READ, required = false) markRead: Boolean = false,
  ): OpdsFeed {
    val seriesSearch = SeriesSearch(
      libraryIds = principal.user.getAuthorizedLibraryIds(null),
      deleted = false,
    )

    val entries = seriesRepository.findAll(seriesSearch)
      .map { SeriesWithInfo(it, seriesMetadataRepository.findById(it.id)) }
      .sortedByDescending { it.series.lastModifiedDate }
      .map { it.toOpdsEntry(markRead) }

    return OpdsFeedNavigation(
      id = ID_SERIES_LATEST,
      title = "Latest series",
      updated = ZonedDateTime.now(),
      author = komgaAuthor,
      links = listOf(
        OpdsLinkFeedNavigation(OpdsLinkRel.SELF, "$routeBase$ROUTE_SERIES_LATEST?$MARK_READ=$markRead"),
        linkStart(markRead),
      ),
      entries = entries
    )
  }

  @GetMapping(ROUTE_BOOKS_LATEST)
  fun getLatestBooks(
    @AuthenticationPrincipal principal: KomgaPrincipal,
    @RequestHeader(name = HttpHeaders.USER_AGENT, required = false, defaultValue = "") userAgent: String,
    @RequestParam(name = MARK_READ, required = false) markRead: Boolean = false,
  ): OpdsFeed {
    val bookSearch = BookSearch(
      libraryIds = principal.user.getAuthorizedLibraryIds(null),
      mediaStatus = setOf(Media.Status.READY),
      deleted = false,
    )
    val pageRequest = PageRequest.of(0, 50, Sort.by(Sort.Order.desc("createdDate")))

    val entries = bookRepository.findAll(bookSearch, pageRequest)
      .map { BookWithInfo(it, mediaRepository.findById(it.id), bookMetadataRepository.findById(it.id)) }
      .content
      .map { it.toOpdsEntry(markRead, shouldPrependBookNumbers(userAgent)) }

    return OpdsFeedAcquisition(
      id = ID_BOOKS_LATEST,
      title = "Latest books",
      updated = ZonedDateTime.now(),
      author = komgaAuthor,
      links = listOf(
        OpdsLinkFeedNavigation(OpdsLinkRel.SELF, "$routeBase$ROUTE_BOOKS_LATEST?$MARK_READ=$markRead"),
        linkStart(markRead),
      ),
      entries = entries
    )
  }

  @GetMapping(ROUTE_LIBRARIES_ALL)
  fun getLibraries(
    @AuthenticationPrincipal principal: KomgaPrincipal,
    @RequestParam(name = MARK_READ, required = false) markRead: Boolean = false,
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
        OpdsLinkFeedNavigation(OpdsLinkRel.SELF, "$routeBase$ROUTE_LIBRARIES_ALL?$MARK_READ=$markRead"),
        linkStart(markRead),
      ),
      entries = libraries.map { it.toOpdsEntry(markRead) }
    )
  }

  @GetMapping(ROUTE_COLLECTIONS_ALL)
  fun getCollections(
    @AuthenticationPrincipal principal: KomgaPrincipal,
    @RequestParam(name = MARK_READ, required = false) markRead: Boolean = false,
  ): OpdsFeed {
    val pageRequest = UnpagedSorted(Sort.by(Sort.Order.asc("name")))
    val collections =
      if (principal.user.sharedAllLibraries) {
        collectionRepository.findAll(pageable = pageRequest)
      } else {
        collectionRepository.findAllByLibraryIds(principal.user.sharedLibrariesIds, principal.user.sharedLibrariesIds, pageable = pageRequest)
      }
    return OpdsFeedNavigation(
      id = ID_COLLECTIONS_ALL,
      title = "All collections",
      updated = ZonedDateTime.now(),
      author = komgaAuthor,
      links = listOf(
        OpdsLinkFeedNavigation(OpdsLinkRel.SELF, "$routeBase$ROUTE_COLLECTIONS_ALL?$MARK_READ=$markRead"),
        linkStart(markRead),
      ),
      entries = collections.content.map { it.toOpdsEntry(markRead) }
    )
  }

  @GetMapping(ROUTE_READLISTS_ALL)
  fun getReadLists(
    @AuthenticationPrincipal principal: KomgaPrincipal,
    @RequestParam(name = MARK_READ, required = false) markRead: Boolean = false,
  ): OpdsFeed {
    val pageRequest = UnpagedSorted(Sort.by(Sort.Order.asc("name")))
    val readLists =
      if (principal.user.sharedAllLibraries) {
        readListRepository.findAll(pageable = pageRequest)
      } else {
        readListRepository.findAllByLibraryIds(principal.user.sharedLibrariesIds, principal.user.sharedLibrariesIds, pageable = pageRequest)
      }
    return OpdsFeedNavigation(
      id = ID_READLISTS_ALL,
      title = "All read lists",
      updated = ZonedDateTime.now(),
      author = komgaAuthor,
      links = listOf(
        OpdsLinkFeedNavigation(OpdsLinkRel.SELF, "$routeBase$ROUTE_READLISTS_ALL?$MARK_READ=$markRead"),
        linkStart(markRead),
      ),
      entries = readLists.content.map { it.toOpdsEntry(markRead) }
    )
  }

  @GetMapping(ROUTE_PUBLISHERS_ALL)
  fun getPublishers(
    @AuthenticationPrincipal principal: KomgaPrincipal,
    @RequestParam(name = MARK_READ, required = false) markRead: Boolean = false,
  ): OpdsFeed {
    val publishers = referentialRepository.findAllPublishers(principal.user.getAuthorizedLibraryIds(null))

    return OpdsFeedNavigation(
      id = ID_PUBLISHERS_ALL,
      title = "All publishers",
      updated = ZonedDateTime.now(),
      author = komgaAuthor,
      links = listOf(
        OpdsLinkFeedNavigation(OpdsLinkRel.SELF, "$routeBase$ROUTE_PUBLISHERS_ALL?$MARK_READ=$markRead"),
        linkStart(markRead),
      ),
      entries = publishers.map {
        val publisherEncoded = UriUtils.encodeQueryParam(it, StandardCharsets.UTF_8)
        OpdsEntryNavigation(
          title = it,
          updated = ZonedDateTime.now(),
          id = "publisher:$publisherEncoded",
          content = "",
          link = OpdsLinkFeedNavigation(OpdsLinkRel.SUBSECTION, "$routeBase$ROUTE_SERIES_ALL?publisher=$publisherEncoded&$MARK_READ=$markRead")
        )
      }
    )
  }

  @GetMapping("series/{id}")
  fun getOneSeries(
    @AuthenticationPrincipal principal: KomgaPrincipal,
    @RequestHeader(name = HttpHeaders.USER_AGENT, required = false, defaultValue = "") userAgent: String,
    @PathVariable id: String,
    @RequestParam(name = MARK_READ, required = false) markRead: Boolean = false,
  ): OpdsFeed =
    seriesRepository.findByIdOrNull(id)?.let { series ->
      if (!principal.user.canAccessSeries(series)) throw ResponseStatusException(HttpStatus.FORBIDDEN)

      val books = bookRepository.findAll(
        BookSearch(
          seriesIds = listOf(id),
          mediaStatus = setOf(Media.Status.READY),
          deleted = false,
        )
      )
      val metadata = seriesMetadataRepository.findById(series.id)

      val entries = books
        .map { BookWithInfo(it, mediaRepository.findById(it.id), bookMetadataRepository.findById(it.id)) }
        .sortedBy { it.metadata.numberSort }
        .map { it.toOpdsEntry(markRead, shouldPrependBookNumbers(userAgent)) }

      OpdsFeedAcquisition(
        id = series.id,
        title = metadata.title,
        updated = series.lastModifiedDate.atZone(ZoneId.systemDefault()) ?: ZonedDateTime.now(),
        author = komgaAuthor,
        links = listOf(
          OpdsLinkFeedNavigation(OpdsLinkRel.SELF, "${routeBase}series/$id?$MARK_READ=$markRead"),
          linkStart(markRead),
        ),
        entries = entries
      )
    } ?: throw ResponseStatusException(HttpStatus.NOT_FOUND)

  @GetMapping("libraries/{id}")
  fun getOneLibrary(
    @AuthenticationPrincipal principal: KomgaPrincipal,
    @PathVariable id: String,
    @RequestParam(name = MARK_READ, required = false) markRead: Boolean = false,
  ): OpdsFeed =
    libraryRepository.findByIdOrNull(id)?.let { library ->
      if (!principal.user.canAccessLibrary(library)) throw ResponseStatusException(HttpStatus.FORBIDDEN)

      val seriesSearch = SeriesSearch(
        libraryIds = setOf(library.id),
        deleted = false,
      )

      val entries = seriesRepository.findAll(seriesSearch)
        .map { SeriesWithInfo(it, seriesMetadataRepository.findById(it.id)) }
        .sortedBy { it.metadata.titleSort.lowercase() }
        .map { it.toOpdsEntry(markRead) }

      OpdsFeedNavigation(
        id = library.id,
        title = library.name,
        updated = library.lastModifiedDate.atZone(ZoneId.systemDefault()) ?: ZonedDateTime.now(),
        author = komgaAuthor,
        links = listOf(
          OpdsLinkFeedNavigation(OpdsLinkRel.SELF, "${routeBase}libraries/$id?$MARK_READ=$markRead"),
          linkStart(markRead),
        ),
        entries = entries
      )
    } ?: throw ResponseStatusException(HttpStatus.NOT_FOUND)

  @GetMapping("collections/{id}")
  fun getOneCollection(
    @AuthenticationPrincipal principal: KomgaPrincipal,
    @PathVariable id: String,
    @RequestParam(name = MARK_READ, required = false) markRead: Boolean = false,
  ): OpdsFeed {
    return collectionRepository.findByIdOrNull(id, principal.user.getAuthorizedLibraryIds(null))?.let { collection ->
      val series = collection.seriesIds.mapNotNull { seriesRepository.findByIdOrNull(it) }
        .filterNot { it.deletedDate != null }
        .map { SeriesWithInfo(it, seriesMetadataRepository.findById(it.id)) }

      val sorted =
        if (!collection.ordered) series.sortedBy { it.metadata.titleSort }
        else series

      val entries = sorted.mapIndexed { index, it ->
        it.toOpdsEntry(markRead, if (collection.ordered) index + 1 else null)
      }

      OpdsFeedNavigation(
        id = collection.id,
        title = collection.name,
        updated = collection.lastModifiedDate.atZone(ZoneId.systemDefault()) ?: ZonedDateTime.now(),
        author = komgaAuthor,
        links = listOf(
          OpdsLinkFeedNavigation(OpdsLinkRel.SELF, "${routeBase}collections/$id?$MARK_READ=$markRead"),
          linkStart(markRead),
        ),
        entries = entries
      )
    } ?: throw ResponseStatusException(HttpStatus.NOT_FOUND)
  }

  @GetMapping("readlists/{id}")
  fun getOneReadList(
    @AuthenticationPrincipal principal: KomgaPrincipal,
    @PathVariable id: String,
    @RequestParam(name = MARK_READ, required = false) markRead: Boolean = false,
  ): OpdsFeed {
    return readListRepository.findByIdOrNull(id, principal.user.getAuthorizedLibraryIds(null))?.let { readList ->
      val books = readList.bookIds.values.mapNotNull { bookRepository.findByIdOrNull(it) }
        .filterNot { it.deletedDate != null }
        .map { BookWithInfo(it, mediaRepository.findById(it.id), bookMetadataRepository.findById(it.id)) }

      val entries = books.mapIndexed { index, it ->
        it.toOpdsEntry(markRead, prependNumber = false, prepend = index + 1)
      }

      OpdsFeedAcquisition(
        id = readList.id,
        title = readList.name,
        updated = readList.lastModifiedDate.atZone(ZoneId.systemDefault()) ?: ZonedDateTime.now(),
        author = komgaAuthor,
        links = listOf(
          OpdsLinkFeedNavigation(OpdsLinkRel.SELF, "${routeBase}readlists/$id?$MARK_READ=$markRead"),
          linkStart(markRead),
        ),
        entries = entries
      )
    } ?: throw ResponseStatusException(HttpStatus.NOT_FOUND)
  }

  private fun SeriesWithInfo.toOpdsEntry(markRead: Boolean, prepend: Int? = null): OpdsEntryNavigation {
    val pre = prepend?.let { decimalFormat.format(it) + " - " } ?: ""
    return OpdsEntryNavigation(
      title = pre + metadata.title,
      updated = series.lastModifiedDate.atZone(ZoneId.systemDefault()) ?: ZonedDateTime.now(),
      id = series.id,
      content = "",
      link = OpdsLinkFeedNavigation(OpdsLinkRel.SUBSECTION, "${routeBase}series/${series.id}?$MARK_READ=$markRead")
    )
  }

  private fun BookWithInfo.toOpdsEntry(markRead: Boolean, prependNumber: Boolean, prepend: Int? = null): OpdsEntryAcquisition {
    val mediaTypes = media.pages.map { it.mediaType }.distinct()

    val opdsLinkPageStreaming = if (mediaTypes.size == 1 && mediaTypes.first() in opdsPseSupportedFormats) {
      OpdsLinkPageStreaming(mediaTypes.first(), "${routeBase}books/${book.id}/pages/{pageNumber}?zero_based=true&$MARK_READ=$markRead", media.pages.size)
    } else {
      OpdsLinkPageStreaming("image/jpeg", "${routeBase}books/${book.id}/pages/{pageNumber}?convert=jpeg&zero_based=true&$MARK_READ=$markRead", media.pages.size)
    }

    val pre = prepend?.let { decimalFormat.format(it) + " - " } ?: ""
    return OpdsEntryAcquisition(
      title = "$pre${if (prependNumber) "${decimalFormat.format(metadata.numberSort)} - " else ""}${metadata.title}",
      updated = book.lastModifiedDate.atZone(ZoneId.systemDefault()) ?: ZonedDateTime.now(),
      id = book.id,
      content = run {
        var content = "${book.path.extension.lowercase()} - ${book.fileSizeHumanReadable}"
        if (metadata.summary.isNotBlank())
          content += "\n\n${metadata.summary}"
        content
      },
      authors = metadata.authors.map { OpdsAuthor(it.name) },
      links = listOf(
        OpdsLinkImageThumbnail("image/jpeg", "${routeBase}books/${book.id}/thumbnail"),
        OpdsLinkImage(media.pages[0].mediaType, "${routeBase}books/${book.id}/pages/1"),
        OpdsLinkFileAcquisition(media.mediaType, "${routeBase}books/${book.id}/file/${sanitize(FilenameUtils.getName(book.url.toString()))}"),
        opdsLinkPageStreaming,
      )
    )
  }

  private fun sanitize(fileName: String): String = fileName.replace(";", "")

  private fun Library.toOpdsEntry(markRead: Boolean): OpdsEntryNavigation =
    OpdsEntryNavigation(
      title = name,
      updated = lastModifiedDate.atZone(ZoneId.systemDefault()) ?: ZonedDateTime.now(),
      id = id,
      content = "",
      link = OpdsLinkFeedNavigation(OpdsLinkRel.SUBSECTION, "${routeBase}libraries/$id?$MARK_READ=$markRead")
    )

  private fun SeriesCollection.toOpdsEntry(markRead: Boolean): OpdsEntryNavigation =
    OpdsEntryNavigation(
      title = name,
      updated = lastModifiedDate.atZone(ZoneId.systemDefault()) ?: ZonedDateTime.now(),
      id = id,
      content = "",
      link = OpdsLinkFeedNavigation(OpdsLinkRel.SUBSECTION, "${routeBase}collections/$id?$MARK_READ=$markRead")
    )

  private fun ReadList.toOpdsEntry(markRead: Boolean): OpdsEntryNavigation =
    OpdsEntryNavigation(
      title = name,
      updated = lastModifiedDate.atZone(ZoneId.systemDefault()) ?: ZonedDateTime.now(),
      id = id,
      content = "",
      link = OpdsLinkFeedNavigation(OpdsLinkRel.SUBSECTION, "${routeBase}readlists/$id?$MARK_READ=$markRead")
    )

  private fun shouldPrependBookNumbers(userAgent: String) =
    userAgent.contains("chunky", ignoreCase = true)

  private data class BookWithInfo(
    val book: Book,
    val media: Media,
    val metadata: BookMetadata
  )

  private data class SeriesWithInfo(
    val series: Series,
    val metadata: SeriesMetadata
  )
}
