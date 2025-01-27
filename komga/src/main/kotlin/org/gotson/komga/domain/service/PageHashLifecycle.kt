package org.gotson.komga.domain.service

import io.github.oshai.kotlinlogging.KotlinLogging
import org.gotson.komga.domain.model.BookPageNumbered
import org.gotson.komga.domain.model.Library
import org.gotson.komga.domain.model.MediaType
import org.gotson.komga.domain.model.PageHashKnown
import org.gotson.komga.domain.model.TypedBytes
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
  private val hashableMediaTypes = listOf(MediaType.ZIP.type)

  fun getBookIdsWithMissingPageHash(library: Library): Collection<String> =
    if (library.hashPages) {
      mediaRepository
        .findAllBookIdsByLibraryIdAndMediaTypeAndWithMissingPageHash(library.id, hashableMediaTypes, komgaProperties.pageHashing)
        .also { logger.info { "Found ${it.size} books with missing page hash" } }
    } else {
      logger.info { "Page hashing is not enabled, skipping" }
      emptyList()
    }

  fun getPage(
    pageHash: String,
    resizeTo: Int? = null,
  ): TypedBytes? {
    val match = pageHashRepository.findMatchesByHash(pageHash, Pageable.ofSize(1)).firstOrNull() ?: return null
    val book = bookRepository.findByIdOrNull(match.bookId) ?: return null

    return bookLifecycle.getBookPage(book, match.pageNumber, resizeTo = resizeTo)
  }

  fun getBookPagesToDeleteAutomatically(library: Library): Map<String, Collection<BookPageNumbered>> = pageHashRepository.findMatchesByKnownHashAction(listOf(PageHashKnown.Action.DELETE_AUTO), library.id)

  fun createOrUpdate(pageHash: PageHashKnown) {
    val existing = pageHashRepository.findKnown(pageHash.hash)
    if (existing == null) {
      pageHashRepository.insert(pageHash, getPage(pageHash.hash, 500)?.bytes)
    } else {
      pageHashRepository.update(existing.copy(action = pageHash.action))
    }
  }
}
