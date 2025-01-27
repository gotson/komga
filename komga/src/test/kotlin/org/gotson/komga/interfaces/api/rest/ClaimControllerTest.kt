package org.gotson.komga.interfaces.api.rest

import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.security.test.context.support.WithAnonymousUser
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post

@SpringBootTest
@AutoConfigureMockMvc(printOnlyOnFailure = false)
@ActiveProfiles("test")
class ClaimControllerTest(
  @Autowired private val mockMvc: MockMvc,
) {
  @ParameterizedTest
  @ValueSource(strings = ["user", "user@domain"])
  fun `given unclaimed server when claiming with invalid email address then returns bad request`(email: String) {
    val password = "password"

    mockMvc
      .post("/api/v1/claim") {
        header("X-Komga-Email", email)
        header("X-Komga-Password", password)
      }.andExpect {
        status { isBadRequest() }
      }
  }

  @Test
  @WithAnonymousUser
  fun `given anonymous user when getting claim status then returns OK`() {
    mockMvc
      .get("/api/v1/claim")
      .andExpect {
        status { isOk() }
      }
  }
}
