package org.gotson.komga.domain.persistence

import org.gotson.komga.domain.model.BookProjection

interface BookProjectionRepository {
  fun save(projection: BookProjection)

  fun delete(bookId: String)

  fun delete(bookIds: Collection<String>)
}
