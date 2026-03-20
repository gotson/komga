package org.gotson.komga.infrastructure.jooq.main

import com.ninjasquad.springmockk.MockkBean
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import org.assertj.core.api.Assertions.assertThat
import org.gotson.komga.domain.model.AgeRestriction
import org.gotson.komga.domain.model.AllowExclude
import org.gotson.komga.domain.model.Author
import org.gotson.komga.domain.model.ContentRestrictions
import org.gotson.komga.domain.model.FilterBy
import org.gotson.komga.domain.model.FilterByEntity
import org.gotson.komga.domain.model.FilterTags
import org.gotson.komga.domain.model.KomgaUser
import org.gotson.komga.domain.model.SearchContext
import org.gotson.komga.domain.model.makeBook
import org.gotson.komga.domain.model.makeLibrary
import org.gotson.komga.domain.model.makeSeries
import org.gotson.komga.domain.persistence.BookMetadataRepository
import org.gotson.komga.domain.persistence.BookRepository
import org.gotson.komga.domain.persistence.KomgaUserRepository
import org.gotson.komga.domain.persistence.LibraryRepository
import org.gotson.komga.domain.persistence.SeriesMetadataRepository
import org.gotson.komga.domain.persistence.SeriesRepository
import org.gotson.komga.domain.service.BookLifecycle
import org.gotson.komga.domain.service.KomgaUserLifecycle
import org.gotson.komga.domain.service.LibraryLifecycle
import org.gotson.komga.domain.service.SeriesLifecycle
import org.gotson.komga.domain.service.SeriesMetadataLifecycle
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
import java.time.LocalDate

