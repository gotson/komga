package org.gotson.komga.infrastructure.jooq

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.catchThrowable
import org.gotson.komga.domain.model.BookPage
import org.gotson.komga.domain.model.Media
import org.gotson.komga.domain.model.makeBook
import org.gotson.komga.domain.model.makeLibrary
import org.gotson.komga.domain.model.makeSeries
import org.gotson.komga.domain.persistence.BookRepository
import org.gotson.komga.domain.persistence.LibraryRepository
import org.gotson.komga.domain.persistence.SeriesRepository
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.junit.jupiter.SpringExtension
import java.time.LocalDateTime
import kotlin.random.Random

@ExtendWith(SpringExtension::class)
@SpringBootTest
class MediaDaoTest(
  @Autowired private val mediaDao: MediaDao,
  @Autowired private val bookRepository: BookRepository,
  @Autowired private val seriesRepository: SeriesRepository,
  @Autowired private val libraryRepository: LibraryRepository
) {
  private val library = makeLibrary()
  private val series = makeSeries("Series")
  private val book = makeBook("Book")

  @BeforeAll
  fun setup() {
    libraryRepository.insert(library)

    seriesRepository.insert(series.copy(libraryId = library.id))

    bookRepository.insert(book.copy(libraryId = library.id, seriesId = series.id))
  }

  @AfterEach
  fun deleteMedia() {
    bookRepository.findAll().forEach {
      mediaDao.delete(it.id)
    }
  }

  @AfterAll
  fun tearDown() {
    bookRepository.deleteAll()
    seriesRepository.deleteAll()
    libraryRepository.deleteAll()
  }

  @Test
  fun `given a media when inserting then it is persisted`() {
    val now = LocalDateTime.now()
    val media = Media(
      status = Media.Status.READY,
      mediaType = "application/zip",
      thumbnail = Random.nextBytes(1),
      pages = listOf(BookPage(
        fileName = "1.jpg",
        mediaType = "image/jpeg"
      )),
      files = listOf("ComicInfo.xml"),
      comment = "comment",
      bookId = book.id
    )

    mediaDao.insert(media)
    val created = mediaDao.findById(media.bookId)

    assertThat(created.bookId).isEqualTo(book.id)
    assertThat(created.createdDate).isCloseTo(now, offset)
    assertThat(created.lastModifiedDate).isCloseTo(now, offset)
    assertThat(created.status).isEqualTo(media.status)
    assertThat(created.mediaType).isEqualTo(media.mediaType)
    assertThat(created.thumbnail).isEqualTo(media.thumbnail)
    assertThat(created.comment).isEqualTo(media.comment)
    assertThat(created.pages).hasSize(1)
    with(created.pages.first()) {
      assertThat(fileName).isEqualTo(media.pages.first().fileName)
      assertThat(mediaType).isEqualTo(media.pages.first().mediaType)
    }
    assertThat(created.files).hasSize(1)
    assertThat(created.files.first()).isEqualTo(media.files.first())
  }

  @Test
  fun `given a minimum media when inserting then it is persisted`() {
    val media = Media(bookId = book.id)

    mediaDao.insert(media)
    val created = mediaDao.findById(media.bookId)

    assertThat(created.bookId).isEqualTo(book.id)
    assertThat(created.status).isEqualTo(Media.Status.UNKNOWN)
    assertThat(created.mediaType).isNull()
    assertThat(created.thumbnail).isNull()
    assertThat(created.comment).isNull()
    assertThat(created.pages).isEmpty()
    assertThat(created.files).isEmpty()
  }

  @Test
  fun `given existing media when updating then it is persisted`() {
    val media = Media(
      status = Media.Status.READY,
      mediaType = "application/zip",
      thumbnail = Random.nextBytes(1),
      pages = listOf(BookPage(
        fileName = "1.jpg",
        mediaType = "image/jpeg"
      )),
      files = listOf("ComicInfo.xml"),
      comment = "comment",
      bookId = book.id
    )
    mediaDao.insert(media)

    val modificationDate = LocalDateTime.now()

    val updated = with(mediaDao.findById(media.bookId)) {
      copy(
        status = Media.Status.ERROR,
        mediaType = "application/rar",
        thumbnail = Random.nextBytes(1),
        pages = listOf(BookPage(
          fileName = "2.png",
          mediaType = "image/png"
        )),
        files = listOf("id.txt"),
        comment = "comment2"
      )
    }

    mediaDao.update(updated)
    val modified = mediaDao.findById(updated.bookId)

    assertThat(modified.bookId).isEqualTo(updated.bookId)
    assertThat(modified.createdDate).isEqualTo(updated.createdDate)
    assertThat(modified.lastModifiedDate)
      .isCloseTo(modificationDate, offset)
      .isNotEqualTo(updated.lastModifiedDate)
    assertThat(modified.status).isEqualTo(updated.status)
    assertThat(modified.mediaType).isEqualTo(updated.mediaType)
    assertThat(modified.thumbnail).isEqualTo(updated.thumbnail)
    assertThat(modified.comment).isEqualTo(updated.comment)
    assertThat(modified.pages.first().fileName).isEqualTo(updated.pages.first().fileName)
    assertThat(modified.pages.first().mediaType).isEqualTo(updated.pages.first().mediaType)
    assertThat(modified.files.first()).isEqualTo(updated.files.first())
  }

  @Test
  fun `given existing media when finding by id then media is returned`() {
    val media = Media(
      status = Media.Status.READY,
      mediaType = "application/zip",
      thumbnail = Random.nextBytes(1),
      pages = listOf(BookPage(
        fileName = "1.jpg",
        mediaType = "image/jpeg"
      )),
      files = listOf("ComicInfo.xml"),
      comment = "comment",
      bookId = book.id
    )
    mediaDao.insert(media)

    val found = catchThrowable { mediaDao.findById(media.bookId) }

    assertThat(found).doesNotThrowAnyException()
  }

  @Test
  fun `given non-existing media when finding by id then exception is thrown`() {
    val found = catchThrowable { mediaDao.findById("128742") }

    assertThat(found).isInstanceOf(Exception::class.java)
  }
}
