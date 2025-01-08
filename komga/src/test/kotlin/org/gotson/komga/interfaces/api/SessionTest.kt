package org.gotson.komga.interfaces.api

import org.assertj.core.api.Assertions.assertThat
import org.gotson.komga.domain.model.KomgaUser
import org.gotson.komga.domain.persistence.KomgaUserRepository
import org.gotson.komga.domain.service.KomgaUserLifecycle
import org.hamcrest.Matchers.containsString
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.HttpHeaders
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get

@SpringBootTest
@AutoConfigureMockMvc(printOnlyOnFailure = false)
class SessionTest(
  @Autowired private val mockMvc: MockMvc,
  @Autowired private val userLifecycle: KomgaUserLifecycle,
  @Autowired private val userRepository: KomgaUserRepository,
  @Autowired private val sessionHeaderName: String,
  @Autowired private val sessionCookieName: String,
) {
  private lateinit var user: KomgaUser

  @BeforeAll
  fun setup() {
    user = KomgaUser("user@example.org", "user")
    userLifecycle.createUser(user)
  }

  @AfterAll
  fun tearDown() {
    userRepository.findAll().forEach {
      userLifecycle.deleteUser(it)
    }
  }

  @Test
  fun `given valid basic credentials when hitting an endpoint then session cookie is returned`() {
    mockMvc
      .get("/api/v2/users/me") {
        with(httpBasic(user.email, user.password))
      }.andExpect {
        header {
          string(HttpHeaders.SET_COOKIE, containsString("$sessionCookieName="))
        }
        cookie {
          exists(sessionCookieName)
          httpOnly(sessionCookieName, true)
        }
      }
  }

  @Test
  fun `given valid basic credentials when providing the auth header then session is returned in headers`() {
    mockMvc
      .get("/api/v2/users/me") {
        with(httpBasic(user.email, user.password))
        header(sessionHeaderName, "")
      }.andExpect {
        header {
          exists(sessionHeaderName)
        }
      }
  }

  @Test
  fun `given existing session when exchanging for cookies then session is returned in cookies`() {
    val sessionId =
      mockMvc
        .get("/api/v2/users/me") {
          with(httpBasic(user.email, user.password))
          header(sessionHeaderName, "")
        }.andReturn()
        .response
        .getHeader(this.sessionHeaderName)

    assertThat(sessionId).isNotNull

    mockMvc
      .get("/api/v1/login/set-cookie") {
        header(sessionHeaderName, sessionId!!)
      }.andExpect {
        header {
          string(HttpHeaders.SET_COOKIE, containsString("$sessionCookieName="))
          doesNotExist(sessionHeaderName)
        }
        cookie {
          exists(sessionCookieName)
          httpOnly(sessionCookieName, true)
        }
      }
  }

  @Test
  fun `given existing session when logging out then session cookie is cleared`() {
    val sessionId =
      mockMvc
        .get("/api/v2/users/me") {
          with(httpBasic(user.email, user.password))
          header(sessionHeaderName, "")
        }.andReturn()
        .response
        .getHeader(this.sessionHeaderName)

    assertThat(sessionId).isNotNull

    mockMvc
      .get("/api/logout") {
        header(sessionHeaderName, sessionId!!)
      }.andExpect {
        header {
          string(HttpHeaders.SET_COOKIE, containsString("$sessionCookieName=;"))
        }
        cookie {
          maxAge(sessionCookieName, 0)
        }
      }
  }
}
