package org.gotson.komga.domain.service

import org.gotson.komga.domain.model.BookPageContent
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
    mediaRepository.findAllBookAndSeriesIdsByLibraryIdAndMediaTypeAndWithMissingPageHash(library.id, hashableMediaTypes, komgaProperties.pageHashing)

  fun getPage(pageHash: PageHash, resizeTo: Int? = null): BookPageContent? {
    val match = pageHashRepository.findMatchesByHash(pageHash, Pageable.ofSize(1)).firstOrNull() ?: return null
    val book = bookRepository.findByIdOrNull(match.bookId) ?: return null

    return bookLifecycle.getBookPage(book, match.pageNumber, resizeTo = resizeTo)
  }

  fun createOrUpdate(pageHash: PageHashKnown) {
    if (pageHash.action == PageHashKnown.Action.DELETE_AUTO && pageHash.size == null) throw IllegalArgumentException("cannot create PageHash without size and Action.DELETE_AUTO")

    val existing = pageHashRepository.findKnown(pageHash)
    if (existing == null) {
      pageHashRepository.insert(pageHash, getPage(pageHash, 500)?.content)
    } else {
      pageHashRepository.update(pageHash)
    }
  }

  fun update(pageHash: PageHashKnown) {
    if (pageHash.action == PageHashKnown.Action.DELETE_AUTO && pageHash.size == null) throw IllegalArgumentException("cannot create PageHash without size and Action.DELETE_AUTO")
  }
}
