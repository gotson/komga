package org.gotson.komga.domain.service

import io.github.oshai.kotlinlogging.KotlinLogging
import org.gotson.komga.domain.model.Book
import org.gotson.komga.domain.model.BookWithMedia
import org.gotson.komga.domain.model.TypedBytes
import org.gotson.komga.domain.persistence.LibraryRepository
import org.gotson.komga.domain.persistence.MediaRepository
import org.gotson.komga.infrastructure.image.ImageType
import org.springframework.stereotype.Service
import java.util.concurrent.ConcurrentHashMap

private val logger = KotlinLogging.logger {}

/**
 * Enhanced page content service with support for image splitting (webtoons).
 * This service wraps the standard page serving with optional tall image splitting.
 */
@Service
class PageContentService(
  private val mediaRepository: MediaRepository,
  private val libraryRepository: LibraryRepository,
  private val bookAnalyzer: BookAnalyzer,
  private val imageSplitterService: ImageSplitterService,
) {
  // Cache for split page mappings: bookId -> pageNumber -> List<split bytes>
  private val splitPageCache = ConcurrentHashMap<String, MutableMap<Int, List<ByteArray>>>()

  /**
   * Get page content with optional tall image splitting.
   * @param book The book
   * @param pageNumber The page number (1-indexed)
   * @param splitIndex If the page is split, which split to return (0-indexed), null for unsplit
   * @return Page content bytes
   */
  fun getPageContent(
    book: Book,
    pageNumber: Int,
    splitIndex: Int? = null,
  ): TypedBytes {
    val library = libraryRepository.findById(book.libraryId)
    val media = mediaRepository.findById(book.id)

    // Get original page content
    val originalPage = bookAnalyzer.getPageContent(BookWithMedia(book, media), pageNumber)

    // If splitting is not enabled or no split index requested, return original
    if (!library.splitTallImages || splitIndex == null) {
      return originalPage
    }

    // Check cache first
    val cacheKey = "${book.id}-$pageNumber"
    val cachedSplits = splitPageCache.getOrPut(book.id) { ConcurrentHashMap() }[pageNumber]

    val splits = cachedSplits ?: run {
      // Split the image
      val splitBytes = imageSplitterService.splitTallImage(
        originalPage.bytes,
        library.splitTallImagesHeightThreshold,
        library.splitTallImagesSplitHeight,
      )

      // Cache if split occurred
      if (splitBytes.size > 1) {
        cachedSplits.getOrPut(book.id) { ConcurrentHashMap() }[pageNumber] = splitBytes
        logger.debug { "Cached ${splitBytes.size} splits for page $pageNumber of book ${book.id}" }
      }

      splitBytes
    }

    // Return requested split or original if split index out of bounds
    return if (splitIndex < splits.size) {
      TypedBytes(splits[splitIndex], originalPage.mediaType)
    } else {
      logger.warn { "Split index $splitIndex out of bounds for page $pageNumber (${splits.size} splits)" }
      originalPage
    }
  }

  /**
   * Get information about split pages.
   * @param book The book
   * @param pageNumber The page number
   * @return Number of splits for this page (1 if not split)
   */
  fun getPageSplitCount(book: Book, pageNumber: Int): Int {
    val library = libraryRepository.findById(book.libraryId)
    if (!library.splitTallImages) return 1

    // Check cache
    val cachedSplits = splitPageCache[book.id]?.get(pageNumber)
    if (cachedSplits != null) {
      return cachedSplits.size
    }

    // Get page and check if it should be split
    val media = mediaRepository.findById(book.id)
    val originalPage = bookAnalyzer.getPageContent(BookWithMedia(book, media), pageNumber)

    return if (imageSplitterService.shouldSplitImage(
        originalPage.bytes,
        library.splitTallImagesHeightThreshold,
      )
    ) {
      // Don't split yet, just return potential count
      // This is a heuristic; actual split will happen on first request
      val splits = imageSplitterService.splitTallImage(
        originalPage.bytes,
        library.splitTallImagesHeightThreshold,
        library.splitTallImagesSplitHeight,
      )
      splits.size
    } else {
      1
    }
  }

  /**
   * Clear split page cache for a book.
   */
  fun clearSplitCache(bookId: String) {
    splitPageCache.remove(bookId)
    logger.debug { "Cleared split page cache for book $bookId" }
  }

  /**
   * Clear all split page caches.
   */
  fun clearAllSplitCaches() {
    val count = splitPageCache.size
    splitPageCache.clear()
    logger.info { "Cleared split page cache for $count books" }
  }
}
