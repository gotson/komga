package org.gotson.komga.interfaces.rest

import org.gotson.komga.domain.model.ROLE_ADMIN
import org.gotson.komga.domain.model.ROLE_USER
import org.gotson.komga.domain.model.makeLibrary
import org.gotson.komga.domain.persistence.LibraryRepository
import org.hamcrest.Matchers
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.security.test.context.support.WithAnonymousUser
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post

@ExtendWith(SpringExtension::class)
@SpringBootTest
@AutoConfigureMockMvc(printOnlyOnFailure = false)
class LibraryControllerTest(
  @Autowired private val mockMvc: MockMvc,
  @Autowired private val libraryRepository: LibraryRepository
) {

  private val route = "/api/v1/libraries"

  private val library = makeLibrary(url = "file:/library1", id = "1")

  @BeforeAll
  fun `setup library`() {
    libraryRepository.insert(library)
  }

  @AfterAll
  fun `teardown library`() {
    libraryRepository.deleteAll()
  }

  @Nested
  inner class AnonymousUser {
    @Test
    @WithAnonymousUser
    fun `given anonymous user when getAll then return unauthorized`() {
      mockMvc.get(route)
        .andExpect { status { isUnauthorized } }
    }

    @Test
    @WithAnonymousUser
    fun `given anonymous user when addOne then return unauthorized`() {
      val jsonString = """{"name":"test", "root": "C:\\Temp"}"""

      mockMvc.post(route) {
        contentType = MediaType.APPLICATION_JSON
        content = jsonString
      }.andExpect { status { isUnauthorized } }
    }
  }

  @Nested
  inner class UserRole {
    @Test
    @WithMockCustomUser
    fun `given user with access to all libraries when getAll then return ok`() {
      mockMvc.get(route)
        .andExpect { status { isOk } }
    }

    @Test
    @WithMockUser(roles = [ROLE_USER])
    fun `given user with USER role when addOne then return forbidden`() {
      val jsonString = """{"name":"test", "root": "C:\\Temp"}"""

      mockMvc.post(route) {
        contentType = MediaType.APPLICATION_JSON
        content = jsonString
      }.andExpect { status { isForbidden } }
    }
  }

  @Nested
  inner class LimitedUser {
    @Test
    @WithMockCustomUser(sharedAllLibraries = false, sharedLibraries = ["1"])
    fun `given user with access to a single library when getAll then only gets this library`() {
      mockMvc.get(route)
        .andExpect {
          status { isOk }
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
      mockMvc.get(route)
        .andExpect {
          status { isOk }
          jsonPath("$[0].root") { value("") }
        }

      mockMvc.get("${route}/${library.id}")
        .andExpect {
          status { isOk }
          jsonPath("$.root") { value("") }
        }
    }

    @Test
    @WithMockCustomUser(roles = [ROLE_ADMIN])
    fun `given admin user when getting books then root is available`() {
      mockMvc.get(route)
        .andExpect {
          status { isOk }
          jsonPath("$[0].root") { value(Matchers.containsString("library1")) }
        }

      mockMvc.get("${route}/${library.id}")
        .andExpect {
          status { isOk }
          jsonPath("$.root") { value(Matchers.containsString("library1")) }
        }
    }
  }
}
