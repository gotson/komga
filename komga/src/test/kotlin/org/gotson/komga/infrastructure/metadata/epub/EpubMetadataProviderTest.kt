package org.gotson.komga.infrastructure.metadata.epub

import io.mockk.every
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import org.apache.commons.validator.routines.ISBNValidator
import org.assertj.core.api.Assertions.assertThat
import org.gotson.komga.domain.model.Author
import org.gotson.komga.domain.model.BookWithMedia
import org.gotson.komga.domain.model.Media
import org.gotson.komga.domain.model.SeriesMetadata
import org.gotson.komga.domain.model.makeBook
import org.gotson.komga.infrastructure.mediacontainer.epub.getPackageFileContent
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.core.io.ClassPathResource
import java.time.LocalDate

class EpubMetadataProviderTest {
  private val isbnValidator = ISBNValidator(true)
  private val epubMetadataProvider = EpubMetadataProvider(isbnValidator)

  private val epubMetadataProviderProper = EpubMetadataProvider(ISBNValidator(true))

  private val book = makeBook("book")
  private val media =
    Media(
      status = Media.Status.READY,
      mediaType = "application/epub+zip",
    )

  @AfterEach
  fun cleanup() {
    unmockkStatic(::getPackageFileContent)
  }

  @Nested
  inner class Book {
    @Test
    fun `given epub 3 opf when getting book metadata then metadata patch is valid`() {
      val opf = ClassPathResource("epub/Panik im Paradies.opf")
      mockkStatic(::getPackageFileContent)
      every { getPackageFileContent(any()) } returns opf.file.readText()

      val patch = epubMetadataProvider.getBookMetadataFromBook(BookWithMedia(book, media))

      with(patch!!) {
        assertThat(title).isEqualTo("Panik im Paradies")
        assertThat(summary).isEqualTo("Bereits im ersten Band \"Panik im Paradies\" machen die drei berühmten Detektive ihrem Namen alle Ehre. Eigentlich haben sie ja gerade Ferien. Doch dann treffen sie auf diesen schrulligen Kapitän Larsson, der sich einen kleinen Privatzoo mit exotischen Tieren hält. Als plötzlich alle Tiere an rätselhaften Infektionen erkranken und die Besucher ausbleiben, werden Justus, Peter und Bob neugierig. Schon bald merken sie, daß da jemand ein düsteres Geheimnis hütet...")
        assertThat(releaseDate).isEqualTo(LocalDate.of(1999, 7, 31))
        assertThat(authors).containsExactlyInAnyOrder(
          Author("Ulf Blanck", "writer"),
          Author("The Editor", "editor"),
        )
        assertThat(isbn).isEqualTo("9783440077894")
        assertThat(number).isEqualTo("1.5")
        assertThat(numberSort).isEqualTo(1.5f)
      }
    }

    @Test
    fun `given another epub 3 opf when getting book metadata then metadata patch is valid`() {
      val opf = ClassPathResource("epub/Die Drei 3.opf")
      mockkStatic(::getPackageFileContent)
      every { getPackageFileContent(any()) } returns opf.file.readText()

      val patch = epubMetadataProvider.getBookMetadataFromBook(BookWithMedia(book, media))

      with(patch!!) {
        assertThat(title).isEqualTo("Die Drei Fragezeichen-Kids, Bd.3, Invasion Der Fliegen")
        assertThat(summary).isEqualTo("Hunderte von Fliegen tummeln sich auf dem Schrottplatz vor Tante Mathildas Haus. Das ist ein Alptraum! Findet diese \"Invasion der Fliegen\" tatsächlich statt oder leidet Tante Mathilda etwa an Halluzinationen? Justus, Peter und Bob nehmen sich der Sache an und kommen schnell dahinter, daß es sich hier nicht um eine Einbildung von Tante Mathilda handelt. Sie verfolgen die Spur der Fliegen bis in einen düsteren Kanalschacht...")
        assertThat(releaseDate).isEqualTo(LocalDate.of(1999, 7, 31))
        assertThat(authors).containsExactlyInAnyOrder(
          Author("Ulf Blanck", "writer"),
          Author("Stefanie Wegner", "writer"),
        )
        assertThat(isbn).isEqualTo("9783440077931")
        assertThat(number).isEqualTo("3")
        assertThat(numberSort).isEqualTo(3f)
      }
    }

    @Test
    fun `given real epub 3 when getting book metadata then metadata patch is valid`() {
      val epubResource = ClassPathResource("epub/The Incomplete Theft - Ralph Burke.epub")
      val epubBook =
        BookWithMedia(
          makeBook("Epub", url = epubResource.url),
          media,
        )

      val patch = epubMetadataProviderProper.getBookMetadataFromBook(epubBook)

      with(patch!!) {
        assertThat(title).isEqualTo("The Incomplete Theft")
        assertThat(summary).isNull()
        assertThat(releaseDate).isEqualTo(LocalDate.of(2021, 6, 20))
        assertThat(authors).containsExactlyInAnyOrder(Author("Ralph Burke", "writer"))
        assertThat(isbn).isNull()
        assertThat(number).isNull()
        assertThat(numberSort).isNull()
      }
    }

    @Test
    fun `given epub 2 opf when getting book metadata then metadata patch is valid`() {
      val opf = ClassPathResource("epub/1979.opf")
      mockkStatic(::getPackageFileContent)
      every { getPackageFileContent(any()) } returns opf.file.readText()

      val patch = epubMetadataProvider.getBookMetadataFromBook(BookWithMedia(book, media))

      with(patch!!) {
        assertThat(title).isEqualTo("1979")
        assertThat(summary).isEqualTo("Auf dem Weg nach Teheran sah ich aus dem Autofenster, mir wurde etwas übel und ich hielt mich an Christophers Knie fest. Sein Hosenbein war von den aufgeplatzten Blasen ganz naß. Wir fuhren an endlosen Reihen von Birken vorbei. Ich schlief.Später hielten wir an, um uns zu erfrischen. Ich trank ein Glas Tee, Christopher eine Limonade. Es wurde sehr rasch Nacht.Es gab einige Militärkontrollen, denn seit September herrschte Kriegsrecht, was ja eigentlich nichts zu bedeuten hatte in diesen Ländern, sagte Christopher. Wir wurden weitergewunken, einmal sah ich einen Arm, eine weiße Bandage darum und eine Taschenlampe, die uns ins Gesicht schien, dann ging es weiter.Die Luft war staubig, ab und zu roch es im Wagen nach Mais. Wir hatten nur zwei Kassetten dabei; wir hörten erst Blondie, dann Devo, dann wieder Blondie. Es waren Christophers Kassetten.CHRISTIAN KRACHT, 1966 geboren, ist Schweizer. Das Foto zeigt den Autor auf der Landstraße nach Tashigang, Tibet, Volksrepublik China.GESTALTUNG: PAUL BARNES UND PETER SAVILLEFOTO: ECKHART NICKEL")
        assertThat(releaseDate).isEqualTo(LocalDate.of(101, 1, 1))
        assertThat(authors).containsExactlyInAnyOrder(
          Author("Kracht, Christian", "writer"),
          Author("The Editor", "editor"),
        )
        assertThat(isbn).isNull()
        assertThat(number).isNull()
        assertThat(numberSort).isNull()
      }
    }
  }

