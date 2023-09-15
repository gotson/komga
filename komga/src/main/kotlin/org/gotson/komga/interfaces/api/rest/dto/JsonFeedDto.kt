package org.gotson.komga.interfaces.api.rest.dto

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import java.time.OffsetDateTime

@JsonIgnoreProperties(ignoreUnknown = true)
data class JsonFeedDto(
  val version: String,
  val title: String,
  @field:JsonProperty("home_page_url")
  val homePageUrl: String?,
  val description: String?,
  val items: List<ItemDto> = emptyList(),
) {
  data class ItemDto(
    val id: String,
    val url: String?,
    val title: String?,
    val summary: String?,
    @field:JsonProperty("content_html")
    val contentHtml: String?,
    @field:JsonProperty("date_modified")
    val dateModified: OffsetDateTime?,
    val author: AuthorDto?,
    val tags: Set<String> = emptySet(),
    @field:JsonProperty("_komga")
    val komgaExtension: KomgaExtensionDto?,
  )

  data class AuthorDto(
    val name: String?,
    val url: String?,
  )

  data class KomgaExtensionDto(
    val read: Boolean,
  )
}
