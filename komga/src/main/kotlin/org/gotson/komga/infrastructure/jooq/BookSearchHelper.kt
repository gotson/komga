package org.gotson.komga.infrastructure.jooq

import io.github.oshai.kotlinlogging.KotlinLogging
import org.gotson.komga.domain.model.Media
import org.gotson.komga.domain.model.MediaType
import org.gotson.komga.domain.model.ReadStatus
import org.gotson.komga.domain.model.SearchCondition
import org.gotson.komga.domain.model.SearchContext
import org.gotson.komga.domain.model.SearchOperator
import org.gotson.komga.infrastructure.datasource.SqliteUdfDataSource
import org.gotson.komga.infrastructure.jooq.RequiredJoin.ReadProgress
import org.gotson.komga.jooq.main.Tables
import org.jooq.Condition
import org.jooq.impl.DSL

private val logger = KotlinLogging.logger {}

/**
 * Helper class to generate a Jooq Condition from a [SearchCondition.Book] and [SearchContext]
 */
class BookSearchHelper(
  val context: SearchContext,
) : ContentRestrictionsSearchHelper() {
  fun toCondition(searchCondition: SearchCondition.Book?): Pair<Condition, Set<RequiredJoin>> {
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
    return toConditionInternal(SearchCondition.AnyOfBook(libraryIds.map { SearchCondition.LibraryId(SearchOperator.Is(it)) }))
  }

  private fun toConditionInternal(searchCondition: SearchCondition.Book?): Pair<Condition, Set<RequiredJoin>> =
    when (searchCondition) {
      is SearchCondition.AllOfBook ->
        searchCondition.conditions.fold(DSL.noCondition() to emptySet()) { acc: Pair<Condition, Set<RequiredJoin>>, cond: SearchCondition.Book ->
          val bookCondition = toConditionInternal(cond)
          acc.first.and(bookCondition.first) to (acc.second + bookCondition.second)
        }

      is SearchCondition.AnyOfBook ->
        searchCondition.conditions.fold(DSL.noCondition() to emptySet()) { acc: Pair<Condition, Set<RequiredJoin>>, cond: SearchCondition.Book ->
          val bookCondition = toConditionInternal(cond)
          acc.first.or(bookCondition.first) to (acc.second + bookCondition.second)
        }

      is SearchCondition.LibraryId -> searchCondition.operator.toCondition(Tables.BOOK.LIBRARY_ID) to emptySet()

      is SearchCondition.SeriesId -> searchCondition.operator.toCondition(Tables.BOOK.SERIES_ID) to emptySet()

      is SearchCondition.ReadListId ->
        when (searchCondition.operator) {
          // for IS condition we have to do a join, so as to order the books by readList number
          is SearchOperator.Is ->
            rlbAlias(searchCondition.operator.value)
              .READLIST_ID
              .eq(searchCondition.operator.value) to setOf(RequiredJoin.ReadList(searchCondition.operator.value))

          is SearchOperator.IsNot -> {
            val inner = { readListId: String ->
              DSL
                .select(Tables.READLIST_BOOK.BOOK_ID)
                .from(Tables.READLIST_BOOK)
                .where(Tables.READLIST_BOOK.READLIST_ID.eq(readListId))
            }
            Tables.BOOK.ID.notIn(inner(searchCondition.operator.value)) to emptySet()
          }
        }

      is SearchCondition.Title ->
        searchCondition.operator.toCondition(Tables.BOOK_METADATA.TITLE) to
          setOf(
            RequiredJoin.BookMetadata,
          )

      is SearchCondition.Deleted ->
        Tables.BOOK.DELETED_DATE.let {
          when (searchCondition.operator) {
            SearchOperator.IsFalse -> it.isNull
            SearchOperator.IsTrue -> it.isNotNull
          }
        } to emptySet()

      is SearchCondition.ReleaseDate ->
        searchCondition.operator.toCondition(Tables.BOOK_METADATA.RELEASE_DATE) to
          setOf(
            RequiredJoin.BookMetadata,
          )

      is SearchCondition.NumberSort ->
        searchCondition.operator.toCondition(Tables.BOOK_METADATA.NUMBER_SORT) to
          setOf(
            RequiredJoin.BookMetadata,
          )

      is SearchCondition.ReadStatus ->
        if (context.userId == null) {
          logger.warn { "SearchCondition.ReadStatus without userId in search context" }
          DSL.falseCondition() to emptySet()
        } else {
          Tables.READ_PROGRESS.COMPLETED.let {
            when (searchCondition.operator) {
              is SearchOperator.Is -> {
                when (searchCondition.operator.value) {
                  ReadStatus.UNREAD -> it.isNull
                  ReadStatus.READ -> it.isTrue
                  ReadStatus.IN_PROGRESS -> it.isFalse
                }
              }

              is SearchOperator.IsNot ->
                when (searchCondition.operator.value) {
                  ReadStatus.UNREAD -> it.isNotNull
                  ReadStatus.READ -> it.isNull.or(it.isFalse)
                  ReadStatus.IN_PROGRESS -> it.isTrue.or(it.isNull)
                }
            }
          } to setOf(ReadProgress(context.userId))
        }

      is SearchCondition.MediaStatus ->
        searchCondition.operator.toCondition(Tables.MEDIA.STATUS, Media.Status::name) to
          setOf(
            RequiredJoin.Media,
          )

      is SearchCondition.MediaProfile ->
        Tables.MEDIA.MEDIA_TYPE.let { field ->
          when (searchCondition.operator) {
            is SearchOperator.Is -> field.`in`(MediaType.matchingMediaProfile(searchCondition.operator.value).map { it.type }.toSet())
            is SearchOperator.IsNot -> field.notIn(MediaType.matchingMediaProfile(searchCondition.operator.value).map { it.type }.toSet())
          }
        } to setOf(RequiredJoin.Media)

      is SearchCondition.Tag ->
        Tables.BOOK.ID.let { field ->
          val inner = { tag: String ->
            DSL
              .select(Tables.BOOK_METADATA_TAG.BOOK_ID)
              .from(Tables.BOOK_METADATA_TAG)
              .where(
                Tables.BOOK_METADATA_TAG.TAG
                  .collate(SqliteUdfDataSource.COLLATION_UNICODE_3)
                  .equalIgnoreCase(tag),
              )
          }
          when (searchCondition.operator) {
            is SearchOperator.Is -> field.`in`(inner(searchCondition.operator.value))
            is SearchOperator.IsNot -> field.notIn(inner(searchCondition.operator.value))
          }
        } to emptySet()

      is SearchCondition.Author ->
        Tables.BOOK.ID.let { field ->
          val inner = { name: String?, role: String? ->
            DSL
              .select(Tables.BOOK_METADATA_AUTHOR.BOOK_ID)
              .from(Tables.BOOK_METADATA_AUTHOR)
              .where(DSL.noCondition())
              .apply {
                if (name != null)
                  and(
                    Tables.BOOK_METADATA_AUTHOR.NAME
                      .collate(SqliteUdfDataSource.COLLATION_UNICODE_3)
                      .equalIgnoreCase(name),
                  )
              }.apply {
                if (role != null)
                  and(
                    Tables.BOOK_METADATA_AUTHOR.ROLE
                      .collate(SqliteUdfDataSource.COLLATION_UNICODE_3)
                      .equalIgnoreCase(role),
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

      is SearchCondition.Poster ->
        Tables.BOOK.ID.let { field ->
          val inner = { type: SearchCondition.PosterMatch.Type?, selected: Boolean? ->
            DSL
              .select(Tables.THUMBNAIL_BOOK.BOOK_ID)
              .from(Tables.THUMBNAIL_BOOK)
              .where(DSL.noCondition())
              .apply {
                if (type != null)
                  and(Tables.THUMBNAIL_BOOK.TYPE.equalIgnoreCase(type.name))
                if (selected != null && selected)
                  and(Tables.THUMBNAIL_BOOK.SELECTED.isTrue)
                if (selected != null && !selected)
                  and(Tables.THUMBNAIL_BOOK.SELECTED.isFalse)
              }
          }
          when (searchCondition.operator) {
            is SearchOperator.Is -> {
              if (searchCondition.operator.value.type == null && searchCondition.operator.value.selected == null)
                DSL.noCondition()
              else
                field.`in`(inner(searchCondition.operator.value.type, searchCondition.operator.value.selected))
            }

            is SearchOperator.IsNot ->
              if (searchCondition.operator.value.type == null && searchCondition.operator.value.selected == null)
                DSL.noCondition()
              else
                field.notIn(inner(searchCondition.operator.value.type, searchCondition.operator.value.selected))
          } to emptySet()
        }

      is SearchCondition.OneShot -> searchCondition.operator.toCondition(Tables.BOOK.ONESHOT) to emptySet()

      null -> DSL.noCondition() to emptySet()
    }
}
