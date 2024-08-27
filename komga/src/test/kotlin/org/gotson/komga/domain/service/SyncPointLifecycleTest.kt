package org.gotson.komga.domain.service

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.within
import org.gotson.komga.domain.model.AgeRestriction
import org.gotson.komga.domain.model.AllowExclude
import org.gotson.komga.domain.model.ContentRestrictions
import org.gotson.komga.domain.model.KomgaUser
import org.gotson.komga.domain.model.Media
import org.gotson.komga.domain.model.MediaType
import org.gotson.komga.domain.model.makeBook
import org.gotson.komga.domain.model.makeLibrary
import org.gotson.komga.domain.model.makeSeries
import org.gotson.komga.domain.persistence.BookMetadataRepository
import org.gotson.komga.domain.persistence.BookRepository
import org.gotson.komga.domain.persistence.KomgaUserRepository
import org.gotson.komga.domain.persistence.LibraryRepository
import org.gotson.komga.domain.persistence.MediaRepository
import org.gotson.komga.domain.persistence.SeriesMetadataRepository
import org.gotson.komga.domain.persistence.SeriesRepository
import org.gotson.komga.domain.persistence.SyncPointRepository
import org.gotson.komga.language.toZonedDateTime
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

@SpringBootTest
class SyncPointLifecycleTest(
  @Autowired private val syncPointLifecycle: SyncPointLifecycle,
  @Autowired private val syncPointRepository: SyncPointRepository,
  @Autowired private val libraryRepository: LibraryRepository,
  @Autowired private val userRepository: KomgaUserRepository,
  @Autowired private val bookRepository: BookRepository,
  @Autowired private val bookMetadataRepository: BookMetadataRepository,
  @Autowired private val seriesMetadataRepository: SeriesMetadataRepository,
  @Autowired private val seriesRepository: SeriesRepository,
  @Autowired private val seriesLifecycle: SeriesLifecycle,
  @Autowired private val mediaRepository: MediaRepository,
) {
  @Autowired
  private lateinit var bookLifecycle: BookLifecycle
  private val library1 = makeLibrary()
  private val library2 = makeLibrary()
  private val library3 = makeLibrary()
  private val user1 =
    KomgaUser(
      "user1@example.org",
      "",
      false,
      sharedLibrariesIds = setOf(library1.id, library2.id),
      restrictions =
        ContentRestrictions(
          ageRestriction = AgeRestriction(18, AllowExclude.EXCLUDE),
          labelsExclude = setOf("exclude"),
        ),
    )

  @BeforeAll
  fun `setup library`() {
    libraryRepository.insert(library1)
    libraryRepository.insert(library2)
    libraryRepository.insert(library3)
    userRepository.insert(user1)
  }

  @AfterAll
  fun teardown() {
    libraryRepository.deleteAll()
    syncPointRepository.deleteAll()
    userRepository.deleteAll()
  }

  @AfterEach
  fun `clear repository`() {
    seriesLifecycle.deleteMany(seriesRepository.findAll())
  }

  @Test
  fun `given user when creating syncpoint then all in-scope books are included`() {
    // given
    val bookValid = makeBook("valid", libraryId = library1.id).copy(fileHash = "hash", fileSize = 12, fileLastModified = LocalDateTime.now())
    val bookExcludedByAge = makeBook("age restriction", libraryId = library1.id)
    val bookExcludedByLabel = makeBook("label restriction", libraryId = library1.id)
    val bookDeleted = makeBook("deleted", libraryId = library1.id).copy(deletedDate = LocalDateTime.now())
    val bookNotReady = makeBook("media not ready", libraryId = library1.id)
    val bookNotEpub = makeBook("not epub", libraryId = library1.id)
    val bookOtherLibrary = makeBook("lib not in list", libraryId = library2.id)
    val bookUnauthorizedLibrary = makeBook("unauthorized lib", libraryId = library3.id)

    makeSeries(name = "series1", libraryId = library1.id).let { series ->
      seriesLifecycle.createSeries(series).let { created ->
        seriesLifecycle.addBooks(created, listOf(bookValid, bookDeleted, bookNotReady, bookNotEpub))
      }
    }
    makeSeries(name = "series age restricted", libraryId = library1.id).let { series ->
      seriesLifecycle.createSeries(series).let { created ->
        seriesLifecycle.addBooks(created, listOf(bookExcludedByAge))
      }
      seriesMetadataRepository.findById(series.id).let { seriesMetadataRepository.update(it.copy(ageRating = 20)) }
    }
    makeSeries(name = "series label restricted", libraryId = library1.id).let { series ->
      seriesLifecycle.createSeries(series).let { created ->
        seriesLifecycle.addBooks(created, listOf(bookExcludedByLabel))
      }
      seriesMetadataRepository.findById(series.id).let { seriesMetadataRepository.update(it.copy(sharingLabels = setOf("exclude"))) }
    }
    makeSeries(name = "series2", libraryId = library2.id).let { series ->
      seriesLifecycle.createSeries(series).let { created ->
        seriesLifecycle.addBooks(created, listOf(bookOtherLibrary))
      }
    }
    makeSeries(name = "series3", libraryId = library3.id).let { series ->
      seriesLifecycle.createSeries(series).let { created ->
        seriesLifecycle.addBooks(created, listOf(bookUnauthorizedLibrary))
      }
    }

    bookRepository.findAll().forEach { mediaRepository.findById(it.id).let { media -> mediaRepository.update(media.copy(status = Media.Status.READY, mediaType = MediaType.EPUB.type)) } }
    mediaRepository.findById(bookNotReady.id).let { media -> mediaRepository.update(media.copy(status = Media.Status.ERROR)) }
    mediaRepository.findById(bookNotEpub.id).let { media -> mediaRepository.update(media.copy(mediaType = MediaType.ZIP.type)) }

    // when
    val syncPoint = syncPointLifecycle.createSyncPoint(user1, null, listOf(library1.id))
    val syncPointBooks = syncPointRepository.findBooksById(syncPoint.id, false, Pageable.unpaged())

    // then
    assertThat(syncPoint.userId).isEqualTo(user1.id)
    assertThat(syncPointBooks).hasSize(1)
    with(syncPointBooks.first()) {
      assertThat(this.bookId).isEqualTo(bookValid.id)
      assertThat(this.fileHash).isEqualTo(bookValid.fileHash)
      assertThat(this.fileSize).isEqualTo(bookValid.fileSize)
      assertThat(this.fileLastModified).isCloseTo(bookValid.fileLastModified.toZonedDateTime(), within(1, ChronoUnit.SECONDS))
    }
  }

  @Test
  fun `given syncpoint when adding new books then syncpoint diff contains new books`() {
    // given
    val book1 = makeBook("valid", libraryId = library1.id)

    val series =
      makeSeries(name = "series1", libraryId = library1.id).also { series ->
        seriesLifecycle.createSeries(series).let { created ->
          seriesLifecycle.addBooks(created, listOf(book1))
        }
      }

    mediaRepository.findById(book1.id).let { media -> mediaRepository.update(media.copy(status = Media.Status.READY, mediaType = MediaType.EPUB.type)) }

    val syncPoint1 = syncPointLifecycle.createSyncPoint(user1, null, listOf(library1.id))

    // when
    val book2 = makeBook("valid", libraryId = library1.id)
    val book3 = makeBook("valid", libraryId = library1.id)
    seriesLifecycle.addBooks(series, listOf(book2, book3))
    mediaRepository.findById(book2.id).let { media -> mediaRepository.update(media.copy(status = Media.Status.READY, mediaType = MediaType.EPUB.type)) }
    mediaRepository.findById(book3.id).let { media -> mediaRepository.update(media.copy(status = Media.Status.READY, mediaType = MediaType.EPUB.type)) }

    val syncPoint2 = syncPointLifecycle.createSyncPoint(user1, null, listOf(library1.id))
    val booksAdded = syncPointRepository.findBooksAdded(syncPoint1.id, syncPoint2.id, false, Pageable.unpaged())
    val page1 = syncPointLifecycle.takeBooksAdded(syncPoint1.id, syncPoint2.id, Pageable.ofSize(1))
    val page2 = syncPointLifecycle.takeBooksAdded(syncPoint1.id, syncPoint2.id, Pageable.ofSize(1))

    // then
    assertThat(booksAdded).hasSize(2)
    assertThat(booksAdded.map { it.bookId }).containsExactlyInAnyOrder(book2.id, book3.id)
    assertThat(page1).hasSize(1)
    assertThat(page1.map { it.bookId })
      .containsAnyElementsOf(listOf(book2.id, book3.id))
      .doesNotContainAnyElementsOf(page2.map { it.bookId })
    assertThat(page2).hasSize(1)
    assertThat(page2.map { it.bookId })
      .containsAnyElementsOf(listOf(book2.id, book3.id))
      .doesNotContainAnyElementsOf(page1.map { it.bookId })
  }

  @Test
  fun `given syncpoint when deleting books then syncpoint diff contains removed books`() {
    // given
    val book1 = makeBook("valid1", libraryId = library1.id)
    val book2 = makeBook("valid2", libraryId = library1.id)
    val book3 = makeBook("valid3", libraryId = library1.id)

    val series =
      makeSeries(name = "series1", libraryId = library1.id).also { series ->
        seriesLifecycle.createSeries(series).let { created ->
          seriesLifecycle.addBooks(created, listOf(book1, book2, book3))
        }
      }

    bookRepository.findAll().forEach { mediaRepository.findById(it.id).let { media -> mediaRepository.update(media.copy(status = Media.Status.READY, mediaType = MediaType.EPUB.type)) } }

    val syncPoint1 = syncPointLifecycle.createSyncPoint(user1, null, listOf(library1.id))

    // when
    bookLifecycle.softDeleteMany(listOf(bookRepository.findByIdOrNull(book2.id)!!))
    bookLifecycle.deleteOne(bookRepository.findByIdOrNull(book3.id)!!)

    val syncPoint2 = syncPointLifecycle.createSyncPoint(user1, null, listOf(library1.id))
    val booksRemoved = syncPointRepository.findBooksRemoved(syncPoint1.id, syncPoint2.id, false, Pageable.unpaged())
    val page1 = syncPointLifecycle.takeBooksRemoved(syncPoint1.id, syncPoint2.id, Pageable.ofSize(1))
    val page2 = syncPointLifecycle.takeBooksRemoved(syncPoint1.id, syncPoint2.id, Pageable.ofSize(1))

    // then
    assertThat(booksRemoved).hasSize(2)
    assertThat(booksRemoved.map { it.bookId }).containsExactlyInAnyOrder(book2.id, book3.id)
    assertThat(page1).hasSize(1)
    assertThat(page1.map { it.bookId })
      .containsAnyElementsOf(listOf(book2.id, book3.id))
      .doesNotContainAnyElementsOf(page2.map { it.bookId })
    assertThat(page2).hasSize(1)
    assertThat(page2.map { it.bookId })
      .containsAnyElementsOf(listOf(book2.id, book3.id))
      .doesNotContainAnyElementsOf(page1.map { it.bookId })
  }

  @Test
  fun `given syncpoint when changing books then syncpoint diff contains changed books`() {
    // given
    val book1 = makeBook("valid1", libraryId = library1.id)
    val book2 = makeBook("valid2", libraryId = library1.id).copy(fileSize = 1, fileHash = "hash1")
    val book3 = makeBook("valid3", libraryId = library1.id)
    val book4 = makeBook("no hash to hash", libraryId = library1.id)

    val series =
      makeSeries(name = "series1", libraryId = library1.id).also { series ->
        seriesLifecycle.createSeries(series).let { created ->
          seriesLifecycle.addBooks(created, listOf(book1, book2, book3))
        }
      }

    bookRepository.findAll().forEach { mediaRepository.findById(it.id).let { media -> mediaRepository.update(media.copy(status = Media.Status.READY, mediaType = MediaType.EPUB.type)) } }

    val syncPoint1 = syncPointLifecycle.createSyncPoint(user1, null, listOf(library1.id))

    // when
    bookRepository.findByIdOrNull(book1.id)?.let { bookRepository.update(it.copy(fileLastModified = LocalDateTime.of(2020, 1, 1, 1, 1))) }
    bookRepository.findByIdOrNull(book2.id)?.let { bookRepository.update(it.copy(fileHash = "hash2")) }
    bookMetadataRepository.findById(book3.id).let { bookMetadataRepository.update(it.copy(title = "changed")) }
    bookRepository.findByIdOrNull(book4.id)?.let { bookRepository.update(it.copy(fileHash = "hash")) } // not included in changed books if it had no hash before

    val syncPoint2 = syncPointLifecycle.createSyncPoint(user1, null, listOf(library1.id))
    val booksChanged = syncPointRepository.findBooksChanged(syncPoint1.id, syncPoint2.id, false, Pageable.unpaged())
    val page1 = syncPointLifecycle.takeBooksChanged(syncPoint1.id, syncPoint2.id, PageRequest.ofSize(1))
    val page2 = syncPointLifecycle.takeBooksChanged(syncPoint1.id, syncPoint2.id, PageRequest.ofSize(1))
    val page3 = syncPointLifecycle.takeBooksChanged(syncPoint1.id, syncPoint2.id, PageRequest.ofSize(1))

    // then
    assertThat(booksChanged).hasSize(3)
    assertThat(booksChanged.map { it.bookId }).containsExactlyInAnyOrder(book1.id, book2.id, book3.id)
    assertThat(page1).hasSize(1)
    assertThat(page1.map { it.bookId })
      .containsAnyElementsOf(listOf(book1.id, book2.id, book3.id))
      .doesNotContainAnyElementsOf((page2 + page3).map { it.bookId })
    assertThat(page2).hasSize(1)
    assertThat(page2.map { it.bookId })
      .containsAnyElementsOf(listOf(book1.id, book2.id, book3.id))
      .doesNotContainAnyElementsOf((page1 + page3).map { it.bookId })
    assertThat(page3).hasSize(1)
    assertThat(page3.map { it.bookId })
      .containsAnyElementsOf(listOf(book1.id, book2.id, book3.id))
      .doesNotContainAnyElementsOf((page1 + page2).map { it.bookId })
  }
}
