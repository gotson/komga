package org.gotson.komga.infrastructure.mediacontainer

import org.apache.tika.config.TikaConfig
import org.assertj.core.api.Assertions.assertThat
import org.gotson.komga.domain.model.Dimension
import org.gotson.komga.infrastructure.image.ImageAnalyzer
import org.junit.jupiter.api.Test
import org.springframework.core.io.ClassPathResource

class EpubExtractorTest {

  private val contentDetector = ContentDetector(TikaConfig())
  private val imageAnalyzer = ImageAnalyzer()
  private val zipExtractor = ZipExtractor(contentDetector, imageAnalyzer)

  private val epubExtractor = EpubExtractor(zipExtractor, contentDetector, imageAnalyzer)

  @Test
  fun `given epub 3 file when parsing for entries then returns all images contained in pages`() {
    val epubResource = ClassPathResource("epub/The Incomplete Theft - Ralph Burke.epub")

    val entries = epubExtractor.getEntries(epubResource.file.toPath(), true)

    assertThat(entries).hasSize(1)
    with(entries.first()) {
      assertThat(name).isEqualTo("cover.jpeg")
      assertThat(mediaType).isEqualTo("image/jpeg")
      assertThat(dimension).isEqualTo(Dimension(461, 616))
      assertThat(fileSize).isEqualTo(56756)
    }
  }

  @Test
  fun `given epub 3 file when parsing for entries without analyzing dimensions then returns all images contained in pages without dimensions`() {
    val epubResource = ClassPathResource("epub/The Incomplete Theft - Ralph Burke.epub")

    val entries = epubExtractor.getEntries(epubResource.file.toPath(), false)

    assertThat(entries).hasSize(1)
    with(entries.first()) {
      assertThat(name).isEqualTo("cover.jpeg")
      assertThat(mediaType).isEqualTo("image/jpeg")
      assertThat(dimension).isNull()
    }
  }
}
