package org.gotson.komga.domain.service

import io.github.oshai.kotlinlogging.KotlinLogging
import org.gotson.komga.domain.model.Dimension
import org.gotson.komga.domain.model.ThumbnailBook
import org.gotson.komga.domain.model.ThumbnailReadList
import org.gotson.komga.domain.model.ThumbnailSeries
import org.gotson.komga.domain.model.ThumbnailSeriesCollection
import org.gotson.komga.domain.persistence.ThumbnailBookRepository
import org.gotson.komga.domain.persistence.ThumbnailReadListRepository
import org.gotson.komga.domain.persistence.ThumbnailSeriesCollectionRepository
import org.gotson.komga.domain.persistence.ThumbnailSeriesRepository
import org.gotson.komga.infrastructure.image.ImageAnalyzer
import org.gotson.komga.infrastructure.mediacontainer.ContentDetector
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import java.net.URL
import java.nio.file.Path
import kotlin.io.path.fileSize
import kotlin.io.path.inputStream
import kotlin.io.path.toPath
import kotlin.reflect.KClass
import kotlin.time.measureTimedValue

private val logger = KotlinLogging.logger {}

@Service
class ThumbnailLifecycle(
  private val thumbnailBookRepository: ThumbnailBookRepository,
  private val thumbnailCollection: ThumbnailSeriesCollectionRepository,
  private val thumbnailReadListRepository: ThumbnailReadListRepository,
  private val thumbnailSeriesRepository: ThumbnailSeriesRepository,
  private val contentDetector: ContentDetector,
  private val imageAnalyzer: ImageAnalyzer,
) {
  /**
   * Find thumbnails without metadata (file size, dimensions, media type),
   * and attempt to fix it.
   *
   * @return true if more thumbnails need fixing
   */
  fun fixThumbnailsMetadata(): Boolean = fixThumbnailMetadataBook() || fixThumbnailMetadataSeries() || fixThumbnailMetadataCollection() || fixThumbnailMetadataReadList()

  private fun fixThumbnailMetadataBook(): Boolean =
    fixThumbnailMetadata(
      ThumbnailBook::class,
      thumbnailBookRepository::findAllWithoutMetadata,
      { t ->
        when {
          t.thumbnail != null -> getMetadata(t.thumbnail)
          t.url != null -> getMetadata(t.url)
          else -> null
        }
      },
      { t, meta ->
        t.copy(
          mediaType = meta.mediaType,
          fileSize = meta.fileSize,
          dimension = meta.dimension,
        )
      },
      thumbnailBookRepository::updateMetadata,
    )

  private fun fixThumbnailMetadataSeries(): Boolean =
    fixThumbnailMetadata(
      ThumbnailSeries::class,
      thumbnailSeriesRepository::findAllWithoutMetadata,
      { t ->
        when {
          t.thumbnail != null -> getMetadata(t.thumbnail)
          t.url != null -> getMetadata(t.url)
          else -> null
        }
      },
      { t, meta ->
        t.copy(
          mediaType = meta.mediaType,
          fileSize = meta.fileSize,
          dimension = meta.dimension,
        )
      },
      thumbnailSeriesRepository::updateMetadata,
    )

  private fun fixThumbnailMetadataCollection(): Boolean =
    fixThumbnailMetadata(
      ThumbnailSeriesCollection::class,
      thumbnailCollection::findAllWithoutMetadata,
      { t -> getMetadata(t.thumbnail) },
      { t, meta ->
        t.copy(
          mediaType = meta.mediaType,
          fileSize = meta.fileSize,
          dimension = meta.dimension,
        )
      },
      thumbnailCollection::updateMetadata,
    )

  private fun fixThumbnailMetadataReadList(): Boolean =
    fixThumbnailMetadata(
      ThumbnailReadList::class,
      thumbnailReadListRepository::findAllWithoutMetadata,
      { t -> getMetadata(t.thumbnail) },
      { t, meta ->
        t.copy(
          mediaType = meta.mediaType,
          fileSize = meta.fileSize,
          dimension = meta.dimension,
        )
      },
      thumbnailReadListRepository::updateMetadata,
    )

  private fun <T : Any> fixThumbnailMetadata(
    clazz: KClass<T>,
    fetcher: (Pageable) -> Page<T>,
    supplier: (T) -> ThumbnailMetadata?,
    copier: (T, ThumbnailMetadata) -> T,
    updater: (Collection<T>) -> Unit,
  ): Boolean {
    val (result, duration) =
      measureTimedValue {
        val thumbs = fetcher(Pageable.ofSize(1000))
        logger.info { "Fetched ${thumbs.numberOfElements} ${clazz.simpleName} to fix, total: ${thumbs.totalElements}" }

        val fixedThumbs =
          thumbs.mapNotNull {
            try {
              val meta = supplier(it)
              if (meta == null)
                null
              else
                copier(it, meta)
            } catch (e: Exception) {
              logger.error(e) { "Could not fix thumbnail: $it" }
              null
            }
          }

        updater(fixedThumbs)
        Result(fixedThumbs.size, (thumbs.numberOfElements < thumbs.totalElements))
      }
    logger.info { "Fixed ${result.processed} ${clazz.simpleName} in $duration" }
    return result.hasMore
  }

  private fun getMetadata(byteArray: ByteArray): ThumbnailMetadata =
    ThumbnailMetadata(
      mediaType = contentDetector.detectMediaType(byteArray.inputStream()),
      fileSize = byteArray.size.toLong(),
      dimension = imageAnalyzer.getDimension(byteArray.inputStream()) ?: Dimension(0, 0),
    )

  private fun getMetadata(url: URL): ThumbnailMetadata =
    ThumbnailMetadata(
      mediaType = contentDetector.detectMediaType(url.toURI().toPath()),
      fileSize = Path.of(url.toURI()).fileSize(),
      dimension = imageAnalyzer.getDimension(url.toURI().toPath().inputStream()) ?: Dimension(0, 0),
    )

  private data class Result(
    val processed: Int,
    val hasMore: Boolean,
  )

  private data class ThumbnailMetadata(
    val mediaType: String,
    val fileSize: Long,
    val dimension: Dimension,
  )
}
