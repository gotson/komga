package org.gotson.komga.interfaces.api.rest

import org.assertj.core.api.Assertions.assertThat
import org.gotson.komga.domain.model.ROLE_ADMIN
import org.gotson.komga.domain.model.ROLE_USER
import org.gotson.komga.infrastructure.configuration.KomgaSettingsProvider
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.security.test.context.support.WithAnonymousUser
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.patch
import kotlin.time.Duration.Companion.days

@SpringBootTest
@AutoConfigureMockMvc(printOnlyOnFailure = false)
class SettingsControllerTest(
  @Autowired private val mockMvc: MockMvc,
  @Autowired private val komgaSettingsProvider: KomgaSettingsProvider,
) {
  @Nested
  inner class NonAdminUser {
    @Test
    @WithAnonymousUser
    fun `given anonymous user when retrieving settings then returns unauthorized`() {
      mockMvc.get("/api/v1/settings")
        .andExpect {
          status { isUnauthorized() }
        }
    }

    @Test
    @WithMockCustomUser(roles = [ROLE_USER])
    fun `given restricted user when retrieving settings then returns forbidden`() {
      mockMvc.get("/api/v1/settings")
        .andExpect {
          status { isForbidden() }
        }
    }
  }

  @Test
  @WithMockCustomUser(roles = [ROLE_ADMIN])
  fun `given admin user when retrieving settings then settings are returned`() {
    komgaSettingsProvider.deleteEmptyCollections = true
    komgaSettingsProvider.deleteEmptyReadLists = false
    komgaSettingsProvider.rememberMeDuration = 5.days

    mockMvc.get("/api/v1/settings")
      .andExpect {
        status { isOk() }
        jsonPath("deleteEmptyCollections") { value(true) }
        jsonPath("deleteEmptyReadLists") { value(false) }
        jsonPath("rememberMeDurationDays") { value(5) }
      }
  }

  @Test
  @WithMockCustomUser(roles = [ROLE_ADMIN])
  fun `given admin user when updating settings then settings are updated`() {
    komgaSettingsProvider.deleteEmptyCollections = true
    komgaSettingsProvider.deleteEmptyReadLists = true
    komgaSettingsProvider.rememberMeDuration = 5.days

    val rememberMeKey = komgaSettingsProvider.rememberMeKey

    //language=JSON
    val jsonString = """
      {
        "deleteEmptyCollections": false,
        "rememberMeDurationDays": 15,
        "renewRememberMeKey": true
      }
    """.trimIndent()

    mockMvc.patch("/api/v1/settings") {
      contentType = MediaType.APPLICATION_JSON
      content = jsonString
    }
      .andExpect {
        status { isNoContent() }
      }

    assertThat(komgaSettingsProvider.deleteEmptyCollections).isFalse
    assertThat(komgaSettingsProvider.deleteEmptyReadLists).isTrue
    assertThat(komgaSettingsProvider.rememberMeDuration).isEqualTo(15.days)
    assertThat(komgaSettingsProvider.rememberMeKey).isNotEqualTo(rememberMeKey)
  }

  @Test
  @WithMockCustomUser(roles = [ROLE_ADMIN])
  fun `given admin user when updating with invalid settings then returns bad request`() {
    //language=JSON
    val jsonString = """
      {
        "rememberMeDurationDays": 0
      }
    """.trimIndent()

    mockMvc.patch("/api/v1/settings") {
      contentType = MediaType.APPLICATION_JSON
      content = jsonString
    }
      .andExpect {
        status { isBadRequest() }
      }
  }
}
