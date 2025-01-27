package org.gotson.komga.domain.model

import org.gotson.komga.language.lowerNotBlank

class ContentRestrictions(
  val ageRestriction: AgeRestriction? = null,
  labelsAllow: Set<String> = emptySet(),
  labelsExclude: Set<String> = emptySet(),
) {
  val labelsAllow =
    labelsAllow
      .lowerNotBlank()
      .toSet()
      .minus(labelsExclude.lowerNotBlank().toSet())

  val labelsExclude = labelsExclude.lowerNotBlank().toSet()

  @delegate:Transient
  val isRestricted: Boolean by lazy { ageRestriction != null || labelsAllow.isNotEmpty() || labelsExclude.isNotEmpty() }

  override fun toString(): String = "ContentRestrictions(ageRestriction=$ageRestriction, labelsAllow=$labelsAllow, labelsExclude=$labelsExclude)"

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other !is ContentRestrictions) return false

    if (ageRestriction != other.ageRestriction) return false
    if (labelsAllow != other.labelsAllow) return false
    if (labelsExclude != other.labelsExclude) return false

    return true
  }

  override fun hashCode(): Int {
    var result = ageRestriction?.hashCode() ?: 0
    result = 31 * result + labelsAllow.hashCode()
    result = 31 * result + labelsExclude.hashCode()
    return result
  }
}
