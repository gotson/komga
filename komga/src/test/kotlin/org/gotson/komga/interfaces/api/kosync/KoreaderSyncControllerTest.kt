package org.gotson.komga.interfaces.api.kosync

import org.gotson.komga.domain.model.KomgaUser
import org.gotson.komga.domain.model.UserRoles
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
import org.springframework.test.web.servlet.post

@SpringBootTest
@AutoConfigureMockMvc(printOnlyOnFailure = false)
class KoreaderSyncControllerTest(
  @Autowired private val mockMvc: MockMvc,
  @Autowired private val userRepository: KomgaUserRepository,
  @Autowired private val komgaUserLifecycle: KomgaUserLifecycle,
) {
  private val user1 =
    KomgaUser(
      "user@example.org",
      "",
      roles = setOf(UserRoles.KOREADER_SYNC),
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
  fun `when creating user then forbidden is thrown`() {
    mockMvc
      .post("/koreader/users/create")
      .andExpect {
        status { isForbidden() }
      }
  }

  @Test
  fun `given missing X-Auth-User header when authenticating user then forbidden is thrown`() {
    mockMvc
      .get("/koreader/users/auth")
      .andExpect {
        status { isForbidden() }
      }
  }

  @Test
  fun `given api key in X-Auth-User header when authenticating user then returns OK`() {
    mockMvc
      .get("/koreader/users/auth") {
        header("x-auth-user", apiKey)
      }.andExpect {
        status { isOk() }
        jsonPath("authorized") { value("OK") }
      }
  }
}
