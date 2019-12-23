package org.gotson.komga.interfaces.web.rest

import org.gotson.komga.domain.model.BookMetadata
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
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.MockMvcResultMatchersDsl
import org.springframework.test.web.servlet.get
import javax.sql.DataSource

@ExtendWith(SpringExtension::class)
@SpringBootTest
@AutoConfigureTestDatabase
@AutoConfigureMockMvc(printOnlyOnFailure = false)
class SeriesControllerTest(
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
  inner class BookOrdering {
    @Test
    @WithMockCustomUser
    fun `given books with unordered index when requesting via api then books are ordered`() {
      val series = makeSeries(
          name = "series",
          books = listOf(makeBook("1"), makeBook("3"))
      ).also { it.library = library }
      seriesRepository.save(series)

      series.books = series.books.toMutableList().also { it.add(makeBook("2")) }
      seriesRepository.save(series)

      mockMvc.get("/api/v1/series/${series.id}/books?ready_only=false")
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
      val series = makeSeries(
          name = "series",
          books = (1..100 step 2).map { makeBook("$it") }
      ).also { it.library = library }
      seriesRepository.save(series)

      series.books = series.books.toMutableList().also { it.add(makeBook("2")) }
      seriesRepository.save(series)

      mockMvc.get("/api/v1/series/${series.id}/books?ready_only=false")
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

    @Test
    @WithMockCustomUser
    fun `given many books in ready state with unordered index when requesting via api then books are ordered and paged`() {
      val series = makeSeries(
          name = "series",
          books = (1..100 step 2).map { makeBook("$it") }
      ).also { it.library = library }
      seriesRepository.save(series)

      series.books = series.books.toMutableList().also { it.add(makeBook("2")) }
      series.books.forEach { it.metadata = BookMetadata(BookMetadata.Status.READY) }
      seriesRepository.save(series)

      mockMvc.get("/api/v1/series/${series.id}/books?ready_only=true")
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
    @WithMockCustomUser(sharedAllLibraries = false, sharedLibraries = [1])
    fun `given user with access to a single library when getting series then only gets series from this library`() {
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
    fun `given user with no access to any library when getting specific series then returns unauthorized`() {
      val series = makeSeries(
          name = "series",
          books = listOf(makeBook("1"))
      ).also { it.library = library }
      seriesRepository.save(series)

      mockMvc.get("/api/v1/series/${series.id}")
          .andExpect { status { isUnauthorized } }
    }

    @Test
    @WithMockCustomUser(sharedAllLibraries = false, sharedLibraries = [])
    fun `given user with no access to any library when getting specific series thumbnail then returns unauthorized`() {
      val series = makeSeries(
          name = "series",
          books = listOf(makeBook("1"))
      ).also { it.library = library }
      seriesRepository.save(series)

      mockMvc.get("/api/v1/series/${series.id}/thumbnail")
          .andExpect { status { isUnauthorized } }
    }

    @Test
    @WithMockCustomUser(sharedAllLibraries = false, sharedLibraries = [])
    fun `given user with no access to any library when getting specific series books then returns unauthorized`() {
      val series = makeSeries(
          name = "series",
          books = listOf(makeBook("1"))
      ).also { it.library = library }
      seriesRepository.save(series)

      mockMvc.get("/api/v1/series/${series.id}/books")
          .andExpect { status { isUnauthorized } }
    }
  }

  @Nested
  inner class BookMetadataNotReady {
    @Test
    @WithMockCustomUser
    fun `given book without thumbnail when getting series thumbnail then returns not found`() {
      val series = makeSeries(
          name = "series",
          books = listOf(makeBook("1"))
      ).also { it.library = library }
      seriesRepository.save(series)

      mockMvc.get("/api/v1/series/${series.id}/thumbnail")
          .andExpect { status { isNotFound } }
    }
  }

  @Nested
  inner class DtoUrlSanitization {
    @Test
    @WithMockCustomUser
    fun `given regular user when getting series then url is hidden`() {
      val series = makeSeries(
          name = "series",
          books = listOf(makeBook("1.cbr"))
      ).also { it.library = library }
      seriesRepository.save(series)

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

      mockMvc.get("/api/v1/series/${series.id}")
          .andExpect {
            status { isOk }
            jsonPath("$.url") { value("") }
          }
    }

    @Test
    @WithMockCustomUser(roles = [UserRoles.ADMIN])
    fun `given admin user when getting series then url is available`() {
      val series = makeSeries(
          name = "series",
          books = listOf(makeBook("1.cbr"))
      ).also { it.library = library }
      seriesRepository.save(series)

      val url = "/series"
      val validation: MockMvcResultMatchersDsl.() -> Unit = {
        status { isOk }
        jsonPath("$.content[0].url") { value(url) }
      }

      mockMvc.get("/api/v1/series")
          .andExpect(validation)

      mockMvc.get("/api/v1/series/latest")
          .andExpect(validation)

      mockMvc.get("/api/v1/series/new")
          .andExpect(validation)

      mockMvc.get("/api/v1/series/${series.id}")
          .andExpect {
            status { isOk }
            jsonPath("$.url") { value(url) }
          }
    }
  }
}
