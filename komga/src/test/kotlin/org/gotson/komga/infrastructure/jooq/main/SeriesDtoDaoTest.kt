package org.gotson.komga.infrastructure.jooq.main

import com.ninjasquad.springmockk.MockkBean
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import org.assertj.core.api.Assertions.assertThat
import org.gotson.komga.domain.model.Author
import org.gotson.komga.domain.model.KomgaUser
import org.gotson.komga.domain.model.ReadProgress
import org.gotson.komga.domain.model.ReadStatus
import org.gotson.komga.domain.model.SearchCondition
import org.gotson.komga.domain.model.SearchContext
import org.gotson.komga.domain.model.SearchOperator
import org.gotson.komga.domain.model.SeriesMetadata
import org.gotson.komga.domain.model.SeriesSearch
import org.gotson.komga.domain.model.makeBook
import org.gotson.komga.domain.model.makeLibrary
import org.gotson.komga.domain.model.makeSeries
import org.gotson.komga.domain.persistence.BookMetadataRepository
import org.gotson.komga.domain.persistence.BookRepository
import org.gotson.komga.domain.persistence.KomgaUserRepository
import org.gotson.komga.domain.persistence.LibraryRepository
import org.gotson.komga.domain.persistence.ReadProgressRepository
import org.gotson.komga.domain.persistence.SeriesMetadataRepository
import org.gotson.komga.domain.persistence.SeriesRepository
import org.gotson.komga.domain.service.KomgaUserLifecycle
import org.gotson.komga.domain.service.LibraryLifecycle
import org.gotson.komga.domain.service.SeriesLifecycle
import org.gotson.komga.domain.service.SeriesMetadataLifecycle
import org.gotson.komga.infrastructure.jooq.UnpagedSorted
import org.gotson.komga.infrastructure.search.SearchIndexLifecycle
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.ApplicationEventPublisher
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import java.time.LocalDate
import java.time.LocalDateTime

