package org.gotson.komga.interfaces.api

import org.gotson.komga.domain.model.KomgaUser
import org.gotson.komga.interfaces.api.rest.dto.SeriesDto
import org.springframework.http.HttpStatus
import org.springframework.web.server.ResponseStatusException

/**
 * Convenience function to check for content restriction.
 *
 * @throws[ResponseStatusException] if the user cannot access the content
 */
fun KomgaUser.checkContentRestriction(series: SeriesDto) {
  if (!canAccessLibrary(series.libraryId)) throw ResponseStatusException(HttpStatus.FORBIDDEN)
  if (!isContentAllowed(series.metadata.ageRating, series.metadata.sharingLabels)) throw ResponseStatusException(HttpStatus.FORBIDDEN)
}
