package org.gotson.komga.domain.service

import org.gotson.komga.domain.model.BookMetadataPatchCapability
import org.gotson.komga.domain.model.Media
import org.gotson.komga.domain.model.MediaNotReadyException
import org.gotson.komga.domain.model.MediaProfile
import org.gotson.komga.domain.model.PathContainedInPath
import org.gotson.komga.domain.model.TransientBook
import org.gotson.komga.domain.model.TypedBytes
import org.gotson.komga.domain.model.toBookWithMedia
import org.gotson.komga.domain.persistence.LibraryRepository
import org.gotson.komga.domain.persistence.SeriesRepository
import org.gotson.komga.domain.persistence.TransientBookRepository
import org.gotson.komga.infrastructure.image.ImageType
import org.gotson.komga.infrastructure.metadata.BookMetadataProvider
import org.gotson.komga.infrastructure.metadata.SeriesMetadataFromBookProvider
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
  private val seriesRepository: SeriesRepository,
  private val seriesMetadataProviders: List<SeriesMetadataFromBookProvider>,
  bookMetadataProviders: List<BookMetadataProvider>,
) {
  val bookMetadataProviders = bookMetadataProviders.filter { it.capabilities.contains(BookMetadataPatchCapability.NUMBER_SORT) }

  fun scanAndPersist(filePath: String): List<TransientBook> {
    val folderToScan = Paths.get(filePath)

    libraryRepository.findAll().forEach { library ->
      if (folderToScan.startsWith(library.path)) throw PathContainedInPath("Cannot scan folder that is part of an existing library", "ERR_1017")
    }

    val books =
      fileSystemScanner
        .scanRootFolder(folderToScan)
        .series.values
        .flatten()
        .map { TransientBook(it, Media()) }

    transientBookRepository.save(books)

    return books
  }

  fun analyzeAndPersist(transientBook: TransientBook): TransientBook {
    val media = bookAnalyzer.analyze(transientBook.book, true)
    val (seriesId, number) = getMetadata(transientBook.copy(media = media))

    val updated = transientBook.copy(media = media, metadata = TransientBook.Metadata(number, seriesId))
    transientBookRepository.save(updated)

    return updated
  }

  fun getMetadata(transientBook: TransientBook): Pair<String?, Float?> {
    val bookWithMedia = transientBook.toBookWithMedia()
    val number = bookMetadataProviders.firstNotNullOfOrNull { it.getBookMetadataFromBook(bookWithMedia)?.numberSort }
    val series =
      seriesMetadataProviders
        .flatMap {
          buildList {
            if (it.supportsAppendVolume) add(it.getSeriesMetadataFromBook(bookWithMedia, true)?.title)
            add(it.getSeriesMetadataFromBook(bookWithMedia, false)?.title)
          }
        }.filterNotNull()
        .firstNotNullOfOrNull { seriesRepository.findAllByTitleContaining(it).firstOrNull() }

    return series?.id to number
  }

  @Throws(
    MediaNotReadyException::class,
    IndexOutOfBoundsException::class,
  )
  fun getBookPage(
    transientBook: TransientBook,
    number: Int,
  ): TypedBytes {
    val pageContent = bookAnalyzer.getPageContent(transientBook.toBookWithMedia(), number)
    val pageMediaType =
      if (transientBook.media.profile == MediaProfile.PDF)
        pdfImageType.mediaType
      else
        transientBook.media.pages[number - 1].mediaType

    return TypedBytes(pageContent, pageMediaType)
  }
}
