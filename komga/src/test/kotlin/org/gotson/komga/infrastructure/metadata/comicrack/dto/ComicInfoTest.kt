package org.gotson.komga.infrastructure.metadata.comicrack.dto

import com.fasterxml.jackson.dataformat.xml.XmlMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class ComicInfoTest {
  private val mapper = XmlMapper()

  @Test
  fun `given valid xml file when deserializing then properties are available`() {
    // language=XML
    val xml =
      """
      <?xml version="1.0"?>
      <ComicInfo>
        <Title>v01 - Preludes &amp; Nocturnes - 30th Anniversary Edition</Title>
        <Series>Sandman</Series>
        <Web>https://www.comixology.com/Sandman/digital-comic/727888</Web>
        <Summary>Neil Gaiman's seminal series, THE SANDMAN, celebrates its 30th anniversary with an all-new edition of THE
            SANDMAN VOL. 1: PRELUDES &amp; NOCTURNES!

             New York Times best-selling author Neil Gaiman's transcendent series THE SANDMAN is often hailed as the
            definitive Vertigo title and one of the finest achievements in graphic storytelling. Gaiman created an
            unforgettable tale of the forces that exist beyond life and death by weaving ancient mythology, folklore and
            fairy tales with his own distinct narrative vision.

             In PRELUDES &amp; NOCTURNES, an occultist attempting to capture Death to bargain for eternal life traps her
            younger brother Dream instead. After his 70 year imprisonment and eventual escape, Dream, also known as
            Morpheus, goes on a quest for his lost objects of power. On his arduous journey Morpheus encounters Lucifer,
            John Constantine, and an all-powerful madman.

             This book also includes the story "The Sound of Her Wings," which introduces us to the pragmatic and perky goth
            girl Death.

             Collects THE SANDMAN #1-8.</Summary>
        <Notes>Scraped metadata from Comixology [CMXDB727888], [RELDATE:2018-10-30]</Notes>
        <Translator>The translator</Translator>
        <Publisher>DC</Publisher>
        <Imprint>Vertigo</Imprint>
        <Count>10</Count>
        <Genre>Fantasy, Supernatural/Occult, Horror, Mature, Superhero, Mythology, Drama</Genre>
        <Tags>dark, Occult</Tags>
        <PageCount>237</PageCount>
        <LanguageISO>en</LanguageISO>
        <AgeRating>Mature 17+</AgeRating>
        <BlackAndWhite>No</BlackAndWhite>
        <Manga>No</Manga>
        <SeriesGroup>Sandman</SeriesGroup>
        <GTIN>ABC123</GTIN>
        <ScanInformation></ScanInformation>
      </ComicInfo>
      """.trimIndent()
    val comicInfo = mapper.readValue<ComicInfo>(xml)

    with(comicInfo) {
      assertThat(title).isEqualTo("v01 - Preludes & Nocturnes - 30th Anniversary Edition")
      assertThat(series).isEqualTo("Sandman")
      assertThat(web).isEqualTo("https://www.comixology.com/Sandman/digital-comic/727888")
      assertThat(summary).startsWith("Neil Gaiman's seminal series")
      assertThat(notes).isEqualTo("Scraped metadata from Comixology [CMXDB727888], [RELDATE:2018-10-30]")
      assertThat(publisher).isEqualTo("DC")
      assertThat(imprint).isEqualTo("Vertigo")
      assertThat(count).isEqualTo(10)
      assertThat(genre).isEqualTo("Fantasy, Supernatural/Occult, Horror, Mature, Superhero, Mythology, Drama")
      assertThat(tags).isEqualTo("dark, Occult")
      assertThat(pageCount).isEqualTo(237)
      assertThat(languageISO).isEqualTo("en")
      assertThat(scanInformation).isEqualTo("")
      assertThat(ageRating).isEqualTo(AgeRating.MATURE_17)
      assertThat(blackAndWhite).isEqualTo(YesNo.NO)
      assertThat(manga).isEqualTo(Manga.NO)
      assertThat(seriesGroup).isEqualTo("Sandman")
      assertThat(translator).isEqualTo("The translator")
      assertThat(gtin).isEqualTo("ABC123")
    }
  }

  @Test
  fun `given another valid xml file when deserializing then properties are available`() {
    // language=XML
    val xml =
      """
      <?xml version="1.0"?>
      <ComicInfo>
          <Title>v01 - Preludes &amp; Nocturnes - 30th Anniversary Edition</Title>
          <Series>Sandman</Series>
          <Web>https://www.comixology.com/Sandman/digital-comic/727888</Web>
          <Summary>Neil Gaiman's seminal series, THE SANDMAN, celebrates its 30th anniversary with an all-new edition of THE
              SANDMAN VOL. 1: PRELUDES &amp; NOCTURNES!

               New York Times best-selling author Neil Gaiman's transcendent series THE SANDMAN is often hailed as the
              definitive Vertigo title and one of the finest achievements in graphic storytelling. Gaiman created an
              unforgettable tale of the forces that exist beyond life and death by weaving ancient mythology, folklore and
              fairy tales with his own distinct narrative vision.

               In PRELUDES &amp; NOCTURNES, an occultist attempting to capture Death to bargain for eternal life traps her
              younger brother Dream instead. After his 70 year imprisonment and eventual escape, Dream, also known as
              Morpheus, goes on a quest for his lost objects of power. On his arduous journey Morpheus encounters Lucifer,
              John Constantine, and an all-powerful madman.

               This book also includes the story "The Sound of Her Wings," which introduces us to the pragmatic and perky goth
              girl Death.

               Collects THE SANDMAN #1-8.
          </Summary>
          <Notes>Scraped metadata from Comixology [CMXDB727888], [RELDATE:2018-10-30]</Notes>
          <Publisher>DC</Publisher>
          <Imprint>Vertigo</Imprint>
          <Genre>Fantasy, Supernatural/Occult, Horror, Mature, Superhero, Mythology, Drama</Genre>
          <PageCount>237</PageCount>
          <LanguageISO>en</LanguageISO>
          <AgeRating>MA15+</AgeRating>
          <BlackAndWhite>No</BlackAndWhite>
          <Year>2018</Year>
          <Month>10</Month>
          <Day>30</Day>
          <Manga>No</Manga>
          <SeriesGroup>Sandman</SeriesGroup>
          <ScanInformation></ScanInformation>
      </ComicInfo>
      """.trimIndent()
    val comicInfo = mapper.readValue<ComicInfo>(xml)

    with(comicInfo) {
      assertThat(title).isEqualTo("v01 - Preludes & Nocturnes - 30th Anniversary Edition")
      assertThat(series).isEqualTo("Sandman")
      assertThat(web).isEqualTo("https://www.comixology.com/Sandman/digital-comic/727888")
      assertThat(summary).startsWith("Neil Gaiman's seminal series")
      assertThat(notes).isEqualTo("Scraped metadata from Comixology [CMXDB727888], [RELDATE:2018-10-30]")
      assertThat(publisher).isEqualTo("DC")
      assertThat(imprint).isEqualTo("Vertigo")
      assertThat(count).isNull()
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
    // language=XML
    val xml =
      """
      <?xml version="1.0"?>
      <ComicInfo xmlns:xsd="http://www.w3.org/2001/XMLSchema" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
        <AgeRating>Non existent</AgeRating>
        <BlackAndWhite>Non existent</BlackAndWhite>
        <Manga>Non existent</Manga>
      </ComicInfo>
      """.trimIndent()
    val comicInfo = mapper.readValue<ComicInfo>(xml)

    with(comicInfo) {
      assertThat(ageRating).isNull()
      assertThat(blackAndWhite).isNull()
      assertThat(manga).isNull()
    }
  }

  @Test
  fun `given valid xml file with StoryArc fields when deserializing then properties are available`() {
    // language=XML
    val xml =
      """
      <?xml version="1.0"?>
      <ComicInfo>
          <StoryArc>Arc</StoryArc>
          <StoryArcNumber>2</StoryArcNumber>
      </ComicInfo>
      """.trimIndent()
    val comicInfo = mapper.readValue<ComicInfo>(xml)

    with(comicInfo) {
      assertThat(storyArc).isEqualTo("Arc")
      assertThat(storyArcNumber).isEqualTo("2")
    }
  }
}
