package org.gotson.komga.domain.service

import io.github.oshai.kotlinlogging.KotlinLogging
import org.gotson.komga.domain.model.ReadListMatch
import org.gotson.komga.domain.model.ReadListRequest
import org.gotson.komga.domain.model.ReadListRequestBookMatchBook
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

    val matches = readListRequestRepository.matchBookRequests(request.books).map { it.sortMatchesByYear() }

    return ReadListRequestMatch(readListMatch, matches)
  }

  /**
   * Sorts the matched series, and the books within them, by how well they corroborate the years of the request.
   *
   * A request matches every series with that exact title, but also the ones where the volume was
   * appended to the title, like `Batman (2016)`. Clients select the first match by default, so the
   * most likely candidate has to come first.
   *
   * Nothing is ever discarded, and matches are left in the order the repository returned them whenever the
   * request carries no year, or no match can be corroborated by one.
   */
  private fun ReadListRequestBookMatches.sortMatchesByYear(): ReadListRequestBookMatches {
    val seriesYear = request.seriesYear
    val issueYear = request.issueYear
    if (seriesYear == null && issueYear == null) return this

    val matchesWithSortedBooks =
      matches.mapValues { (_, books) ->
        if (books.size < 2) books else books.sortedByDescending { if (it.matchesIssueYear(issueYear)) 1 else 0 }
      }
    if (matchesWithSortedBooks.size < 2) return copy(matches = matchesWithSortedBooks)

    return copy(
      matches =
        matchesWithSortedBooks.entries
          .sortedByDescending { (series, books) -> series.yearScore(seriesYear) + books.issueYearScore(issueYear) }
          .associate { (series, books) -> series to books },
    ).also { sorted -> logger.debug { "Sorted matches for $request: ${sorted.matches.keys.map { it.title }}" } }
  }

  /**
   * Scores how much a matched series corroborates [year]. A volume appended to the title is an explicit
   * signal, and ranks higher than the release date of the first book of the series.
   */
  private fun ReadListRequestBookMatchSeries.yearScore(year: Int?): Int {
    if (year == null) return 0
    return (if (title.trimEnd().endsWith(" ($year)")) 2 else 0) +
      (if (releaseDate?.year == year) 1 else 0)
  }

  /**
   * Scores whether a series holds a matched book released in [year], which corroborates the series itself.
   */
  private fun Collection<ReadListRequestBookMatchBook>.issueYearScore(year: Int?): Int = if (any { it.matchesIssueYear(year) }) 1 else 0

  private fun ReadListRequestBookMatchBook.matchesIssueYear(year: Int?): Boolean = year != null && releaseDate?.year == year
}
