package org.gotson.komga.interfaces.api.rest.dto

import org.gotson.komga.domain.model.Author
import org.gotson.komga.domain.model.BookMetadata
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

fun BookMetadata.patch(patch: BookMetadataUpdateDto) =
  patch.let {
    this.copy(
      title = patch.title ?: this.title,
      titleLock = patch.titleLock ?: this.titleLock,
      summary = if (patch.isSet("summary")) patch.summary ?: "" else this.summary,
      summaryLock = patch.summaryLock ?: this.summaryLock,
      number = patch.number ?: this.number,
      numberLock = patch.numberLock ?: this.numberLock,
      numberSort = patch.numberSort ?: this.numberSort,
      numberSortLock = patch.numberSortLock ?: this.numberSortLock,
      releaseDate = if (patch.isSet("releaseDate")) patch.releaseDate else this.releaseDate,
      releaseDateLock = patch.releaseDateLock ?: this.releaseDateLock,
      authors = if (patch.isSet("authors")) {
        if (patch.authors != null) patch.authors!!.map { Author(it.name ?: "", it.role ?: "") } else emptyList()
      } else this.authors,
      authorsLock = patch.authorsLock ?: this.authorsLock,
      tags = if (patch.isSet("tags")) {
        if (patch.tags != null) patch.tags!! else emptySet()
      } else this.tags,
      tagsLock = patch.tagsLock ?: this.tagsLock,
      isbn = if (patch.isSet("isbn")) patch.isbn?.filter { it.isDigit() } ?: "" else this.isbn,
      isbnLock = patch.isbnLock ?: this.isbnLock
    )
  }
