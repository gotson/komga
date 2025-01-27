package org.gotson.komga.interfaces.api.rest

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.patch

@SpringBootTest
@AutoConfigureMockMvc(printOnlyOnFailure = false)
@ActiveProfiles("demo", "test")
class UserControllerDemoTest(
  @Autowired private val mockMvc: MockMvc,
) {
  @Test
  @WithMockCustomUser
  fun `given demo profile is active when a user tries to update its password via api then returns forbidden`() {
    // language=JSON
    val jsonString = """{"password":"new"}"""

    mockMvc
      .patch("/api/v2/users/me/password") {
        contentType = MediaType.APPLICATION_JSON
        content = jsonString
      }.andExpect {
        status { isForbidden() }
      }
  }

  @Test
  @WithMockCustomUser
  fun `given demo profile is active when a user tries to retrieve own authentication activity then returns forbidden`() {
    mockMvc
      .get("/api/v2/users/me/authentication-activity")
      .andExpect {
        status { isForbidden() }
      }
  }
}
