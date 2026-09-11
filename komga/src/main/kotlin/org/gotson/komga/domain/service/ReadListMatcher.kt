package org.gotson.komga.domain.service

import io.github.oshai.kotlinlogging.KotlinLogging
import org.gotson.komga.domain.model.ReadListMatch
import org.gotson.komga.domain.model.ReadListRequest
import org.gotson.komga.domain.model.ReadListRequestBookMatchSeries
import org.gotson.komga.domain.model.ReadListRequestBookMatches
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

    val matches = readListRequestRepository.matchBookRequests(request.books).map { it.sortMatchesBySeriesYear() }

    return ReadListRequestMatch(readListMatch, matches)
  }

  /**
   * Sorts the matched series by how well they corroborate the requested series year, best match first.
   *
   * A request matches every series with that exact title, but also the ones where the volume was
   * appended to the title, like `Batman (2016)`. Clients select the first match by default, so the
   * most likely candidate has to come first.
   */
  private fun ReadListRequestBookMatches.sortMatchesBySeriesYear(): ReadListRequestBookMatches {
    val year = request.seriesYear
    if (year == null || matches.size < 2) return this

    return copy(
      matches =
        matches.entries
          .sortedByDescending { (series, _) -> series.seriesYearScore(year) }
          .associate { (series, books) -> series to books },
    ).also { sorted -> logger.debug { "Sorted matches for $request by series year $year: ${sorted.matches.keys.map { it.title }}" } }
  }

  /**
   * Scores how much a matched series corroborates [year]. A volume appended to the title is an explicit
   * signal, and ranks higher than the release date of the first book of the series.
   */
  private fun ReadListRequestBookMatchSeries.seriesYearScore(year: Int): Int =
    (if (title.trimEnd().endsWith(" ($year)")) 2 else 0) +
      (if (releaseDate?.year == year) 1 else 0)
}
