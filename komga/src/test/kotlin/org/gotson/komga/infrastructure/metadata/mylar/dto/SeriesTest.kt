package org.gotson.komga.infrastructure.metadata.mylar.dto

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.core.io.ClassPathResource

class SeriesTest {

  private val mapper = ObjectMapper().registerKotlinModule()

  @Test
  fun `given valid json file when deserializing then properties are available`() {
    val file = ClassPathResource("mylar/series.json")
    val seriesJson = mapper.readValue<Series>(file.url)

    assertThat(seriesJson.metadata).hasSize(1)
    with(seriesJson.metadata.first()) {
      assertThat(type).isEqualTo("comicSeries")
      assertThat(publisher).isEqualTo("DC Comics")
      assertThat(imprint).isNull()
      assertThat(name).isEqualTo("American Vampire 1976")
      assertThat(comicId).isEqualTo("130865")
      assertThat(year).isEqualTo(2020)
      assertThat(descriptionText).isEqualTo("Nine issue mini-series, the closing chapter of American Vampire")
      assertThat(descriptionFormatted).isEqualTo("Nine issue mini-series, the closing chapter of American Vampire")
      assertThat(volume).isNull()
      assertThat(bookType).isEqualTo("Print")
      assertThat(comicImage).isEqualTo("https://comicvine.gamespot.com/a/uploads/scale_large/6/67663/7603293-01.jpg")
      assertThat(totalIssues).isEqualTo(8)
      assertThat(publicationRun).isEqualTo("December 2020 - Present")
      assertThat(status).isEqualTo(Status.Continuing)
    }
  }

  @Test
  fun `given another valid json file when deserializing then properties are available`() {
    val file = ClassPathResource("mylar/series1.json")
    val seriesJson = mapper.readValue<Series>(file.url)

    assertThat(seriesJson.metadata).hasSize(1)
    with(seriesJson.metadata.first()) {
      assertThat(type).isEqualTo("comicSeries")
      assertThat(publisher).isEqualTo("IDW Publishing")
      assertThat(imprint).isNull()
      assertThat(name).isEqualTo("Usagi Yojimbo")
      assertThat(comicId).isEqualTo("119731")
      assertThat(year).isEqualTo(2019)
      assertThat(descriptionText).isNull()
      assertThat(descriptionFormatted).isNull()
      assertThat(volume).isEqualTo("v4")
      assertThat(bookType).isEqualTo("Print")
      assertThat(comicImage).isEqualTo("https://comicvine1.cbsistatic.com/uploads/scale_large/6/67663/6974029-01a.jpg")
      assertThat(totalIssues).isEqualTo(19)
      assertThat(publicationRun).isEqualTo("June 2019 - Present")
      assertThat(status).isEqualTo(Status.Ended)
    }
  }
}
