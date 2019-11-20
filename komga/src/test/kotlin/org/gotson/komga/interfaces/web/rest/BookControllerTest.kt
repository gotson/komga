package org.gotson.komga.interfaces.web.rest

import org.gotson.komga.domain.model.BookMetadata
import org.gotson.komga.domain.model.BookPage
import org.gotson.komga.domain.model.makeBook
import org.gotson.komga.domain.model.makeLibrary
import org.gotson.komga.domain.model.makeSeries
import org.gotson.komga.domain.persistence.LibraryRepository
import org.gotson.komga.domain.persistence.SeriesRepository
import org.gotson.komga.interfaces.web.WithMockCustomUser
import org.hamcrest.CoreMatchers.equalTo
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
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
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

      mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/books"))
          .andExpect(MockMvcResultMatchers.status().isOk)
          .andExpect(MockMvcResultMatchers.jsonPath("$.content.length()", equalTo(1)))
          .andExpect(MockMvcResultMatchers.jsonPath("$.content[0].name", equalTo("1")))
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

      mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/series/${series.id}/books/${book.id}"))
          .andExpect(MockMvcResultMatchers.status().isUnauthorized)

      mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/books/${book.id}"))
          .andExpect(MockMvcResultMatchers.status().isUnauthorized)
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

      mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/series/${series.id}/books/${book.id}/thumbnail"))
          .andExpect(MockMvcResultMatchers.status().isUnauthorized)

      mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/books/${book.id}/thumbnail"))
          .andExpect(MockMvcResultMatchers.status().isUnauthorized)
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

      mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/series/${series.id}/books/${book.id}/file"))
          .andExpect(MockMvcResultMatchers.status().isUnauthorized)

      mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/books/${book.id}/file"))
          .andExpect(MockMvcResultMatchers.status().isUnauthorized)
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

      mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/series/${series.id}/books/${book.id}/pages"))
          .andExpect(MockMvcResultMatchers.status().isUnauthorized)

      mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/books/${book.id}/pages"))
          .andExpect(MockMvcResultMatchers.status().isUnauthorized)
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

      mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/series/${series.id}/books/${book.id}/pages/1"))
          .andExpect(MockMvcResultMatchers.status().isUnauthorized)

      mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/books/${book.id}/pages/1"))
          .andExpect(MockMvcResultMatchers.status().isUnauthorized)
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

      mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/series/${series.id}/books/${book.id}/thumbnail"))
          .andExpect(MockMvcResultMatchers.status().isNotFound)

      mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/books/${book.id}/thumbnail"))
          .andExpect(MockMvcResultMatchers.status().isNotFound)
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

      mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/series/${series.id}/books/${book.id}/file"))
          .andExpect(MockMvcResultMatchers.status().isNotFound)

      mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/books/${book.id}/file"))
          .andExpect(MockMvcResultMatchers.status().isNotFound)
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

      mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/series/${series.id}/books/${book.id}/pages"))
          .andExpect(MockMvcResultMatchers.status().isNotFound)

      mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/books/${book.id}/pages"))
          .andExpect(MockMvcResultMatchers.status().isNotFound)
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

      mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/series/${series.id}/books/${book.id}/pages/1"))
          .andExpect(MockMvcResultMatchers.status().isNotFound)

      mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/books/${book.id}/pages/1"))
          .andExpect(MockMvcResultMatchers.status().isNotFound)
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

    mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/series/${series.id}/books/${book.id}/pages/$page"))
        .andExpect(MockMvcResultMatchers.status().isBadRequest)

    mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/books/${book.id}/pages/$page"))
        .andExpect(MockMvcResultMatchers.status().isBadRequest)
  }
}
