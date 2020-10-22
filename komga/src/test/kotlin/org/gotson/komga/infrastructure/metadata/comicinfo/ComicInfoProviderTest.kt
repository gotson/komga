package org.gotson.komga.infrastructure.metadata.comicinfo

import com.fasterxml.jackson.dataformat.xml.XmlMapper
import io.mockk.every
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.gotson.komga.domain.model.Media
import org.gotson.komga.domain.model.SeriesMetadata
import org.gotson.komga.domain.model.makeBook
import org.gotson.komga.domain.service.BookAnalyzer
import org.gotson.komga.infrastructure.metadata.comicinfo.dto.AgeRating
import org.gotson.komga.infrastructure.metadata.comicinfo.dto.ComicInfo
import org.gotson.komga.infrastructure.metadata.comicinfo.dto.Manga
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.time.LocalDate

class ComicInfoProviderTest {

  private val mockMapper = mockk<XmlMapper>()
  private val mockAnalyzer = mockk<BookAnalyzer>().also {
    every { it.getFileContent(any(), "ComicInfo.xml") } returns ByteArray(0)
  }

  private val comicInfoProvider = ComicInfoProvider(mockMapper, mockAnalyzer)

  private val book = makeBook("book")
  private val media = Media(
    status = Media.Status.READY,
    mediaType = "application/zip",
    files = listOf("ComicInfo.xml")
  )

  @Nested
  inner class Book {

    @Test
    fun `given comicInfo when getting book metadata then metadata patch is valid`() {
      val comicInfo = ComicInfo().apply {
        title = "title"
        summary = "summary"
        number = "010"
        year = 2020
        month = 2
        alternateSeries = "story arc"
        alternateNumber = "5"
        storyArc = "one, two, three"
      }

      every { mockMapper.readValue(any<ByteArray>(), ComicInfo::class.java) } returns comicInfo

      val patch = comicInfoProvider.getBookMetadataFromBook(book, media)

      with(patch!!) {
        assertThat(title).isEqualTo("title")
        assertThat(summary).isEqualTo("summary")
        assertThat(number).isEqualTo("010")
        assertThat(numberSort).isEqualTo(10F)
        assertThat(releaseDate).isEqualTo(LocalDate.of(2020, 2, 1))
        with(readLists) {
          assertThat(this).hasSize(4)
          assertThat(this.map { it.name }).containsExactly("story arc", "one", "two", "three")
          this.first { it.number != null }.let {
            assertThat(it.name).isEqualTo("story arc")
            assertThat(it.number).isEqualTo(5)
          }
        }
      }
    }

    @Test
    fun `given comicInfo with blank values when getting series metadata then blank values are omitted`() {
      val comicInfo = ComicInfo().apply {
        title = ""
        summary = ""
        number = ""
        alternateSeries = ""
        alternateNumber = ""
        storyArc = ""
        penciller = ""
      }

      every { mockMapper.readValue(any<ByteArray>(), ComicInfo::class.java) } returns comicInfo

      val patch = comicInfoProvider.getBookMetadataFromBook(book, media)

      with(patch!!) {
        assertThat(title).isNull()
        assertThat(summary).isNull()
        assertThat(number).isNull()
        assertThat(numberSort).isNull()
        assertThat(authors).isNull()
        assertThat(readLists).isEmpty()
      }
    }

    @Test
    fun `given comicInfo without year when getting book metadata then release date is null`() {
      val comicInfo = ComicInfo().apply {
        month = 2
      }

      every { mockMapper.readValue(any<ByteArray>(), ComicInfo::class.java) } returns comicInfo

      val patch = comicInfoProvider.getBookMetadataFromBook(book, media)

      with(patch!!) {
        assertThat(releaseDate).isNull()
      }
    }

    @Test
    fun `given comicInfo with year but without month when getting book metadata then release date is set`() {
      val comicInfo = ComicInfo().apply {
        year = 2020
      }

      every { mockMapper.readValue(any<ByteArray>(), ComicInfo::class.java) } returns comicInfo

      val patch = comicInfoProvider.getBookMetadataFromBook(book, media)

      with(patch!!) {
        assertThat(releaseDate).isEqualTo(LocalDate.of(2020, 1, 1))
      }
    }

    @Test
    fun `given comicInfo with authors when getting book metadata then authors are set`() {
      val comicInfo = ComicInfo().apply {
        writer = "writer"
        penciller = "penciller"
        inker = "inker"
        colorist = "colorist"
        editor = "editor"
        letterer = "letterer"
        coverArtist = "coverArtist"
      }

      every { mockMapper.readValue(any<ByteArray>(), ComicInfo::class.java) } returns comicInfo

      val patch = comicInfoProvider.getBookMetadataFromBook(book, media)

      with(patch!!) {
        assertThat(authors).hasSize(7)
        assertThat(authors?.map { it.name }).containsExactlyInAnyOrder("writer", "penciller", "inker", "colorist", "editor", "letterer", "coverArtist")
        assertThat(authors?.map { it.role }).containsExactlyInAnyOrder("writer", "penciller", "inker", "colorist", "editor", "letterer", "cover")
      }
    }

    @Test
    fun `given comicInfo with multiple authors when getting book metadata then authors are set`() {
      val comicInfo = ComicInfo().apply {
        writer = "writer, writer2"
        penciller = "penciller, penciller2"
        inker = "inker, inker2"
        colorist = "colorist, colorist2"
        editor = "editor, editor2"
        letterer = "letterer, letterer2"
        coverArtist = "coverArtist, coverArtist2"
      }

      every { mockMapper.readValue(any<ByteArray>(), ComicInfo::class.java) } returns comicInfo

      val patch = comicInfoProvider.getBookMetadataFromBook(book, media)

      with(patch!!) {
        assertThat(authors).hasSize(14)
        assertThat(authors?.map { it.name }).containsExactlyInAnyOrder("writer", "penciller", "inker", "colorist", "editor", "letterer", "coverArtist", "writer2", "penciller2", "inker2", "colorist2", "editor2", "letterer2", "coverArtist2")
        assertThat(authors?.map { it.role }?.distinct()).containsExactlyInAnyOrder("writer", "penciller", "inker", "colorist", "editor", "letterer", "cover")
      }
    }

    @Test
    fun `given book without comicInfo file when getting book metadata then return null`() {
      val book = makeBook("book")
      val media = Media(Media.Status.READY)

      val patch = comicInfoProvider.getBookMetadataFromBook(book, media)

      assertThat(patch).isNull()
    }
  }

