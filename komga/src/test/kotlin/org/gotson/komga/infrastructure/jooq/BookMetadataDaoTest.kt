package org.gotson.komga.infrastructure.jooq

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.catchThrowable
import org.gotson.komga.domain.model.Author
import org.gotson.komga.domain.model.BookMetadata
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
import java.time.LocalDate
import java.time.LocalDateTime

@ExtendWith(SpringExtension::class)
@SpringBootTest
class BookMetadataDaoTest(
  @Autowired private val bookMetadataDao: BookMetadataDao,
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
      bookMetadataDao.delete(it.id)
    }
  }

  @AfterAll
  fun tearDown() {
    bookRepository.deleteAll()
    seriesRepository.deleteAll()
    libraryRepository.deleteAll()
  }

  @Test
  fun `given a metadata when inserting then it is persisted`() {
    val now = LocalDateTime.now()
    val metadata = BookMetadata(
      title = "Book",
      summary = "Summary",
      number = "1",
      numberSort = 1F,
      readingDirection = BookMetadata.ReadingDirection.LEFT_TO_RIGHT,
      publisher = "publisher",
      ageRating = 18,
      releaseDate = LocalDate.now(),
      authors = mutableListOf(Author("author", "role")),
      bookId = book.id,
      titleLock = true,
      summaryLock = true,
      numberLock = true,
      numberSortLock = true,
      readingDirectionLock = true,
      publisherLock = true,
      ageRatingLock = true,
      releaseDateLock = true,
      authorsLock = true
    )

    bookMetadataDao.insert(metadata)
    val created = bookMetadataDao.findById(metadata.bookId)

    assertThat(created.bookId).isEqualTo(book.id)
    assertThat(created.createdDate).isCloseTo(now, offset)
    assertThat(created.lastModifiedDate).isCloseTo(now, offset)

    assertThat(created.title).isEqualTo(metadata.title)
    assertThat(created.summary).isEqualTo(metadata.summary)
    assertThat(created.number).isEqualTo(metadata.number)
    assertThat(created.numberSort).isEqualTo(metadata.numberSort)
    assertThat(created.readingDirection).isEqualTo(metadata.readingDirection)
    assertThat(created.publisher).isEqualTo(metadata.publisher)
    assertThat(created.ageRating).isEqualTo(metadata.ageRating)
    assertThat(created.releaseDate).isEqualTo(metadata.releaseDate)
    assertThat(created.authors).hasSize(1)
    with(created.authors.first()) {
      assertThat(name).isEqualTo(metadata.authors.first().name)
      assertThat(role).isEqualTo(metadata.authors.first().role)
    }

    assertThat(created.titleLock).isEqualTo(metadata.titleLock)
    assertThat(created.summaryLock).isEqualTo(metadata.summaryLock)
    assertThat(created.numberLock).isEqualTo(metadata.numberLock)
    assertThat(created.numberSortLock).isEqualTo(metadata.numberSortLock)
    assertThat(created.readingDirectionLock).isEqualTo(metadata.readingDirectionLock)
    assertThat(created.publisherLock).isEqualTo(metadata.publisherLock)
    assertThat(created.ageRatingLock).isEqualTo(metadata.ageRatingLock)
    assertThat(created.releaseDateLock).isEqualTo(metadata.releaseDateLock)
    assertThat(created.authorsLock).isEqualTo(metadata.authorsLock)
  }

  @Test
  fun `given a minimum metadata when inserting then it is persisted`() {
    val metadata = BookMetadata(
      title = "Book",
      number = "1",
      numberSort = 1F,
      bookId = book.id
    )

    bookMetadataDao.insert(metadata)
    val created = bookMetadataDao.findById(metadata.bookId)

    assertThat(created.bookId).isEqualTo(book.id)

    assertThat(created.title).isEqualTo(metadata.title)
    assertThat(created.summary).isBlank()
    assertThat(created.number).isEqualTo(metadata.number)
    assertThat(created.numberSort).isEqualTo(metadata.numberSort)
    assertThat(created.readingDirection).isNull()
    assertThat(created.publisher).isBlank()
    assertThat(created.ageRating).isNull()
    assertThat(created.releaseDate).isNull()
    assertThat(created.authors).isEmpty()

    assertThat(created.titleLock).isFalse()
    assertThat(created.summaryLock).isFalse()
    assertThat(created.numberLock).isFalse()
    assertThat(created.numberSortLock).isFalse()
    assertThat(created.readingDirectionLock).isFalse()
    assertThat(created.publisherLock).isFalse()
    assertThat(created.ageRatingLock).isFalse()
    assertThat(created.releaseDateLock).isFalse()
    assertThat(created.authorsLock).isFalse()
  }

  @Test
  fun `given existing metadata when updating then it is persisted`() {
    val metadata = BookMetadata(
      title = "Book",
      summary = "Summary",
      number = "1",
      numberSort = 1F,
      readingDirection = BookMetadata.ReadingDirection.LEFT_TO_RIGHT,
      publisher = "publisher",
      ageRating = 18,
      releaseDate = LocalDate.now(),
      authors = mutableListOf(Author("author", "role")),
      bookId = book.id
    )
    bookMetadataDao.insert(metadata)

    val modificationDate = LocalDateTime.now()
    val updated = with(bookMetadataDao.findById(metadata.bookId)) {
      copy(
        title = "BookUpdated",
        summary = "SummaryUpdated",
        number = "2",
        numberSort = 2F,
        readingDirection = BookMetadata.ReadingDirection.RIGHT_TO_LEFT,
        publisher = "publisher2",
        ageRating = 15,
        releaseDate = LocalDate.now(),
        authors = mutableListOf(Author("author2", "role2")),
        titleLock = true,
        summaryLock = true,
        numberLock = true,
        numberSortLock = true,
        readingDirectionLock = true,
        publisherLock = true,
        ageRatingLock = true,
        releaseDateLock = true,
        authorsLock = true
      )
    }

    bookMetadataDao.update(updated)
    val modified = bookMetadataDao.findById(updated.bookId)

    assertThat(modified.bookId).isEqualTo(updated.bookId)
    assertThat(modified.createdDate).isEqualTo(updated.createdDate)
    assertThat(modified.lastModifiedDate)
      .isCloseTo(modificationDate, offset)
      .isNotEqualTo(updated.lastModifiedDate)

    assertThat(modified.title).isEqualTo(updated.title)
    assertThat(modified.summary).isEqualTo(updated.summary)
    assertThat(modified.number).isEqualTo(updated.number)
    assertThat(modified.numberSort).isEqualTo(updated.numberSort)
    assertThat(modified.readingDirection).isEqualTo(updated.readingDirection)
    assertThat(modified.publisher).isEqualTo(updated.publisher)
    assertThat(modified.ageRating).isEqualTo(updated.ageRating)

    assertThat(modified.titleLock).isEqualTo(updated.titleLock)
    assertThat(modified.summaryLock).isEqualTo(updated.summaryLock)
    assertThat(modified.numberLock).isEqualTo(updated.numberLock)
    assertThat(modified.numberSortLock).isEqualTo(updated.numberSortLock)
    assertThat(modified.readingDirectionLock).isEqualTo(updated.readingDirectionLock)
    assertThat(modified.publisherLock).isEqualTo(updated.publisherLock)
    assertThat(modified.ageRatingLock).isEqualTo(updated.ageRatingLock)
    assertThat(modified.releaseDateLock).isEqualTo(updated.releaseDateLock)
    assertThat(modified.authorsLock).isEqualTo(updated.authorsLock)

    assertThat(modified.authors.first().name).isEqualTo(updated.authors.first().name)
    assertThat(modified.authors.first().role).isEqualTo(updated.authors.first().role)
  }

  @Test
  fun `given existing metadata when finding by id then metadata is returned`() {
    val metadata = BookMetadata(
      title = "Book",
      summary = "Summary",
      number = "1",
      numberSort = 1F,
      readingDirection = BookMetadata.ReadingDirection.LEFT_TO_RIGHT,
      publisher = "publisher",
      ageRating = 18,
      releaseDate = LocalDate.now(),
      authors = mutableListOf(Author("author", "role")),
      bookId = book.id
    )
    bookMetadataDao.insert(metadata)

    val found = catchThrowable { bookMetadataDao.findById(metadata.bookId) }

    assertThat(found).doesNotThrowAnyException()
  }

  @Test
  fun `given non-existing metadata when finding by id then exception is thrown`() {
    val found = catchThrowable { bookMetadataDao.findById("128742") }

    assertThat(found).isInstanceOf(Exception::class.java)
  }
}

