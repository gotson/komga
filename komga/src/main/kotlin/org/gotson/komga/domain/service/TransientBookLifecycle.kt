package org.gotson.komga.domain.service

import org.gotson.komga.domain.model.BookPageContent
import org.gotson.komga.domain.model.BookWithMedia
import org.gotson.komga.domain.model.Media
import org.gotson.komga.domain.model.MediaNotReadyException
import org.gotson.komga.domain.model.MediaProfile
import org.gotson.komga.domain.model.PathContainedInPath
import org.gotson.komga.domain.persistence.LibraryRepository
import org.gotson.komga.domain.persistence.TransientBookRepository
import org.gotson.komga.infrastructure.image.ImageType
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Service
import java.nio.file.Paths

@Service
class TransientBookLifecycle(
  private val transientBookRepository: TransientBookRepository,
  private val bookAnalyzer: BookAnalyzer,
  private val fileSystemScanner: FileSystemScanner,
  private val libraryRepository: LibraryRepository,
  @Qualifier("pdfImageType")
  private val pdfImageType: ImageType,
) {

  fun scanAndPersist(filePath: String): List<BookWithMedia> {
    val folderToScan = Paths.get(filePath)

    libraryRepository.findAll().forEach { library ->
      if (folderToScan.startsWith(library.path)) throw PathContainedInPath("Cannot scan folder that is part of an existing library", "ERR_1017")
    }

    val books = fileSystemScanner.scanRootFolder(folderToScan).series.values.flatten().map { BookWithMedia(it, Media()) }

    transientBookRepository.save(books)

    return books
  }

  fun analyzeAndPersist(transientBook: BookWithMedia): BookWithMedia {
    val media = bookAnalyzer.analyze(transientBook.book, true)

    val updated = transientBook.copy(media = media)
    transientBookRepository.save(updated)

    return updated
  }

  @Throws(
    MediaNotReadyException::class,
    IndexOutOfBoundsException::class,
  )
  fun getBookPage(transientBook: BookWithMedia, number: Int): BookPageContent {
    val pageContent = bookAnalyzer.getPageContent(transientBook, number)
    val pageMediaType =
      if (transientBook.media.profile == MediaProfile.PDF) pdfImageType.mediaType
      else transientBook.media.pages[number - 1].mediaType

    return BookPageContent(pageContent, pageMediaType)
  }
}
