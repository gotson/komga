package org.gotson.komga.infrastructure.metadata.comicinfo.dto

import com.fasterxml.jackson.dataformat.xml.XmlMapper
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.io.ClassPathResource
import org.springframework.core.io.ResourceLoader

class ComicInfoTest{

  @Test
  fun `given valid xml file when deserializing then properties are available`() {
    val file = ClassPathResource("comicinfo/ComicInfo.xml")
    val mapper = XmlMapper()
    val comicInfo = mapper.readValue(file.url, ComicInfo::class.java)

    with(comicInfo){
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
    }
  }

  @Test
  fun `given incorrect enum values when deserializing then it is ignored`() {
    val file = ClassPathResource("comicinfo/InvalidEnumValues.xml")
    val mapper = XmlMapper()
    val comicInfo = mapper.readValue(file.url, ComicInfo::class.java)

    with(comicInfo){
      assertThat(ageRating).isNull()
      assertThat(blackAndWhite).isNull()
      assertThat(manga).isNull()
    }
  }
}
