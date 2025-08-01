package org.gotson.komga.infrastructure.search

import org.apache.lucene.analysis.Analyzer
import org.apache.lucene.index.IndexWriter
import org.apache.lucene.index.IndexWriterConfig
import org.apache.lucene.search.SearcherFactory
import org.apache.lucene.search.SearcherManager
import org.apache.lucene.store.ByteBuffersDirectory
import org.apache.lucene.store.Directory
import org.apache.lucene.store.FSDirectory
import org.apache.lucene.store.SingleInstanceLockFactory
import org.gotson.komga.infrastructure.configuration.KomgaProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import java.nio.file.Paths

@Configuration
class LuceneConfiguration(
  private val komgaProperties: KomgaProperties,
) {
  @Bean
  fun indexAnalyzer() =
    with(komgaProperties.lucene.indexAnalyzer) {
      MultiLingualNGramAnalyzer(minGram, maxGram, preserveOriginal)
    }

  @Bean
  fun searchAnalyzer() = MultiLingualAnalyzer()

  @Bean
  @Profile("test")
  fun memoryDirectory(): Directory = ByteBuffersDirectory()

  @Bean
  @Profile("!test")
  fun diskDirectory(): Directory = FSDirectory.open(Paths.get(komgaProperties.lucene.dataDirectory), SingleInstanceLockFactory())

  @Bean
  fun indexWriter(
    directory: Directory,
    indexAnalyzer: Analyzer,
  ): IndexWriter = IndexWriter(directory, IndexWriterConfig(indexAnalyzer))

  @Bean
  fun searcherManager(indexWriter: IndexWriter) = SearcherManager(indexWriter, SearcherFactory())
}
