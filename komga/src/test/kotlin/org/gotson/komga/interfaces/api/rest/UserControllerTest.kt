package org.gotson.komga.interfaces.api.rest

import org.gotson.komga.domain.model.ROLE_ADMIN
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
import org.springframework.test.web.servlet.post

@ExtendWith(SpringExtension::class)
@SpringBootTest
@AutoConfigureMockMvc(printOnlyOnFailure = false)
@ActiveProfiles("test")
class UserControllerTest(
  @Autowired private val mockMvc: MockMvc

) {
  @ParameterizedTest
  @ValueSource(strings = ["user", "user@domain"])
  @WithMockCustomUser(roles = [ROLE_ADMIN])
  fun `when creating a user with invalid email then returns bad request`(email: String) {
    val jsonString = """{"email":"$email","password":"password"}"""

    mockMvc.post("/api/v1/users") {
      contentType = MediaType.APPLICATION_JSON
      content = jsonString
    }.andExpect {
      status { isBadRequest() }
    }
  }
}
