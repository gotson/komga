package org.gotson.komga.domain.service

import mu.KotlinLogging
import org.gotson.komga.domain.model.Book
import org.gotson.komga.domain.model.BookMetadataPatch
import org.gotson.komga.domain.model.BookMetadataPatchCapability
import org.gotson.komga.domain.model.BookWithMedia
import org.gotson.komga.domain.model.ReadList
import org.gotson.komga.domain.persistence.BookMetadataRepository
import org.gotson.komga.domain.persistence.LibraryRepository
import org.gotson.komga.domain.persistence.MediaRepository
import org.gotson.komga.domain.persistence.ReadListRepository
import org.gotson.komga.infrastructure.metadata.BookMetadataProvider
import org.gotson.komga.infrastructure.metadata.barcode.IsbnBarcodeProvider
import org.gotson.komga.infrastructure.metadata.comicrack.ComicInfoProvider
import org.gotson.komga.infrastructure.metadata.epub.EpubMetadataProvider
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
  private val readListLifecycle: ReadListLifecycle
) {

  fun refreshMetadata(book: Book, capabilities: List<BookMetadataPatchCapability>) {
    logger.info { "Refresh metadata for book: $book with capabilities: $capabilities" }
    val media = mediaRepository.findById(book.id)

    val library = libraryRepository.findById(book.libraryId)

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
          logger.debug { "Provider: $provider" }
          val patch = provider.getBookMetadataFromBook(BookWithMedia(book, media))

          if (
            (provider is ComicInfoProvider && library.importComicInfoBook) ||
            (provider is EpubMetadataProvider && library.importEpubBook) ||
            (provider is IsbnBarcodeProvider && library.importBarcodeIsbn)
          ) {
            handlePatchForBookMetadata(patch, book)
          }

          if (provider is ComicInfoProvider && library.importComicInfoReadList) {
            handlePatchForReadLists(patch, book)
          }
        }
      }
    }
  }

  private fun handlePatchForReadLists(
    patch: BookMetadataPatch?,
    book: Book
  ) {
    patch?.readLists?.forEach { readList ->

      readListRepository.findByNameOrNull(readList.name).let { existing ->
        if (existing != null) {
          if (existing.bookIds.containsValue(book.id))
            logger.debug { "Book is already in existing readlist '${existing.name}'" }
          else {
            val map = existing.bookIds.toSortedMap()
            val key = if (readList.number != null && existing.bookIds.containsKey(readList.number)) {
              logger.debug { "Existing readlist '${existing.name}' already contains a book at position ${readList.number}, adding book '${book.name}' at the end" }
              existing.bookIds.lastKey() + 1
            } else {
              logger.debug { "Adding book '${book.name}' to existing readlist '${existing.name}'" }
              readList.number ?: existing.bookIds.lastKey() + 1
            }
            map[key] = book.id
            readListLifecycle.updateReadList(
              existing.copy(bookIds = map)
            )
          }
        } else {
          logger.debug { "Adding book '${book.name}' to new readlist '$readList'" }
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
        logger.debug { "Original metadata: $it" }
        val patched = metadataApplier.apply(bPatch, it)
        logger.debug { "Patched metadata: $patched" }

        bookMetadataRepository.update(patched)
      }
    }
  }
}
