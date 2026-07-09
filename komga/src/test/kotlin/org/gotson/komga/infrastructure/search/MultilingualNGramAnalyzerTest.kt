package org.gotson.komga.infrastructure.search

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.text.Normalizer

class MultilingualNGramAnalyzerTest {
  @Test
  fun `single letter`() {
    // given
    val text = "J"

    // when
    val tokens = MultiLingualNGramAnalyzer(3, 8, false).getTokens(text)
    val tokensPreserveOriginal = MultiLingualNGramAnalyzer(3, 8, true).getTokens(text)

    // then
    assertThat(tokensPreserveOriginal).containsExactly("j")
    assertThat(tokens).isEmpty()
  }

  @Test
  fun `chinese mixed`() {
    // given
    val text = "[不道德公會][河添太一 ][東立]Vol.04-搬运"

    // when
    val tokens = MultiLingualNGramAnalyzer(3, 8, true).getTokens(text)

    // then
    assertThat(tokens).containsExactly("不道", "道德", "德公", "公會", "河添", "添太", "太一", "東立", "vol", "04", "搬运")
  }

  @Test
  fun `chinese only`() {
    // given
    val text = "不道德公會河添太一東立搬运"

    // when
    val tokens = MultiLingualNGramAnalyzer(3, 8, true).getTokens(text)

    // then
    assertThat(tokens).containsExactly("不道", "道德", "德公", "公會", "會河", "河添", "添太", "太一", "一東", "東立", "立搬", "搬运")
  }

  @Test
  fun `hiragana only`() {
    // given
    val text = "探偵はもう、死んでいる。"

    // when
    val tokens = MultiLingualNGramAnalyzer(3, 8, true).getTokens(text)

    // then
    assertThat(tokens).containsExactly("探偵", "偵は", "はも", "もう", "死ん", "んで", "でい", "いる")
  }

  @Test
  fun `katakana only`() {
    // given
    val text = "ワンパンマン"

    // when
    val tokens = MultiLingualNGramAnalyzer(3, 8, true).getTokens(text)

    // then
    assertThat(tokens).containsExactly("ワン", "ンパ", "パン", "ンマ", "マン")
  }

  @Test
  fun `korean only`() {
    // given
    val text = "고교생을 환불해 주세요"

    // when
    val tokens = MultiLingualNGramAnalyzer(3, 8, true).getTokens(text)

    // then
    assertThat(tokens).containsExactly("고교", "교생", "생을", "환불", "불해", "주세", "세요")
  }

  @Test
  fun `korean decomposed NFD is indexed with the same terms as precomposed NFC`() {
    // given - the same Korean title in precomposed (NFC) and decomposed (NFD) form
    val nfc = Normalizer.normalize("바스타드", Normalizer.Form.NFC)
    val nfd = Normalizer.normalize("바스타드", Normalizer.Form.NFD)
    assertThat(nfd).isNotEqualTo(nfc) // sanity check: the two Unicode forms really differ

    val analyzer = MultiLingualNGramAnalyzer(3, 8, true)

    // when
    val tokensFromNfc = analyzer.getTokens(nfc)
    val tokensFromNfd = analyzer.getTokens(nfd)

    // then - the index analyzer stores identical terms regardless of the input Unicode form
    assertThat(tokensFromNfd).isEqualTo(tokensFromNfc)
    assertThat(tokensFromNfc).isNotEmpty
  }
}
