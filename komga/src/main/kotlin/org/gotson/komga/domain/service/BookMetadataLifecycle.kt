package org.gotson.komga.domain.service

import mu.KotlinLogging
import org.gotson.komga.domain.model.Book
import org.gotson.komga.domain.model.BookMetadataPatch
import org.gotson.komga.domain.model.BookMetadataPatchCapability
import org.gotson.komga.domain.model.BookWithMedia
import org.gotson.komga.domain.model.DomainEvent
import org.gotson.komga.domain.model.MetadataPatchTarget
import org.gotson.komga.domain.model.ReadList
import org.gotson.komga.domain.persistence.BookMetadataRepository
import org.gotson.komga.domain.persistence.LibraryRepository
import org.gotson.komga.domain.persistence.MediaRepository
import org.gotson.komga.domain.persistence.ReadListRepository
import org.gotson.komga.infrastructure.metadata.BookMetadataProvider
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Service

private val logger = KotlinLogging.logger {}

@Service
class BookMetadataLifecycle(
  private val bookMetadataProviders: List<BookMetadataProvider>,
  private val metadataApplier: MetadataApplier,
  private val mediaRepository: MediaRepository,
  private val bookMetadataRepository: BookMetadataRepository,
  private val libraryRepository: LibraryRepository,
  private val readListRepository: ReadListRepository,
  private val readListLifecycle: ReadListLifecycle,
  private val eventPublisher: ApplicationEventPublisher,
) {

  fun refreshMetadata(book: Book, capabilities: Set<BookMetadataPatchCapability>) {
    logger.info { "Refresh metadata for book: $book with capabilities: $capabilities" }
    val media = mediaRepository.findById(book.id)

    val library = libraryRepository.findById(book.libraryId)
    var changed = false

    bookMetadataProviders.forEach { provider ->
      when {
        capabilities.intersect(provider.getCapabilities()).isEmpty() ->
          logger.info { "Provider does not support requested capabilities, skipping: ${provider.javaClass.simpleName}" }

        !(provider.shouldLibraryHandlePatch(library, MetadataPatchTarget.BOOK) || provider.shouldLibraryHandlePatch(library, MetadataPatchTarget.READLIST)) ->
          logger.info { "Library is not set to import book or read lists metadata for this provider, skipping: ${provider.javaClass.simpleName}" }

        else -> {
          logger.debug { "Provider: ${provider.javaClass.simpleName}" }
          val patch = provider.getBookMetadataFromBook(BookWithMedia(book, media))

          if (provider.shouldLibraryHandlePatch(library, MetadataPatchTarget.BOOK)) {
            handlePatchForBookMetadata(patch, book)
            changed = true
          }

          if (provider.shouldLibraryHandlePatch(library, MetadataPatchTarget.READLIST)) {
            handlePatchForReadLists(patch, book)
          }
        }
      }
    }

    if (changed) eventPublisher.publishEvent(DomainEvent.BookUpdated(book))
  }

  private fun handlePatchForReadLists(
    patch: BookMetadataPatch?,
    book: Book,
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
              readList.number ?: (existing.bookIds.lastKey() + 1)
            }
            map[key] = book.id
            readListLifecycle.updateReadList(
              existing.copy(bookIds = map),
            )
          }
        } else {
          logger.debug { "Adding book '${book.name}' to new read list '$readList'" }
          readListLifecycle.addReadList(
            ReadList(
              name = readList.name,
              bookIds = mapOf((readList.number ?: 0) to book.id).toSortedMap(),
            ),
          )
        }
      }
    }
  }

  private fun handlePatchForBookMetadata(
    patch: BookMetadataPatch?,
    book: Book,
  ) {
    patch?.let { bPatch ->
      bookMetadataRepository.findById(book.id).let {
        logger.debug { "Apply metadata for book: $book" }

        logger.debug { "Original metadata: $it" }
        logger.debug { "Patch: $bPatch" }
        val patched = metadataApplier.apply(bPatch, it)
        logger.debug { "Patched metadata: $patched" }

        bookMetadataRepository.update(patched)
      }
    }
  }
}
