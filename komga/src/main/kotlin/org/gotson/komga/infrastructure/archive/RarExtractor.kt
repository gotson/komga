package org.gotson.komga.infrastructure.archive

import com.github.junrar.Archive
import com.github.junrar.Junrar
import org.springframework.stereotype.Service
import java.io.InputStream
import java.nio.file.Files
import java.nio.file.Path

@Service
class RarExtractor : ArchiveExtractor() {

  override fun getFilenames(path: Path): List<String> {
    val contentsDescription = Junrar.getContentsDescription(path.toFile())

    return contentsDescription.map { it.path }.sortedWith(natSortComparator)
  }

  override fun getEntryStream(path: Path, entryName: String): InputStream {
    val archive = Archive(Files.newInputStream(path))
    val header = archive.fileHeaders.find { it.fileNameString == entryName }
    return archive.getInputStream(header)
  }
}