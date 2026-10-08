package org.gotson.komga.domain.model

import java.io.Serial
import java.io.Serializable

data class AgeRestriction(
  val age: Int,
  val restriction: AllowExclude,
) : Serializable {
  companion object {
    @Serial
    private const val serialVersionUID: Long = -6060796340851263994L
  }
}

enum class AllowExclude : Serializable {
  ALLOW_ONLY,
  EXCLUDE,
}
