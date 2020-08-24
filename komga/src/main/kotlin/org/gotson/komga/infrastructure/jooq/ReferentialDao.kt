package org.gotson.komga.infrastructure.jooq

import org.gotson.komga.domain.persistence.ReferentialRepository
import org.gotson.komga.jooq.Tables
import org.jooq.DSLContext
import org.jooq.impl.DSL.lower
import org.jooq.impl.DSL.select
import org.springframework.stereotype.Component

@Component
class ReferentialDao(
  private val dsl: DSLContext
) : ReferentialRepository {

  private val a = Tables.BOOK_METADATA_AUTHOR
  private val sd = Tables.SERIES_METADATA
  private val g = Tables.SERIES_METADATA_GENRE
  private val bt = Tables.BOOK_METADATA_TAG
  private val st = Tables.SERIES_METADATA_TAG

  override fun findAuthorsByName(search: String): List<String> =
    dsl.selectDistinct(a.NAME)
    .from(a)
    .where(a.NAME.containsIgnoreCase(search))
    .orderBy(a.NAME)
    .fetch(a.NAME)

  override fun findAllGenres(): Set<String> =
    dsl.selectDistinct(g.GENRE)
      .from(g)
      .orderBy(lower(g.GENRE))
      .fetchSet(g.GENRE)

  override fun findAllTags(): Set<String> =
    dsl.select(bt.TAG.`as`("tag"))
      .from(bt)
      .union(
        select(st.TAG.`as`("tag")).from(st)
      )
      .fetchSet(0, String::class.java)
      .sortedBy { it.toLowerCase() }
      .toSet()

  override fun findAllLanguages(): Set<String> =
    dsl.selectDistinct(sd.LANGUAGE)
      .from(sd)
      .orderBy(sd.LANGUAGE)
      .fetchSet(sd.LANGUAGE)
}
