package org.gotson.komga.interfaces.api.rest.dto

import org.gotson.komga.domain.model.ReadListMatch
import org.gotson.komga.domain.model.ReadListRequestBook
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
        it.request.toDtoV2(),
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
  val request: ReadListRequestBookV2Dto,
  val matches: List<ReadListRequestBookMatchDto>,
)

data class ReadListRequestBookV2Dto(
  val series: Set<String>,
  val number: String,
)

fun ReadListRequestBook.toDtoV2() =
  ReadListRequestBookV2Dto(
    series = series,
    number = number,
  )

data class ReadListRequestBookMatchDto(
  val seriesId: String,
  val bookIds: List<String>,
)

fun ReadListMatch.toDto() = ReadListMatchDto(name, errorCode)
