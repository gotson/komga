package org.gotson.komga.infrastructure.jooq.main

import org.gotson.komga.domain.model.ReadListRequestBook
import org.gotson.komga.domain.model.ReadListRequestBookMatchBook
import org.gotson.komga.domain.model.ReadListRequestBookMatchSeries
import org.gotson.komga.domain.model.ReadListRequestBookMatches
import org.gotson.komga.domain.persistence.ReadListRequestRepository
import org.gotson.komga.infrastructure.jooq.noCase
import org.gotson.komga.jooq.main.Tables
import org.jooq.DSLContext
import org.jooq.impl.DSL.ltrim
import org.jooq.impl.DSL.row
import org.jooq.impl.DSL.value
import org.jooq.impl.DSL.values
import org.springframework.stereotype.Component
import java.time.LocalDate

@Component
class ReadListRequestDao(
  private val dsl: DSLContext,
) : ReadListRequestRepository {
  private val sd = Tables.SERIES_METADATA
  private val b = Tables.BOOK
  private val bd = Tables.BOOK_METADATA
  private val bma = Tables.BOOK_METADATA_AGGREGATION

  override fun matchBookRequests(requests: Collection<ReadListRequestBook>): Collection<ReadListRequestBookMatches> {
    // use a table expression to join the requests to their potential matches
    val requestsAsRows = requests.flatMapIndexed { i, r -> r.series.map { row(i, it, r.number) } }
    val seriesField = "series"
    val indexField = "index"
    val numberField = "number"
    val requestsTable = values(*requestsAsRows.toTypedArray()).`as`("request", indexField, seriesField, numberField)
    val matchedRequests =
      dsl
        .select(
          requestsTable.field(indexField, Int::class.java),
          sd.SERIES_ID,
          sd.TITLE,
          bd.BOOK_ID,
          bd.NUMBER,
          bd.TITLE,
          bma.RELEASE_DATE,
        ).from(requestsTable)
        .innerJoin(sd)
        .on(requestsTable.field(seriesField, String::class.java)?.eq(sd.TITLE.noCase()))
        .leftJoin(bma)
        .on(sd.SERIES_ID.eq(bma.SERIES_ID))
        .innerJoin(b)
        .on(sd.SERIES_ID.eq(b.SERIES_ID))
        .innerJoin(bd)
        .on(
          b.ID
            .eq(bd.BOOK_ID)
            .and(ltrim(bd.NUMBER, value("0")).eq(ltrim(requestsTable.field(numberField, String::class.java), value("0")).noCase())),
        ).fetchGroups(requestsTable.field(indexField, Int::class.java))
        .mapValues { (_, records) ->
          // use the requests index to match results
          records.groupBy(
            { ReadListRequestBookMatchSeries(it.get(1, String::class.java), it.get(2, String::class.java), it.get(6, LocalDate::class.java)) },
            { ReadListRequestBookMatchBook(it.get(3, String::class.java), it.get(4, String::class.java), it.get(5, String::class.java)) },
          )
        }

    return requests.mapIndexed { i, request ->
      ReadListRequestBookMatches(
        request,
        matchedRequests.getOrDefault(i, emptyMap()),
      )
    }
  }
}
