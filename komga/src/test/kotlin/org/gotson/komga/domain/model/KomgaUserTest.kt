package org.gotson.komga.domain.model

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class KomgaUserTest {
  val defaultUser = KomgaUser("user@example.org", "aPassword")

  @Nested
  inner class ContentRestriction {
    @Test
    fun `given user with age AllowOnlyUnder restriction when checking for content restriction then it is accurate`() {
      val user = defaultUser.copy(restrictions = ContentRestrictions(AgeRestriction(5, AllowExclude.ALLOW_ONLY)))

      assertThat(user.isContentAllowed(ageRating = 3)).isTrue
      assertThat(user.isContentAllowed(ageRating = 5)).isTrue
      assertThat(user.isContentAllowed(ageRating = 8)).isFalse
      assertThat(user.isContentAllowed(ageRating = null)).isFalse
    }

    @Test
    fun `given user with age ExcludeOver restriction when checking for content restriction then it is accurate`() {
      val user = defaultUser.copy(restrictions = ContentRestrictions(AgeRestriction(16, AllowExclude.EXCLUDE)))

      assertThat(user.isContentAllowed(ageRating = 10)).`as`("age 10 is allowed").isTrue
      assertThat(user.isContentAllowed(ageRating = null)).`as`("age null is allowed").isTrue
      assertThat(user.isContentAllowed(ageRating = 16)).`as`("age 16 is not allowed").isFalse
      assertThat(user.isContentAllowed(ageRating = 18)).`as`("age 18 is not allowed").isFalse
    }

    @Test
    fun `given user with sharing label AllowOnly restriction when checking for content restriction then it is accurate`() {
      val user = defaultUser.copy(restrictions = ContentRestrictions(labelsAllow = setOf("allow", "this")))

      assertThat(user.isContentAllowed(sharingLabels = setOf("allow"))).`as`("any tag is fine: allow").isTrue
      assertThat(user.isContentAllowed(sharingLabels = setOf("this"))).`as`("any tag is fine: this").isTrue
      assertThat(user.isContentAllowed(sharingLabels = setOf("allow", "this"))).`as`("both tags are fine").isTrue
      assertThat(user.isContentAllowed(sharingLabels = setOf("other"))).`as`("no allowed tags: other").isFalse
      assertThat(user.isContentAllowed(sharingLabels = emptySet())).`as`("no allowed tags: emptySet").isFalse
    }

    @Test
    fun `given user with sharing label Exclude restriction when checking for content restriction then it is accurate`() {
      val user = defaultUser.copy(restrictions = ContentRestrictions(labelsExclude = setOf("exclude", "this")))

      assertThat(user.isContentAllowed(sharingLabels = emptySet())).`as`("no label so no exclusion").isTrue
      assertThat(user.isContentAllowed(sharingLabels = setOf("allow"))).`as`("label allow is not in exclusion list").isTrue
      assertThat(user.isContentAllowed(sharingLabels = setOf("other", "this"))).`as`("label this is in exclusion list, other label is ignored").isFalse
      assertThat(user.isContentAllowed(sharingLabels = setOf("this"))).`as`("label this is in exclusion list").isFalse
    }

    @Test
    fun `given user with both sharing label AllowOnly and Exclude restriction when checking for content restriction then it is accurate`() {
      val user =
        defaultUser.copy(
          restrictions =
            ContentRestrictions(
              labelsAllow = setOf("allow", "both"),
              labelsExclude = setOf("exclude", "both"),
            ),
        )

      assertThat(user.isContentAllowed(sharingLabels = setOf("allow"))).isTrue
      assertThat(user.isContentAllowed(sharingLabels = setOf("allow", "other"))).isTrue
      assertThat(user.isContentAllowed(sharingLabels = setOf("allow", "both"))).isFalse
      assertThat(user.isContentAllowed(sharingLabels = setOf("exclude"))).isFalse
      assertThat(user.isContentAllowed(sharingLabels = emptySet())).isFalse
    }

    @Test
    fun `given user with both age AllowOnlyUnder restriction and sharing label AllowOnly restriction when checking for content restriction then it is accurate`() {
      val user =
        defaultUser.copy(
          restrictions =
            ContentRestrictions(
              ageRestriction = AgeRestriction(10, AllowExclude.ALLOW_ONLY),
              labelsAllow = setOf("allow"),
            ),
        )

      assertThat(user.isContentAllowed(ageRating = 5)).`as`("age 5 only is sufficient").isTrue
      assertThat(user.isContentAllowed(ageRating = 15)).`as`("age 15 is not allowed").isFalse
      assertThat(user.isContentAllowed(ageRating = null)).`as`("missing age and no allowed label: age null").isFalse
      assertThat(user.isContentAllowed(sharingLabels = setOf("allow"))).`as`("allowed tag is sufficient").isTrue
      assertThat(user.isContentAllowed(sharingLabels = setOf("other"))).`as`("missing age and no allowed label: other").isFalse
      assertThat(user.isContentAllowed(sharingLabels = emptySet())).`as`("missing age and empty set label").isFalse
      assertThat(user.isContentAllowed(5, setOf("allow"))).`as`("age and tag are good").isTrue
      assertThat(user.isContentAllowed(5, setOf("other"))).`as`("age is good, other tag is ignored").isTrue
      assertThat(user.isContentAllowed(15, setOf("allow"))).`as`("age is ignored, tag is allowed").isTrue
      assertThat(user.isContentAllowed(15, setOf("other"))).`as`("age is too high, and no tag is allowed").isFalse
    }

    @Test
    fun `given user with both age AllowOnlyUnder restriction and sharing label Exclude restriction when checking for content restriction then it is accurate`() {
      val user =
        defaultUser.copy(
          restrictions =
            ContentRestrictions(
              ageRestriction = AgeRestriction(10, AllowExclude.ALLOW_ONLY),
              labelsExclude = setOf("exclude"),
            ),
        )

      assertThat(user.isContentAllowed(ageRating = 5)).isTrue
      assertThat(user.isContentAllowed(ageRating = 15)).isFalse
      assertThat(user.isContentAllowed(ageRating = null)).isFalse
      assertThat(user.isContentAllowed(sharingLabels = setOf("exclude"))).isFalse
      assertThat(user.isContentAllowed(sharingLabels = setOf("other"))).isFalse
      assertThat(user.isContentAllowed(sharingLabels = emptySet())).isFalse
      assertThat(user.isContentAllowed(5, setOf("exclude"))).isFalse
      assertThat(user.isContentAllowed(5, setOf("other"))).isTrue
      assertThat(user.isContentAllowed(15, setOf("exclude"))).isFalse
      assertThat(user.isContentAllowed(15, setOf("other"))).isFalse
    }
  }
}
