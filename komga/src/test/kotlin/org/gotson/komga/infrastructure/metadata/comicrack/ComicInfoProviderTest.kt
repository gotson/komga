package org.gotson.komga.infrastructure.metadata.comicrack

import com.fasterxml.jackson.dataformat.xml.XmlMapper
import io.mockk.every
import io.mockk.mockk
import org.apache.commons.validator.routines.ISBNValidator
import org.assertj.core.api.Assertions.assertThat
import org.gotson.komga.domain.model.BookMetadataPatch
import org.gotson.komga.domain.model.BookWithMedia
import org.gotson.komga.domain.model.Media
import org.gotson.komga.domain.model.MediaFile
import org.gotson.komga.domain.model.SeriesMetadata
import org.gotson.komga.domain.model.WebLink
import org.gotson.komga.domain.model.makeBook
import org.gotson.komga.domain.service.BookAnalyzer
import org.gotson.komga.infrastructure.metadata.comicrack.dto.AgeRating
import org.gotson.komga.infrastructure.metadata.comicrack.dto.ComicInfo
import org.gotson.komga.infrastructure.metadata.comicrack.dto.Manga
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.net.URI
import java.time.LocalDate
import java.util.stream.Stream

class ComicInfoProviderTest {
  private val mockMapper = mockk<XmlMapper>()
  private val mockAnalyzer =
    mockk<BookAnalyzer>().also {
      every { it.getFileContent(any(), "ComicInfo.xml") } returns ByteArray(0)
    }
  private val isbnValidator = ISBNValidator(true)

  private val comicInfoProvider = ComicInfoProvider(mockMapper, mockAnalyzer, isbnValidator)

  private val book = makeBook("book")
  private val media =
    Media(
      status = Media.Status.READY,
      mediaType = "application/zip",
      files = listOf(MediaFile("ComicInfo.xml")),
    )

