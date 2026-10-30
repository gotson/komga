package org.gotson.komga.interfaces.api.rest

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.gotson.komga.domain.persistence.KomgaUserRepository
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
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
  @Autowired private val userRepository: KomgaUserRepository,
) {
  @BeforeEach
  @AfterEach
  fun setup() {
    userRepository.deleteAll()
  }

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

  @Test
  fun `given unclaimed server when concurrently claiming then only one succeeds`() =
    runTest {
      val requestsCount = 10

      val responses =
        (1..requestsCount)
          .map { index ->
            async(Dispatchers.IO) {
              runCatching {
                mockMvc
                  .post("/api/v1/claim") {
                    header("X-Komga-Email", "admin$index@example.com")
                    header("X-Komga-Password", "password")
                  }.andReturn()
                  .response.status
              }.getOrDefault(500)
            }
          }.awaitAll()

      assertThat(userRepository.count()).isEqualTo(1)

      assertThat(responses.count { it == 200 }).isEqualTo(1)
      assertThat(responses.count { it != 200 }).isEqualTo(requestsCount - 1)
    }
}
