package org.gotson.komga.domain.service

import com.google.common.jimfs.Configuration
import com.google.common.jimfs.Jimfs
import com.ninjasquad.springmockk.MockkBean
import com.ninjasquad.springmockk.SpykBean
import io.mockk.every
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.gotson.komga.domain.model.BookPage
import org.gotson.komga.domain.model.KomgaUser
import org.gotson.komga.domain.model.Media
import org.gotson.komga.domain.model.ThumbnailBook
import org.gotson.komga.domain.model.makeBook
import org.gotson.komga.domain.model.makeBookPage
import org.gotson.komga.domain.model.makeLibrary
import org.gotson.komga.domain.model.makeSeries
import org.gotson.komga.domain.persistence.BookRepository
import org.gotson.komga.domain.persistence.KomgaUserRepository
import org.gotson.komga.domain.persistence.LibraryRepository
import org.gotson.komga.domain.persistence.MediaRepository
import org.gotson.komga.domain.persistence.ReadProgressRepository
import org.gotson.komga.domain.persistence.SeriesRepository
import org.gotson.komga.domain.persistence.ThumbnailBookRepository
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.junit.jupiter.SpringExtension
import java.nio.file.Files
import java.nio.file.Paths

@ExtendWith(SpringExtension::class)
@SpringBootTest
class BookLifecycleTest(
  @Autowired private val bookRepository: BookRepository,
  @Autowired private val libraryRepository: LibraryRepository,
  @Autowired private val seriesRepository: SeriesRepository,
  @Autowired private val seriesLifecycle: SeriesLifecycle,
  @Autowired private val readProgressRepository: ReadProgressRepository,
  @Autowired private val mediaRepository: MediaRepository,
  @Autowired private val userRepository: KomgaUserRepository,
  @Autowired private val thumbnailBookRepository: ThumbnailBookRepository,
) {

  @SpykBean
  private lateinit var bookLifecycle: BookLifecycle

  @MockkBean
  private lateinit var mockAnalyzer: BookAnalyzer

  private val library = makeLibrary()
  private val user1 = KomgaUser("user1@example.org", "", false)
  private val user2 = KomgaUser("user2@example.org", "", false)

  @BeforeAll
  fun `setup library`() {
    libraryRepository.insert(library)

    userRepository.insert(user1)
    userRepository.insert(user2)
  }

  @AfterAll
  fun teardown() {
    libraryRepository.deleteAll()
    readProgressRepository.deleteAll()
    userRepository.deleteAll()
  }

  @AfterEach
  fun `clear repository`() {
    seriesLifecycle.deleteMany(seriesRepository.findAll())
  }

  @Test
  fun `given outdated book with different number of pages than before when analyzing then existing read progress is deleted`() {
    // given
    makeSeries(name = "series", libraryId = library.id).let { series ->
      seriesLifecycle.createSeries(series).let { created ->
        val books = listOf(makeBook("1", libraryId = library.id))
        seriesLifecycle.addBooks(created, books)
      }
    }

    val book = bookRepository.findAll().first()
    mediaRepository.findById(book.id).let { media ->
      mediaRepository.update(
        media.copy(
          status = Media.Status.OUTDATED,
          pages = (1..10).map { BookPage("$it", "image/jpeg") },
        ),
      )
    }

    bookLifecycle.markReadProgressCompleted(book.id, user1)
    bookLifecycle.markReadProgress(book, user2, 4)

    assertThat(readProgressRepository.findAll()).hasSize(2)

    // when
    every { mockAnalyzer.analyze(any(), any()) } returns Media(status = Media.Status.READY, mediaType = "application/zip", pages = mutableListOf(makeBookPage("1.jpg"), makeBookPage("2.jpg")), bookId = book.id)
    bookLifecycle.analyzeAndPersist(book)

    // then
    assertThat(readProgressRepository.findAll()).isEmpty()
  }

  @Test
  fun `given outdated book with same number of pages than before when analyzing then existing read progress is kept`() {
    // given
    makeSeries(name = "series", libraryId = library.id).let { series ->
      seriesLifecycle.createSeries(series).let { created ->
        val books = listOf(makeBook("1", libraryId = library.id))
        seriesLifecycle.addBooks(created, books)
      }
    }

    val book = bookRepository.findAll().first()
    mediaRepository.findById(book.id).let { media ->
      mediaRepository.update(
        media.copy(
          status = Media.Status.OUTDATED,
          pages = (1..10).map { BookPage("$it", "image/jpeg") },
        ),
      )
    }

    bookLifecycle.markReadProgressCompleted(book.id, user1)
    bookLifecycle.markReadProgress(book, user2, 4)

    assertThat(readProgressRepository.findAll()).hasSize(2)

    // when
    every { mockAnalyzer.analyze(any(), any()) } returns Media(status = Media.Status.READY, mediaType = "application/zip", pages = (1..10).map { BookPage("$it", "image/jpeg") }, bookId = book.id)
    bookLifecycle.analyzeAndPersist(book)

    // then
    assertThat(readProgressRepository.findAll()).hasSize(2)
  }

  @Test
  fun `given a book with a sidecar when deleting book then all book files should be deleted`() {
    Jimfs.newFileSystem(Configuration.unix()).use { fs ->
      // given
      val root = fs.getPath("/root")
      Files.createDirectory(root)
      val seriesPath = root.resolve("series")
      Files.createDirectory(seriesPath)
      val bookPath = seriesPath.resolve("book1.cbz")
      Files.createFile(bookPath)
      val sidecarPath = seriesPath.resolve("sidecar1.png")
      Files.createFile(sidecarPath)

      val series = makeSeries(name = "series", libraryId = library.id, url = seriesPath.toUri().toURL())
      val book = makeBook("1", libraryId = library.id, seriesId = series.id, url = bookPath.toUri().toURL())
      val sidecar = ThumbnailBook(bookId = book.id, type = ThumbnailBook.Type.SIDECAR, url = sidecarPath.toUri().toURL())

      seriesLifecycle.createSeries(series)
      seriesLifecycle.addBooks(series, listOf(book))
      thumbnailBookRepository.insert(sidecar)

      // when
      bookLifecycle.deleteBookFiles(book)

      // then
      assertThat(Files.notExists(bookPath))
      assertThat(Files.notExists(sidecarPath))
    }
  }

  @Test
  fun `given a non-existent book file when deleting book then it returns`() {
    // given
    val bookPath = Paths.get("/non-existent")
    val book = makeBook("1", libraryId = library.id, url = bookPath.toUri().toURL())

    // when
    bookLifecycle.deleteBookFiles(book)

    // then
    verify(exactly = 0) { bookLifecycle.softDeleteMany(any()) }
  }

  @Test
  fun `given a book and a non-existent sidecar file when deleting book then book should be deleted`() {
    Jimfs.newFileSystem(Configuration.unix()).use { fs ->
      // given
      val root = fs.getPath("/root")
      Files.createDirectory(root)
      val seriesPath = root.resolve("series")
      Files.createDirectory(seriesPath)
      val bookPath = seriesPath.resolve("book1.cbz")
      Files.createFile(bookPath)
      val sidecar1Path = seriesPath.resolve("sidecar1.png")
      Files.createFile(sidecar1Path)
      val sidecar2Path = seriesPath.resolve("sidecar2.png")

      val series = makeSeries(name = "series", libraryId = library.id, url = seriesPath.toUri().toURL())
      val book = makeBook("1", libraryId = library.id, seriesId = series.id, url = bookPath.toUri().toURL())
      val sidecar1 = ThumbnailBook(bookId = book.id, type = ThumbnailBook.Type.SIDECAR, url = sidecar1Path.toUri().toURL())
      val sidecar2 = ThumbnailBook(bookId = book.id, type = ThumbnailBook.Type.SIDECAR, url = sidecar2Path.toUri().toURL())

      seriesLifecycle.createSeries(series)
      seriesLifecycle.addBooks(series, listOf(book))
      thumbnailBookRepository.insert(sidecar1)
      thumbnailBookRepository.insert(sidecar2)

      // when
      bookLifecycle.deleteBookFiles(book)

      // then
      assertThat(Files.notExists(seriesPath))
    }
  }

  @Test
  fun `given a single book file when deleting book then parent directory should be deleted`() {
    Jimfs.newFileSystem(Configuration.unix()).use { fs ->
      // given
      val root = fs.getPath("/root")
      Files.createDirectory(root)
      val seriesPath = root.resolve("series")
      Files.createDirectory(seriesPath)
      val bookPath = seriesPath.resolve("book1.cbz")
      Files.createFile(bookPath)

      val series = makeSeries(name = "series", libraryId = library.id, url = seriesPath.toUri().toURL())
      val book = makeBook("1", libraryId = library.id, seriesId = series.id, url = bookPath.toUri().toURL())

      seriesLifecycle.createSeries(series)
      seriesLifecycle.addBooks(series, listOf(book))

      // when
      bookLifecycle.deleteBookFiles(book)

      // then
      assertThat(Files.notExists(seriesPath))
    }
  }

  @Test
  fun `given a single book file with unrelated files in directory when deleting book then parent directory should not be deleted`() {
    Jimfs.newFileSystem(Configuration.unix()).use { fs ->
      // given
      val root = fs.getPath("/root")
      Files.createDirectory(root)
      val seriesPath = root.resolve("series")
      Files.createDirectory(seriesPath)
      val bookPath = seriesPath.resolve("book1.cbz")
      Files.createFile(bookPath)
      val filePath = seriesPath.resolve("file.txt")
      Files.createFile(filePath)

      val series = makeSeries(name = "series", libraryId = library.id, url = seriesPath.toUri().toURL())
      val book = makeBook("1", libraryId = library.id, seriesId = series.id, url = bookPath.toUri().toURL())

      seriesLifecycle.createSeries(series)
      seriesLifecycle.addBooks(series, listOf(book))

      // when
      bookLifecycle.deleteBookFiles(book)

      // then
      assertThat(Files.exists(seriesPath))
      assertThat(Files.exists(filePath))
      assertThat(Files.notExists(bookPath))
    }
  }
}
