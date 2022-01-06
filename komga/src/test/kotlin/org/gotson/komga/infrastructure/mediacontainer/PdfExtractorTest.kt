package org.gotson.komga.infrastructure.mediacontainer

import org.assertj.core.api.Assertions
import org.gotson.komga.domain.model.Dimension
import org.junit.jupiter.api.Test
import org.springframework.core.io.ClassPathResource

class PdfExtractorTest {
  private val pdfExtractor = PdfExtractor()

  @Test
  fun `given pdf file when parsing for entries then returns all images`() {
    val fileResource = ClassPathResource("pdf/komga.pdf")

    val entries = pdfExtractor.getEntries(fileResource.file.toPath(), true)

    Assertions.assertThat(entries).hasSize(1)
    with(entries.first()) {
      Assertions.assertThat(name).isEqualTo("0")
      Assertions.assertThat(mediaType).isEqualTo("image/jpeg")
      Assertions.assertThat(dimension).isEqualTo(Dimension(1536, 1536))
      Assertions.assertThat(fileSize).isNull()
    }
  }

  @Test
  fun `given pdf file when parsing for entries without analyzing dimensions  then returns all images without dimensions`() {
    val fileResource = ClassPathResource("pdf/komga.pdf")

    val entries = pdfExtractor.getEntries(fileResource.file.toPath(), false)

    Assertions.assertThat(entries).hasSize(1)
    with(entries.first()) {
      Assertions.assertThat(name).isEqualTo("0")
      Assertions.assertThat(mediaType).isEqualTo("image/jpeg")
      Assertions.assertThat(dimension).isNull()
      Assertions.assertThat(fileSize).isNull()
    }
  }
}
