package org.gotson.komga.infrastructure.jooq

import org.assertj.core.api.Assertions.assertThat
import org.gotson.komga.domain.model.Book
import org.gotson.komga.domain.model.BookSearch
import org.gotson.komga.domain.model.makeBook
import org.gotson.komga.domain.model.makeLibrary
import org.gotson.komga.domain.model.makeSeries
import org.gotson.komga.domain.persistence.LibraryRepository
import org.gotson.komga.domain.persistence.SeriesRepository
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.junit.jupiter.SpringExtension
import java.net.URL
import java.time.LocalDateTime

@ExtendWith(SpringExtension::class)
@SpringBootTest
@AutoConfigureTestDatabase
class BookDaoTest(
  @Autowired private val bookDao: BookDao,
  @Autowired private val seriesRepository: SeriesRepository,
  @Autowired private val libraryRepository: LibraryRepository
) {

  private var library = makeLibrary()
  private var series = makeSeries("Series")

  @BeforeAll
  fun setup() {
    library = libraryRepository.insert(library)
    series = seriesRepository.insert(series.copy(libraryId = library.id))
  }

  @AfterEach
  fun deleteBooks() {
    bookDao.deleteAll()
    assertThat(bookDao.count()).isEqualTo(0)
  }

  @AfterAll
  fun tearDown() {
    seriesRepository.deleteAll()
    libraryRepository.deleteAll()
  }


  @Test
  fun `given a book when inserting then it is persisted`() {
    val now = LocalDateTime.now()
    val book = Book(
      name = "Book",
      url = URL("file://book"),
      fileLastModified = now,
      fileSize = 3,
      seriesId = series.id,
      libraryId = library.id
    )

    Thread.sleep(5)

    val created = bookDao.insert(book)

    assertThat(created.id).isNotEqualTo(0)
    assertThat(created.createdDate).isAfter(now)
    assertThat(created.lastModifiedDate).isAfter(now)
    assertThat(created.name).isEqualTo(book.name)
    assertThat(created.url).isEqualTo(book.url)
    assertThat(created.fileLastModified).isEqualTo(book.fileLastModified)
    assertThat(created.fileSize).isEqualTo(book.fileSize)
  }

  @Test
  fun `given existing book when updating then it is persisted`() {
    val book = Book(
      name = "Book",
      url = URL("file://book"),
      fileLastModified = LocalDateTime.now(),
      fileSize = 3,
      seriesId = series.id,
      libraryId = library.id
    )
    val created = bookDao.insert(book)

    Thread.sleep(5)

    val modificationDate = LocalDateTime.now()

    val updated = with(created) {
      copy(
        name = "Updated",
        url = URL("file://updated"),
        fileLastModified = modificationDate,
        fileSize = 5
      )
    }

    bookDao.update(updated)
    val modified = bookDao.findByIdOrNull(updated.id)!!

    assertThat(modified.id).isEqualTo(updated.id)
    assertThat(modified.createdDate).isEqualTo(updated.createdDate)
    assertThat(modified.lastModifiedDate)
      .isAfterOrEqualTo(modificationDate)
      .isNotEqualTo(updated.lastModifiedDate)
    assertThat(modified.name).isEqualTo("Updated")
    assertThat(modified.url).isEqualTo(URL("file://updated"))
    assertThat(modified.fileLastModified).isEqualTo(modificationDate)
    assertThat(modified.fileSize).isEqualTo(5)
  }

  @Test
  fun `given existing book when finding by id then book is returned`() {
    val book = Book(
      name = "Book",
      url = URL("file://book"),
      fileLastModified = LocalDateTime.now(),
      fileSize = 3,
      seriesId = series.id,
      libraryId = library.id
    )
    val created = bookDao.insert(book)

    val found = bookDao.findByIdOrNull(created.id)

    assertThat(found).isNotNull
    assertThat(found?.name).isEqualTo("Book")
  }

  @Test
  fun `given non-existing book when finding by id then null is returned`() {
    val found = bookDao.findByIdOrNull(128742)

    assertThat(found).isNull()
  }

  @Test
  fun `given some books when finding all then all are returned`() {
    bookDao.insert(makeBook("1", libraryId = library.id, seriesId = series.id))
    bookDao.insert(makeBook("2", libraryId = library.id, seriesId = series.id))

    val found = bookDao.findAll()

    assertThat(found).hasSize(2)
  }

  @Test
  fun `given some books when searching then results are returned`() {
    bookDao.insert(makeBook("1", libraryId = library.id, seriesId = series.id))
    bookDao.insert(makeBook("2", libraryId = library.id, seriesId = series.id))

    val search = BookSearch(
      libraryIds = listOf(library.id),
      seriesIds = listOf(series.id)
    )
    val found = bookDao.findAll(search)

    assertThat(found).hasSize(2)
  }

  @Test
  fun `given some books when finding by libraryId then results are returned`() {
    bookDao.insert(makeBook("1", libraryId = library.id, seriesId = series.id))
    bookDao.insert(makeBook("2", libraryId = library.id, seriesId = series.id))

    val found = bookDao.findAllIdByLibraryId(library.id)

    assertThat(found).hasSize(2)
  }

  @Test
  fun `given some books when finding by seriesId then results are returned`() {
    bookDao.insert(makeBook("1", libraryId = library.id, seriesId = series.id))
    bookDao.insert(makeBook("2", libraryId = library.id, seriesId = series.id))

    val found = bookDao.findAllIdBySeriesId(series.id)

    assertThat(found).hasSize(2)
  }

  @Test
  fun `given some books when deleting all then count is zero`() {
    bookDao.insert(makeBook("1", libraryId = library.id, seriesId = series.id))
    bookDao.insert(makeBook("2", libraryId = library.id, seriesId = series.id))

    bookDao.deleteAll()

    assertThat(bookDao.count()).isEqualTo(0)
  }
}
