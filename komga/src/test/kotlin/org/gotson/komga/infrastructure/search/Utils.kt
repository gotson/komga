package org.gotson.komga.infrastructure.search

import org.apache.lucene.analysis.Analyzer

fun Analyzer.getTokens(text: String): List<String> {
  val tokenStream = tokenStream("text", text)

  val tokens = mutableListOf<String>()
  tokenStream.use { ts ->
    ts.reset()
    while (ts.incrementToken()) {
      ts.reflectWith { _, key, value -> if (key == "term") tokens += value.toString() }
    }
    ts.end()
  }

  return tokens
}
