package org.gotson.komga.infrastructure.metadata.epub

import org.apache.commons.validator.routines.ISBNValidator
import org.gotson.komga.domain.model.Author
import org.gotson.komga.domain.model.BCP47TagValidator
import org.gotson.komga.domain.model.BookMetadataPatch
import org.gotson.komga.domain.model.BookMetadataPatchCapability
import org.gotson.komga.domain.model.BookWithMedia
import org.gotson.komga.domain.model.Library
import org.gotson.komga.domain.model.MediaType
import org.gotson.komga.domain.model.MetadataPatchTarget
import org.gotson.komga.domain.model.SeriesMetadata
import org.gotson.komga.domain.model.SeriesMetadataPatch
import org.gotson.komga.infrastructure.mediacontainer.epub.getPackageFileContent
import org.gotson.komga.infrastructure.metadata.BookMetadataProvider
import org.gotson.komga.infrastructure.metadata.SeriesMetadataFromBookProvider
import org.gotson.komga.language.stripAccents
import org.jsoup.Jsoup
import org.jsoup.parser.Parser
import org.jsoup.safety.Safelist
import org.springframework.stereotype.Service
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Service
class EpubMetadataProvider(
  private val isbnValidator: ISBNValidator,
) : BookMetadataProvider,
  SeriesMetadataFromBookProvider {
  private val relators =
    mapOf(
      "aut" to "writer",
      "clr" to "colorist",
      "cov" to "cover",
      "edt" to "editor",
      "art" to "penciller",
      "ill" to "penciller",
      "trl" to "translator",
    )

  override val capabilities =
    setOf(
      BookMetadataPatchCapability.TITLE,
      BookMetadataPatchCapability.SUMMARY,
      BookMetadataPatchCapability.RELEASE_DATE,
      BookMetadataPatchCapability.AUTHORS,
      BookMetadataPatchCapability.ISBN,
    )

  override fun getBookMetadataFromBook(book: BookWithMedia): BookMetadataPatch? {
    if (book.media.mediaType != MediaType.EPUB.type) return null
    getPackageFileContent(book.book.path)?.let { packageFile ->
      val opf = Jsoup.parse(packageFile, "", Parser.xmlParser())

      val title = opf.selectFirst("metadata > dc|title")?.text()?.ifBlank { null }
      val description =
        opf
          .selectFirst("metadata > dc|description")
          ?.text()
          ?.let { Jsoup.clean(it, Safelist.none()) }
          ?.ifBlank { null }
      val date = opf.selectFirst("metadata > dc|date")?.text()?.let { parseDate(it) }

      val authorRoles =
        opf
          .select("metadata > *|meta[property=role][scheme=marc:relators]")
          .associate { it.attr("refines").removePrefix("#") to it.text() }
      val authors =
        opf
          .select("metadata > dc|creator")
          .mapNotNull { el ->
            val name = el.text().trim()
            if (name.isBlank()) {
              null
            } else {
              val opfRole = el.attr("opf:role").ifBlank { null }
              val id = el.attr("id").ifBlank { null }
              val refineRole = authorRoles[id]?.ifBlank { null }
              Author(name, relators[opfRole ?: refineRole] ?: "writer")
            }
          }.ifEmpty { null }

      val isbn =
        opf
          .select("metadata > dc|identifier")
          .map { it.text().lowercase().removePrefix("isbn:") }
          .firstNotNullOfOrNull { isbnValidator.validate(it) }

      val seriesIndex =
        opf
          .selectFirst("metadata > *|meta[property=belongs-to-collection]")
          ?.attr("id")
          ?.let { id ->
            opf.selectFirst("metadata > *|meta[refines=#$id][property=group-position]")
          }?.text()

      return BookMetadataPatch(
        title = title,
        summary = description,
        releaseDate = date,
        authors = authors,
        isbn = isbn,
        number = seriesIndex?.ifBlank { null },
        numberSort = seriesIndex?.toFloatOrNull(),
      )
    }
    return null
  }

  override val supportsAppendVolume = false

  override fun getSeriesMetadataFromBook(
    book: BookWithMedia,
    appendVolumeToTitle: Boolean,
  ): SeriesMetadataPatch? {
    if (book.media.mediaType != MediaType.EPUB.type) return null
    getPackageFileContent(book.book.path)?.let { packageFile ->
      val opf = Jsoup.parse(packageFile, "", Parser.xmlParser())

      val series = opf.selectFirst("metadata > *|meta[property=belongs-to-collection]")?.text()?.ifBlank { null }
      val publisher = opf.selectFirst("metadata > dc|publisher")?.text()?.ifBlank { null }
      val language = opf.selectFirst("metadata > dc|language")?.text()?.ifBlank { null }
      val genres =
        opf
          .select("metadata > dc|subject")
          .mapNotNull { it.text().trim().ifBlank { null } }
          .toSet()
          .ifEmpty { null }

      val direction =
        opf.getElementsByTag("spine").first()?.attr("page-progression-direction")?.let {
          when (it) {
            "rtl" -> SeriesMetadata.ReadingDirection.RIGHT_TO_LEFT
            "ltr" -> SeriesMetadata.ReadingDirection.LEFT_TO_RIGHT
            else -> null
          }
        }

      return SeriesMetadataPatch(
        title = series,
        titleSort = series?.stripAccents(),
        status = null,
        readingDirection = direction,
        publisher = publisher,
        ageRating = null,
        summary = null,
        language = if (language != null && BCP47TagValidator.isValid(language)) BCP47TagValidator.normalize(language) else null,
        genres = genres,
        totalBookCount = null,
        collections = emptySet(),
      )
    }
    return null
  }

  override fun shouldLibraryHandlePatch(
    library: Library,
    target: MetadataPatchTarget,
  ): Boolean =
    when (target) {
      MetadataPatchTarget.BOOK -> library.importEpubBook
      MetadataPatchTarget.SERIES -> library.importEpubSeries
      else -> false
    }

  private fun parseDate(date: String): LocalDate? =
    try {
      LocalDate.parse(date, DateTimeFormatter.ISO_DATE)
    } catch (e: Exception) {
      try {
        LocalDate.parse(date, DateTimeFormatter.ISO_LOCAL_DATE)
      } catch (e: Exception) {
        try {
          LocalDate.parse(date, DateTimeFormatter.ISO_DATE_TIME)
        } catch (e: Exception) {
          null
        }
      }
    }
}
