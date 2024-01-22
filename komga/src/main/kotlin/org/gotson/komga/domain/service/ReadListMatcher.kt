package org.gotson.komga.domain.service

import io.github.oshai.kotlinlogging.KotlinLogging
import org.gotson.komga.domain.model.ReadListMatch
import org.gotson.komga.domain.model.ReadListRequest
import org.gotson.komga.domain.model.ReadListRequestMatch
import org.gotson.komga.domain.persistence.ReadListRepository
import org.gotson.komga.domain.persistence.ReadListRequestRepository
import org.springframework.stereotype.Service

private val logger = KotlinLogging.logger {}

@Service
class ReadListMatcher(
  private val readListRepository: ReadListRepository,
  private val readListRequestRepository: ReadListRequestRepository,
) {
  fun matchReadListRequest(request: ReadListRequest): ReadListRequestMatch {
    logger.info { "Trying to match $request" }

    val readListMatch =
      if (readListRepository.existsByName(request.name))
        ReadListMatch(request.name, "ERR_1009")
      else
        ReadListMatch(request.name)

    val matches = readListRequestRepository.matchBookRequests(request.books)

    return ReadListRequestMatch(readListMatch, matches)
  }
}
