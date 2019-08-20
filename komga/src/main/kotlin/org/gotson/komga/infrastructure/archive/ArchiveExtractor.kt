package org.gotson.komga.infrastructure.archive

import net.greypanther.natsort.CaseInsensitiveSimpleNaturalComparator
import org.gotson.komga.domain.model.BookPage
import java.nio.file.Path
import java.util.*

abstract class ArchiveExtractor {
  protected val natSortComparator: Comparator<String> = CaseInsensitiveSimpleNaturalComparator.getInstance()

  abstract fun getPagesList(path: Path): List<BookPage>
  abstract fun getPageStream(path: Path, entryName: String): ByteArray
}