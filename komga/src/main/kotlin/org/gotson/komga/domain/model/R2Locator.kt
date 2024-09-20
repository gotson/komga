package org.gotson.komga.domain.model

import com.fasterxml.jackson.annotation.JsonInclude

/**
 * Locators are meant to provide a precise location in a publication in a format that can be stored and shared.
 *
 * There are many different use cases for locators:
 * - reporting and saving the current progression
 * - bookmarks
 * - highlights & annotations
 * - search results
 * - human-readable (as-in shareable) references
 * - jumping to a location
 * - enhancing a table of contents
 *
 * Each locator must contain a reference to a resource in a publication (href and type). href must not point to the fragment of a resource.
 *
 * It may also contain:
 * - a title (`title`)
 * - one or more locations in a resource (grouped together in locations)
 * - one or more text references, if the resource is a document (`text`)
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
data class R2Locator(
  /**
   * The URI of the resource that the Locator Object points to.
   */
  val href: String,
  /**
   * The media type of the resource that the Locator Object points to.
   */
  val type: String,
  /**
   * The title of the chapter or section which is more relevant in the context of this locator.
   */
  val title: String? = null,
  /**
   * One or more alternative expressions of the location.
   */
  val locations: Location? = null,
  /**
   * Textual context of the locator.
   */
  val text: Text? = null,
  /**
   * Komga specific, used to have a mapping between a [R2Locator] and a koboSpan
   */
  val koboSpan: String? = null,
) {
  @JsonInclude(JsonInclude.Include.NON_EMPTY)
  data class Location(
    /**
     * Contains one or more fragment in the resource referenced by the Locator Object.
     */
    val fragments: List<String> = emptyList(),
    /**
     * Progression in the resource expressed as a percentage.
     * Between 0 and 1.
     */
    val progression: Float? = null,
    /**
     * An index in the publication.
     */
    val position: Int? = null,
    /**
     * Progression in the publication expressed as a percentage.
     * Between 0 and 1.
     */
    val totalProgression: Float? = null,
  )

  /**
   * A Locator Text Object contains multiple text fragments, useful to give a context to the Locator or for highlights.
   */
  @JsonInclude(JsonInclude.Include.NON_EMPTY)
  data class Text(
    /**
     * The text after the locator.
     */
    val after: String? = null,
    /**
     * The text before the locator.
     */
    val before: String? = null,
    /**
     * The text at the locator.
     */
    val highlight: String? = null,
  )
}
