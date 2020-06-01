package org.gotson.komga.infrastructure.metadata.epub

import org.gotson.komga.domain.model.Author
import org.gotson.komga.domain.model.Book
import org.gotson.komga.domain.model.BookMetadata
import org.gotson.komga.domain.model.BookMetadataPatch
import org.gotson.komga.domain.model.Media
import org.gotson.komga.domain.model.SeriesMetadataPatch
import org.gotson.komga.infrastructure.mediacontainer.EpubExtractor
import org.gotson.komga.infrastructure.metadata.BookMetadataProvider
import org.jsoup.Jsoup
import org.springframework.stereotype.Service
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Service
class EpubMetadataProvider(
  private val epubExtractor: EpubExtractor
) : BookMetadataProvider {

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
      val opf = Jsoup.parse(packageFile.toString())

      val title = opf.selectFirst("metadata > dc|title")?.text()
      val publisher = opf.selectFirst("metadata > dc|publisher")?.text()
      val description = opf.selectFirst("metadata > dc|description")?.text()
      val date = opf.selectFirst("metadata > dc|date")?.text()?.let { parseDate(it) }

      val direction = opf.getElementsByTag("spine").first().attr("page-progression-direction")?.let {
        when (it) {
          "rtl" -> BookMetadata.ReadingDirection.RIGHT_TO_LEFT
          "ltr" -> BookMetadata.ReadingDirection.LEFT_TO_RIGHT
          else -> null
        }
      }

      val creatorRefines = opf.select("metadata > meta[property=role][scheme=marc:relators]")
        .associate { it.attr("refines").removePrefix("#") to it.text() }
      val authors = opf.select("metadata > dc|creator")
        .map {
          val name = it.text()
          val opfRole = it.attr("opf|role").orNull()
          val id = it.attr("id").orNull()
          val refineRole = creatorRefines[id]
          val role = opfRole ?: refineRole
          Author(name, relators[role] ?: "writer")
        }

      val series = opf.selectFirst("metadata > meta[property=belongs-to-collection]")?.text()

      return BookMetadataPatch(
        title = title,
        summary = description,
        number = null,
        numberSort = null,
        readingDirection = direction,
        publisher = publisher,
        ageRating = null,
        releaseDate = date,
        authors = authors,
        series = SeriesMetadataPatch(series, series, null)
      )
    }
    return null
  }

  private fun String.orNull() = if (isBlank()) null else this

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
