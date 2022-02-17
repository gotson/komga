package org.gotson.komga.domain.service

import com.ninjasquad.springmockk.MockkBean
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import org.assertj.core.api.Assertions.assertThat
import org.gotson.komga.application.tasks.TaskEmitter
import org.gotson.komga.domain.model.ReadList
import org.gotson.komga.domain.model.ReadListRequest
import org.gotson.komga.domain.model.ReadListRequestBook
import org.gotson.komga.domain.model.makeBook
import org.gotson.komga.domain.model.makeLibrary
import org.gotson.komga.domain.model.makeSeries
import org.gotson.komga.domain.persistence.BookMetadataRepository
import org.gotson.komga.domain.persistence.LibraryRepository
import org.gotson.komga.domain.persistence.ReadListRepository
import org.gotson.komga.domain.persistence.SeriesMetadataRepository
import org.gotson.komga.domain.persistence.SeriesRepository
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.junit.jupiter.SpringExtension

@ExtendWith(SpringExtension::class)
@SpringBootTest
class ReadListMatcherTest(
  @Autowired private val seriesLifecycle: SeriesLifecycle,
  @Autowired private val seriesRepository: SeriesRepository,
  @Autowired private val seriesMetadataRepository: SeriesMetadataRepository,
  @Autowired private val readListLifecycle: ReadListLifecycle,
  @Autowired private val readListRepository: ReadListRepository,
  @Autowired private val libraryRepository: LibraryRepository,
  @Autowired private val bookMetadataRepository: BookMetadataRepository,
  @Autowired private val readListMatcher: ReadListMatcher,
) {

  private val library = makeLibrary()

  @MockkBean
  private lateinit var mockTaskEmitter: TaskEmitter

  @BeforeAll
  fun `setup library`() {
    libraryRepository.insert(library)
  }

  @BeforeEach
  fun beforeEach() {
    every { mockTaskEmitter.refreshBookMetadata(any(), any()) } just Runs
  }

  @AfterAll
  fun `teardown library`() {
    libraryRepository.deleteAll()
  }

  @AfterEach
  fun `clear repository`() {
    readListRepository.deleteAll()
    seriesLifecycle.deleteMany(seriesRepository.findAll())
  }

  @Test
  fun `given request with existing series and books when matching then result contains a read list with all books`() {
    // given
    val booksSeries1 = listOf(
      makeBook("book1", libraryId = library.id),
      makeBook("book5", libraryId = library.id),
    )
    makeSeries(name = "batman", libraryId = library.id).let { s ->
      seriesLifecycle.createSeries(s)
      seriesLifecycle.addBooks(s, booksSeries1)
      seriesLifecycle.sortBooks(s)
      seriesMetadataRepository.findById(s.id).let {
        seriesMetadataRepository.update(it.copy(title = "Batman: White Knight"))
      }
    }

    val booksSeries2 = listOf(
      makeBook("book1", libraryId = library.id),
      makeBook("book2", libraryId = library.id),
    )
    makeSeries(name = "joker", libraryId = library.id).let { s ->
      seriesLifecycle.createSeries(s)
      seriesLifecycle.addBooks(s, booksSeries2)
      seriesLifecycle.sortBooks(s)

      bookMetadataRepository.findById(booksSeries2[0].id).let {
        bookMetadataRepository.update(it.copy(number = "0025"))
      }
    }

    val request = ReadListRequest(
      name = "readlist",
      books = listOf(
        ReadListRequestBook(series = "Batman: White Knight", number = "1"),
        ReadListRequestBook(series = "joker", number = "02"),
        ReadListRequestBook(series = "Batman: White Knight", number = "2"),
        ReadListRequestBook(series = "joker", number = "25"),
      ),
    )

    // when
    val result = readListMatcher.matchReadListRequest(request)

    // then
    with(result) {
      assertThat(readList).isNotNull
      assertThat(unmatchedBooks).isEmpty()
      assertThat(errorCode).isBlank
      with(readList!!) {
        assertThat(name).isEqualTo(request.name)
        assertThat(bookIds).hasSize(4)
        assertThat(bookIds).containsExactlyEntriesOf(
          mapOf(
            0 to booksSeries1[0].id,
            1 to booksSeries2[1].id,
            2 to booksSeries1[1].id,
            3 to booksSeries2[0].id,
          ),
        )
      }
    }
  }

  @Test
  fun `given request with existing read list when matching then result has no readlist and appropriate error code`() {
    // given
    readListLifecycle.addReadList(
      ReadList(name = "my ReadList"),
    )

    val request = ReadListRequest(
      name = "my readlist",
      books = listOf(
        ReadListRequestBook(series = "batman: white knight", number = "1"),
        ReadListRequestBook(series = "joker", number = "2"),
        ReadListRequestBook(series = "BATMAN: WHITE KNIGHT", number = "2"),
        ReadListRequestBook(series = "joker", number = "25"),
      ),
    )

    // when
    val result = readListMatcher.matchReadListRequest(request)

    // then
    with(result) {
      assertThat(readList).isNull()
      assertThat(errorCode).isEqualTo("ERR_1009")
      assertThat(unmatchedBooks.map { it.book }).containsExactlyElementsOf(request.books)
    }
  }

  @Test
  fun `given request and some matching series or books when matching then returns result with appropriate error codes`() {
    // given
    val booksSeries1 = listOf(
      makeBook("book1", libraryId = library.id),
      makeBook("book5", libraryId = library.id),
    )
    makeSeries(name = "batman", libraryId = library.id).let { s ->
      seriesLifecycle.createSeries(s)
      seriesLifecycle.addBooks(s, booksSeries1)
      seriesLifecycle.sortBooks(s)

      bookMetadataRepository.findById(booksSeries1[0].id).let {
        bookMetadataRepository.update(it.copy(number = "2"))
      }
    }

    val booksSeries2 = listOf(
      makeBook("book1", libraryId = library.id),
      makeBook("book2", libraryId = library.id),
    )
    makeSeries(name = "joker", libraryId = library.id).let { s ->
      seriesLifecycle.createSeries(s)
      seriesLifecycle.addBooks(s, booksSeries2)
      seriesLifecycle.sortBooks(s)
    }
    makeSeries(name = "joker", libraryId = library.id).let { s ->
      seriesLifecycle.createSeries(s)
    }

    val request = ReadListRequest(
      name = "readlist",
      books = listOf(
        ReadListRequestBook(series = "tokyo ghost", number = "1"),
        ReadListRequestBook(series = "batman", number = "3"),
        ReadListRequestBook(series = "joker", number = "3"),
        ReadListRequestBook(series = "batman", number = "2"),
      ),
    )

    // when
    val result = readListMatcher.matchReadListRequest(request)

    // then
    with(result) {
      assertThat(readList).isNull()
      assertThat(errorCode).isEqualTo("ERR_1010")

      assertThat(unmatchedBooks).hasSize(4)

      assertThat(unmatchedBooks[0].book).isEqualTo(request.books[0])
      assertThat(unmatchedBooks[0].errorCode).isEqualTo("ERR_1012")

      assertThat(unmatchedBooks[1].book).isEqualTo(request.books[1])
      assertThat(unmatchedBooks[1].errorCode).isEqualTo("ERR_1014")

      assertThat(unmatchedBooks[2].book).isEqualTo(request.books[2])
      assertThat(unmatchedBooks[2].errorCode).isEqualTo("ERR_1011")

      assertThat(unmatchedBooks[3].book).isEqualTo(request.books[3])
      assertThat(unmatchedBooks[3].errorCode).isEqualTo("ERR_1013")
    }
  }

  @Test
  fun `given request with duplicate books when matching then returns result with appropriate error codes`() {
    // given
    val booksSeries1 = listOf(
      makeBook("book1", libraryId = library.id),
      makeBook("book2", libraryId = library.id),
    )
    makeSeries(name = "batman", libraryId = library.id).let { s ->
      seriesLifecycle.createSeries(s)
      seriesLifecycle.addBooks(s, booksSeries1)
      seriesLifecycle.sortBooks(s)
    }

    val request = ReadListRequest(
      name = "readlist",
      books = listOf(
        ReadListRequestBook(series = "batman", number = "1"),
        ReadListRequestBook(series = "batman", number = "2"),
        ReadListRequestBook(series = "batman", number = "2"),
      ),
    )

    // when
    val result = readListMatcher.matchReadListRequest(request)

    // then
    with(result) {
      assertThat(readList).isNotNull
      with(readList!!) {
        assertThat(name).isEqualTo(request.name)
        assertThat(bookIds).hasSize(2)
        assertThat(bookIds).containsExactlyEntriesOf(
          mapOf(
            0 to booksSeries1[0].id,
            1 to booksSeries1[1].id,
          ),
        )
      }

      assertThat(errorCode).isBlank

      assertThat(unmatchedBooks).hasSize(1)

      assertThat(unmatchedBooks[0].book).isEqualTo(request.books[2])
      assertThat(unmatchedBooks[0].errorCode).isEqualTo("ERR_1023")
    }
  }
}
