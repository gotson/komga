package org.gotson.komga.infrastructure.metadata.comicrack

import com.fasterxml.jackson.dataformat.xml.XmlMapper
import io.github.oshai.kotlinlogging.KotlinLogging
import org.apache.commons.validator.routines.ISBNValidator
import org.gotson.komga.domain.model.Author
import org.gotson.komga.domain.model.BCP47TagValidator
import org.gotson.komga.domain.model.BookMetadataPatch
import org.gotson.komga.domain.model.BookMetadataPatchCapability
import org.gotson.komga.domain.model.BookWithMedia
import org.gotson.komga.domain.model.Library
import org.gotson.komga.domain.model.MetadataPatchTarget
import org.gotson.komga.domain.model.SeriesMetadata
import org.gotson.komga.domain.model.SeriesMetadataPatch
import org.gotson.komga.domain.model.WebLink
import org.gotson.komga.domain.service.BookAnalyzer
import org.gotson.komga.infrastructure.metadata.BookMetadataProvider
import org.gotson.komga.infrastructure.metadata.SeriesMetadataFromBookProvider
import org.gotson.komga.infrastructure.metadata.comicrack.dto.ComicInfo
import org.gotson.komga.infrastructure.metadata.comicrack.dto.Manga
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.net.URI
import java.time.LocalDate

private val logger = KotlinLogging.logger {}

private const val COMIC_INFO = "ComicInfo.xml"

