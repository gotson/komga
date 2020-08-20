package org.gotson.komga.domain.service

import mu.KotlinLogging
import org.gotson.komga.domain.model.DuplicateNameException
import org.gotson.komga.domain.model.ReadList
import org.gotson.komga.domain.persistence.ReadListRepository
import org.gotson.komga.infrastructure.image.MosaicGenerator
import org.springframework.stereotype.Service

private val logger = KotlinLogging.logger {}

@Service
class ReadListLifecycle(
  private val readListRepository: ReadListRepository,
  private val bookLifecycle: BookLifecycle,
  private val mosaicGenerator: MosaicGenerator
) {

  @Throws(
    DuplicateNameException::class
  )
  fun addReadList(readList: ReadList): ReadList {
    logger.info { "Adding new read list: $readList" }

    if (readListRepository.existsByName(readList.name))
      throw DuplicateNameException("Read list name already exists")

    readListRepository.insert(readList)

    return readListRepository.findByIdOrNull(readList.id)!!
  }

  fun updateReadList(toUpdate: ReadList) {
    val existing = readListRepository.findByIdOrNull(toUpdate.id)
      ?: throw IllegalArgumentException("Cannot update read list that does not exist")

    if (existing.name != toUpdate.name && readListRepository.existsByName(toUpdate.name))
      throw DuplicateNameException("Read list name already exists")

    readListRepository.update(toUpdate)
  }

  fun deleteReadList(readListId: String) {
    readListRepository.delete(readListId)
  }

  fun getThumbnailBytes(readList: ReadList): ByteArray {
    val ids = with(mutableListOf<String>()) {
      while (size < 4) {
        this += readList.bookIds.values.take(4)
      }
      this.take(4)
    }

    val images = ids.mapNotNull { bookLifecycle.getThumbnailBytes(it) }
    return mosaicGenerator.createMosaic(images)
  }
}
