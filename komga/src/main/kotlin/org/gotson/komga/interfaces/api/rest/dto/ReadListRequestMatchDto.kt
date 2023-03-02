package org.gotson.komga.interfaces.api.rest.dto

import org.gotson.komga.domain.model.ReadListMatch
import org.gotson.komga.domain.model.ReadListRequestMatch

data class ReadListRequestMatchDto(
  val readListMatch: ReadListMatchDto,
  val matches: List<ReadListRequestBookMatchesDto>,
  val errorCode: String = "",
)

fun ReadListRequestMatch.toDto() =
  ReadListRequestMatchDto(
    readListMatch.toDto(),
    matches.map {
      ReadListRequestBookMatchesDto(
        it.request.toDto(),
        it.matches.entries.map { (series, books) ->
          ReadListRequestBookMatchDto(
            series.id,
            books.map { book -> book.id },
          )
        },
      )
    },
  )

data class ReadListMatchDto(
  val name: String,
  val errorCode: String = "",
)

data class ReadListRequestBookMatchesDto(
  val request: ReadListRequestBookDto,
  val matches: List<ReadListRequestBookMatchDto>,
)

data class ReadListRequestBookMatchDto(
  val seriesId: String,
  val bookIds: List<String>,
)

fun ReadListMatch.toDto() = ReadListMatchDto(name, errorCode)
