package org.gotson.komga.domain.service

import mu.KotlinLogging
import org.gotson.komga.domain.model.Book
import org.gotson.komga.domain.model.Serie
import org.springframework.stereotype.Service
import java.io.File

private val logger = KotlinLogging.logger {}

@Service
class FileSystemScanner(
) {
  fun scanRootFolder(root: String): List<Serie> {
    logger.info { "Scanning folder: $root" }
    return File(root).walk()
        .filter { !it.isHidden }
        .filter { it.isDirectory }
        .filter { it.path != root }
        .mapNotNull { dir ->
          val books = dir.listFiles { f -> f.isFile }
              ?.map {
                Book(
                    name = it.nameWithoutExtension,
                    url = it.toURI().toURL()
                )
              } ?: return@mapNotNull null
          Serie(
              name = dir.name,
              url = dir.toURI().toURL(),
              books = books
          ).also { serie ->
            serie.books.forEach { it.serie = serie }
          }
        }.toList()
  }
}