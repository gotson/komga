package org.gotson.komga.domain.model

import java.io.Serializable

data class AgeRestriction(
  val age: Int,
  val restriction: AllowExclude,
) : Serializable

enum class AllowExclude {
  ALLOW_ONLY, EXCLUDE,
}
