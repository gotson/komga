package org.gotson.komga.infrastructure.mediacontainer.epub

import org.assertj.core.api.Assertions.assertThat
import org.gotson.komga.domain.model.EpubTocEntry
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import org.springframework.core.io.ClassPathResource
import java.util.stream.Stream
import kotlin.io.path.Path

class NcxTest {
  @ParameterizedTest
  @MethodSource("paramSource")
  fun `given ncx document when parsing nav then nav entries are valid`(
    navType: Epub2Nav,
    prefix: String?,
    expectedProvider: (String) -> List<EpubTocEntry>,
  ) {
    // given
    val ncxResource = ClassPathResource("epub/toc.ncx")
    val ncxString = ncxResource.inputStream.readAllBytes().decodeToString()

    // when
    val ncxNav = processNcx(ResourceContent(Path("${prefix?.let { "$it/" }}toc.ncx"), ncxString), navType)

    // then
    val expectedNav = expectedProvider(prefix?.let { "$it/" } ?: "")

    assertThat(ncxNav).isEqualTo(expectedNav)
  }

  private fun paramSource(): Stream<Arguments> =
    Stream.of(
      Arguments.of(Epub2Nav.TOC, null, ::getExpectedNcxToc),
      Arguments.of(Epub2Nav.TOC, "PREFIX", ::getExpectedNcxToc),
      Arguments.of(Epub2Nav.PAGELIST, null, ::getExpectedNcxPageList),
      Arguments.of(Epub2Nav.PAGELIST, "PREFIX", ::getExpectedNcxPageList),
    )

  private fun getExpectedNcxToc(prefix: String = "") =
    listOf(
      EpubTocEntry("COVER", "${prefix}Text/Mart_9780553897852_epub_cvi_r1.htm#b02-cvi"),
      EpubTocEntry("BRAN", "${prefix}Text/Mart_9780553897852_epub_c69_r1.htm"),
      EpubTocEntry(
        "APPENDIX",
        "${prefix}Text/Mart_9780553897852_epub_app_r1.htm",
        children =
          listOf(
            EpubTocEntry("THE KINGS AND THEIR COURTS", "${prefix}Text/Mart_9780553897852_epub_app_r1.htm#apps01.00"),
            EpubTocEntry("THE KING ON THE IRON THRONE", "${prefix}Text/Mart_9780553897852_epub_app_r1.htm#apps01.01"),
            EpubTocEntry("THE KING IN THE NARROW SEA", "${prefix}Text/Mart_9780553897852_epub_app_r1.htm#apps01.02"),
            EpubTocEntry("THE KING IN HIGHGARDEN", "${prefix}Text/Mart_9780553897852_epub_app_r1.htm#apps01.03"),
            EpubTocEntry("THE KING IN THE NORTH", "${prefix}Text/Mart_9780553897852_epub_app_r1.htm#apps01.04"),
            EpubTocEntry(
              "THE QUEEN ACROSS THE WATER",
              "${prefix}Text/Mart_9780553897852_epub_app_r1.htm#apps01.05",
              children =
                listOf(
                  EpubTocEntry("Another level", "${prefix}Text/Mart_9780553897852 epub_app_r1.htm#apps01.06"),
                  EpubTocEntry("Yet another level", "${prefix}Text/Mart_9780553897852 epub_app_r1.htm#apps01.07"),
                ),
            ),
          ),
      ),
      EpubTocEntry("ACKNOWLEDGMENTS", "${prefix}Text/Mart_9780553897852_epub_ack_r1.htm"),
    )

  private fun getExpectedNcxPageList(prefix: String = "") =
    listOf(
      EpubTocEntry("Cover Page", "${prefix}xhtml/cover.xhtml"),
      EpubTocEntry("iii", "${prefix}xhtml/title.xhtml#pg_iii"),
      EpubTocEntry("v", "${prefix}xhtml/dedication.xhtml#pg_v"),
      EpubTocEntry("vii", "${prefix}xhtml/formoreinformation.xhtml#pg_vii"),
      EpubTocEntry("viii", "${prefix}xhtml/formoreinformation.xhtml#pg_viii"),
      EpubTocEntry("ix", "${prefix}xhtml/formoreinformation.xhtml#pg_ix"),
      EpubTocEntry("x", "${prefix}xhtml/formoreinformation.xhtml#pg_x"),
      EpubTocEntry("xi", "${prefix}xhtml/formoreinformation.xhtml#pg_xi"),
      EpubTocEntry("1", "${prefix}xhtml/chapter1.xhtml#pg_1"),
      EpubTocEntry("2", "${prefix}xhtml/chapter1.xhtml#pg_2"),
      EpubTocEntry("3", "${prefix}xhtml/chapter1.xhtml#pg_3"),
    )
}
