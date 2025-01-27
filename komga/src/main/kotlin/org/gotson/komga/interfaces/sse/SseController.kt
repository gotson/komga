package org.gotson.komga.interfaces.sse

import io.github.oshai.kotlinlogging.KotlinLogging
import org.gotson.komga.application.tasks.TasksRepository
import org.gotson.komga.domain.model.DomainEvent
import org.gotson.komga.domain.model.KomgaUser
import org.gotson.komga.domain.persistence.BookRepository
import org.gotson.komga.infrastructure.security.KomgaPrincipal
import org.gotson.komga.infrastructure.web.toFilePath
import org.gotson.komga.interfaces.sse.dto.BookImportSseDto
import org.gotson.komga.interfaces.sse.dto.BookSseDto
import org.gotson.komga.interfaces.sse.dto.CollectionSseDto
import org.gotson.komga.interfaces.sse.dto.LibrarySseDto
import org.gotson.komga.interfaces.sse.dto.ReadListSseDto
import org.gotson.komga.interfaces.sse.dto.ReadProgressSeriesSseDto
import org.gotson.komga.interfaces.sse.dto.ReadProgressSseDto
import org.gotson.komga.interfaces.sse.dto.SeriesSseDto
import org.gotson.komga.interfaces.sse.dto.SessionExpiredDto
import org.gotson.komga.interfaces.sse.dto.TaskQueueSseDto
import org.gotson.komga.interfaces.sse.dto.ThumbnailBookSseDto
import org.gotson.komga.interfaces.sse.dto.ThumbnailReadListSseDto
import org.gotson.komga.interfaces.sse.dto.ThumbnailSeriesCollectionSseDto
import org.gotson.komga.interfaces.sse.dto.ThumbnailSeriesSseDto
import org.springframework.context.SmartLifecycle
import org.springframework.context.event.EventListener
import org.springframework.http.MediaType
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter
import java.io.IOException
import java.util.Collections

private val logger = KotlinLogging.logger {}

