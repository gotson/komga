package org.gotson.komga.domain.service

import mu.KotlinLogging
import org.gotson.komga.domain.model.BookPageContent
import org.gotson.komga.domain.model.BookPageNumbered
import org.gotson.komga.domain.model.Library
import org.gotson.komga.domain.model.MediaType
import org.gotson.komga.domain.model.PageHash
import org.gotson.komga.domain.model.PageHashKnown
import org.gotson.komga.domain.persistence.BookRepository
import org.gotson.komga.domain.persistence.MediaRepository
import org.gotson.komga.domain.persistence.PageHashRepository
import org.gotson.komga.infrastructure.configuration.KomgaProperties
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service

private val logger = KotlinLogging.logger {}

@Service
class PageHashLifecycle(
  private val pageHashRepository: PageHashRepository,
  private val mediaRepository: MediaRepository,
  private val bookLifecycle: BookLifecycle,
  private val bookRepository: BookRepository,
  private val komgaProperties: KomgaProperties,
) {

  private val hashableMediaTypes = listOf(MediaType.ZIP.value)

  /**
   * @return a Collection of Pair of BookId/SeriesId
   */
  fun getBookAndSeriesIdsWithMissingPageHash(library: Library): Collection<Pair<String, String>> =
    if (library.hashPages)
      mediaRepository.findAllBookAndSeriesIdsByLibraryIdAndMediaTypeAndWithMissingPageHash(library.id, hashableMediaTypes, komgaProperties.pageHashing)
        .also { logger.info { "Found ${it.size} books with missing page hash" } }
    else {
      logger.info { "Page hashing is not enabled, skipping" }
      emptyList()
    }

  fun getPage(pageHash: PageHash, resizeTo: Int? = null): BookPageContent? {
    val match = pageHashRepository.findMatchesByHash(pageHash, null, Pageable.ofSize(1)).firstOrNull() ?: return null
    val book = bookRepository.findByIdOrNull(match.bookId) ?: return null

    return bookLifecycle.getBookPage(book, match.pageNumber, resizeTo = resizeTo)
  }

  fun getBookPagesToDeleteAutomatically(library: Library): Map<String, Collection<BookPageNumbered>> {
    val hashesAutoDelete = pageHashRepository.findAllKnown(listOf(PageHashKnown.Action.DELETE_AUTO), Pageable.unpaged()).content

    return hashesAutoDelete.map { hash ->
      pageHashRepository.findMatchesByHash(hash, library.id, Pageable.unpaged()).content
        .groupBy(
          { it.bookId },
          {
            BookPageNumbered(
              fileName = it.fileName,
              mediaType = hash.mediaType,
              fileHash = hash.hash,
              fileSize = hash.size,
              pageNumber = it.pageNumber,
            )
          },
        )
    }.flatMap { it.entries }
      .groupBy({ it.key }, { it.value })
      .mapValues { it.value.flatten() }
      .filter { it.value.isNotEmpty() }
  }

  fun createOrUpdate(pageHash: PageHashKnown) {
    if (pageHash.action == PageHashKnown.Action.DELETE_AUTO && pageHash.size == null) throw IllegalArgumentException("cannot create PageHash without size and Action.DELETE_AUTO")

    val existing = pageHashRepository.findKnown(pageHash)
    if (existing == null) {
      pageHashRepository.insert(pageHash, getPage(pageHash, 500)?.content)
    } else {
      pageHashRepository.update(existing.copy(action = pageHash.action))
    }
  }

  fun update(pageHash: PageHashKnown) {
    if (pageHash.action == PageHashKnown.Action.DELETE_AUTO && pageHash.size == null) throw IllegalArgumentException("cannot create PageHash without size and Action.DELETE_AUTO")
  }
}
