package org.gotson.komga.infrastructure.jooq.main

import com.ninjasquad.springmockk.MockkBean
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatCode
import org.gotson.komga.domain.model.Author
import org.gotson.komga.domain.model.BookSearch
import org.gotson.komga.domain.model.KomgaUser
import org.gotson.komga.domain.model.Media
import org.gotson.komga.domain.model.ReadProgress
import org.gotson.komga.domain.model.ReadStatus
import org.gotson.komga.domain.model.SearchCondition
import org.gotson.komga.domain.model.SearchContext
import org.gotson.komga.domain.model.SearchOperator
import org.gotson.komga.domain.model.makeBook
import org.gotson.komga.domain.model.makeLibrary
import org.gotson.komga.domain.model.makeSeries
import org.gotson.komga.domain.persistence.BookMetadataRepository
import org.gotson.komga.domain.persistence.BookRepository
import org.gotson.komga.domain.persistence.KomgaUserRepository
import org.gotson.komga.domain.persistence.LibraryRepository
import org.gotson.komga.domain.persistence.MediaRepository
import org.gotson.komga.domain.persistence.ReadProgressRepository
import org.gotson.komga.domain.service.BookLifecycle
import org.gotson.komga.domain.service.KomgaUserLifecycle
import org.gotson.komga.domain.service.LibraryLifecycle
import org.gotson.komga.domain.service.SeriesLifecycle
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
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import java.net.URL
import java.time.LocalDate
import java.time.LocalDateTime

