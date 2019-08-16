package org.gotson.komga.infrastructure.archive

import net.greypanther.natsort.CaseInsensitiveSimpleNaturalComparator
import java.io.InputStream
import java.nio.file.Path
import java.util.*

abstract class ArchiveExtractor {
  protected val natSortComparator: Comparator<String> = CaseInsensitiveSimpleNaturalComparator.getInstance()

  abstract fun getFilenames(path: Path): List<String>
  abstract fun getEntryStream(path: Path, entryName: String): InputStream
}