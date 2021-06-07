package org.gotson.komga.infrastructure.metadata.comicrack.dto

import com.fasterxml.jackson.dataformat.xml.XmlMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.core.io.ClassPathResource

class ComicInfoTest {

  private val mapper = XmlMapper()

  @Test
  fun `given valid xml file when deserializing then properties are available`() {
    val file = ClassPathResource("comicrack/ComicInfo.xml")
    val comicInfo = mapper.readValue<ComicInfo>(file.url)

    with(comicInfo) {
      assertThat(title).isEqualTo("v01 - Preludes & Nocturnes - 30th Anniversary Edition")
      assertThat(series).isEqualTo("Sandman")
      assertThat(web).isEqualTo("https://www.comixology.com/Sandman/digital-comic/727888")
      assertThat(summary).startsWith("Neil Gaiman's seminal series")
      assertThat(notes).isEqualTo("Scraped metadata from Comixology [CMXDB727888], [RELDATE:2018-10-30]")
      assertThat(publisher).isEqualTo("DC")
      assertThat(imprint).isEqualTo("Vertigo")
      assertThat(genre).isEqualTo("Fantasy, Supernatural/Occult, Horror, Mature, Superhero, Mythology, Drama")
      assertThat(pageCount).isEqualTo(237)
      assertThat(languageISO).isEqualTo("en")
      assertThat(scanInformation).isEqualTo("")
      assertThat(ageRating).isEqualTo(AgeRating.MATURE_17)
      assertThat(blackAndWhite).isEqualTo(YesNo.NO)
      assertThat(manga).isEqualTo(Manga.NO)
      assertThat(seriesGroup).isEqualTo("Sandman")
    }
  }

  @Test
  fun `given another valid xml file when deserializing then properties are available`() {
    val file = ClassPathResource("comicrack/ComicInfo2.xml")
    val comicInfo = mapper.readValue<ComicInfo>(file.url)

    with(comicInfo) {
      assertThat(title).isEqualTo("v01 - Preludes & Nocturnes - 30th Anniversary Edition")
      assertThat(series).isEqualTo("Sandman")
      assertThat(web).isEqualTo("https://www.comixology.com/Sandman/digital-comic/727888")
      assertThat(summary).startsWith("Neil Gaiman's seminal series")
      assertThat(notes).isEqualTo("Scraped metadata from Comixology [CMXDB727888], [RELDATE:2018-10-30]")
      assertThat(publisher).isEqualTo("DC")
      assertThat(imprint).isEqualTo("Vertigo")
      assertThat(genre).isEqualTo("Fantasy, Supernatural/Occult, Horror, Mature, Superhero, Mythology, Drama")
      assertThat(pageCount).isEqualTo(237)
      assertThat(languageISO).isEqualTo("en")
      assertThat(scanInformation).isEqualTo("")
      assertThat(ageRating).isEqualTo(AgeRating.MA_15)
      assertThat(blackAndWhite).isEqualTo(YesNo.NO)
      assertThat(manga).isEqualTo(Manga.NO)
      assertThat(seriesGroup).isEqualTo("Sandman")
      assertThat(year).isEqualTo(2018)
      assertThat(month).isEqualTo(10)
      assertThat(day).isEqualTo(30)
    }
  }

  @Test
  fun `given incorrect enum values when deserializing then it is ignored`() {
    val file = ClassPathResource("comicrack/InvalidEnumValues.xml")
    val comicInfo = mapper.readValue<ComicInfo>(file.url)

    with(comicInfo) {
      assertThat(ageRating).isNull()
      assertThat(blackAndWhite).isNull()
      assertThat(manga).isNull()
    }
  }
}
