package org.gotson.komga.domain.service

import org.assertj.core.api.Assertions.assertThat
import org.gotson.komga.domain.model.makeBook
import org.gotson.komga.domain.model.makeLibrary
import org.gotson.komga.domain.model.makeSeries
import org.gotson.komga.domain.persistence.BookRepository
import org.gotson.komga.domain.persistence.LibraryRepository
import org.gotson.komga.domain.persistence.SeriesMetadataRepository
import org.gotson.komga.domain.persistence.SeriesRepository
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.junit.jupiter.SpringExtension

@ExtendWith(SpringExtension::class)
@SpringBootTest
class SeriesLifecycleTest(
  @Autowired private val seriesLifecycle: SeriesLifecycle,
  @Autowired private val bookLifecycle: BookLifecycle,
  @Autowired private val seriesRepository: SeriesRepository,
  @Autowired private val seriesMetadataRepository: SeriesMetadataRepository,
  @Autowired private val bookRepository: BookRepository,
  @Autowired private val libraryRepository: LibraryRepository
) {

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
    seriesLifecycle.deleteMany(seriesRepository.findAll().map { it.id })
  }

  @Test
  fun `given series with unordered books when saving then books are ordered with natural sort`() {
    // given
    val books = listOf(
      makeBook("book 1", libraryId = library.id),
      makeBook("book 05", libraryId = library.id),
      makeBook("book 6", libraryId = library.id),
      makeBook("book 002", libraryId = library.id)
    )
    val createdSeries = makeSeries(name = "series", libraryId = library.id).let {
      seriesLifecycle.createSeries(it)
    }
    seriesLifecycle.addBooks(createdSeries, books)

    // when
    seriesLifecycle.sortBooks(createdSeries)

    // then
    assertThat(seriesRepository.count()).isEqualTo(1)
    assertThat(bookRepository.count()).isEqualTo(4)

    val savedBooks = bookRepository.findBySeriesId(createdSeries.id).sortedBy { it.number }
    assertThat(savedBooks.map { it.name }).containsExactly("book 1", "book 002", "book 05", "book 6")
    assertThat(savedBooks.map { it.number }).containsExactly(1, 2, 3, 4)
  }

  @Test
  fun `given series when removing a book then remaining books are indexed in sequence`() {
    // given
    val books = listOf(
      makeBook("book 1", libraryId = library.id),
      makeBook("book 2", libraryId = library.id),
      makeBook("book 3", libraryId = library.id),
      makeBook("book 4", libraryId = library.id)
    )
    val createdSeries = makeSeries(name = "series", libraryId = library.id).let {
      seriesLifecycle.createSeries(it)
    }
    seriesLifecycle.addBooks(createdSeries, books)
    seriesLifecycle.sortBooks(createdSeries)

    // when
    val book = bookRepository.findBySeriesId(createdSeries.id).first { it.name == "book 2" }
    bookLifecycle.deleteOne(book.id)
    seriesLifecycle.sortBooks(createdSeries)

    // then
    assertThat(seriesRepository.count()).isEqualTo(1)
    assertThat(bookRepository.count()).isEqualTo(3)

    val savedBooks = bookRepository.findBySeriesId(createdSeries.id).sortedBy { it.number }
    assertThat(savedBooks.map { it.name }).containsExactly("book 1", "book 3", "book 4")
    assertThat(savedBooks.map { it.number }).containsExactly(1, 2, 3)
  }

  @Test
  fun `given series when adding a book then all books are indexed in sequence`() {
    // given
    val books = listOf(
      makeBook("book 1", libraryId = library.id),
      makeBook("book 2", libraryId = library.id),
      makeBook("book 4", libraryId = library.id),
      makeBook("book 5", libraryId = library.id)
    )
    val createdSeries = makeSeries(name = "series", libraryId = library.id).let {
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

    val savedBooks = bookRepository.findBySeriesId(createdSeries.id).sortedBy { it.number }
    assertThat(savedBooks.map { it.name }).containsExactly("book 1", "book 2", "book 3", "book 4", "book 5")
    assertThat(savedBooks.map { it.number }).containsExactly(1, 2, 3, 4, 5)
  }

  @Test
  fun `given series name with diacritics when creating series then diacritics are stripped from metadata titlesort`() {
    // given
    val series1 = makeSeries("À l'assaut", library.id)
    val series2 = makeSeries("Être ou ne pas être", library.id)
    val series3 = makeSeries("Écarlate", library.id)

    // when
    val created1 = seriesLifecycle.createSeries(series1)
    val created2 = seriesLifecycle.createSeries(series2)
    val created3 = seriesLifecycle.createSeries(series3)

    // then
    with(seriesMetadataRepository.findById(created1.id)) {
      assertThat(title).isEqualTo(series1.name)
      assertThat(titleSort).isEqualTo("A l'assaut")
    }
    with(seriesMetadataRepository.findById(created2.id)) {
      assertThat(title).isEqualTo(series2.name)
      assertThat(titleSort).isEqualTo("Etre ou ne pas etre")
    }
    with(seriesMetadataRepository.findById(created3.id)) {
      assertThat(title).isEqualTo(series3.name)
      assertThat(titleSort).isEqualTo("Ecarlate")
    }
  }
}
