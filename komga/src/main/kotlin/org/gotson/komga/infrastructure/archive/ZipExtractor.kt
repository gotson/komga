package org.gotson.komga.infrastructure.archive

import org.springframework.stereotype.Service
import java.io.InputStream
import java.nio.file.Path
import java.util.zip.ZipFile

@Service
class ZipExtractor(
    private val contentDetector: ContentDetector
) : ArchiveExtractor() {

  override fun getFilenames(path: Path): List<String> {
    val zip = ZipFile(path.toFile())
    return zip.entries().toList()
        .filter { !it.isDirectory }
        .filter { contentDetector.isImage(zip.getInputStream(it)) }
        .map { it.name }
        .sortedWith(natSortComparator)
  }

  override fun getEntryStream(path: Path, entryName: String): InputStream =
      ZipFile(path.toFile()).let {
        it.getInputStream(it.getEntry(entryName))
      }
}