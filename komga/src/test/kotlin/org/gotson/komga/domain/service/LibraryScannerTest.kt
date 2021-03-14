package org.gotson.komga.domain.service

import com.google.common.jimfs.Configuration
import com.google.common.jimfs.Jimfs
import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.gotson.komga.domain.model.BookMetadata
import org.gotson.komga.domain.model.KomgaUser
import org.gotson.komga.domain.model.Media
import org.gotson.komga.domain.model.ReadProgress
import org.gotson.komga.domain.model.makeBook
import org.gotson.komga.domain.model.makeBookPage
import org.gotson.komga.domain.model.makeLibrary
import org.gotson.komga.domain.model.makeSeries
import org.gotson.komga.domain.persistence.BookMetadataRepository
import org.gotson.komga.domain.persistence.BookRepository
import org.gotson.komga.domain.persistence.KomgaUserRepository
import org.gotson.komga.domain.persistence.LibraryRepository
import org.gotson.komga.domain.persistence.MediaRepository
import org.gotson.komga.domain.persistence.ReadProgressRepository
import org.gotson.komga.domain.persistence.SeriesMetadataRepository
import org.gotson.komga.domain.persistence.SeriesRepository
import org.gotson.komga.infrastructure.hash.XXHasher
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.junit.jupiter.SpringExtension
import java.nio.file.Files
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
  @Autowired private val libraryLifecycle: LibraryLifecycle,
  @Autowired private val readProgressRepository: ReadProgressRepository,
  @Autowired private val bookMetadataRepository: BookMetadataRepository,
  @Autowired private val seriesMetadataRepository: SeriesMetadataRepository,
  @Autowired private val userRepository: KomgaUserRepository,
  @Autowired private val userLifecycle: KomgaUserLifecycle
) {

  @MockkBean
  private lateinit var mockScanner: FileSystemScanner

  @MockkBean
  private lateinit var mockHasher: XXHasher

  @MockkBean
  private lateinit var mockAnalyzer: BookAnalyzer

  @AfterEach
  fun `clear repositories`() {
    userRepository.findAll().forEach {
      userLifecycle.deleteUser(it)
    }

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

    every { mockHasher.getHash(any()) }.returns("")
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
    every { mockHasher.getHash(any()) }.returns("")
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
    every { mockHasher.getHash(any()) }.returns("")
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
    every { mockHasher.getHash(any()) }.returns("")
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
    every { mockHasher.getHash(any()) }.returns("")
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
        mapOf(
          makeSeries(name = "series") to listOf(
            makeBook(
              name = "book1",
              fileLastModified = book1.fileLastModified
            )
          )
        )
      )
    every { mockHasher.getHash(any()) }.returns("")
    libraryScanner.scanRootFolder(library)

    every { mockAnalyzer.analyze(any()) } returns Media(
      status = Media.Status.READY,
      mediaType = "application/zip",
      pages = mutableListOf(makeBookPage("1.jpg"), makeBookPage("2.jpg"))
    )
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
    every { mockHasher.getHash(any()) }.returns("")
    libraryScanner.scanRootFolder(library)

    every { mockAnalyzer.analyze(any()) } returns Media(
      status = Media.Status.READY,
      mediaType = "application/zip",
      pages = mutableListOf(makeBookPage("1.jpg"), makeBookPage("2.jpg"))
    )
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
    every { mockHasher.getHash(any()) }.returnsMany("1", "2")

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

  @Test
  fun `given existing series when removing and restoring a book then deleted book restored with same metadata and read progress`() {
    // given
    val user = KomgaUser("user@example.org", "", false)
    userRepository.insert(user)

    val library = makeLibrary()
    libraryRepository.insert(library)

    val book = makeBook("book1")
    every { mockScanner.scanRootFolder(any()) }
      .returnsMany(
        mapOf(makeSeries(name = "series") to listOf(book)),
        mapOf(makeSeries(name = "series") to listOf()),
        mapOf(makeSeries(name = "series") to listOf(book)),
      )
    every { mockHasher.getHash(book.path()) }.returns("1")
    libraryScanner.scanRootFolder(library)

    val readProgress = ReadProgress(book.id, user.id, 1, false)
    readProgressRepository.save(readProgress)
    val metadata: BookMetadata = bookMetadataRepository.findById(book.id).copy(title = "custom title", titleLock = true)
    bookMetadataRepository.update(metadata)

    // when
    libraryScanner.scanRootFolder(library)
    assertThat(bookRepository.findAll()).hasSize(0)
    libraryScanner.scanRootFolder(library)

    // then
    val allSeries = seriesRepository.findAll()
    val allBooks = bookRepository.findAll()
    val restoredMetadata = bookMetadataRepository.findById(book.id)
    val restoredReadProgress = readProgressRepository.findByBookIdAndUserId(book.id, user.id)

    verify(exactly = 3) { mockScanner.scanRootFolder(any()) }

    assertThat(allSeries).hasSize(1)
    assertThat(allBooks).hasSize(1)
    assertThat(allBooks.map { it.name }).containsExactly(book.name)

    assertThat(restoredMetadata.bookId).isEqualTo(metadata.bookId)
    assertThat(restoredMetadata.title).isEqualTo(metadata.title)
    assertThat(restoredReadProgress).isNotNull
    assertThat(restoredReadProgress!!.page).isEqualTo(readProgress.page)
  }

  @Test
  fun `given existing series when renaming book then book metadata is preserved`() {
    // given
    val user = KomgaUser("user@example.org", "", false)
    userRepository.insert(user)

    val library = makeLibrary()
    libraryRepository.insert(library)

    val book = makeBook(name = "book1")
    every { mockScanner.scanRootFolder(any()) }
      .returnsMany(
        mapOf(makeSeries(name = "series") to listOf(book)),
        mapOf(makeSeries(name = "series") to listOf(makeBook(name = "book2"))),
      )
    every { mockHasher.getHash(any()) }.returns("")
    libraryScanner.scanRootFolder(library)

    val readProgress = ReadProgress(book.id, user.id, 1, false)
    readProgressRepository.save(readProgress)
    val metadata: BookMetadata = bookMetadataRepository.findById(book.id).copy(title = "custom title", titleLock = true)
    bookMetadataRepository.update(metadata)

    // when
    libraryScanner.scanRootFolder(library)

    // then
    val allSeries = seriesRepository.findAll()
    val allBooks = bookRepository.findAll().sortedBy { it.number }
    val restoredMetadata = bookMetadataRepository.findById(book.id)
    val restoredReadProgress = readProgressRepository.findByBookIdAndUserId(book.id, user.id)

    verify(exactly = 2) { mockScanner.scanRootFolder(any()) }

    assertThat(allSeries).hasSize(1)
    assertThat(allBooks).hasSize(1)
    assertThat(allBooks.map { it.name }).containsExactly("book2")

    assertThat(restoredMetadata.bookId).isEqualTo(metadata.bookId)
    assertThat(restoredMetadata.title).isEqualTo(metadata.title)
    assertThat(restoredReadProgress).isNotNull
    assertThat(restoredReadProgress!!.page).isEqualTo(readProgress.page)
  }

  @Test
  fun `given existing series when removing and restoring a series then series and it's books metadata is preserved`() {
    // given
    val user = KomgaUser("user@example.org", "", false)
    userRepository.insert(user)

    val library = makeLibrary()
    libraryRepository.insert(library)

    val book = makeBook(name = "book1")
    val series = makeSeries(name = "series")
    every { mockScanner.scanRootFolder(any()) }
      .returnsMany(
        mapOf(series to listOf(book)),
        mapOf(),
        mapOf(makeSeries(name = series.name) to listOf(makeBook(name = book.name))),
      )
    every { mockHasher.getHash(any()) }.returns("")
    libraryScanner.scanRootFolder(library)

    val readProgress = ReadProgress(book.id, user.id, 1, false)
    readProgressRepository.save(readProgress)
    val metadata: BookMetadata = bookMetadataRepository.findById(book.id).copy(title = "custom title", titleLock = true)
    bookMetadataRepository.update(metadata)

    // when
    libraryScanner.scanRootFolder(library)
    libraryScanner.scanRootFolder(library)

    // then
    val allSeries = seriesRepository.findAll()
    val allBooks = bookRepository.findAll().sortedBy { it.number }
    val restoredMetadata = bookMetadataRepository.findById(book.id)
    val restoredReadProgress = readProgressRepository.findByBookIdAndUserId(book.id, user.id)

    verify(exactly = 3) { mockScanner.scanRootFolder(any()) }

    assertThat(allSeries).hasSize(1)
    assertThat(allBooks).hasSize(1)
    assertThat(allBooks.map { it.name }).containsExactly(book.name)

    assertThat(restoredMetadata.bookId).isEqualTo(metadata.bookId)
    assertThat(restoredMetadata.title).isEqualTo(metadata.title)
    assertThat(restoredReadProgress).isNotNull
    assertThat(restoredReadProgress!!.page).isEqualTo(readProgress.page)
  }

  @Test
  fun `given existing series with two books that have same hash when simultaneously renaming these books then books added as new ones with old metadata being deleted`() {
    Jimfs.newFileSystem(Configuration.unix()).use { fs ->
      // given
      val root = fs.getPath("/")
      val seriesPath = Files.createDirectory(root.resolve("series1"))
      val book1Path = Files.createFile(seriesPath.resolve("book1"))
      val book2Path = Files.createFile(seriesPath.resolve("book2"))
      val book1 = makeBook(name = "book1").copy(url = book1Path.toUri().toURL())
      val book2 = makeBook(name = "book2").copy(url = book2Path.toUri().toURL())
      val book1Renamed = makeBook(name = "book1Renamed").copy(url = seriesPath.resolve("book1Renamed").toUri().toURL())
      val book2Renamed = makeBook(name = "book2Renamed").copy(url = seriesPath.resolve("book2Renamed").toUri().toURL())

      val user = KomgaUser("user@example.org", "", false)
      userRepository.insert(user)

      val library = makeLibrary()
      libraryRepository.insert(library)

      every { mockScanner.scanRootFolder(any()) }
        .returnsMany(
          mapOf(makeSeries(name = "series") to listOf(book1, book2)),
          mapOf(makeSeries(name = "series") to listOf(book1Renamed, book2Renamed)),
        )
      every { mockHasher.getHash(any()) }.returns("")
      libraryScanner.scanRootFolder(library)

      val readProgress1 = ReadProgress(book1.id, user.id, 1, false)
      val readProgress2 = ReadProgress(book2.id, user.id, 2, false)
      readProgressRepository.save(readProgress1)
      readProgressRepository.save(readProgress2)

      // when
      libraryScanner.scanRootFolder(library)

      // then
      val allSeries = seriesRepository.findAll()
      val allBooks = bookRepository.findAll()
      val book1RenamedMetadata = bookMetadataRepository.findById(book1Renamed.id)
      val book2RenamedMetadata = bookMetadataRepository.findById(book2Renamed.id)
      val book1RenamedReadProgress = readProgressRepository.findByBookIdAndUserId(book1Renamed.id, user.id)
      val book2RenamedReadProgress = readProgressRepository.findByBookIdAndUserId(book2Renamed.id, user.id)

      assertThat(allSeries).hasSize(1)
      assertThat(allBooks).hasSize(2)

      assertThat(book1RenamedMetadata.bookId).isEqualTo(book1Renamed.id)
      assertThat(book2RenamedMetadata.bookId).isEqualTo(book2Renamed.id)
      assertThat(book1RenamedReadProgress).isNull()
      assertThat(book2RenamedReadProgress).isNull()
    }
  }

  @Test
  fun `given 2 series with the books that have same hash when simultaneously renaming these books then books stay withing the same series and metadata is preserved`() {
    Jimfs.newFileSystem(Configuration.unix()).use { fs ->
      // given
      val root = fs.getPath("/")
      val series1Path = Files.createDirectory(root.resolve("series1"))
      val series2Path = Files.createDirectory(root.resolve("series2"))

      val user = KomgaUser("user@example.org", "", false)
      userRepository.insert(user)

      val library = makeLibrary()
      libraryRepository.insert(library)

      val series1 = makeSeries(name = "series1").copy(url = series1Path.toUri().toURL())
      val series2 = makeSeries(name = "series2").copy(url = series2Path.toUri().toURL())
      val book1Path = Files.createFile(series1Path.resolve("book1"))
      val book2Path = Files.createFile(series2Path.resolve("book2"))
      val book1 = makeBook(name = "book1").copy(url = book1Path.toUri().toURL())
      val book2 = makeBook(name = "book2").copy(url = book2Path.toUri().toURL())

      val book1RenamedPath = series1Path.resolve("book1Renamed")
      val book2RenamedPath = series2Path.resolve("book2Renamed")
      every { mockScanner.scanRootFolder(any()) }
        .returnsMany(
          mapOf(
            series1 to listOf(book1),
            series2 to listOf(book2)
          ),
          mapOf(
            series1.copy(fileLastModified = series1.fileLastModified.plusSeconds(1)) to listOf(
              makeBook(name = "book1Renamed").copy(
                url = book1RenamedPath.toUri().toURL()
              )
            ),
            series2.copy(fileLastModified = series1.fileLastModified.plusSeconds(1)) to listOf(
              makeBook(name = "book2Renamed").copy(
                url = book2RenamedPath.toUri().toURL()
              )
            )
          )
        )
      every { mockHasher.getHash(any()) }.returns("")

      libraryScanner.scanRootFolder(library)

      val readProgress1 = ReadProgress(book1.id, user.id, 1, false)
      val readProgress2 = ReadProgress(book2.id, user.id, 2, false)
      readProgressRepository.save(readProgress1)
      readProgressRepository.save(readProgress2)
      val bookMetadata1 = bookMetadataRepository.findById(book1.id).copy(title = "book1 title", titleLock = true)
      val bookMetadata2 = bookMetadataRepository.findById(book2.id).copy(title = "book2 title", titleLock = true)
      bookMetadataRepository.update(bookMetadata1)
      bookMetadataRepository.update(bookMetadata2)

      // when
      Files.move(book1Path, book1RenamedPath)
      Files.move(book2Path, book2RenamedPath)
      libraryScanner.scanRootFolder(library)

      // then
      val series1Books = bookRepository.findBySeriesId(series1.id)
      val series2Books = bookRepository.findBySeriesId(series2.id)
      val restoredBook1Metadata = bookMetadataRepository.findById(book1.id)
      val restoredBook2Metadata = bookMetadataRepository.findById(book2.id)
      val restoredReadProgress1 = readProgressRepository.findByBookIdAndUserId(book1.id, user.id)
      val restoredReadProgress2 = readProgressRepository.findByBookIdAndUserId(book2.id, user.id)

      assertThat(series1Books).hasSize(1)
      assertThat(series2Books).hasSize(1)
      assertThat(series1Books.map { it.id }).contains(book1.id)
      assertThat(series2Books.map { it.id }).contains(book2.id)

      assertThat(restoredBook1Metadata.bookId).isEqualTo(bookMetadata1.bookId)
      assertThat(restoredBook1Metadata.title).isEqualTo(bookMetadata1.title)
      assertThat(restoredReadProgress1).isNotNull
      assertThat(restoredReadProgress1!!.page).isEqualTo(readProgress1.page)

      assertThat(restoredBook2Metadata.bookId).isEqualTo(bookMetadata2.bookId)
      assertThat(restoredBook2Metadata.title).isEqualTo(bookMetadata2.title)
      assertThat(restoredReadProgress2).isNotNull
      assertThat(restoredReadProgress2!!.page).isEqualTo(readProgress2.page)
    }
  }

  @Test
  fun `given 2 series withing a library with the books that have same hash when simultaneously renaming series then series added as new ones with new metadata`() {
    Jimfs.newFileSystem(Configuration.unix()).use { fs ->
      // given
      val root = fs.getPath("/")
      val series1Path = Files.createDirectory(root.resolve("series1"))
      val series2Path = Files.createDirectory(root.resolve("series2"))

      val user = KomgaUser("user@example.org", "", false)
      userRepository.insert(user)

      val library = makeLibrary()
      libraryRepository.insert(library)

      val series1 = makeSeries(name = "series1").copy(url = series1Path.toUri().toURL())
      val series2 = makeSeries(name = "series2").copy(url = series2Path.toUri().toURL())

      val series1RenamedPath = root.resolve("series1Renamed")
      val series2RenamedPath = root.resolve("series2Renamed")
      val series1Renamed = makeSeries(name = "series1Renamed").copy(url = series1RenamedPath.toUri().toURL())
      val series2Renamed = makeSeries(name = "series2Renamed").copy(url = series2RenamedPath.toUri().toURL())

      val book1Path = Files.createFile(series1Path.resolve("book1"))
      val book2Path = Files.createFile(series2Path.resolve("book2"))
      val book1 = makeBook(name = "book1").copy(url = book1Path.toUri().toURL())
      val book2 = makeBook(name = "book2").copy(url = book2Path.toUri().toURL())

      val book1Renamed = makeBook(name = "book1Renamed").copy(url = series1RenamedPath.resolve("book1").toUri().toURL())
      val book2Renamed = makeBook(name = "book2Renamed").copy(url = series2RenamedPath.resolve("book2").toUri().toURL())

      every { mockScanner.scanRootFolder(any()) }
        .returnsMany(
          mapOf(series1 to listOf(book1), series2 to listOf(book2)),
          mapOf(series1Renamed to listOf(book1Renamed), series2Renamed to listOf(book2Renamed))
        )
      every { mockHasher.getHash(any()) }.returns("")

      libraryScanner.scanRootFolder(library)

      val readProgress1 = ReadProgress(book1.id, user.id, 1, false)
      val readProgress2 = ReadProgress(book2.id, user.id, 2, false)
      readProgressRepository.save(readProgress1)
      readProgressRepository.save(readProgress2)

      val bookMetadata1 = bookMetadataRepository.findById(book1.id).copy(title = "book1 title", titleLock = true)
      val bookMetadata2 = bookMetadataRepository.findById(book2.id).copy(title = "book2 title", titleLock = true)
      bookMetadataRepository.update(bookMetadata1)
      bookMetadataRepository.update(bookMetadata2)

      // when
      Files.move(series1Path, series1RenamedPath)
      Files.move(series2Path, series2RenamedPath)
      libraryScanner.scanRootFolder(library)

      // then
      val series1Books = bookRepository.findBySeriesId(series1Renamed.id)
      val series2Books = bookRepository.findBySeriesId(series2Renamed.id)
      val newBook1Metadata = bookMetadataRepository.findById(book1Renamed.id)
      val newBook2Metadata = bookMetadataRepository.findById(book2Renamed.id)
      val newReadProgress1 = readProgressRepository.findByBookIdAndUserId(book1Renamed.id, user.id)
      val newReadProgress2 = readProgressRepository.findByBookIdAndUserId(book2Renamed.id, user.id)

      assertThat(series1Books).hasSize(1)
      assertThat(series2Books).hasSize(1)
      assertThat(series1Books.map { it.id }).contains(book1Renamed.id)
      assertThat(series2Books.map { it.id }).contains(book2Renamed.id)

      assertThat(newBook1Metadata.bookId).isNotEqualTo(bookMetadata1.bookId)
      assertThat(newBook2Metadata.bookId).isNotEqualTo(bookMetadata2.bookId)
      assertThat(newReadProgress1).isNull()
      assertThat(newReadProgress2).isNull()
    }
  }

  @Test
  fun `given 2 libraries when moving series from one library to another then series and it's books metadata is preserved`() {
    // given
    val user = KomgaUser("user@example.org", "", false)
    userRepository.insert(user)

    val library1 = makeLibrary(name = "library1")
    libraryRepository.insert(library1)
    val library2 = makeLibrary(name = "library2")
    libraryRepository.insert(library2)

    val series = makeSeries(name = "series1")
    val book = makeBook(name = "book1")

    every { mockScanner.scanRootFolder(Paths.get(library1.root.toURI())) } returns
      mapOf(series to listOf(book))
    every { mockScanner.scanRootFolder(Paths.get(library2.root.toURI())) }.returnsMany(
      emptyMap(),
      mapOf(series to listOf(book))
    )
    every { mockHasher.getHash(any()) }.returns("")
    libraryScanner.scanRootFolder(library1)
    libraryScanner.scanRootFolder(library2)

    val readProgress = ReadProgress(book.id, user.id, 1, false)
    readProgressRepository.save(readProgress)
    val seriesMetadata = seriesMetadataRepository.findById(series.id).copy(title = "series metadata title", titleLock = true)
    val bookMetadata: BookMetadata = bookMetadataRepository.findById(book.id).copy(title = "book metadata title", titleLock = true)
    seriesMetadataRepository.update(seriesMetadata)
    bookMetadataRepository.update(bookMetadata)

    // when
    libraryScanner.scanRootFolder(library2)

    // then
    val allSeries = seriesRepository.findAll()
    val allBooks = bookRepository.findAll().sortedBy { it.number }
    val restoredSeriesMetadata = seriesMetadataRepository.findById(series.id)
    val restoredBookMetadata = bookMetadataRepository.findById(book.id)
    val restoredReadProgress = readProgressRepository.findByBookIdAndUserId(book.id, user.id)

    assertThat(allSeries).hasSize(1)
    assertThat(allBooks).hasSize(1)
    assertThat(allBooks.map { it.name }).containsExactly(book.name)

    assertThat(restoredSeriesMetadata.seriesId).isEqualTo(seriesMetadata.seriesId)
    assertThat(restoredSeriesMetadata.title).isEqualTo(seriesMetadata.title)
    assertThat(restoredBookMetadata.bookId).isEqualTo(bookMetadata.bookId)
    assertThat(restoredBookMetadata.title).isEqualTo(bookMetadata.title)
    assertThat(restoredReadProgress).isNotNull
    assertThat(restoredReadProgress!!.page).isEqualTo(readProgress.page)
  }

  @Test
  fun `given 2 libraries with a series that have books with the same hash when simultaneously renaming series then series and it's books metadata is preserved`() {
    // given
    val user = KomgaUser("user@example.org", "", false)
    userRepository.insert(user)

    val library1 = makeLibrary(name = "library1")
    libraryRepository.insert(library1)
    val library2 = makeLibrary(name = "library2")
    libraryRepository.insert(library2)

    val series = makeSeries(name = "series1")
    val book = makeBook(name = "book1")

    every { mockScanner.scanRootFolder(Paths.get(library1.root.toURI())) } returns
      mapOf(series to listOf(book))
    every { mockScanner.scanRootFolder(Paths.get(library2.root.toURI())) }.returnsMany(
      emptyMap(),
      mapOf(series to listOf(book))
    )
    every { mockHasher.getHash(any()) }.returns("")
    libraryScanner.scanRootFolder(library1)
    libraryScanner.scanRootFolder(library2)

    val readProgress = ReadProgress(book.id, user.id, 1, false)
    readProgressRepository.save(readProgress)
    val seriesMetadata = seriesMetadataRepository.findById(series.id).copy(title = "series metadata title", titleLock = true)
    val bookMetadata = bookMetadataRepository.findById(book.id).copy(title = "book metadata title", titleLock = true)
    seriesMetadataRepository.update(seriesMetadata)
    bookMetadataRepository.update(bookMetadata)

    // when
    libraryScanner.scanRootFolder(library2)

    // then
    val allSeries = seriesRepository.findAll()
    val allBooks = bookRepository.findAll().sortedBy { it.number }
    val restoredSeriesMetadata = seriesMetadataRepository.findById(series.id)
    val restoredBookMetadata = bookMetadataRepository.findById(book.id)
    val restoredReadProgress = readProgressRepository.findByBookIdAndUserId(book.id, user.id)

    assertThat(allSeries).hasSize(1)
    assertThat(allBooks).hasSize(1)
    assertThat(allBooks.map { it.name }).containsExactly(book.name)

    assertThat(restoredSeriesMetadata.seriesId).isEqualTo(seriesMetadata.seriesId)
    assertThat(restoredSeriesMetadata.title).isEqualTo(seriesMetadata.title)
    assertThat(restoredBookMetadata.bookId).isEqualTo(bookMetadata.bookId)
    assertThat(restoredBookMetadata.title).isEqualTo(bookMetadata.title)
    assertThat(restoredReadProgress).isNotNull
    assertThat(restoredReadProgress!!.page).isEqualTo(readProgress.page)
  }

  @Test
  fun `given 2 libraries when moving book from one series in one library to another series in another library then book metadata is preserved`() {
    // given
    val user = KomgaUser("user@example.org", "", false)
    userRepository.insert(user)

    val library1 = makeLibrary(name = "library1")
    libraryRepository.insert(library1)
    val library2 = makeLibrary(name = "library2")
    libraryRepository.insert(library2)

    val series1 = makeSeries(name = "series1")
    val series2 = makeSeries(name = "series2")
    val bookToMove = makeBook(name = "book1")
    val book2 = makeBook(name = "book2")
    val book3 = makeBook(name = "book3")

    every { mockScanner.scanRootFolder(Paths.get(library1.root.toURI())) } returns
      mapOf(series1 to listOf(bookToMove, book2))
    every { mockScanner.scanRootFolder(Paths.get(library2.root.toURI())) }.returnsMany(
      mapOf(series2 to listOf(book3)),
      mapOf(series2 to listOf(bookToMove, book3))
    )
    every { mockHasher.getHash(book2.path()) }.returns("1")
    every { mockHasher.getHash(bookToMove.path()) }.returns("2")
    every { mockHasher.getHash(book3.path()) }.returns("3")

    libraryScanner.scanRootFolder(library1)
    libraryScanner.scanRootFolder(library2)

    val readProgress = ReadProgress(bookToMove.id, user.id, 1, false)
    readProgressRepository.save(readProgress)
    val metadata = bookMetadataRepository.findById(bookToMove.id).copy(title = "book metadata title", titleLock = true)
    bookMetadataRepository.update(metadata)

    // when
    libraryScanner.scanRootFolder(library2)

    // then
    val series2Books = bookRepository.findBySeriesId(series2.id)
    val restoredMetadata = bookMetadataRepository.findById(bookToMove.id)
    val restoredReadProgress = readProgressRepository.findByBookIdAndUserId(bookToMove.id, user.id)

    assertThat(seriesRepository.findByLibraryId(library1.id)).hasSize(1)
    assertThat(seriesRepository.findByLibraryId(library2.id)).hasSize(1)
    assertThat(bookRepository.findBySeriesId(series1.id)).hasSize(1)
    assertThat(series2Books.map { it.name }).containsExactly(bookToMove.name, book3.name)

    assertThat(restoredMetadata.bookId).isEqualTo(metadata.bookId)
    assertThat(restoredMetadata.title).isEqualTo(metadata.title)
    assertThat(restoredReadProgress).isNotNull
    assertThat(restoredReadProgress!!.page).isEqualTo(readProgress.page)
  }

  @Test
  fun `given a soft-deleted and existing series when changing path of existing series to soft-deleted then soft-deleted is removed and existing series path is changed`() {
    // given
    val user = KomgaUser("user@example.org", "", false)
    userRepository.insert(user)

    val library = makeLibrary()
    libraryRepository.insert(library)

    val series1 = makeSeries(name = "series1")
    val book1 = makeBook(name = "book1")

    val series2 = makeSeries(name = "series2")
    val book2 = makeBook(name = "book2")

    val series2MovedToSeries1Path = series2.copy(name = series1.name, url = series1.url)
    val book2MovedToBook1Path = book2.copy(url = book1.url)

    every { mockScanner.scanRootFolder(any()) }
      .returnsMany(
        mapOf(series1 to listOf(book1)),
        mapOf(series2 to listOf(book2)),
        mapOf(series2MovedToSeries1Path to listOf(book2MovedToBook1Path))
      )
    every { mockHasher.getHash(book1.path()) }.returnsMany("1", "2")
    every { mockHasher.getHash(book2.path()) }.returns("2")
    libraryScanner.scanRootFolder(library)
    libraryScanner.scanRootFolder(library)

    // when
    libraryScanner.scanRootFolder(library)

    // then
    val allSeries = seriesRepository.findAll()
    val allSeriesDeleted = seriesRepository.findAllDeleted()
    val allBooks = bookRepository.findAll()
    val allBooksDeleted = bookRepository.findAllDeleted()

    verify(exactly = 3) { mockScanner.scanRootFolder(any()) }

    assertThat(allSeries).hasSize(1)
    assertThat(allSeriesDeleted).isEmpty()
    assertThat(allBooks).hasSize(1)
    assertThat(allBooksDeleted).isEmpty()

    assertThat(allSeries.first().id).isEqualTo(series2.id)
    assertThat(allBooks.first().id).isEqualTo(book2.id)
  }

  @Test
  fun `given soft-deleted series when adding new series with the same path but different books then soft-deleted series is removed and new series is created`() {
    // given
    val user = KomgaUser("user@example.org", "", false)
    userRepository.insert(user)

    val library = makeLibrary()
    libraryRepository.insert(library)

    val series1 = makeSeries(name = "series1")
    val book1 = makeBook(name = "book1")

    val series2 = makeSeries(name = "series2").copy(url = series1.url)
    val book2 = makeBook(name = "book2").copy(url = book1.url)

    every { mockScanner.scanRootFolder(any()) }
      .returnsMany(
        mapOf(series1 to listOf(book1)),
        mapOf(),
        mapOf(series2 to listOf(book2)),
      )
    every { mockHasher.getHash(book1.path()) }.returnsMany("1", "2")
    libraryScanner.scanRootFolder(library)
    // delete series
    libraryScanner.scanRootFolder(library)

    // when
    libraryScanner.scanRootFolder(library)

    // then
    val allSeries = seriesRepository.findAll()
    val allSeriesDeleted = seriesRepository.findAllDeleted()
    val allBooks = bookRepository.findAll()
    val allBooksDeleted = bookRepository.findAllDeleted()

    verify(exactly = 3) { mockScanner.scanRootFolder(any()) }

    assertThat(allSeries).hasSize(1)
    assertThat(allSeriesDeleted).isEmpty()
    assertThat(allBooks).hasSize(1)
    assertThat(allBooksDeleted).isEmpty()

    assertThat(allSeries.first().id).isEqualTo(series2.id)
    assertThat(allBooks.first().id).isEqualTo(book2.id)
  }
}
