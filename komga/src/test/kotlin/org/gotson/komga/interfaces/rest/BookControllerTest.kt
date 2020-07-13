package org.gotson.komga.interfaces.rest

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.groups.Tuple.tuple
import org.gotson.komga.domain.model.Author
import org.gotson.komga.domain.model.BookMetadata
import org.gotson.komga.domain.model.BookPage
import org.gotson.komga.domain.model.BookSearch
import org.gotson.komga.domain.model.KomgaUser
import org.gotson.komga.domain.model.Media
import org.gotson.komga.domain.model.ROLE_ADMIN
import org.gotson.komga.domain.model.makeBook
import org.gotson.komga.domain.model.makeLibrary
import org.gotson.komga.domain.model.makeSeries
import org.gotson.komga.domain.persistence.BookMetadataRepository
import org.gotson.komga.domain.persistence.BookRepository
import org.gotson.komga.domain.persistence.KomgaUserRepository
import org.gotson.komga.domain.persistence.LibraryRepository
import org.gotson.komga.domain.persistence.MediaRepository
import org.gotson.komga.domain.persistence.SeriesRepository
import org.gotson.komga.domain.service.KomgaUserLifecycle
import org.gotson.komga.domain.service.LibraryLifecycle
import org.gotson.komga.domain.service.SeriesLifecycle
import org.gotson.komga.infrastructure.security.KomgaPrincipal
import org.hamcrest.Matchers
import org.hamcrest.core.IsNull
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource
import org.junit.jupiter.params.provider.ValueSource
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.MockMvcResultMatchersDsl
import org.springframework.test.web.servlet.delete
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.patch
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import java.time.LocalDate
import kotlin.random.Random

