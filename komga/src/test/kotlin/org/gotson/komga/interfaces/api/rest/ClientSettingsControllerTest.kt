package org.gotson.komga.interfaces.api.rest

import org.gotson.komga.domain.model.KomgaUser
import org.gotson.komga.domain.persistence.KomgaUserRepository
import org.gotson.komga.infrastructure.jooq.main.ClientSettingsDtoDao
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.security.test.context.support.WithAnonymousUser
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.put

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
        .get("/api/v1/client-settings/list")
        .andExpect {
          status { isOk() }
          jsonPath("$.length()") { value(1) }
          jsonPath("$[0].allowUnauthorized") { doesNotExist() }
        }
    }

    @Test
    @WithAnonymousUser
    fun `given anonymous user when updating global settings then returns unauthorized`() {
      //language=JSON
      val jsonString =
        """
        {
          "key": "setting",
          "value": "value",
          "allowUnauthorized": false
        }
        """.trimIndent()

      mockMvc.put("/api/v1/client-settings/global") {
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
    fun `given authenticated user when retrieving settings then returns all settings`() {
      clientSettingsDtoDao.saveGlobal("forall", "value", true)
      clientSettingsDtoDao.saveGlobal("restricted", "value", false)

      mockMvc
        .get("/api/v1/client-settings/list")
        .andExpect {
          status { isOk() }
          jsonPath("$.length()") { value(2) }
          jsonPath("$[0].allowUnauthorized") { doesNotExist() }
        }
    }

    @Test
    @WithMockCustomUser
    fun `given non-admin user when updating global settings then returns forbidden`() {
      //language=JSON
      val jsonString =
        """
        {
          "key": "setting",
          "value": "value",
          "allowUnauthorized": false
        }
        """.trimIndent()

      mockMvc.put("/api/v1/client-settings/global") {
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
          "key": "setting",
          "value": "value"
        }
        """.trimIndent()

      mockMvc.put("/api/v1/client-settings/user") {
        contentType = MediaType.APPLICATION_JSON
        content = jsonString
      }.andExpect {
        status { isNoContent() }
      }

      mockMvc
        .get("/api/v1/client-settings/list")
        .andExpect {
          status { isOk() }
          jsonPath("$.length()") { value(1) }
          jsonPath("$[0].key") { value("setting") }
          jsonPath("$[0].value") { value("value") }
          jsonPath("$[0].userId") { value("user1") }
        }
    }

    @Test
    @WithMockCustomUser(id = "user1")
    fun `given non-admin user when retrieving settings then user settings take precedence over global settings`() {
      clientSettingsDtoDao.saveGlobal("setting", "global", false)
      clientSettingsDtoDao.saveForUser(user1.id, "setting", "local")

      mockMvc
        .get("/api/v1/client-settings/list")
        .andExpect {
          status { isOk() }
          jsonPath("$.length()") { value(1) }
          jsonPath("$[0].key") { value("setting") }
          jsonPath("$[0].value") { value("local") }
          jsonPath("$[0].userId") { value("user1") }
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
        .get("/api/v1/client-settings/list")
        .andExpect {
          status { isOk() }
          jsonPath("$.length()") { value(2) }
          jsonPath("$[0].allowUnauthorized") { value(true) }
        }
    }

    @Test
    @WithMockCustomUser(roles = ["ADMIN"])
    fun `given admin user when updating global settings then settings are updated`() {
      //language=JSON
      val jsonString =
        """
        {
          "key": "setting",
          "value": "value",
          "allowUnauthorized": false
        }
        """.trimIndent()

      mockMvc.put("/api/v1/client-settings/global") {
        contentType = MediaType.APPLICATION_JSON
        content = jsonString
      }.andExpect {
        status { isNoContent() }
      }

      mockMvc
        .get("/api/v1/client-settings/list")
        .andExpect {
          status { isOk() }
          jsonPath("$.length()") { value(1) }
          jsonPath("$[0].key") { value("setting") }
          jsonPath("$[0].value") { value("value") }
          jsonPath("$[0].allowUnauthorized") { value(false) }
        }
    }
  }
}
