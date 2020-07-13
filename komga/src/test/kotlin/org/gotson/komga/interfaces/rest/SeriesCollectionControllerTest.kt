package org.gotson.komga.interfaces.rest

import org.gotson.komga.domain.model.ROLE_ADMIN
import org.gotson.komga.domain.model.Series
import org.gotson.komga.domain.model.SeriesCollection
import org.gotson.komga.domain.model.makeLibrary
import org.gotson.komga.domain.model.makeSeries
import org.gotson.komga.domain.persistence.LibraryRepository
import org.gotson.komga.domain.persistence.SeriesCollectionRepository
import org.gotson.komga.domain.service.LibraryLifecycle
import org.gotson.komga.domain.service.SeriesCollectionLifecycle
import org.gotson.komga.domain.service.SeriesLifecycle
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
class SeriesCollectionControllerTest(
  @Autowired private val mockMvc: MockMvc,
  @Autowired private val collectionLifecycle: SeriesCollectionLifecycle,
  @Autowired private val collectionRepository: SeriesCollectionRepository,
  @Autowired private val libraryLifecycle: LibraryLifecycle,
  @Autowired private val libraryRepository: LibraryRepository,
  @Autowired private val seriesLifecycle: SeriesLifecycle
) {

  private val library1 = makeLibrary("Library1", id = "1")
  private val library2 = makeLibrary("Library2", id = "2")
  private lateinit var seriesLibrary1: List<Series>
  private lateinit var seriesLibrary2: List<Series>
  private lateinit var colLib1: SeriesCollection
  private lateinit var colLib2: SeriesCollection
  private lateinit var colLibBoth: SeriesCollection

  @BeforeAll
  fun setup() {
    libraryRepository.insert(library1)
    libraryRepository.insert(library2)

    seriesLibrary1 = (1..5)
      .map { makeSeries("Series_$it", library1.id) }
      .map { seriesLifecycle.createSeries(it) }

    seriesLibrary2 = (6..10)
      .map { makeSeries("Series_$it", library2.id) }
      .map { seriesLifecycle.createSeries(it) }
  }

  @AfterAll
  fun teardown() {
    libraryRepository.findAll().forEach {
      libraryLifecycle.deleteLibrary(it)
    }
  }

  @AfterEach
  fun clear() {
    collectionRepository.deleteAll()
  }

  private fun makeCollections() {
    colLib1 = collectionLifecycle.addCollection(SeriesCollection(
      name = "Lib1",
      seriesIds = seriesLibrary1.map { it.id }
    ))

    colLib2 = collectionLifecycle.addCollection(SeriesCollection(
      name = "Lib2",
      seriesIds = seriesLibrary2.map { it.id }
    ))

    colLibBoth = collectionLifecycle.addCollection(SeriesCollection(
      name = "Lib1+2",
      seriesIds = (seriesLibrary1 + seriesLibrary2).map { it.id }
    ))
  }

  @Nested
  inner class GetAndFilter {
    @Test
    @WithMockCustomUser
    fun `given user with access to all libraries when getting collections then get all collections`() {
      makeCollections()

      mockMvc.get("/api/v1/collections")
        .andExpect {
          status { isOk }
          jsonPath("$.totalElements") { value(3) }
          jsonPath("$.content[?(@.name == 'Lib1')].filtered") { value(false) }
          jsonPath("$.content[?(@.name == 'Lib2')].filtered") { value(false) }
          jsonPath("$.content[?(@.name == 'Lib1+2')].filtered") { value(false) }
        }
    }

    @Test
    @WithMockCustomUser(sharedAllLibraries = false, sharedLibraries = ["1"])
    fun `given user with access to a single library when getting collections then only get collections from this library`() {
      makeCollections()

      mockMvc.get("/api/v1/collections")
        .andExpect {
          status { isOk }
          jsonPath("$.totalElements") { value(2) }
          jsonPath("$.content[?(@.name == 'Lib1')].filtered") { value(false) }
          jsonPath("$.content[?(@.name == 'Lib1+2')].filtered") { value(true) }
        }
    }

    @Test
    @WithMockCustomUser
    fun `given user with access to all libraries when getting single collection then it is not filtered`() {
      makeCollections()

      mockMvc.get("/api/v1/collections/${colLibBoth.id}")
        .andExpect {
          status { isOk }
          jsonPath("$.seriesIds.length()") { value(10) }
          jsonPath("$.filtered") { value(false) }
        }
    }

    @Test
    @WithMockCustomUser(sharedAllLibraries = false, sharedLibraries = ["1"])
    fun `given user with access to a single library when getting single collection with items from 2 libraries then it is filtered`() {
      makeCollections()

      mockMvc.get("/api/v1/collections/${colLibBoth.id}")
        .andExpect {
          status { isOk }
          jsonPath("$.seriesIds.length()") { value(5) }
          jsonPath("$.filtered") { value(true) }
        }
    }

    @Test
    @WithMockCustomUser(sharedAllLibraries = false, sharedLibraries = ["1"])
    fun `given user with access to a single library when getting single collection from another library then return not found`() {
      makeCollections()

      mockMvc.get("/api/v1/collections/${colLib2.id}")
        .andExpect {
          status { isNotFound }
        }
    }
  }

  @Nested
  inner class Creation {
    @Test
    @WithMockCustomUser
    fun `given non-admin user when creating collection then return forbidden`() {
      val jsonString = """
        {"name":"collection","ordered":false,"seriesIds":[3]}
      """.trimIndent()

      mockMvc.post("/api/v1/collections") {
        contentType = MediaType.APPLICATION_JSON
        content = jsonString
      }.andExpect {
        status { isForbidden }
      }
    }

    @Test
    @WithMockCustomUser(roles = [ROLE_ADMIN])
    fun `given admin user when creating collection then return ok`() {
      val jsonString = """
        {"name":"collection","ordered":false,"seriesIds":["${seriesLibrary1.first().id}"]}
      """.trimIndent()

      mockMvc.post("/api/v1/collections") {
        contentType = MediaType.APPLICATION_JSON
        content = jsonString
      }.andExpect {
        status { isOk }
        jsonPath("$.seriesIds.length()") { value(1) }
        jsonPath("$.name") { value("collection") }
        jsonPath("$.ordered") { value(false) }
      }
    }

    @Test
    @WithMockCustomUser(roles = [ROLE_ADMIN])
    fun `given existing collections when creating collection with existing name then return bad request`() {
      makeCollections()

      val jsonString = """
        {"name":"Lib1","ordered":false,"seriesIds":[${seriesLibrary1.first().id}]}
      """.trimIndent()

      mockMvc.post("/api/v1/collections") {
        contentType = MediaType.APPLICATION_JSON
        content = jsonString
      }.andExpect {
        status { isBadRequest }
      }
    }

    @Test
    @WithMockCustomUser(roles = [ROLE_ADMIN])
    fun `given collection with duplicate seriesIds when creating collection then return bad request`() {
      makeCollections()

      val jsonString = """
        {"name":"Lib1","ordered":false,"seriesIds":[${seriesLibrary1.first().id},${seriesLibrary1.first().id}]}
      """.trimIndent()

      mockMvc.post("/api/v1/collections") {
        contentType = MediaType.APPLICATION_JSON
        content = jsonString
      }.andExpect {
        status { isBadRequest }
      }
    }
  }

  @Nested
  inner class Update {
    @Test
    @WithMockCustomUser
    fun `given non-admin user when updating collection then return forbidden`() {
      val jsonString = """
        {"name":"collection","ordered":false,"seriesIds":[3]}
      """.trimIndent()

      mockMvc.patch("/api/v1/collections/5") {
        contentType = MediaType.APPLICATION_JSON
        content = jsonString
      }.andExpect {
        status { isForbidden }
      }
    }

    @Test
    @WithMockCustomUser(roles = [ROLE_ADMIN])
    fun `given admin user when updating collection then return no content`() {
      makeCollections()

      val jsonString = """
        {"name":"updated","ordered":true,"seriesIds":["${seriesLibrary1.first().id}"]}
      """.trimIndent()

      mockMvc.patch("/api/v1/collections/${colLib1.id}") {
        contentType = MediaType.APPLICATION_JSON
        content = jsonString
      }.andExpect {
        status { isNoContent }
      }

      mockMvc.get("/api/v1/collections/${colLib1.id}")
        .andExpect {
          status { isOk }
          jsonPath("$.name") { value("updated") }
          jsonPath("$.ordered") { value(true) }
          jsonPath("$.seriesIds.length()") { value(1) }
          jsonPath("$.filtered") { value(false) }
        }
    }

    @Test
    @WithMockCustomUser(roles = [ROLE_ADMIN])
    fun `given existing collections when updating collection with existing name then return bad request`() {
      makeCollections()

      val jsonString = """{"name":"Lib2"}"""

      mockMvc.patch("/api/v1/collections/${colLib1.id}") {
        contentType = MediaType.APPLICATION_JSON
        content = jsonString
      }.andExpect {
        status { isBadRequest }
      }
    }

    @Test
    @WithMockCustomUser(roles = [ROLE_ADMIN])
    fun `given existing collection when updating collection with duplicate seriesIds then return bad request`() {
      makeCollections()

      val jsonString = """{"seriesIds":[${seriesLibrary1.first().id},${seriesLibrary1.first().id}]}"""

      mockMvc.patch("/api/v1/collections/${colLib1.id}") {
        contentType = MediaType.APPLICATION_JSON
        content = jsonString
      }.andExpect {
        status { isBadRequest }
      }
    }

    @Test
    @WithMockCustomUser(roles = [ROLE_ADMIN])
    fun `given admin user when updating collection then only updated fields are modified`() {
      makeCollections()

      mockMvc.patch("/api/v1/collections/${colLib1.id}") {
        contentType = MediaType.APPLICATION_JSON
        content = """{"ordered":true}"""
      }

      mockMvc.get("/api/v1/collections/${colLib1.id}")
        .andExpect {
          status { isOk }
          jsonPath("$.name") { value("Lib1") }
          jsonPath("$.ordered") { value(true) }
          jsonPath("$.seriesIds.length()") { value(5) }
        }


      mockMvc.patch("/api/v1/collections/${colLib2.id}") {
        contentType = MediaType.APPLICATION_JSON
        content = """{"name":"newName"}"""
      }

      mockMvc.get("/api/v1/collections/${colLib2.id}")
        .andExpect {
          status { isOk }
          jsonPath("$.name") { value("newName") }
          jsonPath("$.ordered") { value(false) }
          jsonPath("$.seriesIds.length()") { value(5) }
        }


      mockMvc.patch("/api/v1/collections/${colLibBoth.id}") {
        contentType = MediaType.APPLICATION_JSON
        content = """{"seriesIds":["${seriesLibrary1.first().id}"]}"""
      }

      mockMvc.get("/api/v1/collections/${colLibBoth.id}")
        .andExpect {
          status { isOk }
          jsonPath("$.name") { value("Lib1+2") }
          jsonPath("$.ordered") { value(false) }
          jsonPath("$.seriesIds.length()") { value(1) }
        }
    }
  }

  @Nested
  inner class Delete {
    @Test
    @WithMockCustomUser
    fun `given non-admin user when deleting collection then return forbidden`() {
      mockMvc.delete("/api/v1/collections/5")
        .andExpect {
          status { isForbidden }
        }
    }

    @Test
    @WithMockCustomUser(roles = [ROLE_ADMIN])
    fun `given admin user when deleting collection then return no content`() {
      makeCollections()

      mockMvc.delete("/api/v1/collections/${colLib1.id}")
        .andExpect {
          status { isNoContent }
        }

      mockMvc.get("/api/v1/collections/${colLib1.id}")
        .andExpect {
          status { isNotFound }
        }
    }
  }
}
