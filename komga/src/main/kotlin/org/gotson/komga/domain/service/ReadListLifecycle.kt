package org.gotson.komga.domain.service

import io.github.oshai.kotlinlogging.KotlinLogging
import org.gotson.komga.domain.model.Book
import org.gotson.komga.domain.model.DomainEvent
import org.gotson.komga.domain.model.DuplicateNameException
import org.gotson.komga.domain.model.ReadList
import org.gotson.komga.domain.model.ReadListRequestMatch
import org.gotson.komga.domain.model.ThumbnailReadList
import org.gotson.komga.domain.persistence.ReadListRepository
import org.gotson.komga.domain.persistence.ThumbnailReadListRepository
import org.gotson.komga.infrastructure.image.MosaicGenerator
import org.gotson.komga.infrastructure.metadata.comicrack.ReadListProvider
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.transaction.support.TransactionTemplate

private val logger = KotlinLogging.logger {}

@Service
class ReadListLifecycle(
  private val readListRepository: ReadListRepository,
  private val thumbnailReadListRepository: ThumbnailReadListRepository,
  private val bookLifecycle: BookLifecycle,
  private val mosaicGenerator: MosaicGenerator,
  private val readListMatcher: ReadListMatcher,
  private val readListProvider: ReadListProvider,
  private val eventPublisher: ApplicationEventPublisher,
  private val transactionTemplate: TransactionTemplate,
) {
  @Throws(
    DuplicateNameException::class,
  )
  @Transactional
  fun addReadList(readList: ReadList): ReadList {
    logger.info { "Adding new read list: $readList" }

    if (readListRepository.existsByName(readList.name))
      throw DuplicateNameException("Read list name already exists")

    readListRepository.insert(readList)

    eventPublisher.publishEvent(DomainEvent.ReadListAdded(readList))

    return readListRepository.findByIdOrNull(readList.id)!!
  }

  @Transactional
  fun updateReadList(toUpdate: ReadList) {
    logger.info { "Update read list: $toUpdate" }
    val existing =
      readListRepository.findByIdOrNull(toUpdate.id)
        ?: throw IllegalArgumentException("Cannot update read list that does not exist")

    if (!existing.name.equals(toUpdate.name, true) && readListRepository.existsByName(toUpdate.name))
      throw DuplicateNameException("Read list name already exists")

    readListRepository.update(toUpdate)

    eventPublisher.publishEvent(DomainEvent.ReadListUpdated(toUpdate))
  }

  fun deleteReadList(readList: ReadList) {
    transactionTemplate.executeWithoutResult {
      thumbnailReadListRepository.deleteByReadListId(readList.id)
      readListRepository.delete(readList.id)
    }

    eventPublisher.publishEvent(DomainEvent.ReadListDeleted(readList))
  }

  /**
   * Add book to read list by name.
   * Read list will be created if it doesn't exist.
   */
  @Transactional
  fun addBookToReadList(
    readListName: String,
    book: Book,
    numberInList: Int?,
  ) {
    readListRepository.findByNameOrNull(readListName).let { existing ->
      if (existing != null) {
        if (existing.bookIds.containsValue(book.id)) {
          logger.debug { "Book is already in existing read list '${existing.name}'" }
        } else {
          val map = existing.bookIds.toSortedMap()
          val key =
            if (numberInList != null && existing.bookIds.containsKey(numberInList)) {
              logger.debug { "Existing read list '${existing.name}' already contains a book at position $numberInList, adding book '${book.name}' at the end" }
              existing.bookIds.lastKey() + 1
            } else {
              logger.debug { "Adding book '${book.name}' to existing read list '${existing.name}'" }
              numberInList ?: (existing.bookIds.lastKey() + 1)
            }
          map[key] = book.id
          updateReadList(
            existing.copy(bookIds = map),
          )
        }
      } else {
        logger.debug { "Adding book '${book.name}' to new read list '$readListName'" }
        addReadList(
          ReadList(
            name = readListName,
            bookIds = mapOf((numberInList ?: 0) to book.id).toSortedMap(),
          ),
        )
      }
    }
  }

  fun deleteEmptyReadLists() {
    logger.info { "Deleting empty read lists" }
    transactionTemplate.executeWithoutResult {
      val toDelete = readListRepository.findAllEmpty()
      thumbnailReadListRepository.deleteByReadListIds(toDelete.map { it.id })
      readListRepository.delete(toDelete.map { it.id })

      toDelete.forEach { eventPublisher.publishEvent(DomainEvent.ReadListDeleted(it)) }
    }
  }

  fun addThumbnail(thumbnail: ThumbnailReadList): ThumbnailReadList {
    when (thumbnail.type) {
      ThumbnailReadList.Type.USER_UPLOADED -> {
        thumbnailReadListRepository.insert(thumbnail)
        if (thumbnail.selected) {
          thumbnailReadListRepository.markSelected(thumbnail)
        }
      }
    }

    eventPublisher.publishEvent(DomainEvent.ThumbnailReadListAdded(thumbnail))
    return thumbnail
  }

  fun markSelectedThumbnail(thumbnail: ThumbnailReadList) {
    thumbnailReadListRepository.markSelected(thumbnail)
    eventPublisher.publishEvent(DomainEvent.ThumbnailReadListAdded(thumbnail.copy(selected = true)))
  }

  fun deleteThumbnail(thumbnail: ThumbnailReadList) {
    thumbnailReadListRepository.delete(thumbnail.id)
    thumbnailsHouseKeeping(thumbnail.readListId)
    eventPublisher.publishEvent(DomainEvent.ThumbnailReadListDeleted(thumbnail))
  }

  fun getThumbnailBytes(thumbnailId: String): ByteArray? = thumbnailReadListRepository.findByIdOrNull(thumbnailId)?.thumbnail

  fun getThumbnailBytes(readList: ReadList): ByteArray {
    thumbnailReadListRepository.findSelectedByReadListIdOrNull(readList.id)?.let {
      return it.thumbnail
    }

    val ids =
      with(mutableListOf<String>()) {
        while (size < 4) {
          this += readList.bookIds.values.take(4)
        }
        this.take(4)
      }

    val images = ids.mapNotNull { bookLifecycle.getThumbnailBytes(it)?.bytes }
    return mosaicGenerator.createMosaic(images)
  }

  fun matchComicRackList(fileContent: ByteArray): ReadListRequestMatch {
    val request = readListProvider.importFromCbl(fileContent)

    return readListMatcher.matchReadListRequest(request)
  }

  private fun thumbnailsHouseKeeping(readListId: String) {
    logger.info { "House keeping thumbnails for read list: $readListId" }
    val all = thumbnailReadListRepository.findAllByReadListId(readListId)

    val selected = all.filter { it.selected }
    when {
      selected.size > 1 -> {
        logger.info { "More than one thumbnail is selected, removing extra ones" }
        thumbnailReadListRepository.markSelected(selected[0])
      }

      selected.isEmpty() && all.isNotEmpty() -> {
        logger.info { "Read list has no selected thumbnail, choosing one automatically" }
        thumbnailReadListRepository.markSelected(all.first())
      }
    }
  }
}