  @Nested
  inner class Series {
    @Test
    fun `given comicInfo when getting series metadata then metadata patch is valid`() {
      val comicInfo = ComicInfo().apply {
        series = "series"
        seriesGroup = "collection"
        publisher = "publisher"
        ageRating = AgeRating.MA_15
        manga = Manga.YES_AND_RIGHT_TO_LEFT
        languageISO = "en"
        genre = "Action, Adventure"
      }

      every { mockMapper.readValue(any<ByteArray>(), ComicInfo::class.java) } returns comicInfo

      val patch = comicInfoProvider.getSeriesMetadataFromBook(book, media)!!

      with(patch) {
        assertThat(title).isEqualTo("series")
        assertThat(titleSort).isEqualTo("series")
        assertThat(status).isNull()
        assertThat(collections).containsExactly("collection")
        assertThat(publisher).isEqualTo("publisher")
        assertThat(ageRating).isEqualTo(15)
        assertThat(readingDirection).isEqualTo(SeriesMetadata.ReadingDirection.RIGHT_TO_LEFT)
        assertThat(language).isEqualTo("en")
        assertThat(summary).isBlank()
        assertThat(genres).containsExactlyInAnyOrder("Action", "Adventure")
      }
    }

    @Test
    fun `given comicInfo with volume when getting series metadata then metadata patch is valid`() {
      val comicInfo = ComicInfo().apply {
        series = "series"
        volume = 2020
      }

      every { mockMapper.readValue(any<ByteArray>(), ComicInfo::class.java) } returns comicInfo

      val patch = comicInfoProvider.getSeriesMetadataFromBook(book, media)!!

      with(patch) {
        assertThat(title).isEqualTo("series (2020)")
      }
    }

    @Test
    fun `given comicInfo with incorrect values when getting series metadata then metadata patch is valid`() {
      val comicInfo = ComicInfo().apply {
        languageISO = "japanese"
      }

      every { mockMapper.readValue(any<ByteArray>(), ComicInfo::class.java) } returns comicInfo

      val patch = comicInfoProvider.getSeriesMetadataFromBook(book, media)!!

      with(patch) {
        assertThat(language).isNull()
      }
    }

    @Test
    fun `given comicInfo with blank values when getting series metadata then blank values are omitted`() {
      val comicInfo = ComicInfo().apply {
        title = ""
        storyArc = ""
        genre = ""
        languageISO = ""
        publisher = ""
        seriesGroup = ""
      }

      every { mockMapper.readValue(any<ByteArray>(), ComicInfo::class.java) } returns comicInfo

      val patch = comicInfoProvider.getSeriesMetadataFromBook(book, media)!!

      with(patch) {
        assertThat(title).isNull()
        assertThat(titleSort).isNull()
        assertThat(genres).isNull()
        assertThat(language).isNull()
        assertThat(publisher).isNull()
        assertThat(collections).isEmpty()
      }
    }

  }
}
