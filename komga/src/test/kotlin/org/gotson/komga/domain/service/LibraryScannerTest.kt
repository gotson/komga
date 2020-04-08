package org.gotson.komga.domain.service

import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.gotson.komga.application.service.BookLifecycle
import org.gotson.komga.domain.model.Media
import org.gotson.komga.domain.model.makeBook
import org.gotson.komga.domain.model.makeBookPage
import org.gotson.komga.domain.model.makeLibrary
import org.gotson.komga.domain.model.makeSeries
import org.gotson.komga.domain.persistence.BookRepository
import org.gotson.komga.domain.persistence.LibraryRepository
import org.gotson.komga.domain.persistence.SeriesRepository
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.transaction.PlatformTransactionManager
import org.springframework.transaction.annotation.Transactional
import org.springframework.transaction.support.TransactionTemplate
import java.nio.file.Paths

@ExtendWith(SpringExtension::class)
@SpringBootTest
@AutoConfigureTestDatabase
class LibraryScannerTest(
  @Autowired private val seriesRepository: SeriesRepository,
  @Autowired private val libraryRepository: LibraryRepository,
  @Autowired private val bookRepository: BookRepository,
  @Autowired private val libraryScanner: LibraryScanner,
  @Autowired private val bookLifecycle: BookLifecycle,
  @Autowired private val transactionManager: PlatformTransactionManager
) {

  @MockkBean
  private lateinit var mockScanner: FileSystemScanner

  @MockkBean
  private lateinit var mockAnalyzer: BookAnalyzer

  @AfterEach
  fun `clear repositories`() {
    seriesRepository.deleteAll()
    libraryRepository.deleteAll()
  }

  @Test
  @Transactional
  fun `given existing series when adding files and scanning then only updated Books are persisted`() {
    // given
    val library = libraryRepository.save(makeLibrary())

    val series = makeSeries(name = "series", books = listOf(makeBook("book1")))
    val seriesWithMoreBooks = makeSeries(name = "series", books = listOf(makeBook("book1"), makeBook("book2")))

    every { mockScanner.scanRootFolder(any()) }.returnsMany(
      listOf(series),
      listOf(seriesWithMoreBooks)
    )
    libraryScanner.scanRootFolder(library)

    // when
    libraryScanner.scanRootFolder(library)

    // then
    val allSeries = seriesRepository.findAll()

    verify(exactly = 2) { mockScanner.scanRootFolder(any()) }

    assertThat(allSeries).hasSize(1)
    assertThat(allSeries.first().books).hasSize(2)
    assertThat(allSeries.first().books.map { it.name }).containsExactly("book1", "book2")
  }

  @Test
  @Transactional
  fun `given existing series when removing files and scanning then only updated Books are persisted`() {
    // given
    val library = libraryRepository.save(makeLibrary())

    val series = makeSeries(name = "series", books = listOf(makeBook("book1"), makeBook("book2")))
    val seriesWithLessBooks = makeSeries(name = "series", books = listOf(makeBook("book1")))

    every { mockScanner.scanRootFolder(any()) }
      .returnsMany(
        listOf(series),
        listOf(seriesWithLessBooks)
      )
    libraryScanner.scanRootFolder(library)

    // when
    libraryScanner.scanRootFolder(library)

    // then
    val allSeries = seriesRepository.findAll()

    verify(exactly = 2) { mockScanner.scanRootFolder(any()) }

    assertThat(allSeries).hasSize(1)
    assertThat(allSeries.first().books).hasSize(1)
    assertThat(allSeries.first().books.map { it.name }).containsExactly("book1")
    assertThat(bookRepository.count()).describedAs("Orphan book has been removed").isEqualTo(1)
  }

  @Test
  @Transactional
  fun `given existing series when updating files and scanning then Books are updated`() {
    // given
    val library = libraryRepository.save(makeLibrary())

    val series = makeSeries(name = "series", books = listOf(makeBook("book1")))
    val seriesWithUpdatedBooks = makeSeries(name = "series", books = listOf(makeBook("book1")))

    every { mockScanner.scanRootFolder(any()) }
      .returnsMany(
        listOf(series),
        listOf(seriesWithUpdatedBooks)
      )
    libraryScanner.scanRootFolder(library)

    // when
    libraryScanner.scanRootFolder(library)

    // then
    val allSeries = seriesRepository.findAll()

    verify(exactly = 2) { mockScanner.scanRootFolder(any()) }

    assertThat(allSeries).hasSize(1)
    assertThat(allSeries.first().lastModifiedDate).isNotEqualTo(allSeries.first().createdDate)
    assertThat(allSeries.first().books).hasSize(1)
    assertThat(allSeries.first().books.map { it.name }).containsExactly("book1")
    assertThat(allSeries.first().books.first().lastModifiedDate).isNotEqualTo(allSeries.first().books.first().createdDate)
  }

  @Test
  fun `given existing series when deleting all books and scanning then Series and Books are removed`() {
    // given
    val library = libraryRepository.save(makeLibrary())

    every { mockScanner.scanRootFolder(any()) }
      .returnsMany(
        listOf(makeSeries(name = "series", books = listOf(makeBook("book1")))),
        emptyList()
      )
    libraryScanner.scanRootFolder(library)

    // when
    libraryScanner.scanRootFolder(library)

    // then
    verify(exactly = 2) { mockScanner.scanRootFolder(any()) }

    assertThat(seriesRepository.count()).describedAs("Series repository should be empty").isEqualTo(0)
    assertThat(bookRepository.count()).describedAs("Book repository should be empty").isEqualTo(0)
  }

  @Test
  fun `given existing Series when deleting all books of one series and scanning then series and its Books are removed`() {
    // given
    val library = libraryRepository.save(makeLibrary())

    every { mockScanner.scanRootFolder(any()) }
      .returnsMany(
        listOf(makeSeries(name = "series", books = listOf(makeBook("book1"))), makeSeries(name = "series2", books = listOf(makeBook("book2")))),
        listOf(makeSeries(name = "series", books = listOf(makeBook("book1"))))
      )
    libraryScanner.scanRootFolder(library)

    // when
    libraryScanner.scanRootFolder(library)

    // then
    verify(exactly = 2) { mockScanner.scanRootFolder(any()) }

    assertThat(seriesRepository.count()).describedAs("Series repository should be empty").isEqualTo(1)
    assertThat(bookRepository.count()).describedAs("Book repository should be empty").isEqualTo(1)
  }

  @Test
  fun `given existing Book with media when rescanning then media is kept intact`() {
    // given
    val library = libraryRepository.save(makeLibrary())

    val book1 = makeBook("book1")
    every { mockScanner.scanRootFolder(any()) }
      .returnsMany(
        listOf(makeSeries(name = "series", books = listOf(book1))),
        listOf(makeSeries(name = "series", books = listOf(makeBook(name = "book1", fileLastModified = book1.fileLastModified))))
      )
    libraryScanner.scanRootFolder(library)

    every { mockAnalyzer.analyze(any()) } returns Media(status = Media.Status.READY, mediaType = "application/zip", pages = mutableListOf(makeBookPage("1.jpg"), makeBookPage("2.jpg")))
    bookRepository.findAll().map { bookLifecycle.analyzeAndPersist(it) }.map { it.get() }

    // when
    libraryScanner.scanRootFolder(library)

    // then
    verify(exactly = 2) { mockScanner.scanRootFolder(any()) }
    verify(exactly = 1) { mockAnalyzer.analyze(any()) }

    TransactionTemplate(transactionManager).execute {
      val book = bookRepository.findAll().first()
      assertThat(book.media.status).isEqualTo(Media.Status.READY)
      assertThat(book.media.mediaType).isEqualTo("application/zip")
      assertThat(book.media.pages).hasSize(2)
      assertThat(book.media.pages.map { it.fileName }).containsExactly("1.jpg", "2.jpg")
      assertThat(book.lastModifiedDate).isNotEqualTo(book.createdDate)
    }
  }

  @Test
  fun `given 2 libraries when deleting all books of one and scanning then the other library is kept intact`() {
    // given
    val library1 = libraryRepository.save(makeLibrary(name = "library1"))
    val library2 = libraryRepository.save(makeLibrary(name = "library2"))

    every { mockScanner.scanRootFolder(Paths.get(library1.root.toURI())) } returns
      listOf(makeSeries(name = "series1", books = listOf(makeBook("book1"))))

    every { mockScanner.scanRootFolder(Paths.get(library2.root.toURI())) }.returnsMany(
      listOf(makeSeries(name = "series2", books = listOf(makeBook("book2")))),
      emptyList()
    )

    libraryScanner.scanRootFolder(library1)
    libraryScanner.scanRootFolder(library2)

    assertThat(seriesRepository.count()).describedAs("Series repository should be empty").isEqualTo(2)
    assertThat(bookRepository.count()).describedAs("Book repository should be empty").isEqualTo(2)

    // when
    libraryScanner.scanRootFolder(library2)

    // then
    verify(exactly = 1) { mockScanner.scanRootFolder(Paths.get(library1.root.toURI())) }
    verify(exactly = 2) { mockScanner.scanRootFolder(Paths.get(library2.root.toURI())) }

    assertThat(seriesRepository.count()).describedAs("Series repository should be empty").isEqualTo(1)
    assertThat(bookRepository.count()).describedAs("Book repository should be empty").isEqualTo(1)
  }
}
