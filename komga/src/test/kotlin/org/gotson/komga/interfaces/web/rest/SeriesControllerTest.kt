package org.gotson.komga.interfaces.web.rest

import org.gotson.komga.domain.model.BookMetadata
import org.gotson.komga.domain.model.Status
import org.gotson.komga.domain.model.makeBook
import org.gotson.komga.domain.model.makeLibrary
import org.gotson.komga.domain.model.makeSeries
import org.gotson.komga.domain.persistence.LibraryRepository
import org.gotson.komga.domain.persistence.SeriesRepository
import org.hamcrest.CoreMatchers.equalTo
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers

@ExtendWith(SpringExtension::class)
@SpringBootTest
@AutoConfigureTestDatabase
@AutoConfigureMockMvc(printOnlyOnFailure = false)
class SeriesControllerTest(
    @Autowired private val seriesRepository: SeriesRepository,
    @Autowired private val libraryRepository: LibraryRepository,
    @Autowired private val mockMvc: MockMvc

) {

  private val library = makeLibrary()

  @BeforeAll
  fun `setup library`() {
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

  @Test
  @WithMockUser
  fun `given books with unordered index when requesting via api then books are ordered`() {
    val series = makeSeries(
        name = "series",
        books = listOf(makeBook("1"), makeBook("3"))
    ).also { it.library = library }
    seriesRepository.save(series)

    series.books = series.books.toMutableList().also { it.add(makeBook("2")) }
    seriesRepository.save(series)

    mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/series/${series.id}/books?readyonly=false"))
        .andExpect(MockMvcResultMatchers.status().isOk)
        .andExpect(MockMvcResultMatchers.jsonPath("$.content[0].name", equalTo("1")))
        .andExpect(MockMvcResultMatchers.jsonPath("$.content[1].name", equalTo("2")))
        .andExpect(MockMvcResultMatchers.jsonPath("$.content[2].name", equalTo("3")))
  }

  @Test
  @WithMockUser
  fun `given many books with unordered index when requesting via api then books are ordered and paged`() {
    val series = makeSeries(
        name = "series",
        books = (1..100 step 2).map { makeBook("$it") }
    ).also { it.library = library }
    seriesRepository.save(series)

    series.books = series.books.toMutableList().also { it.add(makeBook("2")) }
    seriesRepository.save(series)

    mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/series/${series.id}/books?readyonly=false"))
        .andExpect(MockMvcResultMatchers.status().isOk)
        .andExpect(MockMvcResultMatchers.jsonPath("$.content[0].name", equalTo("1")))
        .andExpect(MockMvcResultMatchers.jsonPath("$.content[1].name", equalTo("2")))
        .andExpect(MockMvcResultMatchers.jsonPath("$.content[2].name", equalTo("3")))
        .andExpect(MockMvcResultMatchers.jsonPath("$.content[3].name", equalTo("5")))
        .andExpect(MockMvcResultMatchers.jsonPath("$.size", equalTo(20)))
        .andExpect(MockMvcResultMatchers.jsonPath("$.first", equalTo(true)))
        .andExpect(MockMvcResultMatchers.jsonPath("$.number", equalTo(0)))
  }

  @Test
  @WithMockUser
  fun `given many books in ready state with unordered index when requesting via api then books are ordered and paged`() {
    val series = makeSeries(
        name = "series",
        books = (1..100 step 2).map { makeBook("$it") }
    ).also { it.library = library }
    seriesRepository.save(series)

    series.books = series.books.toMutableList().also { it.add(makeBook("2")) }
    series.books.forEach { it.metadata = BookMetadata(Status.READY) }
    seriesRepository.save(series)

    mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/series/${series.id}/books?readyonly=true"))
        .andExpect(MockMvcResultMatchers.status().isOk)
        .andExpect(MockMvcResultMatchers.jsonPath("$.content[0].name", equalTo("1")))
        .andExpect(MockMvcResultMatchers.jsonPath("$.content[1].name", equalTo("2")))
        .andExpect(MockMvcResultMatchers.jsonPath("$.content[2].name", equalTo("3")))
        .andExpect(MockMvcResultMatchers.jsonPath("$.content[3].name", equalTo("5")))
        .andExpect(MockMvcResultMatchers.jsonPath("$.size", equalTo(20)))
        .andExpect(MockMvcResultMatchers.jsonPath("$.first", equalTo(true)))
        .andExpect(MockMvcResultMatchers.jsonPath("$.number", equalTo(0)))
  }
}
