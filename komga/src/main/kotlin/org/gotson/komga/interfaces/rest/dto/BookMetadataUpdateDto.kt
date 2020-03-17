package org.gotson.komga.interfaces.rest.dto

import org.gotson.komga.domain.model.BookMetadata
import org.gotson.komga.infrastructure.validation.NullOrNotBlank
import java.time.LocalDate
import javax.validation.Valid
import javax.validation.constraints.NotBlank
import javax.validation.constraints.PositiveOrZero
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

  var readingDirection: BookMetadata.ReadingDirection?
    by Delegates.observable<BookMetadata.ReadingDirection?>(null) { prop, _, _ ->
      isSet[prop.name] = true
    }

  var readingDirectionLock: Boolean? = null

  var publisher: String? = null

  var publisherLock: Boolean? = null

  @get:PositiveOrZero
  var ageRating: Int?
    by Delegates.observable<Int?>(null) { prop, _, _ ->
      isSet[prop.name] = true
    }

  var ageRatingLock: Boolean? = null

  var releaseDate: LocalDate?
    by Delegates.observable<LocalDate?>(null) { prop, _, _ ->
      isSet[prop.name] = true
    }

  var releaseDateLock: Boolean? = null

  @Valid
  var authors: List<AuthorUpdateDto>? = null

  var authorsLock: Boolean? = null
}

class AuthorUpdateDto {
  @get:NotBlank
  val name: String? = null

  @get:NotBlank
  val role: String? = null
}
