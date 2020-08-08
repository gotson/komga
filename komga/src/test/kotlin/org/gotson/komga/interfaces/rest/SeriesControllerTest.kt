package org.gotson.komga.interfaces.rest

import org.assertj.core.api.Assertions.assertThat
import org.gotson.komga.domain.model.BookPage
import org.gotson.komga.domain.model.KomgaUser
import org.gotson.komga.domain.model.Media
import org.gotson.komga.domain.model.ROLE_ADMIN
import org.gotson.komga.domain.model.SeriesMetadata
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
import org.gotson.komga.domain.service.KomgaUserLifecycle
import org.gotson.komga.domain.service.LibraryLifecycle
import org.gotson.komga.domain.service.SeriesLifecycle
import org.hamcrest.Matchers
import org.hamcrest.core.IsNull
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.MockMvcResultMatchersDsl
import org.springframework.test.web.servlet.delete
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.patch
import org.springframework.test.web.servlet.post
import kotlin.random.Random

@ExtendWith(SpringExtension::class)
@SpringBootTest
@AutoConfigureMockMvc(printOnlyOnFailure = false)
class SeriesControllerTest(
  @Autowired private val seriesRepository: SeriesRepository,
  @Autowired private val seriesLifecycle: SeriesLifecycle,
  @Autowired private val seriesMetadataRepository: SeriesMetadataRepository,
  @Autowired private val libraryRepository: LibraryRepository,
  @Autowired private val libraryLifecycle: LibraryLifecycle,
  @Autowired private val bookRepository: BookRepository,
  @Autowired private val mediaRepository: MediaRepository,
  @Autowired private val bookMetadataRepository: BookMetadataRepository,
  @Autowired private val userRepository: KomgaUserRepository,
  @Autowired private val userLifecycle: KomgaUserLifecycle,
  @Autowired private val mockMvc: MockMvc
) {

  private val library = makeLibrary(id = "1")

  @BeforeAll
  fun `setup library`() {
    libraryRepository.insert(library)
    userRepository.insert(KomgaUser("user@example.org", "", false, id = "1"))
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
  inner class SeriesSort {
    @Test
    @WithMockCustomUser
    fun `given series with titleSort when requesting via api then series are sorted by titleSort`() {
      val alphaC = seriesLifecycle.createSeries(makeSeries("TheAlpha", libraryId = library.id))
      seriesMetadataRepository.findById(alphaC.id).let {
        seriesMetadataRepository.update(it.copy(titleSort = "Alpha, The"))
      }
      seriesLifecycle.createSeries(makeSeries("Beta", libraryId = library.id))

      mockMvc.get("/api/v1/series")
        .andExpect {
          status { isOk }
          jsonPath("$.content[0].metadata.title") { value("TheAlpha") }
          jsonPath("$.content[1].metadata.title") { value("Beta") }
        }
    }

    @Test
    @WithMockCustomUser
    fun `given series when requesting via api then series are sorted insensitive of case`() {
      listOf("a", "b", "B", "C")
        .map { name -> makeSeries(name, libraryId = library.id) }
        .forEach {
          seriesLifecycle.createSeries(it)
        }

      mockMvc.get("/api/v1/series") {
        param("sort", "metadata.titleSort,asc")
      }
        .andExpect {
          status { isOk }
          jsonPath("$.content[0].metadata.title") { value("a") }
          jsonPath("$.content[1].metadata.title") { value(Matchers.equalToIgnoringCase("b")) }
          jsonPath("$.content[2].metadata.title") { value(Matchers.equalToIgnoringCase("b")) }
          jsonPath("$.content[3].metadata.title") { value("C") }
        }
    }
  }

  @Nested
  inner class BookOrdering {
    @Test
    @WithMockCustomUser
    fun `given books with unordered index when requesting via api then books are ordered`() {
      val createdSeries = makeSeries(name = "series", libraryId = library.id).let { series ->
        seriesLifecycle.createSeries(series).also { created ->
          val books = listOf(makeBook("1", libraryId = library.id), makeBook("3", libraryId = library.id))
          seriesLifecycle.addBooks(created, books)
        }
      }

      val addedBook = makeBook("2", libraryId = library.id)
      seriesLifecycle.addBooks(createdSeries, listOf(addedBook))
      seriesLifecycle.sortBooks(createdSeries)

      mockMvc.get("/api/v1/series/${createdSeries.id}/books")
        .andExpect {
          status { isOk }
          jsonPath("$.content[0].name") { value("1") }
          jsonPath("$.content[1].name") { value("2") }
          jsonPath("$.content[2].name") { value("3") }
        }
    }

    @Test
    @WithMockCustomUser
    fun `given many books with unordered index when requesting via api then books are ordered and paged`() {
      val createdSeries = makeSeries(name = "series", libraryId = library.id).let { series ->
        seriesLifecycle.createSeries(series).also { created ->
          val books = (1..100 step 2).map { makeBook("$it", libraryId = library.id) }
          seriesLifecycle.addBooks(created, books)
        }
      }

      val addedBook = makeBook("2", libraryId = library.id)
      seriesLifecycle.addBooks(createdSeries, listOf(addedBook))
      seriesLifecycle.sortBooks(createdSeries)

      mockMvc.get("/api/v1/series/${createdSeries.id}/books")
        .andExpect {
          status { isOk }
          jsonPath("$.content[0].name") { value("1") }
          jsonPath("$.content[1].name") { value("2") }
          jsonPath("$.content[2].name") { value("3") }
          jsonPath("$.content[3].name") { value("5") }
          jsonPath("$.size") { value(20) }
          jsonPath("$.first") { value(true) }
          jsonPath("$.number") { value(0) }
        }
    }
  }

  @Nested
  inner class LimitedUser {
    @Test
    @WithMockCustomUser(sharedAllLibraries = false, sharedLibraries = ["1"])
    fun `given user with access to a single library when getting series then only gets series from this library`() {
      makeSeries(name = "series", libraryId = library.id).let { series ->
        seriesLifecycle.createSeries(series).also { created ->
          val books = listOf(makeBook("1", libraryId = library.id))
          seriesLifecycle.addBooks(created, books)
        }
      }

      val otherLibrary = makeLibrary("other")
      libraryRepository.insert(otherLibrary)
      makeSeries(name = "otherSeries", libraryId = otherLibrary.id).let { series ->
        seriesLifecycle.createSeries(series).also { created ->
          val books = listOf(makeBook("2", libraryId = otherLibrary.id))
          seriesLifecycle.addBooks(created, books)
        }
      }

      mockMvc.get("/api/v1/series")
        .andExpect {
          status { isOk }
          jsonPath("$.content.length()") { value(1) }
          jsonPath("$.content[0].name") { value("series") }
        }
    }
  }

  @Nested
  inner class UserWithoutLibraryAccess {
    @Test
    @WithMockCustomUser(sharedAllLibraries = false, sharedLibraries = [])
    fun `given user with no access to any library when getting specific series then returns forbidden`() {
      val createdSeries = makeSeries(name = "series", libraryId = library.id).let { series ->
        seriesLifecycle.createSeries(series).also { created ->
          val books = listOf(makeBook("1", libraryId = library.id))
          seriesLifecycle.addBooks(created, books)
        }
      }

      mockMvc.get("/api/v1/series/${createdSeries.id}")
        .andExpect { status { isForbidden } }
    }

    @Test
    @WithMockCustomUser(sharedAllLibraries = false, sharedLibraries = [])
    fun `given user with no access to any library when getting specific series thumbnail then returns forbidden`() {
      val createdSeries = makeSeries(name = "series", libraryId = library.id).let { series ->
        seriesLifecycle.createSeries(series).also { created ->
          val books = listOf(makeBook("1", libraryId = library.id))
          seriesLifecycle.addBooks(created, books)
        }
      }

      mockMvc.get("/api/v1/series/${createdSeries.id}/thumbnail")
        .andExpect { status { isForbidden } }
    }

    @Test
    @WithMockCustomUser(sharedAllLibraries = false, sharedLibraries = [])
    fun `given user with no access to any library when getting specific series books then returns forbidden`() {
      val createdSeries = makeSeries(name = "series", libraryId = library.id).let { series ->
        seriesLifecycle.createSeries(series).also { created ->
          val books = listOf(makeBook("1", libraryId = library.id))
          seriesLifecycle.addBooks(created, books)
        }
      }

      mockMvc.get("/api/v1/series/${createdSeries.id}/books")
        .andExpect { status { isForbidden } }
    }
  }

  @Nested
  inner class MediaNotReady {
    @Test
    @WithMockCustomUser
    fun `given book without thumbnail when getting series thumbnail then returns not found`() {
      val createdSeries = makeSeries(name = "series", libraryId = library.id).let { series ->
        seriesLifecycle.createSeries(series).also { created ->
          val books = listOf(makeBook("1", libraryId = library.id))
          seriesLifecycle.addBooks(created, books)
        }
      }

      mockMvc.get("/api/v1/series/${createdSeries.id}/thumbnail")
        .andExpect { status { isNotFound } }
    }
  }

  @Nested
  inner class DtoUrlSanitization {
    @Test
    @WithMockCustomUser
    fun `given regular user when getting series then url is hidden`() {
      val createdSeries = makeSeries(name = "series", libraryId = library.id).let { series ->
        seriesLifecycle.createSeries(series).also { created ->
          val books = listOf(makeBook("1", libraryId = library.id))
          seriesLifecycle.addBooks(created, books)
        }
      }

      val validation: MockMvcResultMatchersDsl.() -> Unit = {
        status { isOk }
        jsonPath("$.content[0].url") { value("") }
      }

      mockMvc.get("/api/v1/series")
        .andExpect(validation)

      mockMvc.get("/api/v1/series/latest")
        .andExpect(validation)

      mockMvc.get("/api/v1/series/new")
        .andExpect(validation)

      mockMvc.get("/api/v1/series/${createdSeries.id}")
        .andExpect {
          status { isOk }
          jsonPath("$.url") { value("") }
        }
    }

    @Test
    @WithMockCustomUser(roles = [ROLE_ADMIN])
    fun `given admin user when getting series then url is available`() {
      val createdSeries = makeSeries(name = "series", libraryId = library.id).let { series ->
        seriesLifecycle.createSeries(series).also { created ->
          val books = listOf(makeBook("1", libraryId = library.id))
          seriesLifecycle.addBooks(created, books)
        }
      }

      val validation: MockMvcResultMatchersDsl.() -> Unit = {
        status { isOk }
        jsonPath("$.content[0].url") { value(Matchers.containsString("series")) }
      }

      mockMvc.get("/api/v1/series")
        .andExpect(validation)

      mockMvc.get("/api/v1/series/latest")
        .andExpect(validation)

      mockMvc.get("/api/v1/series/new")
        .andExpect(validation)

      mockMvc.get("/api/v1/series/${createdSeries.id}")
        .andExpect {
          status { isOk }
          jsonPath("$.url") { value(Matchers.containsString("series")) }
        }
    }
  }

  @Nested
  inner class MetadataUpdate {
    @Test
    @WithMockCustomUser
    fun `given non-admin user when updating metadata then raise forbidden`() {
      mockMvc.patch("/api/v1/series/1/metadata") {
        contentType = MediaType.APPLICATION_JSON
        content = "{}"
      }.andExpect {
        status { isForbidden }
      }
    }

    @ParameterizedTest
    @ValueSource(strings = [
      """{"title":""}""",
      """{"titleSort":""}"""
    ])
    @WithMockCustomUser(roles = [ROLE_ADMIN])
    fun `given invalid json when updating metadata then raise validation error`(jsonString: String) {
      mockMvc.patch("/api/v1/series/1/metadata") {
        contentType = MediaType.APPLICATION_JSON
        content = jsonString
      }.andExpect {
        status { isBadRequest }
      }
    }

    @Test
    @WithMockCustomUser(roles = [ROLE_ADMIN])
    fun `given valid json when updating metadata then fields are updated`() {
      val createdSeries = makeSeries(name = "series", libraryId = library.id).let { series ->
        seriesLifecycle.createSeries(series).also { created ->
          val books = listOf(makeBook("1", libraryId = library.id))
          seriesLifecycle.addBooks(created, books)
        }
      }

      val jsonString = """
        {
          "title":"newTitle",
          "titleSort":"newTitleSort",
          "status":"HIATUS",
          "titleLock":true,
          "titleSortLock":true,
          "statusLock":true
        }
      """.trimIndent()

      mockMvc.patch("/api/v1/series/${createdSeries.id}/metadata") {
        contentType = MediaType.APPLICATION_JSON
        content = jsonString
      }.andExpect {
        status { isNoContent }
      }

      val updatedMetadata = seriesMetadataRepository.findById(createdSeries.id)
      with(updatedMetadata) {
        assertThat(title).isEqualTo("newTitle")
        assertThat(titleSort).isEqualTo("newTitleSort")
        assertThat(status).isEqualTo(SeriesMetadata.Status.HIATUS)
        assertThat(titleLock).isEqualTo(true)
        assertThat(titleSortLock).isEqualTo(true)
        assertThat(statusLock).isEqualTo(true)
      }
    }
  }

  @Nested
  inner class HttpCache {
    @Test
    @WithMockCustomUser
    fun `given request with cache headers when getting series thumbnail then returns 304 not modified`() {
      val createdSeries = makeSeries(name = "series", libraryId = library.id).let { series ->
        seriesLifecycle.createSeries(series).also { created ->
          val books = listOf(makeBook("1", libraryId = library.id))
          seriesLifecycle.addBooks(created, books)
        }
      }

      bookRepository.findAll().first().let { book ->
        mediaRepository.findById(book.id).let {
          mediaRepository.update(it.copy(thumbnail = Random.nextBytes(1)))
        }
      }

      val url = "/api/v1/series/${createdSeries.id}/thumbnail"

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
    fun `given request with cache headers and modified first book when getting series thumbnail then returns 200 ok`() {
      val createdSeries = makeSeries(name = "series", libraryId = library.id).let { series ->
        seriesLifecycle.createSeries(series).also { created ->
          val books = listOf(makeBook("1", libraryId = library.id), makeBook("2", libraryId = library.id))
          seriesLifecycle.addBooks(created, books)
        }
      }

      bookRepository.findAll().forEach { book ->
        mediaRepository.findById(book.id).let {
          mediaRepository.update(it.copy(thumbnail = Random.nextBytes(1)))
        }
      }

      val url = "/api/v1/series/${createdSeries.id}/thumbnail"

      val response = mockMvc.get(url).andReturn().response

      bookRepository.findAll().first { it.name == "1" }.let { book ->
        bookMetadataRepository.findById(book.id).let {
          bookMetadataRepository.update(it.copy(numberSort = 3F))
        }
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
  inner class ReadProgress {
    @Test
    @WithMockCustomUser(id = "1")
    fun `given user when marking series as read then progress is marked for all books`() {
      val series = makeSeries(name = "series", libraryId = library.id).let { series ->
        seriesLifecycle.createSeries(series).also { created ->
          val books = listOf(makeBook("1.cbr", libraryId = library.id), makeBook("2.cbr", libraryId = library.id))
          seriesLifecycle.addBooks(created, books)
        }
      }

      bookRepository.findAll().forEach { book ->
        mediaRepository.findById(book.id).let {
          mediaRepository.update(it.copy(
            status = Media.Status.READY,
            pages = (1..10).map { BookPage("$it", "image/jpeg") }
          ))
        }
      }

      mockMvc.post("/api/v1/series/${series.id}/read-progress")
        .andExpect {
          status { isNoContent }
        }

      mockMvc.get("/api/v1/series/${series.id}")
        .andExpect {
          status { isOk }
          jsonPath("$.booksUnreadCount") { value(0) }
          jsonPath("$.booksReadCount") { value(2) }
        }

      mockMvc.get("/api/v1/series/${series.id}/books")
        .andExpect {
          status { isOk }
          jsonPath("$.content[0].readProgress.completed") { value(true) }
          jsonPath("$.content[1].readProgress.completed") { value(true) }
          jsonPath("$.numberOfElements") { value(2) }
        }
    }

    @Test
    @WithMockCustomUser(id = "1")
    fun `given user when marking series as unread then progress is removed for all books`() {
      val series = makeSeries(name = "series", libraryId = library.id).let { series ->
        seriesLifecycle.createSeries(series).also { created ->
          val books = listOf(makeBook("1.cbr", libraryId = library.id), makeBook("2.cbr", libraryId = library.id))
          seriesLifecycle.addBooks(created, books)
        }
      }

      bookRepository.findAll().forEach { book ->
        mediaRepository.findById(book.id).let {
          mediaRepository.update(it.copy(
            status = Media.Status.READY,
            pages = (1..10).map { BookPage("$it", "image/jpeg") }
          ))
        }
      }

      mockMvc.post("/api/v1/series/${series.id}/read-progress")
        .andExpect {
          status { isNoContent }
        }

      mockMvc.delete("/api/v1/series/${series.id}/read-progress")
        .andExpect {
          status { isNoContent }
        }

      mockMvc.get("/api/v1/series/${series.id}")
        .andExpect {
          status { isOk }
          jsonPath("$.booksUnreadCount") { value(2) }
          jsonPath("$.booksReadCount") { value(0) }
        }

      mockMvc.get("/api/v1/series/${series.id}/books")
        .andExpect {
          status { isOk }
          jsonPath("$.content[0].readProgress") { value(IsNull.nullValue()) }
          jsonPath("$.content[1].readProgress") { value(IsNull.nullValue()) }
          jsonPath("$.numberOfElements") { value(2) }
        }
    }

    @Test
    @WithMockCustomUser(id = "1")
    fun `given user when marking book as in progress then progress series return books count accordingly`() {
      val series = makeSeries(name = "series", libraryId = library.id).let { series ->
        seriesLifecycle.createSeries(series).also { created ->
          val books = listOf(
            makeBook("1.cbr", libraryId = library.id),
            makeBook("2.cbr", libraryId = library.id),
            makeBook("3.cbr", libraryId = library.id)
          )
          seriesLifecycle.addBooks(created, books)
        }
      }

      bookRepository.findAll().forEach { book ->
        mediaRepository.findById(book.id).let {
          mediaRepository.update(it.copy(
            status = Media.Status.READY,
            pages = (1..10).map { BookPage("$it", "image/jpeg") }
          ))
        }
      }

      val books = bookRepository.findAll().sortedBy { it.name }

      mockMvc.patch("/api/v1/books/${books.elementAt(0).id}/read-progress") {
        contentType = MediaType.APPLICATION_JSON
        content = """{"page": 5,"completed":false}"""
      }
      mockMvc.patch("/api/v1/books/${books.elementAt(1).id}/read-progress") {
        contentType = MediaType.APPLICATION_JSON
        content = """{"completed":true}"""
      }

      mockMvc.get("/api/v1/series/${series.id}")
        .andExpect {
          status { isOk }
          jsonPath("$.booksUnreadCount") { value(1) }
          jsonPath("$.booksReadCount") { value(1) }
          jsonPath("$.booksInProgressCount") { value(1) }
        }
    }
  }
}
