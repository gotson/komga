package org.gotson.komga.infrastructure.metadata.epub

import org.apache.commons.validator.routines.ISBNValidator
import org.gotson.komga.domain.model.Author
import org.gotson.komga.domain.model.BookMetadataPatch
import org.gotson.komga.domain.model.BookMetadataPatchCapability
import org.gotson.komga.domain.model.BookWithMedia
import org.gotson.komga.domain.model.SeriesMetadata
import org.gotson.komga.domain.model.SeriesMetadataPatch
import org.gotson.komga.infrastructure.mediacontainer.EpubExtractor
import org.gotson.komga.infrastructure.metadata.BookMetadataProvider
import org.gotson.komga.infrastructure.metadata.SeriesMetadataProvider
import org.gotson.komga.infrastructure.validation.BCP47TagValidator
import org.jsoup.Jsoup
import org.springframework.stereotype.Service
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Service
class EpubMetadataProvider(
  private val epubExtractor: EpubExtractor,
  private val isbnValidator: ISBNValidator
) : BookMetadataProvider, SeriesMetadataProvider {

  private val relators = mapOf(
    "aut" to "writer",
    "clr" to "colorist",
    "cov" to "cover",
    "edt" to "editor",
    "art" to "penciller",
    "ill" to "penciller"
  )

  override fun getCapabilities(): List<BookMetadataPatchCapability> =
    listOf(
      BookMetadataPatchCapability.TITLE,
      BookMetadataPatchCapability.SUMMARY,
      BookMetadataPatchCapability.RELEASE_DATE,
      BookMetadataPatchCapability.AUTHORS,
      BookMetadataPatchCapability.ISBN,
    )

  override fun getBookMetadataFromBook(book: BookWithMedia): BookMetadataPatch? {
    if (book.media.mediaType != "application/epub+zip") return null
    epubExtractor.getPackageFile(book.book.path())?.let { packageFile ->
      val opf = Jsoup.parse(packageFile)

      val title = opf.selectFirst("metadata > dc|title")?.text()?.ifBlank { null }
      val description = opf.selectFirst("metadata > dc|description")?.text()?.ifBlank { null }
      val date = opf.selectFirst("metadata > dc|date")?.text()?.let { parseDate(it) }

      val creatorRefines = opf.select("metadata > meta[property=role][scheme=marc:relators]")
        .associate { it.attr("refines").removePrefix("#") to it.text() }
      val authors = opf.select("metadata > dc|creator")
        .flatMap { el ->
          val names = el.text()?.split(",")?.mapNotNull { it.trim().ifBlank { null } } ?: emptyList()
          if (names.isEmpty()) emptyList()
          else {
            val opfRole = el.attr("opf|role").ifBlank { null }
            val id = el.attr("id").ifBlank { null }
            val refineRole = creatorRefines[id]?.ifBlank { null }
            val role = opfRole ?: refineRole
            names.map { Author(it, relators[role] ?: "writer") }
          }
        }

      val isbn = opf.select("metadata > dc|identifier")
        ?.mapNotNull { isbnValidator.validate(it.text()) }
        ?.firstOrNull()

      return BookMetadataPatch(
        title = title,
        summary = description,
        releaseDate = date,
        authors = authors.ifEmpty { null },
        isbn = isbn,
      )
    }
    return null
  }

  override fun getSeriesMetadataFromBook(book: BookWithMedia): SeriesMetadataPatch? {
    if (book.media.mediaType != "application/epub+zip") return null
    epubExtractor.getPackageFile(book.book.path())?.let { packageFile ->
      val opf = Jsoup.parse(packageFile)

      val series = opf.selectFirst("metadata > meta[property=belongs-to-collection]")?.text()?.ifBlank { null }
      val publisher = opf.selectFirst("metadata > dc|publisher")?.text()?.ifBlank { null }
      val language = opf.selectFirst("metadata > dc|language")?.text()?.ifBlank { null }
      val genre = opf.selectFirst("metadata > dc|subject")?.text()?.ifBlank { null }

      val direction = opf.getElementsByTag("spine").first().attr("page-progression-direction")?.let {
        when (it) {
          "rtl" -> SeriesMetadata.ReadingDirection.RIGHT_TO_LEFT
          "ltr" -> SeriesMetadata.ReadingDirection.LEFT_TO_RIGHT
          else -> null
        }
      }

      return SeriesMetadataPatch(
        title = series,
        titleSort = series,
        status = null,
        readingDirection = direction,
        publisher = publisher,
        ageRating = null,
        summary = null,
        language = if (language != null && BCP47TagValidator.isValid(language)) language else null,
        genres = genre?.let { setOf(genre) },
        collections = emptyList()
      )
    }
    return null
  }

  private fun parseDate(date: String): LocalDate? =
    try {
      LocalDate.parse(date, DateTimeFormatter.ISO_DATE)
    } catch (e: Exception) {
      try {
        LocalDate.parse(date, DateTimeFormatter.ISO_LOCAL_DATE)
      } catch (e: Exception) {
        null
      }
    }
}
