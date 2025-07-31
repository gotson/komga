package org.gotson.komga.infrastructure.search

import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import io.mockk.slot
import org.assertj.core.api.Assertions.assertThat
import org.gotson.komga.domain.model.DomainEvent
import org.gotson.komga.domain.model.ReadList
import org.gotson.komga.domain.model.SeriesCollection
import org.gotson.komga.domain.model.makeBook
import org.gotson.komga.domain.model.makeLibrary
import org.gotson.komga.domain.model.makeSeries
import org.gotson.komga.domain.persistence.BookMetadataRepository
import org.gotson.komga.domain.persistence.LibraryRepository
import org.gotson.komga.domain.persistence.ReadListRepository
import org.gotson.komga.domain.persistence.SeriesCollectionRepository
import org.gotson.komga.domain.persistence.SeriesMetadataRepository
import org.gotson.komga.domain.persistence.SeriesRepository
import org.gotson.komga.domain.service.BookLifecycle
import org.gotson.komga.domain.service.LibraryLifecycle
import org.gotson.komga.domain.service.ReadListLifecycle
import org.gotson.komga.domain.service.SeriesCollectionLifecycle
import org.gotson.komga.domain.service.SeriesLifecycle
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.ApplicationEventPublisher
import org.springframework.data.domain.Pageable

