package org.gotson.komga.interfaces.rest.dto

import org.gotson.komga.infrastructure.validation.NullOrBlankOrISBN
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

  var summary: String?
    by Delegates.observable(null) { prop, _, _ ->
    isSet[prop.name] = true
  }

  var summaryLock: Boolean? = null

  @get:NullOrNotBlank
  var number: String? = null

  var numberLock: Boolean? = null

  var numberSort: Float? = null

  var numberSortLock: Boolean? = null

  var releaseDate: LocalDate?
    by Delegates.observable(null) { prop, _, _ ->
    isSet[prop.name] = true
  }

  var releaseDateLock: Boolean? = null

  @get:Valid
  var authors: List<AuthorUpdateDto>?
    by Delegates.observable(null) { prop, _, _ ->
    isSet[prop.name] = true
  }

  var authorsLock: Boolean? = null

  var tags: Set<String>?
    by Delegates.observable(null) { prop, _, _ ->
    isSet[prop.name] = true
  }

  var tagsLock: Boolean? = null

  @get:NullOrBlankOrISBN
  var isbn: String?
    by Delegates.observable(null) { prop, _, _ ->
    isSet[prop.name] = true
  }

  var isbnLock: Boolean? = null
}

class AuthorUpdateDto {
  @get:NotBlank
  val name: String? = null

  @get:NotBlank
  val role: String? = null
}
