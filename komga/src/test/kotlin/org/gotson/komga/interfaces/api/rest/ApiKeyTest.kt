package org.gotson.komga.interfaces.api.rest

import org.gotson.komga.domain.model.KomgaUser
import org.gotson.komga.domain.persistence.KomgaUserRepository
import org.gotson.komga.domain.service.KomgaUserLifecycle
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get

@SpringBootTest
@AutoConfigureMockMvc(printOnlyOnFailure = false)
class ApiKeyTest(
  @Autowired private val mockMvc: MockMvc,
  @Autowired private val userRepository: KomgaUserRepository,
  @Autowired private val komgaUserLifecycle: KomgaUserLifecycle,
) {
  private val user1 =
    KomgaUser(
      "user@example.org",
      "password",
    )
  private lateinit var apiKey: String

  @BeforeAll
  fun setup() {
    userRepository.insert(user1)
    apiKey = komgaUserLifecycle.createApiKey(user1, "test")!!.key
  }

  @AfterAll
  fun teardown() {
    komgaUserLifecycle.deleteUser(user1)
  }

  @Test
  fun `when getting user information then unauthorized is thrown`() {
    mockMvc
      .get("/api/v2/users/me")
      .andExpect {
        status { isUnauthorized() }
      }
  }

  @Test
  fun `given invalid api key in X-API-Key header when getting user information then unauthorized is thrown`() {
    mockMvc
      .get("/api/v2/users/me") {
        header("x-api-key", "abc123")
      }.andExpect {
        status { isUnauthorized() }
      }
  }

  @Test
  fun `given api key in X-API-Key header when getting user information then returns OK`() {
    mockMvc
      .get("/api/v2/users/me") {
        header("x-api-key", apiKey)
      }.andExpect {
        status { isOk() }
        jsonPath("email") { value(user1.email) }
      }
  }
}
