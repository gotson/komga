package org.gotson.komga.interfaces.api.rest

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.groups.Tuple.tuple
import org.gotson.komga.domain.model.Author
import org.gotson.komga.domain.model.BookPage
import org.gotson.komga.domain.model.Dimension
import org.gotson.komga.domain.model.KomgaUser
import org.gotson.komga.domain.model.MarkSelectedPreference
import org.gotson.komga.domain.model.Media
import org.gotson.komga.domain.model.ThumbnailBook
import org.gotson.komga.domain.model.makeBook
import org.gotson.komga.domain.model.makeLibrary
import org.gotson.komga.domain.model.makeSeries
import org.gotson.komga.domain.persistence.BookMetadataRepository
import org.gotson.komga.domain.persistence.BookRepository
import org.gotson.komga.domain.persistence.KomgaUserRepository
import org.gotson.komga.domain.persistence.LibraryRepository
import org.gotson.komga.domain.persistence.MediaRepository
import org.gotson.komga.domain.persistence.SeriesMetadataRepository
import org.gotson.komga.domain.persistence.SeriesRepository
import org.gotson.komga.domain.service.BookLifecycle
import org.gotson.komga.domain.service.KomgaUserLifecycle
import org.gotson.komga.domain.service.LibraryLifecycle
import org.gotson.komga.domain.service.SeriesLifecycle
import org.gotson.komga.infrastructure.security.KomgaPrincipal
import org.hamcrest.Matchers.containsString
import org.hamcrest.core.IsNull
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource
import org.junit.jupiter.params.provider.ValueSource
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.MockMvcResultMatchersDsl
import org.springframework.test.web.servlet.delete
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.patch
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.time.LocalDate
import kotlin.random.Random