@SpringBootTest
class SearchIndexLifecycleTest(
  @Autowired private val libraryRepository: LibraryRepository,
  @Autowired private val libraryLifecycle: LibraryLifecycle,
  @Autowired private val seriesRepository: SeriesRepository,
  @Autowired private val seriesLifecycle: SeriesLifecycle,
  @Autowired private val seriesMetadataRepository: SeriesMetadataRepository,
  @Autowired private val bookMetadataRepository: BookMetadataRepository,
  @Autowired private val bookLifecycle: BookLifecycle,
  @Autowired private val collectionLifecycle: SeriesCollectionLifecycle,
  @Autowired private val collectionRepository: SeriesCollectionRepository,
  @Autowired private val readListLifecycle: ReadListLifecycle,
  @Autowired private val readListRepository: ReadListRepository,
  @Autowired private val searchIndexLifecycle: SearchIndexLifecycle,
  @Autowired private val luceneHelper: LuceneHelper,
) {
  private val library = makeLibrary()

  @MockkBean
  private lateinit var mockEventPublisher: ApplicationEventPublisher

  @BeforeAll
  fun setup() {
    captureEvents()
    libraryRepository.insert(library)
  }

  @BeforeEach
  fun resetMocks() {
    captureEvents()
  }

  @AfterEach
  fun deleteEntities() {
    seriesLifecycle.deleteMany(seriesRepository.findAll())
    collectionRepository.findAll(pageable = Pageable.unpaged()).forEach {
      collectionLifecycle.deleteCollection(it)
    }
    readListRepository.findAll(pageable = Pageable.unpaged()).forEach {
      readListLifecycle.deleteReadList(it)
    }
  }

  @AfterAll
  fun tearDown() {
    captureEvents()
    libraryRepository.findAll().forEach {
      libraryLifecycle.deleteLibrary(it)
    }
  }

  private fun captureEvents() {
    val eventSlot = slot<DomainEvent>()
    every { mockEventPublisher.publishEvent(capture(eventSlot)) } answers {
      searchIndexLifecycle.consumeEvents(eventSlot.captured)
    }
  }

  @Nested
  inner class Book {
    @Test
    fun `given empty index when adding an entity then it is added to the index`() {
      val series = seriesLifecycle.createSeries(makeSeries("Series", libraryId = library.id))
      seriesLifecycle.addBooks(series, listOf(makeBook("book", seriesId = series.id, libraryId = library.id)))

      val found = luceneHelper.searchEntitiesIds("book", LuceneEntity.Book)

      assertThat(found).isNotNull
      assertThat(found).hasSize(1)
    }

    @Test
    fun `given an entity when updating then it is updated in the index`() {
      val series = seriesLifecycle.createSeries(makeSeries("Series", libraryId = library.id))
      val book = makeBook("book", seriesId = series.id, libraryId = library.id)
      seriesLifecycle.addBooks(series, listOf(book))

      luceneHelper.searchEntitiesIds("book", LuceneEntity.Book).let { found ->
        assertThat(found).isNotNull
        assertThat(found).hasSize(1)
      }

      bookMetadataRepository.findById(book.id).let {
        bookMetadataRepository.update(it.copy(title = "updated"))
      }
      mockEventPublisher.publishEvent(DomainEvent.BookUpdated(book))

      luceneHelper.searchEntitiesIds("book", LuceneEntity.Book).let { found ->
        assertThat(found).isNotNull
        assertThat(found).isEmpty()
      }
      luceneHelper.searchEntitiesIds("updated", LuceneEntity.Book).let { found ->
        assertThat(found).isNotNull
        assertThat(found).hasSize(1)
      }
    }

    @Test
    fun `given an entity when deleting then it is removed from the index`() {
      val series = seriesLifecycle.createSeries(makeSeries("Series", libraryId = library.id))
      val book = makeBook("book", seriesId = series.id, libraryId = library.id)
      seriesLifecycle.addBooks(series, listOf(book))

      luceneHelper.searchEntitiesIds("book", LuceneEntity.Book).let { found ->
        assertThat(found).isNotNull
        assertThat(found).hasSize(1)
      }

      bookLifecycle.deleteOne(book)

      luceneHelper.searchEntitiesIds("book", LuceneEntity.Book).let { found ->
        assertThat(found).isNotNull
        assertThat(found).isEmpty()
      }
    }
  }

  @Nested
  inner class Series {
    @Test
    fun `given empty index when adding an entity then it is added to the index`() {
      seriesLifecycle.createSeries(makeSeries("Series", libraryId = library.id))

      val found = luceneHelper.searchEntitiesIds("series", LuceneEntity.Series)

      assertThat(found).isNotNull
      assertThat(found).hasSize(1)
    }

    @Test
    fun `given an entity when updating then it is updated in the index`() {
      val series = seriesLifecycle.createSeries(makeSeries("Series", libraryId = library.id))

      luceneHelper.searchEntitiesIds("series", LuceneEntity.Series).let { found ->
        assertThat(found).isNotNull
        assertThat(found).hasSize(1)
      }

      seriesMetadataRepository.findById(series.id).let {
        seriesMetadataRepository.update(it.copy(title = "updated", titleSort = "updated"))
      }
      mockEventPublisher.publishEvent(DomainEvent.SeriesUpdated(series))

      luceneHelper.searchEntitiesIds("series", LuceneEntity.Series).let { found ->
        assertThat(found).isNotNull
        assertThat(found).isEmpty()
      }
      luceneHelper.searchEntitiesIds("updated", LuceneEntity.Series).let { found ->
        assertThat(found).isNotNull
        assertThat(found).hasSize(1)
      }
    }

    @Test
    fun `given an entity when deleting then it is removed from the index`() {
      val series = seriesLifecycle.createSeries(makeSeries("Series", libraryId = library.id))

      luceneHelper.searchEntitiesIds("series", LuceneEntity.Series).let { found ->
        assertThat(found).isNotNull
        assertThat(found).hasSize(1)
      }

      seriesLifecycle.deleteMany(listOf(series))

      luceneHelper.searchEntitiesIds("series", LuceneEntity.Series).let { found ->
        assertThat(found).isNotNull
        assertThat(found).isEmpty()
      }
    }
  }

  @Nested
  inner class Collection {
    @Test
    fun `given empty index when adding an entity then it is added to the index`() {
      val collection = SeriesCollection("collection")
      collectionLifecycle.addCollection(collection)

      val found = luceneHelper.searchEntitiesIds("collection", LuceneEntity.Collection)

      assertThat(found).isNotNull
      assertThat(found).hasSize(1)
    }

    @Test
    fun `given an entity when updating then it is updated in the index`() {
      val collection = SeriesCollection("collection")
      collectionLifecycle.addCollection(collection)

      luceneHelper.searchEntitiesIds("collection", LuceneEntity.Collection).let { found ->
        assertThat(found).isNotNull
        assertThat(found).hasSize(1)
      }

      collectionRepository.findByIdOrNull(collection.id)?.let {
        collectionRepository.update(it.copy(name = "updated"))
      }
      mockEventPublisher.publishEvent(DomainEvent.CollectionUpdated(collection))

      luceneHelper.searchEntitiesIds("collection", LuceneEntity.Collection).let { found ->
        assertThat(found).isNotNull
        assertThat(found).isEmpty()
      }
      luceneHelper.searchEntitiesIds("updated", LuceneEntity.Collection).let { found ->
        assertThat(found).isNotNull
        assertThat(found).hasSize(1)
      }
    }

    @Test
    fun `given an entity when deleting then it is removed from the index`() {
      val collection = SeriesCollection("collection")
      collectionLifecycle.addCollection(collection)

      luceneHelper.searchEntitiesIds("collection", LuceneEntity.Collection).let { found ->
        assertThat(found).isNotNull
        assertThat(found).hasSize(1)
      }

      collectionLifecycle.deleteCollection(collection)

      luceneHelper.searchEntitiesIds("collection", LuceneEntity.Collection).let { found ->
        assertThat(found).isNotNull
        assertThat(found).isEmpty()
      }
    }
  }

  @Nested
  inner class ReadList {
    @Test
    fun `given empty index when adding an entity then it is added to the index`() {
      val readList = ReadList("readlist")
      readListLifecycle.addReadList(readList)

      val found = luceneHelper.searchEntitiesIds("readlist", LuceneEntity.ReadList)

      assertThat(found).isNotNull
      assertThat(found).hasSize(1)
    }

    @Test
    fun `given an entity when updating then it is updated in the index`() {
      val readList = ReadList("readlist")
      readListLifecycle.addReadList(readList)

      luceneHelper.searchEntitiesIds("readlist", LuceneEntity.ReadList).let { found ->
        assertThat(found).isNotNull
        assertThat(found).hasSize(1)
      }

      readListRepository.findByIdOrNull(readList.id)?.let {
        readListRepository.update(it.copy(name = "updated"))
      }
      mockEventPublisher.publishEvent(DomainEvent.ReadListUpdated(readList))

      luceneHelper.searchEntitiesIds("readlist", LuceneEntity.ReadList).let { found ->
        assertThat(found).isNotNull
        assertThat(found).isEmpty()
      }
      luceneHelper.searchEntitiesIds("updated", LuceneEntity.ReadList).let { found ->
        assertThat(found).isNotNull
        assertThat(found).hasSize(1)
      }
    }

    @Test
    fun `given an entity when deleting then it is removed from the index`() {
      val readList = ReadList("readlist")
      readListLifecycle.addReadList(readList)

      luceneHelper.searchEntitiesIds("readlist", LuceneEntity.ReadList).let { found ->
        assertThat(found).isNotNull
        assertThat(found).hasSize(1)
      }

      readListLifecycle.deleteReadList(readList)

      luceneHelper.searchEntitiesIds("readlist", LuceneEntity.ReadList).let { found ->
        assertThat(found).isNotNull
        assertThat(found).isEmpty()
      }
    }
  }
}
