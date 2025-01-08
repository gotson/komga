package org.gotson.komga.interfaces.api.rest

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.security.test.context.support.WithAnonymousUser
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get

@SpringBootTest
@AutoConfigureMockMvc(printOnlyOnFailure = false)
@ActiveProfiles("test")
class ActuatorTest(
  @Autowired private val mockMvc: MockMvc,
) {
  @Test
  @WithAnonymousUser
  fun `given anonymous user when getting actuator endpoints then returns unauthorized`() {
    mockMvc
      .get("/actuator")
      .andExpect {
        status { isUnauthorized() }
      }

    mockMvc
      .get("/actuator/beans")
      .andExpect {
        status { isUnauthorized() }
      }
  }

  @Test
  @WithAnonymousUser
  fun `given anonymous user when getting actuator health endpoint then returns ok`() {
    mockMvc
      .get("/actuator/health")
      .andExpect {
        status { isOk() }
      }
  }

  @Test
  @WithMockUser
  fun `given regular user when getting actuator endpoints then returns forbidden`() {
    mockMvc
      .get("/actuator")
      .andExpect {
        status { isForbidden() }
      }

    mockMvc
      .get("/actuator/beans")
      .andExpect {
        status { isForbidden() }
      }
  }

  @Test
  @WithMockUser(roles = ["ADMIN"])
  fun `given admin user when getting actuator endpoints then returns ok`() {
    mockMvc
      .get("/actuator")
      .andExpect {
        status { isOk() }
      }

    mockMvc
      .get("/actuator/beans")
      .andExpect {
        status { isOk() }
      }
  }
}
