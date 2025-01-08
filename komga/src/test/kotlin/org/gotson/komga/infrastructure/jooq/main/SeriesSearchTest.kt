package org.gotson.komga.infrastructure.jooq.main

import com.ninjasquad.springmockk.MockkBean
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.catchThrowable
import org.gotson.komga.domain.model.Author
import org.gotson.komga.domain.model.KomgaUser
import org.gotson.komga.domain.model.ReadProgress
import org.gotson.komga.domain.model.ReadStatus
import org.gotson.komga.domain.model.SearchCondition
import org.gotson.komga.domain.model.SearchContext
import org.gotson.komga.domain.model.SearchOperator
import org.gotson.komga.domain.model.SeriesCollection
import org.gotson.komga.domain.model.SeriesMetadata
import org.gotson.komga.domain.model.SeriesSearch
import org.gotson.komga.domain.model.makeBook
import org.gotson.komga.domain.model.makeLibrary
import org.gotson.komga.domain.model.makeSeries
import org.gotson.komga.domain.persistence.BookMetadataRepository
import org.gotson.komga.domain.persistence.KomgaUserRepository
import org.gotson.komga.domain.persistence.LibraryRepository
import org.gotson.komga.domain.persistence.ReadProgressRepository
import org.gotson.komga.domain.persistence.SeriesCollectionRepository
import org.gotson.komga.domain.persistence.SeriesMetadataRepository
import org.gotson.komga.domain.persistence.SeriesRepository
import org.gotson.komga.domain.service.KomgaUserLifecycle
import org.gotson.komga.domain.service.LibraryLifecycle
import org.gotson.komga.domain.service.SeriesLifecycle
import org.gotson.komga.domain.service.SeriesMetadataLifecycle
import org.gotson.komga.infrastructure.jooq.UnpagedSorted
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.ApplicationEventPublisher
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZonedDateTime

