package org.gotson.komga.interfaces.api.rest

import org.gotson.komga.domain.model.Book
import org.gotson.komga.domain.model.ROLE_ADMIN
import org.gotson.komga.domain.model.ReadList
import org.gotson.komga.domain.model.makeBook
import org.gotson.komga.domain.model.makeLibrary
import org.gotson.komga.domain.model.makeSeries
import org.gotson.komga.domain.persistence.LibraryRepository
import org.gotson.komga.domain.persistence.ReadListRepository
import org.gotson.komga.domain.persistence.SeriesMetadataRepository
import org.gotson.komga.domain.service.LibraryLifecycle
import org.gotson.komga.domain.service.ReadListLifecycle
import org.gotson.komga.domain.service.SeriesLifecycle
import org.gotson.komga.language.toIndexedMap
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.delete
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.patch
import org.springframework.test.web.servlet.post

@ExtendWith(SpringExtension::class)
@SpringBootTest
@AutoConfigureMockMvc(printOnlyOnFailure = false)
class ReadListControllerTest(
  @Autowired private val mockMvc: MockMvc,
  @Autowired private val readListLifecycle: ReadListLifecycle,
  @Autowired private val readListRepository: ReadListRepository,
  @Autowired private val libraryLifecycle: LibraryLifecycle,
  @Autowired private val libraryRepository: LibraryRepository,
  @Autowired private val seriesLifecycle: SeriesLifecycle,
  @Autowired private val seriesMetadataRepository: SeriesMetadataRepository,
) {

  private val library1 = makeLibrary("Library1", id = "1")
  private val library2 = makeLibrary("Library2", id = "2")
  private val seriesLib1 = makeSeries("Series1", library1.id)
  private val seriesLib2 = makeSeries("Series2", library2.id)
  private lateinit var booksLibrary1: List<Book>
  private lateinit var booksLibrary2: List<Book>
  private lateinit var rlLib1: ReadList
  private lateinit var rlLib2: ReadList
  private lateinit var rlLibBoth: ReadList

  @BeforeAll
  fun setup() {
    libraryRepository.insert(library1)
    libraryRepository.insert(library2)

    seriesLifecycle.createSeries(seriesLib1)
    seriesLifecycle.createSeries(seriesLib2)

    booksLibrary1 = (1..5).map { makeBook("Book_$it", libraryId = library1.id, seriesId = seriesLib1.id) }
    seriesLifecycle.addBooks(seriesLib1, booksLibrary1)

    booksLibrary2 = (6..10).map { makeBook("Book_$it", libraryId = library2.id, seriesId = seriesLib2.id) }
    seriesLifecycle.addBooks(seriesLib2, booksLibrary2)
  }

  @AfterAll
  fun teardown() {
    libraryRepository.findAll().forEach {
      libraryLifecycle.deleteLibrary(it)
    }
  }

  @AfterEach
  fun clear() {
    readListRepository.deleteAll()
  }

  private fun makeReadLists() {
    rlLib1 = readListLifecycle.addReadList(
      ReadList(
        name = "Lib1",
        bookIds = booksLibrary1.map { it.id }.toIndexedMap(),
      ),
    )

    rlLib2 = readListLifecycle.addReadList(
      ReadList(
        name = "Lib2",
        bookIds = booksLibrary2.map { it.id }.toIndexedMap(),
      ),
    )

    rlLibBoth = readListLifecycle.addReadList(
      ReadList(
        name = "Lib1+2",
        bookIds = (booksLibrary1 + booksLibrary2).map { it.id }.toIndexedMap(),
      ),
    )
  }

  @Nested
  inner class GetAndFilter {
    @Test
    @WithMockCustomUser
    fun `given user with access to all libraries when getting read lists then get all read lists`() {
      makeReadLists()

      mockMvc.get("/api/v1/readlists")
        .andExpect {
          status { isOk() }
          jsonPath("$.totalElements") { value(3) }
          jsonPath("$.content[?(@.name == 'Lib1')].filtered") { value(false) }
          jsonPath("$.content[?(@.name == 'Lib2')].filtered") { value(false) }
          jsonPath("$.content[?(@.name == 'Lib1+2')].filtered") { value(false) }
        }
    }

    @Test
    @WithMockCustomUser(sharedAllLibraries = false, sharedLibraries = ["1"])
    fun `given user with access to a single library when getting read lists then only get read lists from this library`() {
      makeReadLists()

      mockMvc.get("/api/v1/readlists")
        .andExpect {
          status { isOk() }
          jsonPath("$.totalElements") { value(2) }
          jsonPath("$.content[?(@.name == 'Lib1')].filtered") { value(false) }
          jsonPath("$.content[?(@.name == 'Lib1+2')].filtered") { value(true) }
        }
    }

    @Test
    @WithMockCustomUser
    fun `given user with access to all libraries when getting single read list then it is not filtered`() {
      makeReadLists()

      mockMvc.get("/api/v1/readlists/${rlLibBoth.id}")
        .andExpect {
          status { isOk() }
          jsonPath("$.bookIds.length()") { value(10) }
          jsonPath("$.filtered") { value(false) }
        }
    }

    @Test
    @WithMockCustomUser(sharedAllLibraries = false, sharedLibraries = ["1"])
    fun `given user with access to a single library when getting single read list with items from 2 libraries then it is filtered`() {
      makeReadLists()

      mockMvc.get("/api/v1/readlists/${rlLibBoth.id}")
        .andExpect {
          status { isOk() }
          jsonPath("$.bookIds.length()") { value(5) }
          jsonPath("$.filtered") { value(true) }
        }
    }

    @Test
    @WithMockCustomUser(sharedAllLibraries = false, sharedLibraries = ["1"])
    fun `given user with access to a single library when getting single read list from another library then return not found`() {
      makeReadLists()

      mockMvc.get("/api/v1/readlists/${rlLib2.id}")
        .andExpect {
          status { isNotFound() }
        }
    }
  }

  @Nested
  inner class ContentRestriction {
    @Test
    @WithMockCustomUser(allowAgeUnder = 10)
    fun `given user only allowed content with specific age rating when getting read lists then only get read lists that satisfies this criteria`() {
      val book10 = makeBook("book_10", libraryId = library1.id)
      makeSeries(name = "series_10", libraryId = library1.id).also { series ->
        seriesLifecycle.createSeries(series).also { created ->
          val books = listOf(book10)
          seriesLifecycle.addBooks(created, books)
        }
        seriesMetadataRepository.findById(series.id).let {
          seriesMetadataRepository.update(it.copy(ageRating = 10))
        }
      }

      val book = makeBook("book", libraryId = library1.id)
      makeSeries(name = "series_no", libraryId = library1.id).also { series ->
        seriesLifecycle.createSeries(series).also { created ->
          val books = listOf(book)
          seriesLifecycle.addBooks(created, books)
        }
      }

      val rlAllowed = readListLifecycle.addReadList(
        ReadList(
          name = "Allowed",
          bookIds = listOf(book10.id).toIndexedMap(),
        ),
      )

      val rlFiltered = readListLifecycle.addReadList(
        ReadList(
          name = "Filtered",
          bookIds = listOf(book10.id, book.id).toIndexedMap(),
        ),
      )

      val rlDenied = readListLifecycle.addReadList(
        ReadList(
          name = "Denied",
          bookIds = listOf(book.id).toIndexedMap(),
        ),
      )

      mockMvc.get("/api/v1/readlists")
        .andExpect {
          status { isOk() }
          jsonPath("$.totalElements") { value(2) }
          jsonPath("$.content[?(@.name == '${rlAllowed.name}')].filtered") { value(false) }
          jsonPath("$.content[?(@.name == '${rlFiltered.name}')].filtered") { value(true) }
        }

      mockMvc.get("/api/v1/readlists/${rlAllowed.id}")
        .andExpect {
          status { isOk() }
          jsonPath("$.bookIds.length()") { value(1) }
          jsonPath("$.filtered") { value(false) }
        }

      mockMvc.get("/api/v1/readlists/${rlFiltered.id}")
        .andExpect {
          status { isOk() }
          jsonPath("$.bookIds.length()") { value(1) }
          jsonPath("$.filtered") { value(true) }
        }

      mockMvc.get("/api/v1/readlists/${rlDenied.id}")
        .andExpect {
          status { isNotFound() }
        }

      mockMvc.get("/api/v1/books/${book10.id}/readlists")
        .andExpect {
          status { isOk() }
          jsonPath("$.length()") { value(2) }
          jsonPath("$[?(@.name == '${rlAllowed.name}')].filtered") { value(false) }
          jsonPath("$[?(@.name == '${rlFiltered.name}')].filtered") { value(true) }
        }
    }

    @Test
    @WithMockCustomUser(excludeAgeOver = 16)
    fun `given user disallowed content with specific age rating when getting read lists then only get read lists that satisfies this criteria`() {
      val book10 = makeBook("book_10", libraryId = library1.id)
      makeSeries(name = "series_10", libraryId = library1.id).also { series ->
        seriesLifecycle.createSeries(series).also { created ->
          val books = listOf(book10)
          seriesLifecycle.addBooks(created, books)
        }
        seriesMetadataRepository.findById(series.id).let {
          seriesMetadataRepository.update(it.copy(ageRating = 10))
        }
      }

      val book18 = makeBook("1", libraryId = library1.id)
      makeSeries(name = "series_18", libraryId = library1.id).also { series ->
        seriesLifecycle.createSeries(series).also { created ->
          val books = listOf(book18)
          seriesLifecycle.addBooks(created, books)
        }
        seriesMetadataRepository.findById(series.id).let {
          seriesMetadataRepository.update(it.copy(ageRating = 18))
        }
      }

      val book16 = makeBook("1", libraryId = library1.id)
      makeSeries(name = "series_16", libraryId = library1.id).also { series ->
        seriesLifecycle.createSeries(series).also { created ->
          val books = listOf(book16)
          seriesLifecycle.addBooks(created, books)
        }
        seriesMetadataRepository.findById(series.id).let {
          seriesMetadataRepository.update(it.copy(ageRating = 16))
        }
      }

      val book = makeBook("book", libraryId = library1.id)
      makeSeries(name = "series_no", libraryId = library1.id).also { series ->
        seriesLifecycle.createSeries(series).also { created ->
          val books = listOf(book)
          seriesLifecycle.addBooks(created, books)
        }
      }

      val rlAllowed = readListLifecycle.addReadList(
        ReadList(
          name = "Allowed",
          bookIds = listOf(book10.id, book.id).toIndexedMap(),
        ),
      )

      val rlFiltered = readListLifecycle.addReadList(
        ReadList(
          name = "Filtered",
          bookIds = listOf(book10.id, book.id, book16.id, book18.id).toIndexedMap(),
        ),
      )

      val rlDenied = readListLifecycle.addReadList(
        ReadList(
          name = "Denied",
          bookIds = listOf(book16.id, book18.id).toIndexedMap(),
        ),
      )

      mockMvc.get("/api/v1/readlists")
        .andExpect {
          status { isOk() }
          jsonPath("$.totalElements") { value(2) }
          jsonPath("$.content[?(@.name == '${rlAllowed.name}')].filtered") { value(false) }
          jsonPath("$.content[?(@.name == '${rlFiltered.name}')].filtered") { value(true) }
        }

      mockMvc.get("/api/v1/readlists/${rlAllowed.id}")
        .andExpect {
          status { isOk() }
          jsonPath("$.bookIds.length()") { value(2) }
          jsonPath("$.filtered") { value(false) }
        }

      mockMvc.get("/api/v1/readlists/${rlFiltered.id}")
        .andExpect {
          status { isOk() }
          jsonPath("$.bookIds.length()") { value(2) }
          jsonPath("$.filtered") { value(true) }
        }

      mockMvc.get("/api/v1/readlists/${rlDenied.id}")
        .andExpect {
          status { isNotFound() }
        }

      mockMvc.get("/api/v1/books/${book10.id}/readlists")
        .andExpect {
          status { isOk() }
          jsonPath("$.length()") { value(2) }
          jsonPath("$[?(@.name == '${rlAllowed.name}')].filtered") { value(false) }
          jsonPath("$[?(@.name == '${rlFiltered.name}')].filtered") { value(true) }
        }
    }

    @Test
    @WithMockCustomUser(excludeLabels = ["kids", "cute"])
    fun `given user disallowed content with specific labels when getting read lists then only get read lists that satisfies this criteria`() {
      val bookKids = makeBook("book_kids", libraryId = library1.id)
      makeSeries(name = "series_kids", libraryId = library1.id).also { series ->
        seriesLifecycle.createSeries(series).also { created ->
          val books = listOf(bookKids)
          seriesLifecycle.addBooks(created, books)
        }
        seriesMetadataRepository.findById(series.id).let {
          seriesMetadataRepository.update(it.copy(sharingLabels = setOf("kids")))
        }
      }

      val bookCute = makeBook("1", libraryId = library1.id)
      makeSeries(name = "series_cute", libraryId = library1.id).also { series ->
        seriesLifecycle.createSeries(series).also { created ->
          val books = listOf(bookCute)
          seriesLifecycle.addBooks(created, books)
        }
        seriesMetadataRepository.findById(series.id).let {
          seriesMetadataRepository.update(it.copy(sharingLabels = setOf("cute", "other")))
        }
      }

      val bookAdult = makeBook("1", libraryId = library1.id)
      makeSeries(name = "series_adult", libraryId = library1.id).also { series ->
        seriesLifecycle.createSeries(series).also { created ->
          val books = listOf(bookAdult)
          seriesLifecycle.addBooks(created, books)
        }
        seriesMetadataRepository.findById(series.id).let {
          seriesMetadataRepository.update(it.copy(sharingLabels = setOf("adult")))
        }
      }

      val book = makeBook("book", libraryId = library1.id)
      makeSeries(name = "series_no", libraryId = library1.id).also { series ->
        seriesLifecycle.createSeries(series).also { created ->
          val books = listOf(book)
          seriesLifecycle.addBooks(created, books)
        }
      }

      val rlAllowed = readListLifecycle.addReadList(
        ReadList(
          name = "Allowed",
          bookIds = listOf(bookAdult.id, book.id).toIndexedMap(),
        ),
      )

      val rlFiltered = readListLifecycle.addReadList(
        ReadList(
          name = "Filtered",
          bookIds = listOf(bookKids.id, book.id, bookAdult.id, bookCute.id).toIndexedMap(),
        ),
      )

      val rlDenied = readListLifecycle.addReadList(
        ReadList(
          name = "Denied",
          bookIds = listOf(bookKids.id, bookCute.id).toIndexedMap(),
        ),
      )

      mockMvc.get("/api/v1/readlists")
        .andExpect {
          status { isOk() }
          jsonPath("$.totalElements") { value(2) }
          jsonPath("$.content[?(@.name == '${rlAllowed.name}')].filtered") { value(false) }
          jsonPath("$.content[?(@.name == '${rlFiltered.name}')].filtered") { value(true) }
        }

      mockMvc.get("/api/v1/readlists/${rlAllowed.id}")
        .andExpect {
          status { isOk() }
          jsonPath("$.bookIds.length()") { value(2) }
          jsonPath("$.filtered") { value(false) }
        }

      mockMvc.get("/api/v1/readlists/${rlFiltered.id}")
        .andExpect {
          status { isOk() }
          jsonPath("$.bookIds.length()") { value(2) }
          jsonPath("$.filtered") { value(true) }
        }

      mockMvc.get("/api/v1/readlists/${rlDenied.id}")
        .andExpect {
          status { isNotFound() }
        }

      mockMvc.get("/api/v1/books/${bookAdult.id}/readlists")
        .andExpect {
          status { isOk() }
          jsonPath("$.length()") { value(2) }
          jsonPath("$[?(@.name == '${rlAllowed.name}')].filtered") { value(false) }
          jsonPath("$[?(@.name == '${rlFiltered.name}')].filtered") { value(true) }
        }
    }

    @Test
    @WithMockCustomUser(allowAgeUnder = 10, allowLabels = ["kids"], excludeLabels = ["adult", "teen"])
    fun `given user allowed and disallowed content when getting read lists then only get read lists that satisfies this criteria`() {
      val bookKids = makeBook("book_kids", libraryId = library1.id)
      makeSeries(name = "series_kids", libraryId = library1.id).also { series ->
        seriesLifecycle.createSeries(series).also { created ->
          val books = listOf(bookKids)
          seriesLifecycle.addBooks(created, books)
        }
        seriesMetadataRepository.findById(series.id).let {
          seriesMetadataRepository.update(it.copy(sharingLabels = setOf("kids")))
        }
      }

      val bookCute = makeBook("book_cute", libraryId = library1.id)
      makeSeries(name = "series_cute", libraryId = library1.id).also { series ->
        seriesLifecycle.createSeries(series).also { created ->
          val books = listOf(bookCute)
          seriesLifecycle.addBooks(created, books)
        }
        seriesMetadataRepository.findById(series.id).let {
          seriesMetadataRepository.update(it.copy(ageRating = 5, sharingLabels = setOf("cute", "other")))
        }
      }

      val bookAdult = makeBook("book_adult", libraryId = library1.id)
      makeSeries(name = "series_adult", libraryId = library1.id).also { series ->
        seriesLifecycle.createSeries(series).also { created ->
          val books = listOf(bookAdult)
          seriesLifecycle.addBooks(created, books)
        }
        seriesMetadataRepository.findById(series.id).let {
          seriesMetadataRepository.update(it.copy(sharingLabels = setOf("adult")))
        }
      }

      val book = makeBook("book", libraryId = library1.id)
      makeSeries(name = "series_no", libraryId = library1.id).also { series ->
        seriesLifecycle.createSeries(series).also { created ->
          val books = listOf(book)
          seriesLifecycle.addBooks(created, books)
        }
      }

      val rlAllowed = readListLifecycle.addReadList(
        ReadList(
          name = "Allowed",
          bookIds = listOf(bookKids.id, bookCute.id).toIndexedMap(),
        ),
      )

      val rlFiltered = readListLifecycle.addReadList(
        ReadList(
          name = "Filtered",
          bookIds = listOf(bookKids.id, book.id, bookAdult.id, bookCute.id).toIndexedMap(),
        ),
      )

      val rlDenied = readListLifecycle.addReadList(
        ReadList(
          name = "Denied",
          bookIds = listOf(bookAdult.id, book.id).toIndexedMap(),
        ),
      )

      mockMvc.get("/api/v1/readlists")
        .andExpect {
          status { isOk() }
          jsonPath("$.totalElements") { value(2) }
          jsonPath("$.content[?(@.name == '${rlAllowed.name}')].filtered") { value(false) }
          jsonPath("$.content[?(@.name == '${rlFiltered.name}')].filtered") { value(true) }
        }

      mockMvc.get("/api/v1/readlists/${rlAllowed.id}")
        .andExpect {
          status { isOk() }
          jsonPath("$.bookIds.length()") { value(2) }
          jsonPath("$.filtered") { value(false) }
        }

      mockMvc.get("/api/v1/readlists/${rlFiltered.id}")
        .andExpect {
          status { isOk() }
          jsonPath("$.bookIds.length()") { value(2) }
          jsonPath("$.filtered") { value(true) }
        }

      mockMvc.get("/api/v1/readlists/${rlDenied.id}")
        .andExpect {
          status { isNotFound() }
        }

      mockMvc.get("/api/v1/books/${bookKids.id}/readlists")
        .andExpect {
          status { isOk() }
          jsonPath("$.length()") { value(2) }
          jsonPath("$[?(@.name == '${rlAllowed.name}')].filtered") { value(false) }
          jsonPath("$[?(@.name == '${rlFiltered.name}')].filtered") { value(true) }
        }
    }

    @Test
    @WithMockCustomUser(excludeAgeOver = 16, allowLabels = ["teen"])
    fun `given user allowed and disallowed content when getting read lists then only get read lists that satisfies this criteria (2)`() {
      val bookTeen16 = makeBook("book_teen_16", libraryId = library1.id)
      makeSeries(name = "series_teen_16", libraryId = library1.id).also { series ->
        seriesLifecycle.createSeries(series).also { created ->
          val books = listOf(bookTeen16)
          seriesLifecycle.addBooks(created, books)
        }
        seriesMetadataRepository.findById(series.id).let {
          seriesMetadataRepository.update(it.copy(sharingLabels = setOf("teen"), ageRating = 16))
        }
      }

      val bookTeen = makeBook("1", libraryId = library1.id)
      makeSeries(name = "series_teen", libraryId = library1.id).also { series ->
        seriesLifecycle.createSeries(series).also { created ->
          val books = listOf(bookTeen)
          seriesLifecycle.addBooks(created, books)
        }
        seriesMetadataRepository.findById(series.id).let {
          seriesMetadataRepository.update(it.copy(sharingLabels = setOf("teen")))
        }
      }

      val rlAllowed = readListLifecycle.addReadList(
        ReadList(
          name = "Allowed",
          bookIds = listOf(bookTeen.id).toIndexedMap(),
        ),
      )

      val rlFiltered = readListLifecycle.addReadList(
        ReadList(
          name = "Filtered",
          bookIds = listOf(bookTeen16.id, bookTeen.id).toIndexedMap(),
        ),
      )

      val rlDenied = readListLifecycle.addReadList(
        ReadList(
          name = "Denied",
          bookIds = listOf(bookTeen16.id).toIndexedMap(),
        ),
      )

      mockMvc.get("/api/v1/readlists")
        .andExpect {
          status { isOk() }
          jsonPath("$.totalElements") { value(2) }
          jsonPath("$.content[?(@.name == '${rlAllowed.name}')].filtered") { value(false) }
          jsonPath("$.content[?(@.name == '${rlFiltered.name}')].filtered") { value(true) }
        }

      mockMvc.get("/api/v1/readlists/${rlAllowed.id}")
        .andExpect {
          status { isOk() }
          jsonPath("$.bookIds.length()") { value(1) }
          jsonPath("$.filtered") { value(false) }
        }

      mockMvc.get("/api/v1/readlists/${rlFiltered.id}")
        .andExpect {
          status { isOk() }
          jsonPath("$.bookIds.length()") { value(1) }
          jsonPath("$.filtered") { value(true) }
        }

      mockMvc.get("/api/v1/readlists/${rlDenied.id}")
        .andExpect {
          status { isNotFound() }
        }

      mockMvc.get("/api/v1/books/${bookTeen.id}/readlists")
        .andExpect {
          status { isOk() }
          jsonPath("$.length()") { value(2) }
          jsonPath("$[?(@.name == '${rlAllowed.name}')].filtered") { value(false) }
          jsonPath("$[?(@.name == '${rlFiltered.name}')].filtered") { value(true) }
        }
    }
  }

  @Nested
  inner class GetBooksAndFilter {
    @Test
    @WithMockCustomUser
    fun `given user with access to all libraries when getting books from single read list then it is not filtered`() {
      makeReadLists()

      mockMvc.get("/api/v1/readlists/${rlLibBoth.id}/books")
        .andExpect {
          status { isOk() }
          jsonPath("$.content.length()") { value(10) }
        }
    }

    @Test
    @WithMockCustomUser(sharedAllLibraries = false, sharedLibraries = ["1"])
    fun `given user with access to a single library when getting books from single read list with items from 2 libraries then it is filtered`() {
      makeReadLists()

      mockMvc.get("/api/v1/readlists/${rlLibBoth.id}/books")
        .andExpect {
          status { isOk() }
          jsonPath("$.content.length()") { value(5) }
        }
    }

    @Test
    @WithMockCustomUser(sharedAllLibraries = false, sharedLibraries = ["1"])
    fun `given user with access to a single library when getting books from single read list from another library then return not found`() {
      makeReadLists()

      mockMvc.get("/api/v1/readlists/${rlLib2.id}/books")
        .andExpect {
          status { isNotFound() }
        }
    }
  }

  @Nested
  inner class Siblings {
    @Test
    @WithMockCustomUser
    fun `given user with access to all libraries when getting book siblings then it is returned or not found`() {
      makeReadLists()

      val first = booksLibrary1.first().id // Book_1
      val second = booksLibrary1[1].id // Book_2
      val last = booksLibrary2.last().id // Book_10

      mockMvc.get("/api/v1/readlists/${rlLibBoth.id}/books/$first/previous")
        .andExpect { status { isNotFound() } }
      mockMvc.get("/api/v1/readlists/${rlLibBoth.id}/books/$first/next")
        .andExpect {
          status { isOk() }
          jsonPath("$.name") { value("Book_2") }
        }

      mockMvc.get("/api/v1/readlists/${rlLibBoth.id}/books/$second/previous")
        .andExpect {
          status { isOk() }
          jsonPath("$.name") { value("Book_1") }
        }
      mockMvc.get("/api/v1/readlists/${rlLibBoth.id}/books/$second/next")
        .andExpect {
          status { isOk() }
          jsonPath("$.name") { value("Book_3") }
        }

      mockMvc.get("/api/v1/readlists/${rlLibBoth.id}/books/$last/previous")
        .andExpect {
          status { isOk() }
          jsonPath("$.name") { value("Book_9") }
        }
      mockMvc.get("/api/v1/readlists/${rlLibBoth.id}/books/$last/next")
        .andExpect { status { isNotFound() } }
    }

    @Test
    @WithMockCustomUser(sharedAllLibraries = false, sharedLibraries = ["1"])
    fun `given user with access to a single library when getting book siblings then it takes into account the library filter`() {
      makeReadLists()

      val first = booksLibrary1.first().id // Book_1
      val second = booksLibrary1[1].id // Book_2
      val last = booksLibrary1.last().id // Book_5

      mockMvc.get("/api/v1/readlists/${rlLibBoth.id}/books/$first/previous")
        .andExpect { status { isNotFound() } }
      mockMvc.get("/api/v1/readlists/${rlLibBoth.id}/books/$first/next")
        .andExpect {
          status { isOk() }
          jsonPath("$.name") { value("Book_2") }
        }

      mockMvc.get("/api/v1/readlists/${rlLibBoth.id}/books/$second/previous")
        .andExpect {
          status { isOk() }
          jsonPath("$.name") { value("Book_1") }
        }
      mockMvc.get("/api/v1/readlists/${rlLibBoth.id}/books/$second/next")
        .andExpect {
          status { isOk() }
          jsonPath("$.name") { value("Book_3") }
        }

      mockMvc.get("/api/v1/readlists/${rlLibBoth.id}/books/$last/previous")
        .andExpect {
          status { isOk() }
          jsonPath("$.name") { value("Book_4") }
        }
      mockMvc.get("/api/v1/readlists/${rlLibBoth.id}/books/$last/next")
        .andExpect { status { isNotFound() } }
    }

    @Test
    @WithMockCustomUser(sharedAllLibraries = false, sharedLibraries = ["1"])
    fun `given user with access to a single library when getting books from single read list from another library then return not found`() {
      makeReadLists()

      mockMvc.get("/api/v1/readlists/${rlLib2.id}/books")
        .andExpect {
          status { isNotFound() }
        }
    }
  }

  @Nested
  inner class Creation {
    @Test
    @WithMockCustomUser
    fun `given non-admin user when creating read list then return forbidden`() {
      val jsonString = """
        {"name":"readlist","bookIds":["3"]}
      """.trimIndent()

      mockMvc.post("/api/v1/readlists") {
        contentType = MediaType.APPLICATION_JSON
        content = jsonString
      }.andExpect {
        status { isForbidden() }
      }
    }

    @Test
    @WithMockCustomUser(roles = [ROLE_ADMIN])
    fun `given admin user when creating read list then return ok`() {
      val jsonString = """
        {"name":"readlist","summary":"summary","bookIds":["${booksLibrary1.first().id}"]}
      """.trimIndent()

      mockMvc.post("/api/v1/readlists") {
        contentType = MediaType.APPLICATION_JSON
        content = jsonString
      }.andExpect {
        status { isOk() }
        jsonPath("$.bookIds.length()") { value(1) }
        jsonPath("$.name") { value("readlist") }
        jsonPath("$.summary") { value("summary") }
      }
    }

    @Test
    @WithMockCustomUser(roles = [ROLE_ADMIN])
    fun `given existing read lists when creating read list with existing name then return bad request`() {
      makeReadLists()

      val jsonString = """
        {"name":"Lib1","bookIds":["${booksLibrary1.first().id}"]}
      """.trimIndent()

      mockMvc.post("/api/v1/readlists") {
        contentType = MediaType.APPLICATION_JSON
        content = jsonString
      }.andExpect {
        status { isBadRequest() }
      }
    }

    @Test
    @WithMockCustomUser(roles = [ROLE_ADMIN])
    fun `given read list with duplicate bookIds when creating read list then return bad request`() {
      val jsonString = """
        {"name":"Lib1","bookIds":["${booksLibrary1.first().id}","${booksLibrary1.first().id}"]}
      """.trimIndent()

      mockMvc.post("/api/v1/readlists") {
        contentType = MediaType.APPLICATION_JSON
        content = jsonString
      }.andExpect {
        status { isBadRequest() }
      }
    }
  }

  @Nested
  inner class Update {
    @Test
    @WithMockCustomUser
    fun `given non-admin user when updating read list then return forbidden`() {
      val jsonString = """
        {"name":"readlist","bookIds":["3"]}
      """.trimIndent()

      mockMvc.patch("/api/v1/readlists/5") {
        contentType = MediaType.APPLICATION_JSON
        content = jsonString
      }.andExpect {
        status { isForbidden() }
      }
    }

    @Test
    @WithMockCustomUser(roles = [ROLE_ADMIN])
    fun `given admin user when updating read list then return no content`() {
      makeReadLists()

      val jsonString = """
        {"name":"updated","summary":"updatedSummary","bookIds":["${booksLibrary1.first().id}"]}
      """.trimIndent()

      mockMvc.patch("/api/v1/readlists/${rlLib1.id}") {
        contentType = MediaType.APPLICATION_JSON
        content = jsonString
      }.andExpect {
        status { isNoContent() }
      }

      mockMvc.get("/api/v1/readlists/${rlLib1.id}")
        .andExpect {
          status { isOk() }
          jsonPath("$.name") { value("updated") }
          jsonPath("$.summary") { value("updatedSummary") }
          jsonPath("$.bookIds.length()") { value(1) }
          jsonPath("$.filtered") { value(false) }
        }
    }

    @Test
    @WithMockCustomUser(roles = [ROLE_ADMIN])
    fun `given existing read lists when updating read list with existing name then return bad request`() {
      makeReadLists()

      val jsonString = """{"name":"Lib2"}"""

      mockMvc.patch("/api/v1/readlists/${rlLib1.id}") {
        contentType = MediaType.APPLICATION_JSON
        content = jsonString
      }.andExpect {
        status { isBadRequest() }
      }
    }

    @Test
    @WithMockCustomUser(roles = [ROLE_ADMIN])
    fun `given existing read list when updating read list with duplicate bookIds then return bad request`() {
      makeReadLists()

      val jsonString = """{"bookIds":["${booksLibrary1.first().id}","${booksLibrary1.first().id}"]}"""

      mockMvc.patch("/api/v1/readlists/${rlLib1.id}") {
        contentType = MediaType.APPLICATION_JSON
        content = jsonString
      }.andExpect {
        status { isBadRequest() }
      }
    }

    @Test
    @WithMockCustomUser(roles = [ROLE_ADMIN])
    fun `given admin user when updating read list then only updated fields are modified`() {
      makeReadLists()

      mockMvc.patch("/api/v1/readlists/${rlLib2.id}") {
        contentType = MediaType.APPLICATION_JSON
        content = """{"name":"newName"}"""
      }

      mockMvc.get("/api/v1/readlists/${rlLib2.id}")
        .andExpect {
          status { isOk() }
          jsonPath("$.name") { value("newName") }
          jsonPath("$.summary") { value("") }
          jsonPath("$.bookIds.length()") { value(5) }
        }

      mockMvc.patch("/api/v1/readlists/${rlLib1.id}") {
        contentType = MediaType.APPLICATION_JSON
        content = """{"summary":"newSummary"}"""
      }

      mockMvc.get("/api/v1/readlists/${rlLib1.id}")
        .andExpect {
          status { isOk() }
          jsonPath("$.name") { value("Lib1") }
          jsonPath("$.summary") { value("newSummary") }
          jsonPath("$.bookIds.length()") { value(5) }
        }

      mockMvc.patch("/api/v1/readlists/${rlLibBoth.id}") {
        contentType = MediaType.APPLICATION_JSON
        content = """{"bookIds":["${booksLibrary1.first().id}"]}"""
      }

      mockMvc.get("/api/v1/readlists/${rlLibBoth.id}")
        .andExpect {
          status { isOk() }
          jsonPath("$.name") { value("Lib1+2") }
          jsonPath("$.summary") { value("") }
          jsonPath("$.bookIds.length()") { value(1) }
        }
    }
  }

  @Nested
  inner class Delete {
    @Test
    @WithMockCustomUser
    fun `given non-admin user when deleting read list then return forbidden`() {
      mockMvc.delete("/api/v1/readlists/5")
        .andExpect {
          status { isForbidden() }
        }
    }

    @Test
    @WithMockCustomUser(roles = [ROLE_ADMIN])
    fun `given admin user when deleting read list then return no content`() {
      makeReadLists()

      mockMvc.delete("/api/v1/readlists/${rlLib1.id}")
        .andExpect {
          status { isNoContent() }
        }

      mockMvc.get("/api/v1/readlists/${rlLib1.id}")
        .andExpect {
          status { isNotFound() }
        }
    }
  }
}
