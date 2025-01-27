package org.gotson.komga.infrastructure.mediacontainer.epub

import org.assertj.core.api.Assertions.assertThat
import org.gotson.komga.domain.model.EpubTocEntry
import org.jsoup.Jsoup
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import org.springframework.core.io.ClassPathResource
import java.nio.file.Path

class OpfTest {
  @ParameterizedTest
  @ValueSource(strings = ["", "PREFIX"])
  fun `given ncx document and opfDir when landmarks TOC then TOC entries are valid`(prefix: String) {
    // given
    val opfResource = ClassPathResource("epub/clash.opf")
    val opfString = opfResource.inputStream.readAllBytes().decodeToString()
    val opfDoc = Jsoup.parse(opfString)

    // when
    val opfLandmarks = processOpfGuide(opfDoc, if (prefix.isBlank()) null else Path.of(prefix))

    // then
    val expectedToc = getExpectedOpfLandmarks(if (prefix.isBlank()) "" else "$prefix/")

    assertThat(opfLandmarks).isEqualTo(expectedToc)
  }

  private fun getExpectedOpfLandmarks(prefix: String = "") =
    listOf(
      EpubTocEntry("Table Of Contents", "${prefix}Text/Mart_9780553897852_epub_toc_r1.htm"),
      EpubTocEntry("Text", "${prefix}Text/Mart_9780553897852_epub_prl_r1.htm"),
      EpubTocEntry("Cover", "${prefix}Text/Mart_9780553897852_epub_cvi_r1.htm"),
    )
}
