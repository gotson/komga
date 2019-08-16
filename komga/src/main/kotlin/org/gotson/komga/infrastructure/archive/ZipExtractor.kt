package org.gotson.komga.infrastructure.archive

import net.lingala.zip4j.ZipFile
import org.springframework.stereotype.Service
import java.io.InputStream
import java.nio.file.Path

@Service
class ZipExtractor : ArchiveExtractor() {

  override fun getFilenames(path: Path) =
      ZipFile(path.toFile()).fileHeaders
          .filter { !it.isDirectory }
          .map { it.fileName }
          .sortedWith(natSortComparator)

  override fun getEntryStream(path: Path, entryName: String): InputStream =
      ZipFile(path.toFile()).let {
        it.getInputStream(it.getFileHeader(entryName))
      }
}