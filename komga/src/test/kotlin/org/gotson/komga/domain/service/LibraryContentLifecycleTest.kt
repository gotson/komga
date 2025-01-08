package org.gotson.komga.domain.service

import com.ninjasquad.springmockk.MockkBean
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.slot
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.catchThrowable
import org.gotson.komga.application.tasks.TaskEmitter
import org.gotson.komga.domain.model.Book
import org.gotson.komga.domain.model.BookMetadataPatchCapability
import org.gotson.komga.domain.model.Dimension
import org.gotson.komga.domain.model.DirectoryNotFoundException
import org.gotson.komga.domain.model.KomgaUser
import org.gotson.komga.domain.model.MarkSelectedPreference
import org.gotson.komga.domain.model.Media
import org.gotson.komga.domain.model.ReadList
import org.gotson.komga.domain.model.Series
import org.gotson.komga.domain.model.SeriesCollection
import org.gotson.komga.domain.model.ThumbnailBook
import org.gotson.komga.domain.model.ThumbnailSeries
import org.gotson.komga.domain.model.makeBook
import org.gotson.komga.domain.model.makeBookPage
import org.gotson.komga.domain.model.makeLibrary
import org.gotson.komga.domain.model.makeSeries
import org.gotson.komga.domain.persistence.BookMetadataRepository
import org.gotson.komga.domain.persistence.BookRepository
import org.gotson.komga.domain.persistence.KomgaUserRepository
import org.gotson.komga.domain.persistence.LibraryRepository
import org.gotson.komga.domain.persistence.MediaRepository
import org.gotson.komga.domain.persistence.ReadListRepository
import org.gotson.komga.domain.persistence.ReadProgressRepository
import org.gotson.komga.domain.persistence.SeriesCollectionRepository
import org.gotson.komga.domain.persistence.SeriesMetadataRepository
import org.gotson.komga.domain.persistence.SeriesRepository
import org.gotson.komga.domain.persistence.ThumbnailBookRepository
import org.gotson.komga.infrastructure.hash.Hasher
import org.gotson.komga.interfaces.api.persistence.SeriesDtoRepository
import org.gotson.komga.language.toIndexedMap
import org.gotson.komga.toScanResult
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.domain.Pageable
import java.net.URL
import java.nio.file.Path
import java.nio.file.Paths
import kotlin.io.path.nameWithoutExtension

