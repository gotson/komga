package org.gotson.komga.interfaces.web.rest

import org.gotson.komga.interfaces.web.WithMockCustomUser
import org.hamcrest.CoreMatchers
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
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers

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
      mockMvc.perform(MockMvcRequestBuilders.get(route))
          .andExpect(MockMvcResultMatchers.status().isUnauthorized)
    }

    @Test
    @WithAnonymousUser
    fun `given anonymous user when addOne then return unauthorized`() {
      val jsonString = """{"name":"test", "root": "C:\\Temp"}"""

      mockMvc.perform(MockMvcRequestBuilders.post(route)
          .contentType(MediaType.APPLICATION_JSON)
          .content(jsonString))
          .andExpect(MockMvcResultMatchers.status().isUnauthorized)
    }
  }

  @Nested
  inner class UserRole {
    @Test
    @WithMockCustomUser
    fun `given user with access to all libraries when getAll then return ok`() {
      mockMvc.perform(MockMvcRequestBuilders.get(route))
          .andExpect(MockMvcResultMatchers.status().isOk)
    }

    @Test
    @WithMockUser(roles = ["USER"])
    fun `given user with USER role when addOne then return forbidden`() {
      val jsonString = """{"name":"test", "root": "C:\\Temp"}"""

      mockMvc.perform(MockMvcRequestBuilders.post(route)
          .contentType(MediaType.APPLICATION_JSON)
          .content(jsonString))
          .andExpect(MockMvcResultMatchers.status().isForbidden)
    }
  }

  @Nested
  inner class LimitedUser {
    @Test
    @WithMockCustomUser(sharedAllLibraries = false, sharedLibraries = [1])
    fun `given user with access to a single library when getAll then only gets this library`() {
      mockMvc.perform(MockMvcRequestBuilders.get(route))
          .andExpect(MockMvcResultMatchers.status().isOk)
          .andExpect(MockMvcResultMatchers.jsonPath("$.length()", CoreMatchers.equalTo(1)))
          .andExpect(MockMvcResultMatchers.jsonPath("$[0].id", CoreMatchers.equalTo(1)))
    }
  }
}
