package org.gotson.komga.domain.service

import io.github.oshai.kotlinlogging.KotlinLogging
import org.gotson.komga.domain.model.TypedBytes
import org.gotson.komga.domain.persistence.LibraryRepository
import org.springframework.stereotype.Service
import java.util.Collections
import java.util.concurrent.ConcurrentHashMap

private val logger = KotlinLogging.logger {}

/**
 * Cache service for EPUB content to improve performance.
 * Implements LRU caching with configurable size limits.
 */
@Service
class EpubCacheService(
  private val libraryRepository: LibraryRepository,
) {
  // Book ID -> Resource path -> Cached content
  private val resourceCache = ConcurrentHashMap<String, LinkedHashMap<String, TypedBytes>>()

  // Track access order for LRU eviction
  private val accessOrder = Collections.synchronizedMap(LinkedHashMap<String, Long>())

  /**
   * Get cached EPUB resource.
   * @param bookId The book ID
   * @param resourcePath The resource path within the EPUB
   * @return Cached content or null if not cached
   */
  fun getCachedResource(bookId: String, resourcePath: String): TypedBytes? {
    val bookCache = resourceCache[bookId] ?: return null
    val resource = bookCache[resourcePath]

    if (resource != null) {
      // Update access time for LRU
      accessOrder[cacheKey(bookId, resourcePath)] = System.currentTimeMillis()
      logger.trace { "Cache hit for $bookId:$resourcePath" }
    }

    return resource
  }

  /**
   * Cache EPUB resource.
   * @param bookId The book ID
   * @param libraryId The library ID (for getting cache size config)
   * @param resourcePath The resource path
   * @param content The content to cache
   */
  fun cacheResource(bookId: String, libraryId: String, resourcePath: String, content: TypedBytes) {
    try {
      val library = libraryRepository.findById(libraryId)
      val maxCacheSize = library.epubImageCacheSize

      // Don't cache if disabled
      if (maxCacheSize <= 0) return

      // Get or create book cache
      val bookCache = resourceCache.getOrPut(bookId) {
        // LRU cache with max size
        object : LinkedHashMap<String, TypedBytes>(maxCacheSize, 0.75f, true) {
          override fun removeEldestEntry(eldest: MutableMap.MutableEntry<String, TypedBytes>?): Boolean {
            val shouldRemove = size > maxCacheSize
            if (shouldRemove && eldest != null) {
              logger.trace { "Evicting cache entry: ${eldest.key}" }
              accessOrder.remove(cacheKey(bookId, eldest.key))
            }
            return shouldRemove
          }
        }
      }

      // Add to cache
      synchronized(bookCache) {
        bookCache[resourcePath] = content
        accessOrder[cacheKey(bookId, resourcePath)] = System.currentTimeMillis()
      }

      logger.trace { "Cached resource $bookId:$resourcePath (cache size: ${bookCache.size})" }
    } catch (e: Exception) {
      logger.error(e) { "Failed to cache EPUB resource" }
    }
  }

  /**
   * Clear cache for a specific book.
   */
  fun clearBookCache(bookId: String) {
    val removed = resourceCache.remove(bookId)
    if (removed != null) {
      // Remove from access order tracking
      removed.keys.forEach { resourcePath ->
        accessOrder.remove(cacheKey(bookId, resourcePath))
      }
      logger.debug { "Cleared EPUB cache for book $bookId (${removed.size} resources)" }
    }
  }

  /**
   * Clear all EPUB caches.
   */
  fun clearAllCaches() {
    val bookCount = resourceCache.size
    val resourceCount = resourceCache.values.sumOf { it.size }
    resourceCache.clear()
    accessOrder.clear()
    logger.info { "Cleared EPUB cache ($bookCount books, $resourceCount resources)" }
  }

  /**
   * Get cache statistics.
   */
  fun getCacheStats(): EpubCacheStats {
    val bookCount = resourceCache.size
    val totalResources = resourceCache.values.sumOf { it.size }
    val totalSizeBytes = resourceCache.values
      .flatMap { it.values }
      .sumOf { it.bytes.size.toLong() }

    return EpubCacheStats(
      cachedBooks = bookCount,
      cachedResources = totalResources,
      totalSizeBytes = totalSizeBytes,
      totalSizeMB = totalSizeBytes / (1024.0 * 1024.0),
    )
  }

  private fun cacheKey(bookId: String, resourcePath: String) = "$bookId:$resourcePath"

  data class EpubCacheStats(
    val cachedBooks: Int,
    val cachedResources: Int,
    val totalSizeBytes: Long,
    val totalSizeMB: Double,
  )
}
