package org.gotson.komga.interfaces.api.rest

import org.gotson.komga.domain.model.KomgaUser
import org.gotson.komga.domain.persistence.KomgaUserRepository
import org.gotson.komga.infrastructure.jooq.main.ClientSettingsDtoDao
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeAll
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
import org.springframework.test.web.servlet.delete
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.patch

@SpringBootTest
@AutoConfigureMockMvc(printOnlyOnFailure = false)
class ClientSettingsControllerTest(
  @Autowired private val mockMvc: MockMvc,
  @Autowired private val clientSettingsDtoDao: ClientSettingsDtoDao,
  @Autowired private val userRepository: KomgaUserRepository,
) {
  @AfterEach
  fun cleanup() {
    clientSettingsDtoDao.deleteAll()
  }

  @Nested
  inner class AnonymousUser {
    @Test
    @WithAnonymousUser
    fun `given anonymous user when retrieving settings then returns only settings allowed for unauthorized users`() {
      clientSettingsDtoDao.saveGlobal("forall", "value", true)
      clientSettingsDtoDao.saveGlobal("restricted", "value", false)

      mockMvc
        .get("/api/v1/client-settings/global/list")
        .andExpect {
          status { isOk() }
          jsonPath("$.size()") { value(1) }
          jsonPath("$.forall.value") { value("value") }
          jsonPath("$.forall.allowUnauthorized") { value(true) }
        }
    }

    @Test
    @WithAnonymousUser
    fun `given anonymous user when updating global settings then returns unauthorized`() {
      //language=JSON
      val jsonString =
        """
        {
          "setting": {
            "value": "value",
            "allowUnauthorized": false
          }
        }
        """.trimIndent()

      mockMvc
        .patch("/api/v1/client-settings/global") {
          contentType = MediaType.APPLICATION_JSON
          content = jsonString
        }.andExpect {
          status { isUnauthorized() }
        }
    }
  }

  @Nested
  inner class RegularUser {
    private val user1 = KomgaUser("user1@example.org", "", id = "user1")

    @BeforeAll
    fun setup() {
      userRepository.insert(user1)
    }

    @AfterAll
    fun tearDown() {
      userRepository.delete(user1.id)
    }

    @Test
    @WithMockCustomUser
    fun `given authenticated user when retrieving global settings then returns all settings`() {
      clientSettingsDtoDao.saveGlobal("forall", "value", true)
      clientSettingsDtoDao.saveGlobal("restricted", "value", false)

      mockMvc
        .get("/api/v1/client-settings/global/list")
        .andExpect {
          status { isOk() }
          jsonPath("$.size()") { value(2) }
          jsonPath("$.forall.value") { value("value") }
          jsonPath("$.forall.allowUnauthorized") { value(true) }
          jsonPath("$.restricted.value") { value("value") }
          jsonPath("$.restricted.allowUnauthorized") { value(false) }
        }
    }

    @Test
    @WithMockCustomUser
    fun `given non-admin user when updating global settings then returns forbidden`() {
      //language=JSON
      val jsonString =
        """
        {
          "setting": {
            "value": "value",
            "allowUnauthorized": false
          }
        }
        """.trimIndent()

      mockMvc
        .patch("/api/v1/client-settings/global") {
          contentType = MediaType.APPLICATION_JSON
          content = jsonString
        }.andExpect {
          status { isForbidden() }
        }
    }

    @Test
    @WithMockCustomUser(id = "user1")
    fun `given non-admin user when updating user settings then settings are updated`() {
      //language=JSON
      val jsonString =
        """
        {
          "setting": {
            "value": "value"
          }
        }
        """.trimIndent()

      mockMvc
        .patch("/api/v1/client-settings/user") {
          contentType = MediaType.APPLICATION_JSON
          content = jsonString
        }.andExpect {
          status { isNoContent() }
        }

      mockMvc
        .get("/api/v1/client-settings/user/list")
        .andExpect {
          status { isOk() }
          jsonPath("$.size()") { value(1) }
          jsonPath("$.setting.value") { value("value") }
        }
    }

    @ParameterizedTest
    @ValueSource(
      strings = [
        "UPPERCASE",
        "   ",
        "",
        "symbols!",
        "two..dots",
        ".start.with.dot",
        "end.with.dot.",
      ],
    )
    @WithMockCustomUser(id = "user1")
    fun `given non-admin user when updating user settings with invalid key then validation error is thrown`(key: String) {
      //language=JSON
      val jsonString =
        """
        {
          "$key": {
            "value": "value"
          }
        }
        """.trimIndent()

      mockMvc
        .patch("/api/v1/client-settings/user") {
          contentType = MediaType.APPLICATION_JSON
          content = jsonString
        }.andExpect {
          status { isBadRequest() }
        }
    }

    @ParameterizedTest
    @ValueSource(
      strings = [
        "UPPERCASE",
        "   ",
        "",
        "symbols!",
        "two..dots",
        ".start.with.dot",
        "end.with.dot.",
      ],
    )
    @WithMockCustomUser(roles = ["ADMIN"])
    fun `given non-admin user when deleting user settings with invalid key then validation error is thrown`(key: String) {
      //language=JSON
      val jsonString =
        """
        ["$key"]
        """.trimIndent()

      mockMvc
        .delete("/api/v1/client-settings/user") {
          contentType = MediaType.APPLICATION_JSON
          content = jsonString
        }.andExpect {
          status { isBadRequest() }
        }
    }

    @ParameterizedTest
    @ValueSource(
      strings = [
        //language=JSON
        "null",
        //language=JSON
        """{"value": null }""",
        //language=JSON
        """{ "value": "   " }""",
        //language=JSON
        """{ "value": "" }""",
      ],
    )
    @WithMockCustomUser(id = "user1")
    fun `given non-admin user when updating user settings with invalid value then validation error is thrown`(value: String) {
      //language=JSON
      val jsonString =
        """
        {
          "key": $value
        }
        """.trimIndent()

      mockMvc
        .patch("/api/v1/client-settings/user") {
          contentType = MediaType.APPLICATION_JSON
          content = jsonString
        }.andExpect {
          status { isBadRequest() }
        }
    }
  }

  @Nested
  inner class AdminUser {
    @Test
    @WithMockCustomUser(roles = ["ADMIN"])
    fun `given admin user when retrieving settings then returns all settings`() {
      clientSettingsDtoDao.saveGlobal("forall", "value", true)
      clientSettingsDtoDao.saveGlobal("restricted", "value", false)

      mockMvc
        .get("/api/v1/client-settings/global/list")
        .andExpect {
          status { isOk() }
          jsonPath("$.size()") { value(2) }
          jsonPath("$.forall.value") { value("value") }
          jsonPath("$.forall.allowUnauthorized") { value(true) }
          jsonPath("$.restricted.value") { value("value") }
          jsonPath("$.restricted.allowUnauthorized") { value(false) }
        }
    }

    @Test
    @WithMockCustomUser(roles = ["ADMIN"])
    fun `given admin user when updating global settings then settings are updated`() {
      //language=JSON
      val jsonString =
        """
        {
          "setting": {
            "value": "value",
            "allowUnauthorized": false
          }
        }
        """.trimIndent()

      mockMvc
        .patch("/api/v1/client-settings/global") {
          contentType = MediaType.APPLICATION_JSON
          content = jsonString
        }.andExpect {
          status { isNoContent() }
        }

      mockMvc
        .get("/api/v1/client-settings/global/list")
        .andExpect {
          status { isOk() }
          jsonPath("$.size()") { value(1) }
          jsonPath("$.setting.value") { value("value") }
          jsonPath("$.setting.allowUnauthorized") { value(false) }
        }
    }

    @ParameterizedTest
    @ValueSource(
      strings = [
        "UPPERCASE",
        "   ",
        "",
        "symbols!",
        "two..dots",
        ".start.with.dot",
        "end.with.dot.",
      ],
    )
    @WithMockCustomUser(roles = ["ADMIN"])
    fun `given admin user when updating global settings with invalid key then validation error is thrown`(key: String) {
      //language=JSON
      val jsonString =
        """
        {
          "$key": {
            "value": "value",
            "allowUnauthorized": false
          }
        }
        """.trimIndent()

      mockMvc
        .patch("/api/v1/client-settings/global") {
          contentType = MediaType.APPLICATION_JSON
          content = jsonString
        }.andExpect {
          status { isBadRequest() }
        }
    }

    @ParameterizedTest
    @ValueSource(
      strings = [
        "UPPERCASE",
        "   ",
        "",
        "symbols!",
        "two..dots",
        ".start.with.dot",
        "end.with.dot.",
      ],
    )
    @WithMockCustomUser(roles = ["ADMIN"])
    fun `given admin user when deleting global settings with invalid key then validation error is thrown`(key: String) {
      //language=JSON
      val jsonString =
        """
        ["$key"]
        """.trimIndent()

      mockMvc
        .delete("/api/v1/client-settings/global") {
          contentType = MediaType.APPLICATION_JSON
          content = jsonString
        }.andExpect {
          status { isBadRequest() }
        }
    }

    @ParameterizedTest
    @ValueSource(
      strings = [
        //language=JSON
        "null",
        //language=JSON
        """{
          "value": "1",
          "allowUnauthorized": null
        }""",
        //language=JSON
        """{
          "value": " ",
          "allowUnauthorized": true
        }""",
        //language=JSON
        """{
          "value": "  ",
          "allowUnauthorized": false
        }""",
        //language=JSON
        """{
          "value": null,
          "allowUnauthorized": false
        }""",
      ],
    )
    @WithMockCustomUser(roles = ["ADMIN"])
    fun `given admin user when updating global settings with invalid value then validation error is thrown`(value: String) {
      //language=JSON
      val jsonString =
        """
        {
          "setting": $value
        }
        """.trimIndent()

      mockMvc
        .patch("/api/v1/client-settings/global") {
          contentType = MediaType.APPLICATION_JSON
          content = jsonString
        }.andExpect {
          status { isBadRequest() }
        }
    }
  }
}
