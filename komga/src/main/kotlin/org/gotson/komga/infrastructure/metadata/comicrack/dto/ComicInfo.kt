package org.gotson.komga.infrastructure.metadata.comicrack.dto

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import jakarta.xml.bind.annotation.XmlSchemaType

@JsonIgnoreProperties(ignoreUnknown = true)
class ComicInfo {
  @JsonProperty(value = "Title")
  var title: String? = null

  @JsonProperty(value = "Series")
  var series: String? = null

  @JsonProperty(value = "Number")
  var number: String? = null

  @JsonProperty(value = "Count")
  var count: Int? = null

  @JsonProperty(value = "Volume")
  var volume: Int? = null

  @JsonProperty(value = "AlternateSeries")
  var alternateSeries: String? = null

  @JsonProperty(value = "AlternateNumber")
  var alternateNumber: String? = null

  @JsonProperty(value = "AlternateCount")
  var alternateCount: Int? = null

  @JsonProperty(value = "Summary")
  var summary: String? = null

  @JsonProperty(value = "Notes")
  var notes: String? = null

  @JsonProperty(value = "Year")
  var year: Int? = null

  @JsonProperty(value = "Month")
  var month: Int? = null

  @JsonProperty(value = "Day")
  var day: Int? = null

  @JsonProperty(value = "Writer")
  var writer: String? = null

  @JsonProperty(value = "Penciller")
  var penciller: String? = null

  @JsonProperty(value = "Inker")
  var inker: String? = null

  @JsonProperty(value = "Colorist")
  var colorist: String? = null

  @JsonProperty(value = "Letterer")
  var letterer: String? = null

  @JsonProperty(value = "CoverArtist")
  var coverArtist: String? = null

  @JsonProperty(value = "Editor")
  var editor: String? = null

  @JsonProperty(value = "Translator")
  var translator: String? = null

  @JsonProperty(value = "Publisher")
  var publisher: String? = null

  @JsonProperty(value = "Imprint")
  var imprint: String? = null

  @JsonProperty(value = "Genre")
  var genre: String? = null

  @JsonProperty(value = "Tags")
  var tags: String? = null

  @JsonProperty(value = "Web")
  var web: String? = null

  @JsonProperty(value = "PageCount")
  var pageCount: Int? = null

  @JsonProperty(value = "LanguageISO")
  var languageISO: String? = null

  @JsonProperty(value = "Format")
  var format: String? = null

  @JsonProperty(value = "BlackAndWhite", defaultValue = "Unknown")
  @XmlSchemaType(name = "string")
  var blackAndWhite: YesNo? = null

  @JsonProperty(value = "Manga", defaultValue = "Unknown")
  @XmlSchemaType(name = "string")
  var manga: Manga? = null

  @JsonProperty(value = "Characters")
  var characters: String? = null

  @JsonProperty(value = "Teams")
  var teams: String? = null

  @JsonProperty(value = "Locations")
  var locations: String? = null

  @JsonProperty(value = "ScanInformation")
  var scanInformation: String? = null

  @JsonProperty(value = "StoryArc")
  var storyArc: String? = null

  @JsonProperty(value = "StoryArcNumber")
  var storyArcNumber: String? = null

  @JsonProperty(value = "SeriesGroup")
  var seriesGroup: String? = null

  @JsonProperty(value = "AgeRating", defaultValue = "Unknown")
  var ageRating: AgeRating? = null

  @JsonProperty(value = "GTIN")
  var gtin: String? = null
}
