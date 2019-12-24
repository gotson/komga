package org.gotson.komga.interfaces.web.rest

import org.gotson.komga.domain.model.BookMetadata
import org.gotson.komga.domain.model.BookPage
import org.gotson.komga.domain.model.UserRoles
import org.gotson.komga.domain.model.makeBook
import org.gotson.komga.domain.model.makeLibrary
import org.gotson.komga.domain.model.makeSeries
import org.gotson.komga.domain.persistence.LibraryRepository
import org.gotson.komga.domain.persistence.SeriesRepository
import org.gotson.komga.interfaces.web.WithMockCustomUser
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
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.MockMvcResultMatchersDsl
import org.springframework.test.web.servlet.get
import java.time.LocalDateTime
import java.time.ZoneOffset
import javax.sql.DataSource

@ExtendWith(SpringExtension::class)
@SpringBootTest
@AutoConfigureTestDatabase
@AutoConfigureMockMvc(printOnlyOnFailure = false)
class BookControllerTest(
    @Autowired private val seriesRepository: SeriesRepository,
    @Autowired private val libraryRepository: LibraryRepository,
    @Autowired private val mockMvc: MockMvc
) {

  lateinit var jdbcTemplate: JdbcTemplate

  @Autowired
  fun setDataSource(dataSource: DataSource) {
    this.jdbcTemplate = JdbcTemplate(dataSource)
  }

  private val library = makeLibrary()

  @BeforeAll
  fun `setup library`() {
    jdbcTemplate.execute("ALTER SEQUENCE hibernate_sequence RESTART WITH 1")

    libraryRepository.save(library)
  }

  @AfterAll
  fun `teardown library`() {
    libraryRepository.deleteAll()
  }

  @AfterEach
  fun `clear repository`() {
    seriesRepository.deleteAll()
  }

  @Nested
  inner class LimitedUser {
    @Test
    @WithMockCustomUser(sharedAllLibraries = false, sharedLibraries = [1])
    fun `given user with access to a single library when getting books then only gets books from this library`() {
      val series = makeSeries(
          name = "series",
          books = listOf(makeBook("1"))
      ).also { it.library = library }
      seriesRepository.save(series)

      val otherLibrary = makeLibrary("other")
      libraryRepository.save(otherLibrary)

      val otherSeries = makeSeries(
          name = "otherSeries",
          books = listOf(makeBook("2"))
      ).also { it.library = otherLibrary }
      seriesRepository.save(otherSeries)

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
    fun `given user with no access to any library when getting specific book then returns unauthorized`() {
      val series = makeSeries(
          name = "series",
          books = listOf(makeBook("1"))
      ).also { it.library = library }
      seriesRepository.save(series)
      val book = series.books.first()

      mockMvc.get("/api/v1/series/${series.id}/books/${book.id}")
          .andExpect { status { isUnauthorized } }

      mockMvc.get("/api/v1/books/${book.id}")
          .andExpect { status { isUnauthorized } }
    }

    @Test
    @WithMockCustomUser(sharedAllLibraries = false, sharedLibraries = [])
    fun `given user with no access to any library when getting specific book thumbnail then returns unauthorized`() {
      val series = makeSeries(
          name = "series",
          books = listOf(makeBook("1"))
      ).also { it.library = library }
      seriesRepository.save(series)
      val book = series.books.first()

      mockMvc.get("/api/v1/series/${series.id}/books/${book.id}/thumbnail")
          .andExpect { status { isUnauthorized } }

      mockMvc.get("/api/v1/books/${book.id}/thumbnail")
          .andExpect { status { isUnauthorized } }
    }

    @Test
    @WithMockCustomUser(sharedAllLibraries = false, sharedLibraries = [])
    fun `given user with no access to any library when getting specific book file then returns unauthorized`() {
      val series = makeSeries(
          name = "series",
          books = listOf(makeBook("1"))
      ).also { it.library = library }
      seriesRepository.save(series)
      val book = series.books.first()

      mockMvc.get("/api/v1/series/${series.id}/books/${book.id}/file")
          .andExpect { status { isUnauthorized } }

      mockMvc.get("/api/v1/books/${book.id}/file")
          .andExpect { status { isUnauthorized } }
    }

    @Test
    @WithMockCustomUser(sharedAllLibraries = false, sharedLibraries = [])
    fun `given user with no access to any library when getting specific book pages then returns unauthorized`() {
      val series = makeSeries(
          name = "series",
          books = listOf(makeBook("1"))
      ).also { it.library = library }
      seriesRepository.save(series)
      val book = series.books.first()

      mockMvc.get("/api/v1/series/${series.id}/books/${book.id}/pages")
          .andExpect { status { isUnauthorized } }

      mockMvc.get("/api/v1/books/${book.id}/pages")
          .andExpect { status { isUnauthorized } }
    }

    @Test
    @WithMockCustomUser(sharedAllLibraries = false, sharedLibraries = [])
    fun `given user with no access to any library when getting specific book page then returns unauthorized`() {
      val series = makeSeries(
          name = "series",
          books = listOf(makeBook("1"))
      ).also { it.library = library }
      seriesRepository.save(series)
      val book = series.books.first()

      mockMvc.get("/api/v1/series/${series.id}/books/${book.id}/pages/1")
          .andExpect { status { isUnauthorized } }

      mockMvc.get("/api/v1/books/${book.id}/pages/1")
          .andExpect { status { isUnauthorized } }
    }
  }

  @Nested
  inner class BookMetadataNotReady {
    @Test
    @WithMockCustomUser
    fun `given book without thumbnail when getting book thumbnail then returns not found`() {
      val series = makeSeries(
          name = "series",
          books = listOf(makeBook("1"))
      ).also { it.library = library }
      seriesRepository.save(series)
      val book = series.books.first()

      mockMvc.get("/api/v1/series/${series.id}/books/${book.id}/thumbnail")
          .andExpect { status { isNotFound } }

      mockMvc.get("/api/v1/books/${book.id}/thumbnail")
          .andExpect { status { isNotFound } }
    }

    @Test
    @WithMockCustomUser
    fun `given book without file when getting book file then returns not found`() {
      val series = makeSeries(
          name = "series",
          books = listOf(makeBook("1"))
      ).also { it.library = library }
      seriesRepository.save(series)
      val book = series.books.first()

      mockMvc.get("/api/v1/series/${series.id}/books/${book.id}/file")
          .andExpect { status { isNotFound } }

      mockMvc.get("/api/v1/books/${book.id}/file")
          .andExpect { status { isNotFound } }
    }

    @ParameterizedTest
    @EnumSource(value = BookMetadata.Status::class, names = ["READY"], mode = EnumSource.Mode.EXCLUDE)
    @WithMockCustomUser
    fun `given book with metadata status not ready when getting book pages then returns not found`(status: BookMetadata.Status) {
      val series = makeSeries(
          name = "series",
          books = listOf(makeBook("1").also { it.metadata.status = status })
      ).also { it.library = library }
      seriesRepository.save(series)
      val book = series.books.first()

      mockMvc.get("/api/v1/series/${series.id}/books/${book.id}/pages")
          .andExpect { status { isNotFound } }

      mockMvc.get("/api/v1/books/${book.id}/pages")
          .andExpect { status { isNotFound } }
    }

    @ParameterizedTest
    @EnumSource(value = BookMetadata.Status::class, names = ["READY"], mode = EnumSource.Mode.EXCLUDE)
    @WithMockCustomUser
    fun `given book with metadata status not ready when getting specific book page then returns not found`(status: BookMetadata.Status) {
      val series = makeSeries(
          name = "series",
          books = listOf(makeBook("1").also { it.metadata.status = status })
      ).also { it.library = library }
      seriesRepository.save(series)
      val book = series.books.first()

      mockMvc.get("/api/v1/series/${series.id}/books/${book.id}/pages/1")
          .andExpect { status { isNotFound } }

      mockMvc.get("/api/v1/books/${book.id}/pages/1")
          .andExpect { status { isNotFound } }
    }
  }

  @ParameterizedTest
  @ValueSource(strings = ["25", "-5", "0"])
  @WithMockCustomUser
  fun `given book with pages when getting non-existent page then returns bad request`(page: String) {
    val series = makeSeries(
        name = "series",
        books = listOf(makeBook("1").also {
          it.metadata.pages = listOf(BookPage("file", "image/jpeg"))
          it.metadata.status = BookMetadata.Status.READY
        })
    ).also { it.library = library }
    seriesRepository.save(series)
    val book = series.books.first()

    mockMvc.get("/api/v1/series/${series.id}/books/${book.id}/pages/$page")
        .andExpect { status { isBadRequest } }

    mockMvc.get("/api/v1/books/${book.id}/pages/$page")
        .andExpect { status { isBadRequest } }
  }

  @Nested
  inner class DtoUrlSanitization {
    @Test
    @WithMockCustomUser
    fun `given regular user when getting books then full url is hidden`() {
      val series = makeSeries(
          name = "series",
          books = listOf(makeBook("1.cbr"))
      ).also { it.library = library }
      seriesRepository.save(series)

      val validation: MockMvcResultMatchersDsl.() -> Unit = {
        status { isOk }
        jsonPath("$.content[0].url") { value("1.cbr") }
      }

      mockMvc.get("/api/v1/books")
          .andExpect(validation)

      mockMvc.get("/api/v1/books/latest")
          .andExpect(validation)

      mockMvc.get("/api/v1/series/${series.id}/books?ready_only=false")
          .andExpect(validation)

      mockMvc.get("/api/v1/books/${series.books.first().id}")
          .andExpect {
            status { isOk }
            jsonPath("$.url") { value("1.cbr") }
          }
    }

    @Test
    @WithMockCustomUser(roles = [UserRoles.ADMIN])
    fun `given admin user when getting books then full url is available`() {
      val series = makeSeries(
          name = "series",
          books = listOf(makeBook("1.cbr"))
      ).also { it.library = library }
      seriesRepository.save(series)

      val url = "/1.cbr"
      val validation: MockMvcResultMatchersDsl.() -> Unit = {
        status { isOk }
        jsonPath("$.content[0].url") { value(url) }
      }

      mockMvc.get("/api/v1/books")
          .andExpect(validation)

      mockMvc.get("/api/v1/books/latest")
          .andExpect(validation)

      mockMvc.get("/api/v1/series/${series.id}/books?ready_only=false")
          .andExpect(validation)

      mockMvc.get("/api/v1/books/${series.books.first().id}")
          .andExpect {
            status { isOk }
            jsonPath("$.url") { value(url) }
          }
    }
  }

  @Nested
  inner class HttpCache {
    @Test
    @WithMockCustomUser
    fun `given request with If-Modified-Since headers when getting thumbnail then returns 304 not modified`() {
      val series = makeSeries(
          name = "series",
          books = listOf(makeBook("1.cbr"))
      ).also { it.library = library }
      seriesRepository.save(series)

      mockMvc.get("/api/v1/books/${series.books.first().id}/thumbnail") {
        headers {
          ifModifiedSince = LocalDateTime.now().toInstant(ZoneOffset.UTC).toEpochMilli()
        }
      }.andExpect {
        status { isNotModified }
      }
    }

    @Test
    @WithMockCustomUser
    fun `given request with If-Modified-Since headers when getting page then returns 304 not modified`() {
      val series = makeSeries(
          name = "series",
          books = listOf(makeBook("1.cbr"))
      ).also { it.library = library }
      seriesRepository.save(series)

      mockMvc.get("/api/v1/books/${series.books.first().id}/pages/1") {
        headers {
          ifModifiedSince = LocalDateTime.now().toInstant(ZoneOffset.UTC).toEpochMilli()
        }
      }.andExpect {
        status { isNotModified }
      }
    }
  }
}
