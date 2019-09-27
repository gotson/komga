package org.gotson.komga.domain.service

import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.gotson.komga.domain.model.BookMetadata
import org.gotson.komga.domain.model.Status
import org.gotson.komga.domain.model.makeBook
import org.gotson.komga.domain.model.makeBookPage
import org.gotson.komga.domain.model.makeLibrary
import org.gotson.komga.domain.model.makeSerie
import org.gotson.komga.domain.persistence.BookRepository
import org.gotson.komga.domain.persistence.LibraryRepository
import org.gotson.komga.domain.persistence.SerieRepository
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.transaction.annotation.Transactional
import java.nio.file.Paths

@ExtendWith(SpringExtension::class)
@SpringBootTest
@AutoConfigureTestDatabase
class LibraryScannerTest(
    @Autowired private val serieRepository: SerieRepository,
    @Autowired private val libraryRepository: LibraryRepository,
    @Autowired private val bookRepository: BookRepository,
    @Autowired private val libraryScanner: LibraryScanner,
    @Autowired private val bookLifecyle: BookLifecyle
) {

  @MockkBean
  private lateinit var mockScanner: FileSystemScanner

  @MockkBean
  private lateinit var mockParser: BookParser

  @AfterEach
  fun `clear repositories`() {
    serieRepository.deleteAll()
    libraryRepository.deleteAll()
  }

  @Test
  @Transactional
  fun `given existing Serie when adding files and scanning then only updated Books are persisted`() {
    // given
    val library = libraryRepository.save(makeLibrary())

    val serie = makeSerie(name = "serie", books = listOf(makeBook("book1")))
    val serieWithMoreBooks = makeSerie(name = "serie", books = listOf(makeBook("book1"), makeBook("book2")))

    every { mockScanner.scanRootFolder(any()) }.returnsMany(
        listOf(serie),
        listOf(serieWithMoreBooks)
    )
    libraryScanner.scanRootFolder(library)

    // when
    libraryScanner.scanRootFolder(library)

    // then
    val series = serieRepository.findAll()

    verify(exactly = 2) { mockScanner.scanRootFolder(any()) }

    assertThat(series).hasSize(1)
    assertThat(series.first().books).hasSize(2)
    assertThat(series.first().books.map { it.name }).containsExactly("book1", "book2")
  }

  @Test
  @Transactional
  fun `given existing Serie when removing files and scanning then only updated Books are persisted`() {
    // given
    val library = libraryRepository.save(makeLibrary())

    val serie = makeSerie(name = "serie", books = listOf(makeBook("book1"), makeBook("book2")))
    val serieWithLessBooks = makeSerie(name = "serie", books = listOf(makeBook("book1")))

    every { mockScanner.scanRootFolder(any()) }
        .returnsMany(
            listOf(serie),
            listOf(serieWithLessBooks)
        )
    libraryScanner.scanRootFolder(library)

    // when
    libraryScanner.scanRootFolder(library)

    // then
    val series = serieRepository.findAll()

    verify(exactly = 2) { mockScanner.scanRootFolder(any()) }

    assertThat(series).hasSize(1)
    assertThat(series.first().books).hasSize(1)
    assertThat(series.first().books.map { it.name }).containsExactly("book1")
    assertThat(bookRepository.count()).describedAs("Orphan book has been removed").isEqualTo(1)
  }

  @Test
  @Transactional
  fun `given existing Serie when updating files and scanning then Books are updated`() {
    // given
    val library = libraryRepository.save(makeLibrary())

    val serie = makeSerie(name = "serie", books = listOf(makeBook("book1")))
    val serieWithUpdatedBooks = makeSerie(name = "serie", books = listOf(makeBook("book1updated", "file:/book1")))

    every { mockScanner.scanRootFolder(any()) }
        .returnsMany(
            listOf(serie),
            listOf(serieWithUpdatedBooks)
        )
    libraryScanner.scanRootFolder(library)

    // when
    libraryScanner.scanRootFolder(library)

    // then
    val series = serieRepository.findAll()

    verify(exactly = 2) { mockScanner.scanRootFolder(any()) }

    assertThat(series).hasSize(1)
    assertThat(series.first().lastModifiedDate).isNotEqualTo(series.first().createdDate)
    assertThat(series.first().books).hasSize(1)
    assertThat(series.first().books.map { it.name }).containsExactly("book1updated")
    assertThat(series.first().books.first().lastModifiedDate).isNotEqualTo(series.first().books.first().createdDate)
  }

  @Test
  fun `given existing Serie when deleting all books and scanning then Series and Books are removed`() {
    // given
    val library = libraryRepository.save(makeLibrary())

    every { mockScanner.scanRootFolder(any()) }
        .returnsMany(
            listOf(makeSerie(name = "serie", books = listOf(makeBook("book1")))),
            emptyList()
        )
    libraryScanner.scanRootFolder(library)

    // when
    libraryScanner.scanRootFolder(library)

    // then
    verify(exactly = 2) { mockScanner.scanRootFolder(any()) }

    assertThat(serieRepository.count()).describedAs("Serie repository should be empty").isEqualTo(0)
    assertThat(bookRepository.count()).describedAs("Book repository should be empty").isEqualTo(0)
  }

  @Test
  fun `given existing Series when deleting all books of one serie and scanning then Serie and its Books are removed`() {
    // given
    val library = libraryRepository.save(makeLibrary())

    every { mockScanner.scanRootFolder(any()) }
        .returnsMany(
            listOf(makeSerie(name = "serie", books = listOf(makeBook("book1"))), makeSerie(name = "serie2", books = listOf(makeBook("book2")))),
            listOf(makeSerie(name = "serie", books = listOf(makeBook("book1"))))
        )
    libraryScanner.scanRootFolder(library)

    // when
    libraryScanner.scanRootFolder(library)

    // then
    verify(exactly = 2) { mockScanner.scanRootFolder(any()) }

    assertThat(serieRepository.count()).describedAs("Serie repository should be empty").isEqualTo(1)
    assertThat(bookRepository.count()).describedAs("Book repository should be empty").isEqualTo(1)
  }

  @Test
  fun `given existing Book with metadata when rescanning then metadata is kept intact`() {
    // given
    val library = libraryRepository.save(makeLibrary())

    val book1 = makeBook("book1")
    every { mockScanner.scanRootFolder(any()) }
        .returnsMany(
            listOf(makeSerie(name = "serie", books = listOf(book1))),
            listOf(makeSerie(name = "serie", books = listOf(makeBook(name = "book1", fileLastModified = book1.fileLastModified))))
        )
    libraryScanner.scanRootFolder(library)

    every { mockParser.parse(any()) } returns BookMetadata(status = Status.READY, mediaType = "application/zip", pages = mutableListOf(makeBookPage("1.jpg"), makeBookPage("2.jpg")))
    bookRepository.findAll().map { bookLifecyle.parseAndPersist(it) }.map { it.get() }

    // when
    libraryScanner.scanRootFolder(library)

    // then
    verify(exactly = 2) { mockScanner.scanRootFolder(any()) }
    verify(exactly = 1) { mockParser.parse(any()) }

    val book = bookRepository.findAll().first()
    assertThat(book.metadata.status).isEqualTo(Status.READY)
    assertThat(book.metadata.mediaType).isEqualTo("application/zip")
    assertThat(book.metadata.pages).hasSize(2)
    assertThat(book.metadata.pages.map { it.fileName }).containsExactly("1.jpg", "2.jpg")
    assertThat(book.lastModifiedDate).isNotEqualTo(book.createdDate)
  }

  @Test
  fun `given 2 libraries when deleting all books of one and scanning then the other library is kept intact`() {
    // given
    val library1 = libraryRepository.save(makeLibrary(name = "library1"))
    val library2 = libraryRepository.save(makeLibrary(name = "library2"))

    every { mockScanner.scanRootFolder(Paths.get(library1.root.toURI())) } returns
        listOf(makeSerie(name = "serie1", books = listOf(makeBook("book1"))))

    every { mockScanner.scanRootFolder(Paths.get(library2.root.toURI())) }.returnsMany(
        listOf(makeSerie(name = "serie2", books = listOf(makeBook("book2")))),
        emptyList()
    )

    libraryScanner.scanRootFolder(library1)
    libraryScanner.scanRootFolder(library2)

    assertThat(serieRepository.count()).describedAs("Serie repository should be empty").isEqualTo(2)
    assertThat(bookRepository.count()).describedAs("Book repository should be empty").isEqualTo(2)

    // when
    libraryScanner.scanRootFolder(library2)

    // then
    verify(exactly = 1) { mockScanner.scanRootFolder(Paths.get(library1.root.toURI())) }
    verify(exactly = 2) { mockScanner.scanRootFolder(Paths.get(library2.root.toURI())) }

    assertThat(serieRepository.count()).describedAs("Serie repository should be empty").isEqualTo(1)
    assertThat(bookRepository.count()).describedAs("Book repository should be empty").isEqualTo(1)
  }
}