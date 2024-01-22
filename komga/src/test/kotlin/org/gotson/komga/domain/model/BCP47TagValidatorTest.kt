package org.gotson.komga.domain.model

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.util.stream.Stream

class BCP47TagValidatorTest {
  @ParameterizedTest
  @MethodSource("languagesNormalized")
  fun `given source languageTag when normalizing then result is expected`(
    source: String?,
    expected: String,
  ) {
    assertThat(BCP47TagValidator.normalize(source)).isEqualTo(expected)
  }

  private fun languagesNormalized(): Stream<Arguments> =
    Stream.of(
      Arguments.of(null, ""),
      Arguments.of("", ""),
      Arguments.of("fra", "fr"),
      Arguments.of("fra-be", "fr-BE"),
      Arguments.of("JA", "ja"),
      Arguments.of("en-us", "en-US"),
      Arguments.of("zh-Hans", "zh-Hans"),
      Arguments.of("zh-HK", "zh-HK"),
    )

  @ParameterizedTest
  @MethodSource("languagesValid")
  fun `given source languageTag when validating then result is expected`(
    source: String?,
    expected: Boolean,
  ) {
    assertThat(BCP47TagValidator.isValid(source)).isEqualTo(expected)
  }

  private fun languagesValid(): Stream<Arguments> =
    Stream.of(
      Arguments.of(null, false),
      Arguments.of("", false),
      Arguments.of("fra", true),
      Arguments.of("fra-BE", true),
      Arguments.of("en-us", true),
      Arguments.of("JA", true),
      Arguments.of("jp-JP", false),
      Arguments.of("ja-JP", true),
      Arguments.of("zh-Hans", true),
    )
}
