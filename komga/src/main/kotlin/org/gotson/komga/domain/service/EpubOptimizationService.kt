package org.gotson.komga.domain.service

import io.github.oshai.kotlinlogging.KotlinLogging
import org.gotson.komga.domain.model.Book
import org.gotson.komga.domain.model.BookWithMedia
import org.gotson.komga.domain.model.TypedBytes
import org.gotson.komga.domain.persistence.LibraryRepository
import org.gotson.komga.domain.persistence.MediaRepository
import org.springframework.stereotype.Service
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.util.zip.GZIPInputStream
import java.util.zip.GZIPOutputStream

private val logger = KotlinLogging.logger {}

/**
 * Service for optimizing EPUB reading performance.
 * Implements lazy loading, caching, and content optimization.
 */
@Service
class EpubOptimizationService(
  private val mediaRepository: MediaRepository,
  private val libraryRepository: LibraryRepository,
  private val epubCacheService: EpubCacheService,
  private val bookAnalyzer: BookAnalyzer,
) {
  /**
   * Get EPUB resource with caching and lazy loading.
   * @param book The book
   * @param resourcePath The resource path within the EPUB
   * @param enableCache Whether to use caching
   * @return Resource content
   */
  fun getEpubResource(
    book: Book,
    resourcePath: String,
    enableCache: Boolean = true,
  ): TypedBytes {
    val library = libraryRepository.findById(book.libraryId)

    // Check cache first if enabled
    if (enableCache && library.epubUseLazyLoading) {
      val cached = epubCacheService.getCachedResource(book.id, resourcePath)
      if (cached != null) {
        logger.trace { "Returning cached EPUB resource: $resourcePath" }
        return cached
      }
    }

    // Load resource
    logger.trace { "Loading EPUB resource: $resourcePath" }
    val media = mediaRepository.findById(book.id)
    val resource = loadEpubResourceInternal(BookWithMedia(book, media), resourcePath)

    // Cache if enabled
    if (enableCache && library.epubUseLazyLoading) {
      epubCacheService.cacheResource(book.id, book.libraryId, resourcePath, resource)
    }

    return resource
  }

  /**
   * Preload critical EPUB resources (CSS, fonts, cover).
   * This improves perceived performance by loading essential resources first.
   */
  fun preloadCriticalResources(book: Book) {
    val library = libraryRepository.findById(book.libraryId)
    if (!library.epubUseLazyLoading) return

    logger.debug { "Preloading critical EPUB resources for book ${book.id}" }

    try {
      val media = mediaRepository.findById(book.id)
      val bookWithMedia = BookWithMedia(book, media)

      // Preload common critical resources
      val criticalPaths = listOf(
        "stylesheet.css",
        "style.css",
        "styles.css",
        "cover.jpg",
        "cover.png",
        "cover.jpeg",
      )

      criticalPaths.forEach { path ->
        try {
          val resource = loadEpubResourceInternal(bookWithMedia, path)
          epubCacheService.cacheResource(book.id, book.libraryId, path, resource)
        } catch (e: Exception) {
          // Resource might not exist, ignore
          logger.trace { "Skipping preload of $path: ${e.message}" }
        }
      }
    } catch (e: Exception) {
      logger.error(e) { "Failed to preload critical EPUB resources" }
    }
  }

  /**
   * Optimize EPUB HTML content for faster rendering.
   * Removes unnecessary whitespace, comments, etc.
   */
  fun optimizeHtmlContent(html: String): String {
    return html
      // Remove HTML comments
      .replace(Regex("<!--.*?-->", RegexOption.DOT_MATCHES_ALL), "")
      // Remove excessive whitespace
      .replace(Regex("\\s+"), " ")
      // Remove whitespace between tags
      .replace(Regex(">\\s+<"), "><")
      .trim()
  }

  /**
   * Compress content for more efficient caching.
   */
  fun compressContent(content: ByteArray): ByteArray {
    ByteArrayOutputStream().use { byteStream ->
      GZIPOutputStream(byteStream).use { gzipStream ->
        gzipStream.write(content)
      }
      return byteStream.toByteArray()
    }
  }

  /**
   * Decompress content.
   */
  fun decompressContent(compressed: ByteArray): ByteArray {
    ByteArrayInputStream(compressed).use { byteStream ->
      GZIPInputStream(byteStream).use { gzipStream ->
        return gzipStream.readBytes()
      }
    }
  }

  /**
   * Internal method to load EPUB resource.
   * This should delegate to the actual EPUB analyzer/reader.
   */
  private fun loadEpubResourceInternal(book: BookWithMedia, resourcePath: String): TypedBytes {
    // This is a placeholder - in real implementation, this would call
    // the actual EPUB resource extraction logic from bookAnalyzer or similar
    // For now, we'll use a basic approach
    try {
      // Delegate to book analyzer - this would need to be implemented
      // based on how Komga currently handles EPUB resources
      return bookAnalyzer.getEpubResource(book, resourcePath)
    } catch (e: Exception) {
      logger.error(e) { "Failed to load EPUB resource: $resourcePath" }
      throw e
    }
  }

  /**
   * Get EPUB optimization statistics.
   */
  fun getOptimizationStats(bookId: String): EpubOptimizationStats {
    val cacheStats = epubCacheService.getCacheStats()

    return EpubOptimizationStats(
      cacheStats = cacheStats,
      optimizationsEnabled = true,
    )
  }

  data class EpubOptimizationStats(
    val cacheStats: EpubCacheService.EpubCacheStats,
    val optimizationsEnabled: Boolean,
  )
}

/**
 * Extension function for BookAnalyzer to get EPUB resources.
 * This is a placeholder that should be implemented based on Komga's EPUB handling.
 */
private fun BookAnalyzer.getEpubResource(book: BookWithMedia, resourcePath: String): TypedBytes {
  // This would need to be implemented based on how Komga handles EPUB internally
  // For now, return a placeholder
  throw UnsupportedOperationException("EPUB resource extraction not yet implemented")
}
