package org.gotson.komga.interfaces.api.rest.dto

import com.fasterxml.jackson.annotation.JsonFormat
import org.gotson.komga.domain.model.ReadListMatch
import org.gotson.komga.domain.model.ReadListRequestBook
import org.gotson.komga.domain.model.ReadListRequestMatch
import java.time.LocalDate

data class ReadListRequestMatchDto(
  val readListMatch: ReadListMatchDto,
  val requests: Collection<ReadListRequestBookMatchesDto>,
  val errorCode: String = "",
)

fun ReadListRequestMatch.toDto() =
  ReadListRequestMatchDto(
    readListMatch.toDto(),
    requests.map { request ->
      ReadListRequestBookMatchesDto(
        request.request.toDto(),
        request.matches.entries.map { (series, books) ->
          ReadListRequestBookMatchDto(ReadListRequestBookMatchSeriesDto(series.id, series.title, series.releaseDate), books.map { ReadListRequestBookMatchBookDto(it.id, it.number, it.title) })
        },
      )
    },
  )

data class ReadListMatchDto(
  val name: String,
  val errorCode: String = "",
)

fun ReadListMatch.toDto() = ReadListMatchDto(name, errorCode)

data class ReadListRequestBookMatchesDto(
  val request: ReadListRequestBookDto,
  val matches: List<ReadListRequestBookMatchDto>,
)

data class ReadListRequestBookDto(
  val series: Set<String>,
  val number: String,
)

fun ReadListRequestBook.toDto() =
  ReadListRequestBookDto(
    series = series,
    number = number,
  )

data class ReadListRequestBookMatchDto(
  val series: ReadListRequestBookMatchSeriesDto,
  val books: Collection<ReadListRequestBookMatchBookDto>,
)

data class ReadListRequestBookMatchSeriesDto(
  val seriesId: String,
  val title: String,
  @JsonFormat(pattern = "yyyy-MM-dd")
  val releaseDate: LocalDate?,
)

data class ReadListRequestBookMatchBookDto(
  val bookId: String,
  val number: String,
  val title: String,
)
