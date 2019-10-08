package org.gotson.komga.domain.persistence

import org.assertj.core.api.Assertions.assertThat
import org.gotson.komga.domain.model.BookMetadata
import org.gotson.komga.domain.model.Status
import org.gotson.komga.domain.model.makeBook
import org.gotson.komga.domain.model.makeLibrary
import org.gotson.komga.domain.model.makeSeries
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.data.domain.PageRequest
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.transaction.annotation.Transactional

@ExtendWith(SpringExtension::class)
@DataJpaTest
@Transactional
class BookRepositoryTest(
    @Autowired private val seriesRepository: SeriesRepository,
    @Autowired private val bookRepository: BookRepository,
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
  fun `given many books with unordered index when fetching then books are ordered and paged`() {
    val series = makeSeries(
        name = "series",
        books = (1..100 step 2).map { makeBook("$it") }
    ).also { it.library = library }
    seriesRepository.save(series)

    series.books = series.books.toMutableList().also { it.add(makeBook("2")) }
    seriesRepository.save(series)

    val pageable = PageRequest.of(0, 20)
    val pageOfBooks = bookRepository.findAllBySeriesId(series.id, pageable)

    assertThat(pageOfBooks.isFirst).isTrue()
    assertThat(pageOfBooks.isLast).isFalse()
    assertThat(pageOfBooks.size).isEqualTo(20)
    assertThat(pageOfBooks.number).isEqualTo(0)
    assertThat(pageOfBooks.content).hasSize(20)
    assertThat(pageOfBooks.content.map { it.name }).startsWith("1", "2", "3", "5")
  }

  @Test
  fun `given many books in ready state with unordered index when fetching then books are ordered and paged`() {
    val series = makeSeries(
        name = "series",
        books = (1..100 step 2).map { makeBook("$it") }
    ).also { it.library = library }
    seriesRepository.save(series)

    series.books = series.books.toMutableList().also { it.add(makeBook("2")) }
    series.books.forEach { it.metadata = BookMetadata(Status.READY) }
    seriesRepository.save(series)

    val pageable = PageRequest.of(0, 20)
    val pageOfBooks = bookRepository.findAllBySeriesId(series.id, pageable)

    assertThat(pageOfBooks.isFirst).isTrue()
    assertThat(pageOfBooks.isLast).isFalse()
    assertThat(pageOfBooks.size).isEqualTo(20)
    assertThat(pageOfBooks.number).isEqualTo(0)
    assertThat(pageOfBooks.content).hasSize(20)
    assertThat(pageOfBooks.content.map { it.name }).startsWith("1", "2", "3", "5")
  }
}
