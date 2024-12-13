package org.gotson.komga.interfaces.mvc

import org.gotson.komga.interfaces.api.rest.WithMockCustomUser
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get

@SpringBootTest
@AutoConfigureMockMvc(printOnlyOnFailure = false)
class ResourceNotFoundControllerTest(
  @Autowired private val mockMvc: MockMvc,
) {
  @Test
  @WithMockCustomUser
  fun `when getting an unknown API endpoint then 404 is returned`() {
    mockMvc
      .get("/api/v1/doesnotexist")
      .andExpect {
        status { isNotFound() }
      }
  }

  @Test
  @WithMockCustomUser
  fun `when getting an unknown OPDS endpoint then 404 is returned`() {
    mockMvc
      .get("/opds/v2/doesnotexist")
      .andExpect {
        status { isNotFound() }
      }
  }

  @Test
  @WithMockCustomUser
  fun `when getting an unknown SSE endpoint then 404 is returned`() {
    mockMvc
      .get("/sse/v1/doesnotexist")
      .andExpect {
        status { isNotFound() }
      }
  }

  @Test
  @WithMockCustomUser
  fun `when getting an unknown endpoint then it is forwarded to index`() {
    mockMvc
      .get("/book/0DBTWY6S0KNX9")
      .andExpect {
        status { isOk() }
      }
  }
}
