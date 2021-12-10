package org.gotson.komga.interfaces.api.rest.dto

import org.gotson.komga.domain.model.ReadListRequestBook
import org.gotson.komga.domain.model.ReadListRequestResult
import org.gotson.komga.domain.model.ReadListRequestResultBook

data class ReadListRequestBookDto(
  val series: String,
  val number: String,
)

data class ReadListRequestResultDto(
  val readList: ReadListDto?,
  val unmatchedBooks: List<ReadListRequestResultBookDto> = emptyList(),
  val errorCode: String = "",
  val requestName: String,
)

data class ReadListRequestResultBookDto(
  val book: ReadListRequestBookDto,
  val errorCode: String = "",
)

fun ReadListRequestResult.toDto(requestName: String?) =
  ReadListRequestResultDto(
    readList = readList?.toDto(),
    unmatchedBooks = unmatchedBooks.map { it.toDto() },
    errorCode = errorCode,
    requestName = requestName ?: "",
  )

fun ReadListRequestResultBook.toDto() =
  ReadListRequestResultBookDto(
    book = book.toDto(),
    errorCode = errorCode,
  )

fun ReadListRequestBook.toDto() =
  ReadListRequestBookDto(
    series = series,
    number = number,
  )