@SpringBootTest
class SeriesSearchTest(
  @Autowired private val seriesDao: SeriesDao,
  @Autowired private val seriesDtoDao: SeriesDtoDao,
  @Autowired private val seriesRepository: SeriesRepository,
  @Autowired private val bookMetadataRepository: BookMetadataRepository,
  @Autowired private val readProgressRepository: ReadProgressRepository,
  @Autowired private val userRepository: KomgaUserRepository,
  @Autowired private val seriesLifecycle: SeriesLifecycle,
  @Autowired private val libraryRepository: LibraryRepository,
  @Autowired private val libraryLifecycle: LibraryLifecycle,
  @Autowired private val userLifecycle: KomgaUserLifecycle,
  @Autowired private val collectionRepository: SeriesCollectionRepository,
  @Autowired private val seriesMetadataLifecycle: SeriesMetadataLifecycle,
) {
  @Autowired
  private lateinit var seriesMetadataRepository: SeriesMetadataRepository
  private val library1 = makeLibrary()
  private val library2 = makeLibrary()
  private val user1 = KomgaUser("user1@example.org", "p")
  private val user2 = KomgaUser("user2@example.org", "p")

  @MockkBean
  private lateinit var mockEventPublisher: ApplicationEventPublisher

  @BeforeAll
  fun setup() {
    every { mockEventPublisher.publishEvent(any()) } just Runs
    libraryRepository.insert(library1)
    libraryRepository.insert(library2)
    userRepository.insert(user1)
    userRepository.insert(user2)
  }

  @BeforeEach
  fun resetMocks() {
    every { mockEventPublisher.publishEvent(any()) } just Runs
  }

  @AfterEach
  fun deleteSeries() {
    collectionRepository.deleteAll()
    seriesLifecycle.deleteMany(seriesRepository.findAll())
    assertThat(seriesDao.count()).isEqualTo(0)
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

  @Test
  fun `given some series when searching by library then results are accurate`() {
    makeSeries("1", library1.id).let { series ->
      seriesLifecycle.createSeries(series)
    }
    makeSeries("2", library2.id).let { series ->
      seriesLifecycle.createSeries(series)
    }

    run {
      val search = SeriesSearch(SearchCondition.LibraryId(SearchOperator.Is(library1.id)))
      val found = seriesDao.findAll(search.condition, SearchContext(user1), Pageable.unpaged()).content
      val foundDto = seriesDtoDao.findAll(search, SearchContext(user1), Pageable.unpaged()).content

      assertThat(found.map { it.name }).containsExactlyInAnyOrder("1")
      assertThat(foundDto.map { it.name }).containsExactlyInAnyOrder("1")
    }

    run {
      val search = SeriesSearch(SearchCondition.LibraryId(SearchOperator.IsNot(library1.id)))
      val found = seriesDao.findAll(search.condition, SearchContext(user1), Pageable.unpaged()).content
      val foundDto = seriesDtoDao.findAll(search, SearchContext(user1), Pageable.unpaged()).content

      assertThat(found.map { it.name }).containsExactlyInAnyOrder("2")
      assertThat(foundDto.map { it.name }).containsExactlyInAnyOrder("2")
    }
  }

  @Test
  fun `when searching by empty aggregate then it works`() {
    val series1 = makeSeries("1", library1.id)
    seriesLifecycle.createSeries(series1)
    makeSeries("2", library2.id).let { series ->
      seriesLifecycle.createSeries(series)
    }

    run {
      val search = SeriesSearch(SearchCondition.AllOfSeries())
      val found = seriesDao.findAll(search.condition, SearchContext(user1), Pageable.unpaged()).content
      val foundDto = seriesDtoDao.findAll(search, SearchContext(user1), Pageable.unpaged()).content

      assertThat(found.map { it.name }).containsExactlyInAnyOrder("1", "2")
      assertThat(foundDto.map { it.name }).containsExactlyInAnyOrder("1", "2")
    }

    run {
      val search = SeriesSearch(SearchCondition.AnyOfSeries())
      val found = seriesDao.findAll(search.condition, SearchContext(user1), Pageable.unpaged()).content
      val foundDto = seriesDtoDao.findAll(search, SearchContext(user1), Pageable.unpaged()).content

      assertThat(found.map { it.name }).containsExactlyInAnyOrder("1", "2")
      assertThat(foundDto.map { it.name }).containsExactlyInAnyOrder("1", "2")
    }
  }

  @Test
  fun `given some series when searching by collection then results are accurate`() {
    val series1 = makeSeries("1", library1.id)
    seriesLifecycle.createSeries(series1)
    makeSeries("2", library2.id).let { series ->
      seriesLifecycle.createSeries(series)
    }
    val collection = SeriesCollection("col1", seriesIds = listOf(series1.id))
    collectionRepository.insert(collection)

    run {
      val search = SeriesSearch(SearchCondition.CollectionId(SearchOperator.Is(collection.id)))
      val found = seriesDao.findAll(search.condition, SearchContext(user1), UnpagedSorted(Sort.by("collection.number"))).content
      val foundDto = seriesDtoDao.findAll(search, SearchContext(user1), UnpagedSorted(Sort.by("collection.number"))).content

      assertThat(found.map { it.name }).containsExactlyInAnyOrder("1")
      assertThat(foundDto.map { it.name }).containsExactlyInAnyOrder("1")
    }

    run {
      val search = SeriesSearch(SearchCondition.CollectionId(SearchOperator.IsNot(collection.id)))
      val found = seriesDao.findAll(search.condition, SearchContext(user1), Pageable.unpaged()).content
      val foundDto = seriesDtoDao.findAll(search, SearchContext(user1), Pageable.unpaged()).content

      assertThat(found.map { it.name }).containsExactlyInAnyOrder("2")
      assertThat(foundDto.map { it.name }).containsExactlyInAnyOrder("2")
    }
  }

  @Test
  fun `given some series in multiple collections when searching by collection then results are accurate`() {
    val series1 = makeSeries("1", library1.id).also { seriesLifecycle.createSeries(it) }
    val series2 = makeSeries("2", library1.id).also { seriesLifecycle.createSeries(it) }
    val series3 = makeSeries("3", library1.id).also { seriesLifecycle.createSeries(it) }
    val collection1 = SeriesCollection("col1", seriesIds = listOf(series1.id, series2.id, series3.id), ordered = true)
    val collection2 = SeriesCollection("col2", seriesIds = listOf(series3.id, series2.id, series1.id), ordered = true)
    collectionRepository.insert(collection1)
    collectionRepository.insert(collection2)

    // search by collection 1
    run {
      val search = SeriesSearch(SearchCondition.CollectionId(SearchOperator.Is(collection1.id)))
      val found = seriesDao.findAll(search.condition, SearchContext(user1), UnpagedSorted(Sort.by("collection.number"))).content
      val foundDto = seriesDtoDao.findAll(search, SearchContext(user1), UnpagedSorted(Sort.by("collection.number"))).content

      // order not guaranteed for seriesDao
      assertThat(found.map { it.name }).containsExactlyInAnyOrder("1", "2", "3")
      assertThat(foundDto.map { it.name }).containsExactly("1", "2", "3")
    }

    // search by collection 2
    run {
      val search = SeriesSearch(SearchCondition.CollectionId(SearchOperator.Is(collection2.id)))
      val found = seriesDao.findAll(search.condition, SearchContext(user1), UnpagedSorted(Sort.by("collection.number"))).content
      val foundDto = seriesDtoDao.findAll(search, SearchContext(user1), UnpagedSorted(Sort.by("collection.number"))).content

      // order not guaranteed for seriesDao
      assertThat(found.map { it.name }).containsExactlyInAnyOrder("3", "2", "1")
      assertThat(foundDto.map { it.name }).containsExactly("3", "2", "1")
    }
    // search by collection 1 or 2 - order is not guaranteed in that case
    run {
      val search =
        SeriesSearch(
          SearchCondition.AnyOfSeries(
            SearchCondition.CollectionId(SearchOperator.Is(collection1.id)),
            SearchCondition.CollectionId(SearchOperator.Is(collection2.id)),
          ),
        )
      val found = seriesDao.findAll(search.condition, SearchContext(user1), UnpagedSorted(Sort.by("collection.number"))).content
      val foundDto = seriesDtoDao.findAll(search, SearchContext(user1), UnpagedSorted(Sort.by("collection.number"))).content

      assertThat(found.map { it.name }).containsExactlyInAnyOrder("1", "2", "3")
      assertThat(foundDto.map { it.name }).containsExactlyInAnyOrder("1", "2", "3")
    }
  }

  @Test
  fun `given some series when searching by deleted then results are accurate`() {
    makeSeries("1", library1.id).copy(deletedDate = LocalDateTime.now()).let { series ->
      seriesLifecycle.createSeries(series)
    }
    makeSeries("2", library2.id).let { series ->
      seriesLifecycle.createSeries(series)
    }

    run {
      val search = SeriesSearch(SearchCondition.Deleted(SearchOperator.IsTrue))
      val found = seriesDao.findAll(search.condition, SearchContext(user1), Pageable.unpaged()).content
      val foundDto = seriesDtoDao.findAll(search, SearchContext(user1), Pageable.unpaged()).content

      assertThat(found.map { it.name }).containsExactlyInAnyOrder("1")
      assertThat(foundDto.map { it.name }).containsExactlyInAnyOrder("1")
    }

    run {
      val search = SeriesSearch(SearchCondition.Deleted(SearchOperator.IsFalse))
      val found = seriesDao.findAll(search.condition, SearchContext(user1), Pageable.unpaged()).content
      val foundDto = seriesDtoDao.findAll(search, SearchContext(user1), Pageable.unpaged()).content

      assertThat(found.map { it.name }).containsExactlyInAnyOrder("2")
      assertThat(foundDto.map { it.name }).containsExactlyInAnyOrder("2")
    }
  }

  @Test
  fun `given some series when searching by complete then results are accurate`() {
    makeSeries("1", library1.id).let { series ->
      seriesLifecycle.createSeries(series)
      seriesLifecycle.addBooks(series, listOf(makeBook("1", libraryId = series.libraryId)))
      seriesLifecycle.sortBooks(series)
      seriesMetadataRepository.findById(series.id).let { seriesMetadataRepository.update(it.copy(totalBookCount = 1)) }
    }
    makeSeries("2", library2.id).let { series ->
      seriesLifecycle.createSeries(series)
      seriesLifecycle.addBooks(series, listOf(makeBook("2", libraryId = series.libraryId)))
      seriesLifecycle.sortBooks(series)
      seriesMetadataRepository.findById(series.id).let { seriesMetadataRepository.update(it.copy(totalBookCount = 2)) }
    }
    // series without total book count - will not be returned in complete or not complete
    makeSeries("3", library2.id).let { series ->
      seriesLifecycle.createSeries(series)
      seriesLifecycle.addBooks(series, listOf(makeBook("3", libraryId = series.libraryId)))
      seriesLifecycle.sortBooks(series)
    }

    run {
      val search = SeriesSearch(SearchCondition.Complete(SearchOperator.IsTrue))
      val found = seriesDao.findAll(search.condition, SearchContext(user1), Pageable.unpaged()).content
      val foundDto = seriesDtoDao.findAll(search, SearchContext(user1), Pageable.unpaged()).content

      assertThat(found.map { it.name }).containsExactlyInAnyOrder("1")
      assertThat(foundDto.map { it.name }).containsExactlyInAnyOrder("1")
    }

    run {
      val search = SeriesSearch(SearchCondition.Complete(SearchOperator.IsFalse))
      val found = seriesDao.findAll(search.condition, SearchContext(user1), Pageable.unpaged()).content
      val foundDto = seriesDtoDao.findAll(search, SearchContext(user1), Pageable.unpaged()).content

      assertThat(found.map { it.name }).containsExactlyInAnyOrder("2")
      assertThat(foundDto.map { it.name }).containsExactlyInAnyOrder("2")
    }
  }

  @Test
  fun `given some series when searching by one-shot then results are accurate`() {
    makeSeries("1", library1.id).copy(oneshot = true).let { series ->
      seriesLifecycle.createSeries(series)
    }
    makeSeries("2", library2.id).let { series ->
      seriesLifecycle.createSeries(series)
    }

    run {
      val search = SeriesSearch(SearchCondition.OneShot(SearchOperator.IsTrue))
      val found = seriesDao.findAll(search.condition, SearchContext(user1), Pageable.unpaged()).content
      val foundDto = seriesDtoDao.findAll(search, SearchContext(user1), Pageable.unpaged()).content

      assertThat(found.map { it.name }).containsExactlyInAnyOrder("1")
      assertThat(foundDto.map { it.name }).containsExactlyInAnyOrder("1")
    }

    run {
      val search = SeriesSearch(SearchCondition.OneShot(SearchOperator.IsFalse))
      val found = seriesDao.findAll(search.condition, SearchContext(user1), Pageable.unpaged()).content
      val foundDto = seriesDtoDao.findAll(search, SearchContext(user1), Pageable.unpaged()).content

      assertThat(found.map { it.name }).containsExactlyInAnyOrder("2")
      assertThat(foundDto.map { it.name }).containsExactlyInAnyOrder("2")
    }
  }

  @Test
  fun `given some series when searching by release date then results are accurate`() {
    makeSeries("1", library1.id).let { series ->
      seriesLifecycle.createSeries(series)
      val book = makeBook("1", libraryId = series.libraryId)
      seriesLifecycle.addBooks(series, listOf(book))
      bookMetadataRepository.findById(book.id).let { bookMetadataRepository.update(it.copy(releaseDate = LocalDate.now().minusDays(5))) }
      seriesMetadataLifecycle.aggregateMetadata(series)
    }
    makeSeries("2", library2.id).let { series ->
      seriesLifecycle.createSeries(series)
      seriesLifecycle.addBooks(series, listOf(makeBook("2", libraryId = series.libraryId)))
    }

    run {
      val search = SeriesSearch(SearchCondition.ReleaseDate(SearchOperator.After(ZonedDateTime.now().minusDays(10))))
      val found = seriesDao.findAll(search.condition, SearchContext(user1), Pageable.unpaged()).content
      val foundDto = seriesDtoDao.findAll(search, SearchContext(user1), Pageable.unpaged()).content

      assertThat(found.map { it.name }).containsExactlyInAnyOrder("1")
      assertThat(foundDto.map { it.name }).containsExactlyInAnyOrder("1")
    }

    run {
      val search = SeriesSearch(SearchCondition.ReleaseDate(SearchOperator.Before(ZonedDateTime.now())))
      val found = seriesDao.findAll(search.condition, SearchContext(user1), Pageable.unpaged()).content
      val foundDto = seriesDtoDao.findAll(search, SearchContext(user1), Pageable.unpaged()).content

      assertThat(found.map { it.name }).containsExactlyInAnyOrder("1")
      assertThat(foundDto.map { it.name }).containsExactlyInAnyOrder("1")
    }

    run {
      val search = SeriesSearch(SearchCondition.ReleaseDate(SearchOperator.IsInTheLast(Duration.ofDays(10))))
      val found = seriesDao.findAll(search.condition, SearchContext(user1), Pageable.unpaged()).content
      val foundDto = seriesDtoDao.findAll(search, SearchContext(user1), Pageable.unpaged()).content

      assertThat(found.map { it.name }).containsExactlyInAnyOrder("1")
      assertThat(foundDto.map { it.name }).containsExactlyInAnyOrder("1")
    }

    run {
      val search = SeriesSearch(SearchCondition.ReleaseDate(SearchOperator.IsNotInTheLast(Duration.ofDays(10))))
      val found = seriesDao.findAll(search.condition, SearchContext(user1), Pageable.unpaged()).content
      val foundDto = seriesDtoDao.findAll(search, SearchContext(user1), Pageable.unpaged()).content

      assertThat(found).isEmpty()
      assertThat(foundDto).isEmpty()
    }

    run {
      val search = SeriesSearch(SearchCondition.ReleaseDate(SearchOperator.IsNotInTheLast(Duration.ofDays(1))))
      val found = seriesDao.findAll(search.condition, SearchContext(user1), Pageable.unpaged()).content
      val foundDto = seriesDtoDao.findAll(search, SearchContext(user1), Pageable.unpaged()).content

      assertThat(found.map { it.name }).containsExactlyInAnyOrder("1")
      assertThat(foundDto.map { it.name }).containsExactlyInAnyOrder("1")
    }

    run {
      val search = SeriesSearch(SearchCondition.ReleaseDate(SearchOperator.IsNull))
      val found = seriesDao.findAll(search.condition, SearchContext(user1), Pageable.unpaged()).content
      val foundDto = seriesDtoDao.findAll(search, SearchContext(user1), Pageable.unpaged()).content

      assertThat(found.map { it.name }).containsExactlyInAnyOrder("2")
      assertThat(foundDto.map { it.name }).containsExactlyInAnyOrder("2")
    }

    run {
      val search = SeriesSearch(SearchCondition.ReleaseDate(SearchOperator.IsNotNull))
      val found = seriesDao.findAll(search.condition, SearchContext(user1), Pageable.unpaged()).content
      val foundDto = seriesDtoDao.findAll(search, SearchContext(user1), Pageable.unpaged()).content

      assertThat(found.map { it.name }).containsExactlyInAnyOrder("1")
      assertThat(foundDto.map { it.name }).containsExactlyInAnyOrder("1")
    }
  }

  @Test
  fun `given some series when searching by tag then results are accurate`() {
    // series with both aggregated and series tags
    makeSeries("1", library1.id).let { series ->
      seriesLifecycle.createSeries(series)
      val book = makeBook("1", libraryId = series.libraryId)
      seriesLifecycle.addBooks(series, listOf(book))
      bookMetadataRepository.findById(book.id).let { bookMetadataRepository.update(it.copy(tags = setOf("horror"))) }
      seriesMetadataRepository.findById(series.id).let { seriesMetadataRepository.update(it.copy(tags = setOf("fiction"))) }
      seriesMetadataLifecycle.aggregateMetadata(series)
    }
    // series with series tag only
    makeSeries("2", library2.id).let { series ->
      seriesLifecycle.createSeries(series)
      seriesMetadataRepository.findById(series.id).let { seriesMetadataRepository.update(it.copy(tags = setOf("fiction"))) }
    }
    // series with aggregated tag only
    makeSeries("3", library1.id).let { series ->
      seriesLifecycle.createSeries(series)
      val book = makeBook("1", libraryId = series.libraryId)
      seriesLifecycle.addBooks(series, listOf(book))
      bookMetadataRepository.findById(book.id).let { bookMetadataRepository.update(it.copy(tags = setOf("fantasy"))) }
      seriesMetadataLifecycle.aggregateMetadata(series)
    }
    // series without tags
    makeSeries("4", library2.id).let { series ->
      seriesLifecycle.createSeries(series)
    }

    run {
      val search = SeriesSearch(SearchCondition.Tag(SearchOperator.Is("FICTION")))
      val found = seriesDao.findAll(search.condition, SearchContext(user1), Pageable.unpaged()).content
      val foundDto = seriesDtoDao.findAll(search, SearchContext(user1), Pageable.unpaged()).content

      assertThat(found.map { it.name }).containsExactlyInAnyOrder("1", "2")
      assertThat(foundDto.map { it.name }).containsExactlyInAnyOrder("1", "2")
    }

    run {
      val search =
        SeriesSearch(
          SearchCondition.AllOfSeries(
            listOf(
              SearchCondition.Tag(SearchOperator.Is("FICTION")),
              SearchCondition.Tag(SearchOperator.Is("horror")),
            ),
          ),
        )
      val found = seriesDao.findAll(search.condition, SearchContext(user1), Pageable.unpaged()).content
      val foundDto = seriesDtoDao.findAll(search, SearchContext(user1), Pageable.unpaged()).content

      assertThat(found.map { it.name }).containsExactlyInAnyOrder("1")
      assertThat(foundDto.map { it.name }).containsExactlyInAnyOrder("1")
    }

    run {
      val search =
        SeriesSearch(
          SearchCondition.AnyOfSeries(
            listOf(
              SearchCondition.Tag(SearchOperator.Is("horror")),
              SearchCondition.Tag(SearchOperator.Is("notexist")),
              SearchCondition.Tag(SearchOperator.Is("fantasy")),
            ),
          ),
        )
      val found = seriesDao.findAll(search.condition, SearchContext(user1), Pageable.unpaged()).content
      val foundDto = seriesDtoDao.findAll(search, SearchContext(user1), Pageable.unpaged()).content

      assertThat(found.map { it.name }).containsExactlyInAnyOrder("1", "3")
      assertThat(foundDto.map { it.name }).containsExactlyInAnyOrder("1", "3")
    }

    run {
      val search =
        SeriesSearch(
          SearchCondition.AllOfSeries(
            listOf(
              SearchCondition.Tag(SearchOperator.Is("fiction")),
              SearchCondition.Tag(SearchOperator.IsNot("horror")),
            ),
          ),
        )
      val found = seriesDao.findAll(search.condition, SearchContext(user1), Pageable.unpaged()).content
      val foundDto = seriesDtoDao.findAll(search, SearchContext(user1), Pageable.unpaged()).content

      assertThat(found.map { it.name }).containsExactlyInAnyOrder("2")
      assertThat(foundDto.map { it.name }).containsExactlyInAnyOrder("2")
    }

    run {
      val search = SeriesSearch(SearchCondition.Tag(SearchOperator.IsNot("fiction")))
      val found = seriesDao.findAll(search.condition, SearchContext(user1), Pageable.unpaged()).content
      val foundDto = seriesDtoDao.findAll(search, SearchContext(user1), Pageable.unpaged()).content

      assertThat(found.map { it.name }).containsExactlyInAnyOrder("3", "4")
      assertThat(foundDto.map { it.name }).containsExactlyInAnyOrder("3", "4")
    }
  }

  @Test
  fun `given some series when searching by sharing label then results are accurate`() {
    makeSeries("1", library1.id).let { series ->
      seriesLifecycle.createSeries(series)
      seriesMetadataRepository.findById(series.id).let { seriesMetadataRepository.update(it.copy(sharingLabels = setOf("kids", "teens"))) }
    }
    makeSeries("2", library2.id).let { series ->
      seriesLifecycle.createSeries(series)
      seriesMetadataRepository.findById(series.id).let { seriesMetadataRepository.update(it.copy(sharingLabels = setOf("kids"))) }
    }
    makeSeries("3", library1.id).let { series ->
      seriesLifecycle.createSeries(series)
      seriesMetadataRepository.findById(series.id).let { seriesMetadataRepository.update(it.copy(sharingLabels = setOf("adult"))) }
    }
    makeSeries("4", library2.id).let { series ->
      seriesLifecycle.createSeries(series)
    }

    run {
      val search = SeriesSearch(SearchCondition.SharingLabel(SearchOperator.Is("kids")))
      val found = seriesDao.findAll(search.condition, SearchContext(user1), Pageable.unpaged()).content
      val foundDto = seriesDtoDao.findAll(search, SearchContext(user1), Pageable.unpaged()).content

      assertThat(found.map { it.name }).containsExactlyInAnyOrder("1", "2")
      assertThat(foundDto.map { it.name }).containsExactlyInAnyOrder("1", "2")
    }

    run {
      val search =
        SeriesSearch(
          SearchCondition.AllOfSeries(
            listOf(
              SearchCondition.SharingLabel(SearchOperator.Is("kids")),
              SearchCondition.SharingLabel(SearchOperator.Is("teens")),
            ),
          ),
        )
      val found = seriesDao.findAll(search.condition, SearchContext(user1), Pageable.unpaged()).content
      val foundDto = seriesDtoDao.findAll(search, SearchContext(user1), Pageable.unpaged()).content

      assertThat(found.map { it.name }).containsExactlyInAnyOrder("1")
      assertThat(foundDto.map { it.name }).containsExactlyInAnyOrder("1")
    }

    run {
      val search =
        SeriesSearch(
          SearchCondition.AnyOfSeries(
            listOf(
              SearchCondition.SharingLabel(SearchOperator.Is("teens")),
              SearchCondition.SharingLabel(SearchOperator.Is("notexist")),
              SearchCondition.SharingLabel(SearchOperator.Is("adult")),
            ),
          ),
        )
      val found = seriesDao.findAll(search.condition, SearchContext(user1), Pageable.unpaged()).content
      val foundDto = seriesDtoDao.findAll(search, SearchContext(user1), Pageable.unpaged()).content

      assertThat(found.map { it.name }).containsExactlyInAnyOrder("1", "3")
      assertThat(foundDto.map { it.name }).containsExactlyInAnyOrder("1", "3")
    }

    run {
      val search =
        SeriesSearch(
          SearchCondition.AllOfSeries(
            listOf(
              SearchCondition.SharingLabel(SearchOperator.Is("kids")),
              SearchCondition.SharingLabel(SearchOperator.IsNot("teens")),
            ),
          ),
        )
      val found = seriesDao.findAll(search.condition, SearchContext(user1), Pageable.unpaged()).content
      val foundDto = seriesDtoDao.findAll(search, SearchContext(user1), Pageable.unpaged()).content

      assertThat(found.map { it.name }).containsExactlyInAnyOrder("2")
      assertThat(foundDto.map { it.name }).containsExactlyInAnyOrder("2")
    }

    run {
      val search = SeriesSearch(SearchCondition.SharingLabel(SearchOperator.IsNot("kids")))
      val found = seriesDao.findAll(search.condition, SearchContext(user1), Pageable.unpaged()).content
      val foundDto = seriesDtoDao.findAll(search, SearchContext(user1), Pageable.unpaged()).content

      assertThat(found.map { it.name }).containsExactlyInAnyOrder("3", "4")
      assertThat(foundDto.map { it.name }).containsExactlyInAnyOrder("3", "4")
    }
  }

  @Test
  fun `given some series when searching by publisher then results are accurate`() {
    makeSeries("1", library1.id).copy(oneshot = true).let { series ->
      seriesLifecycle.createSeries(series)
      seriesMetadataRepository.findById(series.id).let { seriesMetadataRepository.update(it.copy(publisher = "marvelous")) }
    }
    makeSeries("2", library1.id).copy(oneshot = true).let { series ->
      seriesLifecycle.createSeries(series)
      seriesMetadataRepository.findById(series.id).let { seriesMetadataRepository.update(it.copy(publisher = "infinity")) }
    }
    makeSeries("3", library2.id).let { series ->
      seriesLifecycle.createSeries(series)
    }

    run {
      val search = SeriesSearch(SearchCondition.Publisher(SearchOperator.Is("marvelOUS")))
      val found = seriesDao.findAll(search.condition, SearchContext(user1), Pageable.unpaged()).content
      val foundDto = seriesDtoDao.findAll(search, SearchContext(user1), Pageable.unpaged()).content

      assertThat(found.map { it.name }).containsExactlyInAnyOrder("1")
      assertThat(foundDto.map { it.name }).containsExactlyInAnyOrder("1")
    }

    run {
      val search = SeriesSearch(SearchCondition.Publisher(SearchOperator.IsNot("marvelOUS")))
      val found = seriesDao.findAll(search.condition, SearchContext(user1), Pageable.unpaged()).content
      val foundDto = seriesDtoDao.findAll(search, SearchContext(user1), Pageable.unpaged()).content

      assertThat(found.map { it.name }).containsExactlyInAnyOrder("2", "3")
      assertThat(foundDto.map { it.name }).containsExactlyInAnyOrder("2", "3")
    }
  }

  @Test
  fun `given some series when searching by language then results are accurate`() {
    makeSeries("1", library1.id).let { series ->
      seriesLifecycle.createSeries(series)
      seriesMetadataRepository.findById(series.id).let { seriesMetadataRepository.update(it.copy(language = "en")) }
    }
    makeSeries("2", library1.id).let { series ->
      seriesLifecycle.createSeries(series)
      seriesMetadataRepository.findById(series.id).let { seriesMetadataRepository.update(it.copy(language = "fr")) }
    }
    makeSeries("3", library2.id).let { series ->
      seriesLifecycle.createSeries(series)
    }

    run {
      val search = SeriesSearch(SearchCondition.Language(SearchOperator.Is("EN")))
      val found = seriesDao.findAll(search.condition, SearchContext(user1), Pageable.unpaged()).content
      val foundDto = seriesDtoDao.findAll(search, SearchContext(user1), Pageable.unpaged()).content

      assertThat(found.map { it.name }).containsExactlyInAnyOrder("1")
      assertThat(foundDto.map { it.name }).containsExactlyInAnyOrder("1")
    }

    run {
      val search = SeriesSearch(SearchCondition.Language(SearchOperator.IsNot("en")))
      val found = seriesDao.findAll(search.condition, SearchContext(user1), Pageable.unpaged()).content
      val foundDto = seriesDtoDao.findAll(search, SearchContext(user1), Pageable.unpaged()).content

      assertThat(found.map { it.name }).containsExactlyInAnyOrder("2", "3")
      assertThat(foundDto.map { it.name }).containsExactlyInAnyOrder("2", "3")
    }
  }

  @Test
  fun `given some series when searching by genre then results are accurate`() {
    makeSeries("1", library1.id).let { series ->
      seriesLifecycle.createSeries(series)
      seriesMetadataRepository.findById(series.id).let { seriesMetadataRepository.update(it.copy(genres = setOf("kids", "teens"))) }
    }
    makeSeries("2", library2.id).let { series ->
      seriesLifecycle.createSeries(series)
      seriesMetadataRepository.findById(series.id).let { seriesMetadataRepository.update(it.copy(genres = setOf("kids"))) }
    }
    makeSeries("3", library1.id).let { series ->
      seriesLifecycle.createSeries(series)
      seriesMetadataRepository.findById(series.id).let { seriesMetadataRepository.update(it.copy(genres = setOf("adult"))) }
    }
    makeSeries("4", library2.id).let { series ->
      seriesLifecycle.createSeries(series)
    }

    run {
      val search = SeriesSearch(SearchCondition.Genre(SearchOperator.Is("kids")))
      val found = seriesDao.findAll(search.condition, SearchContext(user1), Pageable.unpaged()).content
      val foundDto = seriesDtoDao.findAll(search, SearchContext(user1), Pageable.unpaged()).content

      assertThat(found.map { it.name }).containsExactlyInAnyOrder("1", "2")
      assertThat(foundDto.map { it.name }).containsExactlyInAnyOrder("1", "2")
    }

    run {
      val search =
        SeriesSearch(
          SearchCondition.AllOfSeries(
            listOf(
              SearchCondition.Genre(SearchOperator.Is("kids")),
              SearchCondition.Genre(SearchOperator.Is("teens")),
            ),
          ),
        )
      val found = seriesDao.findAll(search.condition, SearchContext(user1), Pageable.unpaged()).content
      val foundDto = seriesDtoDao.findAll(search, SearchContext(user1), Pageable.unpaged()).content

      assertThat(found.map { it.name }).containsExactlyInAnyOrder("1")
      assertThat(foundDto.map { it.name }).containsExactlyInAnyOrder("1")
    }

    run {
      val search =
        SeriesSearch(
          SearchCondition.AnyOfSeries(
            listOf(
              SearchCondition.Genre(SearchOperator.Is("teens")),
              SearchCondition.Genre(SearchOperator.Is("notexist")),
              SearchCondition.Genre(SearchOperator.Is("adult")),
            ),
          ),
        )
      val found = seriesDao.findAll(search.condition, SearchContext(user1), Pageable.unpaged()).content
      val foundDto = seriesDtoDao.findAll(search, SearchContext(user1), Pageable.unpaged()).content

      assertThat(found.map { it.name }).containsExactlyInAnyOrder("1", "3")
      assertThat(foundDto.map { it.name }).containsExactlyInAnyOrder("1", "3")
    }

    run {
      val search =
        SeriesSearch(
          SearchCondition.AllOfSeries(
            listOf(
              SearchCondition.Genre(SearchOperator.Is("kids")),
              SearchCondition.Genre(SearchOperator.IsNot("teens")),
            ),
          ),
        )
      val found = seriesDao.findAll(search.condition, SearchContext(user1), Pageable.unpaged()).content
      val foundDto = seriesDtoDao.findAll(search, SearchContext(user1), Pageable.unpaged()).content

      assertThat(found.map { it.name }).containsExactlyInAnyOrder("2")
      assertThat(foundDto.map { it.name }).containsExactlyInAnyOrder("2")
    }

    run {
      val search = SeriesSearch(SearchCondition.Genre(SearchOperator.IsNot("kids")))
      val found = seriesDao.findAll(search.condition, SearchContext(user1), Pageable.unpaged()).content
      val foundDto = seriesDtoDao.findAll(search, SearchContext(user1), Pageable.unpaged()).content

      assertThat(found.map { it.name }).containsExactlyInAnyOrder("3", "4")
      assertThat(foundDto.map { it.name }).containsExactlyInAnyOrder("3", "4")
    }
  }

  @Test
  fun `given some series when searching by series status then results are accurate`() {
    makeSeries("1", library1.id).let { series ->
      seriesLifecycle.createSeries(series)
      seriesMetadataRepository.findById(series.id).let { seriesMetadataRepository.update(it.copy(status = SeriesMetadata.Status.ENDED)) }
    }
    makeSeries("2", library2.id).let { series ->
      seriesLifecycle.createSeries(series)
      seriesMetadataRepository.findById(series.id).let { seriesMetadataRepository.update(it.copy(status = SeriesMetadata.Status.ONGOING)) }
    }

    run {
      val search = SeriesSearch(SearchCondition.SeriesStatus(SearchOperator.Is(SeriesMetadata.Status.ENDED)))
      val found = seriesDao.findAll(search.condition, SearchContext(user1), Pageable.unpaged()).content
      val foundDto = seriesDtoDao.findAll(search, SearchContext(user1), Pageable.unpaged()).content

      assertThat(found.map { it.name }).containsExactlyInAnyOrder("1")
      assertThat(foundDto.map { it.name }).containsExactlyInAnyOrder("1")
    }

    run {
      val search = SeriesSearch(SearchCondition.SeriesStatus(SearchOperator.IsNot(SeriesMetadata.Status.ENDED)))
      val found = seriesDao.findAll(search.condition, SearchContext(user1), Pageable.unpaged()).content
      val foundDto = seriesDtoDao.findAll(search, SearchContext(user1), Pageable.unpaged()).content

      assertThat(found.map { it.name }).containsExactlyInAnyOrder("2")
      assertThat(foundDto.map { it.name }).containsExactlyInAnyOrder("2")
    }

    run {
      val search =
        SeriesSearch(
          SearchCondition.AllOfSeries(
            listOf(
              SearchCondition.SeriesStatus(SearchOperator.Is(SeriesMetadata.Status.ENDED)),
              SearchCondition.SeriesStatus(SearchOperator.Is(SeriesMetadata.Status.HIATUS)),
            ),
          ),
        )
      val found = seriesDao.findAll(search.condition, SearchContext(user1), Pageable.unpaged()).content
      val foundDto = seriesDtoDao.findAll(search, SearchContext(user1), Pageable.unpaged()).content

      assertThat(found).isEmpty()
      assertThat(foundDto).isEmpty()
    }

    run {
      val search =
        SeriesSearch(
          SearchCondition.AnyOfSeries(
            listOf(
              SearchCondition.SeriesStatus(SearchOperator.Is(SeriesMetadata.Status.ENDED)),
              SearchCondition.SeriesStatus(SearchOperator.Is(SeriesMetadata.Status.ONGOING)),
            ),
          ),
        )
      val found = seriesDao.findAll(search.condition, SearchContext(user1), Pageable.unpaged()).content
      val foundDto = seriesDtoDao.findAll(search, SearchContext(user1), Pageable.unpaged()).content

      assertThat(found.map { it.name }).containsExactlyInAnyOrder("1", "2")
      assertThat(foundDto.map { it.name }).containsExactlyInAnyOrder("1", "2")
    }
  }

  @Test
  fun `given some series when searching by read progress then results are accurate`() {
    // in progress series, 1/2 book read
    makeSeries("1", library1.id).let { series ->
      seriesLifecycle.createSeries(series)
      val books = (1..2).map { makeBook("$it", libraryId = series.libraryId) }
      seriesLifecycle.addBooks(series, books)
      seriesLifecycle.sortBooks(series)
      readProgressRepository.save(ReadProgress(books.first().id, user1.id, 5, true))
    }
    // read series
    makeSeries("2", library2.id).let { series ->
      seriesLifecycle.createSeries(series)
      val book = makeBook("1", libraryId = series.libraryId)
      seriesLifecycle.addBooks(series, listOf(book))
      seriesLifecycle.sortBooks(series)
      readProgressRepository.save(ReadProgress(book.id, user1.id, 5, true))
    }
    // unread series
    makeSeries("3", library2.id).let { series ->
      seriesLifecycle.createSeries(series)
    }

    run {
      val search = SeriesSearch(SearchCondition.ReadStatus(SearchOperator.Is(ReadStatus.IN_PROGRESS)))
      val found = seriesDao.findAll(search.condition, SearchContext(user1), Pageable.unpaged()).content
      val foundDto = seriesDtoDao.findAll(search, SearchContext(user1), Pageable.unpaged()).content

      assertThat(found.map { it.name }).containsExactlyInAnyOrder("1")
      assertThat(foundDto.map { it.name }).containsExactlyInAnyOrder("1")
    }

    run {
      val search = SeriesSearch(SearchCondition.ReadStatus(SearchOperator.Is(ReadStatus.UNREAD)))
      val found = seriesDao.findAll(search.condition, SearchContext(user1), Pageable.unpaged()).content
      val foundDto = seriesDtoDao.findAll(search, SearchContext(user1), Pageable.unpaged()).content

      assertThat(found.map { it.name }).containsExactlyInAnyOrder("3")
      assertThat(foundDto.map { it.name }).containsExactlyInAnyOrder("3")
    }

    run {
      val search = SeriesSearch(SearchCondition.ReadStatus(SearchOperator.Is(ReadStatus.READ)))
      val found = seriesDao.findAll(search.condition, SearchContext(user1), Pageable.unpaged()).content
      val foundDto = seriesDtoDao.findAll(search, SearchContext(user1), Pageable.unpaged()).content

      assertThat(found.map { it.name }).containsExactlyInAnyOrder("2")
      assertThat(foundDto.map { it.name }).containsExactlyInAnyOrder("2")
    }

    run {
      val search = SeriesSearch(SearchCondition.ReadStatus(SearchOperator.Is(ReadStatus.READ)))
      val found = seriesDao.findAll(search.condition, SearchContext(user2), Pageable.unpaged()).content
      val foundDto = seriesDtoDao.findAll(search, SearchContext(user2), Pageable.unpaged()).content

      assertThat(found).isEmpty()
      assertThat(foundDto).isEmpty()
    }

    run {
      val search = SeriesSearch(SearchCondition.ReadStatus(SearchOperator.Is(ReadStatus.UNREAD)))
      val found = seriesDao.findAll(search.condition, SearchContext(user2), Pageable.unpaged()).content
      val foundDto = seriesDtoDao.findAll(search, SearchContext(user2), Pageable.unpaged()).content

      assertThat(found.map { it.name }).containsExactlyInAnyOrder("1", "2", "3")
      assertThat(foundDto.map { it.name }).containsExactlyInAnyOrder("1", "2", "3")
    }

    run {
      val search = SeriesSearch(SearchCondition.ReadStatus(SearchOperator.Is(ReadStatus.READ)))
      val found = seriesDao.findAll(search.condition, SearchContext(null), Pageable.unpaged()).content
      val thrown = catchThrowable { seriesDtoDao.findAll(search, SearchContext(null), Pageable.unpaged()) }

      assertThat(found).isEmpty()
      assertThat(thrown).isInstanceOf(IllegalArgumentException::class.java)
    }
  }

  @Test
  fun `given some series when searching by author then results are accurate`() {
    makeSeries("1", library1.id).let { series ->
      seriesLifecycle.createSeries(series)
      val book = makeBook("1", libraryId = series.libraryId)
      seriesLifecycle.addBooks(series, listOf(book))
      bookMetadataRepository.findById(book.id).let {
        bookMetadataRepository.update(it.copy(authors = listOf(Author("john", "writer"), Author("jim", "cover"))))
      }
      seriesMetadataLifecycle.aggregateMetadata(series)
    }
    makeSeries("2", library1.id).let { series ->
      seriesLifecycle.createSeries(series)
      val book = makeBook("2", libraryId = series.libraryId)
      seriesLifecycle.addBooks(series, listOf(book))
      bookMetadataRepository.findById(book.id).let {
        bookMetadataRepository.update(it.copy(authors = listOf(Author("john", "artist"), Author("amanda", "artist"))))
      }
      seriesMetadataLifecycle.aggregateMetadata(series)
    }
    makeSeries("3", library1.id).let { series ->
      seriesLifecycle.createSeries(series)
      val book = makeBook("3", libraryId = series.libraryId)
      seriesLifecycle.addBooks(series, listOf(book))
      bookMetadataRepository.findById(book.id).let {
        bookMetadataRepository.update(it.copy(authors = listOf(Author("jack", "writer"))))
      }
      seriesMetadataLifecycle.aggregateMetadata(series)
    }
    makeSeries("4", library1.id).let { series ->
      seriesLifecycle.createSeries(series)
    }

    // series with an author named 'john'
    run {
      val search =
        SeriesSearch(
          SearchCondition.Author(SearchOperator.Is(SearchCondition.AuthorMatch("john"))),
        )
      val found = seriesDao.findAll(search.condition, SearchContext(user1), Pageable.unpaged()).content
      val foundDto = seriesDtoDao.findAll(search, SearchContext(user1), Pageable.unpaged()).content

      assertThat(found.map { it.name }).containsExactlyInAnyOrder("1", "2")
      assertThat(foundDto.map { it.name }).containsExactlyInAnyOrder("1", "2")
    }

    // series with an author named 'john' with the 'writer' role
    run {
      val search =
        SeriesSearch(
          SearchCondition.Author(SearchOperator.Is(SearchCondition.AuthorMatch("john", "writer"))),
        )
      val found = seriesDao.findAll(search.condition, SearchContext(user1), Pageable.unpaged()).content
      val foundDto = seriesDtoDao.findAll(search, SearchContext(user1), Pageable.unpaged()).content

      assertThat(found.map { it.name }).containsExactlyInAnyOrder("1")
      assertThat(foundDto.map { it.name }).containsExactlyInAnyOrder("1")
    }

    // series with any author with the 'writer' role
    run {
      val search =
        SeriesSearch(
          SearchCondition.Author(SearchOperator.Is(SearchCondition.AuthorMatch(role = "writer"))),
        )
      val found = seriesDao.findAll(search.condition, SearchContext(user1), Pageable.unpaged()).content
      val foundDto = seriesDtoDao.findAll(search, SearchContext(user1), Pageable.unpaged()).content

      assertThat(found.map { it.name }).containsExactlyInAnyOrder("1", "3")
      assertThat(foundDto.map { it.name }).containsExactlyInAnyOrder("1", "3")
    }

    // series without any author named 'john' with a 'writer' role
    run {
      val search =
        SeriesSearch(
          SearchCondition.Author(SearchOperator.IsNot(SearchCondition.AuthorMatch("john", "writer"))),
        )
      val found = seriesDao.findAll(search.condition, SearchContext(user1), Pageable.unpaged()).content
      val foundDto = seriesDtoDao.findAll(search, SearchContext(user1), Pageable.unpaged()).content

      assertThat(found.map { it.name }).containsExactlyInAnyOrder("2", "3", "4")
      assertThat(foundDto.map { it.name }).containsExactlyInAnyOrder("2", "3", "4")
    }

    // series without any author named 'john'
    run {
      val search =
        SeriesSearch(
          SearchCondition.Author(SearchOperator.IsNot(SearchCondition.AuthorMatch("jOhn"))),
        )
      val found = seriesDao.findAll(search.condition, SearchContext(user1), Pageable.unpaged()).content
      val foundDto = seriesDtoDao.findAll(search, SearchContext(user1), Pageable.unpaged()).content

      assertThat(found.map { it.name }).containsExactlyInAnyOrder("3", "4")
      assertThat(foundDto.map { it.name }).containsExactlyInAnyOrder("3", "4")
    }

    // series without any author with the 'writer' role
    run {
      val search =
        SeriesSearch(
          SearchCondition.Author(SearchOperator.IsNot(SearchCondition.AuthorMatch(role = "writer"))),
        )
      val found = seriesDao.findAll(search.condition, SearchContext(user1), Pageable.unpaged()).content
      val foundDto = seriesDtoDao.findAll(search, SearchContext(user1), Pageable.unpaged()).content

      assertThat(found.map { it.name }).containsExactlyInAnyOrder("2", "4")
      assertThat(foundDto.map { it.name }).containsExactlyInAnyOrder("2", "4")
    }

    // empty AuthorMatch does not apply any condition
    run {
      val search =
        SeriesSearch(
          SearchCondition.Author(SearchOperator.Is(SearchCondition.AuthorMatch())),
        )
      val found = seriesDao.findAll(search.condition, SearchContext(user1), Pageable.unpaged()).content
      val foundDto = seriesDtoDao.findAll(search, SearchContext(user1), Pageable.unpaged()).content

      assertThat(found.map { it.name }).containsExactlyInAnyOrder("1", "2", "3", "4")
      assertThat(foundDto.map { it.name }).containsExactlyInAnyOrder("1", "2", "3", "4")
    }
  }

  @Test
  fun `given some series when searching by age rating then results are accurate`() {
    makeSeries("1", library1.id).let { series ->
      seriesLifecycle.createSeries(series)
      seriesMetadataRepository.findById(series.id).let {
        seriesMetadataRepository.update(it.copy(ageRating = 1))
      }
    }
    makeSeries("2", library1.id).let { series ->
      seriesLifecycle.createSeries(series)
      seriesMetadataRepository.findById(series.id).let {
        seriesMetadataRepository.update(it.copy(ageRating = 10))
      }
    }
    makeSeries("3", library1.id).let { series ->
      seriesLifecycle.createSeries(series)
    }

    run {
      val search = SeriesSearch(SearchCondition.AgeRating(SearchOperator.Is(10)))
      val found = seriesDao.findAll(search.condition, SearchContext(user1), Pageable.unpaged()).content
      val foundDto = seriesDtoDao.findAll(search, SearchContext(user1), Pageable.unpaged()).content

      assertThat(found.map { it.name }).containsExactlyInAnyOrder("2")
      assertThat(foundDto.map { it.name }).containsExactlyInAnyOrder("2")
    }

    run {
      val search = SeriesSearch(SearchCondition.AgeRating(SearchOperator.IsNot(10)))
      val found = seriesDao.findAll(search.condition, SearchContext(user1), Pageable.unpaged()).content
      val foundDto = seriesDtoDao.findAll(search, SearchContext(user1), Pageable.unpaged()).content

      assertThat(found.map { it.name }).containsExactlyInAnyOrder("1", "3")
      assertThat(foundDto.map { it.name }).containsExactlyInAnyOrder("1", "3")
    }

    run {
      val search = SeriesSearch(SearchCondition.AgeRating(SearchOperator.GreaterThan(0)))
      val found = seriesDao.findAll(search.condition, SearchContext(user1), Pageable.unpaged()).content
      val foundDto = seriesDtoDao.findAll(search, SearchContext(user1), Pageable.unpaged()).content

      assertThat(found.map { it.name }).containsExactlyInAnyOrder("1", "2")
      assertThat(foundDto.map { it.name }).containsExactlyInAnyOrder("1", "2")
    }

    run {
      val search = SeriesSearch(SearchCondition.AgeRating(SearchOperator.GreaterThan(5)))
      val found = seriesDao.findAll(search.condition, SearchContext(user1), Pageable.unpaged()).content
      val foundDto = seriesDtoDao.findAll(search, SearchContext(user1), Pageable.unpaged()).content

      assertThat(found.map { it.name }).containsExactlyInAnyOrder("2")
      assertThat(foundDto.map { it.name }).containsExactlyInAnyOrder("2")
    }

    run {
      val search = SeriesSearch(SearchCondition.AgeRating(SearchOperator.LessThan(11)))
      val found = seriesDao.findAll(search.condition, SearchContext(user1), Pageable.unpaged()).content
      val foundDto = seriesDtoDao.findAll(search, SearchContext(user1), Pageable.unpaged()).content

      assertThat(found.map { it.name }).containsExactlyInAnyOrder("1", "2")
      assertThat(foundDto.map { it.name }).containsExactlyInAnyOrder("1", "2")
    }

    run {
      val search = SeriesSearch(SearchCondition.AgeRating(SearchOperator.IsNullT()))
      val found = seriesDao.findAll(search.condition, SearchContext(user1), Pageable.unpaged()).content
      val foundDto = seriesDtoDao.findAll(search, SearchContext(user1), Pageable.unpaged()).content

      assertThat(found.map { it.name }).containsExactlyInAnyOrder("3")
      assertThat(foundDto.map { it.name }).containsExactlyInAnyOrder("3")
    }

    run {
      val search = SeriesSearch(SearchCondition.AgeRating(SearchOperator.IsNotNullT()))
      val found = seriesDao.findAll(search.condition, SearchContext(user1), Pageable.unpaged()).content
      val foundDto = seriesDtoDao.findAll(search, SearchContext(user1), Pageable.unpaged()).content

      assertThat(found.map { it.name }).containsExactlyInAnyOrder("1", "2")
      assertThat(foundDto.map { it.name }).containsExactlyInAnyOrder("1", "2")
    }

    run {
      val search =
        SeriesSearch(
          SearchCondition.AnyOfSeries(
            listOf(
              SearchCondition.AgeRating(SearchOperator.Is(10)),
              SearchCondition.AgeRating(SearchOperator.Is(1)),
            ),
          ),
        )
      val found = seriesDao.findAll(search.condition, SearchContext(user1), Pageable.unpaged()).content
      val foundDto = seriesDtoDao.findAll(search, SearchContext(user1), Pageable.unpaged()).content

      assertThat(found.map { it.name }).containsExactlyInAnyOrder("1", "2")
      assertThat(foundDto.map { it.name }).containsExactlyInAnyOrder("1", "2")
    }

    run {
      val search =
        SeriesSearch(
          SearchCondition.AllOfSeries(
            listOf(
              SearchCondition.AgeRating(SearchOperator.Is(10)),
              SearchCondition.AgeRating(SearchOperator.Is(1)),
            ),
          ),
        )
      val found = seriesDao.findAll(search.condition, SearchContext(user1), Pageable.unpaged()).content
      val foundDto = seriesDtoDao.findAll(search, SearchContext(user1), Pageable.unpaged()).content

      assertThat(found).isEmpty()
      assertThat(foundDto).isEmpty()
    }
  }

  @Test
  fun `given some books when searching by title then results are accurate`() {
    makeSeries("1", library1.id).let { series ->
      seriesLifecycle.createSeries(series)
      seriesMetadataRepository.findById(series.id).let {
        seriesMetadataRepository.update(it.copy(title = "Series 1"))
      }
    }
    makeSeries("2", library1.id).let { series ->
      seriesLifecycle.createSeries(series)
      seriesMetadataRepository.findById(series.id).let {
        seriesMetadataRepository.update(it.copy(title = "Series 2"))
      }
    }

    run {
      val search = SeriesSearch(SearchCondition.Title(SearchOperator.Is("seRIES 1")))
      val found = seriesDao.findAll(search.condition, SearchContext(user1), Pageable.unpaged()).content
      val foundDto = seriesDtoDao.findAll(search, SearchContext(user1), Pageable.unpaged()).content

      assertThat(found.map { it.name }).containsExactlyInAnyOrder("1")
      assertThat(foundDto.map { it.name }).containsExactlyInAnyOrder("1")
    }

    run {
      val search = SeriesSearch(SearchCondition.Title(SearchOperator.IsNot("series 1")))
      val found = seriesDao.findAll(search.condition, SearchContext(user1), Pageable.unpaged()).content
      val foundDto = seriesDtoDao.findAll(search, SearchContext(user1), Pageable.unpaged()).content

      assertThat(found.map { it.name }).containsExactlyInAnyOrder("2")
      assertThat(foundDto.map { it.name }).containsExactlyInAnyOrder("2")
    }

    run {
      val search = SeriesSearch(SearchCondition.Title(SearchOperator.Contains("series")))
      val found = seriesDao.findAll(search.condition, SearchContext(user1), Pageable.unpaged()).content
      val foundDto = seriesDtoDao.findAll(search, SearchContext(user1), Pageable.unpaged()).content

      assertThat(found.map { it.name }).containsExactlyInAnyOrder("1", "2")
      assertThat(foundDto.map { it.name }).containsExactlyInAnyOrder("1", "2")
    }

    run {
      val search = SeriesSearch(SearchCondition.Title(SearchOperator.DoesNotContain("1")))
      val found = seriesDao.findAll(search.condition, SearchContext(user1), Pageable.unpaged()).content
      val foundDto = seriesDtoDao.findAll(search, SearchContext(user1), Pageable.unpaged()).content

      assertThat(found.map { it.name }).containsExactlyInAnyOrder("2")
      assertThat(foundDto.map { it.name }).containsExactlyInAnyOrder("2")
    }

    run {
      val search = SeriesSearch(SearchCondition.Title(SearchOperator.BeginsWith("series")))
      val found = seriesDao.findAll(search.condition, SearchContext(user1), Pageable.unpaged()).content
      val foundDto = seriesDtoDao.findAll(search, SearchContext(user1), Pageable.unpaged()).content

      assertThat(found.map { it.name }).containsExactlyInAnyOrder("1", "2")
      assertThat(foundDto.map { it.name }).containsExactlyInAnyOrder("1", "2")
    }

    run {
      val search = SeriesSearch(SearchCondition.Title(SearchOperator.DoesNotBeginWith("series")))
      val found = seriesDao.findAll(search.condition, SearchContext(user1), Pageable.unpaged()).content
      val foundDto = seriesDtoDao.findAll(search, SearchContext(user1), Pageable.unpaged()).content

      assertThat(found.map { it.name }).isEmpty()
      assertThat(foundDto.map { it.name }).isEmpty()
    }

    run {
      val search = SeriesSearch(SearchCondition.Title(SearchOperator.EndsWith("1")))
      val found = seriesDao.findAll(search.condition, SearchContext(user1), Pageable.unpaged()).content
      val foundDto = seriesDtoDao.findAll(search, SearchContext(user1), Pageable.unpaged()).content

      assertThat(found.map { it.name }).containsExactlyInAnyOrder("1")
      assertThat(foundDto.map { it.name }).containsExactlyInAnyOrder("1")
    }

    run {
      val search = SeriesSearch(SearchCondition.Title(SearchOperator.DoesNotEndWith("1")))
      val found = seriesDao.findAll(search.condition, SearchContext(user1), Pageable.unpaged()).content
      val foundDto = seriesDtoDao.findAll(search, SearchContext(user1), Pageable.unpaged()).content

      assertThat(found.map { it.name }).containsExactlyInAnyOrder("2")
      assertThat(foundDto.map { it.name }).containsExactlyInAnyOrder("2")
    }
  }

  @Test
  fun `given some books when searching by titleSort then results are accurate`() {
    makeSeries("1", library1.id).let { series ->
      seriesLifecycle.createSeries(series)
      seriesMetadataRepository.findById(series.id).let {
        seriesMetadataRepository.update(it.copy(titleSort = "Series 1"))
      }
    }
    makeSeries("2", library1.id).let { series ->
      seriesLifecycle.createSeries(series)
      seriesMetadataRepository.findById(series.id).let {
        seriesMetadataRepository.update(it.copy(titleSort = "Series 2"))
      }
    }

    run {
      val search = SeriesSearch(SearchCondition.TitleSort(SearchOperator.Is("seRIES 1")))
      val found = seriesDao.findAll(search.condition, SearchContext(user1), Pageable.unpaged()).content
      val foundDto = seriesDtoDao.findAll(search, SearchContext(user1), Pageable.unpaged()).content

      assertThat(found.map { it.name }).containsExactlyInAnyOrder("1")
      assertThat(foundDto.map { it.name }).containsExactlyInAnyOrder("1")
    }

    run {
      val search = SeriesSearch(SearchCondition.TitleSort(SearchOperator.IsNot("series 1")))
      val found = seriesDao.findAll(search.condition, SearchContext(user1), Pageable.unpaged()).content
      val foundDto = seriesDtoDao.findAll(search, SearchContext(user1), Pageable.unpaged()).content

      assertThat(found.map { it.name }).containsExactlyInAnyOrder("2")
      assertThat(foundDto.map { it.name }).containsExactlyInAnyOrder("2")
    }

    run {
      val search = SeriesSearch(SearchCondition.TitleSort(SearchOperator.Contains("series")))
      val found = seriesDao.findAll(search.condition, SearchContext(user1), Pageable.unpaged()).content
      val foundDto = seriesDtoDao.findAll(search, SearchContext(user1), Pageable.unpaged()).content

      assertThat(found.map { it.name }).containsExactlyInAnyOrder("1", "2")
      assertThat(foundDto.map { it.name }).containsExactlyInAnyOrder("1", "2")
    }

    run {
      val search = SeriesSearch(SearchCondition.TitleSort(SearchOperator.DoesNotContain("1")))
      val found = seriesDao.findAll(search.condition, SearchContext(user1), Pageable.unpaged()).content
      val foundDto = seriesDtoDao.findAll(search, SearchContext(user1), Pageable.unpaged()).content

      assertThat(found.map { it.name }).containsExactlyInAnyOrder("2")
      assertThat(foundDto.map { it.name }).containsExactlyInAnyOrder("2")
    }

    run {
      val search = SeriesSearch(SearchCondition.TitleSort(SearchOperator.BeginsWith("series")))
      val found = seriesDao.findAll(search.condition, SearchContext(user1), Pageable.unpaged()).content
      val foundDto = seriesDtoDao.findAll(search, SearchContext(user1), Pageable.unpaged()).content

      assertThat(found.map { it.name }).containsExactlyInAnyOrder("1", "2")
      assertThat(foundDto.map { it.name }).containsExactlyInAnyOrder("1", "2")
    }

    run {
      val search = SeriesSearch(SearchCondition.TitleSort(SearchOperator.DoesNotBeginWith("series")))
      val found = seriesDao.findAll(search.condition, SearchContext(user1), Pageable.unpaged()).content
      val foundDto = seriesDtoDao.findAll(search, SearchContext(user1), Pageable.unpaged()).content

      assertThat(found.map { it.name }).isEmpty()
      assertThat(foundDto.map { it.name }).isEmpty()
    }

    run {
      val search = SeriesSearch(SearchCondition.TitleSort(SearchOperator.EndsWith("1")))
      val found = seriesDao.findAll(search.condition, SearchContext(user1), Pageable.unpaged()).content
      val foundDto = seriesDtoDao.findAll(search, SearchContext(user1), Pageable.unpaged()).content

      assertThat(found.map { it.name }).containsExactlyInAnyOrder("1")
      assertThat(foundDto.map { it.name }).containsExactlyInAnyOrder("1")
    }

    run {
      val search = SeriesSearch(SearchCondition.TitleSort(SearchOperator.DoesNotEndWith("1")))
      val found = seriesDao.findAll(search.condition, SearchContext(user1), Pageable.unpaged()).content
      val foundDto = seriesDtoDao.findAll(search, SearchContext(user1), Pageable.unpaged()).content

      assertThat(found.map { it.name }).containsExactlyInAnyOrder("2")
      assertThat(foundDto.map { it.name }).containsExactlyInAnyOrder("2")
    }
  }
}
