package org.gotson.komga.domain.service

import org.gotson.komga.domain.model.BookPageContent
import org.gotson.komga.domain.persistence.BookRepository
import org.gotson.komga.domain.persistence.PageHashRepository
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service

@Service
class PageHashLifecycle(
  private val pageHashRepository: PageHashRepository,
  private val bookLifecycle: BookLifecycle,
  private val bookRepository: BookRepository,
) {

  fun getPage(hash: String, resizeTo: Int? = null): BookPageContent? {
    val match = pageHashRepository.findMatchesByHash(hash, Pageable.ofSize(1)).firstOrNull() ?: return null
    val book = bookRepository.findByIdOrNull(match.bookId) ?: return null

    return bookLifecycle.getBookPage(book, match.pageNumber, resizeTo = resizeTo)
  }
}
