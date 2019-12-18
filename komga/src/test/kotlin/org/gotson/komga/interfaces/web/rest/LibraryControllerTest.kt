package org.gotson.komga.interfaces.web.rest

import org.gotson.komga.interfaces.web.WithMockCustomUser
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
    @Autowired private val mockMvc: MockMvc
) {
  private val route = "/api/v1/libraries"

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
    @WithMockUser(roles = ["USER"])
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
    @WithMockCustomUser(sharedAllLibraries = false, sharedLibraries = [1])
    fun `given user with access to a single library when getAll then only gets this library`() {
      mockMvc.get(route)
          .andExpect {
            status { isOk }
            jsonPath("$.length()") { value(1) }
            jsonPath("$[0].id") { value(1) }
          }
    }
  }
}
