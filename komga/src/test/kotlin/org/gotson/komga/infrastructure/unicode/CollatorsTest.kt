package org.gotson.komga.infrastructure.unicode

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class CollatorsTest {
  @Test
  fun collator1() {
    assertThat(Collators.collator1.compare("café", "cafe")).isEqualTo(0) // accents ignored
    assertThat(Collators.collator1.compare("CAFE", "cafe")).isEqualTo(0) // case ignored
    assertThat(Collators.collator1.compare("안녕하세요", "안녕하세요")).isEqualTo(0) // hangul
    assertThat(Collators.collator1.compare("あ", "ア")).isEqualTo(0) // katakana = hiragana
    assertThat(Collators.collator1.compare("が", "か")).isEqualTo(0) // dakuten
  }

  @Test
  fun collator3() {
    assertThat(Collators.collator3.compare("café", "cafe")).isNotEqualTo(0) // accents not ignored
    assertThat(Collators.collator3.compare("CAFE", "cafe")).isNotEqualTo(0) // case not ignored
    assertThat(Collators.collator3.compare("안녕하세요", "안녕하세요")).isEqualTo(0) // hangul
    assertThat(Collators.collator3.compare("あ", "ア")).isNotEqualTo(0) // katakana != hiragana
  }
}
