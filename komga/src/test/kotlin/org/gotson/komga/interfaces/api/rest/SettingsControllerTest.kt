package org.gotson.komga.interfaces.api.rest

import org.assertj.core.api.Assertions.assertThat
import org.gotson.komga.domain.model.ThumbnailSize
import org.gotson.komga.infrastructure.configuration.KomgaSettingsProvider
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
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
      mockMvc
        .get("/api/v1/settings")
        .andExpect {
          status { isUnauthorized() }
        }
    }

    @Test
    @WithMockCustomUser
    fun `given restricted user when retrieving settings then returns forbidden`() {
      mockMvc
        .get("/api/v1/settings")
        .andExpect {
          status { isForbidden() }
        }
    }
  }

  @Test
  @WithMockCustomUser(roles = ["ADMIN"])
  fun `given admin user when retrieving settings then settings are returned`() {
    komgaSettingsProvider.deleteEmptyCollections = true
    komgaSettingsProvider.deleteEmptyReadLists = false
    komgaSettingsProvider.rememberMeDuration = 5.days
    komgaSettingsProvider.thumbnailSize = ThumbnailSize.LARGE
    komgaSettingsProvider.taskPoolSize = 4
    komgaSettingsProvider.serverPort = 1234
    komgaSettingsProvider.serverContextPath = "/example"

    mockMvc
      .get("/api/v1/settings")
      .andExpect {
        status { isOk() }
        jsonPath("deleteEmptyCollections") { value(true) }
        jsonPath("deleteEmptyReadLists") { value(false) }
        jsonPath("rememberMeDurationDays") { value(5) }
        jsonPath("thumbnailSize") { value("LARGE") }
        jsonPath("taskPoolSize") { value(4) }
        jsonPath("serverPort.configurationSource") { value(25600) }
        jsonPath("serverPort.databaseSource") { value(1234) }
        jsonPath("serverPort.effectiveValue") { value(null) } // somehow we don't get the effective value with the mock server
        jsonPath("serverContextPath.configurationSource") { value(null) }
        jsonPath("serverContextPath.databaseSource") { value("/example") }
        jsonPath("serverContextPath.effectiveValue") { value("") }
      }
  }

  @Test
  @WithMockCustomUser(roles = ["ADMIN"])
  fun `given admin user when updating settings then settings are updated`() {
    komgaSettingsProvider.deleteEmptyCollections = true
    komgaSettingsProvider.deleteEmptyReadLists = true
    komgaSettingsProvider.rememberMeDuration = 5.days
    komgaSettingsProvider.thumbnailSize = ThumbnailSize.LARGE
    komgaSettingsProvider.taskPoolSize = 4
    komgaSettingsProvider.serverPort = 1234
    komgaSettingsProvider.serverContextPath = "/example"

    val rememberMeKey = komgaSettingsProvider.rememberMeKey

    //language=JSON
    val jsonString =
      """
      {
        "deleteEmptyCollections": false,
        "rememberMeDurationDays": 15,
        "renewRememberMeKey": true,
        "thumbnailSize": "MEDIUM",
        "taskPoolSize": 8,
        "serverPort": 5678,
        "serverContextPath": "/komga-hyphen/subpath123"
      }
      """.trimIndent()

    mockMvc
      .patch("/api/v1/settings") {
        contentType = MediaType.APPLICATION_JSON
        content = jsonString
      }.andExpect {
        status { isNoContent() }
      }

    assertThat(komgaSettingsProvider.deleteEmptyCollections).isFalse
    assertThat(komgaSettingsProvider.deleteEmptyReadLists).isTrue
    assertThat(komgaSettingsProvider.rememberMeDuration).isEqualTo(15.days)
    assertThat(komgaSettingsProvider.rememberMeKey).isNotEqualTo(rememberMeKey)
    assertThat(komgaSettingsProvider.thumbnailSize).isEqualTo(ThumbnailSize.MEDIUM)
    assertThat(komgaSettingsProvider.taskPoolSize).isEqualTo(8)
    assertThat(komgaSettingsProvider.serverPort).isEqualTo(5678)
    assertThat(komgaSettingsProvider.serverContextPath).isEqualTo("/komga-hyphen/subpath123")
  }

  @Test
  @WithMockCustomUser(roles = ["ADMIN"])
  fun `given admin user when deleting settings then deletable settings are deleted`() {
    komgaSettingsProvider.deleteEmptyCollections = true
    komgaSettingsProvider.deleteEmptyReadLists = true
    komgaSettingsProvider.rememberMeDuration = 5.days
    komgaSettingsProvider.thumbnailSize = ThumbnailSize.LARGE
    komgaSettingsProvider.taskPoolSize = 4
    komgaSettingsProvider.serverPort = 1234
    komgaSettingsProvider.serverContextPath = "/example"

    val rememberMeKey = komgaSettingsProvider.rememberMeKey

    //language=JSON
    val jsonString =
      """
      {
        "deleteEmptyCollections": null,
        "rememberMeDurationDays": null,
        "renewRememberMeKey": null,
        "thumbnailSize": null,
        "taskPoolSize": null,
        "serverPort": null,
        "serverContextPath": null
      }
      """.trimIndent()

    mockMvc
      .patch("/api/v1/settings") {
        contentType = MediaType.APPLICATION_JSON
        content = jsonString
      }.andExpect {
        status { isNoContent() }
      }

    assertThat(komgaSettingsProvider.deleteEmptyCollections).isTrue
    assertThat(komgaSettingsProvider.deleteEmptyReadLists).isTrue
    assertThat(komgaSettingsProvider.rememberMeDuration).isEqualTo(5.days)
    assertThat(komgaSettingsProvider.rememberMeKey).isEqualTo(rememberMeKey)
    assertThat(komgaSettingsProvider.thumbnailSize).isEqualTo(ThumbnailSize.LARGE)
    assertThat(komgaSettingsProvider.taskPoolSize).isEqualTo(4)
    assertThat(komgaSettingsProvider.serverPort).isNull()
    assertThat(komgaSettingsProvider.serverContextPath).isNull()
  }

  @ParameterizedTest
  @WithMockCustomUser(roles = ["ADMIN"])
  @ValueSource(
    strings = [
      //language=JSON
      """{"rememberMeDurationDays": 0}""",
      //language=JSON
      """{"thumbnailSize": "HUGE"}""",
      """{"taskPoolSize": 0}""",
      """{"serverPort": 0}""",
      """{"serverPort": -5}""",
      """{"serverPort": 65536}""",
      """{"serverContextPath": "noSlashBegin"}""",
      """{"serverContextPath": "/slashEnd/"}""",
      """{"serverContextPath": "/invalid=character"}""",
      """{"serverContextPath": "/invalid/end-"}""",
      """{"serverContextPath": "/invalid/end_"}""",
      """{"serverContextPath": "/日本語"}""",
    ],
  )
  fun `given admin user when updating with invalid settings then returns bad request`(jsonString: String) {
    mockMvc
      .patch("/api/v1/settings") {
        contentType = MediaType.APPLICATION_JSON
        content = jsonString
      }.andExpect {
        status { isBadRequest() }
      }
  }
}
