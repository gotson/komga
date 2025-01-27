package org.gotson.komga.infrastructure.metadata.mylar.dto

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.exc.MismatchedInputException
import com.fasterxml.jackson.module.kotlin.readValue
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.catchThrowable
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest
class SeriesTest(
  @Autowired private val mapper: ObjectMapper,
) {
  @Test
  fun `given valid json file when deserializing then properties are available`() {
    // language=JSON
    val json =
      """
      {
        "version": "1.0.1",
        "metadata": {
          "type": "comicSeries",
          "publisher": "DC Comics",
          "imprint": null,
          "name": "American Vampire 1976",
          "cid": 130865,
          "year": 2020,
          "description_text": "Nine issue mini-series, the closing chapter of American Vampire",
          "description_formatted": "Nine issue mini-series, the closing chapter of American Vampire",
          "volume": null,
          "booktype": "Print",
          "age_rating": "Adult",
          "collects": null,
          "ComicImage": "https://comicvine.gamespot.com/a/uploads/scale_large/6/67663/7603293-01.jpg",
          "total_issues": 9,
          "publication_run": "December 2020 - Present",
          "status": "Continuing"
        }
      }
      """.trimIndent()
    val seriesJson = mapper.readValue<Series>(json)

    assertThat(seriesJson.metadata).isNotNull
    with(seriesJson.metadata) {
      assertThat(type).isEqualTo("comicSeries")
      assertThat(publisher).isEqualTo("DC Comics")
      assertThat(imprint).isNull()
      assertThat(name).isEqualTo("American Vampire 1976")
      assertThat(comicid).isEqualTo("130865")
      assertThat(year).isEqualTo(2020)
      assertThat(descriptionText).isEqualTo("Nine issue mini-series, the closing chapter of American Vampire")
      assertThat(descriptionFormatted).isEqualTo("Nine issue mini-series, the closing chapter of American Vampire")
      assertThat(volume).isNull()
      assertThat(bookType).isEqualTo("Print")
      assertThat(ageRating).isEqualTo(AgeRating.ADULT)
      assertThat(comicImage).isEqualTo("https://comicvine.gamespot.com/a/uploads/scale_large/6/67663/7603293-01.jpg")
      assertThat(totalIssues).isEqualTo(9)
      assertThat(publicationRun).isEqualTo("December 2020 - Present")
      assertThat(status).isEqualTo(Status.Continuing)
    }
  }

  @Test
  fun `given another valid json file when deserializing then properties are available`() {
    // language=JSON
    val json =
      """
      {
        "version": "1.0.1",
        "metadata": {
          "type": "comicSeries",
          "publisher": "IDW Publishing",
          "imprint": null,
          "name": "Usagi Yojimbo",
          "comicid": 119731,
          "year": 2019,
          "description_text": null,
          "description_formatted": null,
          "volume": 4,
          "booktype": "Print",
          "age_rating": null,
          "collects": null,
          "comic_image": "https://comicvine1.cbsistatic.com/uploads/scale_large/6/67663/6974029-01a.jpg",
          "total_issues": 20,
          "publication_run": "June 2019 - Present",
          "status": "Ended"
        }
      }
      """.trimIndent()
    val seriesJson = mapper.readValue<Series>(json)

    assertThat(seriesJson.metadata).isNotNull
    with(seriesJson.metadata) {
      assertThat(type).isEqualTo("comicSeries")
      assertThat(publisher).isEqualTo("IDW Publishing")
      assertThat(imprint).isNull()
      assertThat(name).isEqualTo("Usagi Yojimbo")
      assertThat(comicid).isEqualTo("119731")
      assertThat(year).isEqualTo(2019)
      assertThat(descriptionText).isNull()
      assertThat(descriptionFormatted).isNull()
      assertThat(volume).isEqualTo(4)
      assertThat(bookType).isEqualTo("Print")
      assertThat(ageRating).isNull()
      assertThat(comicImage).isEqualTo("https://comicvine1.cbsistatic.com/uploads/scale_large/6/67663/6974029-01a.jpg")
      assertThat(totalIssues).isEqualTo(20)
      assertThat(publicationRun).isEqualTo("June 2019 - Present")
      assertThat(status).isEqualTo(Status.Ended)
    }
  }

  @Test
  fun `given yet another valid json file when deserializing then properties are available`() {
    // language=JSON
    val json =
      """
      {
        "version": "1.0.1",
        "metadata": {
          "type": "comicSeries",
          "publisher": "Kodansha Comics USA",
          "imprint": null,
          "name": "Vinland Saga",
          "comicid": 69157,
          "year": 2013,
          "description_text": "English translation of Vinland Saga (ヴィンランド・サガ).Vinland Saga is the first series from Kodansha Comics USA to be released in hardcovers, each collection collects and translates two volumes of the original Japanese manga",
          "description_formatted": "English translation of Vinland Saga (ヴィンランド・サガ).Vinland Saga is the first series from Kodansha Comics USA to be released in hardcovers, each collection collects and translates two volumes of the original Japanese manga",
          "volume": null,
          "booktype": "Print",
          "age_rating": null,
          "collects": null,
          "ComicImage": "https://comicvine.gamespot.com/a/uploads/scale_large/6/67663/3439178-01.jpg",
          "total_issues": 12,
          "publication_run": "November 2013 - December 2021",
          "status": "Ended"
        }
      }
      """.trimIndent()
    val seriesJson = mapper.readValue<Series>(json)

    assertThat(seriesJson.metadata).isNotNull
    with(seriesJson.metadata) {
      assertThat(type).isEqualTo("comicSeries")
      assertThat(publisher).isEqualTo("Kodansha Comics USA")
      assertThat(imprint).isNull()
      assertThat(name).isEqualTo("Vinland Saga")
      assertThat(comicid).isEqualTo("69157")
      assertThat(year).isEqualTo(2013)
      assertThat(descriptionText).isEqualTo("English translation of Vinland Saga (ヴィンランド・サガ).Vinland Saga is the first series from Kodansha Comics USA to be released in hardcovers, each collection collects and translates two volumes of the original Japanese manga")
      assertThat(descriptionFormatted).isEqualTo("English translation of Vinland Saga (ヴィンランド・サガ).Vinland Saga is the first series from Kodansha Comics USA to be released in hardcovers, each collection collects and translates two volumes of the original Japanese manga")
      assertThat(volume).isNull()
      assertThat(bookType).isEqualTo("Print")
      assertThat(ageRating).isNull()
      assertThat(comicImage).isEqualTo("https://comicvine.gamespot.com/a/uploads/scale_large/6/67663/3439178-01.jpg")
      assertThat(totalIssues).isEqualTo(12)
      assertThat(publicationRun).isEqualTo("November 2013 - December 2021")
      assertThat(status).isEqualTo(Status.Ended)
    }
  }

  @Test
  fun `given invalid json file missing year when deserializing then it fails`() {
    // language=JSON
    val json =
      """
      {
        "version": "1.0.1",
        "metadata": {
          "type": "comicSeries",
          "publisher": "IDW Publishing",
          "imprint": null,
          "name": "Usagi Yojimbo",
          "comicid": 119731,
          "description_text": null,
          "description_formatted": null,
          "volume": 4,
          "booktype": "Print",
          "age_rating": null,
          "collects": null,
          "ComicImage": "https://comicvine1.cbsistatic.com/uploads/scale_large/6/67663/6974029-01a.jpg",
          "total_issues": 20,
          "publication_run": "June 2019 - Present",
          "status": "Ended"
        }
      }
      """.trimIndent()
    val thrown = catchThrowable { mapper.readValue<Series>(json) }

    assertThat(thrown).isInstanceOf(MismatchedInputException::class.java)
  }

  @Test
  fun `given invalid json file missing publisher when deserializing then it fails`() {
    // language=JSON
    val json =
      """
      {
        "version": "1.0.1",
        "metadata": {
          "type": "comicSeries",
          "imprint": null,
          "name": "Usagi Yojimbo",
          "comicid": 119731,
          "year": 2019,
          "description_text": null,
          "description_formatted": null,
          "volume": 4,
          "booktype": "Print",
          "age_rating": null,
          "collects": null,
          "ComicImage": "https://comicvine1.cbsistatic.com/uploads/scale_large/6/67663/6974029-01a.jpg",
          "total_issues": 20,
          "publication_run": "June 2019 - Present",
          "status": "Ended"
        }
      }
      """.trimIndent()
    val thrown = catchThrowable { mapper.readValue<Series>(json) }

    assertThat(thrown).isInstanceOf(MismatchedInputException::class.java)
  }

  @Test
  fun `given invalid json file missing status when deserializing then it fails`() {
    // language=JSON
    val json =
      """
      {
        "version": "1.0.1",
        "metadata": {
          "type": "comicSeries",
          "publisher": "IDW Publishing",
          "imprint": null,
          "name": "Usagi Yojimbo",
          "comicid": 119731,
          "year": 2019,
          "description_text": null,
          "description_formatted": null,
          "volume": 4,
          "booktype": "Print",
          "age_rating": null,
          "collects": null,
          "ComicImage": "https://comicvine1.cbsistatic.com/uploads/scale_large/6/67663/6974029-01a.jpg",
          "total_issues": 20,
          "publication_run": "June 2019 - Present"
        }
      }

      """.trimIndent()
    val thrown = catchThrowable { mapper.readValue<Series>(json) }

    assertThat(thrown).isInstanceOf(MismatchedInputException::class.java)
  }
}