  @Nested
  inner class Book {
    @Test
    fun `given comicInfo when getting book metadata then metadata patch is valid`() {
      val comicInfo =
        ComicInfo().apply {
          title = "title"
          summary = "summary"
          number = "010"
          year = 2020
          month = 2
          alternateSeries = "story arc"
          alternateNumber = "5"
          storyArc = "one, two, three"
          web = "   https://www.comixology.com/Sandman/digital-comic/727888    https://www.comics.com/Sandman/digital-comic/727889   "
          tags = "dark, Occult"
          gtin = "9783440077894"
        }

      every { mockMapper.readValue(any<ByteArray>(), ComicInfo::class.java) } returns comicInfo

      val patch = comicInfoProvider.getBookMetadataFromBook(BookWithMedia(book, media))

      with(patch!!) {
        assertThat(title).isEqualTo("title")
        assertThat(summary).isEqualTo("summary")
        assertThat(number).isEqualTo("010")
        assertThat(numberSort).isEqualTo(10F)
        assertThat(releaseDate).isEqualTo(LocalDate.of(2020, 2, 1))
        assertThat(isbn).isEqualTo("9783440077894")

        assertThat(readLists)
          .hasSize(4)
          .containsExactlyInAnyOrder(
            BookMetadataPatch.ReadListEntry("story arc", 5),
            BookMetadataPatch.ReadListEntry("one"),
            BookMetadataPatch.ReadListEntry("two"),
            BookMetadataPatch.ReadListEntry("three"),
          )

        assertThat(links)
          .containsExactlyInAnyOrder(
            WebLink("www.comixology.com", URI("https://www.comixology.com/Sandman/digital-comic/727888")),
            WebLink("www.comics.com", URI("https://www.comics.com/Sandman/digital-comic/727889")),
          )

        assertThat(tags as Iterable<String>)
          .hasSize(2)
          .containsExactlyInAnyOrder("dark", "occult")
      }
    }

    @Test
    fun `given comicInfo with single link when getting book metadata then metadata patch is valid`() {
      val comicInfo =
        ComicInfo().apply {
          web = "https://www.comixology.com/Sandman/digital-comic/727888"
        }

      every { mockMapper.readValue(any<ByteArray>(), ComicInfo::class.java) } returns comicInfo

      val patch = comicInfoProvider.getBookMetadataFromBook(BookWithMedia(book, media))

      with(patch!!) {
        assertThat(links)
          .containsExactlyInAnyOrder(
            WebLink("www.comixology.com", URI("https://www.comixology.com/Sandman/digital-comic/727888")),
          )
      }
    }

    @Test
    fun `given comicInfo with StoryArcNumber when getting book metadata then metadata patch is valid`() {
      val comicInfo =
        ComicInfo().apply {
          storyArc = "one"
          storyArcNumber = "6"
        }

      every { mockMapper.readValue(any<ByteArray>(), ComicInfo::class.java) } returns comicInfo

      val patch = comicInfoProvider.getBookMetadataFromBook(BookWithMedia(book, media))

      with(patch!!) {
        assertThat(readLists).hasSize(1)
        assertThat(readLists).containsExactlyInAnyOrder(
          BookMetadataPatch.ReadListEntry("one", 6),
        )
      }
    }

    @Test
    fun `given comicInfo with multiple StoryArcNumber when getting book metadata then metadata patch is valid`() {
      val comicInfo =
        ComicInfo().apply {
          alternateSeries = "story arc"
          alternateNumber = "5"
          storyArc = "one, two, three"
          storyArcNumber = "6, 7, 8"
        }

      every { mockMapper.readValue(any<ByteArray>(), ComicInfo::class.java) } returns comicInfo

      val patch = comicInfoProvider.getBookMetadataFromBook(BookWithMedia(book, media))

      with(patch!!) {
        assertThat(readLists).hasSize(4)
        assertThat(readLists).containsExactlyInAnyOrder(
          BookMetadataPatch.ReadListEntry("story arc", 5),
          BookMetadataPatch.ReadListEntry("one", 6),
          BookMetadataPatch.ReadListEntry("two", 7),
          BookMetadataPatch.ReadListEntry("three", 8),
        )
      }
    }

    @Test
    fun `given comicInfo with uneven StoryArcNumber when getting book metadata then metadata patch is valid`() {
      val comicInfo =
        ComicInfo().apply {
          storyArc = "one, two"
          storyArcNumber = "6, 7, 8"
        }

      every { mockMapper.readValue(any<ByteArray>(), ComicInfo::class.java) } returns comicInfo

      val patch = comicInfoProvider.getBookMetadataFromBook(BookWithMedia(book, media))

      with(patch!!) {
        assertThat(readLists).hasSize(2)
        assertThat(readLists).containsExactlyInAnyOrder(
          BookMetadataPatch.ReadListEntry("one", 6),
          BookMetadataPatch.ReadListEntry("two", 7),
        )
      }
    }

    @Test
    fun `given another comicInfo with uneven StoryArcNumber when getting book metadata then metadata patch is valid`() {
      val comicInfo =
        ComicInfo().apply {
          storyArc = "one, two, three"
          storyArcNumber = "6, 7"
        }

      every { mockMapper.readValue(any<ByteArray>(), ComicInfo::class.java) } returns comicInfo

      val patch = comicInfoProvider.getBookMetadataFromBook(BookWithMedia(book, media))

      with(patch!!) {
        assertThat(readLists).hasSize(2)
        assertThat(readLists).containsExactlyInAnyOrder(
          BookMetadataPatch.ReadListEntry("one", 6),
          BookMetadataPatch.ReadListEntry("two", 7),
        )
      }
    }

    @Test
    fun `given comicInfo with invalid StoryArcNumber when getting book metadata then invalid pairs are omitted`() {
      val comicInfo =
        ComicInfo().apply {
          storyArc = "one, two, three"
          storyArcNumber = "6, x, 8"
        }

      every { mockMapper.readValue(any<ByteArray>(), ComicInfo::class.java) } returns comicInfo

      val patch = comicInfoProvider.getBookMetadataFromBook(BookWithMedia(book, media))

      with(patch!!) {
        assertThat(readLists).hasSize(2)
        assertThat(readLists).containsExactlyInAnyOrder(
          BookMetadataPatch.ReadListEntry("one", 6),
          BookMetadataPatch.ReadListEntry("three", 8),
        )
      }
    }

    @Test
    fun `given comicInfo with invalid StoryArc when getting book metadata then invalid pairs are omitted`() {
      val comicInfo =
        ComicInfo().apply {
          storyArc = "one, , three"
          storyArcNumber = "6, 7, 8"
        }

      every { mockMapper.readValue(any<ByteArray>(), ComicInfo::class.java) } returns comicInfo

      val patch = comicInfoProvider.getBookMetadataFromBook(BookWithMedia(book, media))

      with(patch!!) {
        assertThat(readLists).hasSize(2)
        assertThat(readLists).containsExactlyInAnyOrder(
          BookMetadataPatch.ReadListEntry("one", 6),
          BookMetadataPatch.ReadListEntry("three", 8),
        )
      }
    }

    @Test
    fun `given comicInfo with blank values when getting series metadata then blank values are omitted`() {
      val comicInfo =
        ComicInfo().apply {
          title = ""
          summary = ""
          number = ""
          alternateSeries = ""
          alternateNumber = ""
          storyArc = ""
          penciller = ""
          gtin = ""
          web = ""
        }

      every { mockMapper.readValue(any<ByteArray>(), ComicInfo::class.java) } returns comicInfo

      val patch = comicInfoProvider.getBookMetadataFromBook(BookWithMedia(book, media))

      with(patch!!) {
        assertThat(title).isNull()
        assertThat(summary).isNull()
        assertThat(number).isNull()
        assertThat(numberSort).isNull()
        assertThat(authors).isNull()
        assertThat(readLists).isEmpty()
        assertThat(isbn).isNull()
        assertThat(links).isNull()
      }
    }

    @Test
    fun `given comicInfo without year when getting book metadata then release date is null`() {
      val comicInfo =
        ComicInfo().apply {
          month = 2
        }

      every { mockMapper.readValue(any<ByteArray>(), ComicInfo::class.java) } returns comicInfo

      val patch = comicInfoProvider.getBookMetadataFromBook(BookWithMedia(book, media))

      with(patch!!) {
        assertThat(releaseDate).isNull()
      }
    }

    @Test
    fun `given comicInfo with year but without month when getting book metadata then release date is set`() {
      val comicInfo =
        ComicInfo().apply {
          year = 2020
        }

      every { mockMapper.readValue(any<ByteArray>(), ComicInfo::class.java) } returns comicInfo

      val patch = comicInfoProvider.getBookMetadataFromBook(BookWithMedia(book, media))

      with(patch!!) {
        assertThat(releaseDate).isEqualTo(LocalDate.of(2020, 1, 1))
      }
    }

    @Test
    fun `given comicInfo with authors when getting book metadata then authors are set`() {
      val comicInfo =
        ComicInfo().apply {
          writer = "writer"
          penciller = "penciller"
          inker = "inker"
          colorist = "colorist"
          editor = "editor"
          translator = "translator"
          letterer = "letterer"
          coverArtist = "coverArtist"
        }

      every { mockMapper.readValue(any<ByteArray>(), ComicInfo::class.java) } returns comicInfo

      val patch = comicInfoProvider.getBookMetadataFromBook(BookWithMedia(book, media))

      with(patch!!) {
        assertThat(authors).hasSize(8)
        assertThat(authors?.map { it.name }).containsExactlyInAnyOrder("writer", "penciller", "inker", "colorist", "editor", "letterer", "translator", "coverArtist")
        assertThat(authors?.map { it.role }).containsExactlyInAnyOrder("writer", "penciller", "inker", "colorist", "editor", "letterer", "translator", "cover")
      }
    }

    @Test
    fun `given comicInfo with multiple authors when getting book metadata then authors are set`() {
      val comicInfo =
        ComicInfo().apply {
          writer = "writer, writer2"
          penciller = "penciller, penciller2"
          inker = "inker, inker2"
          colorist = "colorist, colorist2"
          editor = "editor, editor2"
          translator = "translator, translator2"
          letterer = "letterer, letterer2"
          coverArtist = "coverArtist, coverArtist2"
        }

      every { mockMapper.readValue(any<ByteArray>(), ComicInfo::class.java) } returns comicInfo

      val patch = comicInfoProvider.getBookMetadataFromBook(BookWithMedia(book, media))

      with(patch!!) {
        assertThat(authors).hasSize(16)
        assertThat(authors?.map { it.name }).containsExactlyInAnyOrder("writer", "penciller", "inker", "colorist", "editor", "letterer", "coverArtist", "writer2", "penciller2", "inker2", "colorist2", "editor2", "letterer2", "coverArtist2", "translator", "translator2")
        assertThat(authors?.map { it.role }?.distinct()).containsExactlyInAnyOrder("writer", "penciller", "inker", "colorist", "editor", "letterer", "cover", "translator")
      }
    }

    @Test
    fun `given book without comicInfo file when getting book metadata then return null`() {
      val book = makeBook("book")
      val media = Media(Media.Status.READY)

      val patch = comicInfoProvider.getBookMetadataFromBook(BookWithMedia(book, media))

      assertThat(patch).isNull()
    }
  }

