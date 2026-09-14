package org.gotson.komga.infrastructure.security.session

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.boot.autoconfigure.web.ServerProperties
import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.mock.web.MockHttpServletResponse
import org.springframework.session.web.http.CookieSerializer
import java.time.Duration

class SessionConfigurationTest {
  private fun writeSessionCookie(serverProperties: ServerProperties): String {
    val response = MockHttpServletResponse()
    SessionConfiguration()
      .cookieSerializer(SessionConfiguration().sessionCookieName(), serverProperties)
      .writeCookieValue(CookieSerializer.CookieValue(MockHttpServletRequest(), response, "session-id"))
    return response.getHeader("Set-Cookie")!!
  }

  @Nested
  inner class CookieMaxAge {
    @Test
    fun `given no max-age configured when writing the session cookie then it is scoped to the browser session`() {
      // given
      val serverProperties = ServerProperties()

      // when
      val setCookie = writeSessionCookie(serverProperties)

      // then
      assertThat(setCookie).contains("KOMGA-SESSION=")
      assertThat(setCookie).doesNotContain("Max-Age")
    }

    @Test
    fun `given max-age configured when writing the session cookie then it is persistent`() {
      // given
      val serverProperties =
        ServerProperties().apply {
          servlet.session.cookie.maxAge = Duration.ofDays(30)
        }

      // when
      val setCookie = writeSessionCookie(serverProperties)

      // then
      assertThat(setCookie).contains("Max-Age=${Duration.ofDays(30).seconds}")
    }
  }
}
