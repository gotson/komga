package org.gotson.komga.domain.service

import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.gotson.komga.domain.model.Media
import org.gotson.komga.domain.model.makeBook
import org.gotson.komga.domain.model.makeBookPage
import org.gotson.komga.domain.model.makeLibrary
import org.gotson.komga.domain.model.makeSeries
import org.gotson.komga.domain.persistence.BookRepository
import org.gotson.komga.domain.persistence.LibraryRepository
import org.gotson.komga.domain.persistence.MediaRepository
import org.gotson.komga.domain.persistence.SeriesRepository
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.junit.jupiter.SpringExtension
import java.nio.file.Paths

@ExtendWith(SpringExtension::class)
@SpringBootTest
class LibraryScannerTest(
  @Autowired private val seriesRepository: SeriesRepository,
  @Autowired private val libraryRepository: LibraryRepository,
  @Autowired private val bookRepository: BookRepository,
  @Autowired private val libraryScanner: LibraryScanner,
  @Autowired private val bookLifecycle: BookLifecycle,
  @Autowired private val mediaRepository: MediaRepository,
  @Autowired private val libraryLifecycle: LibraryLifecycle
) {

  @MockkBean
  private lateinit var mockScanner: FileSystemScanner

  @MockkBean
  private lateinit var mockAnalyzer: BookAnalyzer

  @AfterEach
  fun `clear repositories`() {
    libraryRepository.findAll().forEach {
      libraryLifecycle.deleteLibrary(it)
    }
  }

  @Test
  fun `given existing series when adding files and scanning then only updated Books are persisted`() {
    // given
    val library = makeLibrary()
    libraryRepository.insert(library)

    val books = listOf(makeBook("book1"))
    val moreBooks = listOf(makeBook("book1"), makeBook("book2"))

    every { mockScanner.scanRootFolder(any()) }.returnsMany(
      mapOf(makeSeries(name = "series") to books),
      mapOf(makeSeries(name = "series") to moreBooks)
    )
    libraryScanner.scanRootFolder(library)

    // when
    libraryScanner.scanRootFolder(library)

    // then
    val allSeries = seriesRepository.findAll()
    val allBooks = bookRepository.findAll().sortedBy { it.number }

    verify(exactly = 2) { mockScanner.scanRootFolder(any()) }

    assertThat(allSeries).hasSize(1)
    assertThat(allBooks).hasSize(2)
    assertThat(allBooks.map { it.name }).containsExactly("book1", "book2")
  }

  @Test
  fun `given existing series when removing files and scanning then only updated Books are persisted`() {
    // given
    val library = makeLibrary()
    libraryRepository.insert(library)

    val books = listOf(makeBook("book1"), makeBook("book2"))
    val lessBooks = listOf(makeBook("book1"))

    every { mockScanner.scanRootFolder(any()) }
      .returnsMany(
        mapOf(makeSeries(name = "series") to books),
        mapOf(makeSeries(name = "series") to lessBooks)
      )
    libraryScanner.scanRootFolder(library)

    // when
    libraryScanner.scanRootFolder(library)

    // then
    val allSeries = seriesRepository.findAll()
    val allBooks = bookRepository.findAll().sortedBy { it.number }

    verify(exactly = 2) { mockScanner.scanRootFolder(any()) }

    assertThat(allSeries).hasSize(1)
    assertThat(allBooks).hasSize(1)
    assertThat(allBooks.map { it.name }).containsExactly("book1")
    assertThat(bookRepository.count()).describedAs("Orphan book has been removed").isEqualTo(1)
  }

  @Test
  fun `given existing series when updating files and scanning then Books are updated`() {
    // given
    val library = makeLibrary()
    libraryRepository.insert(library)

    val books = listOf(makeBook("book1"))
    val updatedBooks = listOf(makeBook("book1"))

    every { mockScanner.scanRootFolder(any()) }
      .returnsMany(
        mapOf(makeSeries(name = "series") to books),
        mapOf(makeSeries(name = "series") to updatedBooks)
      )
    libraryScanner.scanRootFolder(library)

    // when
    libraryScanner.scanRootFolder(library)

    // then
    val allSeries = seriesRepository.findAll()
    val allBooks = bookRepository.findAll()

    verify(exactly = 2) { mockScanner.scanRootFolder(any()) }

    assertThat(allSeries).hasSize(1)
    assertThat(allBooks).hasSize(1)
    assertThat(allBooks.map { it.name }).containsExactly("book1")
    assertThat(allBooks.first().lastModifiedDate).isNotEqualTo(allBooks.first().createdDate)
  }

  @Test
  fun `given existing series when deleting all books and scanning then Series and Books are removed`() {
    // given
    val library = makeLibrary()
    libraryRepository.insert(library)

    every { mockScanner.scanRootFolder(any()) }
      .returnsMany(
        mapOf(makeSeries(name = "series") to listOf(makeBook("book1"))),
        emptyMap()
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
    val library = makeLibrary()
    libraryRepository.insert(library)

    every { mockScanner.scanRootFolder(any()) }
      .returnsMany(
        mapOf(
          makeSeries(name = "series") to listOf(makeBook("book1")),
          makeSeries(name = "series2") to listOf(makeBook("book2"))
        ),
        mapOf(makeSeries(name = "series") to listOf(makeBook("book1")))
      )
    libraryScanner.scanRootFolder(library)

    // when
    libraryScanner.scanRootFolder(library)

    // then
    verify(exactly = 2) { mockScanner.scanRootFolder(any()) }

    assertThat(seriesRepository.count()).describedAs("Series repository should not be empty").isEqualTo(1)
    assertThat(bookRepository.count()).describedAs("Book repository should not be empty").isEqualTo(1)
  }

  @Test
  fun `given existing Book with media when rescanning then media is kept intact`() {
    // given
    val library = makeLibrary()
    libraryRepository.insert(library)

    val book1 = makeBook("book1")
    every { mockScanner.scanRootFolder(any()) }
      .returnsMany(
        mapOf(makeSeries(name = "series") to listOf(book1)),
        mapOf(makeSeries(name = "series") to listOf(makeBook(name = "book1", fileLastModified = book1.fileLastModified)))
      )
    libraryScanner.scanRootFolder(library)

    every { mockAnalyzer.analyze(any()) } returns Media(status = Media.Status.READY, mediaType = "application/zip", pages = mutableListOf(makeBookPage("1.jpg"), makeBookPage("2.jpg")))
    bookRepository.findAll().map { bookLifecycle.analyzeAndPersist(it) }

    // when
    libraryScanner.scanRootFolder(library)

    // then
    verify(exactly = 2) { mockScanner.scanRootFolder(any()) }
    verify(exactly = 1) { mockAnalyzer.analyze(any()) }

    bookRepository.findAll().first().let { book ->
      assertThat(book.lastModifiedDate).isNotEqualTo(book.createdDate)

      mediaRepository.findById(book.id).let { media ->
        assertThat(media.status).isEqualTo(Media.Status.READY)
        assertThat(media.mediaType).isEqualTo("application/zip")
        assertThat(media.pages).hasSize(2)
        assertThat(media.pages.map { it.fileName }).containsExactly("1.jpg", "2.jpg")
      }

    }
  }

  @Test
  fun `given existing Book with different last modified date when rescanning then media is marked as outdated`() {
    // given
    val library = makeLibrary()
    libraryRepository.insert(library)

    val book1 = makeBook("book1")
    every { mockScanner.scanRootFolder(any()) }
      .returnsMany(
        mapOf(makeSeries(name = "series") to listOf(book1)),
        mapOf(makeSeries(name = "series") to listOf(makeBook(name = "book1")))
      )
    libraryScanner.scanRootFolder(library)

    every { mockAnalyzer.analyze(any()) } returns Media(status = Media.Status.READY, mediaType = "application/zip", pages = mutableListOf(makeBookPage("1.jpg"), makeBookPage("2.jpg")))
    bookRepository.findAll().map { bookLifecycle.analyzeAndPersist(it) }

    // when
    libraryScanner.scanRootFolder(library)

    // then
    verify(exactly = 2) { mockScanner.scanRootFolder(any()) }
    verify(exactly = 1) { mockAnalyzer.analyze(any()) }

    bookRepository.findAll().first().let { book ->
      assertThat(book.lastModifiedDate).isNotEqualTo(book.createdDate)

      mediaRepository.findById(book.id).let { media ->
        assertThat(media.status).isEqualTo(Media.Status.OUTDATED)
        assertThat(media.mediaType).isEqualTo("application/zip")
        assertThat(media.pages).hasSize(2)
        assertThat(media.pages.map { it.fileName }).containsExactly("1.jpg", "2.jpg")
      }

    }
  }

  @Test
  fun `given 2 libraries when deleting all books of one and scanning then the other library is kept intact`() {
    // given
    val library1 = makeLibrary(name = "library1")
    libraryRepository.insert(library1)
    val library2 = makeLibrary(name = "library2")
    libraryRepository.insert(library2)

    every { mockScanner.scanRootFolder(Paths.get(library1.root.toURI())) } returns
      mapOf(makeSeries(name = "series1") to listOf(makeBook("book1")))

    every { mockScanner.scanRootFolder(Paths.get(library2.root.toURI())) }.returnsMany(
      mapOf(makeSeries(name = "series2") to listOf(makeBook("book2"))),
      emptyMap()
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
