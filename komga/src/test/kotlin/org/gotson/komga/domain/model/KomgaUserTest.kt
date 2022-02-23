package org.gotson.komga.domain.model

import org.assertj.core.api.Assertions.assertThat
import org.gotson.komga.domain.model.ContentRestriction.AgeRestriction
import org.gotson.komga.domain.model.ContentRestriction.LabelsRestriction
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class KomgaUserTest {

  val defaultUser = KomgaUser("user@example.org", "aPassword", false)

  @Nested
  inner class ContentRestriction {

    @Test
    fun `given user with age AllowOnlyUnder restriction when checking for content restriction then it is accurate`() {
      val user = defaultUser.copy(restrictions = setOf(AgeRestriction.AllowOnlyUnder(5)))

      assertThat(user.isContentRestricted(ageRating = 3)).isFalse
      assertThat(user.isContentRestricted(ageRating = 5)).isFalse
      assertThat(user.isContentRestricted(ageRating = 8)).isTrue
      assertThat(user.isContentRestricted(ageRating = null)).isTrue
    }

    @Test
    fun `given user with age ExcludeOver restriction when checking for content restriction then it is accurate`() {
      val user = defaultUser.copy(restrictions = setOf(AgeRestriction.ExcludeOver(16)))

      assertThat(user.isContentRestricted(ageRating = 10)).isFalse
      assertThat(user.isContentRestricted(ageRating = null)).isFalse
      assertThat(user.isContentRestricted(ageRating = 16)).isTrue
      assertThat(user.isContentRestricted(ageRating = 18)).isTrue
    }

    @Test
    fun `given user with sharing label AllowOnly restriction when checking for content restriction then it is accurate`() {
      val user = defaultUser.copy(restrictions = setOf(LabelsRestriction.AllowOnly(setOf("allow", "this"))))

      assertThat(user.isContentRestricted(sharingLabels = setOf("allow"))).isFalse
      assertThat(user.isContentRestricted(sharingLabels = setOf("this"))).isFalse
      assertThat(user.isContentRestricted(sharingLabels = setOf("allow", "this"))).isFalse
      assertThat(user.isContentRestricted(sharingLabels = setOf("other"))).isTrue
      assertThat(user.isContentRestricted(sharingLabels = emptySet())).isTrue
    }

    @Test
    fun `given user with sharing label Exclude restriction when checking for content restriction then it is accurate`() {
      val user = defaultUser.copy(restrictions = setOf(LabelsRestriction.Exclude(setOf("exclude", "this"))))

      assertThat(user.isContentRestricted(sharingLabels = emptySet())).isFalse
      assertThat(user.isContentRestricted(sharingLabels = setOf("allow"))).isFalse
      assertThat(user.isContentRestricted(sharingLabels = setOf("other", "this"))).isTrue
      assertThat(user.isContentRestricted(sharingLabels = setOf("this"))).isTrue
    }

    @Test
    fun `given user with both sharing label AllowOnly and Exclude restriction when checking for content restriction then it is accurate`() {
      val user = defaultUser.copy(
        restrictions = setOf(
          LabelsRestriction.AllowOnly(setOf("allow", "both")),
          LabelsRestriction.Exclude(setOf("exclude", "both")),
        )
      )

      assertThat(user.isContentRestricted(sharingLabels = setOf("allow"))).isFalse
      assertThat(user.isContentRestricted(sharingLabels = setOf("allow", "other"))).isFalse
      assertThat(user.isContentRestricted(sharingLabels = setOf("allow", "both"))).isTrue
      assertThat(user.isContentRestricted(sharingLabels = setOf("exclude"))).isTrue
      assertThat(user.isContentRestricted(sharingLabels = emptySet())).isTrue
    }
  }
}
