package org.gotson.komga.infrastructure.cache

import com.github.benmanes.caffeine.cache.Caffeine
import mu.KotlinLogging
import org.gotson.komga.domain.model.BookWithMedia
import org.gotson.komga.domain.persistence.TransientBookRepository
import org.springframework.stereotype.Service
import java.util.concurrent.TimeUnit

private val logger = KotlinLogging.logger {}

@Service
class TransientBookCache : TransientBookRepository {
  private val cache = Caffeine.newBuilder()
    .expireAfterAccess(1, TimeUnit.HOURS)
    .build<String, BookWithMedia>()

  override fun findByIdOrNull(transientBookId: String): BookWithMedia? = cache.getIfPresent(transientBookId)

  override fun save(transientBook: BookWithMedia) {
    cache.put(transientBook.book.id, transientBook)
  }

  override fun save(transientBooks: Collection<BookWithMedia>) {
    cache.putAll(transientBooks.associateBy { it.book.id })
  }
}
