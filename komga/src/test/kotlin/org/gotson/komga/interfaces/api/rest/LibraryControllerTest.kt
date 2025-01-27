package org.gotson.komga.interfaces.api.rest

import org.gotson.komga.domain.model.makeLibrary
import org.gotson.komga.domain.persistence.LibraryRepository
import org.hamcrest.Matchers
import org.hamcrest.Matchers.hasItems
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.security.test.context.support.WithAnonymousUser
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.patch
import org.springframework.test.web.servlet.post
import java.nio.file.Path

@SpringBootTest
@AutoConfigureMockMvc(printOnlyOnFailure = false)
class LibraryControllerTest(
  @Autowired private val mockMvc: MockMvc,
  @Autowired private val libraryRepository: LibraryRepository,
) {
  private val route = "/api/v1/libraries"

  private val library = makeLibrary(path = "file:/library1", id = "1")

  @BeforeEach
  fun `setup library`() {
    libraryRepository.insert(library)
  }

  @AfterEach
  fun `teardown library`() {
    libraryRepository.deleteAll()
  }

  @Nested
  inner class AnonymousUser {
    @Test
    @WithAnonymousUser
    fun `given anonymous user when getAll then return unauthorized`() {
      mockMvc
        .get(route)
        .andExpect { status { isUnauthorized() } }
    }

    @Test
    @WithAnonymousUser
    fun `given anonymous user when addOne then return unauthorized`() {
      // language=JSON
      val jsonString = """{"name":"test", "root": "C:\\Temp"}"""

      mockMvc
        .post(route) {
          contentType = MediaType.APPLICATION_JSON
          content = jsonString
        }.andExpect { status { isUnauthorized() } }
    }
  }

  @Nested
  inner class UserRoles {
    @Test
    @WithMockCustomUser
    fun `given user with access to all libraries when getAll then return ok`() {
      mockMvc
        .get(route)
        .andExpect { status { isOk() } }
    }

    @Test
    @WithMockUser
    fun `given user with USER role when addOne then return forbidden`() {
      // language=JSON
      val jsonString = """{"name":"test", "root": "C:\\Temp"}"""

      mockMvc
        .post(route) {
          contentType = MediaType.APPLICATION_JSON
          content = jsonString
        }.andExpect { status { isForbidden() } }
    }
  }

  @Nested
  inner class LimitedUser {
    @Test
    @WithMockCustomUser(sharedAllLibraries = false, sharedLibraries = ["1"])
    fun `given user with access to a single library when getAll then only gets this library`() {
      mockMvc
        .get(route)
        .andExpect {
          status { isOk() }
          jsonPath("$.length()") { value(1) }
          jsonPath("$[0].id") { value(1) }
        }
    }
  }

  @Nested
  inner class DtoRootSanitization {
    @Test
    @WithMockCustomUser
    fun `given regular user when getting libraries then root is hidden`() {
      mockMvc
        .get(route)
        .andExpect {
          status { isOk() }
          jsonPath("$[0].root") { value("") }
        }

      mockMvc
        .get("$route/${library.id}")
        .andExpect {
          status { isOk() }
          jsonPath("$.root") { value("") }
        }
    }

    @Test
    @WithMockCustomUser(roles = ["ADMIN"])
    fun `given admin user when getting books then root is available`() {
      mockMvc
        .get(route)
        .andExpect {
          status { isOk() }
          jsonPath("$[0].root") { value(Matchers.containsString("library1")) }
        }

      mockMvc
        .get("$route/${library.id}")
        .andExpect {
          status { isOk() }
          jsonPath("$.root") { value(Matchers.containsString("library1")) }
        }
    }
  }

  @Nested
  inner class DirectoryExclusions {
    @Test
    @WithMockCustomUser
    fun `given library with exclusions when getting libraries then exclusions are present`() {
      libraryRepository.update(library.copy(scanDirectoryExclusions = setOf("test", "value")))

      mockMvc
        .get(route)
        .andExpect {
          status { isOk() }
          jsonPath("$[0].scanDirectoryExclusions.length()") { value(2) }
          jsonPath("$[0].scanDirectoryExclusions") { hasItems("test", "value") }
        }

      mockMvc
        .get("$route/${library.id}")
        .andExpect {
          status { isOk() }
          jsonPath("$.scanDirectoryExclusions.length()") { value(2) }
          jsonPath("$.scanDirectoryExclusions") { hasItems("test", "value") }
        }
    }

    @Test
    @WithMockCustomUser(roles = ["ADMIN"])
    fun `given library with exclusions when updating library then exclusions are updated`(
      @TempDir tmp: Path,
    ) {
      libraryRepository.update(library.copy(root = tmp.toUri().toURL(), scanDirectoryExclusions = setOf("test", "value")))

      // language=JSON
      val jsonString =
        """
        {
          "scanDirectoryExclusions": ["updated"]
        }
        """.trimIndent()

      mockMvc
        .patch("$route/${library.id}") {
          contentType = MediaType.APPLICATION_JSON
          content = jsonString
        }.andExpect {
          status { isNoContent() }
        }

      mockMvc
        .get("$route/${library.id}")
        .andExpect {
          status { isOk() }
          jsonPath("$.scanDirectoryExclusions.length()") { value(1) }
          jsonPath("$.scanDirectoryExclusions") { hasItems("updated") }
        }
    }
  }
}
