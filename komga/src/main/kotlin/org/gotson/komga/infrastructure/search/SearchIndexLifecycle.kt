package org.gotson.komga.infrastructure.search

import mu.KotlinLogging
import org.apache.lucene.document.Document
import org.apache.lucene.index.Term
import org.gotson.komga.domain.model.BookSearchWithReadProgress
import org.gotson.komga.domain.model.DomainEvent
import org.gotson.komga.domain.model.ReadList
import org.gotson.komga.domain.model.SeriesCollection
import org.gotson.komga.domain.model.SeriesSearchWithReadProgress
import org.gotson.komga.domain.persistence.ReadListRepository
import org.gotson.komga.domain.persistence.SeriesCollectionRepository
import org.gotson.komga.infrastructure.jms.QUEUE_SSE
import org.gotson.komga.infrastructure.jms.QUEUE_SSE_SELECTOR
import org.gotson.komga.infrastructure.jms.TOPIC_FACTORY
import org.gotson.komga.interfaces.api.persistence.BookDtoRepository
import org.gotson.komga.interfaces.api.persistence.SeriesDtoRepository
import org.gotson.komga.interfaces.api.rest.dto.BookDto
import org.gotson.komga.interfaces.api.rest.dto.SeriesDto
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.jms.annotation.JmsListener
import org.springframework.stereotype.Component
import kotlin.math.ceil
import kotlin.time.measureTime

private val logger = KotlinLogging.logger {}
private const val INDEX_VERSION = 2

@Component
class SearchIndexLifecycle(
  private val collectionRepository: SeriesCollectionRepository,
  private val readListRepository: ReadListRepository,
  private val bookDtoRepository: BookDtoRepository,
  private val seriesDtoRepository: SeriesDtoRepository,
  private val luceneHelper: LuceneHelper,
) {

  fun rebuildIndex() {
    logger.info { "Rebuild all indexes" }

    LuceneEntity.values().forEach {
      when (it) {
        LuceneEntity.Book -> rebuildIndex(it, { p: Pageable -> bookDtoRepository.findAll(BookSearchWithReadProgress(), "unused", p) }, { e: BookDto -> e.toDocument() })
        LuceneEntity.Series -> rebuildIndex(it, { p: Pageable -> seriesDtoRepository.findAll(SeriesSearchWithReadProgress(), "unused", p) }, { e: SeriesDto -> e.toDocument() })
        LuceneEntity.Collection -> rebuildIndex(it, { p: Pageable -> collectionRepository.findAll(pageable = p) }, { e: SeriesCollection -> e.toDocument() })
        LuceneEntity.ReadList -> rebuildIndex(it, { p: Pageable -> readListRepository.findAll(pageable = p) }, { e: ReadList -> e.toDocument() })
      }
    }
  }

  private fun <T> rebuildIndex(entity: LuceneEntity, provider: (Pageable) -> Page<out T>, toDoc: (T) -> Document) {
    logger.info { "Rebuilding index for ${entity.name}" }

    val count = provider(Pageable.ofSize(1)).totalElements
    val batchSize = 5_000
    val pages = ceil(count.toDouble() / batchSize).toInt()
    logger.info { "Number of entities: $count" }

    luceneHelper.getIndexWriter().use { indexWriter ->
      measureTime {
        indexWriter.deleteDocuments(Term(LuceneEntity.TYPE, entity.type))

        (0 until pages).forEach { page ->
          logger.info { "Processing page $page of $batchSize elements" }
          val entityDocs = provider(PageRequest.of(page, batchSize)).content
            .map { toDoc(it) }
          indexWriter.addDocuments(entityDocs)
        }
      }.also { duration ->
        logger.info { "Wrote ${entity.name} index in $duration" }
      }
    }

    luceneHelper.setIndexVersion(INDEX_VERSION)
    logger.info { "Lucene index version: ${luceneHelper.getIndexVersion()}" }
  }

  @JmsListener(destination = QUEUE_SSE, selector = QUEUE_SSE_SELECTOR, containerFactory = TOPIC_FACTORY)
  fun consumeEvents(event: DomainEvent) {
    when (event) {
      is DomainEvent.SeriesAdded -> seriesDtoRepository.findByIdOrNull(event.series.id, "unused")?.toDocument()?.let { addEntity(it) }
      is DomainEvent.SeriesUpdated -> seriesDtoRepository.findByIdOrNull(event.series.id, "unused")?.toDocument()?.let { updateEntity(LuceneEntity.Series, event.series.id, it) }
      is DomainEvent.SeriesDeleted -> deleteEntity(LuceneEntity.Series, event.series.id)

      is DomainEvent.BookAdded -> bookDtoRepository.findByIdOrNull(event.book.id, "unused")?.toDocument()?.let { addEntity(it) }
      is DomainEvent.BookUpdated -> bookDtoRepository.findByIdOrNull(event.book.id, "unused")?.toDocument()?.let { updateEntity(LuceneEntity.Book, event.book.id, it) }
      is DomainEvent.BookDeleted -> deleteEntity(LuceneEntity.Book, event.book.id)

      is DomainEvent.ReadListAdded -> readListRepository.findByIdOrNull(event.readList.id)?.toDocument()?.let { addEntity(it) }
      is DomainEvent.ReadListUpdated -> readListRepository.findByIdOrNull(event.readList.id)?.toDocument()?.let { updateEntity(LuceneEntity.ReadList, event.readList.id, it) }
      is DomainEvent.ReadListDeleted -> deleteEntity(LuceneEntity.ReadList, event.readList.id)

      is DomainEvent.CollectionAdded -> collectionRepository.findByIdOrNull(event.collection.id)?.toDocument()?.let { addEntity(it) }
      is DomainEvent.CollectionUpdated -> collectionRepository.findByIdOrNull(event.collection.id)?.toDocument()?.let { updateEntity(LuceneEntity.Collection, event.collection.id, it) }
      is DomainEvent.CollectionDeleted -> deleteEntity(LuceneEntity.Collection, event.collection.id)

      else -> Unit
    }
  }

  private fun addEntity(doc: Document) {
    luceneHelper.getIndexWriter().use { indexWriter ->
      indexWriter.addDocument(doc)
    }
  }

  private fun updateEntity(entity: LuceneEntity, entityId: String, newDoc: Document) {
    luceneHelper.getIndexWriter().use { indexWriter ->
      indexWriter.updateDocument(Term(entity.id, entityId), newDoc)
    }
  }

  private fun deleteEntity(entity: LuceneEntity, entityId: String) {
    luceneHelper.getIndexWriter().use { indexWriter ->
      indexWriter.deleteDocuments(Term(entity.id, entityId))
    }
  }
}
