package org.gotson.komga.infrastructure.metadata

import org.gotson.komga.domain.model.Book
import org.gotson.komga.domain.model.BookMetadataPatch
import org.gotson.komga.domain.model.Media

interface BookMetadataProvider {
  fun getBookMetadataFromBook(book: Book, media: Media): BookMetadataPatch?
}
