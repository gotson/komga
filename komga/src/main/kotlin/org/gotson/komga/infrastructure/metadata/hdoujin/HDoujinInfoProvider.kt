package org.gotson.komga.infrastructure.metadata.hdoujin

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import io.github.oshai.kotlinlogging.KotlinLogging
import org.gotson.komga.domain.model.Author
import org.gotson.komga.domain.model.BookMetadataPatch
import org.gotson.komga.domain.model.BookMetadataPatchCapability
import org.gotson.komga.domain.model.BookWithMedia
import org.gotson.komga.domain.model.Library
import org.gotson.komga.domain.model.MetadataPatchTarget
import org.gotson.komga.domain.model.SeriesMetadataPatch
import org.gotson.komga.domain.model.WebLink
import org.gotson.komga.infrastructure.metadata.BookMetadataProvider
import org.gotson.komga.infrastructure.metadata.MetadataProvider
import org.gotson.komga.infrastructure.metadata.SeriesMetadataProvider
import org.springframework.stereotype.Service
import java.io.File
import java.time.LocalDate
import java.time.format.DateTimeFormatter

private val logger = KotlinLogging.logger {}

/**
 * Metadata provider for info.json files from HDoujinDownloader.
 * Supports the info.json format used by HDoujinDownloader and similar tools.
 */
@Service
class HDoujinInfoProvider : BookMetadataProvider, SeriesMetadataProvider {
  private val objectMapper = jacksonObjectMapper()

  override val capabilities: Set<BookMetadataPatchCapability> = setOf(
    BookMetadataPatchCapability.TITLE,
    BookMetadataPatchCapability.SUMMARY,
    BookMetadataPatchCapability.AUTHORS,
    BookMetadataPatchCapability.TAGS,
    BookMetadataPatchCapability.RELEASE_DATE,
    BookMetadataPatchCapability.LINKS,
  )

  override fun shouldLibraryHandlePatch(library: Library, target: MetadataPatchTarget): Boolean {
    // Can be configured via library settings if needed
    return true
  }

  override fun getBookMetadataFromBook(book: BookWithMedia): BookMetadataPatch? {
    return try {
      val infoFile = findInfoJson(book)
      if (infoFile != null && infoFile.exists()) {
        parseInfoJson(infoFile)
      } else {
        null
      }
    } catch (e: Exception) {
      logger.error(e) { "Failed to parse info.json for book ${book.name}" }
      null
    }
  }

  override fun getSeriesMetadata(series: org.gotson.komga.domain.model.Series): SeriesMetadataPatch? {
    return try {
      val seriesDir = File(series.url.path)
      val infoFile = File(seriesDir, "info.json")

      if (infoFile.exists()) {
        parseInfoJsonForSeries(infoFile)
      } else {
        null
      }
    } catch (e: Exception) {
      logger.error(e) { "Failed to parse info.json for series ${series.name}" }
      null
    }
  }

  /**
   * Find info.json file for a book.
   * Checks in the same directory as the book file.
   */
  private fun findInfoJson(book: BookWithMedia): File? {
    val bookFile = File(book.url.path)
    val bookDir = bookFile.parentFile

    // Check for info.json in same directory
    val infoFile = File(bookDir, "info.json")
    if (infoFile.exists()) {
      return infoFile
    }

    // Check for book-specific info.json
    val bookName = bookFile.nameWithoutExtension
    val specificInfoFile = File(bookDir, "$bookName.info.json")
    if (specificInfoFile.exists()) {
      return specificInfoFile
    }

    return null
  }

  /**
   * Parse info.json for book metadata.
   */
  private fun parseInfoJson(file: File): BookMetadataPatch {
    val info = objectMapper.readValue<HDoujinInfo>(file)

    val authors = buildList {
      info.artists?.forEach { add(Author(it, "artist")) }
      info.authors?.forEach { add(Author(it, "writer")) }
      info.groups?.forEach { add(Author(it, "group")) }
    }

    val tags = buildSet {
      info.tags?.let { addAll(it) }
      info.categories?.let { addAll(it) }
      info.parodies?.let { addAll(it) }
      info.characters?.let { addAll(it.map { char -> "character:$char" }) }
    }

    val links = buildList {
      info.url?.let { add(WebLink("Source", it)) }
      info.galleryId?.let { add(WebLink("Gallery", "https://nhentai.net/g/$it")) }
    }

    val releaseDate = info.uploadDate?.let { parseDate(it) }

    return BookMetadataPatch(
      title = info.title ?: info.englishTitle,
      summary = info.description,
      authors = authors.ifEmpty { null },
      tags = tags,
      releaseDate = releaseDate,
      links = links.ifEmpty { null },
    )
  }

  /**
   * Parse info.json for series metadata.
   */
  private fun parseInfoJsonForSeries(file: File): SeriesMetadataPatch {
    val info = objectMapper.readValue<HDoujinInfo>(file)

    val authors = buildList {
      info.artists?.forEach { add(Author(it, "artist")) }
      info.authors?.forEach { add(Author(it, "writer")) }
      info.groups?.forEach { add(Author(it, "group")) }
    }

    val genres = buildSet {
      info.categories?.let { addAll(it) }
      info.tags?.let { addAll(it.filter { tag -> !tag.contains(":") }) }
    }

    val tags = buildSet {
      info.tags?.let { addAll(it) }
    }

    val links = buildList {
      info.url?.let { add(WebLink("Source", it)) }
      info.galleryId?.let { add(WebLink("Gallery", "https://nhentai.net/g/$it")) }
    }

    val releaseDate = info.uploadDate?.let { parseDate(it) }

    return SeriesMetadataPatch(
      title = info.title ?: info.englishTitle,
      summary = info.description,
      authors = authors.ifEmpty { null },
      genres = genres,
      tags = tags,
      releaseDate = releaseDate,
      links = links.ifEmpty { null },
    )
  }

  /**
   * Parse date from various formats.
   */
  private fun parseDate(dateStr: String): LocalDate? {
    return try {
      val formats = listOf(
        DateTimeFormatter.ISO_LOCAL_DATE,
        DateTimeFormatter.ofPattern("yyyy-MM-dd"),
        DateTimeFormatter.ofPattern("dd/MM/yyyy"),
        DateTimeFormatter.ofPattern("MM/dd/yyyy"),
      )

      for (format in formats) {
        try {
          return LocalDate.parse(dateStr, format)
        } catch (e: Exception) {
          // Try next format
        }
      }

      null
    } catch (e: Exception) {
      logger.warn { "Failed to parse date: $dateStr" }
      null
    }
  }
}

/**
 * Data class for HDoujinDownloader info.json format.
 */
data class HDoujinInfo(
  val title: String? = null,
  @JsonProperty("english_title")
  val englishTitle: String? = null,
  @JsonProperty("japanese_title")
  val japaneseTitle: String? = null,
  val description: String? = null,
  val artists: List<String>? = null,
  val authors: List<String>? = null,
  val groups: List<String>? = null,
  val tags: List<String>? = null,
  val categories: List<String>? = null,
  val parodies: List<String>? = null,
  val characters: List<String>? = null,
  val languages: List<String>? = null,
  @JsonProperty("upload_date")
  val uploadDate: String? = null,
  val pages: Int? = null,
  val url: String? = null,
  @JsonProperty("gallery_id")
  val galleryId: String? = null,
  val source: String? = null,
)
