package org.gotson.komga.infrastructure.jooq

import org.assertj.core.api.Assertions.assertThat
import org.gotson.komga.domain.model.BookPage
import org.gotson.komga.domain.model.Media
import org.gotson.komga.domain.model.PageHashKnown
import org.gotson.komga.domain.model.makeBook
import org.gotson.komga.domain.model.makeLibrary
import org.gotson.komga.domain.model.makeSeries
import org.gotson.komga.domain.persistence.BookRepository
import org.gotson.komga.domain.persistence.LibraryRepository
import org.gotson.komga.domain.persistence.SeriesRepository
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.domain.Pageable
import org.springframework.test.context.junit.jupiter.SpringExtension
import java.time.LocalDateTime

@ExtendWith(SpringExtension::class)
@SpringBootTest
class PageHashDaoTest(
  @Autowired private val pageHashDao: PageHashDao,
  @Autowired private val mediaDao: MediaDao,
  @Autowired private val bookRepository: BookRepository,
  @Autowired private val seriesRepository: SeriesRepository,
  @Autowired private val libraryRepository: LibraryRepository,
) {
  private val library = makeLibrary()
  private val series = makeSeries("Series", libraryId = library.id)
  private val books = listOf(
    makeBook("Book", libraryId = library.id, seriesId = series.id),
    makeBook("Book2", libraryId = library.id, seriesId = series.id),
  )

  @BeforeAll
  fun setup() {
    libraryRepository.insert(library)
    seriesRepository.insert(series)
    bookRepository.insert(books)
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

  @Nested
  inner class Known {
    @Test
    fun `given a known page hash when inserting then it is persisted`() {
      val now = LocalDateTime.now()
      val pageHash = PageHashKnown(
        hash = "hashed",
        mediaType = "image/jpeg",
        size = 10,
        action = PageHashKnown.Action.IGNORE,
      )

      pageHashDao.insert(pageHash, null)
      val known = pageHashDao.findKnown(pageHash)!!

      assertThat(known.hash).isEqualTo(pageHash.hash)
      assertThat(known.mediaType).isEqualTo(pageHash.mediaType)
      assertThat(known.size).isEqualTo(pageHash.size)
      assertThat(known.action).isEqualTo(pageHash.action)
      assertThat(known.createdDate).isCloseTo(now, offset)
      assertThat(known.lastModifiedDate).isCloseTo(now, offset)
    }
  }

  @Nested
  inner class Unknown {
    @Test
    fun `given known hashes when finding unknown then known are not included`() {
      // given
      pageHashDao.insert(
        PageHashKnown(
          hash = "hash-1",
          mediaType = "image/jpeg",
          size = 1,
          action = PageHashKnown.Action.IGNORE,
        ),
        null,
      )

      pageHashDao.insert(
        PageHashKnown(
          hash = "hash-2",
          mediaType = "image/jpeg",
          size = null,
          action = PageHashKnown.Action.IGNORE,
        ),
        null,
      )

      val media = Media(
        status = Media.Status.READY,
        mediaType = "application/zip",
        pages = (1..10).map {
          BookPage(
            fileName = "$it.jpg",
            mediaType = "image/jpeg",
            fileHash = "hash-$it",
            fileSize = it.toLong(),
          )
        },
        files = listOf("ComicInfo.xml"),
        comment = "comment",
        bookId = books.first().id,
      )
      mediaDao.insert(media)
      mediaDao.insert(media.copy(bookId = books.last().id))

      // when
      val unknown = pageHashDao.findAllUnknown(Pageable.unpaged())

      // then
      assertThat(unknown).hasSize(9)
      assertThat(unknown.map { it.hash })
        .doesNotContain("hash-1")
        .containsExactlyInAnyOrderElementsOf((2..10).map { "hash-$it" })
    }
  }
}
