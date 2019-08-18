package org.gotson.komga.infrastructure.archive

import org.gotson.komga.domain.model.BookPage
import org.springframework.stereotype.Service
import java.io.InputStream
import java.nio.file.Path
import java.util.zip.ZipFile

@Service
class ZipExtractor(
    private val contentDetector: ContentDetector
) : ArchiveExtractor() {

  override fun getPagesList(path: Path): List<BookPage> {
    val zip = ZipFile(path.toFile())
    return zip.entries().toList()
        .filter { !it.isDirectory }
        .map {
          BookPage(
              it.name,
              contentDetector.detectMediaType(zip.getInputStream(it))
          )
        }
        .filter { contentDetector.isImage(it.mediaType) }
        .sortedWith(
            compareBy(natSortComparator) { it.fileName }
        )
  }

  override fun getPageStream(path: Path, entryName: String): InputStream =
      ZipFile(path.toFile()).let {
        it.getInputStream(it.getEntry(entryName))
      }
}