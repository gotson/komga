package org.gotson.komga.domain.service

import com.google.common.jimfs.Configuration
import com.google.common.jimfs.Jimfs
import com.ninjasquad.springmockk.MockkBean
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.verify
import org.assertj.core.api.Assertions
import org.assertj.core.api.Assertions.assertThat
import org.gotson.komga.application.tasks.TaskEmitter
import org.gotson.komga.domain.model.Book
import org.gotson.komga.domain.model.BookPage
import org.gotson.komga.domain.model.CopyMode
import org.gotson.komga.domain.model.Dimension
import org.gotson.komga.domain.model.KomgaUser
import org.gotson.komga.domain.model.MarkSelectedPreference
import org.gotson.komga.domain.model.Media
import org.gotson.komga.domain.model.PathContainedInPath
import org.gotson.komga.domain.model.ReadList
import org.gotson.komga.domain.model.ThumbnailBook
import org.gotson.komga.domain.model.makeBook
import org.gotson.komga.domain.model.makeLibrary
import org.gotson.komga.domain.model.makeSeries
import org.gotson.komga.domain.persistence.BookMetadataRepository
import org.gotson.komga.domain.persistence.BookRepository
import org.gotson.komga.domain.persistence.KomgaUserRepository
import org.gotson.komga.domain.persistence.LibraryRepository
import org.gotson.komga.domain.persistence.MediaRepository
import org.gotson.komga.domain.persistence.ReadListRepository
import org.gotson.komga.domain.persistence.ReadProgressRepository
import org.gotson.komga.domain.persistence.SeriesRepository
import org.gotson.komga.language.toIndexedMap
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import java.io.FileNotFoundException
import java.nio.file.FileAlreadyExistsException
import java.nio.file.Paths
import kotlin.io.path.createDirectories
import kotlin.io.path.createDirectory
import kotlin.io.path.createFile

