package org.gotson.komga.infrastructure.jooq.main

import org.gotson.komga.domain.model.ContentRestrictions
import org.gotson.komga.infrastructure.jooq.toCondition
import org.gotson.komga.jooq.main.Tables
import org.jooq.DSLContext
import org.jooq.Field
import org.jooq.Record
import org.jooq.Record1
import org.jooq.SelectConditionStep
import org.jooq.SelectJoinStep
import org.jooq.impl.DSL
import org.jooq.impl.DSL.falseCondition
import org.jooq.impl.DSL.name
import org.jooq.impl.DSL.select
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Component
import java.time.LocalDateTime

@Component
class BookCommonDao(
  @Qualifier("dslContextRO") private val dslRO: DSLContext,
) {
  private val b = Tables.BOOK
  private val m = Tables.MEDIA
  private val d = Tables.BOOK_METADATA
  private val r = Tables.READ_PROGRESS
  private val rs = Tables.READ_PROGRESS_SERIES
  private val s = Tables.SERIES
  private val sd = Tables.SERIES_METADATA

  fun getBooksOnDeckQuery(
    userId: String,
    restrictions: ContentRestrictions,
    filterOnLibraryIds: Collection<String>?,
    selectFields: Array<Field<*>>,
  ): Triple<SelectConditionStep<Record>, Field<LocalDateTime>, SelectJoinStep<Record1<LocalDateTime>>> {
    // On Deck books are the first unread book in a series that has at least one book read, but is not in progress
    // cteSeries will return On Deck series
    val cteSeries =
      name("cte_series")
        .asMaterialized(
          select(s.ID, rs.MOST_RECENT_READ_DATE)
            .from(s)
            .innerJoin(rs)
            .on(s.ID.eq(rs.SERIES_ID).and(rs.USER_ID.eq(userId)))
            .innerJoin(sd)
            .on(s.ID.eq(sd.SERIES_ID))
            .where(rs.IN_PROGRESS_COUNT.eq(0))
            .and(rs.READ_COUNT.ne(s.BOOK_COUNT))
            .and(restrictions.toCondition())
            .apply { filterOnLibraryIds?.let { and(s.LIBRARY_ID.`in`(it)) } },
        )

    val cteBooksFieldBookId = b.ID.`as`("cte_books_book_id")
    val cteBooksFieldSeriesId = b.SERIES_ID.`as`("cte_books_series_id")
    val cteBooksFieldNumberSort = d.NUMBER_SORT.`as`("cte_books_number_sort")
    val cteBooks =
      name("cte_books")
        .asMaterialized(
          select(
            cteBooksFieldBookId,
            cteBooksFieldSeriesId,
            cteBooksFieldNumberSort,
          ).from(b)
            .innerJoin(d)
            .on(b.ID.eq(d.BOOK_ID))
            .leftJoin(r)
            .on(b.ID.eq(r.BOOK_ID))
            .and(r.USER_ID.eq(userId))
            .where(r.COMPLETED.isNull)
            .and(
              b.SERIES_ID.`in`(select(cteSeries.field(s.ID)).from(cteSeries)),
            ),
        )

    // finding the first unread book by number_sort is similar to finding the greatest-n-per-group
    val b1 = cteBooks.`as`("b1")
    val b2 = cteBooks.`as`("b2")
    val query =
      dslRO
        .with(cteSeries)
        .with(cteBooks)
        .select(*selectFields)
        .from(cteSeries)
        .innerJoin(b1)
        .on(cteSeries.field(s.ID)!!.eq(b1.field(cteBooksFieldSeriesId)))
        // we join the cteBooks table on itself, using the grouping ID (seriesId) using a left outer join
        // it returns the row b1 for which no other row b2 exists with the same seriesId and a smaller numberSort
        // when b2 is null, it means the left outer join fond no such match, and therefore b1 has the smaller value of numberSort
        .leftOuterJoin(b2)
        .on(
          b1
            .field(cteBooksFieldSeriesId)!!
            .eq(b2.field(cteBooksFieldSeriesId))
            .and(
              b1
                .field(cteBooksFieldNumberSort)!!
                .gt(b2.field(cteBooksFieldNumberSort))
                .or(
                  b1
                    .field(cteBooksFieldNumberSort)!!
                    .eq(b2.field(cteBooksFieldNumberSort))
                    .and(b1.field(cteBooksFieldBookId)!!.gt(b2.field(cteBooksFieldBookId))),
                ),
            ),
        ).innerJoin(b)
        .on(b1.field(cteBooksFieldBookId)!!.eq(b.ID))
        .innerJoin(m)
        .on(b.ID.eq(m.BOOK_ID))
        .innerJoin(d)
        .on(b.ID.eq(d.BOOK_ID))
        .innerJoin(sd)
        .on(b.SERIES_ID.eq(sd.SERIES_ID))
        // fetchAndMap expects some values for ReadProgress
        // On Deck books are by definition unread, thus don't have read progress
        // we join on the table to keep fetchAndMap, with a false condition to only get null values
        .leftOuterJoin(r)
        .on(falseCondition())
        .where(b2.field(cteBooksFieldBookId)!!.isNull)

    val mostRecentReadDateQuery =
      dslRO
        .with(cteSeries)
        .select(DSL.max(cteSeries.field(rs.MOST_RECENT_READ_DATE)))
        .from(cteSeries)

    return Triple(query, cteSeries.field(rs.MOST_RECENT_READ_DATE)!!, mostRecentReadDateQuery)
  }
}
