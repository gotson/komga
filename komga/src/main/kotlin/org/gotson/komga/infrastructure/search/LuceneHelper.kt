package org.gotson.komga.infrastructure.search

import io.github.oshai.kotlinlogging.KotlinLogging
import org.apache.lucene.analysis.Analyzer
import org.apache.lucene.document.Document
import org.apache.lucene.document.Field
import org.apache.lucene.document.StringField
import org.apache.lucene.index.DirectoryReader
import org.apache.lucene.index.IndexUpgrader
import org.apache.lucene.index.IndexWriter
import org.apache.lucene.index.IndexWriterConfig
import org.apache.lucene.index.Term
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser
import org.apache.lucene.queryparser.classic.ParseException
import org.apache.lucene.queryparser.classic.QueryParser
import org.apache.lucene.search.BooleanClause
import org.apache.lucene.search.BooleanQuery
import org.apache.lucene.search.SearcherManager
import org.apache.lucene.search.TermQuery
import org.apache.lucene.store.Directory
import org.springframework.stereotype.Component

private val logger = KotlinLogging.logger {}

private const val MAX_RESULTS = 1000

@Component
class LuceneHelper(
  private val directory: Directory,
  private val searchAnalyzer: Analyzer,
  private val indexAnalyzer: Analyzer,
  private val indexWriter: IndexWriter,
  private val searcherManager: SearcherManager,
  private val luceneCommitter: LuceneCommitter,
) {
  fun indexExists(): Boolean = DirectoryReader.indexExists(directory)

  fun setIndexVersion(version: Int) {
    val doc =
      Document().apply {
        add(StringField("index_version", version.toString(), Field.Store.YES))
        add(StringField("type", "index_version", Field.Store.NO))
      }
    updateDocument(Term("type", "index_version"), doc)
    logger.info { "Lucene index version: ${getIndexVersion()}" }
  }

  fun getIndexVersion(): Int {
    val searcher = searcherManager.acquire()
    val topDocs = searcher.search(TermQuery(Term("type", "index_version")), 1)
    return topDocs.scoreDocs
      .map { searcher.storedFields().document(it.doc)["index_version"] }
      .firstOrNull()
      ?.toIntOrNull() ?: 1
  }

  fun searchEntitiesIds(
    searchTerm: String?,
    entity: LuceneEntity,
  ): List<String>? =
    if (!searchTerm.isNullOrBlank()) {
      try {
        val fieldsQuery =
          MultiFieldQueryParser(entity.defaultFields, searchAnalyzer)
            .apply {
              defaultOperator = QueryParser.Operator.AND
            }.parse("$searchTerm *:*")

        val typeQuery = TermQuery(Term(LuceneEntity.TYPE, entity.type))

        val booleanQuery =
          BooleanQuery
            .Builder()
            .add(fieldsQuery, BooleanClause.Occur.MUST)
            .add(typeQuery, BooleanClause.Occur.MUST)
            .build()

        val searcher = searcherManager.acquire()
        val topDocs = searcher.search(booleanQuery, MAX_RESULTS)
        topDocs.scoreDocs.map { searcher.storedFields().document(it.doc)[entity.id] }
      } catch (_: ParseException) {
        emptyList()
      } catch (e: Exception) {
        logger.error(e) { "Error fetching entities from index" }
        emptyList()
      }
    } else {
      null
    }

  fun upgradeIndex() {
    IndexUpgrader(directory, IndexWriterConfig(indexAnalyzer), true).upgrade()
    logger.info { "Lucene index upgraded" }
  }

  fun addDocument(doc: Document) {
    indexWriter.addDocument(doc)
    luceneCommitter.commitAndMaybeRefresh()
  }

  fun addDocuments(docs: Iterable<Document>) {
    indexWriter.addDocuments(docs)
    luceneCommitter.commitAndMaybeRefresh()
  }

  fun updateDocument(
    term: Term,
    doc: Document,
  ) {
    indexWriter.updateDocument(term, doc)
    luceneCommitter.commitAndMaybeRefresh()
  }

  fun deleteDocuments(term: Term) {
    indexWriter.deleteDocuments(term)
    luceneCommitter.commitAndMaybeRefresh()
  }
}