  @Nested
  inner class Series {
    @Test
    fun `given comicInfo when getting series metadata then metadata patch is valid`() {
      val comicInfo =
        ComicInfo().apply {
          series = "séries"
          seriesGroup = "multiple,collections"
          publisher = "publisher"
          ageRating = AgeRating.MA_15
          manga = Manga.YES_AND_RIGHT_TO_LEFT
          languageISO = "en"
          count = 10
          genre = "Action, Adventure"
        }

      every { mockMapper.readValue(any<ByteArray>(), ComicInfo::class.java) } returns comicInfo

      val patch = comicInfoProvider.getSeriesMetadataFromBook(BookWithMedia(book, media), true)!!

      with(patch) {
        assertThat(title).isEqualTo("séries")
        assertThat(titleSort).isEqualTo("séries")
        assertThat(status).isNull()
        assertThat(collections).containsExactlyInAnyOrder("collections", "multiple")
        assertThat(publisher).isEqualTo("publisher")
        assertThat(ageRating).isEqualTo(15)
        assertThat(readingDirection).isEqualTo(SeriesMetadata.ReadingDirection.RIGHT_TO_LEFT)
        assertThat(language).isEqualTo("en")
        assertThat(summary).isBlank
        assertThat(totalBookCount).isEqualTo(10)
        assertThat(genres).containsExactlyInAnyOrder("Action", "Adventure")
      }
    }

    @Test
    fun `given comicInfo with volume when getting series metadata then metadata patch is valid`() {
      val comicInfo =
        ComicInfo().apply {
          series = "series"
          volume = 2020
        }

      every { mockMapper.readValue(any<ByteArray>(), ComicInfo::class.java) } returns comicInfo

      val patch = comicInfoProvider.getSeriesMetadataFromBook(BookWithMedia(book, media), true)!!

      with(patch) {
        assertThat(title).isEqualTo("series (2020)")
      }

      val patchNoAppend = comicInfoProvider.getSeriesMetadataFromBook(BookWithMedia(book, media), false)!!

      with(patchNoAppend) {
        assertThat(title).isEqualTo("series")
      }
    }

    @Test
    fun `given comicInfo with volume as 1 when getting series metadata then metadata title omits volume`() {
      val comicInfo =
        ComicInfo().apply {
          series = "series"
          volume = 1
        }

      every { mockMapper.readValue(any<ByteArray>(), ComicInfo::class.java) } returns comicInfo

      val patch = comicInfoProvider.getSeriesMetadataFromBook(BookWithMedia(book, media), true)!!

      with(patch) {
        assertThat(title).isEqualTo("series")
      }

      val patchNoAppend = comicInfoProvider.getSeriesMetadataFromBook(BookWithMedia(book, media), false)!!

      with(patchNoAppend) {
        assertThat(title).isEqualTo("series")
      }
    }

    @Test
    fun `given comicInfo with incorrect values when getting series metadata then metadata patch is valid`() {
      val comicInfo =
        ComicInfo().apply {
          languageISO = "japanese"
        }

      every { mockMapper.readValue(any<ByteArray>(), ComicInfo::class.java) } returns comicInfo

      val patch = comicInfoProvider.getSeriesMetadataFromBook(BookWithMedia(book, media), true)!!

      with(patch) {
        assertThat(language).isNull()
      }
    }

    @ParameterizedTest
    @MethodSource("languagesSource")
    fun `given comicInfo with malformed BCP-47 language when getting series metadata then patch language is normalized`(
      source: String,
      expected: String,
    ) {
      val comicInfo =
        ComicInfo().apply {
          languageISO = source
        }

      every { mockMapper.readValue(any<ByteArray>(), ComicInfo::class.java) } returns comicInfo

      val patch = comicInfoProvider.getSeriesMetadataFromBook(BookWithMedia(book, media), true)!!

      with(patch) {
        assertThat(language).isEqualTo(expected)
      }
    }

    private fun languagesSource(): Stream<Arguments> =
      Stream.of(
        Arguments.of("fra", "fr"),
        Arguments.of("fra-be", "fr-BE"),
        Arguments.of("JA", "ja"),
        Arguments.of("en-us", "en-US"),
      )

    @Test
    fun `given comicInfo with blank values when getting series metadata then blank values are omitted`() {
      val comicInfo =
        ComicInfo().apply {
          title = ""
          storyArc = ""
          genre = ""
          languageISO = ""
          publisher = ""
          seriesGroup = ""
        }

      every { mockMapper.readValue(any<ByteArray>(), ComicInfo::class.java) } returns comicInfo

      val patch = comicInfoProvider.getSeriesMetadataFromBook(BookWithMedia(book, media), true)!!

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

  companion object {
    @JvmStatic
    fun computeSeriesFromSeriesAndVolumeArguments(): Stream<Arguments> =
      Stream.of(
        Arguments.of("", null, null),
        Arguments.of(null, null, null),
        Arguments.of("Series", null, "Series"),
        Arguments.of("Series", 1, "Series"),
        Arguments.of("Series", 10, "Series (10)"),
      )
  }

  @ParameterizedTest
  @MethodSource("computeSeriesFromSeriesAndVolumeArguments")
  fun `given series and volume when computing series name then it is correct`(
    series: String?,
    volume: Int?,
    expected: String?,
  ) {
    assertThat(computeSeriesFromSeriesAndVolume(series, volume)).isEqualTo(expected)
  }
}