@SpringBootTest
class LibraryContentLifecycleTest(
  @Autowired private val seriesRepository: SeriesRepository,
  @Autowired private val libraryRepository: LibraryRepository,
  @Autowired private val bookRepository: BookRepository,
  @Autowired private val libraryContentLifecycle: LibraryContentLifecycle,
  @Autowired private val bookLifecycle: BookLifecycle,
  @Autowired private val seriesLifecycle: SeriesLifecycle,
  @Autowired private val mediaRepository: MediaRepository,
  @Autowired private val bookMetadataRepository: BookMetadataRepository,
  @Autowired private val seriesMetadataRepository: SeriesMetadataRepository,
  @Autowired private val libraryLifecycle: LibraryLifecycle,
  @Autowired private val readListRepository: ReadListRepository,
  @Autowired private val readListLifecycle: ReadListLifecycle,
  @Autowired private val collectionRepository: SeriesCollectionRepository,
  @Autowired private val collectionLifecycle: SeriesCollectionLifecycle,
  @Autowired private val readProgressRepository: ReadProgressRepository,
  @Autowired private val userRepository: KomgaUserRepository,
  @Autowired private val userLifecycle: KomgaUserLifecycle,
  @Autowired private val seriesDtoRepository: SeriesDtoRepository,
  @Autowired private val thumbnailBookRepository: ThumbnailBookRepository,
) {
  @MockkBean
  private lateinit var mockScanner: FileSystemScanner

  @MockkBean
  private lateinit var mockAnalyzer: BookAnalyzer

  @MockkBean
  private lateinit var mockHasher: Hasher

  @MockkBean
  private lateinit var mockTaskEmitter: TaskEmitter

  private val user = KomgaUser("user@example.org", "", id = "1")

  @BeforeAll
  fun setup() {
    userRepository.insert(user)
  }

  @BeforeEach
  fun beforeEach() {
    every { mockTaskEmitter.refreshBookMetadata(any<Book>(), any()) } just Runs
    every { mockTaskEmitter.refreshSeriesMetadata(any<String>(), any()) } just Runs
  }

  @AfterAll
  fun tearDown() {
    userRepository.findAll().forEach {
      userLifecycle.deleteUser(it)
    }
  }

  @AfterEach
  fun `clear repositories`() {
    libraryRepository.findAll().forEach {
      libraryLifecycle.deleteLibrary(it)
    }
  }

  @Nested
  inner class Scan {
    @Test
    fun `given existing series when adding files and scanning then only updated books are persisted`() {
      // given
      val library = makeLibrary()
      libraryRepository.insert(library)

      every { mockScanner.scanRootFolder(any()) }.returnsMany(
        mapOf(makeSeries(name = "series") to listOf(makeBook("book1"))).toScanResult(),
        mapOf(makeSeries(name = "series") to listOf(makeBook("book1"), makeBook("book2"))).toScanResult(),
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
    fun `given existing series when removing files and scanning then updated books are persisted and removed books are marked as such`() {
      // given
      val library = makeLibrary()
      libraryRepository.insert(library)

      every { mockScanner.scanRootFolder(any()) }
        .returnsMany(
          mapOf(makeSeries(name = "series") to listOf(makeBook("book1"), makeBook("book2"))).toScanResult(),
          mapOf(makeSeries(name = "series") to listOf(makeBook("book1"))).toScanResult(),
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
    fun `given existing series when updating files and scanning then books are updated`() {
      // given
      val library = makeLibrary()
      libraryRepository.insert(library)

      every { mockScanner.scanRootFolder(any()) }
        .returnsMany(
          mapOf(makeSeries(name = "series") to listOf(makeBook("book1"))).toScanResult(),
          mapOf(makeSeries(name = "series") to listOf(makeBook("book1"))).toScanResult(),
        )
      libraryContentLifecycle.scanRootFolder(library)

      // when
      libraryContentLifecycle.scanRootFolder(library)

      // then
      val allSeries = seriesRepository.findAll()
      val allBooks = bookRepository.findAll()

      verify(exactly = 2) { mockScanner.scanRootFolder(any()) }
      verify(exactly = 0) { mockHasher.computeHash(any<Path>()) }

      assertThat(allSeries).hasSize(1)
      assertThat(allBooks).hasSize(1)
      val book = allBooks.first()
      assertThat(book.name).isEqualTo("book1")
      assertThat(book.lastModifiedDate).isNotEqualTo(book.createdDate)
      val media = mediaRepository.findById(book.id)
      assertThat(media.status).isEqualTo(Media.Status.OUTDATED)
    }

    @Test
    fun `given existing series when scanning and updated files have a different size then books are marked outdated`() {
      // given
      val library = makeLibrary()
      libraryRepository.insert(library)

      every { mockScanner.scanRootFolder(any()) }
        .returnsMany(
          mapOf(makeSeries(name = "series") to listOf(makeBook("book1").copy(fileSize = 1))).toScanResult(),
          mapOf(makeSeries(name = "series") to listOf(makeBook("book1").copy(fileSize = 2))).toScanResult(),
        )
      libraryContentLifecycle.scanRootFolder(library)

      bookRepository.findAll().first().let { book ->
        bookRepository.update(book.copy(fileHash = "hashed"))
        mediaRepository.update(mediaRepository.findById(book.id).copy(status = Media.Status.READY))
      }

      // when
      libraryContentLifecycle.scanRootFolder(library)

      // then
      val allSeries = seriesRepository.findAll()
      val allBooks = bookRepository.findAll()

      verify(exactly = 2) { mockScanner.scanRootFolder(any()) }
      verify(exactly = 0) { mockHasher.computeHash(any<Path>()) }

      assertThat(allSeries).hasSize(1)
      assertThat(allBooks).hasSize(1)
      val book = allBooks.first()
      assertThat(book.name).isEqualTo("book1")
      assertThat(book.lastModifiedDate).isNotEqualTo(book.createdDate)
      assertThat(book.fileHash).isBlank
      val media = mediaRepository.findById(book.id)
      assertThat(media.status).isEqualTo(Media.Status.OUTDATED)
    }

    @Test
    fun `given existing series when scanning and updated files have the same hash then books are not marked outdated`() {
      // given
      val library = makeLibrary()
      libraryRepository.insert(library)

      every { mockScanner.scanRootFolder(any()) }
        .returnsMany(
          mapOf(makeSeries(name = "series") to listOf(makeBook("book1"))).toScanResult(),
          mapOf(makeSeries(name = "series") to listOf(makeBook("book1"))).toScanResult(),
        )
      libraryContentLifecycle.scanRootFolder(library)

      bookRepository.findAll().first().let { book ->
        bookRepository.update(book.copy(fileHash = "hashed"))
        mediaRepository.update(mediaRepository.findById(book.id).copy(status = Media.Status.READY))
      }

      every { mockHasher.computeHash(any<Path>()) } returns "hashed"

      // when
      libraryContentLifecycle.scanRootFolder(library)

      // then
      val allSeries = seriesRepository.findAll()
      val allBooks = bookRepository.findAll()

      verify(exactly = 2) { mockScanner.scanRootFolder(any()) }
      verify(exactly = 1) { mockHasher.computeHash(any<Path>()) }

      assertThat(allSeries).hasSize(1)
      assertThat(allBooks).hasSize(1)
      val book = allBooks.first()
      assertThat(book.name).isEqualTo("book1")
      assertThat(book.lastModifiedDate).isNotEqualTo(book.createdDate)
      assertThat(book.fileHash).isEqualTo("hashed")
      val media = mediaRepository.findById(book.id)
      assertThat(media.status)
        .isNotEqualTo(Media.Status.OUTDATED)
        .isEqualTo(Media.Status.READY)
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
    fun `given existing series when deleting all books of one series and scanning then series and its books are marked as deleted`() {
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
    fun `given existing book with media when rescanning then media is kept intact`() {
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

      every { mockAnalyzer.analyze(any(), any()) } returns Media(status = Media.Status.READY, mediaType = "application/zip", pages = mutableListOf(makeBookPage("1.jpg"), makeBookPage("2.jpg")), bookId = book1.id)
      bookRepository.findAll().map { bookLifecycle.analyzeAndPersist(it) }

      // when
      libraryContentLifecycle.scanRootFolder(library)

      // then
      verify(exactly = 2) { mockScanner.scanRootFolder(any()) }
      verify(exactly = 1) { mockAnalyzer.analyze(any(), any()) }

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
    fun `given existing book with different last modified date and hash when rescanning then media is marked as outdated and hash is reset`() {
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

      every { mockAnalyzer.analyze(any(), any()) } returns Media(status = Media.Status.READY, mediaType = "application/zip", pages = mutableListOf(makeBookPage("1.jpg"), makeBookPage("2.jpg")), bookId = book1.id)
      every { mockHasher.computeHash(any<Path>()) }.returnsMany("abc", "def")

      bookRepository.findAll().map {
        bookLifecycle.analyzeAndPersist(it)
        bookLifecycle.hashAndPersist(it)
      }

      // when
      libraryContentLifecycle.scanRootFolder(library)

      // then
      verify(exactly = 2) { mockScanner.scanRootFolder(any()) }
      verify(exactly = 1) { mockAnalyzer.analyze(any(), any()) }
      verify(exactly = 2) { mockHasher.computeHash(any<Path>()) }

      bookRepository.findAll().first().let { book ->
        assertThat(book.lastModifiedDate).isNotEqualTo(book.createdDate)
        assertThat(book.fileHash).isEqualTo("def")

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

    @Test
    fun `given library with auto empty trash when scanning then removed series and books are deleted permanently`() {
      // given
      val library = makeLibrary().copy(emptyTrashAfterScan = true)
      libraryRepository.insert(library)

      every { mockScanner.scanRootFolder(any()) }
        .returnsMany(
          mapOf(
            makeSeries(name = "series") to listOf(makeBook("book1"), makeBook("book3")),
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

      assertThat(deletedSeries).isEmpty()

      assertThat(books).hasSize(1)
      assertThat(books.map { it.name }).containsExactlyInAnyOrder("book1")

      assertThat(deletedBooks).isEmpty()
    }

    @Test
    fun `given library when scanning and the root folder is not accessible then exception is thrown`() {
      // given
      val library = makeLibrary().copy(emptyTrashAfterScan = true)
      libraryRepository.insert(library)

      every { mockScanner.scanRootFolder(any()) } returns
        mapOf(
          makeSeries(name = "series") to listOf(makeBook("book1"), makeBook("book3")),
          makeSeries(name = "series2") to listOf(makeBook("book2")),
        ).toScanResult() andThenThrows DirectoryNotFoundException("")

      libraryContentLifecycle.scanRootFolder(library)

      // when
      val thrown = catchThrowable { libraryContentLifecycle.scanRootFolder(library) }

      // then
      verify(exactly = 2) { mockScanner.scanRootFolder(any()) }

      val (series, deletedSeries) = seriesRepository.findAll().partition { it.deletedDate == null }
      val (books, deletedBooks) = bookRepository.findAll().partition { it.deletedDate == null }

      assertThat(series).hasSize(2)
      assertThat(series.map { it.name }).containsExactlyInAnyOrder("series", "series2")

      assertThat(deletedSeries).isEmpty()

      assertThat(books).hasSize(3)
      assertThat(books.map { it.name }).containsExactlyInAnyOrder("book1", "book2", "book3")

      assertThat(deletedBooks).isEmpty()

      assertThat(thrown).isExactlyInstanceOf(DirectoryNotFoundException::class.java)
    }
  }

  @Nested
  inner class Restore {
    @Test
    fun `given existing series when removing files and scanning, restoring files and scanning then restored books are available and media status is not set to outdated`() {
      // given
      val library = makeLibrary()
      libraryRepository.insert(library)

      val book2 = makeBook("book2")

      every { mockScanner.scanRootFolder(any()) }
        .returnsMany(
          mapOf(makeSeries(name = "series") to listOf(makeBook("book1"), book2)).toScanResult(),
          mapOf(makeSeries(name = "series") to listOf(makeBook("book1"))).toScanResult(),
          mapOf(makeSeries(name = "series") to listOf(makeBook("book1"), makeBook("book2"))).toScanResult(),
        )
      libraryContentLifecycle.scanRootFolder(library) // creation

      bookRepository.findByIdOrNull(book2.id)?.let {
        bookRepository.update(it.copy(fileHash = "sameHash"))
        mediaRepository.update(mediaRepository.findById(it.id).copy(status = Media.Status.READY))
        bookMetadataRepository.update(bookMetadataRepository.findById(it.id).copy(tags = setOf("my-tag")))
        bookLifecycle.addThumbnailForBook(ThumbnailBook(ByteArray(10), type = ThumbnailBook.Type.USER_UPLOADED, mediaType = "image/jpeg", fileSize = 10L, dimension = Dimension(1, 1), bookId = it.id), MarkSelectedPreference.YES)
      }

      every { mockHasher.computeHash(any<Path>()) } returns "sameHash"

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

      with(allBooks.last()) {
        assertThat(mediaRepository.findById(id).status).`as` { "Book media should be kept intact" }.isEqualTo(Media.Status.READY)
        assertThat(bookMetadataRepository.findById(id).tags).containsExactlyInAnyOrder("my-tag")
        val thumbnail = bookLifecycle.getThumbnail(id)
        assertThat(thumbnail).isNotNull
        assertThat(thumbnail!!.type).isEqualTo(ThumbnailBook.Type.USER_UPLOADED)
        assertThat(thumbnail.fileSize).isEqualTo(10L)
      }
    }

    @Test
    fun `given existing series when deleting all books and scanning then restoring and scanning then Series and Books are available and media status is not set to outdated`() {
      // given
      val library = makeLibrary()
      libraryRepository.insert(library)

      every { mockScanner.scanRootFolder(any()) }
        .returnsMany(
          mapOf(makeSeries(name = "series") to listOf(makeBook("book1"), makeBook("book2"))).toScanResult(),
          emptyMap<Series, List<Book>>().toScanResult(),
          mapOf(makeSeries(name = "series") to listOf(makeBook("book1"), makeBook("book2"))).toScanResult(),
        )
      libraryContentLifecycle.scanRootFolder(library) // creation

      bookRepository.findAll().forEach { book ->
        bookRepository.update(book.copy(fileHash = "HASH-${book.name}"))
        mediaRepository.findById(book.id).let { mediaRepository.update(it.copy(status = Media.Status.READY)) }
      }

      seriesRepository.findAll().forEach { series ->
        seriesMetadataRepository.findById(series.id).let { seriesMetadataRepository.update(it.copy(language = "en")) }
        seriesLifecycle.addThumbnailForSeries(ThumbnailSeries(ByteArray(10), type = ThumbnailSeries.Type.USER_UPLOADED, mediaType = "image/jpeg", fileSize = 10L, dimension = Dimension(1, 1), seriesId = series.id), MarkSelectedPreference.YES)
      }

      val slot = slot<Path>()
      every { mockHasher.computeHash(capture(slot)) } answers {
        "HASH-${slot.captured.nameWithoutExtension}"
      }

      libraryContentLifecycle.scanRootFolder(library) // deletion

      // when
      libraryContentLifecycle.scanRootFolder(library) // restore

      // then
      verify(exactly = 3) { mockScanner.scanRootFolder(any()) }

      val allSeries = seriesRepository.findAll()
      val allBooks = bookRepository.findAll()

      assertThat(allSeries.map { it.deletedDate }).containsOnlyNulls()
      assertThat(allSeries).hasSize(1)

      allSeries.forEach { series ->
        assertThat(seriesMetadataRepository.findById(series.id).language).isEqualTo("en")
        val thumbnail = seriesLifecycle.getSelectedThumbnail(series.id)
        assertThat(thumbnail).isNotNull
        assertThat(thumbnail!!.type).isEqualTo(ThumbnailSeries.Type.USER_UPLOADED)
        assertThat(thumbnail.fileSize).isEqualTo(10L)
      }

      assertThat(allBooks.map { it.deletedDate }).containsOnlyNulls()
      assertThat(allBooks).hasSize(2)

      allBooks.forEach { book ->
        assertThat(mediaRepository.findById(book.id).status).isEqualTo(Media.Status.READY)
      }
    }
  }

  @Nested
  inner class FileRename {
    @Test
    fun `given existing series when renaming 1 file and scanning then renamed book media and generated thumbnails are kept`() {
      // given
      val library = makeLibrary()
      libraryRepository.insert(library)

      val book = makeBook("book").copy(fileSize = 324)
      val bookRenamed = makeBook("book3").copy(fileSize = 324)

      every { mockScanner.scanRootFolder(any()) }
        .returnsMany(
          mapOf(makeSeries(name = "series") to listOf(book, makeBook("book2"))).toScanResult(),
          mapOf(makeSeries(name = "series") to listOf(bookRenamed, makeBook("book2"))).toScanResult(),
        )
      libraryContentLifecycle.scanRootFolder(library) // creation

      bookRepository.findByIdOrNull(book.id)?.let {
        bookRepository.update(it.copy(fileHash = "sameHash"))
        mediaRepository.update(mediaRepository.findById(it.id).copy(status = Media.Status.READY))
        bookLifecycle.addThumbnailForBook(ThumbnailBook(thumbnail = ByteArray(0), type = ThumbnailBook.Type.GENERATED, bookId = book.id, fileSize = 0, mediaType = "", dimension = Dimension(0, 0)), MarkSelectedPreference.NO)
        bookLifecycle.addThumbnailForBook(ThumbnailBook(url = URL("file:/sidecar"), type = ThumbnailBook.Type.SIDECAR, bookId = book.id, fileSize = 0, mediaType = "", dimension = Dimension(0, 0)), MarkSelectedPreference.NO)
      }

      every { mockHasher.computeHash(any<Path>()) } returns "sameHash"

      // when
      libraryContentLifecycle.scanRootFolder(library) // rename

      // then
      verify(exactly = 1) { mockHasher.computeHash(any<Path>()) }

      val allSeries = seriesRepository.findAll()
      val allBooks = bookRepository.findAll().sortedBy { it.number }

      assertThat(allSeries).hasSize(1)
      assertThat(allBooks).hasSize(2)
      assertThat(allBooks.map { it.deletedDate }).containsOnlyNulls()
      with(allBooks.last()) {
        assertThat(name).`as` { "Book name should have changed to match the filename" }.isEqualTo("book3")
        assertThat(mediaRepository.findById(id).status).`as` { "Book media should be kept intact" }.isEqualTo(Media.Status.READY)
        assertThat(thumbnailBookRepository.findAllByBookIdAndType(id, setOf(ThumbnailBook.Type.SIDECAR))).hasSize(0)
        assertThat(thumbnailBookRepository.findAllByBookIdAndType(id, setOf(ThumbnailBook.Type.GENERATED))).hasSize(1)
      }
    }

    @Test
    fun `given existing series when renaming 1 file and scanning but series modified time did not change then renamed book media and generated thumbnails are kept`() {
      // given
      val library = makeLibrary()
      libraryRepository.insert(library)

      val book = makeBook("book").copy(fileSize = 324)
      val bookRenamed = makeBook("book3").copy(fileSize = 324)
      val series = makeSeries(name = "series")

      every { mockScanner.scanRootFolder(any()) }
        .returnsMany(
          mapOf(series to listOf(book, makeBook("book2"))).toScanResult(),
          mapOf(makeSeries(name = "series").copy(fileLastModified = series.fileLastModified) to listOf(bookRenamed, makeBook("book2"))).toScanResult(),
        )
      libraryContentLifecycle.scanRootFolder(library) // creation

      bookRepository.findByIdOrNull(book.id)?.let {
        bookRepository.update(it.copy(fileHash = "sameHash"))
        mediaRepository.update(mediaRepository.findById(it.id).copy(status = Media.Status.READY))
        bookLifecycle.addThumbnailForBook(ThumbnailBook(thumbnail = ByteArray(0), type = ThumbnailBook.Type.GENERATED, bookId = book.id, fileSize = 0, mediaType = "", dimension = Dimension(0, 0)), MarkSelectedPreference.NO)
        bookLifecycle.addThumbnailForBook(ThumbnailBook(url = URL("file:/sidecar"), type = ThumbnailBook.Type.SIDECAR, bookId = book.id, fileSize = 0, mediaType = "", dimension = Dimension(0, 0)), MarkSelectedPreference.NO)
      }

      every { mockHasher.computeHash(any<Path>()) } returns "sameHash"

      // when
      libraryContentLifecycle.scanRootFolder(library) // rename

      // then
      verify(exactly = 1) { mockHasher.computeHash(any<Path>()) }

      val allSeries = seriesRepository.findAll()
      val allBooks = bookRepository.findAll().sortedBy { it.number }

      assertThat(allSeries).hasSize(1)
      assertThat(allBooks).hasSize(2)
      assertThat(allBooks.map { it.deletedDate }).containsOnlyNulls()
      with(allBooks.last()) {
        assertThat(name).`as` { "Book name should have changed to match the filename" }.isEqualTo("book3")
        assertThat(mediaRepository.findById(id).status).`as` { "Book media should be kept intact" }.isEqualTo(Media.Status.READY)
        assertThat(thumbnailBookRepository.findAllByBookIdAndType(id, setOf(ThumbnailBook.Type.SIDECAR))).hasSize(0)
        assertThat(thumbnailBookRepository.findAllByBookIdAndType(id, setOf(ThumbnailBook.Type.GENERATED))).hasSize(1)
      }
    }

    @Test
    fun `given existing series when renaming 1 file and scanning then renamed book read progress is kept`() {
      // given
      val library = makeLibrary()
      libraryRepository.insert(library)

      val book = makeBook("book").copy(fileSize = 324)
      val bookRenamed = makeBook("book3").copy(fileSize = 324)

      every { mockScanner.scanRootFolder(any()) }
        .returnsMany(
          mapOf(makeSeries(name = "series") to listOf(book, makeBook("book2"))).toScanResult(),
          mapOf(makeSeries(name = "series") to listOf(bookRenamed, makeBook("book2"))).toScanResult(),
        )
      libraryContentLifecycle.scanRootFolder(library) // creation

      bookRepository.findByIdOrNull(book.id)?.let {
        bookRepository.update(it.copy(fileHash = "sameHash"))
        bookLifecycle.markReadProgressCompleted(it.id, user)
      }

      every { mockHasher.computeHash(any<Path>()) } returns "sameHash"

      // when
      libraryContentLifecycle.scanRootFolder(library) // rename

      // then
      verify(exactly = 1) { mockHasher.computeHash(any<Path>()) }

      val allSeries = seriesRepository.findAll()
      val allBooks = bookRepository.findAll().sortedBy { it.number }

      assertThat(allSeries).hasSize(1)
      assertThat(allBooks).hasSize(2)
      assertThat(allBooks.map { it.deletedDate }).containsOnlyNulls()
      with(allBooks.last()) {
        assertThat(name).isEqualTo("book3")
        assertThat(readProgressRepository.findByBookIdAndUserIdOrNull(id, user.id)).isNotNull
      }

      assertThat(readProgressRepository.findAll()).hasSize(1)
    }

    @Test
    fun `given existing series when renaming 1 file and scanning then renamed book is still in read lists`() {
      // given
      val library = makeLibrary()
      libraryRepository.insert(library)

      val book = makeBook("book").copy(fileSize = 324)
      val bookRenamed = makeBook("book3").copy(fileSize = 324)

      every { mockScanner.scanRootFolder(any()) }
        .returnsMany(
          mapOf(makeSeries(name = "series") to listOf(book, makeBook("book2"))).toScanResult(),
          mapOf(makeSeries(name = "series") to listOf(bookRenamed, makeBook("book2"))).toScanResult(),
        )
      libraryContentLifecycle.scanRootFolder(library) // creation

      bookRepository.findByIdOrNull(book.id)?.let {
        bookRepository.update(it.copy(fileHash = "sameHash"))
        readListLifecycle.addReadList(ReadList("read list", bookIds = listOf(it.id).toIndexedMap()))
      }

      every { mockHasher.computeHash(any<Path>()) } returns "sameHash"

      // when
      libraryContentLifecycle.scanRootFolder(library) // rename

      // then
      verify(exactly = 1) { mockHasher.computeHash(any<Path>()) }

      val allSeries = seriesRepository.findAll()
      val allBooks = bookRepository.findAll().sortedBy { it.number }

      assertThat(allSeries).hasSize(1)
      assertThat(allBooks).hasSize(2)
      assertThat(allBooks.map { it.deletedDate }).containsOnlyNulls()
      with(allBooks.last()) {
        assertThat(name).isEqualTo("book3")

        readListRepository.findAllContainingBookId(id, null).let { readLists ->
          assertThat(readLists).hasSize(1)
          assertThat(readLists.first().name).isEqualTo("read list")
        }
      }
    }

    @Test
    fun `given existing series when renaming 1 file with locked title and scanning then renamed book's title is not changed and book metadata is not refreshed for title`() {
      // given
      val library = makeLibrary()
      libraryRepository.insert(library)

      val book = makeBook("book").copy(fileSize = 324)
      val bookRenamed = makeBook("book3").copy(fileSize = 324)

      every { mockScanner.scanRootFolder(any()) }
        .returnsMany(
          mapOf(makeSeries(name = "series") to listOf(book, makeBook("book2"))).toScanResult(),
          mapOf(makeSeries(name = "series") to listOf(bookRenamed, makeBook("book2"))).toScanResult(),
        )
      libraryContentLifecycle.scanRootFolder(library) // creation

      bookRepository.findByIdOrNull(book.id)?.let {
        bookRepository.update(it.copy(fileHash = "sameHash"))
        bookMetadataRepository.update(
          bookMetadataRepository.findById(it.id).copy(
            title = "Updated Title",
            titleLock = true,
          ),
        )
      }

      every { mockHasher.computeHash(any<Path>()) } returns "sameHash"

      // when
      libraryContentLifecycle.scanRootFolder(library) // rename

      // then
      verify(exactly = 1) { mockHasher.computeHash(any<Path>()) }
      verify(exactly = 0) { mockTaskEmitter.refreshBookMetadata(bookRenamed, setOf(BookMetadataPatchCapability.TITLE)) }

      val allSeries = seriesRepository.findAll()
      val allBooks = bookRepository.findAll().sortedBy { it.number }

      assertThat(allSeries).hasSize(1)
      assertThat(allBooks).hasSize(2)
      assertThat(allBooks.map { it.deletedDate }).containsOnlyNulls()
      with(allBooks.last()) {
        assertThat(name).isEqualTo("book3")
        bookMetadataRepository.findById(id).let {
          assertThat(it.title).isEqualTo("Updated Title")
          assertThat(it.titleLock).isTrue
        }
      }
    }

    @Test
    fun `given existing series when renaming 1 file and scanning then renamed book's title matches the filename and book metadata is refreshed for title only`() {
      // given
      val library = makeLibrary()
      libraryRepository.insert(library)

      val book = makeBook("book").copy(fileSize = 324)
      val bookRenamed = makeBook("book3").copy(fileSize = 324)

      every { mockScanner.scanRootFolder(any()) }
        .returnsMany(
          mapOf(makeSeries(name = "series") to listOf(book, makeBook("book2"))).toScanResult(),
          mapOf(makeSeries(name = "series") to listOf(bookRenamed, makeBook("book2"))).toScanResult(),
        )
      libraryContentLifecycle.scanRootFolder(library) // creation

      bookRepository.findByIdOrNull(book.id)?.let {
        bookRepository.update(it.copy(fileHash = "sameHash"))
      }

      every { mockHasher.computeHash(any<Path>()) } returns "sameHash"

      // when
      libraryContentLifecycle.scanRootFolder(library) // rename

      // then
      verify(exactly = 1) { mockHasher.computeHash(any<Path>()) }
      verify(exactly = 1) { mockTaskEmitter.refreshBookMetadata(withArg<Book> { assertThat(it.id).isEqualTo(bookRenamed.id) }, setOf(BookMetadataPatchCapability.TITLE)) }

      val allSeries = seriesRepository.findAll()
      val allBooks = bookRepository.findAll().sortedBy { it.number }

      assertThat(allSeries).hasSize(1)
      assertThat(allBooks).hasSize(2)
      assertThat(allBooks.map { it.deletedDate }).containsOnlyNulls()
      with(allBooks.last()) {
        assertThat(name).isEqualTo("book3")
        assertThat(bookMetadataRepository.findById(id).title).`as` { "Book metadata title should have changed to match the filename" }.isEqualTo("book3")
      }
    }

    @Test
    fun `given series when renaming all files in its folder and scanning then series books media is kept`() {
      // given
      val library = makeLibrary()
      libraryRepository.insert(library)

      every { mockScanner.scanRootFolder(any()) }
        .returnsMany(
          mapOf(makeSeries(name = "series1") to listOf(makeBook("book1").copy(fileSize = 1))).toScanResult(),
          mapOf(makeSeries(name = "series1") to listOf(makeBook("book2").copy(fileSize = 1))).toScanResult(),
        )
      libraryContentLifecycle.scanRootFolder(library) // creation

      seriesRepository.findAll().first().let {
        seriesMetadataRepository.update(seriesMetadataRepository.findById(it.id).copy(summary = "Summary"))
      }

      bookRepository.findAll().forEach { book ->
        bookRepository.update(book.copy(fileHash = "sameHash"))
        mediaRepository.findById(book.id).let { mediaRepository.update(it.copy(status = Media.Status.READY)) }
      }

      every { mockHasher.computeHash(any<Path>()) } returns "sameHash"

      // when
      libraryContentLifecycle.scanRootFolder(library) // rename

      // then
      verify(exactly = 1) { mockHasher.computeHash(any<Path>()) }

      val allSeries = seriesRepository.findAll()
      val allBooks = bookRepository.findAll().sortedBy { it.number }

      assertThat(allSeries).hasSize(1)
      allSeries.first().let { series1 ->
        assertThat(series1.name).isEqualTo("series1")
        assertThat(seriesMetadataRepository.findById(series1.id).summary).isEqualTo("Summary")

        val books = bookRepository.findAllBySeriesId(series1.id)
        assertThat(books).hasSize(1)
        books.first().let { book ->
          assertThat(book.name).isEqualTo("book2")
          assertThat(bookMetadataRepository.findById(book.id).title).isEqualTo("book2")
          assertThat(mediaRepository.findById(book.id).status).isEqualTo(Media.Status.READY)
        }
      }

      assertThat(allBooks).hasSize(1)
      assertThat(allBooks.map { it.deletedDate }).containsOnlyNulls()
    }

    @Test
    fun `given series when renaming all files in its folder but folder modified time is not changed and scanning then series books media is kept`() {
      // given
      val library = makeLibrary()
      libraryRepository.insert(library)

      val series = makeSeries(name = "series1")
      every { mockScanner.scanRootFolder(any()) }
        .returnsMany(
          mapOf(series to listOf(makeBook("book1").copy(fileSize = 1))).toScanResult(),
          mapOf(makeSeries(name = "series1").copy(fileLastModified = series.fileLastModified) to listOf(makeBook("book2").copy(fileSize = 1))).toScanResult(),
        )
      libraryContentLifecycle.scanRootFolder(library) // creation

      seriesRepository.findAll().first().let {
        seriesMetadataRepository.update(seriesMetadataRepository.findById(it.id).copy(summary = "Summary"))
      }

      bookRepository.findAll().forEach { book ->
        bookRepository.update(book.copy(fileHash = "sameHash"))
        mediaRepository.findById(book.id).let { mediaRepository.update(it.copy(status = Media.Status.READY)) }
      }

      every { mockHasher.computeHash(any<Path>()) } returns "sameHash"

      // when
      libraryContentLifecycle.scanRootFolder(library) // rename

      // then
      verify(exactly = 1) { mockHasher.computeHash(any<Path>()) }

      val allSeries = seriesRepository.findAll()
      val allBooks = bookRepository.findAll().sortedBy { it.number }

      assertThat(allSeries).hasSize(1)
      allSeries.first().let { series1 ->
        assertThat(series1.name).isEqualTo("series1")
        assertThat(seriesMetadataRepository.findById(series1.id).summary).isEqualTo("Summary")

        val books = bookRepository.findAllBySeriesId(series1.id)
        assertThat(books).hasSize(1)
        books.first().let { book ->
          assertThat(book.name).isEqualTo("book2")
          assertThat(bookMetadataRepository.findById(book.id).title).isEqualTo("book2")
          assertThat(mediaRepository.findById(book.id).status).isEqualTo(Media.Status.READY)
        }
      }

      assertThat(allBooks).hasSize(1)
      assertThat(allBooks.map { it.deletedDate }).containsOnlyNulls()
    }
  }

  @Nested
  inner class FileMoveToAnotherFolder {
    @Test
    fun `given 2 series when moving 1 file from 1 series to another and scanning then moved book's media is kept`() {
      // given
      val library = makeLibrary()
      libraryRepository.insert(library)

      val book2 = makeBook("book2", url = URL("file:/series1/book2")).copy(fileSize = 324)
      val book2Moved = makeBook("book2", url = URL("file:/series2/book2")).copy(fileSize = 324)

      every { mockScanner.scanRootFolder(any()) }
        .returnsMany(
          mapOf(
            makeSeries(name = "series1") to listOf(makeBook("book1"), book2),
            makeSeries(name = "series2") to listOf(makeBook("book1")),
          ).toScanResult(),
          mapOf(
            makeSeries(name = "series1") to listOf(makeBook("book1")),
            makeSeries(name = "series2") to listOf(makeBook("book1"), book2Moved),
          ).toScanResult(),
        )
      libraryContentLifecycle.scanRootFolder(library) // creation

      bookRepository.findByIdOrNull(book2.id)?.let {
        bookRepository.update(it.copy(fileHash = "sameHash"))
        mediaRepository.update(mediaRepository.findById(it.id).copy(status = Media.Status.READY))
      }

      every { mockHasher.computeHash(any<Path>()) } returns "sameHash"

      // when
      libraryContentLifecycle.scanRootFolder(library) // rename

      // then
      val allSeries = seriesRepository.findAll()
      val allBooks = bookRepository.findAll().sortedBy { it.number }

      assertThat(allSeries).hasSize(2)
      with(allSeries.first { it.name == "series1" }) {
        assertThat(bookRepository.findAllBySeriesId(id)).hasSize(1)
      }

      with(allSeries.first { it.name == "series2" }) {
        val books = bookRepository.findAllBySeriesId(id)
        assertThat(books).hasSize(2)

        books.first { it.name == "book2" }.let {
          assertThat(mediaRepository.findById(it.id).status).isEqualTo(Media.Status.READY)
        }
      }

      assertThat(allBooks).hasSize(3)
      assertThat(allBooks.map { it.deletedDate }).containsOnlyNulls()
    }

    @Test
    fun `given 2 series when moving 1 file from 1 series to another and scanning then moved book read progress is kept`() {
      // given
      val library = makeLibrary()
      libraryRepository.insert(library)

      val book2 = makeBook("book2", url = URL("file:/series1/book2")).copy(fileSize = 324)
      val book2Moved = makeBook("book2", url = URL("file:/series2/book2")).copy(fileSize = 324)

      every { mockScanner.scanRootFolder(any()) }
        .returnsMany(
          mapOf(
            makeSeries(name = "series1") to listOf(makeBook("book1"), book2),
            makeSeries(name = "series2") to listOf(makeBook("book1")),
          ).toScanResult(),
          mapOf(
            makeSeries(name = "series1") to listOf(makeBook("book1")),
            makeSeries(name = "series2") to listOf(makeBook("book1"), book2Moved),
          ).toScanResult(),
        )
      libraryContentLifecycle.scanRootFolder(library) // creation

      bookRepository.findByIdOrNull(book2.id)?.let {
        bookRepository.update(it.copy(fileHash = "sameHash"))
        bookLifecycle.markReadProgressCompleted(it.id, user)
      }

      every { mockHasher.computeHash(any<Path>()) } returns "sameHash"

      // when
      libraryContentLifecycle.scanRootFolder(library) // rename

      // then
      val allSeries = seriesRepository.findAll()
      val allBooks = bookRepository.findAll().sortedBy { it.number }

      assertThat(allSeries).hasSize(2)
      with(allSeries.first { it.name == "series1" }) {
        assertThat(bookRepository.findAllBySeriesId(id)).hasSize(1)
      }

      with(allSeries.first { it.name == "series2" }) {
        val books = bookRepository.findAllBySeriesId(id)
        assertThat(books).hasSize(2)

        books.first { it.name == "book2" }.let {
          assertThat(readProgressRepository.findByBookIdAndUserIdOrNull(it.id, user.id)).isNotNull
        }
      }

      assertThat(allBooks).hasSize(3)
      assertThat(allBooks.map { it.deletedDate }).containsOnlyNulls()

      assertThat(readProgressRepository.findAll()).hasSize(1)
    }

    @Test
    fun `given 2 series when moving 1 file from 1 series to another and scanning then moved book is still in read lists`() {
      // given
      val library = makeLibrary()
      libraryRepository.insert(library)

      val book2 = makeBook("book2", url = URL("file:/series1/book2")).copy(fileSize = 324)
      val book2Moved = makeBook("book2", url = URL("file:/series2/book2")).copy(fileSize = 324)

      every { mockScanner.scanRootFolder(any()) }
        .returnsMany(
          mapOf(
            makeSeries(name = "series1") to listOf(makeBook("book1"), book2),
            makeSeries(name = "series2") to listOf(makeBook("book1")),
          ).toScanResult(),
          mapOf(
            makeSeries(name = "series1") to listOf(makeBook("book1")),
            makeSeries(name = "series2") to listOf(makeBook("book1"), book2Moved),
          ).toScanResult(),
        )
      libraryContentLifecycle.scanRootFolder(library) // creation

      bookRepository.findByIdOrNull(book2.id)?.let {
        bookRepository.update(it.copy(fileHash = "sameHash"))
        readListLifecycle.addReadList(ReadList("read list", bookIds = listOf(it.id).toIndexedMap()))
      }

      every { mockHasher.computeHash(any<Path>()) } returns "sameHash"

      // when
      libraryContentLifecycle.scanRootFolder(library) // rename

      // then
      val allSeries = seriesRepository.findAll()
      val allBooks = bookRepository.findAll().sortedBy { it.number }

      assertThat(allSeries).hasSize(2)
      with(allSeries.first { it.name == "series1" }) {
        assertThat(bookRepository.findAllBySeriesId(id)).hasSize(1)
      }

      with(allSeries.first { it.name == "series2" }) {
        val books = bookRepository.findAllBySeriesId(id)
        assertThat(books).hasSize(2)

        books.first { it.name == "book2" }.let {
          readListRepository.findAllContainingBookId(it.id, null).let { readLists ->
            assertThat(readLists).hasSize(1)
            assertThat(readLists.first().name).isEqualTo("read list")
          }
        }
      }

      assertThat(allBooks).hasSize(3)
      assertThat(allBooks.map { it.deletedDate }).containsOnlyNulls()
    }

    @Test
    fun `given 2 series when moving 1 file with locked title from 1 series to another and scanning then moved book's title is not changed and book metadata is not refreshed for title`() {
      // given
      val library = makeLibrary()
      libraryRepository.insert(library)

      val book2 = makeBook("book2", url = URL("file:/series1/book2")).copy(fileSize = 324)
      val book2Moved = makeBook("book2", url = URL("file:/series2/book2")).copy(fileSize = 324)

      every { mockScanner.scanRootFolder(any()) }
        .returnsMany(
          mapOf(
            makeSeries(name = "series1") to listOf(makeBook("book1"), book2),
            makeSeries(name = "series2") to listOf(makeBook("book1")),
          ).toScanResult(),
          mapOf(
            makeSeries(name = "series1") to listOf(makeBook("book1")),
            makeSeries(name = "series2") to listOf(makeBook("book1"), book2Moved),
          ).toScanResult(),
        )
      libraryContentLifecycle.scanRootFolder(library) // creation

      bookRepository.findByIdOrNull(book2.id)?.let {
        bookRepository.update(it.copy(fileHash = "sameHash"))
        bookMetadataRepository.update(
          bookMetadataRepository.findById(it.id).copy(
            title = "Updated Title",
            titleLock = true,
          ),
        )
      }

      every { mockHasher.computeHash(any<Path>()) } returns "sameHash"

      // when
      libraryContentLifecycle.scanRootFolder(library) // rename

      // then
      verify(exactly = 0) { mockTaskEmitter.refreshBookMetadata(book2Moved, setOf(BookMetadataPatchCapability.TITLE)) }

      val allSeries = seriesRepository.findAll()
      val allBooks = bookRepository.findAll().sortedBy { it.number }

      assertThat(allSeries).hasSize(2)
      with(allSeries.first { it.name == "series1" }) {
        assertThat(bookRepository.findAllBySeriesId(id)).hasSize(1)
      }

      with(allSeries.first { it.name == "series2" }) {
        val books = bookRepository.findAllBySeriesId(id)
        assertThat(books).hasSize(2)

        books.first { it.name == "book2" }.let { book2 ->
          bookMetadataRepository.findById(book2.id).let {
            assertThat(it.title).isEqualTo("Updated Title")
            assertThat(it.titleLock).isTrue
          }
        }
      }

      assertThat(allBooks).hasSize(3)
      assertThat(allBooks.map { it.deletedDate }).containsOnlyNulls()
    }

    @Test
    fun `given 2 series when moving 1 file from 1 series to another and scanning then moved book's title matches the filename and book metadata is refreshed for title only`() {
      // given
      val library = makeLibrary()
      libraryRepository.insert(library)

      val book2 = makeBook("book2", url = URL("file:/series1/book2")).copy(fileSize = 324)
      val book2Moved = makeBook("book2", url = URL("file:/series2/book2")).copy(fileSize = 324)

      every { mockScanner.scanRootFolder(any()) }
        .returnsMany(
          mapOf(
            makeSeries(name = "series1") to listOf(makeBook("book1"), book2),
            makeSeries(name = "series2") to listOf(makeBook("book1")),
          ).toScanResult(),
          mapOf(
            makeSeries(name = "series1") to listOf(makeBook("book1")),
            makeSeries(name = "series2") to listOf(makeBook("book1"), book2Moved),
          ).toScanResult(),
        )
      libraryContentLifecycle.scanRootFolder(library) // creation

      bookRepository.findByIdOrNull(book2.id)?.let {
        bookRepository.update(it.copy(fileHash = "sameHash"))
      }

      every { mockHasher.computeHash(any<Path>()) } returns "sameHash"

      // when
      libraryContentLifecycle.scanRootFolder(library) // rename

      // then
      verify(exactly = 1) { mockTaskEmitter.refreshBookMetadata(withArg<Book> { assertThat(it.id).isEqualTo(book2Moved.id) }, setOf(BookMetadataPatchCapability.TITLE)) }

      val allSeries = seriesRepository.findAll()
      val allBooks = bookRepository.findAll().sortedBy { it.number }

      assertThat(allSeries).hasSize(2)
      with(allSeries.first { it.name == "series1" }) {
        assertThat(bookRepository.findAllBySeriesId(id)).hasSize(1)
      }

      with(allSeries.first { it.name == "series2" }) {
        val books = bookRepository.findAllBySeriesId(id)
        assertThat(books).hasSize(2)

        books.first { it.name == "book2" }.let {
          assertThat(bookMetadataRepository.findById(it.id).title).isEqualTo("book2")
        }
      }

      assertThat(allBooks).hasSize(3)
      assertThat(allBooks.map { it.deletedDate }).containsOnlyNulls()
    }
  }

  @Nested
  inner class RenameFolder {
    @Test
    fun `given series when renaming folder and scanning then renamed series books media is kept`() {
      // given
      val library = makeLibrary()
      libraryRepository.insert(library)

      every { mockScanner.scanRootFolder(any()) }
        .returnsMany(
          mapOf(makeSeries(name = "series1") to listOf(makeBook("book1").copy(fileSize = 1), makeBook("book2").copy(fileSize = 2))).toScanResult(),
          mapOf(makeSeries(name = "series2") to listOf(makeBook("book1").copy(fileSize = 1), makeBook("book2").copy(fileSize = 2))).toScanResult(),
        )
      libraryContentLifecycle.scanRootFolder(library) // creation

      bookRepository.findAll().forEach { book ->
        bookRepository.update(book.copy(fileHash = "HASH-${book.name}"))
        mediaRepository.findById(book.id).let { mediaRepository.update(it.copy(status = Media.Status.READY)) }
      }

      val slot = slot<Path>()
      every { mockHasher.computeHash(capture(slot)) } answers {
        "HASH-${slot.captured.nameWithoutExtension}"
      }

      // when
      libraryContentLifecycle.scanRootFolder(library) // rename

      // then
      verify(exactly = 2) { mockHasher.computeHash(any<Path>()) }

      val allSeries = seriesRepository.findAll()
      val allBooks = bookRepository.findAll().sortedBy { it.number }

      assertThat(allSeries).hasSize(1)
      allSeries.first().let { series2 ->
        assertThat(series2.name).isEqualTo("series2")
        val books = bookRepository.findAllBySeriesId(series2.id)
        assertThat(books).hasSize(2)
        books.forEach { book ->
          assertThat(mediaRepository.findById(book.id).status).isEqualTo(Media.Status.READY)
        }
      }

      assertThat(allBooks).hasSize(2)
      assertThat(allBooks.map { it.deletedDate }).containsOnlyNulls()
    }

    @Test
    fun `given series when renaming folder and scanning then renamed series read progress is kept`() {
      // given
      val library = makeLibrary()
      libraryRepository.insert(library)

      every { mockScanner.scanRootFolder(any()) }
        .returnsMany(
          mapOf(makeSeries(name = "series1") to listOf(makeBook("book1").copy(fileSize = 1), makeBook("book2").copy(fileSize = 2))).toScanResult(),
          mapOf(makeSeries(name = "series2") to listOf(makeBook("book1").copy(fileSize = 1), makeBook("book2").copy(fileSize = 2))).toScanResult(),
        )
      libraryContentLifecycle.scanRootFolder(library) // creation

      seriesRepository.findAll().first().let {
        seriesLifecycle.markReadProgressCompleted(it.id, user)
      }

      bookRepository.findAll().forEach { book ->
        bookRepository.update(book.copy(fileHash = "HASH-${book.name}"))
      }

      val slot = slot<Path>()
      every { mockHasher.computeHash(capture(slot)) } answers {
        "HASH-${slot.captured.nameWithoutExtension}"
      }

      // when
      libraryContentLifecycle.scanRootFolder(library) // rename

      // then
      val allSeries = seriesRepository.findAll()
      val allBooks = bookRepository.findAll().sortedBy { it.number }

      assertThat(allSeries).hasSize(1)
      allSeries.first().let { series2 ->
        assertThat(series2.name).isEqualTo("series2")

        seriesDtoRepository.findByIdOrNull(series2.id, user.id)?.let { seriesDto ->
          assertThat(seriesDto.booksReadCount).isEqualTo(2)
        }
      }

      assertThat(allBooks).hasSize(2)
      assertThat(allBooks.map { it.deletedDate }).containsOnlyNulls()

      assertThat(readProgressRepository.findAll()).hasSize(2)
    }

    @Test
    fun `given series when renaming folder and scanning then renamed series is still in collections`() {
      // given
      val library = makeLibrary()
      libraryRepository.insert(library)

      every { mockScanner.scanRootFolder(any()) }
        .returnsMany(
          mapOf(makeSeries(name = "series1") to listOf(makeBook("book1").copy(fileSize = 1), makeBook("book2").copy(fileSize = 2))).toScanResult(),
          mapOf(makeSeries(name = "series2") to listOf(makeBook("book1").copy(fileSize = 1), makeBook("book2").copy(fileSize = 2))).toScanResult(),
        )
      libraryContentLifecycle.scanRootFolder(library) // creation

      seriesRepository.findAll().first().let {
        collectionLifecycle.addCollection(SeriesCollection("collection", seriesIds = listOf(it.id)))
      }

      bookRepository.findAll().forEach { book ->
        bookRepository.update(book.copy(fileHash = "HASH-${book.name}"))
      }

      val slot = slot<Path>()
      every { mockHasher.computeHash(capture(slot)) } answers {
        "HASH-${slot.captured.nameWithoutExtension}"
      }

      // when
      libraryContentLifecycle.scanRootFolder(library) // rename

      // then
      val allSeries = seriesRepository.findAll()
      val allBooks = bookRepository.findAll().sortedBy { it.number }

      assertThat(allSeries).hasSize(1)
      allSeries.first().let { series2 ->
        assertThat(series2.name).isEqualTo("series2")
        assertThat(bookRepository.findAllBySeriesId(series2.id)).hasSize(2)

        collectionRepository.findAllContainingSeriesId(series2.id, null).let { collections ->
          assertThat(collections).hasSize(1)
          assertThat(collections.first().name).isEqualTo("collection")
        }
      }

      assertThat(allBooks).hasSize(2)
      assertThat(allBooks.map { it.deletedDate }).containsOnlyNulls()
    }

    @Test
    fun `given series when renaming folder with locked title and scanning then renamed series title is not changed`() {
      // given
      val library = makeLibrary()
      libraryRepository.insert(library)

      every { mockScanner.scanRootFolder(any()) }
        .returnsMany(
          mapOf(makeSeries(name = "series1") to listOf(makeBook("book1").copy(fileSize = 1), makeBook("book2").copy(fileSize = 2))).toScanResult(),
          mapOf(makeSeries(name = "series2") to listOf(makeBook("book1").copy(fileSize = 1), makeBook("book2").copy(fileSize = 2))).toScanResult(),
        )
      libraryContentLifecycle.scanRootFolder(library) // creation

      seriesRepository.findAll().first().let {
        seriesMetadataRepository.update(seriesMetadataRepository.findById(it.id).copy(title = "Updated", titleLock = true, titleSort = "SortTitle", titleSortLock = true))
      }

      bookRepository.findAll().forEach { book ->
        bookRepository.update(book.copy(fileHash = "HASH-${book.name}"))
      }

      val slot = slot<Path>()
      every { mockHasher.computeHash(capture(slot)) } answers {
        "HASH-${slot.captured.nameWithoutExtension}"
      }

      // when
      libraryContentLifecycle.scanRootFolder(library) // rename

      // then
      val allSeries = seriesRepository.findAll()
      val allBooks = bookRepository.findAll().sortedBy { it.number }

      assertThat(allSeries).hasSize(1)
      allSeries.first().let { series2 ->
        assertThat(series2.name).isEqualTo("series2")
        seriesMetadataRepository.findById(series2.id).let {
          assertThat(it.title).isEqualTo("Updated")
          assertThat(it.titleLock).isTrue
          assertThat(it.titleSort).isEqualTo("SortTitle")
          assertThat(it.titleSortLock).isTrue
        }
        assertThat(bookRepository.findAllBySeriesId(series2.id)).hasSize(2)
      }

      assertThat(allBooks).hasSize(2)
      assertThat(allBooks.map { it.deletedDate }).containsOnlyNulls()
    }

    @Test
    fun `given series when renaming folder and scanning then renamed series title matches the folder name`() {
      // given
      val library = makeLibrary()
      libraryRepository.insert(library)

      every { mockScanner.scanRootFolder(any()) }
        .returnsMany(
          mapOf(makeSeries(name = "series1") to listOf(makeBook("book1").copy(fileSize = 1), makeBook("book2").copy(fileSize = 2))).toScanResult(),
          mapOf(makeSeries(name = "series2") to listOf(makeBook("book1").copy(fileSize = 1), makeBook("book2").copy(fileSize = 2))).toScanResult(),
        )
      libraryContentLifecycle.scanRootFolder(library) // creation

      bookRepository.findAll().forEach { book ->
        bookRepository.update(book.copy(fileHash = "HASH-${book.name}"))
      }

      val slot = slot<Path>()
      every { mockHasher.computeHash(capture(slot)) } answers {
        "HASH-${slot.captured.nameWithoutExtension}"
      }

      // when
      libraryContentLifecycle.scanRootFolder(library) // rename

      // then
      val allSeries = seriesRepository.findAll()
      val allBooks = bookRepository.findAll().sortedBy { it.number }

      assertThat(allSeries).hasSize(1)
      allSeries.first().let { series2 ->
        assertThat(series2.name).isEqualTo("series2")
        with(seriesMetadataRepository.findById(series2.id)) {
          assertThat(title).isEqualTo("series2")
          assertThat(titleSort).isEqualTo("series2")
        }
        assertThat(bookRepository.findAllBySeriesId(series2.id)).hasSize(2)
      }

      assertThat(allBooks).hasSize(2)
      assertThat(allBooks.map { it.deletedDate }).containsOnlyNulls()
    }
  }

  @Nested
  inner class EmptyTrash {
    @Test
    fun `given library with deleted series and books when emptying the trash then deleted elements are permanently removed`() {
      // given
      val library = makeLibrary()
      libraryRepository.insert(library)

      every { mockScanner.scanRootFolder(any()) }
        .returnsMany(
          mapOf(
            makeSeries(name = "series") to listOf(makeBook("book1"), makeBook("book3")),
            makeSeries(name = "series2") to listOf(makeBook("book2")),
          ).toScanResult(),
          mapOf(makeSeries(name = "series") to listOf(makeBook("book1"))).toScanResult(),
        )
      repeat(2) { libraryContentLifecycle.scanRootFolder(library) }

      // when
      libraryContentLifecycle.emptyTrash(library)

      // then
      val (series, deletedSeries) = seriesRepository.findAll().partition { it.deletedDate == null }
      val (books, deletedBooks) = bookRepository.findAll().partition { it.deletedDate == null }

      assertThat(series).hasSize(1)
      assertThat(series.map { it.name }).containsExactlyInAnyOrder("series")
      assertThat(deletedSeries).isEmpty()

      assertThat(books).hasSize(1)
      assertThat(books.map { it.name }).containsExactlyInAnyOrder("book1")
      assertThat(deletedBooks).isEmpty()
    }

    @Test
    fun `given series with books when emptying the trash then the series is properly sorted`() {
      // given
      val library = makeLibrary()
      libraryRepository.insert(library)

      every { mockScanner.scanRootFolder(any()) }
        .returnsMany(
          mapOf(makeSeries(name = "series") to listOf(makeBook("book1"), makeBook("book2"), makeBook("book3"))).toScanResult(),
          mapOf(makeSeries(name = "series") to listOf(makeBook("book2"), makeBook("book3"))).toScanResult(),
        )
      repeat(2) { libraryContentLifecycle.scanRootFolder(library) }

      // when
      libraryContentLifecycle.emptyTrash(library)

      // then
      val series = seriesRepository.findAll().first()
      val books = bookRepository.findAllBySeriesId(series.id).sortedBy { it.number }

      assertThat(books).hasSize(2)
      with(books.first()) {
        assertThat(name).isEqualTo("book2")
        assertThat(number).isEqualTo(1)
      }
      with(books.last()) {
        assertThat(name).isEqualTo("book3")
        assertThat(number).isEqualTo(2)
      }
    }

    @Test
    fun `given collection and read list with deleted elements when emptying the trash then those sets are deleted`() {
      // given
      val library = makeLibrary()
      libraryRepository.insert(library)

      every { mockScanner.scanRootFolder(any()) }
        .returnsMany(
          mapOf(makeSeries(name = "series") to listOf(makeBook("book1"), makeBook("book2"), makeBook("book3"))).toScanResult(),
          emptyMap<Series, List<Book>>().toScanResult(),
        )
      repeat(2) { libraryContentLifecycle.scanRootFolder(library) }

      collectionRepository.insert(SeriesCollection("collection", seriesIds = seriesRepository.findAllIdsByLibraryId(library.id).toList()))
      readListRepository.insert(ReadList("readlist", bookIds = bookRepository.findAllIdsByLibraryId(library.id).toList().toIndexedMap()))

      // when
      libraryContentLifecycle.emptyTrash(library)

      // then
      val collections = collectionRepository.findAll(pageable = Pageable.unpaged())
      val readLists = readListRepository.findAll(pageable = Pageable.unpaged())

      assertThat(collections.content).isEmpty()
      assertThat(readLists.content).isEmpty()
    }
  }
}
