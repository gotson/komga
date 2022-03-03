package org.gotson.komga.domain.model

sealed class ContentRestriction {
  sealed class AgeRestriction : ContentRestriction() {
    abstract val age: Int

    /**
     * Allow only content that has an age rating equal to or under the provided [age]
     *
     * @param[age] the age rating to allow
     */
    data class AllowOnlyUnder(override val age: Int) : AgeRestriction()

    /**
     * Exclude content that has an age rating equal to or over the provided [age]
     *
     * @param[age] the age rating to exclude
     */
    data class ExcludeOver(override val age: Int) : AgeRestriction()
  }

  sealed class LabelsRestriction : ContentRestriction() {
    abstract val labels: Set<String>

    /**
     * Allow only content that has at least one of the provided sharing [labels]
     *
     * @param[labels] a set of sharing labels to allow access to
     */
    data class AllowOnly(override val labels: Set<String>) : LabelsRestriction()

    /**
     * Exclude content that has at least one of the provided sharing [labels]
     *
     * @param[labels] a set of sharing labels to exclude
     */
    data class Exclude(override val labels: Set<String>) : LabelsRestriction()
  }
}
