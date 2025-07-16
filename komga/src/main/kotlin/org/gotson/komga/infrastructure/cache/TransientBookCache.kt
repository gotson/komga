package org.gotson.komga.infrastructure.cache

import com.github.benmanes.caffeine.cache.Caffeine
import org.gotson.komga.domain.model.TransientBook
import org.gotson.komga.domain.persistence.TransientBookRepository
import org.springframework.stereotype.Service
import java.util.concurrent.TimeUnit

@Service
class TransientBookCache : TransientBookRepository {
  private val cache =
    Caffeine
      .newBuilder()
      .expireAfterAccess(1, TimeUnit.HOURS)
      .build<String, TransientBook>()

  override fun findByIdOrNull(transientBookId: String): TransientBook? = cache.getIfPresent(transientBookId)

  override fun save(transientBook: TransientBook) {
    cache.put(transientBook.book.id, transientBook)
  }

  override fun save(transientBooks: Collection<TransientBook>) {
    cache.putAll(transientBooks.associateBy { it.book.id })
  }
}