  @Nested
  inner class Series {
    @Test
    fun `given epub 3 opf when getting series metadata then metadata patch is valid`() {
      val opf = ClassPathResource("epub/Panik im Paradies.opf")
      mockkStatic(::getPackageFileContent)
      every { getPackageFileContent(any()) } returns opf.file.readText()

      val patch = epubMetadataProvider.getSeriesMetadataFromBook(BookWithMedia(book, media), true)

      with(patch!!) {
        assertThat(title).isEqualTo("Die drei ??? Kids")
        assertThat(titleSort).isEqualTo("Die drei ??? Kids")
        assertThat(readingDirection).isEqualTo(SeriesMetadata.ReadingDirection.RIGHT_TO_LEFT)
        assertThat(publisher).isEqualTo("Kosmos")
        assertThat(language).isEqualTo("de")
        assertThat(genres).containsExactlyInAnyOrder("Kinder- und Jugendbücher")
      }
    }

    @Test
    fun `given another epub 3 opf when getting series metadata then metadata patch is valid`() {
      val opf = ClassPathResource("epub/Die Drei 3.opf")
      mockkStatic(::getPackageFileContent)
      every { getPackageFileContent(any()) } returns opf.file.readText()

      val patch = epubMetadataProvider.getSeriesMetadataFromBook(BookWithMedia(book, media), true)

      with(patch!!) {
        assertThat(title).isEqualTo("Die drei ??? Kids")
        assertThat(titleSort).isEqualTo("Die drei ??? Kids")
        assertThat(readingDirection).isNull()
        assertThat(publisher).isEqualTo("Franckh-Kosmos Verlag")
        assertThat(language).isEqualTo("de")
        assertThat(genres).containsExactlyInAnyOrder("Kinder- und Jugendbücher", "Juvenile Fiction", "Mysteries & Detective Stories")
      }
    }

    @Test
    fun `given epub 2 opf when getting series metadata then metadata patch is valid`() {
      val opf = ClassPathResource("epub/1979.opf")
      mockkStatic(::getPackageFileContent)
      every { getPackageFileContent(any()) } returns opf.file.readText()

      val patch = epubMetadataProvider.getSeriesMetadataFromBook(BookWithMedia(book, media), true)

      with(patch!!) {
        assertThat(title).isNull()
        assertThat(titleSort).isNull()
        assertThat(readingDirection).isNull()
        assertThat(publisher).isNull()
        assertThat(language).isEqualTo("de")
        assertThat(genres).isNull()
      }
    }
  }
}
