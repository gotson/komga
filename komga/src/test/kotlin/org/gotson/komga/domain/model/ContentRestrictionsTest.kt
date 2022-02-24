package org.gotson.komga.domain.model

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class ContentRestrictionsTest {

  @Test
  fun `given no arguments when creating restriction then all restrictions are null`() {
    val restriction = ContentRestrictions()

    assertThat(restriction.ageRestriction).isNull()
    assertThat(restriction.labelsAllowRestriction).isNull()
    assertThat(restriction.labelsExcludeRestriction).isNull()
  }

  @Test
  fun `given AllowOnlyUnder restriction only when creating restriction then label restrictions are null`() {
    val restriction = ContentRestrictions(ContentRestriction.AgeRestriction.AllowOnlyUnder(10))

    assertThat(restriction.ageRestriction)
      .isNotNull
      .isInstanceOf(ContentRestriction.AgeRestriction.AllowOnlyUnder::class.java)
    assertThat(restriction.ageRestriction!!.age).isEqualTo(10)

    assertThat(restriction.labelsAllowRestriction).isNull()
    assertThat(restriction.labelsExcludeRestriction).isNull()
  }

  @Test
  fun `given ExcludeOver only when creating restriction then label restrictions are null`() {
    val restriction = ContentRestrictions(ContentRestriction.AgeRestriction.ExcludeOver(10))

    assertThat(restriction.ageRestriction)
      .isNotNull
      .isInstanceOf(ContentRestriction.AgeRestriction.ExcludeOver::class.java)
    assertThat(restriction.ageRestriction!!.age).isEqualTo(10)

    assertThat(restriction.labelsAllowRestriction).isNull()
    assertThat(restriction.labelsExcludeRestriction).isNull()
  }

  @Test
  fun `given empty labels when creating restriction then label restrictions are normalized`() {
    val restriction = ContentRestrictions(
      labelsAllow = setOf("", " "),
      labelsExclude = setOf("", " "),
    )

    assertThat(restriction.labelsAllowRestriction).isNull()
    assertThat(restriction.labelsExcludeRestriction).isNull()
  }

  @Test
  fun `given labels with duplicate values when creating restriction then label restrictions are normalized`() {
    val restriction = ContentRestrictions(
      labelsAllow = setOf("a", "b", "B", "b ", "b", " B "),
      labelsExclude = setOf("c", "d", "D", "d ", "d", " D ")
    )

    assertThat(restriction.labelsAllowRestriction).isNotNull
    assertThat(restriction.labelsAllowRestriction!!.labels).containsExactlyInAnyOrder("a", "b")
    assertThat(restriction.labelsExcludeRestriction).isNotNull
    assertThat(restriction.labelsExcludeRestriction!!.labels).containsExactlyInAnyOrder("c", "d")
  }

  @Test
  fun `given labels with same value in both allow and exclude when creating restriction then exclude labels are removed from allow labels`() {
    val restriction = ContentRestrictions(
      labelsAllow = setOf("a", "b", "B", "b ", "b", " B "),
      labelsExclude = setOf(" A ", "d", "D", "d ", "d", " D ")
    )

    assertThat(restriction.labelsAllowRestriction).isNotNull
    assertThat(restriction.labelsAllowRestriction!!.labels).containsExactlyInAnyOrder("b")
    assertThat(restriction.labelsExcludeRestriction).isNotNull
    assertThat(restriction.labelsExcludeRestriction!!.labels).containsExactlyInAnyOrder("a", "d")
  }

  @Test
  fun `given allow labels with all values in exclude labels when creating restriction then allow labels is null`() {
    val restriction = ContentRestrictions(
      labelsAllow = setOf("a", "b", "B", "b ", "b", " B "),
      labelsExclude = setOf(" A ", "b", "B", "B ", "b", " B ")
    )

    assertThat(restriction.labelsAllowRestriction).isNull()
    assertThat(restriction.labelsExcludeRestriction).isNotNull
    assertThat(restriction.labelsExcludeRestriction!!.labels).containsExactlyInAnyOrder("a", "b")
  }
}
