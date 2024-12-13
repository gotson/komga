package org.gotson.komga.interfaces.api.rest

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.security.test.context.support.WithAnonymousUser
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get

@SpringBootTest
@AutoConfigureMockMvc(printOnlyOnFailure = false)
@ActiveProfiles("test")
class OAuth2ControllerTest(
  @Autowired private val mockMvc: MockMvc,
) {
  @Test
  @WithAnonymousUser
  fun `given anonymous user when getting oauth2 providers then returns OK`() {
    mockMvc
      .get("/api/v1/oauth2/providers")
      .andExpect {
        status { isOk() }
      }
  }
}
