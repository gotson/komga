package org.gotson.komga.domain.service

import com.ninjasquad.springmockk.MockkBean
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import org.assertj.core.api.Assertions.assertThat
import org.gotson.komga.application.tasks.TaskEmitter
import org.gotson.komga.domain.model.Book
import org.gotson.komga.domain.model.ReadList
import org.gotson.komga.domain.model.ReadListRequest
import org.gotson.komga.domain.model.ReadListRequestBook
import org.gotson.komga.domain.model.ReadListRequestBookMatches
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
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import java.time.LocalDate

@SpringBootTest
class ReadListMatcherTest(
  @Autowired private val seriesLifecycle: SeriesLifecycle,
  @Autowired private val seriesRepository: SeriesRepository,
  @Autowired private val seriesMetadataRepository: SeriesMetadataRepository,
  @Autowired private val readListLifecycle: ReadListLifecycle,
  @Autowired private val readListRepository: ReadListRepository,
  @Autowired private val libraryRepository: LibraryRepository,
  @Autowired private val bookMetadataRepository: BookMetadataRepository,
  @Autowired private val seriesMetadataLifecycle: SeriesMetadataLifecycle,
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
    every { mockTaskEmitter.refreshBookMetadata(any<Book>(), any()) } just Runs
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

  @Nested
  inner class Match {
    private fun Collection<ReadListRequestBookMatches>.mapIds() =
      map {
        it.matches
          .mapKeys { (series, _) -> series.id }
          .mapValues { (_, books) -> books.map { book -> book.id } }
      }

    @Test
    fun `given request with existing series and books when matching then all requests are matched with a single result`() {
      // given
      val booksSeries1 =
        listOf(
          makeBook("book1", libraryId = library.id),
          makeBook("book5", libraryId = library.id),
        )
      val series1 =
        makeSeries(name = "batman", libraryId = library.id).also { s ->
          seriesLifecycle.createSeries(s)
          seriesLifecycle.addBooks(s, booksSeries1)
          seriesLifecycle.sortBooks(s)
          seriesMetadataRepository.findById(s.id).let {
            seriesMetadataRepository.update(it.copy(title = "Batman: White Knight"))
          }
        }

      val booksSeries2 =
        listOf(
          makeBook("book1", libraryId = library.id),
          makeBook("book2", libraryId = library.id),
        )
      val series2 =
        makeSeries(name = "joker", libraryId = library.id).also { s ->
          seriesLifecycle.createSeries(s)
          seriesLifecycle.addBooks(s, booksSeries2)
          seriesLifecycle.sortBooks(s)

          bookMetadataRepository.findById(booksSeries2[0].id).let {
            bookMetadataRepository.update(it.copy(number = "0025"))
          }
        }

      val request =
        ReadListRequest(
          name = "readlist request",
          books =
            listOf(
              ReadListRequestBook(series = setOf("Batman: White Knight"), number = "1"),
              ReadListRequestBook(series = setOf("joker"), number = "02"),
              ReadListRequestBook(series = setOf("Batman: White Knight"), number = "2"),
              ReadListRequestBook(series = setOf("joker"), number = "25"),
            ),
        )

      // when
      val result = readListMatcher.matchReadListRequest(request)

      // then
      with(result) {
        with(readListMatch) {
          assertThat(name).isEqualTo(request.name)
          assertThat(errorCode).isBlank
        }
        assertThat(requests).hasSize(4)
        assertThat(requests.map { it.request }).containsExactlyElementsOf(request.books)
        assertThat(requests.mapIds()).isEqualTo(
          listOf(
            mapOf(series1.id to listOf(booksSeries1[0].id)),
            mapOf(series2.id to listOf(booksSeries2[1].id)),
            mapOf(series1.id to listOf(booksSeries1[1].id)),
            mapOf(series2.id to listOf(booksSeries2[0].id)),
          ),
        )
      }
    }

    @Test
    fun `given request with existing read list when matching then result has no readlist name and appropriate error code but correct matches`() {
      // given
      val booksSeries1 =
        listOf(
          makeBook("book1", libraryId = library.id),
          makeBook("book5", libraryId = library.id),
        )
      val series1 =
        makeSeries(name = "batman", libraryId = library.id).also { s ->
          seriesLifecycle.createSeries(s)
          seriesLifecycle.addBooks(s, booksSeries1)
          seriesLifecycle.sortBooks(s)
          seriesMetadataRepository.findById(s.id).let {
            seriesMetadataRepository.update(it.copy(title = "Batman: White Knight"))
          }
        }

      val booksSeries2 =
        listOf(
          makeBook("book1", libraryId = library.id),
          makeBook("book2", libraryId = library.id),
        )
      val series2 =
        makeSeries(name = "joker", libraryId = library.id).also { s ->
          seriesLifecycle.createSeries(s)
          seriesLifecycle.addBooks(s, booksSeries2)
          seriesLifecycle.sortBooks(s)

          bookMetadataRepository.findById(booksSeries2[0].id).let {
            bookMetadataRepository.update(it.copy(number = "0025"))
          }
        }

      readListLifecycle.addReadList(
        ReadList(name = "my ReadList"),
      )

      val request =
        ReadListRequest(
          name = "my readlist",
          books =
            listOf(
              ReadListRequestBook(series = setOf("batman: white knight"), number = "1"),
              ReadListRequestBook(series = setOf("joker"), number = "2"),
              ReadListRequestBook(series = setOf("BATMAN: WHITE KNIGHT"), number = "2"),
              ReadListRequestBook(series = setOf("joker"), number = "25"),
            ),
        )

      // when
      val result = readListMatcher.matchReadListRequest(request)

      // then
      with(result) {
        with(readListMatch) {
          assertThat(name).isEqualTo(request.name)
          assertThat(errorCode).isEqualTo("ERR_1009")
        }
        assertThat(requests).hasSize(4)
        assertThat(requests.map { it.request }).containsExactlyElementsOf(request.books)
        assertThat(requests.mapIds()).isEqualTo(
          listOf(
            mapOf(series1.id to listOf(booksSeries1[0].id)),
            mapOf(series2.id to listOf(booksSeries2[1].id)),
            mapOf(series1.id to listOf(booksSeries1[1].id)),
            mapOf(series2.id to listOf(booksSeries2[0].id)),
          ),
        )
      }
    }

    @Test
    fun `given request and some matching series or books when matching then returns all matches`() {
      // given
      val booksSeries1 =
        listOf(
          makeBook("book1", libraryId = library.id),
          makeBook("book5", libraryId = library.id),
        )
      val series1 =
        makeSeries(name = "batman", libraryId = library.id).also { s ->
          seriesLifecycle.createSeries(s)
          seriesLifecycle.addBooks(s, booksSeries1)
          seriesLifecycle.sortBooks(s)

          bookMetadataRepository.findById(booksSeries1[0].id).let {
            bookMetadataRepository.update(it.copy(number = "2"))
          }
        }

      val booksSeries2 =
        listOf(
          makeBook("book1", libraryId = library.id),
          makeBook("book2", libraryId = library.id),
        )
      val series2 =
        makeSeries(name = "joker", libraryId = library.id).also { s ->
          seriesLifecycle.createSeries(s)
          seriesLifecycle.addBooks(s, booksSeries2)
          seriesLifecycle.sortBooks(s)
        }
      makeSeries(name = "joker", libraryId = library.id).also { s ->
        seriesLifecycle.createSeries(s)
      }

      val request =
        ReadListRequest(
          name = "readlist",
          books =
            listOf(
              ReadListRequestBook(series = setOf("tokyo ghost"), number = "1"),
              ReadListRequestBook(series = setOf("batman"), number = "3"),
              ReadListRequestBook(series = setOf("joker"), number = "2"),
              ReadListRequestBook(series = setOf("batman"), number = "2"),
            ),
        )

      // when
      val result = readListMatcher.matchReadListRequest(request)

      // then
      with(result) {
        with(readListMatch) {
          assertThat(name).isEqualTo(request.name)
          assertThat(errorCode).isBlank
        }
        assertThat(requests).hasSize(4)
        assertThat(requests.map { it.request }).containsExactlyElementsOf(request.books)
        assertThat(requests.mapIds()).isEqualTo(
          listOf(
            emptyMap(),
            emptyMap(),
            mapOf(series2.id to listOf(booksSeries2[1].id)),
            mapOf(series1.id to listOf(booksSeries1[0].id, booksSeries1[1].id)),
          ),
        )
      }
    }

    /**
     * Creates a series with a single book, and aggregates its metadata so the release date is available for matching.
     */
    private fun makeSeriesWithFirstIssue(
      name: String,
      title: String,
      releaseDate: LocalDate,
    ) = makeSeries(name = name, libraryId = library.id).also { s ->
      val books = listOf(makeBook("book1", libraryId = library.id))
      seriesLifecycle.createSeries(s)
      seriesLifecycle.addBooks(s, books)
      seriesLifecycle.sortBooks(s)
      seriesMetadataRepository.findById(s.id).let {
        seriesMetadataRepository.update(it.copy(title = title))
      }
      bookMetadataRepository.findById(books[0].id).let {
        bookMetadataRepository.update(it.copy(number = "1", releaseDate = releaseDate))
      }
      seriesMetadataLifecycle.aggregateMetadata(s)
    }

    @Test
    fun `given series with volume in title when matching with a series year then the matching volume comes first`() {
      // given
      val seriesOriginal = makeSeriesWithFirstIssue("batman-1940", "Batman", LocalDate.of(1940, 4, 25))
      val seriesRebirth = makeSeriesWithFirstIssue("batman-2016", "Batman (2016)", LocalDate.of(2016, 8, 3))

      val request =
        ReadListRequest(
          name = "readlist",
          books = listOf(ReadListRequestBook(series = setOf("Batman (2016)", "Batman"), number = "1", seriesYear = 2016)),
        )

      // when
      val result = readListMatcher.matchReadListRequest(request)

      // then
      assertThat(result.requests).hasSize(1)
      val matchedSeriesIds =
        result.requests
          .first()
          .matches.keys
          .map { it.id }
      assertThat(matchedSeriesIds).containsExactly(seriesRebirth.id, seriesOriginal.id)
    }

    @Test
    fun `given series sharing the same title when matching with a series year then the matching first issue comes first`() {
      // given
      val seriesOriginal = makeSeriesWithFirstIssue("batman-1940", "Batman", LocalDate.of(1940, 4, 25))
      val seriesRebirth = makeSeriesWithFirstIssue("batman-2016", "Batman", LocalDate.of(2016, 8, 3))

      val request =
        ReadListRequest(
          name = "readlist",
          books = listOf(ReadListRequestBook(series = setOf("Batman (2016)", "Batman"), number = "1", seriesYear = 2016)),
        )

      // when
      val result = readListMatcher.matchReadListRequest(request)

      // then
      assertThat(result.requests).hasSize(1)
      val matchedSeriesIds =
        result.requests
          .first()
          .matches.keys
          .map { it.id }
      assertThat(matchedSeriesIds).containsExactly(seriesRebirth.id, seriesOriginal.id)
    }

    @Test
    fun `given series sharing the same title when matching with an older series year then the matching first issue comes first`() {
      // given
      val seriesOriginal = makeSeriesWithFirstIssue("batman-1940", "Batman", LocalDate.of(1940, 4, 25))
      val seriesRebirth = makeSeriesWithFirstIssue("batman-2016", "Batman", LocalDate.of(2016, 8, 3))

      val request =
        ReadListRequest(
          name = "readlist",
          books = listOf(ReadListRequestBook(series = setOf("Batman (1940)", "Batman"), number = "1", seriesYear = 1940)),
        )

      // when
      val result = readListMatcher.matchReadListRequest(request)

      // then
      assertThat(result.requests).hasSize(1)
      val matchedSeriesIds =
        result.requests
          .first()
          .matches.keys
          .map { it.id }
      assertThat(matchedSeriesIds).containsExactly(seriesOriginal.id, seriesRebirth.id)
    }

    @Test
    fun `given series sharing a title when matching without a series year then all matches are kept`() {
      // given
      val seriesOriginal = makeSeriesWithFirstIssue("batman-1940", "Batman", LocalDate.of(1940, 4, 25))
      val seriesRebirth = makeSeriesWithFirstIssue("batman-2016", "Batman (2016)", LocalDate.of(2016, 8, 3))

      val request =
        ReadListRequest(
          name = "readlist",
          books = listOf(ReadListRequestBook(series = setOf("Batman (2016)", "Batman"), number = "1")),
        )

      // when
      val result = readListMatcher.matchReadListRequest(request)

      // then
      assertThat(result.requests).hasSize(1)
      val matchedSeriesIds =
        result.requests
          .first()
          .matches.keys
          .map { it.id }
      assertThat(matchedSeriesIds).containsExactlyInAnyOrder(seriesOriginal.id, seriesRebirth.id)
    }

    @Test
    fun `given series sharing the same title and no series year when matching with an issue year then the matching release date comes first`() {
      // given
      val seriesOriginal = makeSeriesWithFirstIssue("batman-1940", "Batman", LocalDate.of(1940, 4, 25))
      val seriesRebirth = makeSeriesWithFirstIssue("batman-2016", "Batman", LocalDate.of(2016, 8, 3))

      val request =
        ReadListRequest(
          name = "readlist",
          books = listOf(ReadListRequestBook(series = setOf("Batman"), number = "1", issueYear = 2016)),
        )

      // when
      val result = readListMatcher.matchReadListRequest(request)

      // then
      assertThat(result.requests).hasSize(1)
      val matchedSeriesIds =
        result.requests
          .first()
          .matches.keys
          .map { it.id }
      assertThat(matchedSeriesIds).containsExactly(seriesRebirth.id, seriesOriginal.id)
    }

    @Test
    fun `given series sharing the same title when matching without any year then matches are left untouched`() {
      // given
      val seriesOriginal = makeSeriesWithFirstIssue("batman-1940", "Batman", LocalDate.of(1940, 4, 25))
      val seriesRebirth = makeSeriesWithFirstIssue("batman-2016", "Batman", LocalDate.of(2016, 8, 3))

      val request =
        ReadListRequest(
          name = "readlist",
          books = listOf(ReadListRequestBook(series = setOf("Batman"), number = "1")),
        )

      // when
      val result = readListMatcher.matchReadListRequest(request)

      // then
      assertThat(result.requests).hasSize(1)
      val matchedSeriesIds =
        result.requests
          .first()
          .matches.keys
          .map { it.id }
      assertThat(matchedSeriesIds).containsExactlyInAnyOrder(seriesOriginal.id, seriesRebirth.id)
    }

    @Test
    fun `given books sharing the same number when matching with an issue year then the matching book comes first`() {
      // given
      val books =
        listOf(
          makeBook("book1", libraryId = library.id),
          makeBook("book2", libraryId = library.id),
        )
      makeSeries(name = "batman", libraryId = library.id).also { s ->
        seriesLifecycle.createSeries(s)
        seriesLifecycle.addBooks(s, books)
        seriesLifecycle.sortBooks(s)
        seriesMetadataRepository.findById(s.id).let {
          seriesMetadataRepository.update(it.copy(title = "Batman"))
        }
        bookMetadataRepository.findById(books[0].id).let {
          bookMetadataRepository.update(it.copy(number = "1", releaseDate = LocalDate.of(1940, 4, 25)))
        }
        bookMetadataRepository.findById(books[1].id).let {
          bookMetadataRepository.update(it.copy(number = "1", releaseDate = LocalDate.of(2016, 8, 3)))
        }
        seriesMetadataLifecycle.aggregateMetadata(s)
      }

      val request =
        ReadListRequest(
          name = "readlist",
          books = listOf(ReadListRequestBook(series = setOf("Batman"), number = "1", issueYear = 2016)),
        )

      // when
      val result = readListMatcher.matchReadListRequest(request)

      // then
      assertThat(result.requests).hasSize(1)
      val matchedBookIds =
        result.requests
          .first()
          .matches.values
          .first()
          .map { it.id }
      assertThat(matchedBookIds).containsExactly(books[1].id, books[0].id)
    }
  }
}