@SpringBootTest
class SeriesDtoDaoTest(
  @Autowired private val seriesDtoDao: SeriesDtoDao,
  @Autowired private val bookRepository: BookRepository,
  @Autowired private val seriesRepository: SeriesRepository,
  @Autowired private val seriesMetadataRepository: SeriesMetadataRepository,
  @Autowired private val bookMetadataRepository: BookMetadataRepository,
  @Autowired private val seriesLifecycle: SeriesLifecycle,
  @Autowired private val seriesMetadataLifecycle: SeriesMetadataLifecycle,
  @Autowired private val libraryRepository: LibraryRepository,
  @Autowired private val libraryLifecycle: LibraryLifecycle,
  @Autowired private val readProgressRepository: ReadProgressRepository,
  @Autowired private val userRepository: KomgaUserRepository,
  @Autowired private val userLifecycle: KomgaUserLifecycle,
  @Autowired private val searchIndexLifecycle: SearchIndexLifecycle,
) {
  private val library = makeLibrary()
  private val user = KomgaUser("user@example.org", "")

  @MockkBean
  private lateinit var mockEventPublisher: ApplicationEventPublisher

  @BeforeAll
  fun setup() {
    every { mockEventPublisher.publishEvent(any()) } just Runs
    libraryRepository.insert(library)
    userRepository.insert(user)
  }

  @BeforeEach
  fun resetMocks() {
    every { mockEventPublisher.publishEvent(any()) } just Runs
  }

  @AfterEach
  fun deleteSeries() {
    seriesLifecycle.deleteMany(seriesRepository.findAll())
    searchIndexLifecycle.rebuildIndex()
  }

  @AfterAll
  fun tearDown() {
    every { mockEventPublisher.publishEvent(any()) } just Runs
    userRepository.findAll().forEach {
      userLifecycle.deleteUser(it)
    }
    libraryRepository.findAll().forEach {
      libraryLifecycle.deleteLibrary(it)
    }
  }

  private fun setupSeries() {
    (1..4)
      .map { makeSeries("$it", library.id) }
      .forEach { series ->
        val created = seriesLifecycle.createSeries(series)
        seriesLifecycle.addBooks(
          created,
          (1..3).map {
            makeBook("$it", seriesId = created.id, libraryId = library.id)
          },
        )
        seriesLifecycle.sortBooks(created)
      }

    val series = seriesRepository.findAll().sortedBy { it.name }
    // series "1": only in progress books
    series.elementAt(0).let {
      bookRepository.findAllBySeriesId(it.id).forEach { readProgressRepository.save(ReadProgress(it.id, user.id, 5, false)) }
    }
    // series "2": only read books
    series.elementAt(1).let {
      bookRepository.findAllBySeriesId(it.id).forEach { readProgressRepository.save(ReadProgress(it.id, user.id, 5, true)) }
    }
    // series "3": only unread books
    // series "4": read, unread, and in progress
    series.elementAt(3).let {
      val books = bookRepository.findAllBySeriesId(it.id).sortedBy { it.name }
      books.elementAt(0).let { readProgressRepository.save(ReadProgress(it.id, user.id, 5, false)) }
      books.elementAt(1).let { readProgressRepository.save(ReadProgress(it.id, user.id, 5, true)) }
    }
  }

  @Nested
  inner class SortCriteria {
    @Test
    fun `given series when sorting by title sort then results are ordered`() {
      // given
      seriesLifecycle.createSeries(makeSeries("Éb", library.id))
      seriesLifecycle.createSeries(makeSeries("Ea", library.id))
      seriesLifecycle.createSeries(makeSeries("Ec", library.id))

      searchIndexLifecycle.rebuildIndex()
      Thread.sleep(500) // index rebuild is done asynchronously, and need a slight delay to be updated

      // when
      val found =
        seriesDtoDao
          .findAll(
            SeriesSearch(),
            SearchContext(user),
            UnpagedSorted(Sort.by("metadata.titleSort")),
          ).content

      // then
      assertThat(found.map { it.metadata.title }).containsExactly("Ea", "Éb", "Ec")
    }
  }

  @Nested
  inner class ReadProgress {
    @Test
    fun `given series in various read status when searching for read series then only read series are returned`() {
      // given
      setupSeries()

      // when
      val found =
        seriesDtoDao
          .findAll(
            SeriesSearch(SearchCondition.ReadStatus(SearchOperator.Is(ReadStatus.READ))),
            SearchContext(user),
            PageRequest.of(0, 20),
          ).sortedBy { it.name }

      // then
      assertThat(found).hasSize(1)

      assertThat(found.first().booksReadCount).isEqualTo(3)
      assertThat(found.first().name).isEqualTo("2")
    }

    @Test
    fun `given series in various read status when searching for unread series then only unread series are returned`() {
      // given
      setupSeries()

      // when
      val found =
        seriesDtoDao
          .findAll(
            SeriesSearch(SearchCondition.ReadStatus(SearchOperator.Is(ReadStatus.UNREAD))),
            SearchContext(user),
            PageRequest.of(0, 20),
          ).sortedBy { it.name }

      // then
      assertThat(found).hasSize(1)

      assertThat(found.first().booksUnreadCount).isEqualTo(3)
      assertThat(found.first().name).isEqualTo("3")
    }

    @Test
    fun `given series in various read status when searching for in progress series then only in progress series are returned`() {
      // given
      setupSeries()

      // when
      val found =
        seriesDtoDao
          .findAll(
            SeriesSearch(SearchCondition.ReadStatus(SearchOperator.Is(ReadStatus.IN_PROGRESS))),
            SearchContext(user),
            PageRequest.of(0, 20),
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
      val found =
        seriesDtoDao
          .findAll(
            SeriesSearch(
              SearchCondition.AnyOfSeries(
                SearchCondition.ReadStatus(SearchOperator.Is(ReadStatus.READ)),
                SearchCondition.ReadStatus(SearchOperator.Is(ReadStatus.UNREAD)),
              ),
            ),
            SearchContext(user),
            PageRequest.of(0, 20),
          ).sortedBy { it.name }

      // then
      assertThat(found).hasSize(2)
      assertThat(found.map { it.name }).containsExactlyInAnyOrder("2", "3")
    }

    @Test
    fun `given series in various read status when searching for read and in progress series then only matching series are returned`() {
      // given
      setupSeries()

      // when
      val found =
        seriesDtoDao
          .findAll(
            SeriesSearch(
              SearchCondition.AnyOfSeries(
                SearchCondition.ReadStatus(SearchOperator.Is(ReadStatus.READ)),
                SearchCondition.ReadStatus(SearchOperator.Is(ReadStatus.IN_PROGRESS)),
              ),
            ),
            SearchContext(user),
            PageRequest.of(0, 20),
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
      val found =
        seriesDtoDao
          .findAll(
            SeriesSearch(
              SearchCondition.AnyOfSeries(
                SearchCondition.ReadStatus(SearchOperator.Is(ReadStatus.UNREAD)),
                SearchCondition.ReadStatus(SearchOperator.Is(ReadStatus.IN_PROGRESS)),
              ),
            ),
            SearchContext(user),
            PageRequest.of(0, 20),
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
      val found =
        seriesDtoDao
          .findAll(
            SeriesSearch(
              SearchCondition.AnyOfSeries(
                SearchCondition.ReadStatus(SearchOperator.Is(ReadStatus.READ)),
                SearchCondition.ReadStatus(SearchOperator.Is(ReadStatus.IN_PROGRESS)),
                SearchCondition.ReadStatus(SearchOperator.Is(ReadStatus.UNREAD)),
              ),
            ),
            SearchContext(user),
            PageRequest.of(0, 20),
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
      val found =
        seriesDtoDao
          .findAll(
            SearchContext(user),
            PageRequest.of(0, 20),
          ).sortedBy { it.name }

      // then
      assertThat(found).hasSize(4)
      assertThat(found.map { it.name }).containsExactlyInAnyOrder("1", "2", "3", "4")
    }
  }

  @Nested
  inner class FullTextSearch {
    @Test
    fun `given series when searching by term then results are ordered by rank`() {
      // given
      seriesLifecycle.createSeries(makeSeries("The incredible adventures of Batman, the man who is also a bat!", library.id))
      seriesLifecycle.createSeries(makeSeries("Batman and Robin", library.id))
      seriesLifecycle.createSeries(makeSeries("Batman", library.id))

      searchIndexLifecycle.rebuildIndex()
      Thread.sleep(500) // index rebuild is done asynchronously, and need a slight delay to be updated

      // when
      val found =
        seriesDtoDao
          .findAll(
            SeriesSearch(fullTextSearch = "batman"),
            SearchContext(user),
            UnpagedSorted(Sort.by("relevance")),
          ).content

      // then
      assertThat(found).hasSize(3)
      assertThat(found.map { it.metadata.title }).containsExactly("Batman", "Batman and Robin", "The incredible adventures of Batman, the man who is also a bat!")
    }

    @Test
    fun `given series when searching by publisher then results are matched`() {
      // given
      val series = seriesLifecycle.createSeries(makeSeries("Batman", library.id))
      seriesLifecycle.createSeries(makeSeries("Batman and Robin", library.id))

      seriesMetadataRepository.findById(series.id).let {
        seriesMetadataRepository.update(it.copy(publisher = "Vertigo"))
      }

      searchIndexLifecycle.rebuildIndex()
      Thread.sleep(500) // index rebuild is done asynchronously, and need a slight delay to be updated

      // when
      val found =
        seriesDtoDao
          .findAll(
            SeriesSearch(fullTextSearch = "publisher:vertigo"),
            SearchContext(user),
            UnpagedSorted(Sort.by("relevance")),
          ).content

      // then
      assertThat(found).hasSize(1)
      assertThat(found.map { it.metadata.title }).containsExactly("Batman")
    }

    @Test
    fun `given series when searching by status then results are matched`() {
      // given
      val series = seriesLifecycle.createSeries(makeSeries("Batman", library.id))
      seriesLifecycle.createSeries(makeSeries("Batman and Robin", library.id))

      seriesMetadataRepository.findById(series.id).let {
        seriesMetadataRepository.update(it.copy(status = SeriesMetadata.Status.HIATUS))
      }

      searchIndexLifecycle.rebuildIndex()
      Thread.sleep(500) // index rebuild is done asynchronously, and need a slight delay to be updated

      // when
      val found =
        seriesDtoDao
          .findAll(
            SeriesSearch(fullTextSearch = "status:hiatus"),
            SearchContext(user),
            UnpagedSorted(Sort.by("relevance")),
          ).content

      // then
      assertThat(found).hasSize(1)
      assertThat(found.map { it.metadata.title }).containsExactly("Batman")
    }

    @Test
    fun `given series when searching by reading direction then results are matched`() {
      // given
      val series = seriesLifecycle.createSeries(makeSeries("Batman", library.id))
      seriesLifecycle.createSeries(makeSeries("Batman and Robin", library.id))

      seriesMetadataRepository.findById(series.id).let {
        seriesMetadataRepository.update(it.copy(readingDirection = SeriesMetadata.ReadingDirection.LEFT_TO_RIGHT))
      }

      searchIndexLifecycle.rebuildIndex()
      Thread.sleep(500) // index rebuild is done asynchronously, and need a slight delay to be updated

      // when
      val found =
        seriesDtoDao
          .findAll(
            SeriesSearch(fullTextSearch = "reading_direction:left_to_right"),
            SearchContext(user),
            UnpagedSorted(Sort.by("relevance")),
          ).content

      // then
      assertThat(found).hasSize(1)
      assertThat(found.map { it.metadata.title }).containsExactly("Batman")
    }

    @Test
    fun `given series when searching by age rating then results are matched`() {
      // given
      val series = seriesLifecycle.createSeries(makeSeries("Batman", library.id))
      seriesLifecycle.createSeries(makeSeries("Batman and Robin", library.id))

      seriesMetadataRepository.findById(series.id).let {
        seriesMetadataRepository.update(it.copy(ageRating = 12))
      }

      searchIndexLifecycle.rebuildIndex()
      Thread.sleep(500) // index rebuild is done asynchronously, and need a slight delay to be updated

      // when
      val found =
        seriesDtoDao
          .findAll(
            SeriesSearch(fullTextSearch = "age_rating:12"),
            SearchContext(user),
            UnpagedSorted(Sort.by("relevance")),
          ).content

      // then
      assertThat(found).hasSize(1)
      assertThat(found.map { it.metadata.title }).containsExactly("Batman")
    }

    @Test
    fun `given series when searching by language then results are matched`() {
      // given
      val series = seriesLifecycle.createSeries(makeSeries("Batman", library.id))
      seriesLifecycle.createSeries(makeSeries("Batman and Robin", library.id))

      seriesMetadataRepository.findById(series.id).let {
        seriesMetadataRepository.update(it.copy(language = "en-us"))
      }

      searchIndexLifecycle.rebuildIndex()
      Thread.sleep(500) // index rebuild is done asynchronously, and need a slight delay to be updated

      // when
      val found =
        seriesDtoDao
          .findAll(
            SeriesSearch(fullTextSearch = "language:en-us"),
            SearchContext(user),
            UnpagedSorted(Sort.by("relevance")),
          ).content

      // then
      assertThat(found).hasSize(1)
      assertThat(found.map { it.metadata.title }).containsExactly("Batman")
    }

    @Test
    fun `given series when searching by tags then results are matched`() {
      // given
      val series = seriesLifecycle.createSeries(makeSeries("Batman", library.id))
      val book = makeBook("Batman 01", seriesId = series.id, libraryId = library.id)
      seriesLifecycle.addBooks(series, listOf(book))
      seriesLifecycle.createSeries(makeSeries("Batman and Robin", library.id))

      seriesMetadataRepository.findById(series.id).let {
        seriesMetadataRepository.update(it.copy(tags = setOf("seriestag")))
      }
      bookMetadataRepository.findById(book.id).let {
        bookMetadataRepository.update(it.copy(tags = setOf("booktag")))
      }

      seriesMetadataLifecycle.aggregateMetadata(series)
      searchIndexLifecycle.rebuildIndex()
      Thread.sleep(500) // index rebuild is done asynchronously, and need a slight delay to be updated

      // when
      val foundByBookTag =
        seriesDtoDao
          .findAll(
            SeriesSearch(fullTextSearch = "book_tag:booktag"),
            SearchContext(user),
            UnpagedSorted(Sort.by("relevance")),
          ).content

      val notFoundByBookTag =
        seriesDtoDao
          .findAll(
            SeriesSearch(fullTextSearch = "book_tag:seriestag"),
            SearchContext(user),
            UnpagedSorted(Sort.by("relevance")),
          ).content

      val foundBySeriesTag =
        seriesDtoDao
          .findAll(
            SeriesSearch(fullTextSearch = "series_tag:seriestag"),
            SearchContext(user),
            UnpagedSorted(Sort.by("relevance")),
          ).content

      val notFoundBySeriesTag =
        seriesDtoDao
          .findAll(
            SeriesSearch(fullTextSearch = "series_tag:booktag"),
            SearchContext(user),
            UnpagedSorted(Sort.by("relevance")),
          ).content

      val foundByTagFromBook =
        seriesDtoDao
          .findAll(
            SeriesSearch(fullTextSearch = "tag:booktag"),
            SearchContext(user),
            UnpagedSorted(Sort.by("relevance")),
          ).content

      val foundByTagFromSeries =
        seriesDtoDao
          .findAll(
            SeriesSearch(fullTextSearch = "tag:seriestag"),
            SearchContext(user),
            UnpagedSorted(Sort.by("relevance")),
          ).content

      // then
      assertThat(foundByBookTag).hasSize(1)
      assertThat(foundByBookTag.map { it.metadata.title }).containsExactly("Batman")

      assertThat(notFoundByBookTag).isEmpty()

      assertThat(foundBySeriesTag).hasSize(1)
      assertThat(foundBySeriesTag.map { it.metadata.title }).containsExactly("Batman")

      assertThat(notFoundBySeriesTag).isEmpty()

      assertThat(foundByTagFromBook).hasSize(1)
      assertThat(foundByTagFromBook.map { it.metadata.title }).containsExactly("Batman")

      assertThat(foundByTagFromSeries).hasSize(1)
      assertThat(foundByTagFromSeries.map { it.metadata.title }).containsExactly("Batman")
    }

    @Test
    fun `given series when searching by genre then results are matched`() {
      // given
      val series = seriesLifecycle.createSeries(makeSeries("Batman", library.id))
      seriesLifecycle.createSeries(makeSeries("Batman and Robin", library.id))

      seriesMetadataRepository.findById(series.id).let {
        seriesMetadataRepository.update(it.copy(genres = setOf("action")))
      }

      searchIndexLifecycle.rebuildIndex()
      Thread.sleep(500) // index rebuild is done asynchronously, and need a slight delay to be updated

      // when
      val found =
        seriesDtoDao
          .findAll(
            SeriesSearch(fullTextSearch = "genre:action"),
            SearchContext(user),
            UnpagedSorted(Sort.by("relevance")),
          ).content

      // then
      assertThat(found).hasSize(1)
      assertThat(found.map { it.metadata.title }).containsExactly("Batman")
    }

    @Test
    fun `given series when searching by total book count then results are matched`() {
      // given
      val series = seriesLifecycle.createSeries(makeSeries("Batman", library.id))
      seriesLifecycle.createSeries(makeSeries("Batman and Robin", library.id))

      seriesMetadataRepository.findById(series.id).let {
        seriesMetadataRepository.update(it.copy(totalBookCount = 5))
      }

      searchIndexLifecycle.rebuildIndex()
      Thread.sleep(500) // index rebuild is done asynchronously, and need a slight delay to be updated

      // when
      val found =
        seriesDtoDao
          .findAll(
            SeriesSearch(fullTextSearch = "total_book_count:5"),
            SearchContext(user),
            UnpagedSorted(Sort.by("relevance")),
          ).content

      // then
      assertThat(found).hasSize(1)
      assertThat(found.map { it.metadata.title }).containsExactly("Batman")
    }

    @Test
    fun `given series when searching by book count then results are matched`() {
      // given
      val series = seriesLifecycle.createSeries(makeSeries("Batman", library.id))
      seriesLifecycle.addBooks(
        series,
        listOf(
          makeBook("Batman 01", seriesId = series.id, libraryId = library.id),
          makeBook("Batman 02", seriesId = series.id, libraryId = library.id),
        ),
      )
      seriesLifecycle.sortBooks(series)
      seriesLifecycle.createSeries(makeSeries("Batman and Robin", library.id))

      seriesMetadataRepository.findById(series.id).let {
        seriesMetadataRepository.update(it.copy(genres = setOf("action")))
      }

      searchIndexLifecycle.rebuildIndex()
      Thread.sleep(500) // index rebuild is done asynchronously, and need a slight delay to be updated

      // when
      val found =
        seriesDtoDao
          .findAll(
            SeriesSearch(fullTextSearch = "book_count:2"),
            SearchContext(user),
            UnpagedSorted(Sort.by("relevance")),
          ).content

      // then
      assertThat(found).hasSize(1)
      assertThat(found.map { it.metadata.title }).containsExactly("Batman")
    }

    @Test
    fun `given series when searching by authors then results are matched`() {
      // given
      val series = seriesLifecycle.createSeries(makeSeries("Batman", library.id))
      val book = makeBook("Batman 01", seriesId = series.id, libraryId = library.id)
      seriesLifecycle.addBooks(series, listOf(book))
      seriesLifecycle.createSeries(makeSeries("Batman and Robin", library.id))

      bookMetadataRepository.findById(book.id).let {
        bookMetadataRepository.update(
          it.copy(
            authors =
              listOf(
                Author("David", "penciller"),
              ),
          ),
        )
      }

      seriesMetadataLifecycle.aggregateMetadata(series)
      searchIndexLifecycle.rebuildIndex()
      Thread.sleep(500) // index rebuild is done asynchronously, and need a slight delay to be updated

      // when
      val foundGeneric =
        seriesDtoDao
          .findAll(
            SeriesSearch(fullTextSearch = "author:david"),
            SearchContext(user),
            UnpagedSorted(Sort.by("relevance")),
          ).content

      val foundByRole =
        seriesDtoDao
          .findAll(
            SeriesSearch(fullTextSearch = "penciller:david"),
            SearchContext(user),
            UnpagedSorted(Sort.by("relevance")),
          ).content

      val notFoundByRole =
        seriesDtoDao
          .findAll(
            SeriesSearch(fullTextSearch = "writer:david"),
            SearchContext(user),
            UnpagedSorted(Sort.by("relevance")),
          ).content

      // then
      assertThat(foundGeneric).hasSize(1)
      assertThat(foundGeneric.map { it.metadata.title }).containsExactly("Batman")

      assertThat(foundByRole).hasSize(1)
      assertThat(foundByRole.map { it.metadata.title }).containsExactly("Batman")

      assertThat(notFoundByRole).isEmpty()
    }

    @Test
    fun `given series when searching by release year then results are matched`() {
      // given
      val series = seriesLifecycle.createSeries(makeSeries("Batman", library.id))
      val book = makeBook("Batman 01", seriesId = series.id, libraryId = library.id)
      seriesLifecycle.addBooks(series, listOf(book))
      seriesLifecycle.createSeries(makeSeries("Batman and Robin", library.id))

      bookMetadataRepository.findById(book.id).let {
        bookMetadataRepository.update(it.copy(releaseDate = LocalDate.of(1999, 10, 10)))
      }

      seriesMetadataLifecycle.aggregateMetadata(series)
      searchIndexLifecycle.rebuildIndex()
      Thread.sleep(500) // index rebuild is done asynchronously, and need a slight delay to be updated // index rebuild is done asynchronously, and need a slight delay to be updated

      // when
      val found =
        seriesDtoDao
          .findAll(
            SeriesSearch(fullTextSearch = "release_date:1999"),
            SearchContext(user),
            UnpagedSorted(Sort.by("relevance")),
          ).content

      // then
      assertThat(found).hasSize(1)
      assertThat(found.map { it.metadata.title }).containsExactly("Batman")
    }

    @Test
    fun `given series when searching by deleted then results are matched`() {
      // given
      seriesLifecycle.createSeries(makeSeries("Batman", library.id).copy(deletedDate = LocalDateTime.now()))
      seriesLifecycle.createSeries(makeSeries("Batman and Robin", library.id))

      searchIndexLifecycle.rebuildIndex()
      Thread.sleep(500) // index rebuild is done asynchronously, and need a slight delay to be updated

      // when
      val found =
        seriesDtoDao
          .findAll(
            SeriesSearch(fullTextSearch = "deleted:true"),
            SearchContext(user),
            UnpagedSorted(Sort.by("relevance")),
          ).content

      // then
      assertThat(found).hasSize(1)
      assertThat(found.map { it.metadata.title }).containsExactly("Batman")
    }
  }
}
