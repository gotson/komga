package org.gotson.komga.interfaces.rest.dto

import org.gotson.komga.infrastructure.validation.NullOrNotBlank
import java.time.LocalDate
import javax.validation.Valid
import javax.validation.constraints.NotBlank
import kotlin.properties.Delegates

class BookMetadataUpdateDto {
  private val isSet = mutableMapOf<String, Boolean>()
  fun isSet(prop: String) = isSet.getOrDefault(prop, false)

  @get:NullOrNotBlank
  var title: String? = null

  var titleLock: Boolean? = null

  var summary: String? = null

  var summaryLock: Boolean? = null

  @get:NullOrNotBlank
  var number: String? = null

  var numberLock: Boolean? = null

  var numberSort: Float? = null

  var numberSortLock: Boolean? = null

  var releaseDate: LocalDate?
    by Delegates.observable<LocalDate?>(null) { prop, _, _ ->
    isSet[prop.name] = true
  }

  var releaseDateLock: Boolean? = null

  @get:Valid
  var authors: List<AuthorUpdateDto>?
    by Delegates.observable<List<AuthorUpdateDto>?>(null) { prop, _, _ ->
    isSet[prop.name] = true
  }

  var authorsLock: Boolean? = null

  var tags: Set<String>?
    by Delegates.observable<Set<String>?>(null) { prop, _, _ ->
    isSet[prop.name] = true
  }

  var tagsLock: Boolean? = null
}

class AuthorUpdateDto {
  @get:NotBlank
  val name: String? = null

  @get:NotBlank
  val role: String? = null
}
