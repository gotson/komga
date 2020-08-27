package org.gotson.komga.infrastructure.metadata.epub

import org.gotson.komga.domain.model.Author
import org.gotson.komga.domain.model.Book
import org.gotson.komga.domain.model.BookMetadataPatch
import org.gotson.komga.domain.model.Media
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
  private val epubExtractor: EpubExtractor
) : BookMetadataProvider, SeriesMetadataProvider {

  private val relators = mapOf(
    "aut" to "writer",
    "clr" to "colorist",
    "cov" to "cover",
    "edt" to "editor",
    "art" to "penciller",
    "ill" to "penciller"
  )

  override fun getBookMetadataFromBook(book: Book, media: Media): BookMetadataPatch? {
    if (media.mediaType != "application/epub+zip") return null
    epubExtractor.getPackageFile(book.path())?.let { packageFile ->
      val opf = Jsoup.parse(packageFile)

      val title = opf.selectFirst("metadata > dc|title")?.text()
      val description = opf.selectFirst("metadata > dc|description")?.text()
      val date = opf.selectFirst("metadata > dc|date")?.text()?.let { parseDate(it) }

      val creatorRefines = opf.select("metadata > meta[property=role][scheme=marc:relators]")
        .associate { it.attr("refines").removePrefix("#") to it.text() }
      val authors = opf.select("metadata > dc|creator")
        .map {
          val name = it.text()
          val opfRole = it.attr("opf|role").ifBlank { null }
          val id = it.attr("id").ifBlank { null }
          val refineRole = creatorRefines[id]
          val role = opfRole ?: refineRole
          Author(name, relators[role] ?: "writer")
        }

      return BookMetadataPatch(
        title = title,
        summary = description,
        number = null,
        numberSort = null,
        releaseDate = date,
        authors = authors
      )
    }
    return null
  }

  override fun getSeriesMetadataFromBook(book: Book, media: Media): SeriesMetadataPatch? {
    if (media.mediaType != "application/epub+zip") return null
    epubExtractor.getPackageFile(book.path())?.let { packageFile ->
      val opf = Jsoup.parse(packageFile)

      val series = opf.selectFirst("metadata > meta[property=belongs-to-collection]")?.text()
      val publisher = opf.selectFirst("metadata > dc|publisher")?.text()
      val language = opf.selectFirst("metadata > dc|language")?.text()
      val genre = opf.selectFirst("metadata > dc|subject")?.text()

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
        language = if(language != null && BCP47TagValidator.isValid(language)) language else null,
        genres = if(genre != null) setOf(genre) else emptySet(),
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
