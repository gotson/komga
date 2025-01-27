package org.gotson.komga.infrastructure.jooq.main

import com.ninjasquad.springmockk.MockkBean
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.catchThrowable
import org.gotson.komga.domain.model.Author
import org.gotson.komga.domain.model.BookSearch
import org.gotson.komga.domain.model.Dimension
import org.gotson.komga.domain.model.KomgaUser
import org.gotson.komga.domain.model.MarkSelectedPreference
import org.gotson.komga.domain.model.Media
import org.gotson.komga.domain.model.MediaProfile
import org.gotson.komga.domain.model.MediaType
import org.gotson.komga.domain.model.ReadList
import org.gotson.komga.domain.model.ReadProgress
import org.gotson.komga.domain.model.ReadStatus
import org.gotson.komga.domain.model.SearchCondition
import org.gotson.komga.domain.model.SearchCondition.AuthorMatch
import org.gotson.komga.domain.model.SearchContext
import org.gotson.komga.domain.model.SearchOperator
import org.gotson.komga.domain.model.ThumbnailBook
import org.gotson.komga.domain.model.makeBook
import org.gotson.komga.domain.model.makeLibrary
import org.gotson.komga.domain.model.makeSeries
import org.gotson.komga.domain.persistence.BookMetadataRepository
import org.gotson.komga.domain.persistence.BookRepository
import org.gotson.komga.domain.persistence.KomgaUserRepository
import org.gotson.komga.domain.persistence.LibraryRepository
import org.gotson.komga.domain.persistence.MediaRepository
import org.gotson.komga.domain.persistence.ReadListRepository
import org.gotson.komga.domain.persistence.ReadProgressRepository
import org.gotson.komga.domain.service.BookLifecycle
import org.gotson.komga.domain.service.KomgaUserLifecycle
import org.gotson.komga.domain.service.LibraryLifecycle
import org.gotson.komga.domain.service.SeriesLifecycle
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
class BookSearchTest(
  @Autowired private val bookDao: BookDao,
  @Autowired private val bookDtoDao: BookDtoDao,
  @Autowired private val libraryRepository: LibraryRepository,
  @Autowired private val bookMetadataRepository: BookMetadataRepository,
  @Autowired private val mediaRepository: MediaRepository,
  @Autowired private val readProgressRepository: ReadProgressRepository,
  @Autowired private val userRepository: KomgaUserRepository,
  @Autowired private val bookRepository: BookRepository,
  @Autowired private val bookLifecycle: BookLifecycle,
  @Autowired private val seriesLifecycle: SeriesLifecycle,
  @Autowired private val libraryLifecycle: LibraryLifecycle,
  @Autowired private val userLifecycle: KomgaUserLifecycle,
  @Autowired private val readListRepository: ReadListRepository,
) {
  private val library1 = makeLibrary()
  private val library2 = makeLibrary()
  private val series1 = makeSeries("Series 1").copy(libraryId = library1.id)
  private val series2 = makeSeries("Series 2").copy(libraryId = library2.id)
  private val user1 = KomgaUser("user1@example.org", "p")
  private val user2 = KomgaUser("user2@example.org", "p")

  @MockkBean
  private lateinit var mockEventPublisher: ApplicationEventPublisher

  @BeforeAll
  fun setup() {
    every { mockEventPublisher.publishEvent(any()) } just Runs
    libraryRepository.insert(library1)
    libraryRepository.insert(library2)
    seriesLifecycle.createSeries(series1)
    seriesLifecycle.createSeries(series2)
    userRepository.insert(user1)
    userRepository.insert(user2)
  }

  @BeforeEach
  fun resetMocks() {
    every { mockEventPublisher.publishEvent(any()) } just Runs
  }

  @AfterEach
  fun deleteBooks() {
    readListRepository.deleteAll()
    bookLifecycle.deleteMany(bookRepository.findAll())
    assertThat(bookDao.count()).isEqualTo(0)
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
  fun `given some books when searching by library then results are accurate`() {
    val book1 = makeBook("1", libraryId = library1.id, seriesId = series1.id)
    val book2 = makeBook("2", libraryId = library2.id, seriesId = series2.id)
    seriesLifecycle.addBooks(series1, listOf(book1))
    seriesLifecycle.addBooks(series2, listOf(book2))

    run {
      val search = BookSearch(SearchCondition.LibraryId(SearchOperator.Is(library1.id)))
      val found = bookDao.findAll(search.condition, SearchContext(user1), UnpagedSorted(Sort.by("readList.number"))).content
      val foundDto = bookDtoDao.findAll(search, SearchContext(user1), UnpagedSorted(Sort.by("readList.number"))).content

      assertThat(found.map { it.name }).containsExactlyInAnyOrder("1")
      assertThat(foundDto.map { it.name }).containsExactlyInAnyOrder("1")
    }

    run {
      val search = BookSearch(SearchCondition.LibraryId(SearchOperator.IsNot(library1.id)))
      val found = bookDao.findAll(search.condition, SearchContext(user1), Pageable.unpaged()).content
      val foundDto = bookDtoDao.findAll(search, SearchContext(user1), Pageable.unpaged()).content

      assertThat(found.map { it.name }).containsExactlyInAnyOrder("2")
      assertThat(foundDto.map { it.name }).containsExactlyInAnyOrder("2")
    }
  }

  @Test
  fun `given some books when searching by series then results are accurate`() {
    val book1 = makeBook("1", libraryId = library1.id, seriesId = series1.id)
    val book2 = makeBook("2", libraryId = library2.id, seriesId = series2.id)
    seriesLifecycle.addBooks(series1, listOf(book1))
    seriesLifecycle.addBooks(series2, listOf(book2))

    run {
      val search = BookSearch(SearchCondition.SeriesId(SearchOperator.Is(series1.id)))
      val found = bookDao.findAll(search.condition, SearchContext(user1), Pageable.unpaged()).content
      val foundDto = bookDtoDao.findAll(search, SearchContext(user1), Pageable.unpaged()).content

      assertThat(found.map { it.name }).containsExactlyInAnyOrder("1")
      assertThat(foundDto.map { it.name }).containsExactlyInAnyOrder("1")
    }

    run {
      val search = BookSearch(SearchCondition.SeriesId(SearchOperator.IsNot(series1.id)))
      val found = bookDao.findAll(search.condition, SearchContext(user1), Pageable.unpaged()).content
      val foundDto = bookDtoDao.findAll(search, SearchContext(user1), Pageable.unpaged()).content

      assertThat(found.map { it.name }).containsExactlyInAnyOrder("2")
      assertThat(foundDto.map { it.name }).containsExactlyInAnyOrder("2")
    }
  }

  @Test
  fun `given some books when searching by read list then results are accurate`() {
    val book1 = makeBook("1", libraryId = library1.id, seriesId = series1.id)
    val book2 = makeBook("2", libraryId = library2.id, seriesId = series2.id)
    seriesLifecycle.addBooks(series1, listOf(book1))
    seriesLifecycle.addBooks(series2, listOf(book2))
    val readList = ReadList("rl1", bookIds = mapOf(1 to book1.id).toSortedMap())
    readListRepository.insert(readList)

    run {
      val search = BookSearch(SearchCondition.ReadListId(SearchOperator.Is(readList.id)))
      val found = bookDao.findAll(search.condition, SearchContext(user1), Pageable.unpaged()).content
      val foundDto = bookDtoDao.findAll(search, SearchContext(user1), Pageable.unpaged()).content

      assertThat(found.map { it.name }).containsExactlyInAnyOrder("1")
      assertThat(foundDto.map { it.name }).containsExactlyInAnyOrder("1")
    }

    run {
      val search = BookSearch(SearchCondition.ReadListId(SearchOperator.IsNot(readList.id)))
      val found = bookDao.findAll(search.condition, SearchContext(user1), Pageable.unpaged()).content
      val foundDto = bookDtoDao.findAll(search, SearchContext(user1), Pageable.unpaged()).content

      assertThat(found.map { it.name }).containsExactlyInAnyOrder("2")
      assertThat(foundDto.map { it.name }).containsExactlyInAnyOrder("2")
    }
  }

  @Test
  fun `given some books in multiple read lists when searching by read list then results are accurate`() {
    val book1 = makeBook("1", libraryId = library1.id, seriesId = series1.id)
    val book2 = makeBook("2", libraryId = library2.id, seriesId = series2.id)
    seriesLifecycle.addBooks(series1, listOf(book1))
    seriesLifecycle.addBooks(series2, listOf(book2))
    val readList1 = ReadList("rl1", bookIds = mapOf(1 to book1.id, 2 to book2.id).toSortedMap())
    val readList2 = ReadList("rl2", bookIds = mapOf(1 to book2.id, 2 to book1.id).toSortedMap())
    readListRepository.insert(readList1)
    readListRepository.insert(readList2)

    // search by readList 1
    run {
      val search = BookSearch(SearchCondition.ReadListId(SearchOperator.Is(readList1.id)))
      val found = bookDao.findAll(search.condition, SearchContext(user1), UnpagedSorted(Sort.by(Sort.Order.asc("readList.number")))).content
      val foundDto = bookDtoDao.findAll(search, SearchContext(user1), UnpagedSorted(Sort.by(Sort.Order.asc("readList.number")))).content

      // order not guaranteed for bookDao
      assertThat(found.map { it.name }).containsExactlyInAnyOrder("1", "2")
      assertThat(foundDto.map { it.name }).containsExactly("1", "2")
    }

    // search by readList 2
    run {
      val search = BookSearch(SearchCondition.ReadListId(SearchOperator.Is(readList2.id)))
      val found = bookDao.findAll(search.condition, SearchContext(user1), UnpagedSorted(Sort.by(Sort.Order.asc("readList.number")))).content
      val foundDto = bookDtoDao.findAll(search, SearchContext(user1), UnpagedSorted(Sort.by(Sort.Order.asc("readList.number")))).content

      // order not guaranteed for bookDao
      assertThat(found.map { it.name }).containsExactlyInAnyOrder("2", "1")
      assertThat(foundDto.map { it.name }).containsExactly("2", "1")
    }

    // search by readList 1 or 2 - order is not guaranteed in that case
    run {
      val search =
        BookSearch(
          SearchCondition.AnyOfBook(
            SearchCondition.ReadListId(SearchOperator.Is(readList1.id)),
            SearchCondition.ReadListId(SearchOperator.Is(readList2.id)),
          ),
        )
      val found = bookDao.findAll(search.condition, SearchContext(user1), UnpagedSorted(Sort.by(Sort.Order.asc("readList.number")))).content
      val foundDto = bookDtoDao.findAll(search, SearchContext(user1), UnpagedSorted(Sort.by(Sort.Order.asc("readList.number")))).content

      assertThat(found.map { it.name }).containsExactlyInAnyOrder("2", "1")
      assertThat(foundDto.map { it.name }).containsExactlyInAnyOrder("2", "1")
    }
  }

  @Test
  fun `given some books when searching by deleted then results are accurate`() {
    val book1 = makeBook("1", libraryId = library1.id, seriesId = series1.id).copy(deletedDate = LocalDateTime.now())
    val book2 = makeBook("2", libraryId = library2.id, seriesId = series2.id)
    seriesLifecycle.addBooks(series1, listOf(book1))
    seriesLifecycle.addBooks(series2, listOf(book2))

    run {
      val search = BookSearch(SearchCondition.Deleted(SearchOperator.IsTrue))
      val found = bookDao.findAll(search.condition, SearchContext(user1), Pageable.unpaged()).content
      val foundDto = bookDtoDao.findAll(search, SearchContext(user1), Pageable.unpaged()).content

      assertThat(found.map { it.name }).containsExactlyInAnyOrder("1")
      assertThat(foundDto.map { it.name }).containsExactlyInAnyOrder("1")
    }

    run {
      val search = BookSearch(SearchCondition.Deleted(SearchOperator.IsFalse))
      val found = bookDao.findAll(search.condition, SearchContext(user1), Pageable.unpaged()).content
      val foundDto = bookDtoDao.findAll(search, SearchContext(user1), Pageable.unpaged()).content

      assertThat(found.map { it.name }).containsExactlyInAnyOrder("2")
      assertThat(foundDto.map { it.name }).containsExactlyInAnyOrder("2")
    }
  }

  @Test
  fun `given some books when searching by title then results are accurate`() {
    makeBook("1", libraryId = library1.id, seriesId = series1.id).let { book ->
      seriesLifecycle.addBooks(series1, listOf(book))
      bookMetadataRepository.findById(book.id).let {
        bookMetadataRepository.update(it.copy(title = "Book 1"))
      }
    }
    makeBook("2", libraryId = library2.id, seriesId = series2.id).let { book ->
      seriesLifecycle.addBooks(series2, listOf(book))
      bookMetadataRepository.findById(book.id).let {
        bookMetadataRepository.update(it.copy(title = "Book 2"))
      }
    }

    run {
      val search = BookSearch(SearchCondition.Title(SearchOperator.Is("boOK 1")))
      val found = bookDao.findAll(search.condition, SearchContext(user1), Pageable.unpaged()).content
      val foundDto = bookDtoDao.findAll(search, SearchContext(user1), Pageable.unpaged()).content

      assertThat(found.map { it.name }).containsExactlyInAnyOrder("1")
      assertThat(foundDto.map { it.name }).containsExactlyInAnyOrder("1")
    }

    run {
      val search = BookSearch(SearchCondition.Title(SearchOperator.IsNot("book 1")))
      val found = bookDao.findAll(search.condition, SearchContext(user1), Pageable.unpaged()).content
      val foundDto = bookDtoDao.findAll(search, SearchContext(user1), Pageable.unpaged()).content

      assertThat(found.map { it.name }).containsExactlyInAnyOrder("2")
      assertThat(foundDto.map { it.name }).containsExactlyInAnyOrder("2")
    }

    run {
      val search = BookSearch(SearchCondition.Title(SearchOperator.Contains("book")))
      val found = bookDao.findAll(search.condition, SearchContext(user1), Pageable.unpaged()).content
      val foundDto = bookDtoDao.findAll(search, SearchContext(user1), Pageable.unpaged()).content

      assertThat(found.map { it.name }).containsExactlyInAnyOrder("1", "2")
      assertThat(foundDto.map { it.name }).containsExactlyInAnyOrder("1", "2")
    }

    run {
      val search = BookSearch(SearchCondition.Title(SearchOperator.DoesNotContain("1")))
      val found = bookDao.findAll(search.condition, SearchContext(user1), Pageable.unpaged()).content
      val foundDto = bookDtoDao.findAll(search, SearchContext(user1), Pageable.unpaged()).content

      assertThat(found.map { it.name }).containsExactlyInAnyOrder("2")
      assertThat(foundDto.map { it.name }).containsExactlyInAnyOrder("2")
    }

    run {
      val search = BookSearch(SearchCondition.Title(SearchOperator.BeginsWith("book")))
      val found = bookDao.findAll(search.condition, SearchContext(user1), Pageable.unpaged()).content
      val foundDto = bookDtoDao.findAll(search, SearchContext(user1), Pageable.unpaged()).content

      assertThat(found.map { it.name }).containsExactlyInAnyOrder("1", "2")
      assertThat(foundDto.map { it.name }).containsExactlyInAnyOrder("1", "2")
    }

    run {
      val search = BookSearch(SearchCondition.Title(SearchOperator.EndsWith("1")))
      val found = bookDao.findAll(search.condition, SearchContext(user1), Pageable.unpaged()).content
      val foundDto = bookDtoDao.findAll(search, SearchContext(user1), Pageable.unpaged()).content

      assertThat(found.map { it.name }).containsExactlyInAnyOrder("1")
      assertThat(foundDto.map { it.name }).containsExactlyInAnyOrder("1")
    }
  }

  @Test
  fun `given some books when searching by release date then results are accurate`() {
    makeBook("1", libraryId = library1.id, seriesId = series1.id).let { book ->
      seriesLifecycle.addBooks(series1, listOf(book))
      bookMetadataRepository.findById(book.id).let {
        bookMetadataRepository.update(it.copy(releaseDate = LocalDate.now().minusDays(5)))
      }
    }
    makeBook("2", libraryId = library2.id, seriesId = series2.id).let { book ->
      seriesLifecycle.addBooks(series2, listOf(book))
    }

    run {
      val search = BookSearch(SearchCondition.ReleaseDate(SearchOperator.After(ZonedDateTime.now().minusDays(10))))
      val found = bookDao.findAll(search.condition, SearchContext(user1), Pageable.unpaged()).content
      val foundDto = bookDtoDao.findAll(search, SearchContext(user1), Pageable.unpaged()).content

      assertThat(found.map { it.name }).containsExactlyInAnyOrder("1")
      assertThat(foundDto.map { it.name }).containsExactlyInAnyOrder("1")
    }

    run {
      val search = BookSearch(SearchCondition.ReleaseDate(SearchOperator.Before(ZonedDateTime.now())))
      val found = bookDao.findAll(search.condition, SearchContext(user1), Pageable.unpaged()).content
      val foundDto = bookDtoDao.findAll(search, SearchContext(user1), Pageable.unpaged()).content

      assertThat(found.map { it.name }).containsExactlyInAnyOrder("1")
      assertThat(foundDto.map { it.name }).containsExactlyInAnyOrder("1")
    }

    run {
      val search = BookSearch(SearchCondition.ReleaseDate(SearchOperator.IsInTheLast(Duration.ofDays(10))))
      val found = bookDao.findAll(search.condition, SearchContext(user1), Pageable.unpaged()).content
      val foundDto = bookDtoDao.findAll(search, SearchContext(user1), Pageable.unpaged()).content

      assertThat(found.map { it.name }).containsExactlyInAnyOrder("1")
      assertThat(foundDto.map { it.name }).containsExactlyInAnyOrder("1")
    }

    run {
      val search = BookSearch(SearchCondition.ReleaseDate(SearchOperator.IsNotInTheLast(Duration.ofDays(10))))
      val found = bookDao.findAll(search.condition, SearchContext(user1), Pageable.unpaged()).content
      val foundDto = bookDtoDao.findAll(search, SearchContext(user1), Pageable.unpaged()).content

      assertThat(found).isEmpty()
      assertThat(foundDto).isEmpty()
    }

    run {
      val search = BookSearch(SearchCondition.ReleaseDate(SearchOperator.IsNotInTheLast(Duration.ofDays(1))))
      val found = bookDao.findAll(search.condition, SearchContext(user1), Pageable.unpaged()).content
      val foundDto = bookDtoDao.findAll(search, SearchContext(user1), Pageable.unpaged()).content

      assertThat(found.map { it.name }).containsExactlyInAnyOrder("1")
      assertThat(foundDto.map { it.name }).containsExactlyInAnyOrder("1")
    }

    run {
      val search = BookSearch(SearchCondition.ReleaseDate(SearchOperator.IsNull))
      val found = bookDao.findAll(search.condition, SearchContext(user1), Pageable.unpaged()).content
      val foundDto = bookDtoDao.findAll(search, SearchContext(user1), Pageable.unpaged()).content

      assertThat(found.map { it.name }).containsExactlyInAnyOrder("2")
      assertThat(foundDto.map { it.name }).containsExactlyInAnyOrder("2")
    }

    run {
      val search = BookSearch(SearchCondition.ReleaseDate(SearchOperator.IsNotNull))
      val found = bookDao.findAll(search.condition, SearchContext(user1), Pageable.unpaged()).content
      val foundDto = bookDtoDao.findAll(search, SearchContext(user1), Pageable.unpaged()).content

      assertThat(found.map { it.name }).containsExactlyInAnyOrder("1")
      assertThat(foundDto.map { it.name }).containsExactlyInAnyOrder("1")
    }
  }

  @Test
  fun `given some books when searching by number sort then results are accurate`() {
    makeBook("1", libraryId = library1.id, seriesId = series1.id).let { book ->
      seriesLifecycle.addBooks(series1, listOf(book))
      bookMetadataRepository.findById(book.id).let {
        bookMetadataRepository.update(it.copy(numberSort = 1F))
      }
    }
    makeBook("2", libraryId = library2.id, seriesId = series2.id).let { book ->
      seriesLifecycle.addBooks(series2, listOf(book))
      bookMetadataRepository.findById(book.id).let {
        bookMetadataRepository.update(it.copy(numberSort = 10.5F))
      }
    }

    run {
      val search = BookSearch(SearchCondition.NumberSort(SearchOperator.Is(10.5F)))
      val found = bookDao.findAll(search.condition, SearchContext(user1), Pageable.unpaged()).content
      val foundDto = bookDtoDao.findAll(search, SearchContext(user1), Pageable.unpaged()).content

      assertThat(found.map { it.name }).containsExactlyInAnyOrder("2")
      assertThat(foundDto.map { it.name }).containsExactlyInAnyOrder("2")
    }

    run {
      val search = BookSearch(SearchCondition.NumberSort(SearchOperator.IsNot(10.5F)))
      val found = bookDao.findAll(search.condition, SearchContext(user1), Pageable.unpaged()).content
      val foundDto = bookDtoDao.findAll(search, SearchContext(user1), Pageable.unpaged()).content

      assertThat(found.map { it.name }).containsExactlyInAnyOrder("1")
      assertThat(foundDto.map { it.name }).containsExactlyInAnyOrder("1")
    }

    run {
      val search = BookSearch(SearchCondition.NumberSort(SearchOperator.GreaterThan(0F)))
      val found = bookDao.findAll(search.condition, SearchContext(user1), Pageable.unpaged()).content
      val foundDto = bookDtoDao.findAll(search, SearchContext(user1), Pageable.unpaged()).content

      assertThat(found.map { it.name }).containsExactlyInAnyOrder("1", "2")
      assertThat(foundDto.map { it.name }).containsExactlyInAnyOrder("1", "2")
    }

    run {
      val search = BookSearch(SearchCondition.NumberSort(SearchOperator.GreaterThan(5F)))
      val found = bookDao.findAll(search.condition, SearchContext(user1), Pageable.unpaged()).content
      val foundDto = bookDtoDao.findAll(search, SearchContext(user1), Pageable.unpaged()).content

      assertThat(found.map { it.name }).containsExactlyInAnyOrder("2")
      assertThat(foundDto.map { it.name }).containsExactlyInAnyOrder("2")
    }

    run {
      val search = BookSearch(SearchCondition.NumberSort(SearchOperator.LessThan(11F)))
      val found = bookDao.findAll(search.condition, SearchContext(user1), Pageable.unpaged()).content
      val foundDto = bookDtoDao.findAll(search, SearchContext(user1), Pageable.unpaged()).content

      assertThat(found.map { it.name }).containsExactlyInAnyOrder("1", "2")
      assertThat(foundDto.map { it.name }).containsExactlyInAnyOrder("1", "2")
    }

    run {
      val search =
        BookSearch(
          SearchCondition.AnyOfBook(
            listOf(
              SearchCondition.NumberSort(SearchOperator.Is(10.5F)),
              SearchCondition.NumberSort(SearchOperator.Is(1F)),
            ),
          ),
        )
      val found = bookDao.findAll(search.condition, SearchContext(user1), Pageable.unpaged()).content
      val foundDto = bookDtoDao.findAll(search, SearchContext(user1), Pageable.unpaged()).content

      assertThat(found.map { it.name }).containsExactlyInAnyOrder("1", "2")
      assertThat(foundDto.map { it.name }).containsExactlyInAnyOrder("1", "2")
    }

    run {
      val search =
        BookSearch(
          SearchCondition.AllOfBook(
            listOf(
              SearchCondition.NumberSort(SearchOperator.Is(10.5F)),
              SearchCondition.NumberSort(SearchOperator.Is(1F)),
            ),
          ),
        )
      val found = bookDao.findAll(search.condition, SearchContext(user1), Pageable.unpaged()).content
      val foundDto = bookDtoDao.findAll(search, SearchContext(user1), Pageable.unpaged()).content

      assertThat(found).isEmpty()
      assertThat(foundDto).isEmpty()
    }
  }

  @Test
  fun `given some books when searching by tag then results are accurate`() {
    makeBook("1", libraryId = library1.id, seriesId = series1.id).let { book ->
      seriesLifecycle.addBooks(series1, listOf(book))
      bookMetadataRepository.findById(book.id).let {
        bookMetadataRepository.update(it.copy(tags = setOf("fiction", "horror")))
      }
    }
    makeBook("2", libraryId = library2.id, seriesId = series2.id).let { book ->
      seriesLifecycle.addBooks(series2, listOf(book))
      bookMetadataRepository.findById(book.id).let {
        bookMetadataRepository.update(it.copy(tags = setOf("fiction")))
      }
    }
    makeBook("3", libraryId = library2.id, seriesId = series2.id).let { book ->
      seriesLifecycle.addBooks(series2, listOf(book))
      bookMetadataRepository.findById(book.id).let {
        bookMetadataRepository.update(it.copy(tags = setOf("fantasy")))
      }
    }
    makeBook("4", libraryId = library2.id, seriesId = series2.id).let { book ->
      seriesLifecycle.addBooks(series2, listOf(book))
    }

    run {
      val search = BookSearch(SearchCondition.Tag(SearchOperator.Is("FICTION")))
      val found = bookDao.findAll(search.condition, SearchContext(user1), Pageable.unpaged()).content
      val foundDto = bookDtoDao.findAll(search, SearchContext(user1), Pageable.unpaged()).content

      assertThat(found.map { it.name }).containsExactlyInAnyOrder("1", "2")
      assertThat(foundDto.map { it.name }).containsExactlyInAnyOrder("1", "2")
    }

    run {
      val search =
        BookSearch(
          SearchCondition.AllOfBook(
            listOf(
              SearchCondition.Tag(SearchOperator.Is("FICTION")),
              SearchCondition.Tag(SearchOperator.Is("horror")),
            ),
          ),
        )
      val found = bookDao.findAll(search.condition, SearchContext(user1), Pageable.unpaged()).content
      val foundDto = bookDtoDao.findAll(search, SearchContext(user1), Pageable.unpaged()).content

      assertThat(found.map { it.name }).containsExactlyInAnyOrder("1")
      assertThat(foundDto.map { it.name }).containsExactlyInAnyOrder("1")
    }

    run {
      val search =
        BookSearch(
          SearchCondition.AnyOfBook(
            listOf(
              SearchCondition.Tag(SearchOperator.Is("horror")),
              SearchCondition.Tag(SearchOperator.Is("notexist")),
              SearchCondition.Tag(SearchOperator.Is("fantasy")),
            ),
          ),
        )
      val found = bookDao.findAll(search.condition, SearchContext(user1), Pageable.unpaged()).content
      val foundDto = bookDtoDao.findAll(search, SearchContext(user1), Pageable.unpaged()).content

      assertThat(found.map { it.name }).containsExactlyInAnyOrder("1", "3")
      assertThat(foundDto.map { it.name }).containsExactlyInAnyOrder("1", "3")
    }

    run {
      val search =
        BookSearch(
          SearchCondition.AllOfBook(
            listOf(
              SearchCondition.Tag(SearchOperator.Is("fiction")),
              SearchCondition.Tag(SearchOperator.IsNot("horror")),
            ),
          ),
        )
      val found = bookDao.findAll(search.condition, SearchContext(user1), Pageable.unpaged()).content
      val foundDto = bookDtoDao.findAll(search, SearchContext(user1), Pageable.unpaged()).content

      assertThat(found.map { it.name }).containsExactlyInAnyOrder("2")
      assertThat(foundDto.map { it.name }).containsExactlyInAnyOrder("2")
    }

    run {
      val search = BookSearch(SearchCondition.Tag(SearchOperator.IsNot("fiction")))
      val found = bookDao.findAll(search.condition, SearchContext(user1), Pageable.unpaged()).content
      val foundDto = bookDtoDao.findAll(search, SearchContext(user1), Pageable.unpaged()).content

      assertThat(found.map { it.name }).containsExactlyInAnyOrder("3", "4")
      assertThat(foundDto.map { it.name }).containsExactlyInAnyOrder("3", "4")
    }
  }

  @Test
  fun `given some books when searching by media status then results are accurate`() {
    makeBook("1", libraryId = library1.id, seriesId = series1.id).let { book ->
      seriesLifecycle.addBooks(series1, listOf(book))
      mediaRepository.findById(book.id).let {
        mediaRepository.update(it.copy(status = Media.Status.READY))
      }
    }
    makeBook("2", libraryId = library2.id, seriesId = series2.id).let { book ->
      seriesLifecycle.addBooks(series2, listOf(book))
      mediaRepository.findById(book.id).let {
        mediaRepository.update(it.copy(status = Media.Status.ERROR))
      }
    }

    run {
      val search = BookSearch(SearchCondition.MediaStatus(SearchOperator.Is(Media.Status.READY)))
      val found = bookDao.findAll(search.condition, SearchContext(user1), Pageable.unpaged()).content
      val foundDto = bookDtoDao.findAll(search, SearchContext(user1), Pageable.unpaged()).content

      assertThat(found.map { it.name }).containsExactlyInAnyOrder("1")
      assertThat(foundDto.map { it.name }).containsExactlyInAnyOrder("1")
    }

    run {
      val search = BookSearch(SearchCondition.MediaStatus(SearchOperator.IsNot(Media.Status.READY)))
      val found = bookDao.findAll(search.condition, SearchContext(user1), Pageable.unpaged()).content
      val foundDto = bookDtoDao.findAll(search, SearchContext(user1), Pageable.unpaged()).content

      assertThat(found.map { it.name }).containsExactlyInAnyOrder("2")
      assertThat(foundDto.map { it.name }).containsExactlyInAnyOrder("2")
    }

    run {
      val search =
        BookSearch(
          SearchCondition.AllOfBook(
            listOf(
              SearchCondition.MediaStatus(SearchOperator.Is(Media.Status.READY)),
              SearchCondition.MediaStatus(SearchOperator.Is(Media.Status.ERROR)),
            ),
          ),
        )
      val found = bookDao.findAll(search.condition, SearchContext(user1), Pageable.unpaged()).content
      val foundDto = bookDtoDao.findAll(search, SearchContext(user1), Pageable.unpaged()).content

      assertThat(found).isEmpty()
      assertThat(foundDto).isEmpty()
    }

    run {
      val search =
        BookSearch(
          SearchCondition.AnyOfBook(
            listOf(
              SearchCondition.MediaStatus(SearchOperator.Is(Media.Status.READY)),
              SearchCondition.MediaStatus(SearchOperator.Is(Media.Status.ERROR)),
            ),
          ),
        )
      val found = bookDao.findAll(search.condition, SearchContext(user1), Pageable.unpaged()).content
      val foundDto = bookDtoDao.findAll(search, SearchContext(user1), Pageable.unpaged()).content

      assertThat(found.map { it.name }).containsExactlyInAnyOrder("1", "2")
      assertThat(foundDto.map { it.name }).containsExactlyInAnyOrder("1", "2")
    }
  }

  @Test
  fun `given some books when searching by media profile then results are accurate`() {
    makeBook("1", libraryId = library1.id, seriesId = series1.id).let { book ->
      seriesLifecycle.addBooks(series1, listOf(book))
      mediaRepository.findById(book.id).let {
        mediaRepository.update(it.copy(mediaType = MediaType.ZIP.type))
      }
    }
    makeBook("2", libraryId = library2.id, seriesId = series2.id).let { book ->
      seriesLifecycle.addBooks(series2, listOf(book))
      mediaRepository.findById(book.id).let {
        mediaRepository.update(it.copy(mediaType = MediaType.RAR_4.type))
      }
    }
    makeBook("3", libraryId = library2.id, seriesId = series2.id).let { book ->
      seriesLifecycle.addBooks(series2, listOf(book))
      mediaRepository.findById(book.id).let {
        mediaRepository.update(it.copy(mediaType = MediaType.EPUB.type))
      }
    }

    run {
      val search = BookSearch(SearchCondition.MediaProfile(SearchOperator.Is(MediaProfile.DIVINA)))
      val found = bookDao.findAll(search.condition, SearchContext(user1), Pageable.unpaged()).content
      val foundDto = bookDtoDao.findAll(search, SearchContext(user1), Pageable.unpaged()).content

      assertThat(found.map { it.name }).containsExactlyInAnyOrder("1", "2")
      assertThat(foundDto.map { it.name }).containsExactlyInAnyOrder("1", "2")
    }

    run {
      val search = BookSearch(SearchCondition.MediaProfile(SearchOperator.IsNot(MediaProfile.EPUB)))
      val found = bookDao.findAll(search.condition, SearchContext(user1), Pageable.unpaged()).content
      val foundDto = bookDtoDao.findAll(search, SearchContext(user1), Pageable.unpaged()).content

      assertThat(found.map { it.name }).containsExactlyInAnyOrder("1", "2")
      assertThat(foundDto.map { it.name }).containsExactlyInAnyOrder("1", "2")
    }

    run {
      val search = BookSearch(SearchCondition.MediaProfile(SearchOperator.Is(MediaProfile.EPUB)))
      val found = bookDao.findAll(search.condition, SearchContext(user1), Pageable.unpaged()).content
      val foundDto = bookDtoDao.findAll(search, SearchContext(user1), Pageable.unpaged()).content

      assertThat(found.map { it.name }).containsExactlyInAnyOrder("3")
      assertThat(foundDto.map { it.name }).containsExactlyInAnyOrder("3")
    }

    run {
      val search =
        BookSearch(
          SearchCondition.AllOfBook(
            listOf(
              SearchCondition.MediaProfile(SearchOperator.Is(MediaProfile.DIVINA)),
              SearchCondition.MediaProfile(SearchOperator.Is(MediaProfile.EPUB)),
            ),
          ),
        )
      val found = bookDao.findAll(search.condition, SearchContext(user1), Pageable.unpaged()).content
      val foundDto = bookDtoDao.findAll(search, SearchContext(user1), Pageable.unpaged()).content

      assertThat(found).isEmpty()
      assertThat(foundDto).isEmpty()
    }

    run {
      val search =
        BookSearch(
          SearchCondition.AnyOfBook(
            listOf(
              SearchCondition.MediaProfile(SearchOperator.Is(MediaProfile.DIVINA)),
              SearchCondition.MediaProfile(SearchOperator.Is(MediaProfile.EPUB)),
            ),
          ),
        )
      val found = bookDao.findAll(search.condition, SearchContext(user1), Pageable.unpaged()).content
      val foundDto = bookDtoDao.findAll(search, SearchContext(user1), Pageable.unpaged()).content

      assertThat(found.map { it.name }).containsExactlyInAnyOrder("1", "2", "3")
      assertThat(foundDto.map { it.name }).containsExactlyInAnyOrder("1", "2", "3")
    }
  }

  @Test
  fun `given some books when searching by read progress then results are accurate`() {
    makeBook("1", libraryId = library1.id, seriesId = series1.id).let { book ->
      seriesLifecycle.addBooks(series1, listOf(book))
      readProgressRepository.save(ReadProgress(book.id, user1.id, 5, false))
    }
    makeBook("2", libraryId = library2.id, seriesId = series2.id).let { book ->
      seriesLifecycle.addBooks(series2, listOf(book))
      readProgressRepository.save(ReadProgress(book.id, user1.id, 10, true))
    }
    makeBook("3", libraryId = library2.id, seriesId = series2.id).let { book ->
      seriesLifecycle.addBooks(series2, listOf(book))
    }

    run {
      val search = BookSearch(SearchCondition.ReadStatus(SearchOperator.Is(ReadStatus.IN_PROGRESS)))
      val found = bookDao.findAll(search.condition, SearchContext(user1), Pageable.unpaged()).content
      val foundDto = bookDtoDao.findAll(search, SearchContext(user1), Pageable.unpaged()).content

      assertThat(found.map { it.name }).containsExactlyInAnyOrder("1")
      assertThat(foundDto.map { it.name }).containsExactlyInAnyOrder("1")
    }

    run {
      val search = BookSearch(SearchCondition.ReadStatus(SearchOperator.Is(ReadStatus.UNREAD)))
      val found = bookDao.findAll(search.condition, SearchContext(user1), Pageable.unpaged()).content
      val foundDto = bookDtoDao.findAll(search, SearchContext(user1), Pageable.unpaged()).content

      assertThat(found.map { it.name }).containsExactlyInAnyOrder("3")
      assertThat(foundDto.map { it.name }).containsExactlyInAnyOrder("3")
    }

    run {
      val search = BookSearch(SearchCondition.ReadStatus(SearchOperator.Is(ReadStatus.READ)))
      val found = bookDao.findAll(search.condition, SearchContext(user1), Pageable.unpaged()).content
      val foundDto = bookDtoDao.findAll(search, SearchContext(user1), Pageable.unpaged()).content

      assertThat(found.map { it.name }).containsExactlyInAnyOrder("2")
      assertThat(foundDto.map { it.name }).containsExactlyInAnyOrder("2")
    }

    run {
      val search = BookSearch(SearchCondition.ReadStatus(SearchOperator.Is(ReadStatus.READ)))
      val found = bookDao.findAll(search.condition, SearchContext(user2), Pageable.unpaged()).content
      val foundDto = bookDtoDao.findAll(search, SearchContext(user2), Pageable.unpaged()).content

      assertThat(found).isEmpty()
      assertThat(foundDto).isEmpty()
    }

    run {
      val search = BookSearch(SearchCondition.ReadStatus(SearchOperator.Is(ReadStatus.UNREAD)))
      val found = bookDao.findAll(search.condition, SearchContext(user2), Pageable.unpaged()).content
      val foundDto = bookDtoDao.findAll(search, SearchContext(user2), Pageable.unpaged()).content

      assertThat(found.map { it.name }).containsExactlyInAnyOrder("1", "2", "3")
      assertThat(foundDto.map { it.name }).containsExactlyInAnyOrder("1", "2", "3")
    }

    run {
      val search = BookSearch(SearchCondition.ReadStatus(SearchOperator.Is(ReadStatus.READ)))
      val found = bookDao.findAll(search.condition, SearchContext(null), Pageable.unpaged()).content
      val thrown = catchThrowable { bookDtoDao.findAll(search, SearchContext(null), Pageable.unpaged()) }

      assertThat(found).isEmpty()
      assertThat(thrown).isInstanceOf(IllegalArgumentException::class.java)
    }
  }

  @Test
  fun `given some books when searching by author then results are accurate`() {
    makeBook("1", libraryId = library1.id, seriesId = series1.id).let { book ->
      seriesLifecycle.addBooks(series1, listOf(book))
      bookMetadataRepository.findById(book.id).let {
        bookMetadataRepository.update(it.copy(authors = listOf(Author("john", "writer"), Author("jim", "cover"))))
      }
    }
    makeBook("2", libraryId = library2.id, seriesId = series2.id).let { book ->
      seriesLifecycle.addBooks(series2, listOf(book))
      bookMetadataRepository.findById(book.id).let {
        bookMetadataRepository.update(it.copy(authors = listOf(Author("john", "artist"), Author("amanda", "artist"))))
      }
    }
    makeBook("3", libraryId = library2.id, seriesId = series2.id).let { book ->
      seriesLifecycle.addBooks(series2, listOf(book))
      bookMetadataRepository.findById(book.id).let {
        bookMetadataRepository.update(it.copy(authors = listOf(Author("jack", "writer"))))
      }
    }
    makeBook("4", libraryId = library2.id, seriesId = series2.id).let { book ->
      seriesLifecycle.addBooks(series2, listOf(book))
    }

    // books with an author named 'john'
    run {
      val search =
        BookSearch(
          SearchCondition.Author(SearchOperator.Is(AuthorMatch("john"))),
        )
      val found = bookDao.findAll(search.condition, SearchContext(user1), Pageable.unpaged()).content
      val foundDto = bookDtoDao.findAll(search, SearchContext(user1), Pageable.unpaged()).content

      assertThat(found.map { it.name }).containsExactlyInAnyOrder("1", "2")
      assertThat(foundDto.map { it.name }).containsExactlyInAnyOrder("1", "2")
    }

    // books with an author named 'john' with the 'writer' role
    run {
      val search =
        BookSearch(
          SearchCondition.Author(SearchOperator.Is(AuthorMatch("john", "writer"))),
        )
      val found = bookDao.findAll(search.condition, SearchContext(user1), Pageable.unpaged()).content
      val foundDto = bookDtoDao.findAll(search, SearchContext(user1), Pageable.unpaged()).content

      assertThat(found.map { it.name }).containsExactlyInAnyOrder("1")
      assertThat(foundDto.map { it.name }).containsExactlyInAnyOrder("1")
    }

    // books with any author with the 'writer' role
    run {
      val search =
        BookSearch(
          SearchCondition.Author(SearchOperator.Is(AuthorMatch(role = "writer"))),
        )
      val found = bookDao.findAll(search.condition, SearchContext(user1), Pageable.unpaged()).content
      val foundDto = bookDtoDao.findAll(search, SearchContext(user1), Pageable.unpaged()).content

      assertThat(found.map { it.name }).containsExactlyInAnyOrder("1", "3")
      assertThat(foundDto.map { it.name }).containsExactlyInAnyOrder("1", "3")
    }

    // books without any author named 'john' with a 'writer' role
    run {
      val search =
        BookSearch(
          SearchCondition.Author(SearchOperator.IsNot(AuthorMatch("john", "writer"))),
        )
      val found = bookDao.findAll(search.condition, SearchContext(user1), Pageable.unpaged()).content
      val foundDto = bookDtoDao.findAll(search, SearchContext(user1), Pageable.unpaged()).content

      assertThat(found.map { it.name }).containsExactlyInAnyOrder("2", "3", "4")
      assertThat(foundDto.map { it.name }).containsExactlyInAnyOrder("2", "3", "4")
    }

    // books without any author named 'john'
    run {
      val search =
        BookSearch(
          SearchCondition.Author(SearchOperator.IsNot(AuthorMatch("jOhn"))),
        )
      val found = bookDao.findAll(search.condition, SearchContext(user1), Pageable.unpaged()).content
      val foundDto = bookDtoDao.findAll(search, SearchContext(user1), Pageable.unpaged()).content

      assertThat(found.map { it.name }).containsExactlyInAnyOrder("3", "4")
      assertThat(foundDto.map { it.name }).containsExactlyInAnyOrder("3", "4")
    }

    // books without any author with the 'writer' role
    run {
      val search =
        BookSearch(
          SearchCondition.Author(SearchOperator.IsNot(AuthorMatch(role = "writer"))),
        )
      val found = bookDao.findAll(search.condition, SearchContext(user1), Pageable.unpaged()).content
      val foundDto = bookDtoDao.findAll(search, SearchContext(user1), Pageable.unpaged()).content

      assertThat(found.map { it.name }).containsExactlyInAnyOrder("2", "4")
      assertThat(foundDto.map { it.name }).containsExactlyInAnyOrder("2", "4")
    }

    // empty AuthorMatch does not apply any condition
    run {
      val search =
        BookSearch(
          SearchCondition.Author(SearchOperator.Is(AuthorMatch())),
        )
      val found = bookDao.findAll(search.condition, SearchContext(user1), Pageable.unpaged()).content
      val foundDto = bookDtoDao.findAll(search, SearchContext(user1), Pageable.unpaged()).content

      assertThat(found.map { it.name }).containsExactlyInAnyOrder("1", "2", "3", "4")
      assertThat(foundDto.map { it.name }).containsExactlyInAnyOrder("1", "2", "3", "4")
    }
  }

  @Test
  fun `given some books when searching by one-shot then results are accurate`() {
    makeBook("1", libraryId = library1.id, seriesId = series1.id).copy(oneshot = true).let { book ->
      seriesLifecycle.addBooks(series1, listOf(book))
    }
    makeBook("2", libraryId = library2.id, seriesId = series2.id).let { book ->
      seriesLifecycle.addBooks(series2, listOf(book))
    }

    run {
      val search =
        BookSearch(
          SearchCondition.OneShot(SearchOperator.IsTrue),
        )
      val found = bookDao.findAll(search.condition, SearchContext(user1), Pageable.unpaged()).content
      val foundDto = bookDtoDao.findAll(search, SearchContext(user1), Pageable.unpaged()).content

      assertThat(found.map { it.name }).containsExactlyInAnyOrder("1")
      assertThat(foundDto.map { it.name }).containsExactlyInAnyOrder("1")
    }

    run {
      val search =
        BookSearch(
          SearchCondition.OneShot(SearchOperator.IsFalse),
        )
      val found = bookDao.findAll(search.condition, SearchContext(user1), Pageable.unpaged()).content
      val foundDto = bookDtoDao.findAll(search, SearchContext(user1), Pageable.unpaged()).content

      assertThat(found.map { it.name }).containsExactlyInAnyOrder("2")
      assertThat(foundDto.map { it.name }).containsExactlyInAnyOrder("2")
    }
  }

  @Test
  fun `given some books when searching by poster then results are accurate`() {
    // book with GENERATED selected
    makeBook("1", libraryId = library1.id, seriesId = series1.id).let { book ->
      seriesLifecycle.addBooks(series1, listOf(book))
      bookLifecycle.addThumbnailForBook(ThumbnailBook(bookId = book.id, type = ThumbnailBook.Type.GENERATED, mediaType = "image/jpeg", fileSize = 0L, dimension = Dimension(0, 0)), MarkSelectedPreference.YES)
    }
    // book with GENERATED not selected, SIDECAR selected
    makeBook("2", libraryId = library2.id, seriesId = series2.id).let { book ->
      seriesLifecycle.addBooks(series2, listOf(book))
      bookLifecycle.addThumbnailForBook(ThumbnailBook(bookId = book.id, type = ThumbnailBook.Type.GENERATED, mediaType = "image/jpeg", fileSize = 0L, dimension = Dimension(0, 0)), MarkSelectedPreference.YES)
      bookLifecycle.addThumbnailForBook(ThumbnailBook(bookId = book.id, type = ThumbnailBook.Type.SIDECAR, mediaType = "image/jpeg", fileSize = 0L, dimension = Dimension(0, 0)), MarkSelectedPreference.YES)
    }
    // book with GENERATED not selected, USER_UPLOADED selected
    makeBook("3", libraryId = library2.id, seriesId = series2.id).let { book ->
      seriesLifecycle.addBooks(series2, listOf(book))
      bookLifecycle.addThumbnailForBook(ThumbnailBook(bookId = book.id, type = ThumbnailBook.Type.GENERATED, mediaType = "image/jpeg", fileSize = 0L, dimension = Dimension(0, 0)), MarkSelectedPreference.YES)
      bookLifecycle.addThumbnailForBook(ThumbnailBook(bookId = book.id, type = ThumbnailBook.Type.USER_UPLOADED, mediaType = "image/jpeg", fileSize = 0L, dimension = Dimension(0, 0)), MarkSelectedPreference.YES)
    }
    // book without poster
    makeBook("4", libraryId = library2.id, seriesId = series2.id).let { book ->
      seriesLifecycle.addBooks(series2, listOf(book))
    }

    // books with a poster of type GENERATED
    run {
      val search =
        BookSearch(
          SearchCondition.Poster(SearchOperator.Is(SearchCondition.PosterMatch(SearchCondition.PosterMatch.Type.GENERATED))),
        )
      val found = bookDao.findAll(search.condition, SearchContext(user1), Pageable.unpaged()).content
      val foundDto = bookDtoDao.findAll(search, SearchContext(user1), Pageable.unpaged()).content

      assertThat(found.map { it.name }).containsExactlyInAnyOrder("1", "2", "3")
      assertThat(foundDto.map { it.name }).containsExactlyInAnyOrder("1", "2", "3")
    }

    // books with a poster of type GENERATED, selected
    run {
      val search =
        BookSearch(
          SearchCondition.Poster(SearchOperator.Is(SearchCondition.PosterMatch(SearchCondition.PosterMatch.Type.GENERATED, true))),
        )
      val found = bookDao.findAll(search.condition, SearchContext(user1), Pageable.unpaged()).content
      val foundDto = bookDtoDao.findAll(search, SearchContext(user1), Pageable.unpaged()).content

      assertThat(found.map { it.name }).containsExactlyInAnyOrder("1")
      assertThat(foundDto.map { it.name }).containsExactlyInAnyOrder("1")
    }

    // books with any poster not selected
    run {
      val search =
        BookSearch(
          SearchCondition.Poster(SearchOperator.Is(SearchCondition.PosterMatch(selected = false))),
        )
      val found = bookDao.findAll(search.condition, SearchContext(user1), Pageable.unpaged()).content
      val foundDto = bookDtoDao.findAll(search, SearchContext(user1), Pageable.unpaged()).content

      assertThat(found.map { it.name }).containsExactlyInAnyOrder("2", "3")
      assertThat(foundDto.map { it.name }).containsExactlyInAnyOrder("2", "3")
    }

    // books without a poster of type SIDECAR
    run {
      val search =
        BookSearch(
          SearchCondition.Poster(SearchOperator.IsNot(SearchCondition.PosterMatch(SearchCondition.PosterMatch.Type.SIDECAR))),
        )
      val found = bookDao.findAll(search.condition, SearchContext(user1), Pageable.unpaged()).content
      val foundDto = bookDtoDao.findAll(search, SearchContext(user1), Pageable.unpaged()).content

      assertThat(found.map { it.name }).containsExactlyInAnyOrder("1", "3", "4")
      assertThat(foundDto.map { it.name }).containsExactlyInAnyOrder("1", "3", "4")
    }

    // books without a poster of type GENERATED
    run {
      val search =
        BookSearch(
          SearchCondition.Poster(SearchOperator.IsNot(SearchCondition.PosterMatch(SearchCondition.PosterMatch.Type.GENERATED))),
        )
      val found = bookDao.findAll(search.condition, SearchContext(user1), Pageable.unpaged()).content
      val foundDto = bookDtoDao.findAll(search, SearchContext(user1), Pageable.unpaged()).content

      assertThat(found.map { it.name }).containsExactlyInAnyOrder("4")
      assertThat(foundDto.map { it.name }).containsExactlyInAnyOrder("4")
    }

    // books without selected poster
    run {
      val search =
        BookSearch(
          SearchCondition.Poster(SearchOperator.IsNot(SearchCondition.PosterMatch(selected = true))),
        )
      val found = bookDao.findAll(search.condition, SearchContext(user1), Pageable.unpaged()).content
      val foundDto = bookDtoDao.findAll(search, SearchContext(user1), Pageable.unpaged()).content

      assertThat(found.map { it.name }).containsExactlyInAnyOrder("4")
      assertThat(foundDto.map { it.name }).containsExactlyInAnyOrder("4")
    }

    // empty PosterMatch does not apply any condition
    run {
      val search =
        BookSearch(
          SearchCondition.Poster(SearchOperator.Is(SearchCondition.PosterMatch())),
        )
      val found = bookDao.findAll(search.condition, SearchContext(user1), Pageable.unpaged()).content
      val foundDto = bookDtoDao.findAll(search, SearchContext(user1), Pageable.unpaged()).content

      assertThat(found.map { it.name }).containsExactlyInAnyOrder("1", "2", "3", "4")
      assertThat(foundDto.map { it.name }).containsExactlyInAnyOrder("1", "2", "3", "4")
    }
  }
}
