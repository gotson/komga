package org.gotson.komga.infrastructure.metadata

import org.gotson.komga.domain.model.BookMetadataPatch
import org.gotson.komga.domain.model.BookMetadataPatchCapability
import org.gotson.komga.domain.model.BookWithMedia

interface BookMetadataProvider {
  fun getCapabilities(): Set<BookMetadataPatchCapability>
  fun getBookMetadataFromBook(book: BookWithMedia): BookMetadataPatch?
}
