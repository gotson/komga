package org.gotson.komga.domain.service

import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.gotson.komga.domain.model.Book
import org.gotson.komga.domain.model.Media
import org.gotson.komga.domain.model.ScanResult
import org.gotson.komga.domain.model.Series
import org.gotson.komga.domain.model.makeBook
import org.gotson.komga.domain.model.makeBookPage
import org.gotson.komga.domain.model.makeLibrary
import org.gotson.komga.domain.model.makeSeries
import org.gotson.komga.domain.persistence.BookRepository
import org.gotson.komga.domain.persistence.LibraryRepository
import org.gotson.komga.domain.persistence.MediaRepository
import org.gotson.komga.domain.persistence.SeriesRepository
import org.gotson.komga.infrastructure.hash.Hasher
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.junit.jupiter.SpringExtension
import java.nio.file.Paths

@ExtendWith(SpringExtension::class)
@SpringBootTest
class LibraryContentLifecycleTest(
  @Autowired private val seriesRepository: SeriesRepository,
  @Autowired private val libraryRepository: LibraryRepository,
  @Autowired private val bookRepository: BookRepository,
  @Autowired private val libraryContentLifecycle: LibraryContentLifecycle,
  @Autowired private val bookLifecycle: BookLifecycle,
  @Autowired private val mediaRepository: MediaRepository,
  @Autowired private val libraryLifecycle: LibraryLifecycle
) {

  @MockkBean
  private lateinit var mockScanner: FileSystemScanner

  @MockkBean
  private lateinit var mockAnalyzer: BookAnalyzer

  @MockkBean
  private lateinit var mockHasher: Hasher

  @AfterEach
  fun `clear repositories`() {
    libraryRepository.findAll().forEach {
      libraryLifecycle.deleteLibrary(it)
    }
  }

  private fun Map<Series, List<Book>>.toScanResult() =
    ScanResult(this, emptyList())

  @Test
  fun `given existing series when adding files and scanning then only updated Books are persisted`() {
    // given
    val library = makeLibrary()
    libraryRepository.insert(library)

    val books = listOf(makeBook("book1"))
    val moreBooks = listOf(makeBook("book1"), makeBook("book2"))

    every { mockScanner.scanRootFolder(any()) }.returnsMany(
      mapOf(makeSeries(name = "series") to books).toScanResult(),
      mapOf(makeSeries(name = "series") to moreBooks).toScanResult(),
    )
    libraryContentLifecycle.scanRootFolder(library)

    // when
    libraryContentLifecycle.scanRootFolder(library)

    // then
    val allSeries = seriesRepository.findAll()
    val allBooks = bookRepository.findAll().sortedBy { it.number }

    verify(exactly = 2) { mockScanner.scanRootFolder(any()) }

    assertThat(allSeries).hasSize(1)
    assertThat(allBooks).hasSize(2)
    assertThat(allBooks.map { it.name }).containsExactly("book1", "book2")
  }

  @Test
  fun `given existing series when removing files and scanning then updated Books are persisted and removed books are marked as such`() {
    // given
    val library = makeLibrary()
    libraryRepository.insert(library)

    val books = listOf(makeBook("book1"), makeBook("book2"))
    val lessBooks = listOf(makeBook("book1"))

    every { mockScanner.scanRootFolder(any()) }
      .returnsMany(
        mapOf(makeSeries(name = "series") to books).toScanResult(),
        mapOf(makeSeries(name = "series") to lessBooks).toScanResult(),
      )
    libraryContentLifecycle.scanRootFolder(library)

    // when
    libraryContentLifecycle.scanRootFolder(library)

    // then
    val allSeries = seriesRepository.findAll()
    val allBooks = bookRepository.findAll().sortedBy { it.number }

    verify(exactly = 2) { mockScanner.scanRootFolder(any()) }

    assertThat(allSeries).hasSize(1)
    assertThat(allBooks).hasSize(2)
    assertThat(allBooks.filter { it.deletedDate == null }.map { it.name }).containsExactly("book1")
    assertThat(allBooks.filter { it.deletedDate != null }.map { it.name }).containsExactly("book2")
  }

  @Test
  fun `given existing series when removing files and scanning, restoring files and scanning then restored books are available`() {
    // given
    val library = makeLibrary()
    libraryRepository.insert(library)

    val books = listOf(makeBook("book1"), makeBook("book2"))
    val lessBooks = listOf(makeBook("book1"))

    every { mockScanner.scanRootFolder(any()) }
      .returnsMany(
        mapOf(makeSeries(name = "series") to books).toScanResult(),
        mapOf(makeSeries(name = "series") to lessBooks).toScanResult(),
        mapOf(makeSeries(name = "series") to books).toScanResult(),
      )
    libraryContentLifecycle.scanRootFolder(library) // creation
    libraryContentLifecycle.scanRootFolder(library) // deletion

    // when
    libraryContentLifecycle.scanRootFolder(library) // restore

    // then
    val allSeries = seriesRepository.findAll()
    val allBooks = bookRepository.findAll().sortedBy { it.number }

    verify(exactly = 3) { mockScanner.scanRootFolder(any()) }

    assertThat(allSeries).hasSize(1)
    assertThat(allBooks).hasSize(2)
    assertThat(allBooks.map { it.deletedDate }).containsOnlyNulls()
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
        mapOf(makeSeries(name = "series") to books).toScanResult(),
        mapOf(makeSeries(name = "series") to updatedBooks).toScanResult(),
      )
    libraryContentLifecycle.scanRootFolder(library)

    // when
    libraryContentLifecycle.scanRootFolder(library)

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
  fun `given existing series when deleting all books and scanning then Series and Books are marked as deleted`() {
    // given
    val library = makeLibrary()
    libraryRepository.insert(library)

    every { mockScanner.scanRootFolder(any()) }
      .returnsMany(
        mapOf(makeSeries(name = "series") to listOf(makeBook("book1"))).toScanResult(),
        emptyMap<Series, List<Book>>().toScanResult(),
      )
    libraryContentLifecycle.scanRootFolder(library)

    // when
    libraryContentLifecycle.scanRootFolder(library)

    // then
    verify(exactly = 2) { mockScanner.scanRootFolder(any()) }

    val allSeries = seriesRepository.findAll()
    val allBooks = bookRepository.findAll()

    assertThat(allSeries.map { it.deletedDate }).doesNotContainNull()
    assertThat(allSeries).hasSize(1)
    assertThat(allBooks.map { it.deletedDate }).doesNotContainNull()
    assertThat(allBooks).hasSize(1)
  }

  @Test
  fun `given existing series when deleting all books and scanning then restoring and scanning then Series and Books are available`() {
    // given
    val library = makeLibrary()
    libraryRepository.insert(library)

    val series = makeSeries(name = "series")
    val book = makeBook("book1")
    every { mockScanner.scanRootFolder(any()) }
      .returnsMany(
        mapOf(series to listOf(book)).toScanResult(),
        emptyMap<Series, List<Book>>().toScanResult(),
        mapOf(series to listOf(book)).toScanResult(),
      )
    libraryContentLifecycle.scanRootFolder(library) // creation
    libraryContentLifecycle.scanRootFolder(library) // deletion

    // when
    libraryContentLifecycle.scanRootFolder(library) // restore

    // then
    verify(exactly = 3) { mockScanner.scanRootFolder(any()) }

    val allSeries = seriesRepository.findAll()
    val allBooks = bookRepository.findAll()

    assertThat(allSeries.map { it.deletedDate }).containsOnlyNulls()
    assertThat(allSeries).hasSize(1)
    assertThat(allBooks.map { it.deletedDate }).containsOnlyNulls()
    assertThat(allBooks).hasSize(1)
  }

  @Test
  fun `given existing Series when deleting all books of one series and scanning then series and its Books are marked as deleted`() {
    // given
    val library = makeLibrary()
    libraryRepository.insert(library)

    every { mockScanner.scanRootFolder(any()) }
      .returnsMany(
        mapOf(
          makeSeries(name = "series") to listOf(makeBook("book1")),
          makeSeries(name = "series2") to listOf(makeBook("book2")),
        ).toScanResult(),
        mapOf(makeSeries(name = "series") to listOf(makeBook("book1"))).toScanResult(),
      )
    libraryContentLifecycle.scanRootFolder(library)

    // when
    libraryContentLifecycle.scanRootFolder(library)

    // then
    verify(exactly = 2) { mockScanner.scanRootFolder(any()) }

    val (series, deletedSeries) = seriesRepository.findAll().partition { it.deletedDate == null }
    val (books, deletedBooks) = bookRepository.findAll().partition { it.deletedDate == null }

    assertThat(series).hasSize(1)
    assertThat(series.map { it.name }).containsExactlyInAnyOrder("series")

    assertThat(deletedSeries).hasSize(1)
    assertThat(deletedSeries.map { it.name }).containsExactlyInAnyOrder("series2")

    assertThat(books).hasSize(1)
    assertThat(books.map { it.name }).containsExactlyInAnyOrder("book1")

    assertThat(deletedBooks).hasSize(1)
    assertThat(deletedBooks.map { it.name }).containsExactlyInAnyOrder("book2")
  }

  @Test
  fun `given existing Book with media when rescanning then media is kept intact`() {
    // given
    val library = makeLibrary()
    libraryRepository.insert(library)

    val book1 = makeBook("book1")
    every { mockScanner.scanRootFolder(any()) }
      .returnsMany(
        mapOf(makeSeries(name = "series") to listOf(book1)).toScanResult(),
        mapOf(makeSeries(name = "series") to listOf(makeBook(name = "book1", fileLastModified = book1.fileLastModified))).toScanResult(),
      )
    libraryContentLifecycle.scanRootFolder(library)

    every { mockAnalyzer.analyze(any()) } returns Media(status = Media.Status.READY, mediaType = "application/zip", pages = mutableListOf(makeBookPage("1.jpg"), makeBookPage("2.jpg")), bookId = book1.id)
    bookRepository.findAll().map { bookLifecycle.analyzeAndPersist(it) }

    // when
    libraryContentLifecycle.scanRootFolder(library)

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
  fun `given existing Book with different last modified date when rescanning then media is marked as outdated and hash is reset`() {
    // given
    val library = makeLibrary()
    libraryRepository.insert(library)

    val book1 = makeBook("book1")
    every { mockScanner.scanRootFolder(any()) }
      .returnsMany(
        mapOf(makeSeries(name = "series") to listOf(book1)).toScanResult(),
        mapOf(makeSeries(name = "series") to listOf(makeBook(name = "book1"))).toScanResult(),
      )
    libraryContentLifecycle.scanRootFolder(library)

    every { mockAnalyzer.analyze(any()) } returns Media(status = Media.Status.READY, mediaType = "application/zip", pages = mutableListOf(makeBookPage("1.jpg"), makeBookPage("2.jpg")), bookId = book1.id)
    every { mockHasher.computeHash(any()) }.returnsMany("abc", "def")

    bookRepository.findAll().map {
      bookLifecycle.analyzeAndPersist(it)
      bookLifecycle.hashAndPersist(it)
    }

    // when
    libraryContentLifecycle.scanRootFolder(library)

    // then
    verify(exactly = 2) { mockScanner.scanRootFolder(any()) }
    verify(exactly = 1) { mockAnalyzer.analyze(any()) }
    verify(exactly = 1) { mockHasher.computeHash(any()) }

    bookRepository.findAll().first().let { book ->
      assertThat(book.lastModifiedDate).isNotEqualTo(book.createdDate)
      assertThat(book.fileHash).isEmpty()

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
      mapOf(makeSeries(name = "series1") to listOf(makeBook("book1"))).toScanResult()

    every { mockScanner.scanRootFolder(Paths.get(library2.root.toURI())) }.returnsMany(
      mapOf(makeSeries(name = "series2") to listOf(makeBook("book2"))).toScanResult(),
      emptyMap<Series, List<Book>>().toScanResult(),
    )

    libraryContentLifecycle.scanRootFolder(library1)
    libraryContentLifecycle.scanRootFolder(library2)

    assertThat(seriesRepository.count()).describedAs("Series repository should not be empty").isEqualTo(2)
    assertThat(bookRepository.count()).describedAs("Book repository should not be empty").isEqualTo(2)

    // when
    libraryContentLifecycle.scanRootFolder(library2)

    // then
    verify(exactly = 1) { mockScanner.scanRootFolder(Paths.get(library1.root.toURI())) }
    verify(exactly = 2) { mockScanner.scanRootFolder(Paths.get(library2.root.toURI())) }

    val (seriesLib1, seriesLib2) = seriesRepository.findAll().partition { it.libraryId == library1.id }
    val (booksLib1, booksLib2) = bookRepository.findAll().partition { it.libraryId == library1.id }

    assertThat(seriesLib1.map { it.deletedDate }).containsOnlyNulls()
    assertThat(seriesLib1.map { it.name }).containsExactlyInAnyOrder("series1")

    assertThat(seriesLib2.map { it.deletedDate }).doesNotContainNull()
    assertThat(seriesLib2.map { it.name }).containsExactlyInAnyOrder("series2")

    assertThat(booksLib1.map { it.deletedDate }).containsOnlyNulls()
    assertThat(booksLib1.map { it.name }).containsExactlyInAnyOrder("book1")

    assertThat(booksLib2.map { it.deletedDate }).doesNotContainNull()
    assertThat(booksLib2.map { it.name }).containsExactlyInAnyOrder("book2")
  }
}
