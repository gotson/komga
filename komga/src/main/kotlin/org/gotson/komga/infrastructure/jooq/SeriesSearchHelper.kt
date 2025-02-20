package org.gotson.komga.infrastructure.jooq

import io.github.oshai.kotlinlogging.KotlinLogging
import org.gotson.komga.domain.model.ReadStatus
import org.gotson.komga.domain.model.SearchCondition
import org.gotson.komga.domain.model.SearchContext
import org.gotson.komga.domain.model.SearchOperator
import org.gotson.komga.domain.model.SeriesMetadata
import org.gotson.komga.infrastructure.datasource.SqliteUdfDataSource
import org.gotson.komga.jooq.main.Tables
import org.jooq.Condition
import org.jooq.impl.DSL

private val logger = KotlinLogging.logger {}

/**
 * Helper class to generate a Jooq Condition from a [SearchCondition.Series] and [SearchContext]
 */
class SeriesSearchHelper(
  val context: SearchContext,
) : ContentRestrictionsSearchHelper() {
  fun toCondition(searchCondition: SearchCondition.Series?): Pair<Condition, Set<RequiredJoin>> {
    val base = toCondition()
    val search = toConditionInternal(searchCondition)
    return search.first.and(base.first) to (search.second + base.second)
  }

  fun toCondition(): Pair<Condition, Set<RequiredJoin>> {
    val restrictions = toConditionInternal(context.restrictions)
    val authorizedLibraries = toConditionInternal(context.libraryIds)
    return restrictions.first.and(authorizedLibraries.first) to (restrictions.second + authorizedLibraries.second)
  }

  private fun toConditionInternal(libraryIds: Collection<String>?): Pair<Condition, Set<RequiredJoin>> {
    if (libraryIds == null) return DSL.noCondition() to emptySet()
    if (libraryIds.isEmpty()) return DSL.falseCondition() to emptySet()
    return toConditionInternal(SearchCondition.AnyOfSeries(libraryIds.map { SearchCondition.LibraryId(SearchOperator.Is(it)) }))
  }

  private fun toConditionInternal(searchCondition: SearchCondition.Series?): Pair<Condition, Set<RequiredJoin>> =
    when (searchCondition) {
      is SearchCondition.AllOfSeries ->
        searchCondition.conditions.fold(DSL.noCondition() to emptySet()) { acc: Pair<Condition, Set<RequiredJoin>>, cond: SearchCondition.Series ->
          val seriesCondition = toConditionInternal(cond)
          acc.first.and(seriesCondition.first) to (acc.second + seriesCondition.second)
        }

      is SearchCondition.AnyOfSeries ->
        searchCondition.conditions.fold(DSL.noCondition() to emptySet()) { acc: Pair<Condition, Set<RequiredJoin>>, cond: SearchCondition.Series ->
          val seriesCondition = toConditionInternal(cond)
          acc.first.or(seriesCondition.first) to (acc.second + seriesCondition.second)
        }

      is SearchCondition.LibraryId -> searchCondition.operator.toCondition(Tables.SERIES.LIBRARY_ID) to emptySet()

      is SearchCondition.Deleted ->
        Tables.SERIES.DELETED_DATE.let {
          when (searchCondition.operator) {
            SearchOperator.IsFalse -> it.isNull
            SearchOperator.IsTrue -> it.isNotNull
          }
        } to emptySet()

      is SearchCondition.ReleaseDate ->
        searchCondition.operator.toCondition(Tables.BOOK_METADATA_AGGREGATION.RELEASE_DATE) to setOf(RequiredJoin.BookMetadataAggregation)

      is SearchCondition.ReadStatus ->
        if (context.userId == null) {
          logger.warn { "SearchCondition.ReadStatus without userId in search context" }
          DSL.falseCondition() to emptySet()
        } else {
          Tables.READ_PROGRESS_SERIES.READ_COUNT.let { field ->
            when (searchCondition.operator) {
              is SearchOperator.Is -> {
                when (searchCondition.operator.value) {
                  ReadStatus.UNREAD -> field.isNull
                  ReadStatus.READ -> field.eq(Tables.SERIES.BOOK_COUNT)
                  ReadStatus.IN_PROGRESS -> field.ne(Tables.SERIES.BOOK_COUNT)
                }
              }

              is SearchOperator.IsNot ->
                when (searchCondition.operator.value) {
                  ReadStatus.UNREAD -> field.isNotNull
                  ReadStatus.READ -> field.ne(Tables.SERIES.BOOK_COUNT).or(field.isNull)
                  ReadStatus.IN_PROGRESS -> field.eq(Tables.SERIES.BOOK_COUNT).or(field.isNull)
                }
            }
          } to setOf(RequiredJoin.ReadProgress(context.userId))
        }

      is SearchCondition.SeriesStatus ->
        searchCondition.operator.toCondition(Tables.SERIES_METADATA.STATUS, SeriesMetadata.Status::name) to
          setOf(
            RequiredJoin.SeriesMetadata,
          )

      is SearchCondition.Tag ->
        Tables.SERIES.ID.let { field ->
          val innerEquals = { tag: String ->
            DSL
              .select(Tables.SERIES_METADATA_TAG.SERIES_ID)
              .from(Tables.SERIES_METADATA_TAG)
              .where(
                Tables.SERIES_METADATA_TAG.TAG
                  .collate(SqliteUdfDataSource.COLLATION_UNICODE_3)
                  .equalIgnoreCase(tag),
              ).union(
                DSL
                  .select(Tables.BOOK_METADATA_AGGREGATION_TAG.SERIES_ID)
                  .from(Tables.BOOK_METADATA_AGGREGATION_TAG)
                  .where(
                    Tables.BOOK_METADATA_AGGREGATION_TAG.TAG
                      .collate(SqliteUdfDataSource.COLLATION_UNICODE_3)
                      .equalIgnoreCase(tag),
                  ),
              )
          }
          val innerAny = {
            DSL
              .select(Tables.SERIES_METADATA_TAG.SERIES_ID)
              .from(Tables.SERIES_METADATA_TAG)
              .where(Tables.SERIES_METADATA_TAG.TAG.isNotNull)
              .union(
                DSL
                  .select(Tables.BOOK_METADATA_AGGREGATION_TAG.SERIES_ID)
                  .from(Tables.BOOK_METADATA_AGGREGATION_TAG)
                  .where(Tables.BOOK_METADATA_AGGREGATION_TAG.TAG.isNotNull),
              )
          }

          when (searchCondition.operator) {
            is SearchOperator.Is -> field.`in`(innerEquals(searchCondition.operator.value))
            is SearchOperator.IsNot -> field.notIn(innerEquals(searchCondition.operator.value))
            is SearchOperator.IsNullT<*> -> field.notIn(innerAny())
            is SearchOperator.IsNotNullT<*> -> field.`in`(innerAny())
          }
        } to emptySet()

      is SearchCondition.Author ->
        Tables.SERIES.ID.let { field ->
          val inner = { name: String?, role: String? ->
            DSL
              .select(Tables.BOOK_METADATA_AGGREGATION_AUTHOR.SERIES_ID)
              .from(Tables.BOOK_METADATA_AGGREGATION_AUTHOR)
              .where(DSL.noCondition())
              .apply {
                if (name != null)
                  and(
                    Tables.BOOK_METADATA_AGGREGATION_AUTHOR.NAME
                      .collate(
                        SqliteUdfDataSource.COLLATION_UNICODE_3,
                      ).equalIgnoreCase(name),
                  )
              }.apply {
                if (role != null)
                  and(
                    Tables.BOOK_METADATA_AGGREGATION_AUTHOR.ROLE
                      .collate(
                        SqliteUdfDataSource.COLLATION_UNICODE_3,
                      ).equalIgnoreCase(role),
                  )
              }
          }
          when (searchCondition.operator) {
            is SearchOperator.Is -> {
              if (searchCondition.operator.value.name == null && searchCondition.operator.value.role == null)
                DSL.noCondition()
              else
                field.`in`(inner(searchCondition.operator.value.name, searchCondition.operator.value.role))
            }

            is SearchOperator.IsNot -> {
              if (searchCondition.operator.value.name == null && searchCondition.operator.value.role == null)
                DSL.noCondition()
              else
                field.notIn(inner(searchCondition.operator.value.name, searchCondition.operator.value.role))
            }
          } to emptySet()
        }

      is SearchCondition.OneShot -> searchCondition.operator.toCondition(Tables.SERIES.ONESHOT) to emptySet()

      is SearchCondition.AgeRating -> searchCondition.operator.toCondition(Tables.SERIES_METADATA.AGE_RATING) to setOf(RequiredJoin.SeriesMetadata)

      is SearchCondition.CollectionId ->
        when (searchCondition.operator) {
          // for IS condition we have to do a join, so as to order the series by collection number
          is SearchOperator.Is ->
            csAlias(searchCondition.operator.value)
              .COLLECTION_ID
              .eq(searchCondition.operator.value) to setOf(RequiredJoin.Collection(searchCondition.operator.value))

          is SearchOperator.IsNot -> {
            val inner = { collectionId: String ->
              DSL
                .select(Tables.COLLECTION_SERIES.SERIES_ID)
                .from(Tables.COLLECTION_SERIES)
                .where(Tables.COLLECTION_SERIES.COLLECTION_ID.eq(collectionId))
            }
            Tables.SERIES.ID.notIn(inner(searchCondition.operator.value)) to emptySet()
          }
        }

      is SearchCondition.Complete ->
        Tables.SERIES_METADATA.TOTAL_BOOK_COUNT.let { field ->
          when (searchCondition.operator) {
            SearchOperator.IsTrue -> field.isNotNull.and(field.eq(Tables.SERIES.BOOK_COUNT))
            SearchOperator.IsFalse -> field.isNotNull.and(field.ne(Tables.SERIES.BOOK_COUNT))
          } to setOf(RequiredJoin.SeriesMetadata)
        }

      is SearchCondition.Genre ->
        Tables.SERIES.ID.let { field ->
          val innerEquals = { genre: String ->
            DSL
              .select(Tables.SERIES_METADATA_GENRE.SERIES_ID)
              .from(Tables.SERIES_METADATA_GENRE)
              .where(
                Tables.SERIES_METADATA_GENRE.GENRE
                  .collate(SqliteUdfDataSource.COLLATION_UNICODE_3)
                  .equalIgnoreCase(genre),
              )
          }
          val innerAny = {
            DSL
              .select(Tables.SERIES_METADATA_GENRE.SERIES_ID)
              .from(Tables.SERIES_METADATA_GENRE)
              .where(Tables.SERIES_METADATA_GENRE.GENRE.isNotNull)
          }

          when (searchCondition.operator) {
            is SearchOperator.Is -> field.`in`(innerEquals(searchCondition.operator.value))
            is SearchOperator.IsNot -> field.notIn(innerEquals(searchCondition.operator.value))
            is SearchOperator.IsNullT<*> -> field.notIn(innerAny())
            is SearchOperator.IsNotNullT<*> -> field.`in`(innerAny())
          }
        } to emptySet()

      is SearchCondition.Language -> searchCondition.operator.toCondition(Tables.SERIES_METADATA.LANGUAGE, true) to setOf(RequiredJoin.SeriesMetadata)

      is SearchCondition.Publisher -> searchCondition.operator.toCondition(Tables.SERIES_METADATA.PUBLISHER, true) to setOf(RequiredJoin.SeriesMetadata)

      is SearchCondition.SharingLabel ->
        Tables.SERIES.ID.let { field ->
          val innerEquals = { label: String ->
            DSL
              .select(Tables.SERIES_METADATA_SHARING.SERIES_ID)
              .from(Tables.SERIES_METADATA_SHARING)
              .where(
                Tables.SERIES_METADATA_SHARING.LABEL
                  .collate(SqliteUdfDataSource.COLLATION_UNICODE_3)
                  .equalIgnoreCase(label),
              )
          }
          val innerAny = {
            DSL
              .select(Tables.SERIES_METADATA_SHARING.SERIES_ID)
              .from(Tables.SERIES_METADATA_SHARING)
              .where(Tables.SERIES_METADATA_SHARING.LABEL.isNotNull)
          }

          when (searchCondition.operator) {
            is SearchOperator.Is -> field.`in`(innerEquals(searchCondition.operator.value))
            is SearchOperator.IsNot -> field.notIn(innerEquals(searchCondition.operator.value))
            is SearchOperator.IsNullT<*> -> field.notIn(innerAny())
            is SearchOperator.IsNotNullT<*> -> field.`in`(innerAny())
          }
        } to emptySet()

      is SearchCondition.Title ->
        searchCondition.operator.toCondition(Tables.SERIES_METADATA.TITLE) to
          setOf(
            RequiredJoin.SeriesMetadata,
          )

      is SearchCondition.TitleSort ->
        searchCondition.operator.toCondition(Tables.SERIES_METADATA.TITLE_SORT) to
          setOf(
            RequiredJoin.SeriesMetadata,
          )

      null -> DSL.noCondition() to emptySet()
    }
}
