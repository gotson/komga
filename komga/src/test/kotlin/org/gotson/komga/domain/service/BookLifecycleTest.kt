package org.gotson.komga.domain.service

import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import org.assertj.core.api.Assertions.assertThat
import org.gotson.komga.domain.model.BookPage
import org.gotson.komga.domain.model.KomgaUser
import org.gotson.komga.domain.model.Media
import org.gotson.komga.domain.model.makeBook
import org.gotson.komga.domain.model.makeBookPage
import org.gotson.komga.domain.model.makeLibrary
import org.gotson.komga.domain.model.makeSeries
import org.gotson.komga.domain.persistence.BookRepository
import org.gotson.komga.domain.persistence.KomgaUserRepository
import org.gotson.komga.domain.persistence.LibraryRepository
import org.gotson.komga.domain.persistence.MediaRepository
import org.gotson.komga.domain.persistence.ReadProgressRepository
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
class BookLifecycleTest(
  @Autowired private val bookLifecycle: BookLifecycle,
  @Autowired private val bookRepository: BookRepository,
  @Autowired private val libraryRepository: LibraryRepository,
  @Autowired private val seriesRepository: SeriesRepository,
  @Autowired private val seriesLifecycle: SeriesLifecycle,
  @Autowired private val readProgressRepository: ReadProgressRepository,
  @Autowired private val mediaRepository: MediaRepository,
  @Autowired private val userRepository: KomgaUserRepository,
) {

  @MockkBean
  private lateinit var mockAnalyzer: BookAnalyzer

  private val library = makeLibrary()
  private val user1 = KomgaUser("user1@example.org", "", false)
  private val user2 = KomgaUser("user2@example.org", "", false)

  @BeforeAll
  fun `setup library`() {
    libraryRepository.insert(library)

    userRepository.insert(user1)
    userRepository.insert(user2)
  }

  @AfterAll
  fun teardown() {
    libraryRepository.deleteAll()
    userRepository.deleteAll()
  }

  @AfterEach
  fun `clear repository`() {
    seriesLifecycle.deleteMany(seriesRepository.findAll())
  }

  @Test
  fun `given outdated book with different number of pages than before when analyzing then existing read progress is deleted`() {
    // given
    makeSeries(name = "series", libraryId = library.id).let { series ->
      seriesLifecycle.createSeries(series).let { created ->
        val books = listOf(makeBook("1", libraryId = library.id))
        seriesLifecycle.addBooks(created, books)
      }
    }

    val book = bookRepository.findAll().first()
    mediaRepository.findById(book.id).let { media ->
      mediaRepository.update(
        media.copy(
          status = Media.Status.OUTDATED,
          pages = (1..10).map { BookPage("$it", "image/jpeg") }
        )
      )
    }

    bookLifecycle.markReadProgressCompleted(book.id, user1)
    bookLifecycle.markReadProgress(book, user2, 4)

    assertThat(readProgressRepository.findAll()).hasSize(2)

    // when
    every { mockAnalyzer.analyze(any()) } returns Media(status = Media.Status.READY, mediaType = "application/zip", pages = mutableListOf(makeBookPage("1.jpg"), makeBookPage("2.jpg")), bookId = book.id)
    bookLifecycle.analyzeAndPersist(book)

    // then
    assertThat(readProgressRepository.findAll()).isEmpty()
  }

  @Test
  fun `given outdated book with same number of pages than before when analyzing then existing read progress is kept`() {
    // given
    makeSeries(name = "series", libraryId = library.id).let { series ->
      seriesLifecycle.createSeries(series).let { created ->
        val books = listOf(makeBook("1", libraryId = library.id))
        seriesLifecycle.addBooks(created, books)
      }
    }

    val book = bookRepository.findAll().first()
    mediaRepository.findById(book.id).let { media ->
      mediaRepository.update(
        media.copy(
          status = Media.Status.OUTDATED,
          pages = (1..10).map { BookPage("$it", "image/jpeg") }
        )
      )
    }

    bookLifecycle.markReadProgressCompleted(book.id, user1)
    bookLifecycle.markReadProgress(book, user2, 4)

    assertThat(readProgressRepository.findAll()).hasSize(2)

    // when
    every { mockAnalyzer.analyze(any()) } returns Media(status = Media.Status.READY, mediaType = "application/zip", pages = (1..10).map { BookPage("$it", "image/jpeg") }, bookId = book.id)
    bookLifecycle.analyzeAndPersist(book)

    // then
    assertThat(readProgressRepository.findAll()).hasSize(2)
  }
}
