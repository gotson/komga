package org.gotson.komga.infrastructure.jooq.main

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.catchThrowable
import org.gotson.komga.domain.model.Author
import org.gotson.komga.domain.model.BookMetadataAggregation
import org.gotson.komga.domain.model.makeLibrary
import org.gotson.komga.domain.model.makeSeries
import org.gotson.komga.domain.persistence.LibraryRepository
import org.gotson.komga.domain.persistence.SeriesRepository
import org.gotson.komga.infrastructure.jooq.offset
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import java.time.LocalDate
import java.time.LocalDateTime

@SpringBootTest
class BookMetadataAggregationDaoTest(
  @Autowired private val bookMetadataAggregationDao: BookMetadataAggregationDao,
  @Autowired private val seriesRepository: SeriesRepository,
  @Autowired private val libraryRepository: LibraryRepository,
) {
  private val library = makeLibrary()

  @BeforeAll
  fun setup() {
    libraryRepository.insert(library)
  }

  @AfterEach
  fun deleteSeries() {
    seriesRepository.findAll().forEach {
      bookMetadataAggregationDao.delete(it.id)
    }
    seriesRepository.deleteAll()
  }

  @AfterAll
  fun tearDown() {
    libraryRepository.deleteAll()
  }

  @Test
  fun `given a bookMetadataAggregation when inserting then it is persisted`() {
    val series = makeSeries("Series", libraryId = library.id).also { seriesRepository.insert(it) }

    val now = LocalDateTime.now()
    val metadata =
      BookMetadataAggregation(
        authors = listOf(Author("author", "role")),
        tags = setOf("tag1", "tag2"),
        releaseDate = LocalDate.now(),
        summary = "Summary",
        summaryNumber = "1",
        seriesId = series.id,
      )

    bookMetadataAggregationDao.insert(metadata)
    val created = bookMetadataAggregationDao.findById(metadata.seriesId)

    assertThat(created.seriesId).isEqualTo(series.id)
    assertThat(created.createdDate).isCloseTo(now, offset)
    assertThat(created.lastModifiedDate).isCloseTo(now, offset)

    assertThat(created.releaseDate).isEqualTo(metadata.releaseDate)
    assertThat(created.summary).isEqualTo(metadata.summary)
    assertThat(created.summaryNumber).isEqualTo(metadata.summaryNumber)
    with(created.authors.first()) {
      assertThat(name).isEqualTo(metadata.authors.first().name)
      assertThat(role).isEqualTo(metadata.authors.first().role)
    }
    assertThat(created.tags).containsExactlyInAnyOrderElementsOf(metadata.tags)
  }

  @Test
  fun `given a minimum bookMetadataAggregation when inserting then it is persisted`() {
    val series = makeSeries("Series", libraryId = library.id).also { seriesRepository.insert(it) }

    val now = LocalDateTime.now()
    val metadata =
      BookMetadataAggregation(
        seriesId = series.id,
      )

    bookMetadataAggregationDao.insert(metadata)
    val created = bookMetadataAggregationDao.findById(metadata.seriesId)

    assertThat(created.seriesId).isEqualTo(series.id)
    assertThat(created.createdDate).isCloseTo(now, offset)
    assertThat(created.lastModifiedDate).isCloseTo(now, offset)

    assertThat(created.releaseDate).isNull()
    assertThat(created.summary).isBlank
    assertThat(created.summaryNumber).isBlank
    assertThat(created.authors).isEmpty()
  }

  @Test
  fun `given existing bookMetadataAggregation when finding by id then metadata is returned`() {
    val series = makeSeries("Series", libraryId = library.id).also { seriesRepository.insert(it) }

    val metadata =
      BookMetadataAggregation(
        authors = listOf(Author("author", "role")),
        tags = setOf("tag1", "tag2"),
        releaseDate = LocalDate.now(),
        summary = "Summary",
        seriesId = series.id,
      )

    bookMetadataAggregationDao.insert(metadata)

    val found = bookMetadataAggregationDao.findById(series.id)

    assertThat(found).isNotNull
    assertThat(found.summary).isEqualTo("Summary")
  }

  @Test
  fun `given non-existing bookMetadataAggregation when finding by id then exception is thrown`() {
    val found = catchThrowable { bookMetadataAggregationDao.findById("128742") }

    assertThat(found).isInstanceOf(Exception::class.java)
  }

  @Test
  fun `given non-existing bookMetadataAggregation when findByIdOrNull then null is returned`() {
    val found = bookMetadataAggregationDao.findByIdOrNull("128742")

    assertThat(found).isNull()
  }

  @Test
  fun `given a bookMetadataAggregation when updating then it is persisted`() {
    val series = makeSeries("Series", libraryId = library.id).also { seriesRepository.insert(it) }

    val metadata =
      BookMetadataAggregation(
        authors = listOf(Author("author", "role")),
        tags = setOf("tag1", "tag2"),
        releaseDate = LocalDate.now(),
        summary = "Summary",
        summaryNumber = "1",
        seriesId = series.id,
      )
    bookMetadataAggregationDao.insert(metadata)
    val created = bookMetadataAggregationDao.findById(metadata.seriesId)

    val modificationDate = LocalDateTime.now()

    val updated =
      with(created) {
        copy(
          releaseDate = LocalDate.now().plusYears(1),
          summary = "SummaryUpdated",
          summaryNumber = "2",
          authors = listOf(Author("authorUpdated", "roleUpdated"), Author("author2", "role2")),
          tags = setOf("tag1", "tag2updated"),
        )
      }

    bookMetadataAggregationDao.update(updated)
    val modified = bookMetadataAggregationDao.findById(updated.seriesId)

    assertThat(modified.seriesId).isEqualTo(series.id)
    assertThat(modified.createdDate).isEqualTo(updated.createdDate)
    assertThat(modified.lastModifiedDate)
      .isCloseTo(modificationDate, offset)
      .isNotEqualTo(modified.createdDate)

    assertThat(modified.releaseDate).isEqualTo(updated.releaseDate)
    assertThat(modified.summary).isEqualTo(updated.summary)
    assertThat(modified.authors).hasSize(2)
    assertThat(modified.authors.map { it.name }).containsExactlyInAnyOrderElementsOf(updated.authors.map { it.name })
    assertThat(modified.authors.map { it.role }).containsExactlyInAnyOrderElementsOf(updated.authors.map { it.role })
    assertThat(modified.tags).containsExactlyInAnyOrderElementsOf(updated.tags)
  }
}
