package org.gotson.komga.interfaces.api.rest

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get

@SpringBootTest
@AutoConfigureMockMvc(printOnlyOnFailure = false)
class FontsControllerTest(
  @Autowired private val mockMvc: MockMvc,
) {
  @Test
  @WithMockCustomUser
  fun `when getting font families then the embedded fonts are returned`() {
    mockMvc
      .get("/api/v1/fonts/families")
      .andExpect {
        status { isOk() }
        jsonPath("$.length()") { value(1) }
        jsonPath("$[0]") { value("OpenDyslexic") }
      }
  }
}