@Controller
class SseController(
  private val bookRepository: BookRepository,
  private val tasksRepository: TasksRepository,
) : SmartLifecycle {
  private var acceptingConnections = true
  private val emitters = Collections.synchronizedMap(HashMap<SseEmitter, KomgaUser>())

  @GetMapping("sse/v1/events")
  fun sse(
    @AuthenticationPrincipal principal: KomgaPrincipal,
  ): SseEmitter {
    if (!acceptingConnections) throw IllegalStateException("Server is shutting down, not accepting new SSE connections")
    val emitter = SseEmitter()
    emitter.onCompletion { synchronized(emitters) { emitters.remove(emitter) } }
    emitter.onTimeout { synchronized(emitters) { emitters.remove(emitter) } }
    emitter.onError { synchronized(emitters) { emitters.remove(emitter) } }
    emitters[emitter] = principal.user
    return emitter
  }

  @Scheduled(fixedRate = 10_000)
  fun taskCount() {
    if (emitters.isNotEmpty()) {
      val tasksCount = tasksRepository.countBySimpleType()
      emitSse("TaskQueueStatus", TaskQueueSseDto(tasksCount.values.sum(), tasksCount), adminOnly = true)
    }
  }

  @EventListener
  fun handleSseEvent(event: DomainEvent) {
    when (event) {
      is DomainEvent.LibraryAdded -> emitSse("LibraryAdded", LibrarySseDto(event.library.id))
      is DomainEvent.LibraryUpdated -> emitSse("LibraryChanged", LibrarySseDto(event.library.id))
      is DomainEvent.LibraryDeleted -> emitSse("LibraryDeleted", LibrarySseDto(event.library.id))
      is DomainEvent.LibraryScanned -> Unit

      is DomainEvent.SeriesAdded -> emitSse("SeriesAdded", SeriesSseDto(event.series.id, event.series.libraryId))
      is DomainEvent.SeriesUpdated -> emitSse("SeriesChanged", SeriesSseDto(event.series.id, event.series.libraryId))
      is DomainEvent.SeriesDeleted -> emitSse("SeriesDeleted", SeriesSseDto(event.series.id, event.series.libraryId))

      is DomainEvent.BookAdded -> emitSse("BookAdded", BookSseDto(event.book.id, event.book.seriesId, event.book.libraryId))
      is DomainEvent.BookUpdated -> emitSse("BookChanged", BookSseDto(event.book.id, event.book.seriesId, event.book.libraryId))
      is DomainEvent.BookDeleted -> emitSse("BookDeleted", BookSseDto(event.book.id, event.book.seriesId, event.book.libraryId))
      is DomainEvent.BookImported -> emitSse("BookImported", BookImportSseDto(event.book?.id, event.sourceFile.toFilePath(), event.success, event.message), adminOnly = true)

      is DomainEvent.ReadListAdded -> emitSse("ReadListAdded", ReadListSseDto(event.readList.id, event.readList.bookIds.map { it.value }))
      is DomainEvent.ReadListUpdated -> emitSse("ReadListChanged", ReadListSseDto(event.readList.id, event.readList.bookIds.map { it.value }))
      is DomainEvent.ReadListDeleted -> emitSse("ReadListDeleted", ReadListSseDto(event.readList.id, event.readList.bookIds.map { it.value }))

      is DomainEvent.CollectionAdded -> emitSse("CollectionAdded", CollectionSseDto(event.collection.id, event.collection.seriesIds))
      is DomainEvent.CollectionUpdated -> emitSse("CollectionChanged", CollectionSseDto(event.collection.id, event.collection.seriesIds))
      is DomainEvent.CollectionDeleted -> emitSse("CollectionDeleted", CollectionSseDto(event.collection.id, event.collection.seriesIds))

      is DomainEvent.ReadProgressChanged -> emitSse("ReadProgressChanged", ReadProgressSseDto(event.progress.bookId, event.progress.userId), userIdOnly = event.progress.userId)
      is DomainEvent.ReadProgressDeleted -> emitSse("ReadProgressDeleted", ReadProgressSseDto(event.progress.bookId, event.progress.userId), userIdOnly = event.progress.userId)
      is DomainEvent.ReadProgressSeriesChanged -> emitSse("ReadProgressSeriesChanged", ReadProgressSeriesSseDto(event.seriesId, event.userId), userIdOnly = event.userId)
      is DomainEvent.ReadProgressSeriesDeleted -> emitSse("ReadProgressSeriesDeleted", ReadProgressSeriesSseDto(event.seriesId, event.userId), userIdOnly = event.userId)

      is DomainEvent.ThumbnailBookAdded -> emitSse("ThumbnailBookAdded", ThumbnailBookSseDto(event.thumbnail.bookId, bookRepository.getSeriesIdOrNull(event.thumbnail.bookId).orEmpty(), event.thumbnail.selected))
      is DomainEvent.ThumbnailBookDeleted -> emitSse("ThumbnailBookDeleted", ThumbnailBookSseDto(event.thumbnail.bookId, bookRepository.getSeriesIdOrNull(event.thumbnail.bookId).orEmpty(), event.thumbnail.selected))
      is DomainEvent.ThumbnailSeriesAdded -> emitSse("ThumbnailSeriesAdded", ThumbnailSeriesSseDto(event.thumbnail.seriesId, event.thumbnail.selected))
      is DomainEvent.ThumbnailSeriesDeleted -> emitSse("ThumbnailSeriesDeleted", ThumbnailSeriesSseDto(event.thumbnail.seriesId, event.thumbnail.selected))
      is DomainEvent.ThumbnailSeriesCollectionAdded -> emitSse("ThumbnailSeriesCollectionAdded", ThumbnailSeriesCollectionSseDto(event.thumbnail.collectionId, event.thumbnail.selected))
      is DomainEvent.ThumbnailSeriesCollectionDeleted -> emitSse("ThumbnailSeriesCollectionDeleted", ThumbnailSeriesCollectionSseDto(event.thumbnail.collectionId, event.thumbnail.selected))
      is DomainEvent.ThumbnailReadListAdded -> emitSse("ThumbnailReadListAdded", ThumbnailReadListSseDto(event.thumbnail.readListId, event.thumbnail.selected))
      is DomainEvent.ThumbnailReadListDeleted -> emitSse("ThumbnailReadListDeleted", ThumbnailReadListSseDto(event.thumbnail.readListId, event.thumbnail.selected))

      is DomainEvent.UserUpdated -> if (event.expireSession) emitSse("SessionExpired", SessionExpiredDto(event.user.id), userIdOnly = event.user.id)
      is DomainEvent.UserDeleted -> emitSse("SessionExpired", SessionExpiredDto(event.user.id), userIdOnly = event.user.id)
    }
  }

  private fun emitSse(
    name: String,
    data: Any,
    adminOnly: Boolean = false,
    userIdOnly: String? = null,
  ) {
    logger.debug { "Publish SSE: '$name':$data" }

    synchronized(emitters) {
      emitters
        .filter { if (adminOnly) it.value.isAdmin else true }
        .filter { if (userIdOnly != null) it.value.id == userIdOnly else true }
        .forEach { (emitter, _) ->
          try {
            emitter.send(
              SseEmitter
                .event()
                .name(name)
                .data(data, MediaType.APPLICATION_JSON),
            )
          } catch (_: IOException) {
          }
        }
    }
  }

  override fun start() = Unit

  override fun stop() {
    logger.debug { "Closing all SSE connections" }
    acceptingConnections = false
    synchronized(emitters) {
      emitters.forEach { (emitter, _) -> emitter.complete() }
    }
  }

  override fun isRunning(): Boolean = true

  override fun getPhase(): Int = SmartLifecycle.DEFAULT_PHASE
}
