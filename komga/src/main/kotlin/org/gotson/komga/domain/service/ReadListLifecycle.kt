package org.gotson.komga.domain.service

import mu.KotlinLogging
import org.gotson.komga.domain.model.DuplicateNameException
import org.gotson.komga.domain.model.ReadList
import org.gotson.komga.domain.model.ReadListRequestResult
import org.gotson.komga.domain.persistence.ReadListRepository
import org.gotson.komga.infrastructure.image.MosaicGenerator
import org.gotson.komga.infrastructure.metadata.comicrack.ReadListProvider
import org.springframework.stereotype.Service

private val logger = KotlinLogging.logger {}

@Service
class ReadListLifecycle(
  private val readListRepository: ReadListRepository,
  private val bookLifecycle: BookLifecycle,
  private val mosaicGenerator: MosaicGenerator,
  private val readListMatcher: ReadListMatcher,
  private val readListProvider: ReadListProvider,
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
    logger.info { "Update read list: $toUpdate" }
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

  fun importReadList(fileContent: ByteArray): ReadListRequestResult {
    val request = try {
      readListProvider.importFromCbl(fileContent) ?: return ReadListRequestResult(null, emptyList(), "ERR_1015")
    } catch (e: Exception) {
      return ReadListRequestResult(null, emptyList(), "ERR_1015")
    }

    val result = readListMatcher.matchReadListRequest(request)
    return when {
      result.readList != null -> {
        readListRepository.insert(result.readList)
        result.copy(readList = readListRepository.findByIdOrNull(result.readList.id)!!)
      }
      else -> result
    }
  }
}
