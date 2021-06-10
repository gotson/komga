package org.gotson.komga.domain.persistence

import org.gotson.komga.domain.model.BookWithMedia

interface TransientBookRepository {
  fun findByIdOrNull(transientBookId: String): BookWithMedia?
  fun save(transientBook: BookWithMedia)
  fun save(transientBooks: Collection<BookWithMedia>)
}
