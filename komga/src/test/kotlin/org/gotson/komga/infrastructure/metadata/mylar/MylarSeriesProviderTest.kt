package org.gotson.komga.infrastructure.metadata.mylar

import com.fasterxml.jackson.databind.ObjectMapper
import io.mockk.every
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.gotson.komga.domain.model.SeriesMetadata
import org.gotson.komga.domain.model.makeSeries
import org.gotson.komga.infrastructure.metadata.mylar.dto.AgeRating
import org.gotson.komga.infrastructure.metadata.mylar.dto.MylarMetadata
import org.gotson.komga.infrastructure.metadata.mylar.dto.Series
import org.gotson.komga.infrastructure.metadata.mylar.dto.Status
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File
import java.nio.file.Files
import java.nio.file.Path

class MylarSeriesProviderTest {
  private val mockMapper = mockk<ObjectMapper>()

  private val mylarSeriesProvider = MylarSeriesProvider(mockMapper)

  private lateinit var series: org.gotson.komga.domain.model.Series

  @BeforeAll
  fun setupSeries(
    @TempDir dir: Path,
  ) {
    Files.createFile(dir.resolve("series.json"))
    series = makeSeries("series", url = dir.toUri().toURL())
  }

  @Test
  fun `given seriesJson when getting series metadata then metadata patch is valid`() {
    val metadata =
      MylarMetadata(
        type = "comicSeries",
        publisher = "DC",
        imprint = "Vertigo",
        name = "Sàndman",
        comicid = "12345",
        year = 1990,
        descriptionText = "Sandman comics",
        descriptionFormatted = "Sandman comics formatted",
        volume = null,
        bookType = "TPB",
        ageRating = AgeRating.ADULT,
        comicImage = "unused",
        totalIssues = 2,
        publicationRun = "unused",
        status = Status.Ended,
      )
    val root = Series(metadata)

    every { mockMapper.readValue(any<File>(), Series::class.java) } returns root

    val patch = mylarSeriesProvider.getSeriesMetadata(series)!!

    with(patch) {
      assertThat(title).isEqualTo("Sàndman")
      assertThat(titleSort).isEqualTo("Sandman")
      assertThat(status).isEqualTo(SeriesMetadata.Status.ENDED)
      assertThat(summary).isEqualTo("Sandman comics formatted")
      assertThat(readingDirection).isNull()
      assertThat(publisher).isEqualTo("DC")
      assertThat(ageRating).isEqualTo(18)
      assertThat(language).isNull()
      assertThat(genres).isNull()
      assertThat(totalBookCount).isEqualTo(2)
      assertThat(collections).isEmpty()
    }
  }

  @Test
  fun `given another seriesJson when getting series metadata then metadata patch is valid`() {
    val metadata =
      MylarMetadata(
        type = "comicSeries",
        publisher = "DC",
        imprint = "Vertigo",
        name = "Sandman",
        comicid = "12345",
        year = 1990,
        descriptionText = "Sandman comics",
        descriptionFormatted = null,
        volume = null,
        bookType = "TPB",
        ageRating = null,
        comicImage = "unused",
        totalIssues = 2,
        publicationRun = "unused",
        status = Status.Continuing,
      )
    val root = Series(metadata)

    every { mockMapper.readValue(any<File>(), Series::class.java) } returns root

    val patch = mylarSeriesProvider.getSeriesMetadata(series)!!

    with(patch) {
      assertThat(title).isEqualTo("Sandman")
      assertThat(titleSort).isEqualTo("Sandman")
      assertThat(status).isEqualTo(SeriesMetadata.Status.ONGOING)
      assertThat(summary).isEqualTo("Sandman comics")
      assertThat(readingDirection).isNull()
      assertThat(publisher).isEqualTo("DC")
      assertThat(ageRating).isNull()
      assertThat(language).isNull()
      assertThat(genres).isNull()
      assertThat(totalBookCount).isEqualTo(2)
      assertThat(collections).isEmpty()
    }
  }

  @Test
  fun `given seriesJson with volume != 1 and year when getting series metadata then metadata patch has title containing the year`() {
    val metadata =
      MylarMetadata(
        type = "comicSeries",
        publisher = "DC",
        imprint = "Vertigo",
        name = "Sandman",
        comicid = "12345",
        year = 1990,
        descriptionText = "Sandman comics",
        descriptionFormatted = "Sandman comics formatted",
        volume = 2,
        bookType = "TPB",
        ageRating = AgeRating.ADULT,
        comicImage = "unused",
        totalIssues = 2,
        publicationRun = "unused",
        status = Status.Ended,
      )
    val root = Series(metadata)

    every { mockMapper.readValue(any<File>(), Series::class.java) } returns root

    val patch = mylarSeriesProvider.getSeriesMetadata(series)!!

    with(patch) {
      assertThat(title).isEqualTo("Sandman (1990)")
      assertThat(titleSort).isEqualTo("Sandman (1990)")
    }
  }

  @Test
  fun `given seriesJson with volume == 1 and year when getting series metadata then metadata patch has title not containing the year`() {
    val metadata =
      MylarMetadata(
        type = "comicSeries",
        publisher = "DC",
        imprint = "Vertigo",
        name = "Sandman",
        comicid = "12345",
        year = 1990,
        descriptionText = "Sandman comics",
        descriptionFormatted = "Sandman comics formatted",
        volume = 1,
        bookType = "TPB",
        ageRating = AgeRating.ADULT,
        comicImage = "unused",
        totalIssues = 2,
        publicationRun = "unused",
        status = Status.Ended,
      )
    val root = Series(metadata)

    every { mockMapper.readValue(any<File>(), Series::class.java) } returns root

    val patch = mylarSeriesProvider.getSeriesMetadata(series)!!

    with(patch) {
      assertThat(title).isEqualTo("Sandman")
      assertThat(titleSort).isEqualTo("Sandman")
    }
  }

  @Test
  fun `given seriesJson with volume == null and year when getting series metadata then metadata patch has title not containing the year`() {
    val metadata =
      MylarMetadata(
        type = "comicSeries",
        publisher = "DC",
        imprint = "Vertigo",
        name = "Sandman",
        comicid = "12345",
        year = 1990,
        descriptionText = "Sandman comics",
        descriptionFormatted = "Sandman comics formatted",
        volume = null,
        bookType = "TPB",
        ageRating = AgeRating.ADULT,
        comicImage = "unused",
        totalIssues = 2,
        publicationRun = "unused",
        status = Status.Ended,
      )
    val root = Series(metadata)

    every { mockMapper.readValue(any<File>(), Series::class.java) } returns root

    val patch = mylarSeriesProvider.getSeriesMetadata(series)!!

    with(patch) {
      assertThat(title).isEqualTo("Sandman")
      assertThat(titleSort).isEqualTo("Sandman")
    }
  }
}
