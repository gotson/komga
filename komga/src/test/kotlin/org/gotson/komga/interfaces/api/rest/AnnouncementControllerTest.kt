package org.gotson.komga.interfaces.api.rest

import com.ninjasquad.springmockk.SpykBean
import io.mockk.every
import io.mockk.verify
import org.gotson.komga.interfaces.api.rest.dto.JsonFeedDto
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import java.time.LocalDate
import java.time.OffsetDateTime
import java.time.ZoneOffset

@SpringBootTest
@AutoConfigureMockMvc(printOnlyOnFailure = false)
class AnnouncementControllerTest(
  @Autowired private val mockMvc: MockMvc,
) {
  @SpykBean
  private lateinit var announcementController: AnnouncementController

  private val mockFeed =
    JsonFeedDto(
      "https://jsonfeed.org/version/1",
      "Announcements",
      "https://komga.org/blog",
      "Latest Komga announcements",
      listOf(
        JsonFeedDto.ItemDto(
          "https://komga.org/blog/prepare-v1",
          "https://komga.org/blog/prepare-v1",
          "Prepare for v1.0.0",
          "The future v1.0.0 will bring some breaking changes, this guide will help you to prepare for the next major version.",
          """
          You can still change the port <a href="/docs/installation/configuration#server_port--serverport-port">through configuration</a>
          Another link <a href="/blog/post/">here</a>.
          A normal <a href="https://google.com">link</a>.
          """.trimIndent(),
          OffsetDateTime.of(
            LocalDate.of(2023, 3, 21).atStartOfDay(),
            ZoneOffset.UTC,
          ),
          JsonFeedDto.AuthorDto("gotson", "https://github.com/gotson"),
          setOf(
            "breaking change",
            "upgrade",
            "komga",
          ),
          null,
        ),
      ),
    )

  @Test
  @WithMockCustomUser(roles = ["ADMIN"])
  fun `when getting announcements multiple times then the server announcements are only fetched once`() {
    every { announcementController.fetchWebsiteAnnouncements() } returns mockFeed

    repeat(2) {
      mockMvc
        .get("/api/v1/announcements")
        .andExpect {
          status { isOk() }
          jsonPath("$.items.length()") { value(1) }
          jsonPath("$.items[0].date_modified") { value("2023-03-21T00:00:00Z") }
        }
    }

    verify(exactly = 1) { announcementController.fetchWebsiteAnnouncements() }
  }
}
