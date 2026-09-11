package org.gotson.komga.domain.model

import java.time.LocalDate

/**
 * Represents a request to create a reading list.
 */
data class ReadListRequest(
  val name: String,
  val books: List<ReadListRequestBook>,
)

data class ReadListRequestBook(
  val series: Set<String>,
  val number: String,
  /**
   * The year the series started, if known. Used to discriminate between series sharing the same title.
   */
  val seriesYear: Int? = null,
  /**
   * The year the requested issue was released, if known. Used to discriminate between books sharing the same number.
   */
  val issueYear: Int? = null,
)

data class ReadListRequestMatch(
  val readListMatch: ReadListMatch,
  val requests: Collection<ReadListRequestBookMatches>,
  val errorCode: String = "",
)

data class ReadListMatch(
  val name: String,
  val errorCode: String = "",
)

data class ReadListRequestBookMatches(
  val request: ReadListRequestBook,
  val matches: Map<ReadListRequestBookMatchSeries, Collection<ReadListRequestBookMatchBook>>,
)

data class ReadListRequestBookMatchSeries(
  val id: String,
  val title: String,
  val releaseDate: LocalDate?,
)

data class ReadListRequestBookMatchBook(
  val id: String,
  val number: String,
  val title: String,
  val releaseDate: LocalDate? = null,
)
