package org.gotson.komga.infrastructure.metadata.comicrack

import com.fasterxml.jackson.dataformat.xml.XmlMapper
import mu.KotlinLogging
import org.gotson.komga.domain.model.Author
import org.gotson.komga.domain.model.BookMetadataPatch
import org.gotson.komga.domain.model.BookMetadataPatchCapability
import org.gotson.komga.domain.model.BookWithMedia
import org.gotson.komga.domain.model.SeriesMetadata
import org.gotson.komga.domain.model.SeriesMetadataPatch
import org.gotson.komga.domain.service.BookAnalyzer
import org.gotson.komga.infrastructure.metadata.BookMetadataProvider
import org.gotson.komga.infrastructure.metadata.SeriesMetadataFromBookProvider
import org.gotson.komga.infrastructure.metadata.comicrack.dto.ComicInfo
import org.gotson.komga.infrastructure.metadata.comicrack.dto.Manga
import org.gotson.komga.infrastructure.validation.BCP47TagValidator
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.time.LocalDate

private val logger = KotlinLogging.logger {}

private const val COMIC_INFO = "ComicInfo.xml"

@Service
class ComicInfoProvider(
  @Autowired(required = false) private val mapper: XmlMapper = XmlMapper(),
  private val bookAnalyzer: BookAnalyzer
) : BookMetadataProvider, SeriesMetadataFromBookProvider {

  override fun getCapabilities(): List<BookMetadataPatchCapability> =
    listOf(
      BookMetadataPatchCapability.TITLE,
      BookMetadataPatchCapability.SUMMARY,
      BookMetadataPatchCapability.NUMBER,
      BookMetadataPatchCapability.NUMBER_SORT,
      BookMetadataPatchCapability.RELEASE_DATE,
      BookMetadataPatchCapability.AUTHORS,
      BookMetadataPatchCapability.READ_LISTS,
    )

  override fun getBookMetadataFromBook(book: BookWithMedia): BookMetadataPatch? {
    getComicInfo(book)?.let { comicInfo ->
      val releaseDate = comicInfo.year?.let {
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

      val readLists = mutableListOf<BookMetadataPatch.ReadListEntry>()
      if (!comicInfo.alternateSeries.isNullOrBlank()) {
        readLists.add(
          BookMetadataPatch.ReadListEntry(
            comicInfo.alternateSeries!!,
            comicInfo.alternateNumber?.toIntOrNull()
          )
        )
      }

      comicInfo.storyArc?.let { value ->
        val arcs = value.split(",").mapNotNull { it.trim().ifBlank { null } }
        readLists.addAll(arcs.map { BookMetadataPatch.ReadListEntry(it) })
      }

      return BookMetadataPatch(
        title = comicInfo.title?.ifBlank { null },
        summary = comicInfo.summary?.ifBlank { null },
        number = comicInfo.number?.ifBlank { null },
        numberSort = comicInfo.number?.toFloatOrNull(),
        releaseDate = releaseDate,
        authors = authors.ifEmpty { null },
        readLists = readLists
      )
    }
    return null
  }

  override fun getSeriesMetadataFromBook(book: BookWithMedia): SeriesMetadataPatch? {
    getComicInfo(book)?.let { comicInfo ->
      val readingDirection = when (comicInfo.manga) {
        Manga.NO -> SeriesMetadata.ReadingDirection.LEFT_TO_RIGHT
        Manga.YES_AND_RIGHT_TO_LEFT -> SeriesMetadata.ReadingDirection.RIGHT_TO_LEFT
        else -> null
      }

      val genres = comicInfo.genre?.split(',')?.mapNotNull { it.trim().ifBlank { null } }
      val series = computeSeriesFromSeriesAndVolume(comicInfo.series, comicInfo.volume)

      return SeriesMetadataPatch(
        title = series,
        titleSort = series,
        status = null,
        summary = null,
        readingDirection = readingDirection,
        publisher = comicInfo.publisher?.ifBlank { null },
        ageRating = comicInfo.ageRating?.ageRating,
        language = if (comicInfo.languageISO != null && BCP47TagValidator.isValid(comicInfo.languageISO!!)) comicInfo.languageISO else null,
        genres = if (!genres.isNullOrEmpty()) genres.toSet() else null,
        collections = listOfNotNull(comicInfo.seriesGroup?.ifBlank { null })
      )
    }
    return null
  }

  private fun getComicInfo(book: BookWithMedia): ComicInfo? {
    try {
      if (book.media.files.none { it == COMIC_INFO }) {
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

fun computeSeriesFromSeriesAndVolume(series: String?, volume: Int?): String? =
  series?.ifBlank { null }?.let { s ->
    s + (volume?.let { if (it != 1) " ($it)" else "" } ?: "")
  }
