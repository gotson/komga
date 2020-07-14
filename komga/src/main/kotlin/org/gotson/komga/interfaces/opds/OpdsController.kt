package org.gotson.komga.interfaces.opds

import mu.KotlinLogging
import org.gotson.komga.domain.model.Book
import org.gotson.komga.domain.model.BookMetadata
import org.gotson.komga.domain.model.BookSearch
import org.gotson.komga.domain.model.Library
import org.gotson.komga.domain.model.Media
import org.gotson.komga.domain.model.Series
import org.gotson.komga.domain.model.SeriesCollection
import org.gotson.komga.domain.model.SeriesMetadata
import org.gotson.komga.domain.model.SeriesSearch
import org.gotson.komga.domain.persistence.BookMetadataRepository
import org.gotson.komga.domain.persistence.BookRepository
import org.gotson.komga.domain.persistence.LibraryRepository
import org.gotson.komga.domain.persistence.MediaRepository
import org.gotson.komga.domain.persistence.SeriesCollectionRepository
import org.gotson.komga.domain.persistence.SeriesMetadataRepository
import org.gotson.komga.domain.persistence.SeriesRepository
import org.gotson.komga.infrastructure.jooq.UnpagedSorted
import org.gotson.komga.infrastructure.security.KomgaPrincipal
import org.gotson.komga.interfaces.opds.dto.OpdsAuthor
import org.gotson.komga.interfaces.opds.dto.OpdsEntryAcquisition
import org.gotson.komga.interfaces.opds.dto.OpdsEntryNavigation
import org.gotson.komga.interfaces.opds.dto.OpdsFeed
import org.gotson.komga.interfaces.opds.dto.OpdsFeedAcquisition
import org.gotson.komga.interfaces.opds.dto.OpdsFeedNavigation
import org.gotson.komga.interfaces.opds.dto.OpdsLinkFeedNavigation
import org.gotson.komga.interfaces.opds.dto.OpdsLinkFileAcquisition
import org.gotson.komga.interfaces.opds.dto.OpdsLinkImage
import org.gotson.komga.interfaces.opds.dto.OpdsLinkImageThumbnail
import org.gotson.komga.interfaces.opds.dto.OpdsLinkPageStreaming
import org.gotson.komga.interfaces.opds.dto.OpdsLinkRel
import org.gotson.komga.interfaces.opds.dto.OpdsLinkSearch
import org.gotson.komga.interfaces.opds.dto.OpenSearchDescription
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
import java.net.URI
import java.text.DecimalFormat
import java.time.ZoneId
import java.time.ZonedDateTime
import javax.servlet.ServletContext

private val logger = KotlinLogging.logger {}

private const val ROUTE_BASE = "/opds/v1.2/"
private const val ROUTE_CATALOG = "catalog"
private const val ROUTE_SERIES_ALL = "series"
private const val ROUTE_SERIES_LATEST = "series/latest"
private const val ROUTE_LIBRARIES_ALL = "libraries"
private const val ROUTE_COLLECTIONS_ALL = "collections"
private const val ROUTE_SEARCH = "search"

private const val ID_SERIES_ALL = "allSeries"
private const val ID_SERIES_LATEST = "latestSeries"
private const val ID_LIBRARIES_ALL = "allLibraries"
private const val ID_COLLECTIONS_ALL = "allCollections"

