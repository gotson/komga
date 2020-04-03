package org.gotson.komga.infrastructure.metadata

import org.gotson.komga.domain.model.Book
import org.gotson.komga.domain.model.BookMetadataPatch

interface BookMetadataProvider {
  fun getBookMetadataFromBook(book: Book): BookMetadataPatch?
}
