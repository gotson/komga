package org.gotson.komga.infrastructure.archive

import org.springframework.stereotype.Service
import java.io.InputStream
import java.nio.file.Path
import java.util.zip.ZipFile

@Service
class ZipExtractor : ArchiveExtractor() {

  override fun getFilenames(path: Path) =
      ZipFile(path.toFile()).entries().toList()
          .filter { !it.isDirectory }
          .map { it.name }
          .sortedWith(natSortComparator)

  override fun getEntryStream(path: Path, entryName: String): InputStream =
      ZipFile(path.toFile()).let {
        it.getInputStream(it.getEntry(entryName))
      }
}