package org.gotson.komga.interfaces.api.rest

import org.assertj.core.api.Assertions.assertThat
import org.gotson.komga.domain.model.AgeRestriction
import org.gotson.komga.domain.model.AllowExclude
import org.gotson.komga.domain.model.ContentRestrictions
import org.gotson.komga.domain.model.KomgaUser
import org.gotson.komga.domain.model.ROLE_ADMIN
import org.gotson.komga.domain.model.ROLE_FILE_DOWNLOAD
import org.gotson.komga.domain.model.ROLE_PAGE_STREAMING
import org.gotson.komga.domain.model.makeLibrary
import org.gotson.komga.domain.persistence.KomgaUserRepository
import org.gotson.komga.domain.persistence.LibraryRepository
import org.gotson.komga.domain.service.KomgaUserLifecycle
import org.gotson.komga.domain.service.LibraryLifecycle
import org.hamcrest.text.MatchesPattern
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
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.delete
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.patch
import org.springframework.test.web.servlet.post

@SpringBootTest
@AutoConfigureMockMvc(printOnlyOnFailure = false)
@ActiveProfiles("test")
class UserControllerTest(
  @Autowired private val mockMvc: MockMvc,
  @Autowired private val libraryRepository: LibraryRepository,
  @Autowired private val libraryLifecycle: LibraryLifecycle,
  @Autowired private val userRepository: KomgaUserRepository,
) {
  @Autowired
  private lateinit var userLifecycle: KomgaUserLifecycle

  private val admin = KomgaUser("admin@example.org", "", true, id = "admin")

  @BeforeAll
  fun setup() {
    libraryRepository.insert(makeLibrary(id = "1"))
    libraryRepository.insert(makeLibrary(id = "2"))
    userRepository.insert(admin)
  }

  @AfterAll
  fun teardown() {
    userRepository.findAll().forEach {
      userLifecycle.deleteUser(it)
    }
    libraryRepository.findAll().forEach {
      libraryLifecycle.deleteLibrary(it)
    }
  }

  @AfterEach
  fun deleteUsers() {
    userRepository.findAll()
      .filterNot { it.email == admin.email }
      .forEach { userLifecycle.deleteUser(it) }
  }

  @ParameterizedTest
  @ValueSource(strings = ["user", "user@domain"])
  @WithMockCustomUser(roles = [ROLE_ADMIN])
  fun `when creating a user with invalid email then returns bad request`(email: String) {
    // language=JSON
    val jsonString = """{"email":"$email","password":"password"}"""

    mockMvc.post("/api/v2/users") {
      contentType = MediaType.APPLICATION_JSON
      content = jsonString
    }.andExpect {
      status { isBadRequest() }
    }
  }

  @Nested
  inner class Update {
    @Test
    @WithMockCustomUser(id = "admin", roles = [ROLE_ADMIN])
    fun `given user without roles when updating roles then roles are updated`() {
      val user = KomgaUser("user@example.org", "", false, id = "user", roleFileDownload = false, rolePageStreaming = false)
      userLifecycle.createUser(user)

      // language=JSON
      val jsonString =
        """
        {
          "roles": ["$ROLE_FILE_DOWNLOAD","$ROLE_PAGE_STREAMING"]
        }
        """.trimIndent()

      mockMvc.patch("/api/v2/users/${user.id}") {
        contentType = MediaType.APPLICATION_JSON
        content = jsonString
      }.andExpect {
        status { isNoContent() }
      }

      with(userRepository.findByIdOrNull(user.id)) {
        assertThat(this).isNotNull
        assertThat(this!!.roleFileDownload).isTrue
        assertThat(this.rolePageStreaming).isTrue
        assertThat(this.roleAdmin).isFalse
      }
    }

    @Test
    @WithMockCustomUser(id = "admin", roles = [ROLE_ADMIN])
    fun `given user with roles when updating roles then roles are updated`() {
      val user = KomgaUser("user@example.org", "", true, id = "user", roleFileDownload = true, rolePageStreaming = true)
      userLifecycle.createUser(user)

      // language=JSON
      val jsonString =
        """
        {
          "roles": []
        }
        """.trimIndent()

      mockMvc.patch("/api/v2/users/${user.id}") {
        contentType = MediaType.APPLICATION_JSON
        content = jsonString
      }.andExpect {
        status { isNoContent() }
      }

      with(userRepository.findByIdOrNull(user.id)) {
        assertThat(this).isNotNull
        assertThat(this!!.roleFileDownload).isFalse
        assertThat(this.rolePageStreaming).isFalse
        assertThat(this.roleAdmin).isFalse
      }
    }

    @Test
    @WithMockCustomUser(id = "admin", roles = [ROLE_ADMIN])
    fun `given user with library restrictions when updating available libraries then they are updated`() {
      val user = KomgaUser("user@example.org", "", false, id = "user", sharedAllLibraries = false, sharedLibrariesIds = setOf("1"))
      userLifecycle.createUser(user)

      // language=JSON
      val jsonString =
        """
        {
          "sharedLibraries": {
            "all": "false",
            "libraryIds" : ["1", "2"]
          }
        }
        """.trimIndent()

      mockMvc.patch("/api/v2/users/${user.id}") {
        contentType = MediaType.APPLICATION_JSON
        content = jsonString
      }.andExpect {
        status { isNoContent() }
      }

      with(userRepository.findByIdOrNull(user.id)) {
        assertThat(this).isNotNull
        assertThat(this!!.sharedAllLibraries).isFalse
        assertThat(this.sharedLibrariesIds).containsExactlyInAnyOrder("1", "2")
      }
    }

    @Test
    @WithMockCustomUser(id = "admin", roles = [ROLE_ADMIN])
    fun `given user without library restrictions when restricting libraries then they restrictions are updated`() {
      val user = KomgaUser("user@example.org", "", false, id = "user", sharedAllLibraries = true)
      userLifecycle.createUser(user)

      // language=JSON
      val jsonString =
        """
        {
          "sharedLibraries": {
            "all": "false",
            "libraryIds" : ["2"]
          }
        }
        """.trimIndent()

      mockMvc.patch("/api/v2/users/${user.id}") {
        contentType = MediaType.APPLICATION_JSON
        content = jsonString
      }.andExpect {
        status { isNoContent() }
      }

      with(userRepository.findByIdOrNull(user.id)) {
        assertThat(this).isNotNull
        assertThat(this!!.sharedAllLibraries).isFalse
        assertThat(this.sharedLibrariesIds).containsExactlyInAnyOrder("2")
      }
    }

    @Test
    @WithMockCustomUser(id = "admin", roles = [ROLE_ADMIN])
    fun `given user with library restrictions when removing restrictions then the restrictions are updated`() {
      val user = KomgaUser("user@example.org", "", false, id = "user", sharedAllLibraries = false, sharedLibrariesIds = setOf("2"))
      userLifecycle.createUser(user)

      // language=JSON
      val jsonString =
        """
        {
          "sharedLibraries": {
            "all": "true",
            "libraryIds": []
          }
        }
        """.trimIndent()

      mockMvc.patch("/api/v2/users/${user.id}") {
        contentType = MediaType.APPLICATION_JSON
        content = jsonString
      }.andExpect {
        status { isNoContent() }
      }

      with(userRepository.findByIdOrNull(user.id)) {
        assertThat(this).isNotNull
        assertThat(this!!.sharedAllLibraries).isTrue
        assertThat(this.sharedLibrariesIds).isEmpty()
      }
    }

    @Test
    @WithMockCustomUser(id = "admin", roles = [ROLE_ADMIN])
    fun `given user without labels restrictions when adding restrictions then restrictions are updated`() {
      val user = KomgaUser("user@example.org", "", false, id = "user")
      userLifecycle.createUser(user)

      // language=JSON
      val jsonString =
        """
        {
          "labelsAllow": ["cute", "kids"],
          "labelsExclude": ["adult"]
        }
        """.trimIndent()

      mockMvc.patch("/api/v2/users/${user.id}") {
        contentType = MediaType.APPLICATION_JSON
        content = jsonString
      }.andExpect {
        status { isNoContent() }
      }

      with(userRepository.findByIdOrNull(user.id)) {
        assertThat(this).isNotNull
        assertThat(this!!.restrictions.labelsAllow).containsExactlyInAnyOrder("cute", "kids")
        assertThat(this.restrictions.labelsExclude).containsOnly("adult")
      }
    }

    @Test
    @WithMockCustomUser(id = "admin", roles = [ROLE_ADMIN])
    fun `given user with labels restrictions when removing restrictions then restrictions are updated`() {
      val user =
        KomgaUser(
          "user@example.org",
          "",
          false,
          id = "user",
          restrictions =
            ContentRestrictions(
              labelsAllow = setOf("kids", "cute"),
              labelsExclude = setOf("adult"),
            ),
        )
      userLifecycle.createUser(user)

      // language=JSON
      val jsonString =
        """
        {
          "labelsAllow": [],
          "labelsExclude": null
        }
        """.trimIndent()

      mockMvc.patch("/api/v2/users/${user.id}") {
        contentType = MediaType.APPLICATION_JSON
        content = jsonString
      }.andExpect {
        status { isNoContent() }
      }

      with(userRepository.findByIdOrNull(user.id)) {
        assertThat(this).isNotNull
        assertThat(this!!.restrictions.labelsAllow).isEmpty()
        assertThat(this.restrictions.labelsExclude).isEmpty()
      }
    }

    @Test
    @WithMockCustomUser(id = "admin", roles = [ROLE_ADMIN])
    fun `given user without age restriction when adding restrictions then restrictions are updated`() {
      val user = KomgaUser("user@example.org", "", false, id = "user")
      userLifecycle.createUser(user)

      // language=JSON
      val jsonString =
        """
        {
          "ageRestriction": {
            "age": 12,
            "restriction": "ALLOW_ONLY"
          }
        }
        """.trimIndent()

      mockMvc.patch("/api/v2/users/${user.id}") {
        contentType = MediaType.APPLICATION_JSON
        content = jsonString
      }.andExpect {
        status { isNoContent() }
      }

      with(userRepository.findByIdOrNull(user.id)) {
        assertThat(this).isNotNull
        assertThat(this!!.restrictions.ageRestriction).isNotNull
        assertThat(this.restrictions.ageRestriction!!.age).isEqualTo(12)
        assertThat(this.restrictions.ageRestriction!!.restriction).isEqualTo(AllowExclude.ALLOW_ONLY)
      }
    }

    @Test
    @WithMockCustomUser(id = "admin", roles = [ROLE_ADMIN])
    fun `given user without age restriction when adding incorrect restrictions then bad request`() {
      val user = KomgaUser("user@example.org", "", false, id = "user")
      userLifecycle.createUser(user)

      // language=JSON
      val jsonString =
        """
        {
          "ageRestriction": {
            "age": -12,
            "restriction": "ALLOW_ONLY"
          }
        }
        """.trimIndent()

      mockMvc.patch("/api/v2/users/${user.id}") {
        contentType = MediaType.APPLICATION_JSON
        content = jsonString
      }.andExpect {
        status { isBadRequest() }
      }
    }

    @Test
    @WithMockCustomUser(id = "admin", roles = [ROLE_ADMIN])
    fun `given user with age restriction when removing restriction then restrictions are updated`() {
      val user =
        KomgaUser(
          "user@example.org",
          "",
          false,
          id = "user",
          restrictions =
            ContentRestrictions(
              ageRestriction = AgeRestriction(12, AllowExclude.ALLOW_ONLY),
            ),
        )
      userLifecycle.createUser(user)

      // language=JSON
      val jsonString =
        """
        {
          "ageRestriction": null
        }
        """.trimIndent()

      mockMvc.patch("/api/v2/users/${user.id}") {
        contentType = MediaType.APPLICATION_JSON
        content = jsonString
      }.andExpect {
        status { isNoContent() }
      }

      with(userRepository.findByIdOrNull(user.id)) {
        assertThat(this).isNotNull
        assertThat(this!!.restrictions.ageRestriction).isNull()
      }
    }

    @Test
    @WithMockCustomUser(id = "admin", roles = [ROLE_ADMIN])
    fun `given user with age restriction when changing restriction then restrictions are updated`() {
      val user =
        KomgaUser(
          "user@example.org",
          "",
          false,
          id = "user",
          restrictions =
            ContentRestrictions(
              ageRestriction = AgeRestriction(12, AllowExclude.ALLOW_ONLY),
            ),
        )
      userLifecycle.createUser(user)

      // language=JSON
      val jsonString =
        """
        {
          "ageRestriction": {
            "age": 16,
            "restriction": "EXCLUDE"
          }
        }
        """.trimIndent()

      mockMvc.patch("/api/v2/users/${user.id}") {
        contentType = MediaType.APPLICATION_JSON
        content = jsonString
      }.andExpect {
        status { isNoContent() }
      }

      with(userRepository.findByIdOrNull(user.id)) {
        assertThat(this).isNotNull
        assertThat(this!!.restrictions.ageRestriction).isNotNull
        assertThat(this.restrictions.ageRestriction!!.age).isEqualTo(16)
        assertThat(this.restrictions.ageRestriction!!.restriction).isEqualTo(AllowExclude.EXCLUDE)
      }
    }
  }

  @Nested
  inner class ApiKey {
    @AfterEach
    fun cleanup() {
      userRepository.deleteApiKeyByUserId(admin.id)
    }

    @Test
    @WithMockCustomUser(id = "admin")
    fun `given user when creating API key then it is returned in plain text`() {
      // language=JSON
      val jsonString =
        """
        {
          "comment": "test api key"
        }
        """.trimIndent()

      mockMvc.post("/api/v2/users/me/api-keys") {
        contentType = MediaType.APPLICATION_JSON
        content = jsonString
      }.andExpect {
        status { isOk() }
        jsonPath("$.userId") { value(admin.id) }
        jsonPath("$.key") { value(MatchesPattern(Regex("""[^*]+""").toPattern())) }
        jsonPath("$.comment") { value("test api key") }
      }

      with(userRepository.findApiKeyByUserId(admin.id)) {
        assertThat(this).hasSize(1)
        with(this.first()!!) {
          assertThat(this.userId).isEqualTo(admin.id)
          assertThat(this.comment).isEqualTo("test api key")
        }
      }

      mockMvc.get("/api/v2/users/me/api-keys")
        .andExpect {
          status { isOk() }
          jsonPath("$.length()") { value(1) }
          jsonPath("$[0].userId") { value(admin.id) }
          jsonPath("$[0].key") { value(MatchesPattern(Regex("""[*]+""").toPattern())) }
          jsonPath("$[0].comment") { value("test api key") }
        }
    }

    @Test
    @WithMockCustomUser(id = "admin")
    fun `given user when creating API key without comment then returns bad request`() {
      // language=JSON
      val jsonString =
        """
        {
          "comment": ""
        }
        """.trimIndent()

      mockMvc.post("/api/v2/users/me/api-keys") {
        contentType = MediaType.APPLICATION_JSON
        content = jsonString
      }.andExpect {
        status { isBadRequest() }
      }
    }

    @Test
    @WithMockCustomUser(id = "admin")
    fun `given user with api key when deleting API key then it is deleted`() {
      val apiKey = userLifecycle.createApiKey(admin, "test")!!

      mockMvc.delete("/api/v2/users/me/api-keys/${apiKey.id}")
        .andExpect {
          status { isNoContent() }
        }

      assertThat(userRepository.findApiKeyByUserId(admin.id)).isEmpty()
    }

    @Test
    @WithMockCustomUser(id = "admin")
    fun `given user with api key when deleting different API key ID then returns bad request`() {
      val apiKey = userLifecycle.createApiKey(admin, "test")!!

      mockMvc.delete("/api/v2/users/me/api-keys/abc123")
        .andExpect {
          status { isNotFound() }
        }

      assertThat(userRepository.findApiKeyByUserId(admin.id)).isNotEmpty()
    }
  }
}
