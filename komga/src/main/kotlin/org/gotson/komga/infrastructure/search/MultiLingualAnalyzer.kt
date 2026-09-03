package org.gotson.komga.infrastructure.search

import org.apache.lucene.analysis.Analyzer
import org.apache.lucene.analysis.LowerCaseFilter
import org.apache.lucene.analysis.TokenStream
import org.apache.lucene.analysis.Tokenizer
import org.apache.lucene.analysis.cjk.CJKBigramFilter
import org.apache.lucene.analysis.cjk.CJKWidthFilter
import org.apache.lucene.analysis.miscellaneous.ASCIIFoldingFilter
import org.apache.lucene.analysis.standard.StandardTokenizer
import java.io.Reader
import java.io.StringReader
import java.text.Normalizer

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

  // Normalize the input to Unicode NFC (precomposed form) before tokenizing.
  // This runs at both index time and query time, since createComponents/normalize read from
  // the reader returned here. Some filesystems (e.g. SMB shares on macOS) expose filenames in
  // decomposed form (NFD), so auto-generated titles would be indexed as NFD while search queries
  // are sent as NFC. Without normalization the two never match (e.g. Korean Hangul). Applying NFC
  // consistently on both sides makes NFD filenames searchable with NFC queries and vice versa.
  override fun initReader(
    fieldName: String?,
    reader: Reader,
  ): Reader = StringReader(Normalizer.normalize(reader.readText(), Normalizer.Form.NFC))

  override fun initReaderForNormalization(
    fieldName: String?,
    reader: Reader,
  ): Reader = StringReader(Normalizer.normalize(reader.readText(), Normalizer.Form.NFC))
}
