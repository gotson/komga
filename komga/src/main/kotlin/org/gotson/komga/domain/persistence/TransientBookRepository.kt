package org.gotson.komga.domain.persistence

import org.gotson.komga.domain.model.BookWithMedia

interface TransientBookRepository {
  fun findById(transientBookId: String): BookWithMedia?
  fun save(transientBook: BookWithMedia)
  fun saveAll(transientBooks: Collection<BookWithMedia>)
}
