package org.gotson.komga.domain.model

/**
 * Represents a request to create a reading list.
 */
data class ReadListRequest(
  val name: String,
  val books: List<ReadListRequestBook>,
)

data class ReadListRequestBook(
  val series: String,
  val number: String,
)

data class ReadListRequestResult(
  val readList: ReadList?,
  val unmatchedBooks: List<ReadListRequestResultBook> = emptyList(),
  val errorCode: String = "",
)

data class ReadListRequestResultBook(
  val book: ReadListRequestBook,
  val errorCode: String = "",
)

data class ReadListRequestMatch(
  val readListMatch: ReadListMatch,
  val matches: List<ReadListRequestBookMatches>,
  val errorCode: String = "",
)

data class ReadListMatch(
  val name: String,
  val errorCode: String = "",
)

data class ReadListRequestBookMatches(
  val request: ReadListRequestBook,
  val matches: Map<Series, Collection<Book>>,
)
