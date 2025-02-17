package org.gotson.komga.interfaces.api.rest

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.HttpHeaders
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get

@SpringBootTest
@AutoConfigureMockMvc(printOnlyOnFailure = false)
class FontsControllerTest(
  @Autowired private val mockMvc: MockMvc,
) {
  @Test
  @WithMockCustomUser
  fun `when getting font families then the embedded fonts are returned`() {
    mockMvc
      .get("/api/v1/fonts/families")
      .andExpect {
        status { isOk() }
        jsonPath("$.length()") { value(1) }
        jsonPath("$[0]") { value("OpenDyslexic") }
      }
  }

  @Test
  @WithMockCustomUser
  fun `when getting font file then the content type headers are correct`() {
    mockMvc
      .get("/api/v1/fonts/resource/OpenDyslexic/OpenDyslexic-Bold.woff")
      .andExpect {
        status { isOk() }
        header {
          string(HttpHeaders.CONTENT_TYPE, "font/woff")
        }
      }
  }

  @Test
  @WithMockCustomUser
  fun `when getting font css file then the content is correct`() {
    mockMvc
      .get("/api/v1/fonts/resource/OpenDyslexic/css")
      .andExpect {
        status { isOk() }
        header {
          string(HttpHeaders.CONTENT_TYPE, "text/css")
        }
        content {
          string(
            """
            @font-face {
                font-family: 'OpenDyslexic';
                src: url('OpenDyslexic-Bold-Italic.woff') format('woff'),url('OpenDyslexic-Bold-Italic.woff2') format('woff2');
                font-weight: bold;
                font-style: italic;
            }

            @font-face {
                font-family: 'OpenDyslexic';
                src: url('OpenDyslexic-Bold.woff') format('woff'),url('OpenDyslexic-Bold.woff2') format('woff2');
                font-weight: bold;
                font-style: normal;
            }

            @font-face {
                font-family: 'OpenDyslexic';
                src: url('OpenDyslexic-Italic.woff') format('woff'),url('OpenDyslexic-Italic.woff2') format('woff2');
                font-weight: normal;
                font-style: italic;
            }

            @font-face {
                font-family: 'OpenDyslexic';
                src: url('OpenDyslexic-Regular.woff') format('woff'),url('OpenDyslexic-Regular.woff2') format('woff2');
                font-weight: normal;
                font-style: normal;
            }

            """.trimIndent(),
          )
        }
      }
  }
}
