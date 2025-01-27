package org.gotson.komga.domain.service

import com.google.common.jimfs.Configuration
import com.google.common.jimfs.Jimfs
import com.ninjasquad.springmockk.MockkBean
import com.ninjasquad.springmockk.SpykBean
import io.mockk.every
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatNoException
import org.assertj.core.api.Assertions.catchThrowable
import org.gotson.komga.domain.model.BookPage
import org.gotson.komga.domain.model.Dimension
import org.gotson.komga.domain.model.KomgaUser
import org.gotson.komga.domain.model.Media
import org.gotson.komga.domain.model.MediaExtensionEpub
import org.gotson.komga.domain.model.MediaFile
import org.gotson.komga.domain.model.R2Device
import org.gotson.komga.domain.model.R2Locator
import org.gotson.komga.domain.model.R2Progression
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
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import java.nio.file.Files
import java.nio.file.Paths
import java.time.ZonedDateTime

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
  private val user1 = KomgaUser("user1@example.org", "")
  private val user2 = KomgaUser("user2@example.org", "")

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
  fun `given outdated book with different number of pages than before when analyzing then existing incomplete read progress is reset to 1`() {
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
          pageCount = 10,
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
    with(readProgressRepository.findByBookIdAndUserIdOrNull(book.id, user1.id)!!) {
      assertThat(page).isEqualTo(2)
      assertThat(completed).isTrue
    }
    with(readProgressRepository.findByBookIdAndUserIdOrNull(book.id, user2.id)!!) {
      assertThat(page).isEqualTo(1)
      assertThat(completed).isFalse
    }
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
          pageCount = 10,
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
      val sidecar = ThumbnailBook(bookId = book.id, type = ThumbnailBook.Type.SIDECAR, url = sidecarPath.toUri().toURL(), fileSize = 0, mediaType = "", dimension = Dimension(0, 0))

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
      val sidecar1 = ThumbnailBook(bookId = book.id, type = ThumbnailBook.Type.SIDECAR, url = sidecar1Path.toUri().toURL(), fileSize = 0, mediaType = "", dimension = Dimension(0, 0))
      val sidecar2 = ThumbnailBook(bookId = book.id, type = ThumbnailBook.Type.SIDECAR, url = sidecar2Path.toUri().toURL(), fileSize = 0, mediaType = "", dimension = Dimension(0, 0))

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

  @Nested
  inner class Progression {
    @BeforeEach
    fun setup() {
      makeSeries(name = "series", libraryId = library.id).let { series ->
        seriesLifecycle.createSeries(series).let { created ->
          val books = listOf(makeBook("1", libraryId = library.id))
          seriesLifecycle.addBooks(created, books)
        }
      }
    }

    private val device = R2Device("abc", "device")
    private val epubResources =
      listOf(
        MediaFile("ch1.xhtml", "application/xhtml+xml"),
        MediaFile("ch2.xhtml", "application/xhtml+xml"),
        MediaFile("ch3.xhtml", "application/xhtml+xml"),
      )

    private fun makeEpubPositions(): List<R2Locator> {
      var startPosition = 1
      return epubResources.flatMap { file ->
        (1..10).map {
          R2Locator(file.fileName, file.mediaType!!, locations = R2Locator.Location(position = startPosition++, progression = it.toFloat()))
        }
      }
    }

    @Test
    fun `given book when marking progress older than saved then it fails`() {
      val book = bookRepository.findAll().first()
      mediaRepository.findById(book.id).let { media ->
        mediaRepository.update(media.copy(status = Media.Status.READY, mediaType = "application/zip", pageCount = 10))
      }

      val progress = R2Progression(ZonedDateTime.now(), device, R2Locator("", "", locations = R2Locator.Location(position = 5)))

      bookLifecycle.markProgression(book, user1, progress)

      // when
      val thrown =
        catchThrowable {
          bookLifecycle.markProgression(book, user1, progress.copy(modified = progress.modified.minusHours(1)))
        }

      // then
      assertThat(thrown)
        .isInstanceOf(IllegalStateException::class.java)
        .hasMessageContaining("older than existing")
    }

    @ParameterizedTest
    @ValueSource(strings = ["application/zip", "application/pdf"])
    fun `given divina or pdf book when marking progress over the page count then it fails`(mediaType: String) {
      val book = bookRepository.findAll().first()
      mediaRepository.findById(book.id).let { media ->
        mediaRepository.update(media.copy(status = Media.Status.READY, pageCount = 10, mediaType = mediaType))
      }

      // when
      val thrown =
        catchThrowable {
          bookLifecycle.markProgression(book, user1, R2Progression(ZonedDateTime.now(), device, R2Locator("", "", locations = R2Locator.Location(position = 15))))
        }

      // then
      assertThat(thrown)
        .isInstanceOf(IllegalArgumentException::class.java)
        .hasMessageContaining("must be within 1 and book page count")
    }

    @Test
    fun `given epub book when marking progress with non-existing href then it fails`() {
      val book = bookRepository.findAll().first()
      mediaRepository.findById(book.id).let { media ->
        mediaRepository.update(media.copy(status = Media.Status.READY, mediaType = "application/epub+zip", files = epubResources))
      }

      // when
      val thrown =
        catchThrowable {
          bookLifecycle.markProgression(book, user1, R2Progression(ZonedDateTime.now(), device, R2Locator("ch5.xhtml", "", locations = R2Locator.Location(position = 15))))
        }

      // then
      assertThat(thrown)
        .isInstanceOf(IllegalArgumentException::class.java)
        .hasMessageContaining("Resource does not exist in book")
    }

    @Test
    fun `given epub book when marking progress without location progression then it fails`() {
      val book = bookRepository.findAll().first()
      mediaRepository.findById(book.id).let { media ->
        mediaRepository.update(media.copy(status = Media.Status.READY, mediaType = "application/epub+zip", files = epubResources))
      }

      // when
      val thrown =
        catchThrowable {
          bookLifecycle.markProgression(book, user1, R2Progression(ZonedDateTime.now(), device, R2Locator("ch1.xhtml", "", locations = R2Locator.Location(position = 15))))
        }

      // then
      assertThat(thrown)
        .isInstanceOf(IllegalArgumentException::class.java)
        .hasMessageContaining("location.progression is required")
    }

    @Test
    fun `given epub book without extension when marking progress then it fails`() {
      val book = bookRepository.findAll().first()
      mediaRepository.findById(book.id).let { media ->
        mediaRepository.update(media.copy(status = Media.Status.READY, mediaType = "application/epub+zip", files = epubResources))
      }

      // when
      val thrown =
        catchThrowable {
          bookLifecycle.markProgression(book, user1, R2Progression(ZonedDateTime.now(), device, R2Locator("ch1.xhtml", "", locations = R2Locator.Location(progression = 0.3F))))
        }

      // then
      assertThat(thrown)
        .isInstanceOf(IllegalArgumentException::class.java)
        .hasMessageContaining("extension not found")
    }

    @Test
    fun `given epub book when marking progress with exact position then it succeeds`() {
      val book = bookRepository.findAll().first()
      val epubPositions = makeEpubPositions()
      mediaRepository.findById(book.id).let { media ->
        mediaRepository.update(media.copy(status = Media.Status.READY, mediaType = "application/epub+zip", files = epubResources, extension = MediaExtensionEpub(positions = epubPositions)))
      }

      // when
      val newProgression = R2Progression(ZonedDateTime.now(), device, epubPositions.first())
      assertThatNoException().isThrownBy {
        bookLifecycle.markProgression(book, user1, newProgression)
      }
      val savedProgression = readProgressRepository.findByBookIdAndUserIdOrNull(book.id, user1.id)

      // then
      assertThat(savedProgression).isNotNull
      assertThat(savedProgression!!.locator).isEqualTo(newProgression.locator)
    }

    @Test
    fun `given epub book when marking progress with skewed position then it succeeds`() {
      val book = bookRepository.findAll().first()
      val epubPositions = makeEpubPositions()
      mediaRepository.findById(book.id).let { media ->
        mediaRepository.update(media.copy(status = Media.Status.READY, mediaType = "application/epub+zip", files = epubResources, extension = MediaExtensionEpub(positions = epubPositions)))
      }

      // when
      val newProgression =
        R2Progression(
          ZonedDateTime.now(),
          device,
          with(epubPositions[0]) {
            copy(locations = locations!!.copy(progression = locations!!.progression!! + 0.5F))
          },
        )
      assertThatNoException().isThrownBy {
        bookLifecycle.markProgression(book, user1, newProgression)
      }
      val savedProgression = readProgressRepository.findByBookIdAndUserIdOrNull(book.id, user1.id)

      // then
      assertThat(savedProgression).isNotNull
      assertThat(savedProgression!!.locator).isEqualTo(newProgression.locator)
    }
  }
}
