package org.gotson.komga.domain.model

import org.gotson.komga.language.lowerNotBlank
import org.gotson.komga.language.toSetOrNull
import java.io.Serializable

class ContentRestrictions(
  val ageRestriction: ContentRestriction.AgeRestriction? = null,
  labelsAllow: Set<String> = emptySet(),
  labelsExclude: Set<String> = emptySet(),
) : Serializable {
  val labelsAllowRestriction =
    labelsAllow.lowerNotBlank().toSet()
      .minus(labelsExclude.lowerNotBlank().toSet())
      .toSetOrNull()?.let { ContentRestriction.LabelsRestriction.AllowOnly(it) }

  val labelsExcludeRestriction =
    labelsExclude.lowerNotBlank().toSetOrNull()?.let { ContentRestriction.LabelsRestriction.Exclude(it) }

  fun isRestricted() = ageRestriction != null || labelsAllowRestriction != null || labelsExcludeRestriction != null

  override fun toString(): String = "ContentRestriction(ageRestriction=$ageRestriction, labelsAllowRestriction=$labelsAllowRestriction, labelsExcludeRestriction=$labelsExcludeRestriction)"

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other !is ContentRestrictions) return false

    if (ageRestriction != other.ageRestriction) return false
    if (labelsAllowRestriction != other.labelsAllowRestriction) return false
    if (labelsExcludeRestriction != other.labelsExcludeRestriction) return false

    return true
  }

  override fun hashCode(): Int {
    var result = ageRestriction?.hashCode() ?: 0
    result = 31 * result + (labelsAllowRestriction?.hashCode() ?: 0)
    result = 31 * result + (labelsExcludeRestriction?.hashCode() ?: 0)
    return result
  }
}
