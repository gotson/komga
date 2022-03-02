package org.gotson.komga.domain.model

sealed class ContentRestriction {
  sealed class AgeRestriction(val age: Int) : ContentRestriction() {
    /**
     * Allow only content that has an age rating equal to or under the provided [age]
     *
     * @param[age] the age rating to allow
     */
    class AllowOnlyUnder(age: Int) : AgeRestriction(age)

    /**
     * Exclude content that has an age rating equal to or over the provided [age]
     *
     * @param[age] the age rating to exclude
     */
    class ExcludeOver(age: Int) : AgeRestriction(age)
  }

  sealed class LabelsRestriction(val labels: Set<String>) : ContentRestriction() {
    /**
     * Allow only content that has at least one of the provided sharing [labels]
     *
     * @param[labels] a set of sharing labels to allow access to
     */
    class AllowOnly(labels: Set<String>) : LabelsRestriction(labels)

    /**
     * Exclude content that has at least one of the provided sharing [labels]
     *
     * @param[labels] a set of sharing labels to exclude
     */
    class Exclude(labels: Set<String>) : LabelsRestriction(labels)
  }
}