@SpringBootTest
class ReferentialDaoTest(
  @Autowired private val referentialDao: ReferentialDao,
  @Autowired private val userRepository: KomgaUserRepository,
  @Autowired private val bookRepository: BookRepository,
  @Autowired private val seriesRepository: SeriesRepository,
  @Autowired private val seriesMetadataLifecycle: SeriesMetadataLifecycle,
  @Autowired private val bookMetadataRepository: BookMetadataRepository,
  @Autowired private val bookLifecycle: BookLifecycle,
  @Autowired private val userLifecycle: KomgaUserLifecycle,
  @Autowired private val seriesLifecycle: SeriesLifecycle,
  @Autowired private val libraryRepository: LibraryRepository,
  @Autowired private val libraryLifecycle: LibraryLifecycle,
  @Autowired private val seriesMetadataRepository: SeriesMetadataRepository,
) {
  private val library1 = makeLibrary()
  private val library2 = makeLibrary()
  private val series1 = makeSeries("Series 1").copy(libraryId = library1.id)
  private val series2 = makeSeries("Series 2").copy(libraryId = library2.id)
  private val seriesEmpty = makeSeries("Series Empty").copy(libraryId = library2.id)
  private val seriesShared = makeSeries("Series Shared").copy(libraryId = library1.id)
  private val seriesAge10 = makeSeries("Series Age 10").copy(libraryId = library1.id)
  private val userAll = KomgaUser("user1@example.org", "p")
  private val userLib1 = KomgaUser("user2@example.org", "p", sharedLibrariesIds = setOf(library1.id), sharedAllLibraries = false)
  private val userLabelAllow = KomgaUser("user3@example.org", "p", restrictions = ContentRestrictions(labelsAllow = setOf("item_shared")))
  private val userAge10 = KomgaUser("user4@example.org", "p", restrictions = ContentRestrictions(ageRestriction = AgeRestriction(10, AllowExclude.ALLOW_ONLY)))

  @MockkBean
  private lateinit var mockEventPublisher: ApplicationEventPublisher

  @BeforeAll
  fun setup() {
    every { mockEventPublisher.publishEvent(any()) } just Runs
    libraryRepository.insert(library1)
    libraryRepository.insert(library2)
    seriesLifecycle.createSeries(series1)
    seriesLifecycle.createSeries(series2)
    seriesLifecycle.createSeries(seriesEmpty)
    seriesLifecycle.createSeries(seriesShared)
    seriesLifecycle.createSeries(seriesAge10)
    userRepository.insert(userAll)
    userRepository.insert(userLib1)
    userRepository.insert(userLabelAllow)
    userRepository.insert(userAge10)

    // setup restrictions context
    seriesMetadataRepository.findById(seriesShared.id).let {
      seriesMetadataRepository.update(it.copy(sharingLabels = setOf("item_shared")))
    }

    seriesMetadataRepository.findById(seriesAge10.id).let {
      seriesMetadataRepository.update(it.copy(ageRating = 10))
    }

    // prepare metadata
    makeBook("1", libraryId = library1.id, seriesId = series1.id).let { book ->
      seriesLifecycle.addBooks(series1, listOf(book))
      bookMetadataRepository.findById(book.id).let {
        bookMetadataRepository.update(it.copy(authors = listOf(Author("item1", "writer")), releaseDate = LocalDate.of(2002, 1, 1), tags = setOf("bt1")))
      }
      seriesMetadataRepository.findById(series1.id).let {
        seriesMetadataRepository.update(it.copy(genres = setOf("item1"), sharingLabels = setOf("item1"), language = "fr", publisher = "item1", ageRating = 18, tags = setOf("st1")))
      }
    }
    seriesMetadataLifecycle.aggregateMetadata(series1)

    makeBook("2", libraryId = library2.id, seriesId = series2.id).let { book ->
      seriesLifecycle.addBooks(series2, listOf(book))
      bookMetadataRepository.findById(book.id).let {
        bookMetadataRepository.update(it.copy(authors = listOf(Author("item2", "inker")), releaseDate = LocalDate.of(2002, 2, 1), tags = setOf("bt2")))
      }
      seriesMetadataRepository.findById(series2.id).let {
        seriesMetadataRepository.update(it.copy(genres = setOf("item2"), sharingLabels = setOf("item2"), language = "en", publisher = "item2", ageRating = 19, tags = setOf("st2")))
      }
    }
    seriesMetadataLifecycle.aggregateMetadata(series2)

    makeBook("Empty", libraryId = library2.id, seriesId = seriesEmpty.id).let { book ->
      seriesLifecycle.addBooks(seriesEmpty, listOf(book))
    }
    seriesMetadataLifecycle.aggregateMetadata(seriesEmpty)

    makeBook("shared", libraryId = library1.id, seriesId = seriesShared.id).let { book ->
      seriesLifecycle.addBooks(seriesShared, listOf(book))
      bookMetadataRepository.findById(book.id).let {
        bookMetadataRepository.update(it.copy(authors = listOf(Author("item_shared", "penciller")), releaseDate = LocalDate.of(2003, 1, 1), tags = setOf("bt_shared")))
      }
      seriesMetadataRepository.findById(seriesShared.id).let {
        seriesMetadataRepository.update(it.copy(genres = setOf("item_shared"), language = "ja", publisher = "item_shared", tags = setOf("st_shared")))
      }
    }
    seriesMetadataLifecycle.aggregateMetadata(seriesShared)

    makeBook("10", libraryId = library1.id, seriesId = seriesAge10.id).let { book ->
      seriesLifecycle.addBooks(seriesAge10, listOf(book))
      bookMetadataRepository.findById(book.id).let {
        bookMetadataRepository.update(it.copy(authors = listOf(Author("item_10", "cover")), releaseDate = LocalDate.of(2004, 1, 1), tags = setOf("bt_10")))
      }
      seriesMetadataRepository.findById(seriesAge10.id).let {
        seriesMetadataRepository.update(it.copy(genres = setOf("item_10"), sharingLabels = setOf("item_10"), language = "sp", publisher = "item_10", tags = setOf("st_10")))
      }
    }
    seriesMetadataLifecycle.aggregateMetadata(seriesAge10)
  }

  @BeforeEach
  fun resetMocks() {
    every { mockEventPublisher.publishEvent(any()) } just Runs
  }

  @AfterEach
  fun deleteBooks() {
    bookLifecycle.deleteMany(bookRepository.findAll())
    assertThat(bookRepository.count()).isEqualTo(0)
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

  @Nested
  inner class Author {
    @Test
    fun `given search when getting authors then matching authors are returned`() {
      val context = SearchContext(userAll)
      val items = referentialDao.findAuthors(context, "shared", null, null, Pageable.unpaged()).content

      assertThat(items.map { it.name }).containsExactlyInAnyOrder("item_shared")
    }

    @Test
    fun `given role when getting authors then matching authors are returned`() {
      val context = SearchContext(userAll)
      val items = referentialDao.findAuthors(context, null, "writer", null, Pageable.unpaged()).content

      assertThat(items.map { it.name }).containsExactlyInAnyOrder("item1")
    }

    @Test
    fun `given filter by library when getting authors then only matching authors are returned`() {
      val context = SearchContext(userAll)
      val items = referentialDao.findAuthors(context, null, null, FilterBy(FilterByEntity.LIBRARY, setOf(library2.id)), Pageable.unpaged()).content

      assertThat(items.map { it.name }).containsExactlyInAnyOrder("item2")
    }

    @Test
    fun `given user without restrictions when getting authors then all authors are returned`() {
      val context = SearchContext(userAll)
      val items = referentialDao.findAuthors(context, null, null, null, Pageable.unpaged()).content

      assertThat(items.map { it.name }).containsExactlyInAnyOrder("item1", "item2", "item_shared", "item_10")
    }

    @Test
    fun `given user with restricted library access when getting authors then only allowed authors are returned`() {
      val context = SearchContext(userLib1)
      val items = referentialDao.findAuthors(context, null, null, null, Pageable.unpaged()).content

      assertThat(items.map { it.name }).containsExactlyInAnyOrder("item1", "item_shared", "item_10")
    }

    @Test
    fun `given user with restricted label access when getting authors then only allowed authors are returned`() {
      val context = SearchContext(userLabelAllow)
      val items = referentialDao.findAuthors(context, null, null, null, Pageable.unpaged()).content

      assertThat(items.map { it.name }).containsExactlyInAnyOrder("item_shared")
    }

    @Test
    fun `given user with restricted age access when getting authors then only allowed authors are returned`() {
      val context = SearchContext(userAge10)
      val items = referentialDao.findAuthors(context, null, null, null, Pageable.unpaged()).content

      assertThat(items.map { it.name }).containsExactlyInAnyOrder("item_10")
    }
  }

  @Nested
  inner class AuthorName {
    @Test
    fun `given search when getting authors then matching authors are returned`() {
      val context = SearchContext(userAll)
      val items = referentialDao.findAuthorsNames(context, "shared", null, null, Pageable.unpaged()).content

      assertThat(items).containsExactlyInAnyOrder("item_shared")
    }

    @Test
    fun `given role when getting authors then matching authors are returned`() {
      val context = SearchContext(userAll)
      val items = referentialDao.findAuthorsNames(context, null, "writer", null, Pageable.unpaged()).content

      assertThat(items).containsExactlyInAnyOrder("item1")
    }

    @Test
    fun `given filter by library when getting authors then only matching authors are returned`() {
      val context = SearchContext(userAll)
      val items = referentialDao.findAuthorsNames(context, null, null, FilterBy(FilterByEntity.LIBRARY, setOf(library2.id)), Pageable.unpaged()).content

      assertThat(items).containsExactlyInAnyOrder("item2")
    }

    @Test
    fun `given user without restrictions when getting authors then all authors are returned`() {
      val context = SearchContext(userAll)
      val items = referentialDao.findAuthorsNames(context, null, null, null, Pageable.unpaged()).content

      assertThat(items).containsExactlyInAnyOrder("item1", "item2", "item_shared", "item_10")
    }

    @Test
    fun `given user with restricted library access when getting authors then only allowed authors are returned`() {
      val context = SearchContext(userLib1)
      val items = referentialDao.findAuthorsNames(context, null, null, null, Pageable.unpaged()).content

      assertThat(items).containsExactlyInAnyOrder("item1", "item_shared", "item_10")
    }

    @Test
    fun `given user with restricted label access when getting authors then only allowed authors are returned`() {
      val context = SearchContext(userLabelAllow)
      val items = referentialDao.findAuthorsNames(context, null, null, null, Pageable.unpaged()).content

      assertThat(items).containsExactlyInAnyOrder("item_shared")
    }

    @Test
    fun `given user with restricted age access when getting authors then only allowed authors are returned`() {
      val context = SearchContext(userAge10)
      val items = referentialDao.findAuthorsNames(context, null, null, null, Pageable.unpaged()).content

      assertThat(items).containsExactlyInAnyOrder("item_10")
    }
  }

  @Nested
  inner class AuthorRole {
    @Test
    fun `given filter by library when getting authors then only matching authors are returned`() {
      val context = SearchContext(userAll)
      val items = referentialDao.findAuthorsRoles(context, FilterBy(FilterByEntity.LIBRARY, setOf(library2.id)), Pageable.unpaged()).content

      assertThat(items).containsExactlyInAnyOrder("inker")
    }

    @Test
    fun `given user without restrictions when getting authors then all authors are returned`() {
      val context = SearchContext(userAll)
      val items = referentialDao.findAuthorsRoles(context, null, Pageable.unpaged()).content

      assertThat(items).containsExactlyInAnyOrder("writer", "inker", "penciller", "cover")
    }

    @Test
    fun `given user with restricted library access when getting authors then only allowed authors are returned`() {
      val context = SearchContext(userLib1)
      val items = referentialDao.findAuthorsRoles(context, null, Pageable.unpaged()).content

      assertThat(items).containsExactlyInAnyOrder("writer", "penciller", "cover")
    }

    @Test
    fun `given user with restricted label access when getting authors then only allowed authors are returned`() {
      val context = SearchContext(userLabelAllow)
      val items = referentialDao.findAuthorsRoles(context, null, Pageable.unpaged()).content

      assertThat(items).containsExactlyInAnyOrder("penciller")
    }

    @Test
    fun `given user with restricted age access when getting authors then only allowed authors are returned`() {
      val context = SearchContext(userAge10)
      val items = referentialDao.findAuthorsRoles(context, null, Pageable.unpaged()).content

      assertThat(items).containsExactlyInAnyOrder("cover")
    }
  }

  @Nested
  inner class Genre {
    @Test
    fun `given search when getting genres then all genres are returned`() {
      val context = SearchContext(userAll)
      val items = referentialDao.findGenres(context, "shared", null, Pageable.unpaged()).content

      assertThat(items).containsExactlyInAnyOrder("item_shared")
    }

    @Test
    fun `given filter by library when getting genres then only matching genres are returned`() {
      val context = SearchContext(userAll)
      val items = referentialDao.findGenres(context, null, FilterBy(FilterByEntity.LIBRARY, setOf(library2.id)), Pageable.unpaged()).content

      assertThat(items).containsExactlyInAnyOrder("item2")
    }

    @Test
    fun `given user without restrictions when getting genres then all genres are returned`() {
      val context = SearchContext(userAll)
      val items = referentialDao.findGenres(context, null, null, Pageable.unpaged()).content

      assertThat(items).containsExactlyInAnyOrder("item1", "item2", "item_shared", "item_10")
    }

    @Test
    fun `given user with restricted library access when getting genres then only allowed genres are returned`() {
      val context = SearchContext(userLib1)
      val items = referentialDao.findGenres(context, null, null, Pageable.unpaged()).content

      assertThat(items).containsExactlyInAnyOrder("item1", "item_shared", "item_10")
    }

    @Test
    fun `given user with restricted label access when getting genres then only allowed genres are returned`() {
      val context = SearchContext(userLabelAllow)
      val items = referentialDao.findGenres(context, null, null, Pageable.unpaged()).content

      assertThat(items).containsExactlyInAnyOrder("item_shared")
    }

    @Test
    fun `given user with restricted age access when getting genres then only allowed genres are returned`() {
      val context = SearchContext(userAge10)
      val items = referentialDao.findGenres(context, null, null, Pageable.unpaged()).content

      assertThat(items).containsExactlyInAnyOrder("item_10")
    }
  }

  @Nested
  inner class BookTag {
    @Test
    fun `given search when getting tags then all tags are returned`() {
      val context = SearchContext(userAll)
      val items = referentialDao.findTags(context, "shared", null, FilterTags.BOOK, Pageable.unpaged()).content

      assertThat(items).containsExactlyInAnyOrder("bt_shared")
    }

    @Test
    fun `given filter by library when getting tags then only matching tags are returned`() {
      val context = SearchContext(userAll)
      val items = referentialDao.findTags(context, null, FilterBy(FilterByEntity.LIBRARY, setOf(library2.id)), FilterTags.BOOK, Pageable.unpaged()).content

      assertThat(items).containsExactlyInAnyOrder("bt2")
    }

    @Test
    fun `given user without restrictions when getting tags then all tags are returned`() {
      val context = SearchContext(userAll)
      val items = referentialDao.findTags(context, null, null, FilterTags.BOOK, Pageable.unpaged()).content

      assertThat(items).containsExactlyInAnyOrder("bt1", "bt2", "bt_shared", "bt_10")
    }

    @Test
    fun `given user with restricted library access when getting tags then only allowed tags are returned`() {
      val context = SearchContext(userLib1)
      val items = referentialDao.findTags(context, null, null, FilterTags.BOOK, Pageable.unpaged()).content

      assertThat(items).containsExactlyInAnyOrder("bt1", "bt_shared", "bt_10")
    }

    @Test
    fun `given user with restricted label access when getting tags then only allowed tags are returned`() {
      val context = SearchContext(userLabelAllow)
      val items = referentialDao.findTags(context, null, null, FilterTags.BOOK, Pageable.unpaged()).content

      assertThat(items).containsExactlyInAnyOrder("bt_shared")
    }

    @Test
    fun `given user with restricted age access when getting tags then only allowed tags are returned`() {
      val context = SearchContext(userAge10)
      val items = referentialDao.findTags(context, null, null, FilterTags.BOOK, Pageable.unpaged()).content

      assertThat(items).containsExactlyInAnyOrder("bt_10")
    }
  }

  @Nested
  inner class SeriesTag {
    @Test
    fun `given search when getting tags then all tags are returned`() {
      val context = SearchContext(userAll)
      val items = referentialDao.findTags(context, "shared", null, FilterTags.SERIES, Pageable.unpaged()).content

      assertThat(items).containsExactlyInAnyOrder("st_shared")
    }

    @Test
    fun `given filter by library when getting tags then only matching tags are returned`() {
      val context = SearchContext(userAll)
      val items = referentialDao.findTags(context, null, FilterBy(FilterByEntity.LIBRARY, setOf(library2.id)), FilterTags.SERIES, Pageable.unpaged()).content

      assertThat(items).containsExactlyInAnyOrder("st2")
    }

    @Test
    fun `given user without restrictions when getting tags then all tags are returned`() {
      val context = SearchContext(userAll)
      val items = referentialDao.findTags(context, null, null, FilterTags.SERIES, Pageable.unpaged()).content

      assertThat(items).containsExactlyInAnyOrder("st1", "st2", "st_shared", "st_10")
    }

    @Test
    fun `given user with restricted library access when getting tags then only allowed tags are returned`() {
      val context = SearchContext(userLib1)
      val items = referentialDao.findTags(context, null, null, FilterTags.SERIES, Pageable.unpaged()).content

      assertThat(items).containsExactlyInAnyOrder("st1", "st_shared", "st_10")
    }

    @Test
    fun `given user with restricted label access when getting tags then only allowed tags are returned`() {
      val context = SearchContext(userLabelAllow)
      val items = referentialDao.findTags(context, null, null, FilterTags.SERIES, Pageable.unpaged()).content

      assertThat(items).containsExactlyInAnyOrder("st_shared")
    }

    @Test
    fun `given user with restricted age access when getting tags then only allowed tags are returned`() {
      val context = SearchContext(userAge10)
      val items = referentialDao.findTags(context, null, null, FilterTags.SERIES, Pageable.unpaged()).content

      assertThat(items).containsExactlyInAnyOrder("st_10")
    }
  }

  @Nested
  inner class BothTag {
    @Test
    fun `given search when getting tags then all tags are returned`() {
      val context = SearchContext(userAll)
      val items = referentialDao.findTags(context, "shared", null, FilterTags.BOTH, Pageable.unpaged()).content

      assertThat(items).containsExactlyInAnyOrder("st_shared", "bt_shared")
    }

    @Test
    fun `given filter by library when getting tags then only matching tags are returned`() {
      val context = SearchContext(userAll)
      val items = referentialDao.findTags(context, null, FilterBy(FilterByEntity.LIBRARY, setOf(library2.id)), FilterTags.BOTH, Pageable.unpaged()).content

      assertThat(items).containsExactlyInAnyOrder("st2", "bt2")
    }

    @Test
    fun `given user without restrictions when getting tags then all tags are returned`() {
      val context = SearchContext(userAll)
      val items = referentialDao.findTags(context, null, null, FilterTags.BOTH, Pageable.unpaged()).content

      assertThat(items).containsExactlyInAnyOrder("st1", "st2", "st_shared", "st_10", "bt1", "bt2", "bt_shared", "bt_10")
    }

    @Test
    fun `given user with restricted library access when getting tags then only allowed tags are returned`() {
      val context = SearchContext(userLib1)
      val items = referentialDao.findTags(context, null, null, FilterTags.BOTH, Pageable.unpaged()).content

      assertThat(items).containsExactlyInAnyOrder("st1", "st_shared", "st_10", "bt1", "bt_shared", "bt_10")
    }

    @Test
    fun `given user with restricted label access when getting tags then only allowed tags are returned`() {
      val context = SearchContext(userLabelAllow)
      val items = referentialDao.findTags(context, null, null, FilterTags.BOTH, Pageable.unpaged()).content

      assertThat(items).containsExactlyInAnyOrder("st_shared", "bt_shared")
    }

    @Test
    fun `given user with restricted age access when getting tags then only allowed tags are returned`() {
      val context = SearchContext(userAge10)
      val items = referentialDao.findTags(context, null, null, FilterTags.BOTH, Pageable.unpaged()).content

      assertThat(items).containsExactlyInAnyOrder("st_10", "bt_10")
    }
  }

  @Nested
  inner class SharingLabel {
    @Test
    fun `given search when getting sharing labels then all sharing labels are returned`() {
      val context = SearchContext(userAll)
      val items = referentialDao.findSharingLabels(context, "shared", null, Pageable.unpaged()).content

      assertThat(items).containsExactlyInAnyOrder("item_shared")
    }

    @Test
    fun `given filter by library when getting sharing labels then only matching sharing labels are returned`() {
      val context = SearchContext(userAll)
      val items = referentialDao.findSharingLabels(context, null, FilterBy(FilterByEntity.LIBRARY, setOf(library2.id)), Pageable.unpaged()).content

      assertThat(items).containsExactlyInAnyOrder("item2")
    }

    @Test
    fun `given user without restrictions when getting sharing labels then all sharing labels are returned`() {
      val context = SearchContext(userAll)
      val items = referentialDao.findSharingLabels(context, null, null, Pageable.unpaged()).content

      assertThat(items).containsExactlyInAnyOrder("item1", "item2", "item_shared", "item_10")
    }

    @Test
    fun `given user with restricted library access when getting sharing labels then only allowed sharing labels are returned`() {
      val context = SearchContext(userLib1)
      val items = referentialDao.findSharingLabels(context, null, null, Pageable.unpaged()).content

      assertThat(items).containsExactlyInAnyOrder("item1", "item_shared", "item_10")
    }

    @Test
    fun `given user with restricted label access when getting sharing labels then only allowed sharing labels are returned`() {
      val context = SearchContext(userLabelAllow)
      val items = referentialDao.findSharingLabels(context, null, null, Pageable.unpaged()).content

      assertThat(items).containsExactlyInAnyOrder("item_shared")
    }

    @Test
    fun `given user with restricted age access when getting sharing labels then only allowed sharing labels are returned`() {
      val context = SearchContext(userAge10)
      val items = referentialDao.findSharingLabels(context, null, null, Pageable.unpaged()).content

      assertThat(items).containsExactlyInAnyOrder("item_10")
    }
  }

  @Nested
  inner class Publisher {
    @Test
    fun `given search when getting publishers then all publishers are returned`() {
      val context = SearchContext(userAll)
      val items = referentialDao.findPublishers(context, "shared", null, Pageable.unpaged()).content

      assertThat(items).containsExactlyInAnyOrder("item_shared")
    }

    @Test
    fun `given filter by library when getting publishers then only matching publishers are returned`() {
      val context = SearchContext(userAll)
      val items = referentialDao.findPublishers(context, null, FilterBy(FilterByEntity.LIBRARY, setOf(library2.id)), Pageable.unpaged()).content

      assertThat(items).containsExactlyInAnyOrder("item2")
    }

    @Test
    fun `given user without restrictions when getting publishers then all publishers are returned`() {
      val context = SearchContext(userAll)
      val items = referentialDao.findPublishers(context, null, null, Pageable.unpaged()).content

      assertThat(items).containsExactlyInAnyOrder("item1", "item2", "item_shared", "item_10")
    }

    @Test
    fun `given user with restricted library access when getting publishers then only allowed publishers are returned`() {
      val context = SearchContext(userLib1)
      val items = referentialDao.findPublishers(context, null, null, Pageable.unpaged()).content

      assertThat(items).containsExactlyInAnyOrder("item1", "item_shared", "item_10")
    }

    @Test
    fun `given user with restricted label access when getting publishers then only allowed publishers are returned`() {
      val context = SearchContext(userLabelAllow)
      val items = referentialDao.findPublishers(context, null, null, Pageable.unpaged()).content

      assertThat(items).containsExactlyInAnyOrder("item_shared")
    }

    @Test
    fun `given user with restricted age access when getting publishers then only allowed publishers are returned`() {
      val context = SearchContext(userAge10)
      val items = referentialDao.findPublishers(context, null, null, Pageable.unpaged()).content

      assertThat(items).containsExactlyInAnyOrder("item_10")
    }
  }

  @Nested
  inner class Language {
    @Test
    fun `given search when getting languages then all languages are returned`() {
      val context = SearchContext(userAll)
      val items = referentialDao.findLanguages(context, "j", null, Pageable.unpaged()).content

      assertThat(items).containsExactlyInAnyOrder("ja")
    }

    @Test
    fun `given filter by library when getting languages then only matching languages are returned`() {
      val context = SearchContext(userAll)
      val items = referentialDao.findLanguages(context, null, FilterBy(FilterByEntity.LIBRARY, setOf(library2.id)), Pageable.unpaged()).content

      assertThat(items).containsExactlyInAnyOrder("en")
    }

    @Test
    fun `given user without restrictions when getting languages then all languages are returned`() {
      val context = SearchContext(userAll)
      val items = referentialDao.findLanguages(context, null, null, Pageable.unpaged()).content

      assertThat(items).containsExactlyInAnyOrder("fr", "en", "ja", "sp")
    }

    @Test
    fun `given user with restricted library access when getting languages then only allowed languages are returned`() {
      val context = SearchContext(userLib1)
      val items = referentialDao.findLanguages(context, null, null, Pageable.unpaged()).content

      assertThat(items).containsExactlyInAnyOrder("fr", "ja", "sp")
    }

    @Test
    fun `given user with restricted label access when getting languages then only allowed languages are returned`() {
      val context = SearchContext(userLabelAllow)
      val items = referentialDao.findLanguages(context, null, null, Pageable.unpaged()).content

      assertThat(items).containsExactlyInAnyOrder("ja")
    }

    @Test
    fun `given user with restricted age access when getting languages then only allowed languages are returned`() {
      val context = SearchContext(userAge10)
      val items = referentialDao.findLanguages(context, null, null, Pageable.unpaged()).content

      assertThat(items).containsExactlyInAnyOrder("sp")
    }
  }

  @Nested
  inner class AgeRating {
    @Test
    fun `given filter by library when getting age ratings then only matching age ratings are returned`() {
      val context = SearchContext(userAll)
      val items = referentialDao.findAgeRatings(context, FilterBy(FilterByEntity.LIBRARY, setOf(library2.id)), Pageable.unpaged()).content

      assertThat(items).containsExactly(19)
    }

    @Test
    fun `given user without restrictions when getting age ratings then all age ratings are returned`() {
      val context = SearchContext(userAll)
      val items = referentialDao.findAgeRatings(context, null, Pageable.unpaged()).content

      assertThat(items).containsExactly(10, 18, 19)
    }

    @Test
    fun `given user with restricted library access when getting age ratings then only allowed age ratings are returned`() {
      val context = SearchContext(userLib1)
      val items = referentialDao.findAgeRatings(context, null, Pageable.unpaged()).content

      assertThat(items).containsExactly(10, 18)
    }

    @Test
    fun `given user with restricted label access when getting age ratings then only allowed age ratings are returned`() {
      val context = SearchContext(userLabelAllow)
      val items = referentialDao.findAgeRatings(context, null, Pageable.unpaged()).content

      assertThat(items).isEmpty()
    }

    @Test
    fun `given user with restricted age access when getting age ratings then only allowed age ratings are returned`() {
      val context = SearchContext(userAge10)
      val items = referentialDao.findAgeRatings(context, null, Pageable.unpaged()).content

      assertThat(items).containsExactly(10)
    }
  }

  @Nested
  inner class SeriesReleaseYear {
    @Test
    fun `given filter by library when getting series release dates then only matching series release dates are returned`() {
      val context = SearchContext(userAll)
      val items = referentialDao.findSeriesReleaseYears(context, FilterBy(FilterByEntity.LIBRARY, setOf(library2.id)), Pageable.unpaged()).content

      assertThat(items).containsExactly("2002")
    }

    @Test
    fun `given user without restrictions when getting series release dates then all series release dates are returned`() {
      val context = SearchContext(userAll)
      val items = referentialDao.findSeriesReleaseYears(context, null, Pageable.unpaged()).content

      assertThat(items).containsExactly("2004", "2003", "2002")
    }

    @Test
    fun `given user with restricted library access when getting series release dates then only allowed series release dates are returned`() {
      val context = SearchContext(userLib1)
      val items = referentialDao.findSeriesReleaseYears(context, null, Pageable.unpaged()).content

      assertThat(items).containsExactly("2004", "2003", "2002")
    }

    @Test
    fun `given user with restricted label access when getting series release dates then only allowed series release dates are returned`() {
      val context = SearchContext(userLabelAllow)
      val items = referentialDao.findSeriesReleaseYears(context, null, Pageable.unpaged()).content

      assertThat(items).containsExactly("2003")
    }

    @Test
    fun `given user with restricted age access when getting series release dates then only allowed series release dates are returned`() {
      val context = SearchContext(userAge10)
      val items = referentialDao.findSeriesReleaseYears(context, null, Pageable.unpaged()).content

      assertThat(items).containsExactly("2004")
    }
  }
}
