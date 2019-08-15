package org.gotson.komga.domain.service

import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.gotson.komga.domain.model.Book
import org.gotson.komga.domain.model.Library
import org.gotson.komga.domain.model.Serie
import org.gotson.komga.domain.persistence.BookRepository
import org.gotson.komga.domain.persistence.SerieRepository
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.transaction.annotation.Transactional
import java.net.URL
import java.time.LocalDateTime

@ExtendWith(SpringExtension::class)
@SpringBootTest
@AutoConfigureTestDatabase
@Transactional
class LibraryManagerTest(
    @Autowired private val serieRepository: SerieRepository,
    @Autowired private val bookRepository: BookRepository,
    @Autowired private val libraryManager: LibraryManager
) {

  @MockkBean
  private lateinit var mockScanner: FileSystemScanner

  private val library = Library(name = "test", root = "/root")

  @AfterEach
  fun `clear repository`() {
    serieRepository.deleteAll()
  }

  private fun makeBook(name: String, url: String = "file:/$name") =
      Book(name = name, url = URL(url), updated = LocalDateTime.now())

  private fun makeSerie(name: String, url: String = "file:/$name", books: MutableList<Book> = mutableListOf()) =
      Serie(name = name, url = URL(url), updated = LocalDateTime.now()).also { it.books = books }

  @Test
  fun `given existing Serie when adding files and scanning then only updated Books are persisted`() {
    //given
    val serie = makeSerie(name = "serie", books = mutableListOf(makeBook("book1")))
    val serieWithMoreBooks = makeSerie(name = "serie", books = mutableListOf(makeBook("book1"), makeBook("book2")))

    every { mockScanner.scanRootFolder(any()) }.returnsMany(
        listOf(serie),
        listOf(serieWithMoreBooks)
    )
    libraryManager.scanRootFolder(library)

    // when
    libraryManager.scanRootFolder(library)

    // then
    val series = serieRepository.findAll()

    verify(exactly = 2) { mockScanner.scanRootFolder(any()) }

    assertThat(series).hasSize(1)
    assertThat(series.first().books).hasSize(2)
    assertThat(series.first().books.map { it.name }).containsExactlyInAnyOrder("book1", "book2")
  }

  @Test
  fun `given existing Serie when removing files and scanning then only updated Books are persisted`() {
    //given
    val serie = makeSerie(name = "serie", books = mutableListOf(makeBook("book1"), makeBook("book2")))
    val serieWithLessBooks = makeSerie(name = "serie", books = mutableListOf(makeBook("book1")))

    every { mockScanner.scanRootFolder(any()) }
        .returnsMany(
            listOf(serie),
            listOf(serieWithLessBooks)
        )
    libraryManager.scanRootFolder(library)

    // when
    libraryManager.scanRootFolder(library)

    // then
    val series = serieRepository.findAll()

    verify(exactly = 2) { mockScanner.scanRootFolder(any()) }

    assertThat(series).hasSize(1)
    assertThat(series.first().books).hasSize(1)
    assertThat(series.first().books.map { it.name }).containsExactlyInAnyOrder("book1")
    assertThat(bookRepository.count()).describedAs("Orphan book has been removed").isEqualTo(1)
  }

  @Test
  fun `given existing Serie when updating files and scanning then Books are updated`() {
    //given
    val serie = makeSerie(name = "serie", books = mutableListOf(makeBook("book1")))
    val serieWithUpdatedBooks = makeSerie(name = "serie", books = mutableListOf(makeBook("book1updated", "file:/book1")))

    every { mockScanner.scanRootFolder(any()) }
        .returnsMany(
            listOf(serie),
            listOf(serieWithUpdatedBooks)
        )
    libraryManager.scanRootFolder(library)

    // when
    libraryManager.scanRootFolder(library)

    // then
    val series = serieRepository.findAll()

    verify(exactly = 2) { mockScanner.scanRootFolder(any()) }

    assertThat(series).hasSize(1)
    assertThat(series.first().books).hasSize(1)
    assertThat(series.first().books.map { it.name }).containsExactlyInAnyOrder("book1updated")
  }

  @Test
  fun `given existing Serie when deleting all books and scanning then Series and Books are removed`() {
    //given
    val serie = makeSerie(name = "serie", books = mutableListOf(makeBook("book1")))

    every { mockScanner.scanRootFolder(any()) }
        .returnsMany(
            listOf(serie),
            emptyList()
        )
    libraryManager.scanRootFolder(library)

    // when
    libraryManager.scanRootFolder(library)

    // then
    verify(exactly = 2) { mockScanner.scanRootFolder(any()) }

    assertThat(serieRepository.count()).describedAs("Serie repository should be empty").isEqualTo(0)
    assertThat(bookRepository.count()).describedAs("Book repository should be empty").isEqualTo(0)
  }
}