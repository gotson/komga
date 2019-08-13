package org.gotson.komga.domain.service

import mu.KotlinLogging
import org.apache.commons.io.FilenameUtils
import org.gotson.komga.domain.model.Book
import org.gotson.komga.domain.model.Serie
import org.springframework.stereotype.Service
import java.nio.file.Files
import java.nio.file.Path
import kotlin.streams.asSequence
import kotlin.streams.toList

private val logger = KotlinLogging.logger {}

@Service
class FileSystemScanner(
) {

  val supportedExtensions = listOf("cbr", "rar", "cbz", "zip")

  fun scanRootFolder(root: Path): List<Serie> {
    logger.info { "Scanning folder: $root" }

    return Files.walk(root).asSequence()
        .filter { !Files.isHidden(it) }
        .filter { Files.isDirectory(it) }
        .mapNotNull { dir ->
          val books = Files.list(dir)
              .filter { Files.isRegularFile(it) }
              .filter { supportedExtensions.contains(FilenameUtils.getExtension(it.fileName.toString())) }
              .map {
                Book(
                    name = FilenameUtils.getBaseName(it.fileName.toString()),
                    url = it.toUri().toURL()
                )
              }.toList()
          if (books.isNullOrEmpty()) return@mapNotNull null
          Serie(
              name = dir.fileName.toString(),
              url = dir.toUri().toURL(),
              books = books
          ).also { serie ->
            serie.books.forEach { it.serie = serie }
          }
        }.toList()
  }
}