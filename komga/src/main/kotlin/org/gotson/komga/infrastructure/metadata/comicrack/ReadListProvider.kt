package org.gotson.komga.infrastructure.metadata.comicrack

import com.fasterxml.jackson.dataformat.xml.XmlMapper
import io.github.oshai.kotlinlogging.KotlinLogging
import org.gotson.komga.domain.model.ComicRackListException
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
  @Throws(ComicRackListException::class)
  fun importFromCbl(cbl: ByteArray): ReadListRequest {
    val readingList =
      try {
        mapper.readValue(cbl, ReadingList::class.java)
      } catch (e: Exception) {
        logger.error(e) { "Error while trying to parse ComicRack ReadingList" }
        throw ComicRackListException("Error while trying to parse ComicRack ReadingList", "ERR_1015")
      }
    logger.debug { "Trying to convert ComicRack ReadingList to ReadListRequest: $readingList" }

    if (readingList.name.isNullOrBlank()) throw ComicRackListException("ReadingList has no Name element", "ERR_1030")
    if (readingList.books.isEmpty()) throw ComicRackListException("ReadingList does not contain any Book element", "ERR_1029")

    val books =
      readingList.books.map {
        if (it.series.isNullOrBlank() || it.number == null) throw ComicRackListException("Book is missing series or number: $it", "ERR_1031")
        val series = setOfNotNull(computeSeriesFromSeriesAndVolume(it.series, it.volume), it.series?.ifBlank { null })
        ReadListRequestBook(series, it.number!!.trim())
      }

    return ReadListRequest(name = readingList.name!!, books = books)
      .also { logger.debug { "Converted request: $it" } }
  }
}
