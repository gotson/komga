package org.gotson.komga.infrastructure.mediacontainer.divina

import org.apache.tika.config.TikaConfig
import org.assertj.core.api.Assertions
import org.gotson.komga.domain.model.Dimension
import org.gotson.komga.infrastructure.image.ImageAnalyzer
import org.gotson.komga.infrastructure.mediacontainer.ContentDetector
import org.junit.jupiter.api.Test
import org.springframework.core.io.ClassPathResource

class RarExtractorTest {
  private val contentDetector = ContentDetector(TikaConfig())
  private val imageAnalyzer = ImageAnalyzer()
  private val rarExtractor = RarExtractor(contentDetector, imageAnalyzer)

  @Test
  fun `given rar file when parsing for entries then returns all images`() {
    val fileResource = ClassPathResource("archives/rar4.rar")

    val entries = rarExtractor.getEntries(fileResource.file.toPath(), true)

    Assertions.assertThat(entries).hasSize(3)
    with(entries.first()) {
      Assertions.assertThat(name).isEqualTo("komga-1.png")
      Assertions.assertThat(mediaType).isEqualTo("image/png")
      Assertions.assertThat(dimension).isEqualTo(Dimension(48, 48))
      Assertions.assertThat(fileSize).isEqualTo(3108)
    }
  }

  @Test
  fun `given rar file when parsing for entries without analyzing dimensions then returns all images without dimensions`() {
    val fileResource = ClassPathResource("archives/rar4.rar")

    val entries = rarExtractor.getEntries(fileResource.file.toPath(), false)

    Assertions.assertThat(entries).hasSize(3)
    with(entries.first()) {
      Assertions.assertThat(name).isEqualTo("komga-1.png")
      Assertions.assertThat(mediaType).isEqualTo("image/png")
      Assertions.assertThat(dimension).isNull()
      Assertions.assertThat(fileSize).isEqualTo(3108)
    }
  }
}