@SpringBootTest
class BookDtoDaoTest(
  @Autowired private val bookDtoDao: BookDtoDao,
  @Autowired private val bookRepository: BookRepository,
  @Autowired private val bookMetadataRepository: BookMetadataRepository,
  @Autowired private val mediaRepository: MediaRepository,
  @Autowired private val bookLifecycle: BookLifecycle,
  @Autowired private val seriesLifecycle: SeriesLifecycle,
  @Autowired private val libraryRepository: LibraryRepository,
  @Autowired private val libraryLifecycle: LibraryLifecycle,
  @Autowired private val readProgressRepository: ReadProgressRepository,
  @Autowired private val userRepository: KomgaUserRepository,
  @Autowired private val userLifecycle: KomgaUserLifecycle,
  @Autowired private val searchIndexLifecycle: SearchIndexLifecycle,
) {
  private val library = makeLibrary()
  private var series = makeSeries("Series")
  private val user = KomgaUser("user@example.org", "")

  @MockkBean
  private lateinit var mockEventPublisher: ApplicationEventPublisher

  @BeforeAll
  fun setup() {
    every { mockEventPublisher.publishEvent(any()) } just Runs
    libraryRepository.insert(library)
    series = seriesLifecycle.createSeries(series.copy(libraryId = library.id))
    userRepository.insert(user)
  }

  @BeforeEach
  fun resetMocks() {
    every { mockEventPublisher.publishEvent(any()) } just Runs
  }

  @AfterEach
  fun deleteBooks() {
    bookLifecycle.deleteMany(bookRepository.findAll())
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

  private fun setupBooks() {
    seriesLifecycle.addBooks(
      series,
      (1..3).map {
        makeBook("$it", seriesId = series.id, libraryId = library.id)
      },
    )

    val books = bookRepository.findAll().sortedBy { it.name }
    books.elementAt(0).let { readProgressRepository.save(ReadProgress(it.id, user.id, 5, false)) }
    books.elementAt(1).let { readProgressRepository.save(ReadProgress(it.id, user.id, 5, true)) }
  }

  @Nested
  inner class Criteria {
    @Test
    fun `given books when searching by multiple tags then results are matched and not duplicated`() {
      // given
      val book1 = makeBook("Éric le rouge", seriesId = series.id, libraryId = library.id)
      val book2 = makeBook("Éric le bleu", seriesId = series.id, libraryId = library.id)
      seriesLifecycle.addBooks(
        series,
        listOf(
          book1,
          book2,
        ),
      )

      bookMetadataRepository.findById(book1.id).let {
        bookMetadataRepository.update(it.copy(tags = setOf("tag1", "tag2")))
      }
      bookMetadataRepository.findById(book2.id).let {
        bookMetadataRepository.update(it.copy(tags = setOf("tag1", "tag2")))
      }

      // when
      val page =
        bookDtoDao.findAll(
          BookSearch(
            SearchCondition.AnyOfBook(
              SearchCondition.Tag(SearchOperator.Is("tag1")),
              SearchCondition.Tag(SearchOperator.Is("tag2")),
            ),
          ),
          SearchContext(user),
          Pageable.unpaged(),
        )

      // then
      assertThat(page.totalElements).isEqualTo(2)
      assertThat(page.content).hasSize(2)
      assertThat(page.content.map { it.metadata.title }).containsExactly("Éric le rouge", "Éric le bleu")
    }

    @Test
    fun `given books when searching by multiple authors then results are matched and not duplicated`() {
      // given
      val book1 = makeBook("Éric le rouge", seriesId = series.id, libraryId = library.id)
      val book2 = makeBook("Éric le bleu", seriesId = series.id, libraryId = library.id)
      seriesLifecycle.addBooks(
        series,
        listOf(
          book1,
          book2,
        ),
      )

      bookMetadataRepository.findById(book1.id).let {
        bookMetadataRepository.update(it.copy(authors = listOf(Author("Mark", "writer"), Author("Jim", "inker"))))
      }
      bookMetadataRepository.findById(book2.id).let {
        bookMetadataRepository.update(it.copy(authors = listOf(Author("Mark", "writer"), Author("Jim", "inker"))))
      }

      // when
      val page =
        bookDtoDao.findAll(
          BookSearch(
            SearchCondition.AnyOfBook(
              SearchCondition.Author(SearchOperator.Is(SearchCondition.AuthorMatch("Mark", "writer"))),
              SearchCondition.Author(SearchOperator.Is(SearchCondition.AuthorMatch("Jim", "inker"))),
            ),
          ),
          SearchContext(user),
          Pageable.unpaged(),
        )

      // then
      assertThat(page.totalElements).isEqualTo(2)
      assertThat(page.content).hasSize(2)
      assertThat(page.content.map { it.metadata.title }).containsExactly("Éric le rouge", "Éric le bleu")
    }
  }

  @Nested
  inner class ReadProgress {
    @Test
    fun `given books in various read status when searching for read books then only read books are returned`() {
      // given
      setupBooks()

      // when
      val found =
        bookDtoDao.findAll(
          BookSearch(SearchCondition.ReadStatus(SearchOperator.Is(ReadStatus.READ))),
          SearchContext(user),
          PageRequest.of(0, 20),
        )

      // then
      assertThat(found).hasSize(1)
      assertThat(found.first().readProgress?.completed).isTrue
      assertThat(found.first().name).isEqualTo("2")
    }

    @Test
    fun `given books in various read status when searching for unread books then only unread books are returned`() {
      // given
      setupBooks()

      // when
      val found =
        bookDtoDao.findAll(
          BookSearch(SearchCondition.ReadStatus(SearchOperator.Is(ReadStatus.UNREAD))),
          SearchContext(user),
          PageRequest.of(0, 20),
        )

      // then
      assertThat(found).hasSize(1)
      assertThat(found.first().readProgress).isNull()
      assertThat(found.first().name).isEqualTo("3")
    }

    @Test
    fun `given books in various read status when searching for in progress books then only in progress books are returned`() {
      // given
      setupBooks()

      // when
      val found =
        bookDtoDao.findAll(
          BookSearch(SearchCondition.ReadStatus(SearchOperator.Is(ReadStatus.IN_PROGRESS))),
          SearchContext(user),
          PageRequest.of(0, 20),
        )

      // then
      assertThat(found).hasSize(1)
      assertThat(found.first().readProgress?.completed).isFalse
      assertThat(found.first().name).isEqualTo("1")
    }

    @Test
    fun `given books in various read status when searching for read and unread books then only matching books are returned`() {
      // given
      setupBooks()

      // when
      val found =
        bookDtoDao.findAll(
          BookSearch(
            SearchCondition.AnyOfBook(
              SearchCondition.ReadStatus(SearchOperator.Is(ReadStatus.READ)),
              SearchCondition.ReadStatus(SearchOperator.Is(ReadStatus.UNREAD)),
            ),
          ),
          SearchContext(user),
          PageRequest.of(0, 20),
        )

      // then
      assertThat(found).hasSize(2)
      assertThat(found.map { it.name }).containsExactlyInAnyOrder("2", "3")
    }

    @Test
    fun `given books in various read status when searching for read and in progress books then only matching books are returned`() {
      // given
      setupBooks()

      // when
      val found =
        bookDtoDao.findAll(
          BookSearch(
            SearchCondition.AnyOfBook(
              SearchCondition.ReadStatus(SearchOperator.Is(ReadStatus.READ)),
              SearchCondition.ReadStatus(SearchOperator.Is(ReadStatus.IN_PROGRESS)),
            ),
          ),
          SearchContext(user),
          PageRequest.of(0, 20),
        )

      // then
      assertThat(found).hasSize(2)
      assertThat(found.map { it.name }).containsExactlyInAnyOrder("2", "1")
    }

    @Test
    fun `given books in various read status when searching for unread and in progress books then only matching books are returned`() {
      // given
      setupBooks()

      // when
      val found =
        bookDtoDao.findAll(
          BookSearch(
            SearchCondition.AnyOfBook(
              SearchCondition.ReadStatus(SearchOperator.Is(ReadStatus.UNREAD)),
              SearchCondition.ReadStatus(SearchOperator.Is(ReadStatus.IN_PROGRESS)),
            ),
          ),
          SearchContext(user),
          PageRequest.of(0, 20),
        )

      // then
      assertThat(found).hasSize(2)
      assertThat(found.map { it.name }).containsExactlyInAnyOrder("3", "1")
    }

    @Test
    fun `given books in various read status when searching for read and unread and in progress books then only matching books are returned`() {
      // given
      setupBooks()

      // when
      val found =
        bookDtoDao.findAll(
          BookSearch(
            SearchCondition.AnyOfBook(
              SearchCondition.ReadStatus(SearchOperator.Is(ReadStatus.READ)),
              SearchCondition.ReadStatus(SearchOperator.Is(ReadStatus.IN_PROGRESS)),
              SearchCondition.ReadStatus(SearchOperator.Is(ReadStatus.UNREAD)),
            ),
          ),
          SearchContext(user),
          PageRequest.of(0, 20),
        )

      // then
      assertThat(found).hasSize(3)
      assertThat(found.map { it.name }).containsExactlyInAnyOrder("3", "1", "2")
    }

    @Test
    fun `given books in various read status when searching without read progress then all books are returned`() {
      // given
      setupBooks()

      // when
      val found =
        bookDtoDao.findAll(
          BookSearch(),
          SearchContext(user),
          PageRequest.of(0, 20),
        )

      // then
      assertThat(found).hasSize(3)
      assertThat(found.map { it.name }).containsExactlyInAnyOrder("3", "1", "2")
    }
  }

  @Nested
  inner class OnDeck {
    @Test
    fun `given series with in progress books status when searching for on deck then nothing is returned`() {
      // given
      setupBooks()

      // when
      val found =
        bookDtoDao.findAllOnDeck(
          user.id,
          null,
          PageRequest.of(0, 20),
        )

      // then
      assertThat(found).isEmpty()
    }

    @Test
    fun `given series with only unread books when searching for on deck then no books are returned`() {
      // given
      seriesLifecycle.addBooks(
        series,
        (1..3).map {
          makeBook("$it", seriesId = series.id, libraryId = library.id)
        },
      )

      // when
      val found =
        bookDtoDao.findAllOnDeck(
          user.id,
          null,
          PageRequest.of(0, 20),
        )

      // then
      assertThat(found).hasSize(0)
    }

    @Test
    fun `given series with some unread books when searching for on deck then first unread book of series is returned`() {
      // given
      seriesLifecycle.addBooks(
        series,
        (1..3).map {
          makeBook("$it", seriesId = series.id, libraryId = library.id).copy(number = it)
        },
      )

      val books = bookRepository.findAll().sortedBy { it.name }
      books.first().let { readProgressRepository.save(ReadProgress(it.id, user.id, 5, true)) }

      // when
      val found =
        bookDtoDao.findAllOnDeck(
          user.id,
          null,
          PageRequest.of(0, 20),
        )

      // then
      assertThat(found).hasSize(1)
      assertThat(found.first().name).isEqualTo("2")
    }
  }

  @Nested
  inner class FullTextSearch {
    @Test
    fun `given books when searching by term then results are ordered by rank`() {
      // given
      seriesLifecycle.addBooks(
        series,
        listOf(
          makeBook("The incredible adventures of Batman, the man who is also a bat!", seriesId = series.id, libraryId = library.id),
          makeBook("Robin", seriesId = series.id, libraryId = library.id),
          makeBook("Batman and Robin", seriesId = series.id, libraryId = library.id),
          makeBook("Batman", seriesId = series.id, libraryId = library.id),
        ),
      )

      searchIndexLifecycle.rebuildIndex()

      // when
      val found =
        bookDtoDao
          .findAll(
            BookSearch(fullTextSearch = "batman"),
            SearchContext(user),
            UnpagedSorted(Sort.by("relevance")),
          ).content

      // then
      assertThat(found).hasSize(3)
      assertThat(found.map { it.name }).containsExactly("Batman", "Batman and Robin", "The incredible adventures of Batman, the man who is also a bat!")
    }

    @Test
    fun `given books when searching by term and sort order then results are ordered by sort order`() {
      // given
      seriesLifecycle.addBooks(
        series,
        listOf(
          makeBook("Book 3", seriesId = series.id, libraryId = library.id),
          makeBook("Book 1", seriesId = series.id, libraryId = library.id),
          makeBook("Book 2", seriesId = series.id, libraryId = library.id),
        ),
      )

      searchIndexLifecycle.rebuildIndex()

      // when
      val found =
        bookDtoDao
          .findAll(
            BookSearch(fullTextSearch = "book"),
            SearchContext(user),
            UnpagedSorted(Sort.by("name")),
          ).content
      val pages =
        (0..2).map {
          bookDtoDao.findAll(
            BookSearch(fullTextSearch = "book"),
            SearchContext(user),
            PageRequest.of(it, 1, Sort.by("name")),
          )
        }
      val page0 =
        bookDtoDao.findAll(
          BookSearch(fullTextSearch = "book"),
          SearchContext(user),
          PageRequest.of(0, 2, Sort.by("name")),
        )
      val page1 =
        bookDtoDao.findAll(
          BookSearch(fullTextSearch = "book"),
          SearchContext(user),
          PageRequest.of(1, 2, Sort.by("name")),
        )

      // then
      assertThat(found).hasSize(3)
      assertThat(found.map { it.name }).containsExactly("Book 1", "Book 2", "Book 3")

      assertThat(pages).hasSize(3)
      assertThat(pages.map { it.totalPages }).containsOnly(3)
      assertThat(pages.map { it.totalElements }).containsOnly(3)
      assertThat(pages.map { it.size }).containsOnly(1)
      assertThat(pages.flatMap { it.content }.map { it.name }).containsExactly("Book 1", "Book 2", "Book 3")

      assertThat(page0).hasSize(2)
      assertThat(page0.content.map { it.name }).containsExactly("Book 1", "Book 2")
      assertThat(page1).hasSize(1)
      assertThat(page1.content.map { it.name }).containsExactly("Book 3")
    }

    @Test
    fun `given books when searching by term with accent then results are matched accent insensitive`() {
      // given
      val book1 = makeBook("Éric le rouge", seriesId = series.id, libraryId = library.id)
      seriesLifecycle.addBooks(
        series,
        listOf(
          book1,
          makeBook("Robin", seriesId = series.id, libraryId = library.id),
          makeBook("Batman and Robin", seriesId = series.id, libraryId = library.id),
          makeBook("Batman", seriesId = series.id, libraryId = library.id),
        ),
      )

      bookMetadataRepository.findById(book1.id).let {
        bookMetadataRepository.update(it.copy(title = "Éric le bleu"))
      }

      searchIndexLifecycle.rebuildIndex()

      // when
      val found =
        bookDtoDao
          .findAll(
            BookSearch(fullTextSearch = "eric"),
            SearchContext(user),
            UnpagedSorted(Sort.by("relevance")),
          ).content

      // then
      assertThat(found).hasSize(1)
      assertThat(found.map { it.metadata.title }).containsExactly("Éric le bleu")
    }

    @Test
    fun `given books when searching by ISBN then results are matched`() {
      // given
      val book1 = makeBook("Éric le rouge", seriesId = series.id, libraryId = library.id)
      seriesLifecycle.addBooks(
        series,
        listOf(
          book1,
          makeBook("Robin", seriesId = series.id, libraryId = library.id),
          makeBook("Batman and Robin", seriesId = series.id, libraryId = library.id),
          makeBook("Batman", seriesId = series.id, libraryId = library.id),
        ),
      )

      bookMetadataRepository.findById(book1.id).let {
        bookMetadataRepository.update(it.copy(isbn = "9782413016878"))
      }

      searchIndexLifecycle.rebuildIndex()

      // when
      val found =
        bookDtoDao
          .findAll(
            BookSearch(fullTextSearch = "9782413016878"),
            SearchContext(user),
            UnpagedSorted(Sort.by("relevance")),
          ).content

      // then
      assertThat(found).hasSize(1)
      assertThat(found.map { it.metadata.title }).containsExactly("Éric le rouge")
    }

    @Test
    fun `given books when searching by tags then results are matched`() {
      // given
      val book1 = makeBook("Éric le rouge", seriesId = series.id, libraryId = library.id)
      seriesLifecycle.addBooks(
        series,
        listOf(
          book1,
        ),
      )

      bookMetadataRepository.findById(book1.id).let {
        bookMetadataRepository.update(it.copy(tags = setOf("tag1")))
      }

      searchIndexLifecycle.rebuildIndex()

      // when
      val found =
        bookDtoDao
          .findAll(
            BookSearch(fullTextSearch = "tag:tag1"),
            SearchContext(user),
            UnpagedSorted(Sort.by("relevance")),
          ).content

      // then
      assertThat(found).hasSize(1)
      assertThat(found.map { it.metadata.title }).containsExactly("Éric le rouge")
    }

    @Test
    fun `given books when searching by authors then results are matched`() {
      // given
      val book1 = makeBook("Éric le rouge", seriesId = series.id, libraryId = library.id)
      seriesLifecycle.addBooks(
        series,
        listOf(
          book1,
        ),
      )

      bookMetadataRepository.findById(book1.id).let {
        bookMetadataRepository.update(it.copy(authors = listOf(Author("bob", "writer"))))
      }

      searchIndexLifecycle.rebuildIndex()

      // when
      val foundGeneric =
        bookDtoDao
          .findAll(
            BookSearch(fullTextSearch = "author:bob"),
            SearchContext(user),
            UnpagedSorted(Sort.by("relevance")),
          ).content
      val foundByRole =
        bookDtoDao
          .findAll(
            BookSearch(fullTextSearch = "writer:bob"),
            SearchContext(user),
            UnpagedSorted(Sort.by("relevance")),
          ).content
      val notFound =
        bookDtoDao
          .findAll(
            BookSearch(fullTextSearch = "penciller:bob"),
            SearchContext(user),
            UnpagedSorted(Sort.by("relevance")),
          ).content

      // then
      assertThat(foundGeneric).hasSize(1)
      assertThat(foundGeneric.map { it.metadata.title }).containsExactly("Éric le rouge")
      assertThat(foundByRole).hasSize(1)
      assertThat(foundByRole.map { it.metadata.title }).containsExactly("Éric le rouge")
      assertThat(notFound).isEmpty()
    }

    @Test
    fun `given books when searching by release year then results are matched`() {
      // given
      val book1 = makeBook("Éric le rouge", seriesId = series.id, libraryId = library.id)
      seriesLifecycle.addBooks(series, listOf(book1))

      bookMetadataRepository.findById(book1.id).let {
        bookMetadataRepository.update(it.copy(releaseDate = LocalDate.of(1999, 5, 12)))
      }

      searchIndexLifecycle.rebuildIndex()

      // when
      val found =
        bookDtoDao
          .findAll(
            BookSearch(fullTextSearch = "release_date:1999"),
            SearchContext(user),
            UnpagedSorted(Sort.by("relevance")),
          ).content

      // then
      assertThat(found).hasSize(1)
      assertThat(found.map { it.metadata.title }).containsExactly("Éric le rouge")
    }

    @Test
    fun `given books when searching by release year range then results are matched`() {
      // given
      val book1 = makeBook("Éric le rouge", seriesId = series.id, libraryId = library.id)
      val book2 = makeBook("Éric le bleu", seriesId = series.id, libraryId = library.id)
      seriesLifecycle.addBooks(series, listOf(book1, book2))

      bookMetadataRepository.findById(book1.id).let {
        bookMetadataRepository.update(it.copy(releaseDate = LocalDate.of(1999, 5, 12)))
      }
      bookMetadataRepository.findById(book2.id).let {
        bookMetadataRepository.update(it.copy(releaseDate = LocalDate.of(2005, 5, 12)))
      }

      searchIndexLifecycle.rebuildIndex()

      // when
      val found =
        bookDtoDao
          .findAll(
            BookSearch(fullTextSearch = "release_date:[1990 TO 2010]"),
            SearchContext(user),
            UnpagedSorted(Sort.by("relevance")),
          ).content

      // then
      assertThat(found).hasSize(2)
      assertThat(found.map { it.metadata.title }).containsExactly("Éric le rouge", "Éric le bleu")
    }

    @Test
    fun `given books when searching by media status then results are matched`() {
      // given
      val book1 = makeBook("Éric le rouge", seriesId = series.id, libraryId = library.id)
      seriesLifecycle.addBooks(series, listOf(book1))

      mediaRepository.findById(book1.id).let {
        mediaRepository.update(it.copy(status = Media.Status.ERROR))
      }

      searchIndexLifecycle.rebuildIndex()

      // when
      val found =
        bookDtoDao
          .findAll(
            BookSearch(fullTextSearch = "status:error"),
            SearchContext(user),
            UnpagedSorted(Sort.by("relevance")),
          ).content

      // then
      assertThat(found).hasSize(1)
      assertThat(found.map { it.metadata.title }).containsExactly("Éric le rouge")
    }

    @Test
    fun `given books when searching by deleted then results are matched`() {
      // given
      val book1 =
        makeBook("Éric le rouge", seriesId = series.id, libraryId = library.id)
          .copy(deletedDate = LocalDateTime.now())
      seriesLifecycle.addBooks(
        series,
        listOf(
          book1,
          makeBook("Batman", seriesId = series.id, libraryId = library.id),
        ),
      )

      searchIndexLifecycle.rebuildIndex()

      // when
      val found =
        bookDtoDao
          .findAll(
            BookSearch(fullTextSearch = "deleted:true"),
            SearchContext(user),
            UnpagedSorted(Sort.by("relevance")),
          ).content

      // then
      assertThat(found).hasSize(1)
      assertThat(found.map { it.metadata.title }).containsExactly("Éric le rouge")
    }

    @Test
    fun `given books with dots in title when searching by title then results are matched`() {
      // given
      seriesLifecycle.addBooks(
        series,
        listOf(
          makeBook("S.W.O.R.D.", seriesId = series.id, libraryId = library.id),
          makeBook("Batman", seriesId = series.id, libraryId = library.id),
        ),
      )

      searchIndexLifecycle.rebuildIndex()

      // when
      val found =
        bookDtoDao
          .findAll(
            BookSearch(fullTextSearch = "s.w.o.r.d"),
            SearchContext(user),
            UnpagedSorted(Sort.by("relevance")),
          ).content

      // then
      assertThat(found).hasSize(1)
      assertThat(found.map { it.metadata.title }).containsExactly("S.W.O.R.D.")
    }

    @Test
    fun `given books when searching with multiple words then results are matched`() {
      // given
      seriesLifecycle.addBooks(
        series,
        listOf(
          makeBook("Éric le rouge", seriesId = series.id, libraryId = library.id),
          makeBook("Robin and Batman", seriesId = series.id, libraryId = library.id),
          makeBook("Batman and Robin", seriesId = series.id, libraryId = library.id),
          makeBook("Batman", seriesId = series.id, libraryId = library.id),
        ),
      )

      searchIndexLifecycle.rebuildIndex()

      // when
      val found =
        bookDtoDao
          .findAll(
            BookSearch(fullTextSearch = "batman robin"),
            SearchContext(user),
            UnpagedSorted(Sort.by("relevance")),
          ).content

      // then
      assertThat(found).hasSize(2)
      assertThat(found.map { it.metadata.title }).containsExactlyInAnyOrder("Batman and Robin", "Robin and Batman")
    }

    @Test
    fun `given books when searching by term containing hyphens then results are ordered by rank`() {
      // given
      seriesLifecycle.addBooks(
        series,
        listOf(
          makeBook("Batman", seriesId = series.id, libraryId = library.id),
          makeBook("Another X-Men adventure", seriesId = series.id, libraryId = library.id),
          makeBook("X-Men", seriesId = series.id, libraryId = library.id),
        ),
      )

      searchIndexLifecycle.rebuildIndex()

      // when
      val found =
        bookDtoDao
          .findAll(
            BookSearch(fullTextSearch = "x-men"),
            SearchContext(user),
            UnpagedSorted(Sort.by("relevance")),
          ).content

      // then
      assertThat(found).hasSize(2)
      assertThat(found.map { it.name }).containsExactly("X-Men", "Another X-Men adventure")
    }

    @Test
    fun `when searching by unknown field then empty result are returned and no exception is thrown`() {
      assertThatCode {
        // when
        val found =
          bookDtoDao
            .findAll(
              BookSearch(fullTextSearch = "publisher:batman"),
              SearchContext(user),
              UnpagedSorted(Sort.by("relevance")),
            ).content

        // then
        assertThat(found).hasSize(0)
      }.doesNotThrowAnyException()
    }

    @Test
    fun `given books in CJK when searching by CJK term then results are ordered by rank`() {
      // given
      seriesLifecycle.addBooks(
        series,
        listOf(
          makeBook("[不道德公會][河添太一 ][東立]Vol.04-搬运", seriesId = series.id, libraryId = library.id, url = URL("file:/file.cbz")),
        ),
      )

      searchIndexLifecycle.rebuildIndex()

      // when
      val found =
        bookDtoDao
          .findAll(
            BookSearch(fullTextSearch = "不道德"),
            SearchContext(user),
            UnpagedSorted(Sort.by("relevance")),
          ).content

      // then
      assertThat(found).hasSize(1)
    }
  }

  @Nested
  inner class Duplicates {
    @Test
    fun `given books with same hash and size when searching then results are returned`() {
      // given
      seriesLifecycle.addBooks(
        series,
        listOf(
          makeBook("Book 1", seriesId = series.id, libraryId = library.id).copy(fileHash = "hashed", fileSize = 10),
          makeBook("Book 2", seriesId = series.id, libraryId = library.id).copy(fileHash = "hashed", fileSize = 10),
        ),
      )

      // when
      val found = bookDtoDao.findAllDuplicates(user.id, Pageable.unpaged()).content

      // then
      assertThat(found).hasSize(2)
      assertThat(found.map { it.name }).containsExactlyInAnyOrder("Book 1", "Book 2")
    }

    @Test
    fun `given books with same hash but different size when searching then no results are returned`() {
      // given
      seriesLifecycle.addBooks(
        series,
        listOf(
          makeBook("Book 1", seriesId = series.id, libraryId = library.id).copy(fileHash = "hashed", fileSize = 10),
          makeBook("Book 2", seriesId = series.id, libraryId = library.id).copy(fileHash = "hashed", fileSize = 12),
        ),
      )

      // when
      val found = bookDtoDao.findAllDuplicates(user.id, Pageable.unpaged()).content

      // then
      assertThat(found).isEmpty()
    }
  }
}
