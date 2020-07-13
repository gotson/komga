package org.gotson.komga.infrastructure.jooq

import org.assertj.core.api.Assertions.assertThat
import org.gotson.komga.domain.model.KomgaUser
import org.gotson.komga.domain.model.ReadProgress
import org.gotson.komga.domain.model.ReadStatus
import org.gotson.komga.domain.model.SeriesSearchWithReadProgress
import org.gotson.komga.domain.model.makeBook
import org.gotson.komga.domain.model.makeLibrary
import org.gotson.komga.domain.model.makeSeries
import org.gotson.komga.domain.persistence.BookRepository
import org.gotson.komga.domain.persistence.KomgaUserRepository
import org.gotson.komga.domain.persistence.LibraryRepository
import org.gotson.komga.domain.persistence.ReadProgressRepository
import org.gotson.komga.domain.persistence.SeriesRepository
import org.gotson.komga.domain.service.KomgaUserLifecycle
import org.gotson.komga.domain.service.LibraryLifecycle
import org.gotson.komga.domain.service.SeriesLifecycle
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.domain.PageRequest
import org.springframework.test.context.junit.jupiter.SpringExtension

@ExtendWith(SpringExtension::class)
@SpringBootTest
class SeriesDtoDaoTest(
  @Autowired private val seriesDtoDao: SeriesDtoDao,
  @Autowired private val bookRepository: BookRepository,
  @Autowired private val seriesRepository: SeriesRepository,
  @Autowired private val seriesLifecycle: SeriesLifecycle,
  @Autowired private val libraryRepository: LibraryRepository,
  @Autowired private val libraryLifecycle: LibraryLifecycle,
  @Autowired private val readProgressRepository: ReadProgressRepository,
  @Autowired private val userRepository: KomgaUserRepository,
  @Autowired private val userLifecycle: KomgaUserLifecycle
) {

  private val library = makeLibrary()
  private val user = KomgaUser("user@example.org", "", false)

  @BeforeAll
  fun setup() {
    libraryRepository.insert(library)
    userRepository.insert(user)
  }

  @AfterEach
  fun deleteSeries() {
    seriesLifecycle.deleteMany(seriesRepository.findAll().map { it.id })
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

  private fun setupSeries() {
    (1..4).map { makeSeries("$it", library.id) }
      .forEach { series ->
        val created = seriesLifecycle.createSeries(series)
        seriesLifecycle.addBooks(created,
          (1..3).map {
            makeBook("$it", seriesId = created.id, libraryId = library.id)
          })
      }

    val series = seriesRepository.findAll().sortedBy { it.name }
    // series "1": only in progress books
    series.elementAt(0).let {
      bookRepository.findBySeriesId(it.id).forEach { readProgressRepository.save(ReadProgress(it.id, user.id, 5, false)) }
    }
    // series "2": only read books
    series.elementAt(1).let {
      bookRepository.findBySeriesId(it.id).forEach { readProgressRepository.save(ReadProgress(it.id, user.id, 5, true)) }
    }
    // series "3": only unread books
    // series "4": read, unread, and in progress
    series.elementAt(3).let {
      val books = bookRepository.findBySeriesId(it.id).sortedBy { it.name }
      books.elementAt(0).let { readProgressRepository.save(ReadProgress(it.id, user.id, 5, false)) }
      books.elementAt(1).let { readProgressRepository.save(ReadProgress(it.id, user.id, 5, true)) }
    }
  }

  @Test
  fun `given series in various read status when searching for read series then only read series are returned`() {
    // given
    setupSeries()

    // when
    val found = seriesDtoDao.findAll(
      SeriesSearchWithReadProgress(readStatus = listOf(ReadStatus.READ)),
      user.id,
      PageRequest.of(0, 20)
    ).sortedBy { it.name }

    // then
    assertThat(found).hasSize(2)

    assertThat(found.first().booksReadCount).isEqualTo(3)
    assertThat(found.first().name).isEqualTo("2")

    assertThat(found.last().booksReadCount).isEqualTo(1)
    assertThat(found.last().name).isEqualTo("4")
  }

  @Test
  fun `given series in various read status when searching for unread series then only unread series are returned`() {
    // given
    setupSeries()

    // when
    val found = seriesDtoDao.findAll(
      SeriesSearchWithReadProgress(readStatus = listOf(ReadStatus.UNREAD)),
      user.id,
      PageRequest.of(0, 20)
    ).sortedBy { it.name }

    // then
    assertThat(found).hasSize(2)

    assertThat(found.first().booksUnreadCount).isEqualTo(3)
    assertThat(found.first().name).isEqualTo("3")

    assertThat(found.last().booksUnreadCount).isEqualTo(1)
    assertThat(found.last().name).isEqualTo("4")
  }

  @Test
  fun `given series in various read status when searching for in progress series then only in progress series are returned`() {
    // given
    setupSeries()

    // when
    val found = seriesDtoDao.findAll(
      SeriesSearchWithReadProgress(readStatus = listOf(ReadStatus.IN_PROGRESS)),
      user.id,
      PageRequest.of(0, 20)
    ).sortedBy { it.name }

    // then
    assertThat(found).hasSize(2)

    assertThat(found.first().booksInProgressCount).isEqualTo(3)
    assertThat(found.first().name).isEqualTo("1")

    assertThat(found.last().booksInProgressCount).isEqualTo(1)
    assertThat(found.last().name).isEqualTo("4")
  }

  @Test
  fun `given series in various read status when searching for read and unread series then only matching series are returned`() {
    // given
    setupSeries()

    // when
    val found = seriesDtoDao.findAll(
      SeriesSearchWithReadProgress(readStatus = listOf(ReadStatus.READ, ReadStatus.UNREAD)),
      user.id,
      PageRequest.of(0, 20)
    ).sortedBy { it.name }

    // then
    assertThat(found).hasSize(3)
    assertThat(found.map { it.name }).containsExactlyInAnyOrder("2", "3", "4")
  }

  @Test
  fun `given series in various read status when searching for read and in progress series then only matching series are returned`() {
    // given
    setupSeries()

    // when
    val found = seriesDtoDao.findAll(
      SeriesSearchWithReadProgress(readStatus = listOf(ReadStatus.READ, ReadStatus.IN_PROGRESS)),
      user.id,
      PageRequest.of(0, 20)
    ).sortedBy { it.name }

    // then
    assertThat(found).hasSize(3)
    assertThat(found.map { it.name }).containsExactlyInAnyOrder("1", "2", "4")
  }

  @Test
  fun `given series in various read status when searching for unread and in progress series then only matching series are returned`() {
    // given
    setupSeries()

    // when
    val found = seriesDtoDao.findAll(
      SeriesSearchWithReadProgress(readStatus = listOf(ReadStatus.UNREAD, ReadStatus.IN_PROGRESS)),
      user.id,
      PageRequest.of(0, 20)
    ).sortedBy { it.name }

    // then
    assertThat(found).hasSize(3)
    assertThat(found.map { it.name }).containsExactlyInAnyOrder("1", "3", "4")
  }

  @Test
  fun `given series in various read status when searching for read and unread and in progress series then only matching series are returned`() {
    // given
    setupSeries()

    // when
    val found = seriesDtoDao.findAll(
      SeriesSearchWithReadProgress(readStatus = listOf(ReadStatus.READ, ReadStatus.IN_PROGRESS, ReadStatus.UNREAD)),
      user.id,
      PageRequest.of(0, 20)
    ).sortedBy { it.name }

    // then
    assertThat(found).hasSize(4)
    assertThat(found.map { it.name }).containsExactlyInAnyOrder("1", "2", "3", "4")
  }

  @Test
  fun `given series in various read status when searching without read progress then all series are returned`() {
    // given
    setupSeries()

    // when
    val found = seriesDtoDao.findAll(
      SeriesSearchWithReadProgress(),
      user.id,
      PageRequest.of(0, 20)
    ).sortedBy { it.name }

    // then
    assertThat(found).hasSize(4)
    assertThat(found.map { it.name }).containsExactlyInAnyOrder("1", "2", "3", "4")
  }
}
