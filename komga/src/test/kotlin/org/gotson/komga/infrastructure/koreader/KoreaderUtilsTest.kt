package org.gotson.komga.infrastructure.koreader

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.util.stream.Stream

class KoreaderUtilsTest {
  @ParameterizedTest
  @MethodSource("progressStrings")
  fun `given progress strings when parsing then correct resource is returned`(
    progress: String,
    expected: Int?,
  ) {
    val actual = KoreaderUtils.parseResourceIndexFromProgressString(progress)
    assertThat(actual).isEqualTo(expected)
  }

  private fun progressStrings(): Stream<Arguments> =
    Stream.of(
      Arguments.of("/body/DocFragment[2]/body/section/p[3]/text().0", 1),
      Arguments.of("/body/DocFragment/body/section/h2/text().0", 0),
      Arguments.of("/body/body/section/h2/text().0", null),
    )
}
