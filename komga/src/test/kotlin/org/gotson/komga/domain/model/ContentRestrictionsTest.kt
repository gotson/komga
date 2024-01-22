package org.gotson.komga.domain.model

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class ContentRestrictionsTest {
  @Test
  fun `given no arguments when creating restriction then all restrictions are null`() {
    val restriction = ContentRestrictions()

    assertThat(restriction.ageRestriction).isNull()
    assertThat(restriction.labelsAllow).isEmpty()
    assertThat(restriction.labelsExclude).isEmpty()
  }

  @Test
  fun `given AllowOnlyUnder restriction only when creating restriction then label restrictions are null`() {
    val restriction = ContentRestrictions(AgeRestriction(10, AllowExclude.ALLOW_ONLY))

    assertThat(restriction.ageRestriction).isNotNull
    assertThat(restriction.ageRestriction!!.age).isEqualTo(10)
    assertThat(restriction.ageRestriction!!.restriction).isEqualTo(AllowExclude.ALLOW_ONLY)

    assertThat(restriction.labelsAllow).isEmpty()
    assertThat(restriction.labelsExclude).isEmpty()
  }

  @Test
  fun `given ExcludeOver only when creating restriction then label restrictions are null`() {
    val restriction = ContentRestrictions(AgeRestriction(10, AllowExclude.EXCLUDE))

    assertThat(restriction.ageRestriction).isNotNull
    assertThat(restriction.ageRestriction!!.age).isEqualTo(10)
    assertThat(restriction.ageRestriction!!.restriction).isEqualTo(AllowExclude.EXCLUDE)

    assertThat(restriction.labelsAllow).isEmpty()
    assertThat(restriction.labelsExclude).isEmpty()
  }

  @Test
  fun `given empty labels when creating restriction then label restrictions are normalized`() {
    val restriction =
      ContentRestrictions(
        labelsAllow = setOf("", " "),
        labelsExclude = setOf("", " "),
      )

    assertThat(restriction.labelsAllow).isEmpty()
    assertThat(restriction.labelsExclude).isEmpty()
  }

  @Test
  fun `given labels with duplicate values when creating restriction then label restrictions are normalized`() {
    val restriction =
      ContentRestrictions(
        labelsAllow = setOf("a", "b", "B", "b ", "b", " B "),
        labelsExclude = setOf("c", "d", "D", "d ", "d", " D "),
      )

    assertThat(restriction.labelsAllow).containsExactlyInAnyOrder("a", "b")
    assertThat(restriction.labelsExclude).containsExactlyInAnyOrder("c", "d")
  }

  @Test
  fun `given labels with same value in both allow and exclude when creating restriction then exclude labels are removed from allow labels`() {
    val restriction =
      ContentRestrictions(
        labelsAllow = setOf("a", "b", "B", "b ", "b", " B "),
        labelsExclude = setOf(" A ", "d", "D", "d ", "d", " D "),
      )

    assertThat(restriction.labelsAllow).containsExactlyInAnyOrder("b")
    assertThat(restriction.labelsExclude).containsExactlyInAnyOrder("a", "d")
  }

  @Test
  fun `given allow labels with all values in exclude labels when creating restriction then allow labels is null`() {
    val restriction =
      ContentRestrictions(
        labelsAllow = setOf("a", "b", "B", "b ", "b", " B "),
        labelsExclude = setOf(" A ", "b", "B", "B ", "b", " B "),
      )

    assertThat(restriction.labelsAllow).isEmpty()
    assertThat(restriction.labelsExclude).containsExactlyInAnyOrder("a", "b")
  }
}
