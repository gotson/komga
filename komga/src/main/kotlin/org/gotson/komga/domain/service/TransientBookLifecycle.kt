package org.gotson.komga.domain.service

import org.gotson.komga.domain.model.BookPageContent
import org.gotson.komga.domain.model.BookWithMedia
import org.gotson.komga.domain.model.Media
import org.gotson.komga.domain.model.MediaNotReadyException
import org.gotson.komga.domain.persistence.TransientBookRepository
import org.springframework.stereotype.Service
import java.nio.file.Paths

@Service
class TransientBookLifecycle(
  private val transientBookRepository: TransientBookRepository,
  private val bookAnalyzer: BookAnalyzer,
  private val fileSystemScanner: FileSystemScanner,
) {

  fun scanAndPersist(filePath: String): List<BookWithMedia> {
    val books = fileSystemScanner.scanRootFolder(Paths.get(filePath)).values.flatten().map { BookWithMedia(it, Media()) }

    transientBookRepository.saveAll(books)

    return books
  }

  fun analyzeAndPersist(transientBook: BookWithMedia): BookWithMedia {
    val media = bookAnalyzer.analyze(transientBook.book)

    val updated = transientBook.copy(media = media)
    transientBookRepository.save(updated)

    return updated
  }

  @Throws(
    MediaNotReadyException::class,
    IndexOutOfBoundsException::class
  )
  fun getBookPage(transientBook: BookWithMedia, number: Int): BookPageContent {
    val pageContent = bookAnalyzer.getPageContent(transientBook, number)
    val pageMediaType = transientBook.media.pages[number - 1].mediaType

    return BookPageContent(number, pageContent, pageMediaType)
  }
}