@ExtendWith(SpringExtension::class)
@SpringBootTest
@AutoConfigureMockMvc(printOnlyOnFailure = false)
class BookControllerTest(
  @Autowired private val seriesRepository: SeriesRepository,
  @Autowired private val seriesLifecycle: SeriesLifecycle,
  @Autowired private val mediaRepository: MediaRepository,
  @Autowired private val bookMetadataRepository: BookMetadataRepository,
  @Autowired private val libraryRepository: LibraryRepository,
  @Autowired private val libraryLifecycle: LibraryLifecycle,
  @Autowired private val bookRepository: BookRepository,
  @Autowired private val userRepository: KomgaUserRepository,
  @Autowired private val userLifecycle: KomgaUserLifecycle,
  @Autowired private val mockMvc: MockMvc
) {

  private val library = makeLibrary(id = "1")
  private val user = KomgaUser("user@example.org", "", false, id = "1")
  private val user2 = KomgaUser("user2@example.org", "", false, id = "2")

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
    seriesLifecycle.deleteMany(seriesRepository.findAll().map { it.id })
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

      mockMvc.get("/api/v1/books")
        .andExpect {
          status { isOk }
          jsonPath("$.content.length()") { value(1) }
          jsonPath("$.content[0].name") { value("1") }
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

      mockMvc.get("/api/v1/books/${book.id}")
        .andExpect { status { isForbidden } }
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

      mockMvc.get("/api/v1/books/${book.id}/thumbnail")
        .andExpect { status { isForbidden } }
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

      mockMvc.get("/api/v1/books/${book.id}/file")
        .andExpect { status { isForbidden } }
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

      mockMvc.get("/api/v1/books/${book.id}/pages")
        .andExpect { status { isForbidden } }
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

      mockMvc.get("/api/v1/books/${book.id}/pages/1")
        .andExpect { status { isForbidden } }
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

      mockMvc.get("/api/v1/books/${book.id}/pages/1")
        .andExpect { status { isForbidden } }
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

      mockMvc.get("/api/v1/books/${book.id}/file")
        .andExpect { status { isForbidden } }
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

      mockMvc.get("/api/v1/books/${book.id}/thumbnail")
        .andExpect { status { isNotFound } }
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

      mockMvc.get("/api/v1/books/${book.id}/file")
        .andExpect { status { isNotFound } }
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

      mockMvc.get("/api/v1/books/${book.id}/pages")
        .andExpect { status { isNotFound } }
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

      mockMvc.get("/api/v1/books/${book.id}/pages/1")
        .andExpect { status { isNotFound } }
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
      mediaRepository.update(it.copy(
        status = Media.Status.READY,
        pages = listOf(BookPage("file", "image/jpeg"))
      ))
    }

    mockMvc.get("/api/v1/books/${book.id}/pages/$page")
      .andExpect { status { isBadRequest } }
  }

  @Nested
  inner class Siblings {

    @Test
    @WithMockCustomUser
    fun `given series with multiple books when getting siblings then it is returned or not found`() {
      val series = makeSeries(name = "series", libraryId = library.id).let { series ->
        seriesLifecycle.createSeries(series).also { created ->
          val books = listOf(
            makeBook("1", libraryId = library.id),
            makeBook("2", libraryId = library.id),
            makeBook("3", libraryId = library.id)
          )
          seriesLifecycle.addBooks(created, books)
          seriesLifecycle.sortBooks(created)
        }
      }

      val first = bookRepository.findFirstIdInSeries(series.id)
      val second = bookRepository.findAll(BookSearch(searchTerm = "2")).first().id
      val third = bookRepository.findAll(BookSearch(searchTerm = "3")).first().id

      mockMvc.get("/api/v1/books/${first}/previous")
        .andExpect { status { isNotFound } }
      mockMvc.get("/api/v1/books/${first}/next")
        .andExpect {
          status { isOk }
          jsonPath("$.name") { value("2") }
        }

      mockMvc.get("/api/v1/books/${second}/previous")
        .andExpect {
          status { isOk }
          jsonPath("$.name") { value("1") }
        }
      mockMvc.get("/api/v1/books/${second}/next")
        .andExpect {
          status { isOk }
          jsonPath("$.name") { value("3") }
        }

      mockMvc.get("/api/v1/books/${third}/previous")
        .andExpect {
          status { isOk }
          jsonPath("$.name") { value("2") }
        }
      mockMvc.get("/api/v1/books/${third}/next")
        .andExpect { status { isNotFound } }

    }
  }

  @Nested
  inner class DtoUrlSanitization {
    @Test
    @WithMockCustomUser
    fun `given regular user when getting books then full url is hidden`() {
      val createdSeries = makeSeries(name = "series", libraryId = library.id).let { series ->
        seriesLifecycle.createSeries(series).also { created ->
          val books = listOf(makeBook("1.cbr", libraryId = library.id))
          seriesLifecycle.addBooks(created, books)
        }
      }

      val book = bookRepository.findAll().first()

      val validation: MockMvcResultMatchersDsl.() -> Unit = {
        status { isOk }
        jsonPath("$.content[0].url") { value("1.cbr") }
      }

      mockMvc.get("/api/v1/books")
        .andExpect(validation)

      mockMvc.get("/api/v1/books/latest")
        .andExpect(validation)

      mockMvc.get("/api/v1/series/${createdSeries.id}/books")
        .andExpect(validation)

      mockMvc.get("/api/v1/books/${book.id}")
        .andExpect {
          status { isOk }
          jsonPath("$.url") { value("1.cbr") }
        }
    }

    @Test
    @WithMockCustomUser(roles = [ROLE_ADMIN])
    fun `given admin user when getting books then full url is available`() {
      val createdSeries = makeSeries(name = "series", libraryId = library.id).let { series ->
        seriesLifecycle.createSeries(series).also { created ->
          val books = listOf(makeBook("1.cbr", libraryId = library.id))
          seriesLifecycle.addBooks(created, books)
        }
      }

      val book = bookRepository.findAll().first()

      val validation: MockMvcResultMatchersDsl.() -> Unit = {
        status { isOk }
        jsonPath("$.content[0].url") { value(Matchers.containsString("1.cbr")) }
      }

      mockMvc.get("/api/v1/books")
        .andExpect(validation)

      mockMvc.get("/api/v1/books/latest")
        .andExpect(validation)

      mockMvc.get("/api/v1/series/${createdSeries.id}/books")
        .andExpect(validation)

      mockMvc.get("/api/v1/books/${book.id}")
        .andExpect {
          status { isOk }
          jsonPath("$.url") { value(Matchers.containsString("1.cbr")) }
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
      mediaRepository.findById(book.id).let {
        mediaRepository.update(it.copy(thumbnail = Random.nextBytes(100)))
      }


      val url = "/api/v1/books/${book.id}/thumbnail"

      val response = mockMvc.get(url)
        .andReturn().response

      mockMvc.get(url) {
        headers {
          ifNoneMatch = listOf(response.getHeader(HttpHeaders.ETAG)!!)
        }
      }.andExpect {
        status { isNotModified }
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

      val lastModified = mockMvc.get(url)
        .andReturn().response.getHeader(HttpHeaders.LAST_MODIFIED)

      mockMvc.get(url) {
        headers {
          set(HttpHeaders.IF_MODIFIED_SINCE, lastModified!!)
        }
      }.andExpect {
        status { isNotModified }
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
      mediaRepository.findById(book.id).let {
        mediaRepository.update(it.copy(thumbnail = Random.nextBytes(1)))
      }

      val url = "/api/v1/books/${book.id}/thumbnail"

      val response = mockMvc.get(url).andReturn().response

      Thread.sleep(100)
      mediaRepository.findById(book.id).let {
        mediaRepository.update(it.copy(thumbnail = Random.nextBytes(1)))
      }

      mockMvc.get(url) {
        headers {
          ifNoneMatch = listOf(response.getHeader(HttpHeaders.ETAG)!!)
        }
      }.andExpect {
        status { isOk }
      }
    }
  }

  @Nested
  inner class MetadataUpdate {
    @Test
    @WithMockCustomUser
    fun `given non-admin user when updating metadata then raise forbidden`() {
      mockMvc.patch("/api/v1/books/1/metadata") {
        contentType = MediaType.APPLICATION_JSON
        content = "{}"
      }.andExpect {
        status { isForbidden }
      }
    }

    @ParameterizedTest
    @ValueSource(strings = [
      """{"title":""}""",
      """{"number":""}""",
      """{"authors":"[{"name":""}]"}""",
      """{"ageRating":-1}"""
    ])
    @WithMockCustomUser(roles = [ROLE_ADMIN])
    fun `given invalid json when updating metadata then raise validation error`(jsonString: String) {
      mockMvc.patch("/api/v1/books/1/metadata") {
        contentType = MediaType.APPLICATION_JSON
        content = jsonString
      }.andExpect {
        status { isBadRequest }
      }
    }

    @Test
    @WithMockCustomUser(roles = [ROLE_ADMIN])
    fun `given valid json when updating metadata then fields are updated`() {
      makeSeries(name = "series", libraryId = library.id).let { series ->
        seriesLifecycle.createSeries(series).also { created ->
          val books = listOf(makeBook("1.cbr", libraryId = library.id))
          seriesLifecycle.addBooks(created, books)
        }
      }

      val bookId = bookRepository.findAll().first().id

      val jsonString = """
        {
          "title":"newTitle",
          "titleLock":true,
          "summary":"newSummary",
          "summaryLock":true,
          "number":"newNumber",
          "numberLock":true,
          "numberSort": 1.0,
          "numberSortLock":true,
          "readingDirection":"LEFT_TO_RIGHT",
          "readingDirectionLock":true,
          "publisher":"newPublisher",
          "publisherLock":true,
          "ageRating":12,
          "ageRatingLock":true,
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
          "authorsLock":true
        }
      """.trimIndent()

      mockMvc.patch("/api/v1/books/${bookId}/metadata") {
        contentType = MediaType.APPLICATION_JSON
        content = jsonString
      }.andExpect {
        status { isNoContent }
      }

      val metadata = bookMetadataRepository.findById(bookId)
      with(metadata) {
        assertThat(title).isEqualTo("newTitle")
        assertThat(summary).isEqualTo("newSummary")
        assertThat(number).isEqualTo("newNumber")
        assertThat(numberSort).isEqualTo(1F)
        assertThat(readingDirection).isEqualTo(BookMetadata.ReadingDirection.LEFT_TO_RIGHT)
        assertThat(publisher).isEqualTo("newPublisher")
        assertThat(ageRating).isEqualTo(12)
        assertThat(releaseDate).isEqualTo(LocalDate.of(2020, 1, 1))
        assertThat(authors)
          .hasSize(2)
          .extracting("name", "role")
          .containsExactlyInAnyOrder(
            tuple("newAuthor", "newauthorrole"),
            tuple("newAuthor2", "newauthorrole2")
          )

        assertThat(titleLock).isEqualTo(true)
        assertThat(summaryLock).isEqualTo(true)
        assertThat(numberLock).isEqualTo(true)
        assertThat(numberSortLock).isEqualTo(true)
        assertThat(readingDirectionLock).isEqualTo(true)
        assertThat(publisherLock).isEqualTo(true)
        assertThat(ageRatingLock).isEqualTo(true)
        assertThat(releaseDateLock).isEqualTo(true)
        assertThat(authorsLock).isEqualTo(true)
      }
    }

    @Test
    @WithMockCustomUser(roles = [ROLE_ADMIN])
    fun `given json with null fields when updating metadata then fields with null are unset`() {
      val testDate = LocalDate.of(2020, 1, 1)

      makeSeries(name = "series", libraryId = library.id).let { series ->
        seriesLifecycle.createSeries(series).also { created ->
          val books = listOf(makeBook("1.cbr", libraryId = library.id))
          seriesLifecycle.addBooks(created, books)
        }
      }

      val bookId = bookRepository.findAll().first().id
      bookMetadataRepository.findById(bookId).let {
        val updated = it.copy(
          ageRating = 12,
          readingDirection = BookMetadata.ReadingDirection.LEFT_TO_RIGHT,
          authors = it.authors.toMutableList().also { it.add(Author("Author", "role")) },
          releaseDate = testDate
        )

        bookMetadataRepository.update(updated)
      }

      val metadata = bookMetadataRepository.findById(bookId)
      with(metadata) {
        assertThat(readingDirection).isEqualTo(BookMetadata.ReadingDirection.LEFT_TO_RIGHT)
        assertThat(ageRating).isEqualTo(12)
        assertThat(authors).hasSize(1)
        assertThat(releaseDate).isEqualTo(testDate)
      }

      val jsonString = """
        {
          "readingDirection":null,
          "ageRating":null,
          "authors":null,
          "releaseDate":null
        }
      """.trimIndent()

      mockMvc.patch("/api/v1/books/${bookId}/metadata") {
        contentType = MediaType.APPLICATION_JSON
        content = jsonString
      }.andExpect {
        status { isNoContent }
      }

      val updatedMetadata = bookMetadataRepository.findById(bookId)
      with(updatedMetadata) {
        assertThat(readingDirection).isNull()
        assertThat(ageRating).isNull()
        assertThat(authors).isEmpty()
        assertThat(releaseDate).isNull()
      }
    }

    @Test
    @WithMockCustomUser(roles = [ROLE_ADMIN])
    fun `given json without fields when updating metadata then existing fields are untouched`() {
      val testDate = LocalDate.of(2020, 1, 1)

      makeSeries(name = "series", libraryId = library.id).let { series ->
        seriesLifecycle.createSeries(series).also { created ->
          val books = listOf(makeBook("1.cbr", libraryId = library.id))
          seriesLifecycle.addBooks(created, books)
        }
      }

      val bookId = bookRepository.findAll().first().id
      bookMetadataRepository.findById(bookId).let {
        val updated = it.copy(
          ageRating = 12,
          readingDirection = BookMetadata.ReadingDirection.LEFT_TO_RIGHT,
          authors = it.authors.toMutableList().also { it.add(Author("Author", "role")) },
          releaseDate = testDate,
          summary = "summary",
          number = "number",
          numberLock = true,
          numberSort = 2F,
          numberSortLock = true,
          publisher = "publisher",
          title = "title"
        )

        bookMetadataRepository.update(updated)
      }

      val jsonString = """
        {
        }
      """.trimIndent()

      mockMvc.patch("/api/v1/books/${bookId}/metadata") {
        contentType = MediaType.APPLICATION_JSON
        content = jsonString
      }.andExpect {
        status { isNoContent }
      }

      val metadata = bookMetadataRepository.findById(bookId)
      with(metadata) {
        assertThat(readingDirection).isEqualTo(BookMetadata.ReadingDirection.LEFT_TO_RIGHT)
        assertThat(ageRating).isEqualTo(12)
        assertThat(authors).hasSize(1)
        assertThat(releaseDate).isEqualTo(testDate)
        assertThat(summary).isEqualTo("summary")
        assertThat(number).isEqualTo("number")
        assertThat(numberSort).isEqualTo(2F)
        assertThat(publisher).isEqualTo("publisher")
        assertThat(title).isEqualTo("title")
      }
    }
  }

  @Nested
  inner class ReadProgress {

    @ParameterizedTest
    @ValueSource(strings = [
      """{"completed": false}""",
      """{}""",
      """{"page":0}"""
    ])
    @WithMockCustomUser
    fun `given invalid payload when marking book in progress then validation error is returned`(jsonString: String) {
      mockMvc.patch("/api/v1/books/1/read-progress") {
        contentType = MediaType.APPLICATION_JSON
        content = jsonString
      }.andExpect {
        status { isBadRequest }
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
      mediaRepository.findById(book.id).let {
        mediaRepository.update(it.copy(
          status = Media.Status.READY,
          pages = (1..10).map { BookPage("$it", "image/jpeg") }
        ))
      }

      val jsonString = """
        {
          "page": 5
        }
      """.trimIndent()

      mockMvc.patch("/api/v1/books/${book.id}/read-progress") {
        contentType = MediaType.APPLICATION_JSON
        content = jsonString
      }.andExpect {
        status { isNoContent }
      }

      mockMvc.get("/api/v1/books/${book.id}")
        .andExpect {
          status { isOk }
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
      mediaRepository.findById(book.id).let {
        mediaRepository.update(it.copy(
          status = Media.Status.READY,
          pages = (1..10).map { BookPage("$it", "image/jpeg") }
        ))
      }

      val jsonString = """
        {
          "completed": true
        }
      """.trimIndent()

      mockMvc.patch("/api/v1/books/${book.id}/read-progress") {
        contentType = MediaType.APPLICATION_JSON
        content = jsonString
      }.andExpect {
        status { isNoContent }
      }

      mockMvc.get("/api/v1/books/${book.id}")
        .andExpect {
          status { isOk }
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
        mediaRepository.update(media.copy(
          status = Media.Status.READY,
          pages = (1..10).map { BookPage("$it", "image/jpeg") }
        ))
      }

      val jsonString = """
        {
          "page": 5,
          "completed": false
        }
      """.trimIndent()

      mockMvc.patch("/api/v1/books/${book.id}/read-progress") {
        contentType = MediaType.APPLICATION_JSON
        content = jsonString
      }.andExpect {
        status { isNoContent }
      }


      mockMvc.delete("/api/v1/books/${book.id}/read-progress") {
        contentType = MediaType.APPLICATION_JSON
      }.andExpect {
        status { isNoContent }
      }

      mockMvc.get("/api/v1/books/${book.id}")
        .andExpect {
          status { isOk }
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
    mediaRepository.findById(book.id).let {
      mediaRepository.update(it.copy(
        status = Media.Status.READY,
        pages = (1..10).map { BookPage("$it", "image/jpeg") }
      ))
    }

    val jsonString = """
        {
          "completed": true
        }
      """.trimIndent()

    mockMvc.perform(MockMvcRequestBuilders
      .patch("/api/v1/books/${book.id}/read-progress")
      .with(user(KomgaPrincipal(user)))
      .contentType(MediaType.APPLICATION_JSON)
      .content(jsonString)
    )

    mockMvc.perform(MockMvcRequestBuilders
      .get("/api/v1/books")
      .with(user(KomgaPrincipal(user)))
      .contentType(MediaType.APPLICATION_JSON)
    ).andExpect(
      jsonPath("$.totalElements").value(2)
    )

    mockMvc.perform(MockMvcRequestBuilders
      .get("/api/v1/books")
      .with(user(KomgaPrincipal(user2)))
      .contentType(MediaType.APPLICATION_JSON)
    ).andExpect(
      jsonPath("$.totalElements").value(2)
    )
  }
}
