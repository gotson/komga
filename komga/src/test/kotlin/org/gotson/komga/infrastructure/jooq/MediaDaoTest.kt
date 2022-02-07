package org.gotson.komga.infrastructure.jooq

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.catchThrowable
import org.gotson.komga.domain.model.BookPage
import org.gotson.komga.domain.model.Dimension
import org.gotson.komga.domain.model.Media
import org.gotson.komga.domain.model.MediaType
import org.gotson.komga.domain.model.makeBook
import org.gotson.komga.domain.model.makeLibrary
import org.gotson.komga.domain.model.makeSeries
import org.gotson.komga.domain.persistence.BookRepository
import org.gotson.komga.domain.persistence.LibraryRepository
import org.gotson.komga.domain.persistence.SeriesRepository
import org.gotson.komga.infrastructure.configuration.KomgaProperties
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.junit.jupiter.SpringExtension
import java.time.LocalDateTime

@ExtendWith(SpringExtension::class)
@SpringBootTest
class MediaDaoTest(
  @Autowired private val mediaDao: MediaDao,
  @Autowired private val bookRepository: BookRepository,
  @Autowired private val seriesRepository: SeriesRepository,
  @Autowired private val libraryRepository: LibraryRepository,
  @Autowired private val komgaProperties: KomgaProperties,
) {
  private val library = makeLibrary()
  private val series = makeSeries("Series", libraryId = library.id)
  private val book = makeBook("Book", libraryId = library.id, seriesId = series.id)

  @BeforeAll
  fun setup() {
    libraryRepository.insert(library)

    seriesRepository.insert(series)

    bookRepository.insert(book)
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
      pages = listOf(
        BookPage(
          fileName = "1.jpg",
          mediaType = "image/jpeg",
          dimension = Dimension(10, 10),
          fileHash = "hashed",
          fileSize = 10,
        ),
      ),
      files = listOf("ComicInfo.xml"),
      comment = "comment",
      bookId = book.id,
    )

    mediaDao.insert(media)
    val created = mediaDao.findById(media.bookId)

    assertThat(created.bookId).isEqualTo(book.id)
    assertThat(created.createdDate).isCloseTo(now, offset)
    assertThat(created.lastModifiedDate).isCloseTo(now, offset)
    assertThat(created.status).isEqualTo(media.status)
    assertThat(created.mediaType).isEqualTo(media.mediaType)
    assertThat(created.comment).isEqualTo(media.comment)
    assertThat(created.pages).hasSize(1)
    with(created.pages.first()) {
      assertThat(fileName).isEqualTo(media.pages.first().fileName)
      assertThat(mediaType).isEqualTo(media.pages.first().mediaType)
      assertThat(dimension).isEqualTo(media.pages.first().dimension)
      assertThat(fileHash).isEqualTo(media.pages.first().fileHash)
      assertThat(fileSize).isEqualTo(media.pages.first().fileSize)
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
    assertThat(created.comment).isNull()
    assertThat(created.pages).isEmpty()
    assertThat(created.files).isEmpty()
  }

  @Test
  fun `given existing media when updating then it is persisted`() {
    val media = Media(
      status = Media.Status.READY,
      mediaType = "application/zip",
      pages = listOf(
        BookPage(
          fileName = "1.jpg",
          mediaType = "image/jpeg",
        ),
      ),
      files = listOf("ComicInfo.xml"),
      comment = "comment",
      bookId = book.id,
    )
    mediaDao.insert(media)

    val modificationDate = LocalDateTime.now()

    val updated = with(mediaDao.findById(media.bookId)) {
      copy(
        status = Media.Status.ERROR,
        mediaType = "application/rar",
        pages = listOf(
          BookPage(
            fileName = "2.png",
            mediaType = "image/png",
            dimension = Dimension(10, 10),
            fileHash = "hashed",
            fileSize = 10,
          ),
        ),
        files = listOf("id.txt"),
        comment = "comment2",
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
    assertThat(modified.comment).isEqualTo(updated.comment)
    assertThat(modified.pages.first().fileName).isEqualTo(updated.pages.first().fileName)
    assertThat(modified.pages.first().mediaType).isEqualTo(updated.pages.first().mediaType)
    assertThat(modified.pages.first().dimension).isEqualTo(updated.pages.first().dimension)
    assertThat(modified.pages.first().fileHash).isEqualTo(updated.pages.first().fileHash)
    assertThat(modified.pages.first().fileSize).isEqualTo(updated.pages.first().fileSize)
    assertThat(modified.files.first()).isEqualTo(updated.files.first())
  }

  @Test
  fun `given existing media when finding by id then media is returned`() {
    val media = Media(
      status = Media.Status.READY,
      mediaType = "application/zip",
      pages = listOf(
        BookPage(
          fileName = "1.jpg",
          mediaType = "image/jpeg",
        ),
      ),
      files = listOf("ComicInfo.xml"),
      comment = "comment",
      bookId = book.id,
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

  @Nested
  inner class MissingPageHash {

    @Test
    fun `given media with single page not hashed when finding for missing page hash then it is returned`() {
      val media = Media(
        status = Media.Status.READY,
        pages = listOf(
          BookPage(
            fileName = "1.jpg",
            mediaType = "image/jpeg",
          ),
        ),
        mediaType = MediaType.ZIP.value,
        bookId = book.id,
      )
      mediaDao.insert(media)

      val found = mediaDao.findAllBookAndSeriesIdsByLibraryIdAndMediaTypeAndWithMissingPageHash(book.libraryId, listOf(MediaType.ZIP.value), komgaProperties.pageHashing)

      assertThat(found)
        .hasSize(1)
        .containsOnly(Pair(book.id, book.seriesId))
    }

    @Test
    fun `given non-convertible media not hashed when finding for missing page hash then it is returned`() {
      val media = Media(
        status = Media.Status.READY,
        pages = listOf(
          BookPage(
            fileName = "1.jpg",
            mediaType = "image/jpeg",
          ),
        ),
        mediaType = MediaType.RAR_4.value,
        bookId = book.id,
      )
      mediaDao.insert(media)

      val found = mediaDao.findAllBookAndSeriesIdsByLibraryIdAndMediaTypeAndWithMissingPageHash(book.libraryId, listOf(MediaType.ZIP.value), komgaProperties.pageHashing)

      assertThat(found).isEmpty()
    }

    @Test
    fun `given media with no pages hashed when finding for missing page hash then it is not returned`() {
      val media = Media(
        status = Media.Status.READY,
        pages = (1..12).map {
          BookPage(
            fileName = "$it.jpg",
            mediaType = "image/jpeg",
          )
        },
        mediaType = MediaType.ZIP.value,
        bookId = book.id,
      )
      mediaDao.insert(media)

      val found = mediaDao.findAllBookAndSeriesIdsByLibraryIdAndMediaTypeAndWithMissingPageHash(book.libraryId, listOf(MediaType.ZIP.value), komgaProperties.pageHashing)

      assertThat(found)
        .hasSize(1)
        .containsOnly(Pair(book.id, book.seriesId))
    }

    @Test
    fun `given media with single page hashed when finding for missing page hash then it is not returned`() {
      val media = Media(
        status = Media.Status.READY,
        pages = listOf(
          BookPage(
            fileName = "1.jpg",
            mediaType = "image/jpeg",
            fileHash = "hashed",
          ),
        ),
        mediaType = MediaType.ZIP.value,
        bookId = book.id,
      )
      mediaDao.insert(media)

      val found = mediaDao.findAllBookAndSeriesIdsByLibraryIdAndMediaTypeAndWithMissingPageHash(book.libraryId, listOf(MediaType.ZIP.value), komgaProperties.pageHashing)

      assertThat(found).isEmpty()
    }

    @Test
    fun `given media with required pages hashed when finding for missing page hash then it is not returned`() {
      val media = Media(
        status = Media.Status.READY,
        pages = (1..12).map {
          BookPage(
            fileName = "$it.jpg",
            mediaType = "image/jpeg",
            fileHash = if (it <= 3 || it >= 9) "hashed" else "",
          )
        },
        mediaType = MediaType.ZIP.value,
        bookId = book.id,
      )
      mediaDao.insert(media)

      val found = mediaDao.findAllBookAndSeriesIdsByLibraryIdAndMediaTypeAndWithMissingPageHash(book.libraryId, listOf(MediaType.ZIP.value), komgaProperties.pageHashing)

      assertThat(found).isEmpty()
    }

    @Test
    fun `given media with more pages hashed than required when finding for missing page hash then it is not returned`() {
      val media = Media(
        status = Media.Status.READY,
        pages = (1..12).map {
          BookPage(
            fileName = "$it.jpg",
            mediaType = "image/jpeg",
            fileHash = "hashed",
          )
        },
        mediaType = MediaType.ZIP.value,
        bookId = book.id,
      )
      mediaDao.insert(media)

      val found = mediaDao.findAllBookAndSeriesIdsByLibraryIdAndMediaTypeAndWithMissingPageHash(book.libraryId, listOf(MediaType.ZIP.value), komgaProperties.pageHashing)

      assertThat(found).isEmpty()
    }
  }
}
