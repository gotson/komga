package org.gotson.komga.domain.service

import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.gotson.komga.domain.model.BookMetadata
import org.gotson.komga.domain.model.Library
import org.gotson.komga.domain.model.Status
import org.gotson.komga.domain.model.makeBook
import org.gotson.komga.domain.model.makeSerie
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
import javax.persistence.EntityManager

@ExtendWith(SpringExtension::class)
@SpringBootTest
@AutoConfigureTestDatabase
@Transactional
class LibraryManagerTest(
    @Autowired private val serieRepository: SerieRepository,
    @Autowired private val bookRepository: BookRepository,
    @Autowired private val libraryManager: LibraryManager,
    @Autowired private val bookManager: BookManager,
    @Autowired private val entityManager: EntityManager
) {

  @MockkBean
  private lateinit var mockScanner: FileSystemScanner

  @MockkBean
  private lateinit var mockParser: BookParser

  private val library = Library(name = "test", root = "/root")

  @AfterEach
  fun `clear repositories`() {
    entityManager.clear()
  }

  @Test
  fun `given existing Serie when adding files and scanning then only updated Books are persisted`() {
    //given
    val serie = makeSerie(name = "serie", books = listOf(makeBook("book1")))
    val serieWithMoreBooks = makeSerie(name = "serie", books = listOf(makeBook("book1"), makeBook("book2")))

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
    val serie = makeSerie(name = "serie", books = listOf(makeBook("book1"), makeBook("book2")))
    val serieWithLessBooks = makeSerie(name = "serie", books = listOf(makeBook("book1")))

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
    val serie = makeSerie(name = "serie", books = listOf(makeBook("book1")))
    val serieWithUpdatedBooks = makeSerie(name = "serie", books = listOf(makeBook("book1updated", "file:/book1")))

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
    val serie = makeSerie(name = "serie", books = listOf(makeBook("book1")))

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

  @Test
  fun `given existing Book with metadata when rescanning then metadata is kept intact`() {
    //given
    every { mockScanner.scanRootFolder(any()) }
        .returnsMany(
            listOf(makeSerie(name = "serie", books = listOf(makeBook("book1")))),
            listOf(makeSerie(name = "serie", books = listOf(makeBook("book1"))))
        )
    libraryManager.scanRootFolder(library)

    every { mockParser.parse(any()) } returns BookMetadata(status = Status.READY, mediaType = "application/zip", pages = listOf("1.jpg", "2.jpg"))
    bookRepository.findAll().forEach { bookManager.parseAndPersist(it) }

    // when
    libraryManager.scanRootFolder(library)

    // then
    verify(exactly = 2) { mockScanner.scanRootFolder(any()) }
    verify(exactly = 1) { mockParser.parse(any()) }

    val book = bookRepository.findAll().first()
    assertThat(book.metadata.status).isEqualTo(Status.READY)
    assertThat(book.metadata.mediaType).isEqualTo("application/zip")
    assertThat(book.metadata.pages)
        .hasSize(2)
        .containsExactly("1.jpg", "2.jpg")
  }
}