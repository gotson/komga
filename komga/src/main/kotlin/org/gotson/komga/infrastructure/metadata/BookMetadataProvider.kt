package org.gotson.komga.infrastructure.metadata

import org.gotson.komga.domain.model.BookMetadataPatch
import org.gotson.komga.domain.model.BookMetadataPatchCapability
import org.gotson.komga.domain.model.BookWithMedia

interface BookMetadataProvider : MetadataProvider {
  val capabilities: Set<BookMetadataPatchCapability>

  fun getBookMetadataFromBook(book: BookWithMedia): BookMetadataPatch?
}
