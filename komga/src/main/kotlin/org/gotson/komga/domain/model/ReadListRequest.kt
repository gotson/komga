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
