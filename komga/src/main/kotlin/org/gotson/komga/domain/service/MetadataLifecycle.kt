package org.gotson.komga.domain.service

import mu.KotlinLogging
import org.gotson.komga.application.events.EventPublisher
import org.gotson.komga.domain.model.Book
import org.gotson.komga.domain.model.BookMetadataPatch
import org.gotson.komga.domain.model.BookMetadataPatchCapability
import org.gotson.komga.domain.model.BookWithMedia
import org.gotson.komga.domain.model.DomainEvent
import org.gotson.komga.domain.model.ReadList
import org.gotson.komga.domain.model.Series
import org.gotson.komga.domain.model.SeriesCollection
import org.gotson.komga.domain.model.SeriesMetadataPatch
import org.gotson.komga.domain.persistence.BookMetadataAggregationRepository
import org.gotson.komga.domain.persistence.BookMetadataRepository
import org.gotson.komga.domain.persistence.BookRepository
import org.gotson.komga.domain.persistence.LibraryRepository
import org.gotson.komga.domain.persistence.MediaRepository
import org.gotson.komga.domain.persistence.ReadListRepository
import org.gotson.komga.domain.persistence.SeriesCollectionRepository
import org.gotson.komga.domain.persistence.SeriesMetadataRepository
import org.gotson.komga.infrastructure.metadata.BookMetadataProvider
import org.gotson.komga.infrastructure.metadata.SeriesMetadataProvider
import org.gotson.komga.infrastructure.metadata.barcode.IsbnBarcodeProvider
import org.gotson.komga.infrastructure.metadata.comicrack.ComicInfoProvider
import org.gotson.komga.infrastructure.metadata.epub.EpubMetadataProvider
import org.springframework.stereotype.Service

private val logger = KotlinLogging.logger {}

