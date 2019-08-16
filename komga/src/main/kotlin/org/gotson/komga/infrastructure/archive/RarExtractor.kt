package org.gotson.komga.infrastructure.archive

import com.github.junrar.Archive
import org.springframework.stereotype.Service
import java.io.InputStream
import java.nio.file.Files
import java.nio.file.Path

@Service
class RarExtractor(
    private val contentDetector: ContentDetector
) : ArchiveExtractor() {

  override fun getFilenames(path: Path): List<String> {
    val archive = Archive(Files.newInputStream(path))

    return archive.fileHeaders
        .filter { !it.isDirectory }
        .filter { contentDetector.isImage(archive.getInputStream(it)) }
        .map { it.fileNameString }
        .sortedWith(natSortComparator)
  }

  override fun getEntryStream(path: Path, entryName: String): InputStream {
    val archive = Archive(Files.newInputStream(path))
    val header = archive.fileHeaders.find { it.fileNameString == entryName }
    return archive.getInputStream(header)
  }
}