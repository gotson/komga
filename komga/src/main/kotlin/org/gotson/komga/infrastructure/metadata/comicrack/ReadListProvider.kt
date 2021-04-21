package org.gotson.komga.infrastructure.metadata.comicrack

import com.fasterxml.jackson.dataformat.xml.XmlMapper
import mu.KotlinLogging
import org.gotson.komga.domain.model.ReadListRequest
import org.gotson.komga.domain.model.ReadListRequestBook
import org.gotson.komga.infrastructure.metadata.comicrack.dto.ReadingList
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

private val logger = KotlinLogging.logger {}

@Service
class ReadListProvider(
  @Autowired(required = false) private val mapper: XmlMapper = XmlMapper(),
) {

  fun importFromCbl(cbl: ByteArray): ReadListRequest? {
    try {
      val readingList = mapper.readValue(cbl, ReadingList::class.java)
      if (readingList.books.isNotEmpty()) {
        logger.debug { "Trying to convert ComicRack ReadingList to ReadListRequest: $readingList" }
        if (readingList.name.isNullOrBlank()) {
          logger.warn { "ReadingList has no name, skipping" }
          return null
        }

        val books = readingList.books.mapNotNull {
          val series = computeSeriesFromSeriesAndVolume(it.series, it.volume)
          if (!series.isNullOrBlank() && it.number != null)
            ReadListRequestBook(series, it.number!!.trim())
          else {
            logger.warn { "Book is missing series or number, skipping: $it" }
            null
          }
        }

        if (books.isNotEmpty())
          return ReadListRequest(name = readingList.name!!, books = books)
            .also { logger.debug { "Converted request: $it" } }
      }
    } catch (e: Exception) {
      logger.error(e) { "Error while trying to parse ComicRack ReadingList" }
    }

    return null
  }
}