@RestController
@RequestMapping(value = [ROUTE_BASE], produces = [MediaType.APPLICATION_ATOM_XML_VALUE, MediaType.APPLICATION_XML_VALUE, MediaType.TEXT_XML_VALUE])
class OpdsController(
  servletContext: ServletContext,
  private val libraryRepository: LibraryRepository,
  private val collectionRepository: SeriesCollectionRepository,
  private val seriesRepository: SeriesRepository,
  private val seriesMetadataRepository: SeriesMetadataRepository,
  private val bookRepository: BookRepository,
  private val bookMetadataRepository: BookMetadataRepository,
  private val mediaRepository: MediaRepository
) {

  private val routeBase = "${servletContext.contextPath}$ROUTE_BASE"

  private val komgaAuthor = OpdsAuthor("Komga", URI("https://github.com/gotson/komga"))
  private val linkStart = OpdsLinkFeedNavigation(OpdsLinkRel.START, "$routeBase$ROUTE_CATALOG")
  private val linkSearch = OpdsLinkSearch("$routeBase$ROUTE_SEARCH")

  private val decimalFormat = DecimalFormat("0.#")

  private val opdsPseSupportedFormats = listOf("image/jpeg", "image/png", "image/gif")

  private val feedCatalog = OpdsFeedNavigation(
    id = "root",
    title = "Komga OPDS catalog",
    updated = ZonedDateTime.now(),
    author = komgaAuthor,
    links = listOf(
      OpdsLinkFeedNavigation(OpdsLinkRel.SELF, "$routeBase$ROUTE_CATALOG"),
      linkStart,
      linkSearch
    ),
    entries = listOf(
      OpdsEntryNavigation(
        title = "All series",
        updated = ZonedDateTime.now(),
        id = ID_SERIES_ALL,
        content = "Browse by series",
        link = OpdsLinkFeedNavigation(OpdsLinkRel.SUBSECTION, "$routeBase$ROUTE_SERIES_ALL")
      ),
      OpdsEntryNavigation(
        title = "Latest series",
        updated = ZonedDateTime.now(),
        id = ID_SERIES_LATEST,
        content = "Browse latest series",
        link = OpdsLinkFeedNavigation(OpdsLinkRel.SUBSECTION, "$routeBase$ROUTE_SERIES_LATEST")
      ),
      OpdsEntryNavigation(
        title = "All libraries",
        updated = ZonedDateTime.now(),
        id = ID_LIBRARIES_ALL,
        content = "Browse by library",
        link = OpdsLinkFeedNavigation(OpdsLinkRel.SUBSECTION, "$routeBase$ROUTE_LIBRARIES_ALL")
      ),
      OpdsEntryNavigation(
        title = "All collections",
        updated = ZonedDateTime.now(),
        id = ID_COLLECTIONS_ALL,
        content = "Browse by collection",
        link = OpdsLinkFeedNavigation(OpdsLinkRel.SUBSECTION, "$routeBase$ROUTE_COLLECTIONS_ALL")
      )
    )
  )

  private val openSearchDescription = OpenSearchDescription(
    shortName = "Search",
    description = "Search for series",
    url = OpenSearchDescription.OpenSearchUrl("$routeBase$ROUTE_SERIES_ALL?search={searchTerms}")
  )

  @GetMapping(ROUTE_CATALOG)
  fun getCatalog(): OpdsFeed = feedCatalog

  @GetMapping(ROUTE_SEARCH)
  fun getSearch(): OpenSearchDescription = openSearchDescription

  @GetMapping(ROUTE_SERIES_ALL)
  fun getAllSeries(
    @AuthenticationPrincipal principal: KomgaPrincipal,
    @RequestParam("search") searchTerm: String?
  ): OpdsFeed {
    val seriesSearch = SeriesSearch(
      libraryIds = principal.user.getAuthorizedLibraryIds(null),
      searchTerm = searchTerm
    )

    val entries = seriesRepository.findAll(seriesSearch)
      .map { SeriesWithInfo(it, seriesMetadataRepository.findById(it.id)) }
      .sortedBy { it.metadata.titleSort.toLowerCase() }
      .map { it.toOpdsEntry() }


    return OpdsFeedNavigation(
      id = ID_SERIES_ALL,
      title = "All series",
      updated = ZonedDateTime.now(),
      author = komgaAuthor,
      links = listOf(
        OpdsLinkFeedNavigation(OpdsLinkRel.SELF, "$routeBase$ROUTE_SERIES_ALL"),
        linkStart
      ),
      entries = entries
    )
  }

  @GetMapping(ROUTE_SERIES_LATEST)
  fun getLatestSeries(
    @AuthenticationPrincipal principal: KomgaPrincipal
  ): OpdsFeed {
    val seriesSearch = SeriesSearch(
      libraryIds = principal.user.getAuthorizedLibraryIds(null)
    )

    val entries = seriesRepository.findAll(seriesSearch)
      .map { SeriesWithInfo(it, seriesMetadataRepository.findById(it.id)) }
      .sortedBy { it.series.lastModifiedDate }
      .map { it.toOpdsEntry() }

    return OpdsFeedNavigation(
      id = ID_SERIES_LATEST,
      title = "Latest series",
      updated = ZonedDateTime.now(),
      author = komgaAuthor,
      links = listOf(
        OpdsLinkFeedNavigation(OpdsLinkRel.SELF, "$routeBase$ROUTE_SERIES_LATEST"),
        linkStart
      ),
      entries = entries
    )
  }

  @GetMapping(ROUTE_LIBRARIES_ALL)
  fun getLibraries(
    @AuthenticationPrincipal principal: KomgaPrincipal
  ): OpdsFeed {
    val libraries =
      if (principal.user.sharedAllLibraries) {
        libraryRepository.findAll()
      } else {
        libraryRepository.findAllById(principal.user.sharedLibrariesIds)
      }
    return OpdsFeedNavigation(
      id = ID_LIBRARIES_ALL,
      title = "All libraries",
      updated = ZonedDateTime.now(),
      author = komgaAuthor,
      links = listOf(
        OpdsLinkFeedNavigation(OpdsLinkRel.SELF, "$routeBase$ROUTE_LIBRARIES_ALL"),
        linkStart
      ),
      entries = libraries.map { it.toOpdsEntry() }
    )
  }

  @GetMapping(ROUTE_COLLECTIONS_ALL)
  fun getCollections(
    @AuthenticationPrincipal principal: KomgaPrincipal
  ): OpdsFeed {
    val pageRequest = UnpagedSorted(Sort.by(Sort.Order.asc("name")))
    val collections =
      if (principal.user.sharedAllLibraries) {
        collectionRepository.findAll(pageable = pageRequest)
      } else {
        collectionRepository.findAllByLibraries(principal.user.sharedLibrariesIds, principal.user.sharedLibrariesIds, pageable = pageRequest)
      }
    return OpdsFeedNavigation(
      id = ID_COLLECTIONS_ALL,
      title = "All collections",
      updated = ZonedDateTime.now(),
      author = komgaAuthor,
      links = listOf(
        OpdsLinkFeedNavigation(OpdsLinkRel.SELF, "$routeBase$ROUTE_COLLECTIONS_ALL"),
        linkStart
      ),
      entries = collections.content.map { it.toOpdsEntry() }
    )
  }

  @GetMapping("series/{id}")
  fun getOneSeries(
    @AuthenticationPrincipal principal: KomgaPrincipal,
    @RequestHeader(name = HttpHeaders.USER_AGENT, required = false, defaultValue = "") userAgent: String,
    @PathVariable id: String
  ): OpdsFeed =
    seriesRepository.findByIdOrNull(id)?.let { series ->
      if (!principal.user.canAccessSeries(series)) throw ResponseStatusException(HttpStatus.FORBIDDEN)

      val books = bookRepository.findAll(BookSearch(
        seriesIds = listOf(id),
        mediaStatus = setOf(Media.Status.READY)
      ))
      val metadata = seriesMetadataRepository.findById(series.id)

      val entries = books
        .map { BookWithInfo(it, mediaRepository.findById(it.id), bookMetadataRepository.findById(it.id)) }
        .sortedBy { it.metadata.numberSort }
        .map { it.toOpdsEntry(shouldPrependBookNumbers(userAgent)) }

      OpdsFeedAcquisition(
        id = series.id.toString(),
        title = metadata.title,
        updated = series.lastModifiedDate.atZone(ZoneId.systemDefault()) ?: ZonedDateTime.now(),
        author = komgaAuthor,
        links = listOf(
          OpdsLinkFeedNavigation(OpdsLinkRel.SELF, "${routeBase}series/$id"),
          linkStart
        ),
        entries = entries
      )
    } ?: throw ResponseStatusException(HttpStatus.NOT_FOUND)

  @GetMapping("libraries/{id}")
  fun getOneLibrary(
    @AuthenticationPrincipal principal: KomgaPrincipal,
    @PathVariable id: String
  ): OpdsFeed =
    libraryRepository.findByIdOrNull(id)?.let { library ->
      if (!principal.user.canAccessLibrary(library)) throw ResponseStatusException(HttpStatus.FORBIDDEN)

      val seriesSearch = SeriesSearch(libraryIds = setOf(library.id))

      val entries = seriesRepository.findAll(seriesSearch)
        .map { SeriesWithInfo(it, seriesMetadataRepository.findById(it.id)) }
        .sortedBy { it.metadata.titleSort.toLowerCase() }
        .map { it.toOpdsEntry() }

      OpdsFeedNavigation(
        id = library.id.toString(),
        title = library.name,
        updated = library.lastModifiedDate.atZone(ZoneId.systemDefault()) ?: ZonedDateTime.now(),
        author = komgaAuthor,
        links = listOf(
          OpdsLinkFeedNavigation(OpdsLinkRel.SELF, "${routeBase}libraries/$id"),
          linkStart
        ),
        entries = entries
      )
    } ?: throw ResponseStatusException(HttpStatus.NOT_FOUND)

  @GetMapping("collections/{id}")
  fun getOneCollection(
    @AuthenticationPrincipal principal: KomgaPrincipal,
    @PathVariable id: String
  ): OpdsFeed {
    return collectionRepository.findByIdOrNull(id, principal.user.getAuthorizedLibraryIds(null))?.let { collection ->
      val series = collection.seriesIds.mapNotNull { seriesRepository.findByIdOrNull(it) }
        .map { SeriesWithInfo(it, seriesMetadataRepository.findById(it.id)) }

      val sorted =
        if (!collection.ordered) series.sortedBy { it.metadata.titleSort }
        else series

      val entries = sorted.mapIndexed { index, it ->
        it.toOpdsEntry(if (collection.ordered) index + 1 else null)
      }

      OpdsFeedNavigation(
        id = collection.id.toString(),
        title = collection.name,
        updated = collection.lastModifiedDate.atZone(ZoneId.systemDefault()) ?: ZonedDateTime.now(),
        author = komgaAuthor,
        links = listOf(
          OpdsLinkFeedNavigation(OpdsLinkRel.SELF, "${routeBase}collections/$id"),
          linkStart
        ),
        entries = entries
      )
    } ?: throw ResponseStatusException(HttpStatus.NOT_FOUND)
  }


  private fun SeriesWithInfo.toOpdsEntry(prepend: Int? = null): OpdsEntryNavigation {
    val pre = prepend?.let { decimalFormat.format(it) + " - " } ?: ""
    return OpdsEntryNavigation(
      title = pre + metadata.title,
      updated = series.lastModifiedDate.atZone(ZoneId.systemDefault()) ?: ZonedDateTime.now(),
      id = series.id.toString(),
      content = "",
      link = OpdsLinkFeedNavigation(OpdsLinkRel.SUBSECTION, "${routeBase}series/${series.id}")
    )
  }

  private fun BookWithInfo.toOpdsEntry(prependNumber: Boolean): OpdsEntryAcquisition {
    val mediaTypes = media.pages.map { it.mediaType }.distinct()

    val opdsLinkPageStreaming = if (mediaTypes.size == 1 && mediaTypes.first() in opdsPseSupportedFormats) {
      OpdsLinkPageStreaming(mediaTypes.first(), "${routeBase}books/${book.id}/pages/{pageNumber}?zero_based=true", media.pages.size)
    } else {
      OpdsLinkPageStreaming("image/jpeg", "${routeBase}books/${book.id}/pages/{pageNumber}?convert=jpeg&zero_based=true", media.pages.size)
    }

    return OpdsEntryAcquisition(
      title = "${if (prependNumber) "${decimalFormat.format(metadata.numberSort)} - " else ""}${metadata.title}",
      updated = book.lastModifiedDate.atZone(ZoneId.systemDefault()) ?: ZonedDateTime.now(),
      id = book.id.toString(),
      content = run {
        var content = "${book.fileExtension().toUpperCase()} - ${book.fileSizeHumanReadable()}"
        if (metadata.summary.isNotBlank())
          content += "\n\n${metadata.summary}"
        content
      },
      authors = metadata.authors.map { OpdsAuthor(it.name) },
      links = listOf(
        OpdsLinkImageThumbnail("image/jpeg", "${routeBase}books/${book.id}/thumbnail"),
        OpdsLinkImage(media.pages[0].mediaType, "${routeBase}books/${book.id}/pages/1"),
        OpdsLinkFileAcquisition(media.mediaType, "${routeBase}books/${book.id}/file/${book.fileName()}"),
        opdsLinkPageStreaming
      )
    )
  }

  private fun Library.toOpdsEntry(): OpdsEntryNavigation {
    return OpdsEntryNavigation(
      title = name,
      updated = lastModifiedDate.atZone(ZoneId.systemDefault()) ?: ZonedDateTime.now(),
      id = id.toString(),
      content = "",
      link = OpdsLinkFeedNavigation(OpdsLinkRel.SUBSECTION, "${routeBase}libraries/$id")
    )
  }

  private fun SeriesCollection.toOpdsEntry(): OpdsEntryNavigation {
    return OpdsEntryNavigation(
      title = name,
      updated = lastModifiedDate.atZone(ZoneId.systemDefault()) ?: ZonedDateTime.now(),
      id = id.toString(),
      content = "",
      link = OpdsLinkFeedNavigation(OpdsLinkRel.SUBSECTION, "${routeBase}collections/$id")
    )
  }

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
