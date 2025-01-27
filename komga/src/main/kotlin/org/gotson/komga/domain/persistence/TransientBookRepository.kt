package org.gotson.komga.domain.persistence

import org.gotson.komga.domain.model.TransientBook

interface TransientBookRepository {
  fun findByIdOrNull(transientBookId: String): TransientBook?

  fun save(transientBook: TransientBook)

  fun save(transientBooks: Collection<TransientBook>)
}
