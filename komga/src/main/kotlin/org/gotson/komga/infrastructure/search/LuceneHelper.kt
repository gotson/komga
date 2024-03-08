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
import org.apache.lucene.search.SearcherFactory
import org.apache.lucene.search.SearcherManager
import org.apache.lucene.search.TermQuery
import org.apache.lucene.store.Directory
import org.springframework.beans.factory.annotation.Value
import org.springframework.scheduling.TaskScheduler
import org.springframework.stereotype.Component
import java.time.Duration
import java.time.ZonedDateTime
import java.util.concurrent.ScheduledFuture

private val logger = KotlinLogging.logger {}

private const val MAX_RESULTS = 1000

@Component
class LuceneHelper(
  private val directory: Directory,
  private val searchAnalyzer: Analyzer,
  private val taskScheduler: TaskScheduler,
  indexAnalyzer: Analyzer,
  @Value("#{@komgaProperties.lucene.commitDelay}")
  private val commitDelay: Duration,
) {
  private val indexWriterConfig = IndexWriterConfig(indexAnalyzer)
  private val indexWriter: IndexWriter = IndexWriter(directory, indexWriterConfig)
  private val searcherManager = SearcherManager(indexWriter, SearcherFactory())

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
    return topDocs.scoreDocs.map { searcher.storedFields().document(it.doc)["index_version"] }.firstOrNull()?.toIntOrNull() ?: 1
  }

  fun searchEntitiesIds(
    searchTerm: String?,
    entity: LuceneEntity,
  ): List<String>? {
    return if (!searchTerm.isNullOrBlank()) {
      try {
        val fieldsQuery =
          MultiFieldQueryParser(entity.defaultFields, searchAnalyzer).apply {
            defaultOperator = QueryParser.Operator.AND
          }.parse("$searchTerm *:*")

        val typeQuery = TermQuery(Term(LuceneEntity.TYPE, entity.type))

        val booleanQuery =
          BooleanQuery.Builder()
            .add(fieldsQuery, BooleanClause.Occur.MUST)
            .add(typeQuery, BooleanClause.Occur.MUST)
            .build()

        val searcher = searcherManager.acquire()
        val topDocs = searcher.search(booleanQuery, MAX_RESULTS)
        topDocs.scoreDocs.map { searcher.storedFields().document(it.doc)[entity.id] }
      } catch (e: ParseException) {
        emptyList()
      } catch (e: Exception) {
        logger.error(e) { "Error fetching entities from index" }
        emptyList()
      }
    } else {
      null
    }
  }

  fun upgradeIndex() {
    IndexUpgrader(directory, indexWriterConfig, true).upgrade()
    logger.info { "Lucene index upgraded" }
  }

  fun addDocument(doc: Document) {
    indexWriter.addDocument(doc)
    commitAndMaybeRefresh()
  }

  fun addDocuments(docs: Iterable<Document>) {
    indexWriter.addDocuments(docs)
    commitAndMaybeRefresh()
  }

  fun updateDocument(
    term: Term,
    doc: Document,
  ) {
    indexWriter.updateDocument(term, doc)
    commitAndMaybeRefresh()
  }

  fun deleteDocuments(term: Term) {
    indexWriter.deleteDocuments(term)
    commitAndMaybeRefresh()
  }

  @Volatile
  private var commitFuture: ScheduledFuture<*>? = null

  private val commitRunnable =
    Runnable {
      indexWriter.commit()
      searcherManager.maybeRefresh()
    }

  private fun commitAndMaybeRefresh() {
    if (commitFuture == null || commitFuture!!.isDone)
      commitFuture = taskScheduler.schedule(commitRunnable, ZonedDateTime.now().plus(commitDelay).toInstant())
  }
}
