package org.gotson.komga.infrastructure.archive

import org.gotson.komga.domain.model.BookPage
import org.springframework.stereotype.Service
import java.nio.file.Path
import java.util.zip.ZipFile

@Service
class ZipExtractor(
    private val contentDetector: ContentDetector
) : ArchiveExtractor() {

  override fun getPagesList(path: Path): List<BookPage> =
      ZipFile(path.toFile()).use { zip ->
        zip.entries().toList()
            .filter { !it.isDirectory }
            .map {
              BookPage(
                  it.name,
                  contentDetector.detectMediaType(zip.getInputStream(it))
              )
            }
            .filter { contentDetector.isImage(it.mediaType) }
      }

  override fun getPageStream(path: Path, entryName: String): ByteArray =
      ZipFile(path.toFile()).use {
        it.getInputStream(it.getEntry(entryName)).readBytes()
      }
}