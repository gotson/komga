package org.gotson.komga.domain.model

import org.gotson.komga.language.lowerNotBlank
import org.gotson.komga.language.toSetOrNull

class ContentRestrictions(
  val ageRestriction: ContentRestriction.AgeRestriction? = null,
  labelsAllow: Set<String> = emptySet(),
  labelsExclude: Set<String> = emptySet(),
) {
  val labelsAllowRestriction =
    labelsAllow.lowerNotBlank().toSet()
      .minus(labelsExclude.lowerNotBlank().toSet())
      .toSetOrNull()?.let { ContentRestriction.LabelsRestriction.AllowOnly(it) }

  val labelsExcludeRestriction =
    labelsExclude.lowerNotBlank().toSetOrNull()?.let { ContentRestriction.LabelsRestriction.Exclude(it) }

  fun isRestricted() = ageRestriction != null || labelsAllowRestriction != null || labelsExcludeRestriction != null

  override fun toString(): String = "ContentRestriction(ageRestriction=$ageRestriction, labelsAllowRestriction=$labelsAllowRestriction, labelsExcludeRestriction=$labelsExcludeRestriction)"
}
