package org.gotson.komga.domain.service

import com.google.common.jimfs.Configuration
import com.google.common.jimfs.Jimfs
import com.ninjasquad.springmockk.SpykBean
import io.mockk.every
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.catchThrowable
import org.gotson.komga.domain.model.BookMetadata
import org.gotson.komga.domain.model.Dimension
import org.gotson.komga.domain.model.Media
import org.gotson.komga.domain.model.ThumbnailBook
import org.gotson.komga.domain.model.ThumbnailSeries
import org.gotson.komga.domain.model.makeBook
import org.gotson.komga.domain.model.makeLibrary
import org.gotson.komga.domain.model.makeSeries
import org.gotson.komga.domain.persistence.BookMetadataAggregationRepository
import org.gotson.komga.domain.persistence.BookMetadataRepository
import org.gotson.komga.domain.persistence.BookRepository
import org.gotson.komga.domain.persistence.LibraryRepository
import org.gotson.komga.domain.persistence.MediaRepository
import org.gotson.komga.domain.persistence.SeriesMetadataRepository
import org.gotson.komga.domain.persistence.SeriesRepository
import org.gotson.komga.domain.persistence.ThumbnailBookRepository
import org.gotson.komga.domain.persistence.ThumbnailSeriesRepository
import org.jooq.exception.DataAccessException
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import java.nio.file.Files
import java.nio.file.Paths