@SpringBootTest
@AutoConfigureMockMvc(printOnlyOnFailure = false)
class BookControllerTest(
  @Autowired private val seriesRepository: SeriesRepository,
  @Autowired private val seriesMetadataRepository: SeriesMetadataRepository,
  @Autowired private val seriesLifecycle: SeriesLifecycle,
  @Autowired private val mediaRepository: MediaRepository,
  @Autowired private val bookMetadataRepository: BookMetadataRepository,
  @Autowired private val libraryRepository: LibraryRepository,
  @Autowired private val libraryLifecycle: LibraryLifecycle,
  @Autowired private val bookRepository: BookRepository,
  @Autowired private val bookLifecycle: BookLifecycle,
  @Autowired private val userRepository: KomgaUserRepository,
  @Autowired private val userLifecycle: KomgaUserLifecycle,
  @Autowired private val mockMvc: MockMvc,
) {
  private val library = makeLibrary(id = "1")
  private val user = KomgaUser("user@example.org", "", id = "1")
  private val user2 = KomgaUser("user2@example.org", "", id = "2")

  @BeforeAll
  fun `setup library`() {
    libraryRepository.insert(library)
    userRepository.insert(user)
    userRepository.insert(user2)
  }

  @AfterAll
  fun teardown() {
    userRepository.findAll().forEach {
      userLifecycle.deleteUser(it)
    }
    libraryRepository.findAll().forEach {
      libraryLifecycle.deleteLibrary(it)
    }
  }

  @AfterEach
  fun `clear repository`() {
    seriesLifecycle.deleteMany(seriesRepository.findAll())
  }

  @Nested
  inner class LimitedUser {
    @Test
    @WithMockCustomUser(sharedAllLibraries = false, sharedLibraries = ["1"])
    fun `given user with access to a single library when getting books then only gets books from this library`() {
      makeSeries(name = "series", libraryId = library.id).let { series ->
        seriesLifecycle.createSeries(series).let { created ->
          val books = listOf(makeBook("1", libraryId = library.id))
          seriesLifecycle.addBooks(created, books)
        }
      }

      val otherLibrary = makeLibrary("other")
      libraryRepository.insert(otherLibrary)
      makeSeries(name = "otherSeries", libraryId = otherLibrary.id).let { series ->
        seriesLifecycle.createSeries(series).let { created ->
          val otherBooks = listOf(makeBook("2", libraryId = otherLibrary.id))
          seriesLifecycle.addBooks(created, otherBooks)
        }
      }

      mockMvc
        .get("/api/v1/books")
        .andExpect {
          status { isOk() }
          jsonPath("$.content.length()") { value(1) }
          jsonPath("$.content[0].name") { value("1") }
        }
    }
  }

  @Nested
  inner class RestrictedContent {
    @Test
    @WithMockCustomUser(allowAgeUnder = 10)
    fun `given user only allowed content with specific age rating when getting books then only gets books that satisfies this criteria`() {
      val book10 = makeBook("book_10", libraryId = library.id)
      makeSeries(name = "series_10", libraryId = library.id).also { series ->
        seriesLifecycle.createSeries(series).also { created ->
          val books = listOf(book10)
          seriesLifecycle.addBooks(created, books)
        }
        seriesMetadataRepository.findById(series.id).let {
          seriesMetadataRepository.update(it.copy(ageRating = 10))
        }
      }

      val book5 = makeBook("book_5", libraryId = library.id)
      makeSeries(name = "series_5", libraryId = library.id).also { series ->
        seriesLifecycle.createSeries(series).also { created ->
          val books = listOf(book5)
          seriesLifecycle.addBooks(created, books)
        }
        seriesMetadataRepository.findById(series.id).let {
          seriesMetadataRepository.update(it.copy(ageRating = 5))
        }
      }

      val book15 = makeBook("book_15", libraryId = library.id)
      makeSeries(name = "series_15", libraryId = library.id).also { series ->
        seriesLifecycle.createSeries(series).also { created ->
          val books = listOf(book15)
          seriesLifecycle.addBooks(created, books)
        }
        seriesMetadataRepository.findById(series.id).let {
          seriesMetadataRepository.update(it.copy(ageRating = 15))
        }
      }

      val book = makeBook("book", libraryId = library.id)
      makeSeries(name = "series_no", libraryId = library.id).also { series ->
        seriesLifecycle.createSeries(series).also { created ->
          val books = listOf(book)
          seriesLifecycle.addBooks(created, books)
        }
      }

      mockMvc.get("/api/v1/books/${book5.id}").andExpect { status { isOk() } }
      mockMvc.get("/api/v1/books/${book10.id}").andExpect { status { isOk() } }
      mockMvc.get("/api/v1/books/${book15.id}").andExpect { status { isForbidden() } }
      mockMvc.get("/api/v1/books/${book.id}").andExpect { status { isForbidden() } }

      mockMvc
        .get("/api/v1/books?sort=metadata.title")
        .andExpect {
          status { isOk() }
          jsonPath("$.content.length()") { value(2) }
          jsonPath("$.content[0].name") { value(book10.name) }
          jsonPath("$.content[1].name") { value(book5.name) }
        }
    }

    @Test
    @WithMockCustomUser(excludeAgeOver = 10)
    fun `given user disallowed content with specific age rating when getting books then only gets books that satisfies this criteria`() {
      val book10 = makeBook("book_10", libraryId = library.id)
      makeSeries(name = "series_10", libraryId = library.id).also { series ->
        seriesLifecycle.createSeries(series).also { created ->
          val books = listOf(book10)
          seriesLifecycle.addBooks(created, books)
        }
        seriesMetadataRepository.findById(series.id).let {
          seriesMetadataRepository.update(it.copy(ageRating = 10))
        }
      }

      val book5 = makeBook("book_5", libraryId = library.id)
      makeSeries(name = "series_5", libraryId = library.id).also { series ->
        seriesLifecycle.createSeries(series).also { created ->
          val books = listOf(book5)
          seriesLifecycle.addBooks(created, books)
        }
        seriesMetadataRepository.findById(series.id).let {
          seriesMetadataRepository.update(it.copy(ageRating = 5))
        }
      }

      val book15 = makeBook("book_15", libraryId = library.id)
      makeSeries(name = "series_15", libraryId = library.id).also { series ->
        seriesLifecycle.createSeries(series).also { created ->
          val books = listOf(book15)
          seriesLifecycle.addBooks(created, books)
        }
        seriesMetadataRepository.findById(series.id).let {
          seriesMetadataRepository.update(it.copy(ageRating = 15))
        }
      }

      val book = makeBook("book", libraryId = library.id)
      makeSeries(name = "series_no", libraryId = library.id).also { series ->
        seriesLifecycle.createSeries(series).also { created ->
          val books = listOf(book)
          seriesLifecycle.addBooks(created, books)
        }
      }

      mockMvc.get("/api/v1/books/${book5.id}").andExpect { status { isOk() } }
      mockMvc.get("/api/v1/books/${book.id}").andExpect { status { isOk() } }
      mockMvc.get("/api/v1/books/${book10.id}").andExpect { status { isForbidden() } }
      mockMvc.get("/api/v1/books/${book15.id}").andExpect { status { isForbidden() } }

      mockMvc
        .get("/api/v1/books?sort=metadata.title")
        .andExpect {
          status { isOk() }
          jsonPath("$.content.length()") { value(2) }
          jsonPath("$.content[0].name") { value(book.name) }
          jsonPath("$.content[1].name") { value(book5.name) }
        }
    }

    @Test
    @WithMockCustomUser(allowLabels = ["kids", "cute"])
    fun `given user allowed content with specific labels when getting series then only gets books that satisfies this criteria`() {
      val bookKids = makeBook("book_kids", libraryId = library.id)
      makeSeries(name = "series_kids", libraryId = library.id).also { series ->
        seriesLifecycle.createSeries(series).also { created ->
          val books = listOf(bookKids)
          seriesLifecycle.addBooks(created, books)
        }
        seriesMetadataRepository.findById(series.id).let {
          seriesMetadataRepository.update(it.copy(sharingLabels = setOf("kids")))
        }
      }

      val bookCute = makeBook("book_cute", libraryId = library.id)
      makeSeries(name = "series_cute", libraryId = library.id).also { series ->
        seriesLifecycle.createSeries(series).also { created ->
          val books = listOf(bookCute)
          seriesLifecycle.addBooks(created, books)
        }
        seriesMetadataRepository.findById(series.id).let {
          seriesMetadataRepository.update(it.copy(sharingLabels = setOf("cute", "other")))
        }
      }

      val bookAdult = makeBook("book_adult", libraryId = library.id)
      makeSeries(name = "series_adult", libraryId = library.id).also { series ->
        seriesLifecycle.createSeries(series).also { created ->
          val books = listOf(bookAdult)
          seriesLifecycle.addBooks(created, books)
        }
        seriesMetadataRepository.findById(series.id).let {
          seriesMetadataRepository.update(it.copy(sharingLabels = setOf("adult")))
        }
      }

      val book = makeBook("book", libraryId = library.id)
      makeSeries(name = "series_no", libraryId = library.id).also { series ->
        seriesLifecycle.createSeries(series).also { created ->
          val books = listOf(book)
          seriesLifecycle.addBooks(created, books)
        }
      }

      mockMvc.get("/api/v1/books/${bookKids.id}").andExpect { status { isOk() } }
      mockMvc.get("/api/v1/books/${bookCute.id}").andExpect { status { isOk() } }
      mockMvc.get("/api/v1/books/${bookAdult.id}").andExpect { status { isForbidden() } }
      mockMvc.get("/api/v1/books/${book.id}").andExpect { status { isForbidden() } }

      mockMvc
        .get("/api/v1/books?sort=metadata.title")
        .andExpect {
          status { isOk() }
          jsonPath("$.content.length()") { value(2) }
          jsonPath("$.content[0].name") { value(bookCute.name) }
          jsonPath("$.content[1].name") { value(bookKids.name) }
        }
    }

    @Test
    @WithMockCustomUser(excludeLabels = ["kids", "cute"])
    fun `given user disallowed content with specific labels when getting books then only gets books that satisfies this criteria`() {
      val bookKids = makeBook("book_kids", libraryId = library.id)
      makeSeries(name = "series_kids", libraryId = library.id).also { series ->
        seriesLifecycle.createSeries(series).also { created ->
          val books = listOf(bookKids)
          seriesLifecycle.addBooks(created, books)
        }
        seriesMetadataRepository.findById(series.id).let {
          seriesMetadataRepository.update(it.copy(sharingLabels = setOf("kids")))
        }
      }

      val bookCute = makeBook("book_cute", libraryId = library.id)
      makeSeries(name = "series_cute", libraryId = library.id).also { series ->
        seriesLifecycle.createSeries(series).also { created ->
          val books = listOf(bookCute)
          seriesLifecycle.addBooks(created, books)
        }
        seriesMetadataRepository.findById(series.id).let {
          seriesMetadataRepository.update(it.copy(sharingLabels = setOf("cute", "other")))
        }
      }

      val bookAdult = makeBook("book_adult", libraryId = library.id)
      makeSeries(name = "series_adult", libraryId = library.id).also { series ->
        seriesLifecycle.createSeries(series).also { created ->
          val books = listOf(bookAdult)
          seriesLifecycle.addBooks(created, books)
        }
        seriesMetadataRepository.findById(series.id).let {
          seriesMetadataRepository.update(it.copy(sharingLabels = setOf("adult")))
        }
      }

      val book = makeBook("book", libraryId = library.id)
      makeSeries(name = "series_no", libraryId = library.id).also { series ->
        seriesLifecycle.createSeries(series).also { created ->
          val books = listOf(book)
          seriesLifecycle.addBooks(created, books)
        }
      }

      mockMvc.get("/api/v1/books/${bookKids.id}").andExpect { status { isForbidden() } }
      mockMvc.get("/api/v1/books/${bookCute.id}").andExpect { status { isForbidden() } }
      mockMvc.get("/api/v1/books/${bookAdult.id}").andExpect { status { isOk() } }
      mockMvc.get("/api/v1/books/${book.id}").andExpect { status { isOk() } }

      mockMvc
        .get("/api/v1/books?sort=metadata.title")
        .andExpect {
          status { isOk() }
          jsonPath("$.content.length()") { value(2) }
          jsonPath("$.content[0].name") { value(book.name) }
          jsonPath("$.content[1].name") { value(bookAdult.name) }
        }
    }

    @Test
    @WithMockCustomUser(allowAgeUnder = 10, allowLabels = ["kids"], excludeLabels = ["adult", "teen"])
    fun `given user allowed and disallowed content labels when getting books then only gets books that satisfies this criteria`() {
      val bookKids = makeBook("book_kids", libraryId = library.id)
      makeSeries(name = "series_kids", libraryId = library.id).also { series ->
        seriesLifecycle.createSeries(series).also { created ->
          val books = listOf(bookKids)
          seriesLifecycle.addBooks(created, books)
        }
        seriesMetadataRepository.findById(series.id).let {
          seriesMetadataRepository.update(it.copy(sharingLabels = setOf("kids")))
        }
      }

      val bookCute = makeBook("book_cute", libraryId = library.id)
      makeSeries(name = "series_cute", libraryId = library.id).also { series ->
        seriesLifecycle.createSeries(series).also { created ->
          val books = listOf(bookCute)
          seriesLifecycle.addBooks(created, books)
        }
        seriesMetadataRepository.findById(series.id).let {
          seriesMetadataRepository.update(it.copy(ageRating = 5, sharingLabels = setOf("cute", "other")))
        }
      }

      val bookAdult = makeBook("book_adult", libraryId = library.id)
      makeSeries(name = "series_adult", libraryId = library.id).also { series ->
        seriesLifecycle.createSeries(series).also { created ->
          val books = listOf(bookAdult)
          seriesLifecycle.addBooks(created, books)
        }
        seriesMetadataRepository.findById(series.id).let {
          seriesMetadataRepository.update(it.copy(sharingLabels = setOf("adult")))
        }
      }

      val book = makeBook("book", libraryId = library.id)
      makeSeries(name = "series_no", libraryId = library.id).also { series ->
        seriesLifecycle.createSeries(series).also { created ->
          val books = listOf(book)
          seriesLifecycle.addBooks(created, books)
        }
      }

      mockMvc.get("/api/v1/books/${bookKids.id}").andExpect { status { isOk() } }
      mockMvc.get("/api/v1/books/${bookCute.id}").andExpect { status { isOk() } }
      mockMvc.get("/api/v1/books/${bookAdult.id}").andExpect { status { isForbidden() } }
      mockMvc.get("/api/v1/books/${book.id}").andExpect { status { isForbidden() } }

      mockMvc
        .get("/api/v1/books?sort=metadata.title")
        .andExpect {
          status { isOk() }
          jsonPath("$.content.length()") { value(2) }
          jsonPath("$.content[0].name") { value(bookCute.name) }
          jsonPath("$.content[1].name") { value(bookKids.name) }
        }
    }

    @Test
    @WithMockCustomUser(excludeAgeOver = 16, allowLabels = ["teen"])
    fun `given user allowed and disallowed content labels when getting books then only gets books that satisfies this criteria (2)`() {
      val bookTeen16 = makeBook("book_teen_16", libraryId = library.id)
      makeSeries(name = "series_teen_16", libraryId = library.id).also { series ->
        seriesLifecycle.createSeries(series).also { created ->
          val books = listOf(bookTeen16)
          seriesLifecycle.addBooks(created, books)
        }
        seriesMetadataRepository.findById(series.id).let {
          seriesMetadataRepository.update(it.copy(sharingLabels = setOf("teen"), ageRating = 16))
        }
      }

      mockMvc.get("/api/v1/books/${bookTeen16.id}").andExpect { status { isForbidden() } }

      mockMvc
        .get("/api/v1/books")
        .andExpect {
          status { isOk() }
          jsonPath("$.content.length()") { value(0) }
        }
    }
  }

  @Nested
  inner class UserWithoutLibraryAccess {
    @Test
    @WithMockCustomUser(sharedAllLibraries = false, sharedLibraries = [])
    fun `given user with no access to any library when getting specific book then returns forbidden`() {
      makeSeries(name = "series", libraryId = library.id).let { series ->
        seriesLifecycle.createSeries(series).let { created ->
          val books = listOf(makeBook("1", libraryId = library.id))
          seriesLifecycle.addBooks(created, books)
        }
      }

      val book = bookRepository.findAll().first()

      mockMvc
        .get("/api/v1/books/${book.id}")
        .andExpect { status { isForbidden() } }
    }

    @Test
    @WithMockCustomUser(sharedAllLibraries = false, sharedLibraries = [])
    fun `given user with no access to any library when getting specific book thumbnail then returns forbidden`() {
      makeSeries(name = "series", libraryId = library.id).let { series ->
        seriesLifecycle.createSeries(series).let { created ->
          val books = listOf(makeBook("1", libraryId = library.id))
          seriesLifecycle.addBooks(created, books)
        }
      }

      val book = bookRepository.findAll().first()

      mockMvc
        .get("/api/v1/books/${book.id}/thumbnail")
        .andExpect { status { isForbidden() } }
    }

    @Test
    @WithMockCustomUser(sharedAllLibraries = false, sharedLibraries = [])
    fun `given user with no access to any library when getting specific book file then returns forbidden`() {
      makeSeries(name = "series", libraryId = library.id).let { series ->
        seriesLifecycle.createSeries(series).let { created ->
          val books = listOf(makeBook("1", libraryId = library.id))
          seriesLifecycle.addBooks(created, books)
        }
      }

      val book = bookRepository.findAll().first()

      mockMvc
        .get("/api/v1/books/${book.id}/file")
        .andExpect { status { isForbidden() } }
    }

    @Test
    @WithMockCustomUser(sharedAllLibraries = false, sharedLibraries = [])
    fun `given user with no access to any library when getting specific book pages then returns forbidden`() {
      makeSeries(name = "series", libraryId = library.id).let { series ->
        seriesLifecycle.createSeries(series).let { created ->
          val books = listOf(makeBook("1", libraryId = library.id))
          seriesLifecycle.addBooks(created, books)
        }
      }

      val book = bookRepository.findAll().first()

      mockMvc
        .get("/api/v1/books/${book.id}/pages")
        .andExpect { status { isForbidden() } }
    }

    @Test
    @WithMockCustomUser(sharedAllLibraries = false, sharedLibraries = [])
    fun `given user with no access to any library when getting specific book page then returns forbidden`() {
      makeSeries(name = "series", libraryId = library.id).let { series ->
        seriesLifecycle.createSeries(series).let { created ->
          val books = listOf(makeBook("1", libraryId = library.id))
          seriesLifecycle.addBooks(created, books)
        }
      }

      val book = bookRepository.findAll().first()

      mockMvc
        .get("/api/v1/books/${book.id}/pages/1")
        .andExpect { status { isForbidden() } }
    }
  }

  @Nested
  inner class RestrictedUserByRole {
    @Test
    @WithMockCustomUser(roles = [])
    fun `given user without page streaming role when getting specific book page then returns unauthorized`() {
      makeSeries(name = "series", libraryId = library.id).let { series ->
        seriesLifecycle.createSeries(series).let { created ->
          val books = listOf(makeBook("1", libraryId = library.id))
          seriesLifecycle.addBooks(created, books)
        }
      }

      val book = bookRepository.findAll().first()

      mockMvc
        .get("/api/v1/books/${book.id}/pages/1")
        .andExpect { status { isForbidden() } }
    }

    @Test
    @WithMockCustomUser(roles = [])
    fun `given user without file download role when getting specific book file then returns unauthorized`() {
      makeSeries(name = "series", libraryId = library.id).let { series ->
        seriesLifecycle.createSeries(series).let { created ->
          val books = listOf(makeBook("1", libraryId = library.id))
          seriesLifecycle.addBooks(created, books)
        }
      }

      val book = bookRepository.findAll().first()

      mockMvc
        .get("/api/v1/books/${book.id}/file")
        .andExpect { status { isForbidden() } }
    }
  }

  @Nested
  inner class MediaNotReady {
    @Test
    @WithMockCustomUser
    fun `given book without thumbnail when getting book thumbnail then returns not found`() {
      makeSeries(name = "series", libraryId = library.id).let { series ->
        seriesLifecycle.createSeries(series).let { created ->
          val books = listOf(makeBook("1", libraryId = library.id))
          seriesLifecycle.addBooks(created, books)
        }
      }

      val book = bookRepository.findAll().first()

      mockMvc
        .get("/api/v1/books/${book.id}/thumbnail")
        .andExpect { status { isNotFound() } }
    }

    @Test
    @WithMockCustomUser
    fun `given book without file when getting book file then returns not found`() {
      makeSeries(name = "series", libraryId = library.id).let { series ->
        seriesLifecycle.createSeries(series).let { created ->
          val books = listOf(makeBook("1", libraryId = library.id))
          seriesLifecycle.addBooks(created, books)
        }
      }

      val book = bookRepository.findAll().first()

      mockMvc
        .get("/api/v1/books/${book.id}/file")
        .andExpect { status { isNotFound() } }
    }

    @ParameterizedTest
    @EnumSource(value = Media.Status::class, names = ["READY"], mode = EnumSource.Mode.EXCLUDE)
    @WithMockCustomUser
    fun `given book with media status not ready when getting book pages then returns not found`(status: Media.Status) {
      makeSeries(name = "series", libraryId = library.id).let { series ->
        seriesLifecycle.createSeries(series).let { created ->
          val books = listOf(makeBook("1", libraryId = library.id))
          seriesLifecycle.addBooks(created, books)
        }
      }

      val book = bookRepository.findAll().first()
      mediaRepository.findById(book.id).let {
        mediaRepository.update(it.copy(status = status))
      }

      mockMvc
        .get("/api/v1/books/${book.id}/pages")
        .andExpect { status { isNotFound() } }
    }

    @ParameterizedTest
    @EnumSource(value = Media.Status::class, names = ["READY"], mode = EnumSource.Mode.EXCLUDE)
    @WithMockCustomUser
    fun `given book with media status not ready when getting specific book page then returns not found`(status: Media.Status) {
      makeSeries(name = "series", libraryId = library.id).let { series ->
        seriesLifecycle.createSeries(series).let { created ->
          val books = listOf(makeBook("1", libraryId = library.id))
          seriesLifecycle.addBooks(created, books)
        }
      }

      val book = bookRepository.findAll().first()
      mediaRepository.findById(book.id).let {
        mediaRepository.update(it.copy(status = status))
      }

      mockMvc
        .get("/api/v1/books/${book.id}/pages/1")
        .andExpect { status { isNotFound() } }
    }
  }

  @ParameterizedTest
  @ValueSource(strings = ["25", "-5", "0"])
  @WithMockCustomUser
  fun `given book with pages when getting non-existent page then returns bad request`(page: String) {
    makeSeries(name = "series", libraryId = library.id).let { series ->
      seriesLifecycle.createSeries(series).let { created ->
        val books = listOf(makeBook("1", libraryId = library.id))
        seriesLifecycle.addBooks(created, books)
      }
    }

    val book = bookRepository.findAll().first()
    mediaRepository.findById(book.id).let {
      mediaRepository.update(
        it.copy(
          status = Media.Status.READY,
          pages = listOf(BookPage("file", "image/jpeg")),
        ),
      )
    }

    mockMvc
      .get("/api/v1/books/${book.id}/pages/$page")
      .andExpect { status { isBadRequest() } }
  }

  @Nested
  inner class Siblings {
    @Test
    @WithMockCustomUser
    fun `given series with multiple books when getting siblings then it is returned or not found`() {
      val book1 = makeBook("1", libraryId = library.id)
      val book2 = makeBook("2", libraryId = library.id)
      val book3 = makeBook("3", libraryId = library.id)
      makeSeries(name = "series", libraryId = library.id).let { series ->
        seriesLifecycle.createSeries(series).also { created ->
          val books = listOf(book1, book2, book3)
          seriesLifecycle.addBooks(created, books)
          seriesLifecycle.sortBooks(created)
        }
      }

      mockMvc
        .get("/api/v1/books/${book1.id}/previous")
        .andExpect { status { isNotFound() } }
      mockMvc
        .get("/api/v1/books/${book1.id}/next")
        .andExpect {
          status { isOk() }
          jsonPath("$.name") { value("2") }
        }

      mockMvc
        .get("/api/v1/books/${book2.id}/previous")
        .andExpect {
          status { isOk() }
          jsonPath("$.name") { value("1") }
        }
      mockMvc
        .get("/api/v1/books/${book2.id}/next")
        .andExpect {
          status { isOk() }
          jsonPath("$.name") { value("3") }
        }

      mockMvc
        .get("/api/v1/books/${book3.id}/previous")
        .andExpect {
          status { isOk() }
          jsonPath("$.name") { value("2") }
        }
      mockMvc
        .get("/api/v1/books/${book3.id}/next")
        .andExpect { status { isNotFound() } }
    }
  }

  @Nested
  inner class DtoUrlSanitization {
    @Test
    @WithMockCustomUser
    fun `given regular user when getting books then full url is hidden`() {
      val createdSeries =
        makeSeries(name = "series", libraryId = library.id).let { series ->
          seriesLifecycle.createSeries(series).also { created ->
            val books = listOf(makeBook("1.cbr", libraryId = library.id))
            seriesLifecycle.addBooks(created, books)
          }
        }

      val book = bookRepository.findAll().first()

      val validation: MockMvcResultMatchersDsl.() -> Unit = {
        status { isOk() }
        jsonPath("$.content[0].url") { value("1.cbr") }
      }

      mockMvc
        .get("/api/v1/books")
        .andExpect(validation)

      mockMvc
        .get("/api/v1/books/latest")
        .andExpect(validation)

      mockMvc
        .get("/api/v1/series/${createdSeries.id}/books")
        .andExpect(validation)

      mockMvc
        .get("/api/v1/books/${book.id}")
        .andExpect {
          status { isOk() }
          jsonPath("$.url") { value("1.cbr") }
        }
    }

    @Test
    @WithMockCustomUser(roles = ["ADMIN"])
    fun `given admin user when getting books then full url is available`() {
      val createdSeries =
        makeSeries(name = "series", libraryId = library.id).let { series ->
          seriesLifecycle.createSeries(series).also { created ->
            val books = listOf(makeBook("1.cbr", libraryId = library.id))
            seriesLifecycle.addBooks(created, books)
          }
        }

      val book = bookRepository.findAll().first()

      val validation: MockMvcResultMatchersDsl.() -> Unit = {
        status { isOk() }
        jsonPath("$.content[0].url") { value(containsString("1.cbr")) }
      }

      mockMvc
        .get("/api/v1/books")
        .andExpect(validation)

      mockMvc
        .get("/api/v1/books/latest")
        .andExpect(validation)

      mockMvc
        .get("/api/v1/series/${createdSeries.id}/books")
        .andExpect(validation)

      mockMvc
        .get("/api/v1/books/${book.id}")
        .andExpect {
          status { isOk() }
          jsonPath("$.url") { value(containsString("1.cbr")) }
        }
    }
  }

  @Nested
  inner class HttpCache {
    @Test
    @WithMockCustomUser
    fun `given request with cache headers when getting thumbnail then returns 304 not modified`() {
      makeSeries(name = "series", libraryId = library.id).let { series ->
        seriesLifecycle.createSeries(series).also { created ->
          val books = listOf(makeBook("1.cbr", libraryId = library.id))
          seriesLifecycle.addBooks(created, books)
        }
      }

      val book = bookRepository.findAll().first()
      bookLifecycle.addThumbnailForBook(
        ThumbnailBook(
          thumbnail = Random.nextBytes(100),
          bookId = book.id,
          type = ThumbnailBook.Type.GENERATED,
          fileSize = 0,
          mediaType = "",
          dimension = Dimension(0, 0),
        ),
        MarkSelectedPreference.YES,
      )

      val url = "/api/v1/books/${book.id}/thumbnail"

      val response =
        mockMvc
          .get(url)
          .andReturn()
          .response

      mockMvc
        .get(url) {
          headers {
            ifNoneMatch = listOf(response.getHeader(HttpHeaders.ETAG)!!)
          }
        }.andExpect {
          status { isNotModified() }
        }
    }

    @Test
    @WithMockCustomUser
    fun `given request with If-Modified-Since headers when getting page then returns 304 not modified`() {
      makeSeries(name = "series", libraryId = library.id).let { series ->
        seriesLifecycle.createSeries(series).also { created ->
          val books = listOf(makeBook("1.cbr", libraryId = library.id))
          seriesLifecycle.addBooks(created, books)
        }
      }

      val book = bookRepository.findAll().first()

      val url = "/api/v1/books/${book.id}/pages/1"

      val lastModified =
        mockMvc
          .get(url)
          .andReturn()
          .response
          .getHeader(HttpHeaders.LAST_MODIFIED)

      mockMvc
        .get(url) {
          headers {
            set(HttpHeaders.IF_MODIFIED_SINCE, lastModified!!)
          }
        }.andExpect {
          status { isNotModified() }
        }
    }

    @Test
    @WithMockCustomUser
    fun `given request with cache headers and modified resource when getting thumbnail then returns 200 ok`() {
      makeSeries(name = "series", libraryId = library.id).let { series ->
        seriesLifecycle.createSeries(series).also { created ->
          val books = listOf(makeBook("1.cbr", libraryId = library.id))
          seriesLifecycle.addBooks(created, books)
        }
      }

      val book = bookRepository.findAll().first()
      bookLifecycle.addThumbnailForBook(
        ThumbnailBook(
          thumbnail = Random.nextBytes(1),
          bookId = book.id,
          type = ThumbnailBook.Type.GENERATED,
          fileSize = 0,
          mediaType = "",
          dimension = Dimension(0, 0),
        ),
        MarkSelectedPreference.YES,
      )

      val url = "/api/v1/books/${book.id}/thumbnail"

      val response = mockMvc.get(url).andReturn().response

      Thread.sleep(100)
      bookLifecycle.addThumbnailForBook(
        ThumbnailBook(
          thumbnail = Random.nextBytes(1),
          bookId = book.id,
          type = ThumbnailBook.Type.GENERATED,
          fileSize = 0,
          mediaType = "",
          dimension = Dimension(0, 0),
        ),
        MarkSelectedPreference.YES,
      )

      mockMvc
        .get(url) {
          headers {
            ifNoneMatch = listOf(response.getHeader(HttpHeaders.ETAG)!!)
          }
        }.andExpect {
          status { isOk() }
        }
    }
  }

  @Nested
  inner class MetadataUpdate {
    @Test
    @WithMockCustomUser
    fun `given non-admin user when updating metadata then raise forbidden`() {
      mockMvc
        .patch("/api/v1/books/1/metadata") {
          contentType = MediaType.APPLICATION_JSON
          content = "{}"
        }.andExpect {
          status { isForbidden() }
        }
    }

    @ParameterizedTest
    @ValueSource(
      strings = [
        """{"title":""}""",
        """{"number":""}""",
        """{"authors":"[{"name":""}]"}""",
        """{"isbn":"1617290459"}""", // isbn 10
        """{"isbn":"978-123-456-789-6"}""", // invalid check digit
      ],
    )
    @WithMockCustomUser(roles = ["ADMIN"])
    fun `given invalid json when updating metadata then raise validation error`(jsonString: String) {
      mockMvc
        .patch("/api/v1/books/1/metadata") {
          contentType = MediaType.APPLICATION_JSON
          content = jsonString
        }.andExpect {
          status { isBadRequest() }
        }
    }

    @Test
    @WithMockCustomUser(roles = ["ADMIN"])
    fun `given valid json when updating metadata then fields are updated`() {
      makeSeries(name = "series", libraryId = library.id).let { series ->
        seriesLifecycle.createSeries(series).also { created ->
          val books = listOf(makeBook("1.cbr", libraryId = library.id))
          seriesLifecycle.addBooks(created, books)
        }
      }

      val bookId = bookRepository.findAll().first().id

      // language=JSON
      val jsonString =
        """
        {
          "title":"newTitle",
          "titleLock":true,
          "summary":"newSummary",
          "summaryLock":true,
          "number":"newNumber",
          "numberLock":true,
          "numberSort": 1.0,
          "numberSortLock":true,
          "releaseDate":"2020-01-01",
          "releaseDateLock":true,
          "authors":[
            {
              "name":"newAuthor",
              "role":"newAuthorRole"
            },
            {
              "name":"newAuthor2",
              "role":"newAuthorRole2"
            }
          ],
          "authorsLock":true,
          "tags":["tag"],
          "tagsLock":true,
          "isbn":"978-161-729-045-9abc xxxoefj",
          "isbnLock":true
        }
        """.trimIndent()

      mockMvc
        .patch("/api/v1/books/$bookId/metadata") {
          contentType = MediaType.APPLICATION_JSON
          content = jsonString
        }.andExpect {
          status { isNoContent() }
        }

      val metadata = bookMetadataRepository.findById(bookId)
      with(metadata) {
        assertThat(title).isEqualTo("newTitle")
        assertThat(summary).isEqualTo("newSummary")
        assertThat(number).isEqualTo("newNumber")
        assertThat(numberSort).isEqualTo(1F)
        assertThat(releaseDate).isEqualTo(LocalDate.of(2020, 1, 1))
        assertThat(authors)
          .hasSize(2)
          .extracting("name", "role")
          .containsExactlyInAnyOrder(
            tuple("newAuthor", "newauthorrole"),
            tuple("newAuthor2", "newauthorrole2"),
          )
        assertThat(tags).containsExactly("tag")
        assertThat(isbn).isEqualTo("9781617290459")

        assertThat(titleLock).isEqualTo(true)
        assertThat(summaryLock).isEqualTo(true)
        assertThat(numberLock).isEqualTo(true)
        assertThat(numberSortLock).isEqualTo(true)
        assertThat(releaseDateLock).isEqualTo(true)
        assertThat(authorsLock).isEqualTo(true)
        assertThat(tagsLock).isEqualTo(true)
        assertThat(isbnLock).isEqualTo(true)
      }
    }

    @Test
    @WithMockCustomUser(roles = ["ADMIN"])
    fun `given json with blank fields when updating metadata then fields with blanks are unset`() {
      makeSeries(name = "series", libraryId = library.id).let { series ->
        seriesLifecycle.createSeries(series).also { created ->
          val books = listOf(makeBook("1.cbr", libraryId = library.id))
          seriesLifecycle.addBooks(created, books)
        }
      }

      val bookId = bookRepository.findAll().first().id
      bookMetadataRepository.findById(bookId).let { metadata ->
        val updated =
          metadata.copy(
            summary = "summary",
            isbn = "9781617290459",
          )

        bookMetadataRepository.update(updated)
      }

      // language=JSON
      val jsonString =
        """
        {
          "summary":"",
          "isbn":""
        }
        """.trimIndent()

      mockMvc
        .patch("/api/v1/books/$bookId/metadata") {
          contentType = MediaType.APPLICATION_JSON
          content = jsonString
        }.andExpect {
          status { isNoContent() }
        }

      val updatedMetadata = bookMetadataRepository.findById(bookId)
      with(updatedMetadata) {
        assertThat(summary).isBlank
        assertThat(isbn).isBlank
      }
    }

    @Test
    @WithMockCustomUser(roles = ["ADMIN"])
    fun `given json with null fields when updating metadata then fields with null are unset`() {
      val testDate = LocalDate.of(2020, 1, 1)

      makeSeries(name = "series", libraryId = library.id).let { series ->
        seriesLifecycle.createSeries(series).also { created ->
          val books = listOf(makeBook("1.cbr", libraryId = library.id))
          seriesLifecycle.addBooks(created, books)
        }
      }

      val bookId = bookRepository.findAll().first().id
      bookMetadataRepository.findById(bookId).let { metadata ->
        val updated =
          metadata.copy(
            authors = metadata.authors.toMutableList().also { it.add(Author("Author", "role")) },
            releaseDate = testDate,
            tags = setOf("tag"),
            summary = "summary",
            isbn = "9781617290459",
          )

        bookMetadataRepository.update(updated)
      }

      val metadata = bookMetadataRepository.findById(bookId)
      with(metadata) {
        assertThat(authors).hasSize(1)
        assertThat(releaseDate).isEqualTo(testDate)
      }

      // language=JSON
      val jsonString =
        """
        {
          "authors":null,
          "releaseDate":null,
          "tags":null,
          "summary":null,
          "isbn":null
        }
        """.trimIndent()

      mockMvc
        .patch("/api/v1/books/$bookId/metadata") {
          contentType = MediaType.APPLICATION_JSON
          content = jsonString
        }.andExpect {
          status { isNoContent() }
        }

      val updatedMetadata = bookMetadataRepository.findById(bookId)
      with(updatedMetadata) {
        assertThat(authors).isEmpty()
        assertThat(releaseDate).isNull()
        assertThat(tags).isEmpty()
        assertThat(summary).isBlank
        assertThat(isbn).isBlank
      }
    }

    @Test
    @WithMockCustomUser(roles = ["ADMIN"])
    fun `given json without fields when updating metadata then existing fields are untouched`() {
      val testDate = LocalDate.of(2020, 1, 1)

      makeSeries(name = "series", libraryId = library.id).let { series ->
        seriesLifecycle.createSeries(series).also { created ->
          val books = listOf(makeBook("1.cbr", libraryId = library.id))
          seriesLifecycle.addBooks(created, books)
        }
      }

      val bookId = bookRepository.findAll().first().id
      bookMetadataRepository.findById(bookId).let { metadata ->
        val updated =
          metadata.copy(
            authors = metadata.authors.toMutableList().also { it.add(Author("Author", "role")) },
            releaseDate = testDate,
            summary = "summary",
            number = "number",
            numberLock = true,
            numberSort = 2F,
            numberSortLock = true,
            title = "title",
            isbn = "9781617290459",
          )

        bookMetadataRepository.update(updated)
      }

      // language=JSON
      val jsonString =
        """
        {
        }
        """.trimIndent()

      mockMvc
        .patch("/api/v1/books/$bookId/metadata") {
          contentType = MediaType.APPLICATION_JSON
          content = jsonString
        }.andExpect {
          status { isNoContent() }
        }

      val metadata = bookMetadataRepository.findById(bookId)
      with(metadata) {
        assertThat(authors).hasSize(1)
        assertThat(releaseDate).isEqualTo(testDate)
        assertThat(summary).isEqualTo("summary")
        assertThat(number).isEqualTo("number")
        assertThat(numberSort).isEqualTo(2F)
        assertThat(title).isEqualTo("title")
        assertThat(isbn).isEqualTo("9781617290459")
      }
    }
  }

  @Nested
  inner class ReadProgress {
    @ParameterizedTest
    @ValueSource(
      strings = [
        """{"completed": false}""",
        """{}""",
        """{"page":0}""",
      ],
    )
    @WithMockCustomUser
    fun `given invalid payload when marking book in progress then validation error is returned`(jsonString: String) {
      mockMvc
        .patch("/api/v1/books/1/read-progress") {
          contentType = MediaType.APPLICATION_JSON
          content = jsonString
        }.andExpect {
          status { isBadRequest() }
        }
    }

    @Test
    @WithMockCustomUser(id = "1")
    fun `given user when marking book in progress with page read then progress is marked accordingly`() {
      makeSeries(name = "series", libraryId = library.id).let { series ->
        seriesLifecycle.createSeries(series).also { created ->
          val books = listOf(makeBook("1.cbr", libraryId = library.id))
          seriesLifecycle.addBooks(created, books)
        }
      }

      val book = bookRepository.findAll().first()
      mediaRepository.findById(book.id).let { media ->
        mediaRepository.update(
          media.copy(
            status = Media.Status.READY,
            pages = (1..10).map { BookPage("$it", "image/jpeg") },
            pageCount = 10,
          ),
        )
      }

      // language=JSON
      val jsonString =
        """
        {
          "page": 5
        }
        """.trimIndent()

      mockMvc
        .patch("/api/v1/books/${book.id}/read-progress") {
          contentType = MediaType.APPLICATION_JSON
          content = jsonString
        }.andExpect {
          status { isNoContent() }
        }

      mockMvc
        .get("/api/v1/books/${book.id}")
        .andExpect {
          status { isOk() }
          jsonPath("$.readProgress.page") { value(5) }
          jsonPath("$.readProgress.completed") { value(false) }
        }
    }

    @Test
    @WithMockCustomUser(id = "1")
    fun `given user when marking book completed then progress is marked accordingly`() {
      makeSeries(name = "series", libraryId = library.id).let { series ->
        seriesLifecycle.createSeries(series).also { created ->
          val books = listOf(makeBook("1.cbr", libraryId = library.id))
          seriesLifecycle.addBooks(created, books)
        }
      }

      val book = bookRepository.findAll().first()
      mediaRepository.findById(book.id).let { media ->
        mediaRepository.update(
          media.copy(
            status = Media.Status.READY,
            pages = (1..10).map { BookPage("$it", "image/jpeg") },
            pageCount = 10,
          ),
        )
      }

      // language=JSON
      val jsonString =
        """
        {
          "completed": true
        }
        """.trimIndent()

      mockMvc
        .patch("/api/v1/books/${book.id}/read-progress") {
          contentType = MediaType.APPLICATION_JSON
          content = jsonString
        }.andExpect {
          status { isNoContent() }
        }

      mockMvc
        .get("/api/v1/books/${book.id}")
        .andExpect {
          status { isOk() }
          jsonPath("$.readProgress.page") { value(10) }
          jsonPath("$.readProgress.completed") { value(true) }
        }
    }

    @Test
    @WithMockCustomUser(id = "1")
    fun `given user when deleting read progress then progress is removed`() {
      makeSeries(name = "series", libraryId = library.id).let { series ->
        seriesLifecycle.createSeries(series).also { created ->
          val books = listOf(makeBook("1.cbr", libraryId = library.id))
          seriesLifecycle.addBooks(created, books)
        }
      }

      val book = bookRepository.findAll().first()
      mediaRepository.findById(book.id).let { media ->
        mediaRepository.update(
          media.copy(
            status = Media.Status.READY,
            pages = (1..10).map { BookPage("$it", "image/jpeg") },
            pageCount = 10,
          ),
        )
      }

      // language=JSON
      val jsonString =
        """
        {
          "page": 5,
          "completed": false
        }
        """.trimIndent()

      mockMvc
        .patch("/api/v1/books/${book.id}/read-progress") {
          contentType = MediaType.APPLICATION_JSON
          content = jsonString
        }.andExpect {
          status { isNoContent() }
        }

      mockMvc
        .delete("/api/v1/books/${book.id}/read-progress") {
          contentType = MediaType.APPLICATION_JSON
        }.andExpect {
          status { isNoContent() }
        }

      mockMvc
        .get("/api/v1/books/${book.id}")
        .andExpect {
          status { isOk() }
          jsonPath("$.readProgress") { value(IsNull.nullValue()) }
        }
    }
  }

  @Test
  fun `given a user with read progress when getting books for the other user then books are returned correctly`() {
    makeSeries(name = "series", libraryId = library.id).let { series ->
      seriesLifecycle.createSeries(series).also { created ->
        val books = listOf(makeBook("1.cbr", libraryId = library.id), makeBook("2.cbr", libraryId = library.id))
        seriesLifecycle.addBooks(created, books)
      }
    }

    val book = bookRepository.findAll().first()
    mediaRepository.findById(book.id).let { media ->
      mediaRepository.update(
        media.copy(
          status = Media.Status.READY,
          pages = (1..10).map { BookPage("$it", "image/jpeg") },
        ),
      )
    }

    // language=JSON
    val jsonString =
      """
      {
        "completed": true
      }
      """.trimIndent()

    mockMvc.perform(
      MockMvcRequestBuilders
        .patch("/api/v1/books/${book.id}/read-progress")
        .with(user(KomgaPrincipal(user)))
        .contentType(MediaType.APPLICATION_JSON)
        .content(jsonString),
    )

    mockMvc
      .perform(
        MockMvcRequestBuilders
          .get("/api/v1/books")
          .with(user(KomgaPrincipal(user)))
          .contentType(MediaType.APPLICATION_JSON),
      ).andExpect(
        jsonPath("$.totalElements").value(2),
      )

    mockMvc
      .perform(
        MockMvcRequestBuilders
          .get("/api/v1/books")
          .with(user(KomgaPrincipal(user2)))
          .contentType(MediaType.APPLICATION_JSON),
      ).andExpect(
        jsonPath("$.totalElements").value(2),
      )
  }

  @Test
  @WithMockCustomUser
  fun `given book with Unicode name when getting book file then attachment name is correct`() {
    val bookName = "アキラ"
    val tempFile =
      Files
        .createTempFile(bookName, ".cbz")
        .also { it.toFile().deleteOnExit() }
    makeSeries(name = "series", libraryId = library.id).let { series ->
      seriesLifecycle.createSeries(series).let { created ->
        val books = listOf(makeBook(bookName, libraryId = library.id, url = tempFile.toUri().toURL()))
        seriesLifecycle.addBooks(created, books)
      }
    }

    val book = bookRepository.findAll().first()

    mockMvc
      .get("/api/v1/books/${book.id}/file")
      .andExpect {
        status { isOk() }
        header { string("Content-Disposition", containsString(URLEncoder.encode(bookName, StandardCharsets.UTF_8.name()))) }
      }
  }
}
