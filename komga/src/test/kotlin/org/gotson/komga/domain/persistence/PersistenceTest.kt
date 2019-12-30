package org.gotson.komga.domain.persistence

import org.assertj.core.api.Assertions.assertThat
import org.gotson.komga.domain.model.Media
import org.gotson.komga.domain.model.makeBook
import org.gotson.komga.domain.model.makeBookPage
import org.gotson.komga.domain.model.makeLibrary
import org.gotson.komga.domain.model.makeSeries
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.transaction.annotation.Transactional

@ExtendWith(SpringExtension::class)
@DataJpaTest
@Transactional
class PersistenceTest(
    @Autowired private val seriesRepository: SeriesRepository,
    @Autowired private val bookRepository: BookRepository,
    @Autowired private val mediaRepository: MediaRepository,
    @Autowired private val libraryRepository: LibraryRepository
) {

  private val library = makeLibrary()

  @BeforeAll
  fun `setup library`() {
    libraryRepository.save(library)
  }

  @AfterAll
  fun `teardown library`() {
    libraryRepository.deleteAll()
  }

  @AfterEach
  fun `clear repository`() {
    seriesRepository.deleteAll()
  }

  @Test
  fun `given series with book when saving then media is also saved`() {
    // given
    val series = makeSeries(name = "series", books = listOf(makeBook("book1"))).also { it.library = library }

    // when
    seriesRepository.save(series)

    // then
    assertThat(seriesRepository.count()).isEqualTo(1)
    assertThat(bookRepository.count()).isEqualTo(1)
    assertThat(mediaRepository.count()).isEqualTo(1)
  }

  @Test
  fun `given series with unordered books when saving then books are ordered with natural sort`() {
    // given
    val series = makeSeries(name = "series", books = listOf(
        makeBook("book 1"),
        makeBook("book 05"),
        makeBook("book 6"),
        makeBook("book 002")
    )).also { it.library = library }

    // when
    seriesRepository.save(series)

    // then
    assertThat(seriesRepository.count()).isEqualTo(1)
    assertThat(bookRepository.count()).isEqualTo(4)
    assertThat(seriesRepository.findAll().first().books.map { it.name })
        .containsExactly("book 1", "book 002", "book 05", "book 6")
  }

  @Test
  fun `given existing book when updating media then new media is saved`() {
    // given
    val series = makeSeries(name = "series", books = listOf(makeBook("book1"))).also { it.library = library }
    seriesRepository.save(series)

    // when
    val book = bookRepository.findAll().first()
    book.media = Media(status = Media.Status.READY, mediaType = "test", pages = mutableListOf(makeBookPage("page1")))

    bookRepository.save(book)

    // then
    assertThat(seriesRepository.count()).isEqualTo(1)
    assertThat(bookRepository.count()).isEqualTo(1)
    assertThat(mediaRepository.count()).isEqualTo(1)
    mediaRepository.findAll().first().let {
      assertThat(it.status == Media.Status.READY)
      assertThat(it.mediaType == "test")
      assertThat(it.pages).hasSize(1)
      assertThat(it.pages.first().fileName).isEqualTo("page1")
    }
  }

  @Test
  fun `given book pages unordered when saving then pages are ordered with natural sort`() {
    // given
    val series = makeSeries(name = "series", books = listOf(makeBook("book1"))).also { it.library = library }
    seriesRepository.save(series)

    // when
    val book = bookRepository.findAll().first()
    book.media = Media(status = Media.Status.READY, mediaType = "test", pages = listOf(
        makeBookPage("2"),
        makeBookPage("003"),
        makeBookPage("001")
    ))

    bookRepository.save(book)

    // then
    assertThat(seriesRepository.count()).isEqualTo(1)
    assertThat(bookRepository.count()).isEqualTo(1)
    assertThat(mediaRepository.count()).isEqualTo(1)
    mediaRepository.findAll().first().let { media ->
      assertThat(media.status == Media.Status.READY)
      assertThat(media.mediaType == "test")
      assertThat(media.pages).hasSize(3)
      assertThat(media.pages.map { it.fileName }).containsExactly("001", "2", "003")
    }
  }
}
