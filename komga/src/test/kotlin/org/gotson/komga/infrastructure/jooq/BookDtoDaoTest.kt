package org.gotson.komga.infrastructure.jooq

import org.assertj.core.api.Assertions.assertThat
import org.gotson.komga.domain.model.BookSearchWithReadProgress
import org.gotson.komga.domain.model.KomgaUser
import org.gotson.komga.domain.model.ReadProgress
import org.gotson.komga.domain.model.ReadStatus
import org.gotson.komga.domain.model.makeBook
import org.gotson.komga.domain.model.makeLibrary
import org.gotson.komga.domain.model.makeSeries
import org.gotson.komga.domain.persistence.BookRepository
import org.gotson.komga.domain.persistence.KomgaUserRepository
import org.gotson.komga.domain.persistence.LibraryRepository
import org.gotson.komga.domain.persistence.ReadProgressRepository
import org.gotson.komga.domain.service.BookLifecycle
import org.gotson.komga.domain.service.KomgaUserLifecycle
import org.gotson.komga.domain.service.LibraryLifecycle
import org.gotson.komga.domain.service.SeriesLifecycle
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.domain.PageRequest
import org.springframework.test.context.junit.jupiter.SpringExtension

@ExtendWith(SpringExtension::class)
@SpringBootTest
class BookDtoDaoTest(
  @Autowired private val bookDtoDao: BookDtoDao,
  @Autowired private val bookRepository: BookRepository,
  @Autowired private val bookLifecycle: BookLifecycle,
  @Autowired private val seriesLifecycle: SeriesLifecycle,
  @Autowired private val libraryRepository: LibraryRepository,
  @Autowired private val libraryLifecycle: LibraryLifecycle,
  @Autowired private val readProgressRepository: ReadProgressRepository,
  @Autowired private val userRepository: KomgaUserRepository,
  @Autowired private val userLifecycle: KomgaUserLifecycle
) {

  private val library = makeLibrary()
  private var series = makeSeries("Series")
  private val user = KomgaUser("user@example.org", "", false)

  @BeforeAll
  fun setup() {
    libraryRepository.insert(library)
    series = seriesLifecycle.createSeries(series.copy(libraryId = library.id))
    userRepository.insert(user)
  }

  @AfterEach
  fun deleteBooks() {
    bookLifecycle.deleteMany(bookRepository.findAll())
  }

  @AfterAll
  fun tearDown() {
    userRepository.findAll().forEach {
      userLifecycle.deleteUser(it)
    }
    libraryRepository.findAll().forEach {
      libraryLifecycle.deleteLibrary(it)
    }
  }

  private fun setupBooks() {
    seriesLifecycle.addBooks(
      series,
      (1..3).map {
        makeBook("$it", seriesId = series.id, libraryId = library.id)
      }
    )

    val books = bookRepository.findAll().sortedBy { it.name }
    books.elementAt(0).let { readProgressRepository.save(ReadProgress(it.id, user.id, 5, false)) }
    books.elementAt(1).let { readProgressRepository.save(ReadProgress(it.id, user.id, 5, true)) }
  }

  @Nested
  inner class ReadProgress {
    @Test
    fun `given books in various read status when searching for read books then only read books are returned`() {
      // given
      setupBooks()

      // when
      val found = bookDtoDao.findAll(
        BookSearchWithReadProgress(readStatus = listOf(ReadStatus.READ)),
        user.id,
        PageRequest.of(0, 20)
      )

      // then
      assertThat(found).hasSize(1)
      assertThat(found.first().readProgress?.completed).isTrue()
      assertThat(found.first().name).isEqualTo("2")
    }

    @Test
    fun `given books in various read status when searching for unread books then only unread books are returned`() {
      // given
      setupBooks()

      // when
      val found = bookDtoDao.findAll(
        BookSearchWithReadProgress(readStatus = listOf(ReadStatus.UNREAD)),
        user.id,
        PageRequest.of(0, 20)
      )

      // then
      assertThat(found).hasSize(1)
      assertThat(found.first().readProgress).isNull()
      assertThat(found.first().name).isEqualTo("3")
    }

    @Test
    fun `given books in various read status when searching for in progress books then only in progress books are returned`() {
      // given
      setupBooks()

      // when
      val found = bookDtoDao.findAll(
        BookSearchWithReadProgress(readStatus = listOf(ReadStatus.IN_PROGRESS)),
        user.id,
        PageRequest.of(0, 20)
      )

      // then
      assertThat(found).hasSize(1)
      assertThat(found.first().readProgress?.completed).isFalse()
      assertThat(found.first().name).isEqualTo("1")
    }

    @Test
    fun `given books in various read status when searching for read and unread books then only matching books are returned`() {
      // given
      setupBooks()

      // when
      val found = bookDtoDao.findAll(
        BookSearchWithReadProgress(readStatus = listOf(ReadStatus.READ, ReadStatus.UNREAD)),
        user.id,
        PageRequest.of(0, 20)
      )

      // then
      assertThat(found).hasSize(2)
      assertThat(found.map { it.name }).containsExactlyInAnyOrder("2", "3")
    }

    @Test
    fun `given books in various read status when searching for read and in progress books then only matching books are returned`() {
      // given
      setupBooks()

      // when
      val found = bookDtoDao.findAll(
        BookSearchWithReadProgress(readStatus = listOf(ReadStatus.READ, ReadStatus.IN_PROGRESS)),
        user.id,
        PageRequest.of(0, 20)
      )

      // then
      assertThat(found).hasSize(2)
      assertThat(found.map { it.name }).containsExactlyInAnyOrder("2", "1")
    }

    @Test
    fun `given books in various read status when searching for unread and in progress books then only matching books are returned`() {
      // given
      setupBooks()

      // when
      val found = bookDtoDao.findAll(
        BookSearchWithReadProgress(readStatus = listOf(ReadStatus.UNREAD, ReadStatus.IN_PROGRESS)),
        user.id,
        PageRequest.of(0, 20)
      )

      // then
      assertThat(found).hasSize(2)
      assertThat(found.map { it.name }).containsExactlyInAnyOrder("3", "1")
    }

    @Test
    fun `given books in various read status when searching for read and unread and in progress books then only matching books are returned`() {
      // given
      setupBooks()

      // when
      val found = bookDtoDao.findAll(
        BookSearchWithReadProgress(readStatus = listOf(ReadStatus.UNREAD, ReadStatus.IN_PROGRESS, ReadStatus.READ)),
        user.id,
        PageRequest.of(0, 20)
      )

      // then
      assertThat(found).hasSize(3)
      assertThat(found.map { it.name }).containsExactlyInAnyOrder("3", "1", "2")
    }

    @Test
    fun `given books in various read status when searching without read progress then all books are returned`() {
      // given
      setupBooks()

      // when
      val found = bookDtoDao.findAll(
        BookSearchWithReadProgress(),
        user.id,
        PageRequest.of(0, 20)
      )

      // then
      assertThat(found).hasSize(3)
      assertThat(found.map { it.name }).containsExactlyInAnyOrder("3", "1", "2")
    }
  }

  @Nested
  inner class OnDeck {
    @Test
    fun `given series with in progress books status when searching for on deck then nothing is returned`() {
      // given
      setupBooks()

      // when
      val found = bookDtoDao.findAllOnDeck(
        user.id,
        null,
        PageRequest.of(0, 20)
      )

      // then
      assertThat(found).isEmpty()
    }

    @Test
    fun `given series with only unread books when searching for on deck then no books are returned`() {
      // given
      seriesLifecycle.addBooks(
        series,
        (1..3).map {
          makeBook("$it", seriesId = series.id, libraryId = library.id)
        }
      )

      // when
      val found = bookDtoDao.findAllOnDeck(
        user.id,
        null,
        PageRequest.of(0, 20)
      )

      // then
      assertThat(found).hasSize(0)
    }

    @Test
    fun `given series with some unread books when searching for on deck then first unread book of series is returned`() {
      // given
      seriesLifecycle.addBooks(
        series,
        (1..3).map {
          makeBook("$it", seriesId = series.id, libraryId = library.id)
        }
      )

      val books = bookRepository.findAll().sortedBy { it.name }
      books.elementAt(0).let { readProgressRepository.save(ReadProgress(it.id, user.id, 5, true)) }

      // when
      val found = bookDtoDao.findAllOnDeck(
        user.id,
        null,
        PageRequest.of(0, 20)
      )

      // then
      assertThat(found).hasSize(1)
      assertThat(found.first().name).isEqualTo("2")
    }
  }
}
