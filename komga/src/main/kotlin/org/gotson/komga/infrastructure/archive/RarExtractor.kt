package org.gotson.komga.infrastructure.archive

import com.github.junrar.Archive
import org.gotson.komga.domain.model.BookPage
import org.springframework.stereotype.Service
import java.io.InputStream
import java.nio.file.Files
import java.nio.file.Path

@Service
class RarExtractor(
    private val contentDetector: ContentDetector
) : ArchiveExtractor() {

  override fun getPagesList(path: Path): List<BookPage> {
    val rar = Archive(Files.newInputStream(path))

    return rar.fileHeaders
        .filter { !it.isDirectory }
        .map {
          BookPage(
              it.fileNameString,
              contentDetector.detectMediaType(rar.getInputStream(it))
          )
        }
        .filter { contentDetector.isImage(it.mediaType) }
        .sortedWith(
            compareBy(natSortComparator) { it.fileName }
        )
  }

  override fun getPageStream(path: Path, entryName: String): InputStream {
    val archive = Archive(Files.newInputStream(path))
    val header = archive.fileHeaders.find { it.fileNameString == entryName }
    return archive.getInputStream(header)
  }
}