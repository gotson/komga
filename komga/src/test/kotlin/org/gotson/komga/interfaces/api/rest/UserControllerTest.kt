package org.gotson.komga.interfaces.api.rest

import org.assertj.core.api.Assertions.assertThat
import org.gotson.komga.domain.model.KomgaUser
import org.gotson.komga.domain.model.ROLE_ADMIN
import org.gotson.komga.domain.model.ROLE_FILE_DOWNLOAD
import org.gotson.komga.domain.model.ROLE_PAGE_STREAMING
import org.gotson.komga.domain.model.makeLibrary
import org.gotson.komga.domain.persistence.KomgaUserRepository
import org.gotson.komga.domain.persistence.LibraryRepository
import org.gotson.komga.domain.service.KomgaUserLifecycle
import org.gotson.komga.domain.service.LibraryLifecycle
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.patch
import org.springframework.test.web.servlet.post

@ExtendWith(SpringExtension::class)
@SpringBootTest
@AutoConfigureMockMvc(printOnlyOnFailure = false)
@ActiveProfiles("test")
class UserControllerTest(
  @Autowired private val mockMvc: MockMvc,
  @Autowired private val libraryRepository: LibraryRepository,
  @Autowired private val libraryLifecycle: LibraryLifecycle,
  @Autowired private val userRepository: KomgaUserRepository,
  @Autowired private val userLifecycle: KomgaUserLifecycle,
) {
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

      val jsonString = """
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

      val jsonString = """
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

      val jsonString = """
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

      val jsonString = """
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
    fun `given user with library restrictions when removing restrictions then they restrictions are updated`() {
      val user = KomgaUser("user@example.org", "", false, id = "user", sharedAllLibraries = false, sharedLibrariesIds = setOf("2"))
      userLifecycle.createUser(user)

      val jsonString = """
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
  }
}