@SpringBootTest
class SeriesLifecycleTest(
  @Autowired private val seriesLifecycle: SeriesLifecycle,
  @Autowired private val seriesRepository: SeriesRepository,
  @Autowired private val bookRepository: BookRepository,
  @Autowired private val libraryRepository: LibraryRepository,
  @Autowired private val thumbnailSeriesRepository: ThumbnailSeriesRepository,
  @Autowired private val thumbnailBookRepository: ThumbnailBookRepository,
) {
  @SpykBean
  private lateinit var bookLifecycle: BookLifecycle

  @SpykBean
  private lateinit var seriesMetadataRepository: SeriesMetadataRepository

  @SpykBean
  private lateinit var bookMetadataAggregationRepository: BookMetadataAggregationRepository

  @SpykBean
  private lateinit var mediaRepository: MediaRepository

  @SpykBean
  private lateinit var bookMetadataRepository: BookMetadataRepository

  private val library = makeLibrary()

  @BeforeAll
  fun `setup library`() {
    libraryRepository.insert(library)
  }

  @AfterAll
  fun `teardown library`() {
    libraryRepository.deleteAll()
  }

  @AfterEach
  fun `clear repository`() {
    seriesLifecycle.deleteMany(seriesRepository.findAll())
  }

  @Test
  fun `given series with unordered books when saving then books are ordered with natural sort`() {
    // given
    val books =
      listOf(
        makeBook("book 1", libraryId = library.id),
        makeBook("boôk 05", libraryId = library.id),
        makeBook("  book 3", libraryId = library.id),
        makeBook("book   4   ", libraryId = library.id),
        makeBook("book  6", libraryId = library.id),
        makeBook("book  002", libraryId = library.id),
      )
    val createdSeries =
      makeSeries(name = "series", libraryId = library.id).let {
        seriesLifecycle.createSeries(it)
      }
    seriesLifecycle.addBooks(createdSeries, books)

    // when
    seriesLifecycle.sortBooks(createdSeries)

    // then
    assertThat(seriesRepository.count()).isEqualTo(1)
    assertThat(bookRepository.count()).isEqualTo(6)

    val savedBooks = bookRepository.findAllBySeriesId(createdSeries.id).sortedBy { it.number }
    assertThat(savedBooks.map { it.name }).containsExactly("book 1", "book  002", "  book 3", "book   4   ", "boôk 05", "book  6")
    assertThat(savedBooks.map { it.number }).containsExactly(1, 2, 3, 4, 5, 6)
  }

  @Test
  fun `given series when removing a book then remaining books are indexed in sequence`() {
    // given
    val books =
      listOf(
        makeBook("book 1", libraryId = library.id),
        makeBook("book 2", libraryId = library.id),
        makeBook("book 3", libraryId = library.id),
        makeBook("book 4", libraryId = library.id),
      )
    val createdSeries =
      makeSeries(name = "series", libraryId = library.id).let {
        seriesLifecycle.createSeries(it)
      }
    seriesLifecycle.addBooks(createdSeries, books)
    seriesLifecycle.sortBooks(createdSeries)

    // when
    val book = bookRepository.findAllBySeriesId(createdSeries.id).first { it.name == "book 2" }
    bookLifecycle.deleteOne(book)
    seriesLifecycle.sortBooks(createdSeries)

    // then
    assertThat(seriesRepository.count()).isEqualTo(1)
    assertThat(bookRepository.count()).isEqualTo(3)

    val savedBooks = bookRepository.findAllBySeriesId(createdSeries.id).sortedBy { it.number }
    assertThat(savedBooks.map { it.name }).containsExactly("book 1", "book 3", "book 4")
    assertThat(savedBooks.map { it.number }).containsExactly(1, 2, 3)
  }

  @Test
  fun `given series when adding a book then all books are indexed in sequence`() {
    // given
    val books =
      listOf(
        makeBook("book 1", libraryId = library.id),
        makeBook("book 2", libraryId = library.id),
        makeBook("book 4", libraryId = library.id),
        makeBook("book 5", libraryId = library.id),
      )
    val createdSeries =
      makeSeries(name = "series", libraryId = library.id).let {
        seriesLifecycle.createSeries(it)
      }
    seriesLifecycle.addBooks(createdSeries, books)
    seriesLifecycle.sortBooks(createdSeries)

    // when
    val book = makeBook("book 3", libraryId = library.id)
    seriesLifecycle.addBooks(createdSeries, listOf(book))
    seriesLifecycle.sortBooks(createdSeries)

    // then
    assertThat(seriesRepository.count()).isEqualTo(1)
    assertThat(bookRepository.count()).isEqualTo(5)

    val savedBooks = bookRepository.findAllBySeriesId(createdSeries.id).sortedBy { it.number }
    assertThat(savedBooks.map { it.name }).containsExactly("book 1", "book 2", "book 3", "book 4", "book 5")
    assertThat(savedBooks.map { it.number }).containsExactly(1, 2, 3, 4, 5)
  }

  @Nested
  inner class Transactions {
    @Test
    fun `given series when saving and an exception occur while saving metadata then series is not saved`() {
      // given
      val series = makeSeries(name = "series", libraryId = library.id)
      every { seriesMetadataRepository.insert(any()) } throws DataAccessException("")

      // when
      val thrown = catchThrowable { seriesLifecycle.createSeries(series) }

      // then
      assertThat(thrown).isInstanceOf(RuntimeException::class.java)
      assertThat(bookMetadataAggregationRepository.count()).isEqualTo(0)
      assertThat(seriesMetadataRepository.count()).isEqualTo(0)
      assertThat(seriesRepository.count()).isEqualTo(0)
    }

    @Test
    fun `given series when saving and an exception occur while saving metadata aggregation then series is not saved`() {
      // given
      val series = makeSeries(name = "series", libraryId = library.id)
      every { bookMetadataAggregationRepository.insert(any()) } throws DataAccessException("")

      // when
      val thrown = catchThrowable { seriesLifecycle.createSeries(series) }

      // then
      assertThat(thrown).isInstanceOf(RuntimeException::class.java)
      assertThat(bookMetadataAggregationRepository.count()).isEqualTo(0)
      assertThat(seriesMetadataRepository.count()).isEqualTo(0)
      assertThat(seriesRepository.count()).isEqualTo(0)
    }

    @Test
    fun `given series when adding books and an exception occur while saving media then books are not saved`() {
      val books =
        listOf(
          makeBook("book 1", libraryId = library.id),
        )
      val createdSeries =
        makeSeries(name = "series", libraryId = library.id).let {
          seriesLifecycle.createSeries(it)
        }

      every { mediaRepository.insert(any<Collection<Media>>()) } throws DataAccessException("")

      // when
      val thrown = catchThrowable { seriesLifecycle.addBooks(createdSeries, books) }

      // then
      assertThat(thrown).isInstanceOf(Exception::class.java)
      assertThat(mediaRepository.count()).isEqualTo(0)
      assertThat(bookMetadataRepository.count()).isEqualTo(0)
      assertThat(bookRepository.count()).isEqualTo(0)
    }

    @Test
    fun `given series when adding books and an exception occur while saving metadata then books are not saved`() {
      val books =
        listOf(
          makeBook("book 1", libraryId = library.id),
        )
      val createdSeries =
        makeSeries(name = "series", libraryId = library.id).let {
          seriesLifecycle.createSeries(it)
        }

      every { bookMetadataRepository.insert(any<Collection<BookMetadata>>()) } throws DataAccessException("")

      // when
      val thrown = catchThrowable { seriesLifecycle.addBooks(createdSeries, books) }

      // then
      assertThat(thrown).isInstanceOf(Exception::class.java)
      assertThat(mediaRepository.count()).isEqualTo(0)
      assertThat(bookMetadataRepository.count()).isEqualTo(0)
      assertThat(bookRepository.count()).isEqualTo(0)
    }
  }

  @Test
  fun `given a sidecar thumbnail when deleting then IllegarlArgumentException is thrown`() {
    val thumbnail = ThumbnailSeries(type = ThumbnailSeries.Type.SIDECAR, fileSize = 0, mediaType = "", dimension = Dimension(0, 0))

    val thrown = catchThrowable { seriesLifecycle.deleteThumbnailForSeries(thumbnail) }

    assertThat(thrown).isInstanceOf(IllegalArgumentException::class.java)
  }

  @Test
  fun `given a series when deleting series then series directory is deleted`() {
    Jimfs.newFileSystem(Configuration.unix()).use { fs ->
      // given
      val root = fs.getPath("/root")
      Files.createDirectory(root)
      val seriesPath = root.resolve("series")
      Files.createDirectory(seriesPath)
      val book1Path = seriesPath.resolve("book1.cbz")
      Files.createFile(book1Path)
      val book2Path = seriesPath.resolve("book2.cbz")
      Files.createFile(book2Path)
      val bookSidecarPath = seriesPath.resolve("sidecar1.png")
      Files.createFile(bookSidecarPath)

      val series = makeSeries(name = "series", libraryId = library.id, url = seriesPath.toUri().toURL())
      val books =
        listOf(
          makeBook("1", libraryId = library.id, url = book1Path.toUri().toURL()),
          makeBook("2", libraryId = library.id, url = book2Path.toUri().toURL()),
        )
      val bookSidecar = ThumbnailBook(bookId = books[0].id, type = ThumbnailBook.Type.SIDECAR, url = bookSidecarPath.toUri().toURL(), fileSize = 0, mediaType = "", dimension = Dimension(0, 0))

      seriesLifecycle.createSeries(series)
      seriesLifecycle.addBooks(series, books)
      thumbnailBookRepository.insert(bookSidecar)

      // when
      seriesLifecycle.deleteSeriesFiles(series)

      // then
      assertThat(Files.notExists(seriesPath))
    }
  }

  @Test
  fun `given a series with a series sidecar when deleting series then series directory is deleted`() {
    Jimfs.newFileSystem(Configuration.unix()).use { fs ->
      // given
      val root = fs.getPath("/root")
      Files.createDirectory(root)
      val seriesPath = root.resolve("series")
      Files.createDirectory(seriesPath)
      val book1Path = seriesPath.resolve("book1.cbz")
      Files.createFile(book1Path)
      val book2Path = seriesPath.resolve("book2.cbz")
      Files.createFile(book2Path)
      val bookSidecarPath = seriesPath.resolve("sidecar1.png")
      Files.createFile(bookSidecarPath)
      val seriesSidecarPath = seriesPath.resolve("cover.png")
      Files.createFile(seriesSidecarPath)

      val series = makeSeries(name = "series", libraryId = library.id, url = seriesPath.toUri().toURL())
      val books =
        listOf(
          makeBook("1", libraryId = library.id, url = book1Path.toUri().toURL()),
          makeBook("2", libraryId = library.id, url = book2Path.toUri().toURL()),
        )
      val bookSidecar = ThumbnailBook(bookId = books[0].id, type = ThumbnailBook.Type.SIDECAR, url = bookSidecarPath.toUri().toURL(), fileSize = 0, mediaType = "", dimension = Dimension(0, 0))
      val seriesSidecar = ThumbnailSeries(seriesId = series.id, type = ThumbnailSeries.Type.SIDECAR, url = seriesSidecarPath.toUri().toURL(), fileSize = 0, mediaType = "", dimension = Dimension(0, 0))

      seriesLifecycle.createSeries(series)
      seriesLifecycle.addBooks(series, books)
      thumbnailBookRepository.insert(bookSidecar)
      thumbnailSeriesRepository.insert(seriesSidecar)

      // when
      seriesLifecycle.deleteSeriesFiles(series)

      // then
      assertThat(Files.notExists(seriesPath))
    }
  }

  @Test
  fun `given a series directory with unrelated files when deleting series then series directory should not be deleted`() {
    Jimfs.newFileSystem(Configuration.unix()).use { fs ->
      // given
      val root = fs.getPath("/root")
      Files.createDirectory(root)
      val seriesPath = root.resolve("series")
      Files.createDirectory(seriesPath)
      val book1Path = seriesPath.resolve("book1.cbz")
      Files.createFile(book1Path)
      val book2Path = seriesPath.resolve("book2.cbz")
      Files.createFile(book2Path)
      val filePath = seriesPath.resolve("file.txt")
      Files.createFile(filePath)
      val bookSidecarPath = seriesPath.resolve("sidecar1.png")
      Files.createFile(bookSidecarPath)
      val seriesSidecarPath = seriesPath.resolve("cover.png")
      Files.createFile(seriesSidecarPath)

      val series = makeSeries(name = "series", libraryId = library.id, url = seriesPath.toUri().toURL())
      val books =
        listOf(
          makeBook("1", libraryId = library.id, url = book1Path.toUri().toURL()),
          makeBook("2", libraryId = library.id, url = book2Path.toUri().toURL()),
        )
      val bookSidecar = ThumbnailBook(bookId = books[0].id, type = ThumbnailBook.Type.SIDECAR, url = bookSidecarPath.toUri().toURL(), fileSize = 0, mediaType = "", dimension = Dimension(0, 0))
      val seriesSidecar = ThumbnailSeries(seriesId = series.id, type = ThumbnailSeries.Type.SIDECAR, url = seriesSidecarPath.toUri().toURL(), fileSize = 0, mediaType = "", dimension = Dimension(0, 0))

      seriesLifecycle.createSeries(series)
      seriesLifecycle.addBooks(series, books)
      thumbnailBookRepository.insert(bookSidecar)
      thumbnailSeriesRepository.insert(seriesSidecar)

      // when
      seriesLifecycle.deleteSeriesFiles(series)

      // then
      assertThat(Files.exists(seriesPath))
      assertThat(Files.exists(filePath))
      assertThat(Files.notExists(book1Path))
      assertThat(Files.notExists(book2Path))
      assertThat(Files.notExists(bookSidecarPath))
      assertThat(Files.notExists(seriesSidecarPath))
    }
  }

  @Test
  fun `given a non-existent series directory when deleting series then it returns`() {
    // given
    val seriesPath = Paths.get("/non-existent")
    val series = makeSeries(name = "series", libraryId = library.id, url = seriesPath.toUri().toURL())

    // when
    seriesLifecycle.deleteSeriesFiles(series)

    // then
    verify(exactly = 0) { bookLifecycle.softDeleteMany(any()) }
  }

  @Test
  fun `given a series and a non-existent sidecar file when deleting series then series should be deleted`() {
    Jimfs.newFileSystem(Configuration.unix()).use { fs ->
      // given
      val root = fs.getPath("/root")
      Files.createDirectory(root)
      val seriesPath = root.resolve("series")
      Files.createDirectory(seriesPath)
      val book1Path = seriesPath.resolve("book1.cbz")
      Files.createFile(book1Path)
      val book2Path = seriesPath.resolve("book2.cbz")
      Files.createFile(book2Path)
      val bookSidecarPath = seriesPath.resolve("sidecar1.png")
      Files.createFile(bookSidecarPath)
      val seriesSidecarPath = seriesPath.resolve("cover.png")

      val series = makeSeries(name = "series", libraryId = library.id, url = seriesPath.toUri().toURL())
      val books =
        listOf(
          makeBook("1", libraryId = library.id, url = book1Path.toUri().toURL()),
          makeBook("2", libraryId = library.id, url = book2Path.toUri().toURL()),
        )
      val bookSidecar = ThumbnailBook(bookId = books[0].id, type = ThumbnailBook.Type.SIDECAR, url = bookSidecarPath.toUri().toURL(), fileSize = 0, mediaType = "", dimension = Dimension(0, 0))
      val seriesSidecar = ThumbnailSeries(seriesId = series.id, type = ThumbnailSeries.Type.SIDECAR, url = seriesSidecarPath.toUri().toURL(), fileSize = 0, mediaType = "", dimension = Dimension(0, 0))

      seriesLifecycle.createSeries(series)
      seriesLifecycle.addBooks(series, books)
      thumbnailBookRepository.insert(bookSidecar)
      thumbnailSeriesRepository.insert(seriesSidecar)

      // when
      seriesLifecycle.deleteSeriesFiles(series)

      // then
      assertThat(Files.notExists(seriesPath))
    }
  }
}
