package org.gotson.komga.infrastructure.archive

import org.gotson.komga.domain.model.BookPage
import java.nio.file.Path

abstract class ArchiveExtractor {
  abstract fun getPagesList(path: Path): List<BookPage>
  abstract fun getPageStream(path: Path, entryName: String): ByteArray
}