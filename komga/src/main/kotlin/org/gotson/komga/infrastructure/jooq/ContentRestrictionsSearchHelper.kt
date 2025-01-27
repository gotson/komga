package org.gotson.komga.infrastructure.jooq

import org.gotson.komga.domain.model.AllowExclude
import org.gotson.komga.domain.model.ContentRestrictions
import org.gotson.komga.jooq.main.Tables
import org.jooq.Condition
import org.jooq.impl.DSL

abstract class ContentRestrictionsSearchHelper {
  protected fun toConditionInternal(restrictions: ContentRestrictions): Pair<Condition, Set<RequiredJoin>> {
    val ageAllowed =
      if (restrictions.ageRestriction?.restriction == AllowExclude.ALLOW_ONLY) {
        Tables.SERIES_METADATA.AGE_RATING.isNotNull
          .and(Tables.SERIES_METADATA.AGE_RATING.lessOrEqual(restrictions.ageRestriction.age)) to setOf(RequiredJoin.SeriesMetadata)
      } else {
        DSL.noCondition() to emptySet()
      }

    val labelAllowed =
      if (restrictions.labelsAllow.isNotEmpty())
        Tables.SERIES_METADATA.SERIES_ID.`in`(
          DSL
            .select(Tables.SERIES_METADATA_SHARING.SERIES_ID)
            .from(Tables.SERIES_METADATA_SHARING)
            .where(Tables.SERIES_METADATA_SHARING.LABEL.`in`(restrictions.labelsAllow)),
        ) to setOf(RequiredJoin.SeriesMetadata)
      else
        DSL.noCondition() to emptySet()

    val ageDenied =
      if (restrictions.ageRestriction?.restriction == AllowExclude.EXCLUDE)
        Tables.SERIES_METADATA.AGE_RATING.isNull
          .or(Tables.SERIES_METADATA.AGE_RATING.lessThan(restrictions.ageRestriction.age)) to setOf(RequiredJoin.SeriesMetadata)
      else
        DSL.noCondition() to emptySet()

    val labelDenied =
      if (restrictions.labelsExclude.isNotEmpty())
        Tables.SERIES_METADATA.SERIES_ID.notIn(
          DSL
            .select(Tables.SERIES_METADATA_SHARING.SERIES_ID)
            .from(Tables.SERIES_METADATA_SHARING)
            .where(Tables.SERIES_METADATA_SHARING.LABEL.`in`(restrictions.labelsExclude)),
        ) to setOf(RequiredJoin.SeriesMetadata)
      else
        DSL.noCondition() to emptySet()

    return ageAllowed.first
      .or(labelAllowed.first)
      .and(ageDenied.first.and(labelDenied.first)) to (ageAllowed.second + labelAllowed.second + ageDenied.second + labelDenied.second)
  }
}