@Service
class MetadataLifecycle(
  private val bookMetadataProviders: List<BookMetadataProvider>,
  private val seriesMetadataProviders: List<SeriesMetadataProvider>,
  private val metadataApplier: MetadataApplier,
  private val metadataAggregator: MetadataAggregator,
  private val mediaRepository: MediaRepository,
  private val bookMetadataRepository: BookMetadataRepository,
  private val seriesMetadataRepository: SeriesMetadataRepository,
  private val bookMetadataAggregationRepository: BookMetadataAggregationRepository,
  private val libraryRepository: LibraryRepository,
  private val bookRepository: BookRepository,
  private val collectionRepository: SeriesCollectionRepository,
  private val collectionLifecycle: SeriesCollectionLifecycle,
  private val readListRepository: ReadListRepository,
  private val readListLifecycle: ReadListLifecycle,
  private val eventPublisher: EventPublisher,
) {

  fun refreshMetadata(book: Book, capabilities: List<BookMetadataPatchCapability>) {
    logger.info { "Refresh metadata for book: $book with capabilities: $capabilities" }
    val media = mediaRepository.findById(book.id)

    val library = libraryRepository.findById(book.libraryId)
    var changed = false

    bookMetadataProviders.forEach { provider ->
      when {
        capabilities.intersect(provider.getCapabilities()).isEmpty() ->
          logger.info { "Provider does not support requested capabilities, skipping: $provider" }
        provider is ComicInfoProvider && !library.importComicInfoBook && !library.importComicInfoReadList ->
          logger.info { "Library is not set to import book and read lists metadata from ComicInfo, skipping" }
        provider is EpubMetadataProvider && !library.importEpubBook ->
          logger.info { "Library is not set to import book metadata from Epub, skipping" }
        provider is IsbnBarcodeProvider && !library.importBarcodeIsbn ->
          logger.info { "Library is not set to import book metadata from Barcode ISBN, skipping" }
        else -> {
          logger.debug { "Provider: ${provider::class.simpleName}" }
          val patch = provider.getBookMetadataFromBook(BookWithMedia(book, media))

          if (
            (provider is ComicInfoProvider && library.importComicInfoBook) ||
            (provider is EpubMetadataProvider && library.importEpubBook) ||
            (provider is IsbnBarcodeProvider && library.importBarcodeIsbn)
          ) {
            handlePatchForBookMetadata(patch, book)
            changed = true
          }

          if (provider is ComicInfoProvider && library.importComicInfoReadList) {
            handlePatchForReadLists(patch, book)
          }
        }
      }
    }

    if (changed) eventPublisher.publishEvent(DomainEvent.BookUpdated(book))
  }

  private fun handlePatchForReadLists(
    patch: BookMetadataPatch?,
    book: Book
  ) {
    patch?.readLists?.forEach { readList ->

      readListRepository.findByNameOrNull(readList.name).let { existing ->
        if (existing != null) {
          if (existing.bookIds.containsValue(book.id))
            logger.debug { "Book is already in existing read list '${existing.name}'" }
          else {
            val map = existing.bookIds.toSortedMap()
            val key = if (readList.number != null && existing.bookIds.containsKey(readList.number)) {
              logger.debug { "Existing read list '${existing.name}' already contains a book at position ${readList.number}, adding book '${book.name}' at the end" }
              existing.bookIds.lastKey() + 1
            } else {
              logger.debug { "Adding book '${book.name}' to existing read list '${existing.name}'" }
              readList.number ?: existing.bookIds.lastKey() + 1
            }
            map[key] = book.id
            readListLifecycle.updateReadList(
              existing.copy(bookIds = map)
            )
          }
        } else {
          logger.debug { "Adding book '${book.name}' to new read list '$readList'" }
          readListLifecycle.addReadList(
            ReadList(
              name = readList.name,
              bookIds = mapOf((readList.number ?: 0) to book.id).toSortedMap()
            )
          )
        }
      }
    }
  }

  private fun handlePatchForBookMetadata(
    patch: BookMetadataPatch?,
    book: Book
  ) {
    patch?.let { bPatch ->
      bookMetadataRepository.findById(book.id).let {
        logger.debug { "Apply metadata for book: $book" }

        logger.debug { "Original metadata: $it" }
        val patched = metadataApplier.apply(bPatch, it)
        logger.debug { "Patched metadata: $patched" }

        bookMetadataRepository.update(patched)
      }
    }
  }

  fun refreshMetadata(series: Series) {
    logger.info { "Refresh metadata for series: $series" }

    val library = libraryRepository.findById(series.libraryId)
    var changed = false

    seriesMetadataProviders.forEach { provider ->
      when {
        provider is ComicInfoProvider && !library.importComicInfoSeries && !library.importComicInfoCollection -> logger.info { "Library is not set to import series and collection metadata from ComicInfo, skipping" }
        provider is EpubMetadataProvider && !library.importEpubSeries -> logger.info { "Library is not set to import series metadata from Epub, skipping" }
        else -> {
          logger.debug { "Provider: ${provider::class.simpleName}" }
          val patches = bookRepository.findAllBySeriesId(series.id)
            .mapNotNull { provider.getSeriesMetadataFromBook(BookWithMedia(it, mediaRepository.findById(it.id))) }

          if (
            (provider is ComicInfoProvider && library.importComicInfoSeries) ||
            (provider is EpubMetadataProvider && library.importEpubSeries)
          ) {
            handlePatchForSeriesMetadata(patches, series)
            changed = true
          }

          if (provider is ComicInfoProvider && library.importComicInfoCollection) {
            handlePatchForCollections(patches, series)
          }
        }
      }
    }

    if (changed) eventPublisher.publishEvent(DomainEvent.SeriesUpdated(series))
  }

  private fun handlePatchForCollections(
    patches: List<SeriesMetadataPatch>,
    series: Series
  ) {
    patches.flatMap { it.collections }.distinct().forEach { collection ->
      collectionRepository.findByNameOrNull(collection).let { existing ->
        if (existing != null) {
          if (existing.seriesIds.contains(series.id))
            logger.debug { "Series is already in existing collection '${existing.name}'" }
          else {
            logger.debug { "Adding series '${series.name}' to existing collection '${existing.name}'" }
            collectionLifecycle.updateCollection(
              existing.copy(seriesIds = existing.seriesIds + series.id)
            )
          }
        } else {
          logger.debug { "Adding series '${series.name}' to new collection '$collection'" }
          collectionLifecycle.addCollection(
            SeriesCollection(
              name = collection,
              seriesIds = listOf(series.id)
            )
          )
        }
      }
    }
  }

  private fun handlePatchForSeriesMetadata(
    patches: List<SeriesMetadataPatch>,
    series: Series
  ) {
    val aggregatedPatch = SeriesMetadataPatch(
      title = patches.mostFrequent { it.title },
      titleSort = patches.mostFrequent { it.titleSort },
      status = patches.mostFrequent { it.status },
      genres = patches.mapNotNull { it.genres }.flatten().toSet().ifEmpty { null },
      language = patches.mostFrequent { it.language },
      summary = null,
      readingDirection = patches.mostFrequent { it.readingDirection },
      ageRating = patches.mapNotNull { it.ageRating }.maxOrNull(),
      publisher = patches.mostFrequent { it.publisher },
      collections = emptyList()
    )

    seriesMetadataRepository.findById(series.id).let {
      logger.debug { "Apply metadata for series: $series" }

      logger.debug { "Original metadata: $it" }
      val patched = metadataApplier.apply(aggregatedPatch, it)
      logger.debug { "Patched metadata: $patched" }

      seriesMetadataRepository.update(patched)
    }
  }

  fun aggregateMetadata(series: Series) {
    logger.info { "Aggregate book metadata for series: $series" }

    val metadatas = bookMetadataRepository.findAllByIds(bookRepository.findAllIdsBySeriesId(series.id))
    val aggregation = metadataAggregator.aggregate(metadatas).copy(seriesId = series.id)

    bookMetadataAggregationRepository.update(aggregation)

    eventPublisher.publishEvent(DomainEvent.SeriesUpdated(series))
  }

  private fun <T, R : Any> Iterable<T>.mostFrequent(transform: (T) -> R?): R? {
    return this
      .mapNotNull(transform)
      .groupingBy { it }
      .eachCount()
      .maxByOrNull { it.value }?.key
  }
}
