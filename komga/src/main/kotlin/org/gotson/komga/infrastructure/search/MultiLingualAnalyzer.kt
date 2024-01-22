package org.gotson.komga.infrastructure.search

import org.apache.lucene.analysis.Analyzer
import org.apache.lucene.analysis.LowerCaseFilter
import org.apache.lucene.analysis.TokenStream
import org.apache.lucene.analysis.Tokenizer
import org.apache.lucene.analysis.cjk.CJKBigramFilter
import org.apache.lucene.analysis.cjk.CJKWidthFilter
import org.apache.lucene.analysis.miscellaneous.ASCIIFoldingFilter
import org.apache.lucene.analysis.standard.StandardTokenizer

open class MultiLingualAnalyzer : Analyzer() {
  override fun createComponents(fieldName: String): TokenStreamComponents {
    val source: Tokenizer = StandardTokenizer()
    // run the widthfilter first before bigramming, it sometimes combines characters.
    var filter: TokenStream = CJKWidthFilter(source)
    filter = LowerCaseFilter(filter)
    filter = CJKBigramFilter(filter)
    filter = ASCIIFoldingFilter(filter)
    return TokenStreamComponents(source, filter)
  }

  override fun normalize(
    fieldName: String?,
    `in`: TokenStream,
  ): TokenStream {
    var filter: TokenStream = CJKWidthFilter(`in`)
    filter = LowerCaseFilter(filter)
    filter = ASCIIFoldingFilter(filter)
    return filter
  }
}
