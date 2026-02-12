package org.gotson.komga.infrastructure.unicode

import com.ibm.icu.text.Collator

object Collators {
  /**
   * Used for matching
   */
  val collator1: Collator =
    Collator.getInstance().apply {
      strength = Collator.PRIMARY
      decomposition = Collator.CANONICAL_DECOMPOSITION
    }

  /**
   * Used for sorting
   */
  val collator3: Collator =
    Collator.getInstance().apply {
      strength = Collator.TERTIARY
      decomposition = Collator.CANONICAL_DECOMPOSITION
    }
}
