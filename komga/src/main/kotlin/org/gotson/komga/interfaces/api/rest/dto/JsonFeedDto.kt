package org.gotson.komga.interfaces.api.rest.dto

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.v3.oas.annotations.media.Schema
import java.time.OffsetDateTime

@JsonIgnoreProperties(ignoreUnknown = true)
@Schema(
  example =
    //language=JSON
    """
  {
  "version": "https://jsonfeed.org/version/1",
  "title": "Announcements",
  "home_page_url": "https://komga.org/blog",
  "description": "Latest Komga announcements",
  "items": [
    {
      "id": "https://komga.org/blog/ebook-drop2",
      "url": "https://komga.org/blog/ebook-drop2",
      "title": "eBook drop 2",
      "summary": "Version 1.9.0 contains the second feature drop for Ebooks support.",
      "content_html": "<p>A longer text…</p>",
      "date_modified": "2023-12-15T00:00:00Z",
      "author": {
        "name": "gotson",
        "url": "https://github.com/gotson"
      },
      "tags": [
        "upgrade",
        "komga"
      ],
      "_komga": {
        "read": false
      }
    },
    {
      "id": "https://komga.org/blog/ebook-support",
      "url": "https://komga.org/blog/ebook-support",
      "title": "eBook support",
      "summary": "Version 1.8.0 is bringing a long awaited feature: proper eBook support!",
      "content_html": "<p>A longer text…</p>",
      "date_modified": "2023-11-29T00:00:00Z",
      "author": {
        "name": "gotson",
        "url": "https://github.com/gotson"
      },
      "tags": [
        "upgrade",
        "komga"
      ],
      "_komga": {
        "read": true
      }
    }
  ]
}
""",
)
data class JsonFeedDto(
  @Schema(description = "URL of the version of the format the feed uses", example = "https://jsonfeed.org/version/1")
  val version: String,
  @Schema(description = "Name of the feed", example = "Announcements")
  val title: String,
  @field:JsonProperty("home_page_url")
  @Schema(description = "URL of the resource that the feed describes", example = "https://komga.org/blog")
  val homePageUrl: String?,
  @Schema(description = "Provides more detail on what the feed is about", example = "Latest Komga announcements")
  val description: String?,
  val items: List<ItemDto> = emptyList(),
) {
  data class ItemDto(
    @Schema(description = "Unique for that item for that feed over time", example = "https://komga.org/blog/ebook-drop2")
    val id: String,
    @Schema(description = "URL of the resource described by the item", example = "https://komga.org/blog/ebook-drop2")
    val url: String?,
    @Schema(description = "Plain text title", example = "eBook drop 2")
    val title: String?,
    @Schema(description = "A plain text sentence or two describing the item", example = "Version 1.9.0 contains the second feature drop for Ebooks support.")
    val summary: String?,
    @field:JsonProperty("content_html")
    @Schema(description = "HTML of the item", example = "<p>A longer text…</p>")
    val contentHtml: String?,
    @field:JsonProperty("date_modified")
    @Schema(description = "Modification date in RFC 3339 format", example = "2023-12-15T00:00:00Z")
    val dateModified: OffsetDateTime?,
    @Schema(description = "Author of the item")
    val author: ItemAuthorDto?,
    @Schema(description = "Tags describing the item", examples = ["upgrade", "komga"])
    val tags: Set<String> = emptySet(),
    @field:JsonProperty("_komga")
    @Schema(description = "Additional fields for the item")
    val komgaExtension: KomgaExtensionDto?,
  )

  data class ItemAuthorDto(
    @Schema(description = "Author's name", example = "gotson")
    val name: String?,
    @Schema(description = "URL of a site owned by the author", example = "https://github.com/gotson")
    val url: String?,
  )

  data class KomgaExtensionDto(
    @Schema(description = "Whether the current item has been marked read by the current user", example = "false")
    val read: Boolean,
  )
}
