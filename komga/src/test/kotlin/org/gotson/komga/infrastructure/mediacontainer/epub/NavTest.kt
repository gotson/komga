package org.gotson.komga.infrastructure.mediacontainer.epub

import org.assertj.core.api.Assertions.assertThat
import org.gotson.komga.domain.model.EpubTocEntry
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import org.springframework.core.io.ClassPathResource
import java.util.stream.Stream
import kotlin.io.path.Path

class NavTest {
  @ParameterizedTest
  @MethodSource("paramSource")
  fun `given nav document when parsing nav section then nav entries are valid`(
    navType: Epub3Nav,
    prefix: String?,
    expectedProvider: (String) -> List<EpubTocEntry>,
  ) {
    // given
    val navResource = ClassPathResource("epub/nav.xhtml")
    val navString = navResource.inputStream.readAllBytes().decodeToString()

    // when
    val nav = processNav(ResourceContent(Path("${prefix?.let { "$it/" }}nav.xhtml"), navString), navType)

    // then
    val expectedNav = expectedProvider(prefix?.let { "$it/" } ?: "")

    assertThat(nav).isEqualTo(expectedNav)
  }

  private fun paramSource(): Stream<Arguments> =
    Stream.of(
      Arguments.of(Epub3Nav.TOC, null, ::getExpectedNavToc),
      Arguments.of(Epub3Nav.TOC, "PREFIX", ::getExpectedNavToc),
      Arguments.of(Epub3Nav.LANDMARKS, null, ::getExpectedNavLandmarks),
      Arguments.of(Epub3Nav.LANDMARKS, "PREFIX", ::getExpectedNavLandmarks),
      Arguments.of(Epub3Nav.PAGELIST, null, ::getExpectedNavPageList),
      Arguments.of(Epub3Nav.PAGELIST, "PREFIX", ::getExpectedNavPageList),
    )

  private fun getExpectedNavToc(prefix: String = "") =
    listOf(
      EpubTocEntry("Cover", "${prefix}cover.xhtml"),
      EpubTocEntry("Title Page", "${prefix}titlepage.xhtml"),
      EpubTocEntry("Copyright", "${prefix}copyright.xhtml"),
      EpubTocEntry("Table of Contents", "${prefix}toc.xhtml"),
      EpubTocEntry("An unlinked heading", null),
      EpubTocEntry(
        "Introduction",
        "${prefix}introduction.xhtml",
        children =
          listOf(
            EpubTocEntry("Spring", "${prefix}chapter 001.xhtml"),
            EpubTocEntry("Summer", "${prefix}chapter 027.xhtml"),
            EpubTocEntry("Fall", "${prefix}chapter053.xhtml"),
            EpubTocEntry("Winter", "${prefix}chapter079.xhtml"),
          ),
      ),
      EpubTocEntry("Acknowledgments", "${prefix}acknowledgements.xhtml"),
    )

  private fun getExpectedNavLandmarks(prefix: String = "") =
    listOf(
      EpubTocEntry("Begin Reading", "${prefix}cover.xhtml#coverimage"),
      EpubTocEntry("Table of Contents", "${prefix}toc.xhtml"),
    )

  private fun getExpectedNavPageList(prefix: String = "") =
    listOf(
      EpubTocEntry("Cover Page", "${prefix}xhtml/cover.xhtml"),
      EpubTocEntry("iii", "${prefix}xhtml/title.xhtml#pg_iii"),
      EpubTocEntry("1", "${prefix}xhtml/chapter1.xhtml#pg_1"),
      EpubTocEntry("2", "${prefix}xhtml/chapter1.xhtml#pg_2"),
      EpubTocEntry("107", "${prefix}xhtml/acknowledgments.xhtml#pg_107"),
      EpubTocEntry("ii", "${prefix}xhtml/adcard.xhtml#pg_ii"),
      EpubTocEntry("109", "${prefix}xhtml/abouttheauthor.xhtml#pg_109"),
      EpubTocEntry("iv", "${prefix}xhtml/copyright.xhtml#pg_iv"),
    )
}