@SpringBootTest
class BookImporterTest(
  @Autowired private val bookImporter: BookImporter,
  @Autowired private val bookRepository: BookRepository,
  @Autowired private val bookLifecycle: BookLifecycle,
  @Autowired private val readProgressRepository: ReadProgressRepository,
  @Autowired private val libraryRepository: LibraryRepository,
  @Autowired private val seriesRepository: SeriesRepository,
  @Autowired private val seriesLifecycle: SeriesLifecycle,
  @Autowired private val mediaRepository: MediaRepository,
  @Autowired private val metadataRepository: BookMetadataRepository,
  @Autowired private val userRepository: KomgaUserRepository,
  @Autowired private val readListRepository: ReadListRepository,
  @Autowired private val readListLifecycle: ReadListLifecycle,
) {
  private val library = makeLibrary("lib", "file:/library")
  private val user1 = KomgaUser("user1@example.org", "")
  private val user2 = KomgaUser("user2@example.org", "")

  @MockkBean
  private lateinit var mockTackReceiver: TaskEmitter

  @BeforeAll
  fun init() {
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

  @BeforeEach
  fun initMocks() {
    every { mockTackReceiver.refreshBookMetadata(any<Book>(), any(), any()) } just Runs
    every { mockTackReceiver.refreshBookLocalArtwork(any<Book>(), any()) } just Runs
  }

  @AfterEach
  fun `clear repository`() {
    seriesLifecycle.deleteMany(seriesRepository.findAll())
  }

  @Test
  fun `given non-existent source file when importing then exception is thrown`() {
    // given
    val sourceFile = Paths.get("/non-existent")

    // when
    val thrown =
      Assertions.catchThrowable {
        bookImporter.importBook(sourceFile, makeSeries("a series"), CopyMode.COPY)
      }

    // then
    assertThat(thrown).hasCauseInstanceOf(FileNotFoundException::class.java)
  }

  @Test
  fun `given existing target when importing then exception is thrown`() {
    Jimfs.newFileSystem(Configuration.unix()).use { fs ->
      // given
      val sourceDir = fs.getPath("/source").createDirectory()
      val sourceFile = sourceDir.resolve("source.cbz").createFile()
      val destDir = fs.getPath("/dest").createDirectory()
      destDir.resolve("source.cbz").createFile()

      val series = makeSeries("dest", url = destDir.toUri().toURL())

      // when
      val thrown =
        Assertions.catchThrowable {
          bookImporter.importBook(sourceFile, series, CopyMode.COPY)
        }

      // then
      assertThat(thrown).hasCauseInstanceOf(FileAlreadyExistsException::class.java)
    }
  }

  @Test
  fun `given existing target when importing with destination name then exception is thrown`() {
    Jimfs.newFileSystem(Configuration.unix()).use { fs ->
      // given
      val sourceDir = fs.getPath("/source").createDirectory()
      val sourceFile = sourceDir.resolve("source.cbz").createFile()
      val destDir = fs.getPath("/dest").createDirectory()
      destDir.resolve("dest.cbz").createFile()

      val series = makeSeries("dest").copy(url = destDir.toUri().toURL())

      // when
      val thrown =
        Assertions.catchThrowable {
          bookImporter.importBook(sourceFile, series, CopyMode.COPY, destinationName = "dest")
        }

      // then
      assertThat(thrown).hasCauseInstanceOf(FileAlreadyExistsException::class.java)
    }
  }

  @Test
  fun `given source file part of a Komga library when importing then exception is thrown`() {
    Jimfs.newFileSystem(Configuration.unix()).use { fs ->
      // given
      val sourceDir = fs.getPath("/source").createDirectory()
      val sourceFile = sourceDir.resolve("source.cbz").createFile()
      val destDir = fs.getPath("/dest").createDirectory()

      val series = makeSeries("dest").copy(url = destDir.toUri().toURL())

      val libraryJimfs = makeLibrary("jimfs", url = sourceDir.toUri().toURL())
      libraryRepository.insert(libraryJimfs)

      // when
      val thrown =
        Assertions.catchThrowable {
          bookImporter.importBook(sourceFile, series, CopyMode.COPY)
        }

      // then
      assertThat(thrown).isInstanceOf(PathContainedInPath::class.java)

      libraryRepository.delete(libraryJimfs.id)
    }
  }

  @Test
  fun `given book when importing then book is imported and series is sorted`() {
    Jimfs.newFileSystem(Configuration.unix()).use { fs ->
      // given
      val sourceDir = fs.getPath("/source").createDirectory()
      val sourceFile = sourceDir.resolve("2.cbz").createFile()
      val destDir = fs.getPath("/library/series").createDirectories()

      val existingBooks =
        listOf(
          makeBook("1", libraryId = library.id),
          makeBook("3", libraryId = library.id),
        )
      val series =
        makeSeries("series", url = destDir.toUri().toURL(), libraryId = library.id)
          .also { series ->
            seriesLifecycle.createSeries(series)
            seriesLifecycle.addBooks(series, existingBooks)
            seriesLifecycle.sortBooks(series)
          }

      // when
      bookImporter.importBook(sourceFile, series, CopyMode.COPY)

      // then
      val books = bookRepository.findAllBySeriesId(series.id).sortedBy { it.number }
      assertThat(books).hasSize(3)
      assertThat(books[0].id).isEqualTo(existingBooks[0].id)
      assertThat(books[2].id).isEqualTo(existingBooks[1].id)

      with(books[1]) {
        assertThat(id)
          .isNotEqualTo(existingBooks[0].id)
          .isNotEqualTo(existingBooks[1].id)
        assertThat(number).isEqualTo(2)
        assertThat(name).isEqualTo("2")

        val newMedia = mediaRepository.findById(id)
        assertThat(newMedia.status).isEqualTo(Media.Status.UNKNOWN)
      }
    }
  }

  @Test
  fun `given book with sidecars when importing then book and sidecars are imported`() {
    Jimfs.newFileSystem(Configuration.unix()).use { fs ->
      // given
      val sourceDir = fs.getPath("/source").createDirectory()
      val sourceFile = sourceDir.resolve("book 2.cbz").createFile()
      sourceDir.resolve("book 2.jpg").createFile()
      sourceDir.resolve("BOOK 2-1.jpg").createFile()
      val destDir = fs.getPath("/library/series").createDirectories()

      val existingBooks =
        listOf(
          makeBook("1", libraryId = library.id),
          makeBook("3", libraryId = library.id),
        )
      val series =
        makeSeries("series", url = destDir.toUri().toURL(), libraryId = library.id)
          .also { series ->
            seriesLifecycle.createSeries(series)
            seriesLifecycle.addBooks(series, existingBooks)
            seriesLifecycle.sortBooks(series)
          }

      // when
      bookImporter.importBook(sourceFile, series, CopyMode.COPY)

      // then
      verify(exactly = 2) { mockTackReceiver.refreshBookLocalArtwork(any<Book>()) }

      assertThat(destDir.resolve("book 2.cbz")).exists()
      assertThat(destDir.resolve("book 2.jpg")).exists()
      assertThat(destDir.resolve("BOOK 2-1.jpg")).exists()
    }
  }

  @Test
  fun `given book with sidecars when importing with destination name then book and sidecars are imported`() {
    Jimfs.newFileSystem(Configuration.unix()).use { fs ->
      // given
      val sourceDir = fs.getPath("/source").createDirectory()
      val sourceFile = sourceDir.resolve("book 2.cbz").createFile()
      sourceDir.resolve("book 2.jpg").createFile()
      sourceDir.resolve("BOOK 2-1.jpg").createFile()
      val destDir = fs.getPath("/library/series").createDirectories()

      val existingBooks =
        listOf(
          makeBook("1", libraryId = library.id),
          makeBook("3", libraryId = library.id),
        )
      val series =
        makeSeries("series", url = destDir.toUri().toURL(), libraryId = library.id)
          .also { series ->
            seriesLifecycle.createSeries(series)
            seriesLifecycle.addBooks(series, existingBooks)
            seriesLifecycle.sortBooks(series)
          }

      // when
      bookImporter.importBook(sourceFile, series, CopyMode.COPY, destinationName = "book 5")

      // then
      verify(exactly = 2) { mockTackReceiver.refreshBookLocalArtwork(any<Book>()) }

      assertThat(destDir.resolve("book 5.cbz")).exists()
      assertThat(destDir.resolve("book 5.jpg")).exists()
      assertThat(destDir.resolve("book 5-1.jpg")).exists()
    }
  }

  @Test
  fun `given existing book when importing with upgrade then existing book and sidecars are deleted`() {
    Jimfs.newFileSystem(Configuration.unix()).use { fs ->
      // given
      val sourceDir = fs.getPath("/source").createDirectory()
      val sourceFile = sourceDir.resolve("source.cbz").createFile()
      val destDir = fs.getPath("/library/series").createDirectories()
      val existingFile = destDir.resolve("4.cbz").createFile()
      val existingSidecar = destDir.resolve("4.jpg").createFile()
      val existingSidecar2 = destDir.resolve("4-1.jpg").createFile()

      val bookToUpgrade = makeBook("2", libraryId = library.id, url = existingFile.toUri().toURL())
      val otherBooks =
        listOf(
          makeBook("1", libraryId = library.id),
          makeBook("3", libraryId = library.id),
        )
      val series =
        makeSeries("series", url = destDir.toUri().toURL(), libraryId = library.id)
          .also { series ->
            seriesLifecycle.createSeries(series)
            seriesLifecycle.addBooks(series, listOf(bookToUpgrade) + otherBooks)
            seriesLifecycle.sortBooks(series)
          }

      // when
      bookImporter.importBook(sourceFile, series, CopyMode.MOVE, upgradeBookId = bookToUpgrade.id)

      // then
      val books = bookRepository.findAllBySeriesId(series.id).sortedBy { it.number }
      assertThat(books).hasSize(3)
      assertThat(books[0].id).isEqualTo(otherBooks[0].id)
      assertThat(books[1].id).isEqualTo(otherBooks[1].id)
      assertThat(books[2].id).isNotEqualTo(bookToUpgrade.id)

      assertThat(bookRepository.findByIdOrNull(bookToUpgrade.id)).isNull()

      val upgradedMedia = mediaRepository.findById(books[2].id)
      assertThat(upgradedMedia.status).isEqualTo(Media.Status.OUTDATED)

      assertThat(sourceFile).doesNotExist()

      assertThat(existingFile).doesNotExist()
      assertThat(existingSidecar).doesNotExist()
      assertThat(existingSidecar2).doesNotExist()
    }
  }

  @Test
  fun `given existing book with metadata when importing with upgrade then metadata is kept`() {
    Jimfs.newFileSystem(Configuration.unix()).use { fs ->
      // given
      val sourceDir = fs.getPath("/source").createDirectory()
      val sourceFile = sourceDir.resolve("source.cbz").createFile()
      val destDir = fs.getPath("/library/series").createDirectories()
      val existingFile = destDir.resolve("4.cbz").createFile()

      val bookToUpgrade = makeBook("2", libraryId = library.id, url = existingFile.toUri().toURL())
      val otherBooks =
        listOf(
          makeBook("1", libraryId = library.id),
          makeBook("3", libraryId = library.id),
        )
      val series =
        makeSeries("series", url = destDir.toUri().toURL(), libraryId = library.id)
          .also { series ->
            seriesLifecycle.createSeries(series)
            seriesLifecycle.addBooks(series, listOf(bookToUpgrade) + otherBooks)
            seriesLifecycle.sortBooks(series)
          }

      metadataRepository.findById(bookToUpgrade.id).let {
        metadataRepository.update(
          it.copy(
            summary = "a summary",
            number = "HS",
            numberLock = true,
            numberSort = 100F,
            numberSortLock = true,
          ),
        )
      }

      bookLifecycle.addThumbnailForBook(ThumbnailBook(ByteArray(10), type = ThumbnailBook.Type.USER_UPLOADED, mediaType = "image/jpeg", fileSize = 10L, dimension = Dimension(1, 1), bookId = bookToUpgrade.id), MarkSelectedPreference.YES)

      // when
      bookImporter.importBook(sourceFile, series, CopyMode.MOVE, upgradeBookId = bookToUpgrade.id)

      // then
      val books = bookRepository.findAllBySeriesId(series.id).sortedBy { it.number }
      assertThat(books).hasSize(3)
      assertThat(books[0].id).isEqualTo(otherBooks[0].id)
      assertThat(books[1].id).isEqualTo(otherBooks[1].id)
      assertThat(books[2].id).isNotEqualTo(bookToUpgrade.id)

      assertThat(bookRepository.findByIdOrNull(bookToUpgrade.id)).isNull()

      val upgradedMedia = mediaRepository.findById(books[2].id)
      assertThat(upgradedMedia.status).isEqualTo(Media.Status.OUTDATED)

      with(metadataRepository.findById(books[2].id)) {
        assertThat(summary).isEqualTo("a summary")
        assertThat(number).isEqualTo("HS")
        assertThat(numberLock).isTrue
        assertThat(numberSort).isEqualTo(100F)
        assertThat(numberSortLock).isTrue
      }

      val thumbnail = bookLifecycle.getThumbnail(books[2].id)
      assertThat(thumbnail).isNotNull
      assertThat(thumbnail!!.type).isEqualTo(ThumbnailBook.Type.USER_UPLOADED)
      assertThat(thumbnail.fileSize).isEqualTo(10L)

      assertThat(sourceFile).doesNotExist()
    }
  }

  @Test
  fun `given existing book when importing with upgrade and same name then existing book is replaced`() {
    Jimfs.newFileSystem(Configuration.unix()).use { fs ->
      // given
      val sourceDir = fs.getPath("/source").createDirectory()
      val sourceFile = sourceDir.resolve("source.cbz").createFile()
      val destDir = fs.getPath("/library/series").createDirectories()
      val existingFile = destDir.resolve("2.cbz").createFile()

      val bookToUpgrade = makeBook("2", libraryId = library.id, url = existingFile.toUri().toURL())
      val otherBooks =
        listOf(
          makeBook("1", libraryId = library.id),
          makeBook("3", libraryId = library.id),
        )
      val series =
        makeSeries("series", url = destDir.toUri().toURL(), libraryId = library.id)
          .also { series ->
            seriesLifecycle.createSeries(series)
            seriesLifecycle.addBooks(series, listOf(bookToUpgrade) + otherBooks)
            seriesLifecycle.sortBooks(series)
          }

      // when
      bookImporter.importBook(sourceFile, series, CopyMode.COPY, destinationName = "2", upgradeBookId = bookToUpgrade.id)

      // then
      val books = bookRepository.findAllBySeriesId(series.id).sortedBy { it.number }
      assertThat(books).hasSize(3)
      assertThat(books[0].id).isEqualTo(otherBooks[0].id)
      assertThat(books[1].id).isNotEqualTo(bookToUpgrade.id)
      assertThat(books[2].id).isEqualTo(otherBooks[1].id)

      assertThat(bookRepository.findByIdOrNull(bookToUpgrade.id)).isNull()

      val upgradedMedia = mediaRepository.findById(books[1].id)
      assertThat(upgradedMedia.status).isEqualTo(Media.Status.OUTDATED)

      assertThat(sourceFile).exists()
    }
  }

  @Test
  fun `given book with read progress when importing with upgrade then read progress is kept`() {
    Jimfs.newFileSystem(Configuration.unix()).use { fs ->
      // given
      val sourceDir = fs.getPath("/source").createDirectory()
      val sourceFile = sourceDir.resolve("source.cbz").createFile()
      val destDir = fs.getPath("/library/series").createDirectories()
      val existingFile = destDir.resolve("1.cbz").createFile()

      val bookToUpgrade = makeBook("1", libraryId = library.id, url = existingFile.toUri().toURL())
      val series =
        makeSeries("series", url = destDir.toUri().toURL(), libraryId = library.id)
          .also { series ->
            seriesLifecycle.createSeries(series)
            seriesLifecycle.addBooks(series, listOf(bookToUpgrade))
            seriesLifecycle.sortBooks(series)
          }

      mediaRepository.findById(bookToUpgrade.id).let { media ->
        mediaRepository.update(
          media.copy(
            status = Media.Status.READY,
            pages = (1..10).map { BookPage("$it", "image/jpeg") },
            pageCount = 10,
          ),
        )
      }

      bookLifecycle.markReadProgressCompleted(bookToUpgrade.id, user1)
      bookLifecycle.markReadProgress(bookToUpgrade, user2, 4)

      // when
      bookImporter.importBook(sourceFile, series, CopyMode.MOVE, upgradeBookId = bookToUpgrade.id)

      // then
      val books = bookRepository.findAllBySeriesId(series.id).sortedBy { it.number }
      assertThat(books).hasSize(1)

      val progress = readProgressRepository.findAllByBookId(books[0].id)
      assertThat(progress).hasSize(2)
      with(progress.find { it.userId == user1.id }!!) {
        assertThat(completed).isTrue
      }
      with(progress.find { it.userId == user2.id }!!) {
        assertThat(completed).isFalse
        assertThat(page).isEqualTo(4)
      }
    }
  }

  @Test
  fun `given book part of a read list when importing with upgrade then imported book replaces upgraded book in the read list`() {
    Jimfs.newFileSystem(Configuration.unix()).use { fs ->
      // given
      val sourceDir = fs.getPath("/source").createDirectory()
      val sourceFile = sourceDir.resolve("source.cbz").createFile()
      val destDir = fs.getPath("/library/series").createDirectories()
      val existingFile = destDir.resolve("1.cbz").createFile()

      val bookToUpgrade = makeBook("1", libraryId = library.id, url = existingFile.toUri().toURL())
      val series =
        makeSeries("series", url = destDir.toUri().toURL(), libraryId = library.id)
          .also { series ->
            seriesLifecycle.createSeries(series)
            seriesLifecycle.addBooks(series, listOf(bookToUpgrade))
            seriesLifecycle.sortBooks(series)
          }

      val readList =
        ReadList(
          name = "readlist",
          bookIds = listOf(bookToUpgrade.id).toIndexedMap(),
        )
      readListLifecycle.addReadList(readList)

      // when
      bookImporter.importBook(sourceFile, series, CopyMode.MOVE, upgradeBookId = bookToUpgrade.id)

      // then
      val books = bookRepository.findAllBySeriesId(series.id).sortedBy { it.number }
      assertThat(books).hasSize(1)

      with(readListRepository.findByIdOrNull(readList.id)!!) {
        assertThat(bookIds).hasSize(1)
        assertThat(bookIds[0]).isEqualTo(books[0].id)
      }
    }
  }
}