@Service
class ComicInfoProvider(
  @param:Autowired(required = false) private val mapper: XmlMapper = XmlMapper(),
  private val bookAnalyzer: BookAnalyzer,
  private val isbnValidator: ISBNValidator,
) : BookMetadataProvider,
  SeriesMetadataFromBookProvider {
  override val capabilities =
    setOf(
      BookMetadataPatchCapability.TITLE,
      BookMetadataPatchCapability.SUMMARY,
      BookMetadataPatchCapability.NUMBER,
      BookMetadataPatchCapability.NUMBER_SORT,
      BookMetadataPatchCapability.RELEASE_DATE,
      BookMetadataPatchCapability.AUTHORS,
      BookMetadataPatchCapability.READ_LISTS,
      BookMetadataPatchCapability.LINKS,
    )

  override fun getBookMetadataFromBook(book: BookWithMedia): BookMetadataPatch? {
    getComicInfo(book)?.let { comicInfo ->
      val releaseDate =
        comicInfo.year?.let {
          LocalDate.of(comicInfo.year!!, comicInfo.month ?: 1, comicInfo.day ?: 1)
        }

      val authors = mutableListOf<Author>()
      comicInfo.writer?.splitWithRole("writer")?.let { authors += it }
      comicInfo.penciller?.splitWithRole("penciller")?.let { authors += it }
      comicInfo.inker?.splitWithRole("inker")?.let { authors += it }
      comicInfo.colorist?.splitWithRole("colorist")?.let { authors += it }
      comicInfo.letterer?.splitWithRole("letterer")?.let { authors += it }
      comicInfo.coverArtist?.splitWithRole("cover")?.let { authors += it }
      comicInfo.editor?.splitWithRole("editor")?.let { authors += it }
      comicInfo.translator?.splitWithRole("translator")?.let { authors += it }

      val readLists = mutableListOf<BookMetadataPatch.ReadListEntry>()
      if (!comicInfo.alternateSeries.isNullOrBlank()) {
        readLists.add(
          BookMetadataPatch.ReadListEntry(
            comicInfo.alternateSeries!!,
            comicInfo.alternateNumber?.toIntOrNull(),
          ),
        )
      }

      comicInfo.storyArc?.let { value ->
        // get list of arcs and corresponding number, split by `,`
        val arcs = value.split(",").map { it.trim().ifBlank { null } }
        val numbers = comicInfo.storyArcNumber?.split(",")?.map { it.trim().toIntOrNull() }

        if (!numbers.isNullOrEmpty()) {
          // if there is associated numbers, add each valid association as a read list entry
          (arcs zip numbers).forEach { (arc, number) ->
            if (arc != null && number != null) readLists.add(BookMetadataPatch.ReadListEntry(arc, number))
          }
        } else {
          // if there is no numbers, only use the arcs name
          readLists.addAll(arcs.filterNotNull().map { BookMetadataPatch.ReadListEntry(it) })
        }
      }

      val links =
        comicInfo.web
          ?.split(" ")
          ?.filter { it.isNotBlank() }
          ?.mapNotNull {
            try {
              URI(it.trim()).let { uri -> WebLink(uri.host, uri) }
            } catch (e: Exception) {
              logger.error(e) { "Could not parse Web element as valid URI: $it" }
              null
            }
          }

      val tags = comicInfo.tags?.split(',')?.mapNotNull { it.trim().lowercase().ifBlank { null } }

      val isbn = comicInfo.gtin?.let { isbnValidator.validate(it) }

      return BookMetadataPatch(
        title = comicInfo.title?.ifBlank { null },
        summary = comicInfo.summary?.ifBlank { null },
        number = comicInfo.number?.ifBlank { null },
        numberSort = comicInfo.number?.toFloatOrNull(),
        releaseDate = releaseDate,
        authors = authors.ifEmpty { null },
        readLists = readLists,
        links = links?.ifEmpty { null },
        tags = if (!tags.isNullOrEmpty()) tags.toSet() else null,
        isbn = isbn,
      )
    }
    return null
  }

  override val supportsAppendVolume = true

  override fun getSeriesMetadataFromBook(
    book: BookWithMedia,
    appendVolumeToTitle: Boolean,
  ): SeriesMetadataPatch? {
    getComicInfo(book)?.let { comicInfo ->
      val readingDirection =
        when (comicInfo.manga) {
          Manga.NO -> SeriesMetadata.ReadingDirection.LEFT_TO_RIGHT
          Manga.YES_AND_RIGHT_TO_LEFT -> SeriesMetadata.ReadingDirection.RIGHT_TO_LEFT
          else -> null
        }

      val genres = comicInfo.genre?.split(',')?.mapNotNull { it.trim().ifBlank { null } }
      val series = if (appendVolumeToTitle) computeSeriesFromSeriesAndVolume(comicInfo.series, comicInfo.volume) else comicInfo.series

      return SeriesMetadataPatch(
        title = series,
        titleSort = series,
        status = null,
        summary = null,
        readingDirection = readingDirection,
        publisher = comicInfo.publisher?.ifBlank { null },
        ageRating = comicInfo.ageRating?.ageRating,
        language = if (comicInfo.languageISO != null && BCP47TagValidator.isValid(comicInfo.languageISO!!)) BCP47TagValidator.normalize(comicInfo.languageISO!!) else null,
        genres = if (!genres.isNullOrEmpty()) genres.toSet() else null,
        totalBookCount = comicInfo.count,
        collections =
          comicInfo.seriesGroup
            ?.split(',')
            ?.mapNotNull { it.trim().ifBlank { null } }
            ?.toSet() ?: emptySet(),
      )
    }
    return null
  }

  override fun shouldLibraryHandlePatch(
    library: Library,
    target: MetadataPatchTarget,
  ): Boolean =
    when (target) {
      MetadataPatchTarget.BOOK -> library.importComicInfoBook
      MetadataPatchTarget.SERIES -> library.importComicInfoSeries
      MetadataPatchTarget.READLIST -> library.importComicInfoReadList
      MetadataPatchTarget.COLLECTION -> library.importComicInfoCollection
    }

  private fun getComicInfo(book: BookWithMedia): ComicInfo? {
    try {
      if (book.media.files.none { it.fileName == COMIC_INFO }) {
        logger.debug { "Book does not contain any $COMIC_INFO file: $book" }
        return null
      }

      val fileContent = bookAnalyzer.getFileContent(book, COMIC_INFO)
      return mapper.readValue(fileContent, ComicInfo::class.java)
    } catch (e: Exception) {
      logger.error(e) { "Error while retrieving metadata from $COMIC_INFO" }
      return null
    }
  }

  private fun String.splitWithRole(role: String) =
    split(',').mapNotNull { it.trim().ifBlank { null } }.let { list ->
      if (list.isNotEmpty()) list.map { Author(it, role) } else null
    }
}

fun computeSeriesFromSeriesAndVolume(
  series: String?,
  volume: Int?,
): String? =
  series?.ifBlank { null }?.let { s ->
    s + (volume?.let { if (it != 1) " ($it)" else "" } ?: "")
  }
