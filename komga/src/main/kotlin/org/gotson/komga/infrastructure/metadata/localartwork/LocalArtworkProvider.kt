package org.gotson.komga.infrastructure.metadata.localartwork

import io.github.oshai.kotlinlogging.KotlinLogging
import org.apache.commons.io.FilenameUtils
import org.gotson.komga.domain.model.Book
import org.gotson.komga.domain.model.Dimension
import org.gotson.komga.domain.model.Series
import org.gotson.komga.domain.model.Sidecar
import org.gotson.komga.domain.model.ThumbnailBook
import org.gotson.komga.domain.model.ThumbnailSeries
import org.gotson.komga.infrastructure.image.ImageAnalyzer
import org.gotson.komga.infrastructure.mediacontainer.ContentDetector
import org.gotson.komga.infrastructure.sidecar.SidecarBookConsumer
import org.gotson.komga.infrastructure.sidecar.SidecarSeriesConsumer
import org.springframework.stereotype.Service
import java.nio.file.Files
import kotlin.io.path.extension
import kotlin.io.path.fileSize
import kotlin.io.path.inputStream
import kotlin.io.path.nameWithoutExtension
import kotlin.streams.asSequence

private val logger = KotlinLogging.logger {}

@Service
class LocalArtworkProvider(
  private val contentDetector: ContentDetector,
  private val imageAnalyzer: ImageAnalyzer,
) : SidecarSeriesConsumer,
  SidecarBookConsumer {
  val supportedExtensions = listOf("png", "jpeg", "jpg", "tbn", "webp")
  val supportedSeriesFiles = listOf("cover", "default", "folder", "poster", "series")

  fun getBookThumbnails(book: Book): List<ThumbnailBook> {
    logger.info { "Looking for local thumbnails for book: $book" }
    val bookPath = book.path
    val baseName = bookPath.nameWithoutExtension

    val regex = "${Regex.escape(baseName)}(-\\d+)?".toRegex(RegexOption.IGNORE_CASE)

    return Files.list(bookPath.parent).use { dirStream ->
      dirStream
        .asSequence()
        .filter { Files.isRegularFile(it) }
        .filter { regex.matches(it.nameWithoutExtension) }
        .filter { supportedExtensions.contains(it.extension.lowercase()) }
        .filter { contentDetector.isImage(contentDetector.detectMediaType(it)) }
        .mapIndexed { index, path ->
          logger.info { "Found file: $path" }
          ThumbnailBook(
            url = path.toUri().toURL(),
            type = ThumbnailBook.Type.SIDECAR,
            bookId = book.id,
            selected = index == 0,
            fileSize = path.fileSize(),
            mediaType = contentDetector.detectMediaType(path),
            dimension = imageAnalyzer.getDimension(path.inputStream()) ?: Dimension(0, 0),
          )
        }.toList()
    }
  }

  fun getSeriesThumbnails(series: Series): List<ThumbnailSeries> {
    if (series.oneshot) {
      logger.debug { "Disabled for oneshot series, skipping" }
      return emptyList()
    }

    logger.info { "Looking for local thumbnails for series: $series" }

    return Files.list(series.path).use { dirStream ->
      dirStream
        .asSequence()
        .filter { Files.isRegularFile(it) }
        .filter { supportedSeriesFiles.contains(it.nameWithoutExtension.lowercase()) }
        .filter { supportedExtensions.contains(it.extension.lowercase()) }
        .filter { contentDetector.isImage(contentDetector.detectMediaType(it)) }
        .mapIndexed { index, path ->
          logger.info { "Found file: $path" }
          ThumbnailSeries(
            url = path.toUri().toURL(),
            seriesId = series.id,
            selected = index == 0,
            type = ThumbnailSeries.Type.SIDECAR,
            fileSize = path.fileSize(),
            mediaType = contentDetector.detectMediaType(path),
            dimension = imageAnalyzer.getDimension(path.inputStream()) ?: Dimension(0, 0),
          )
        }.toList()
    }
  }

  override fun getSidecarBookType(): Sidecar.Type = Sidecar.Type.ARTWORK

  override fun getSidecarBookPrefilter(): List<Regex> = supportedExtensions.map { ext -> ".*(-\\d+)?\\.$ext".toRegex(RegexOption.IGNORE_CASE) }

  override fun isSidecarBookMatch(
    basename: String,
    sidecar: String,
  ): Boolean = "${Regex.escape(basename)}(-\\d+)?".toRegex(RegexOption.IGNORE_CASE).matches(FilenameUtils.getBaseName(sidecar))

  override fun getSidecarSeriesType(): Sidecar.Type = Sidecar.Type.ARTWORK

  override fun getSidecarSeriesFilenames(): List<String> =
    supportedSeriesFiles.flatMap { filename ->
      supportedExtensions.map { ext -> "$filename.$ext" }
    }
}
