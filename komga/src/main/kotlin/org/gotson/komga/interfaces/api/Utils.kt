package org.gotson.komga.interfaces.api

import org.gotson.komga.domain.model.KomgaUser
import org.gotson.komga.domain.persistence.BookRepository
import org.gotson.komga.domain.persistence.SeriesMetadataRepository
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

/**
 * Convenience function to check for content restriction.
 * This will retrieve data from repositories if needed.
 *
 * @throws[ResponseStatusException] if the user cannot access the content
 */
fun KomgaUser.checkContentRestriction(
  bookId: String,
  bookRepository: BookRepository,
  seriesMetadataRepository: SeriesMetadataRepository,
) {
  if (!sharedAllLibraries) {
    bookRepository.getLibraryIdOrNull(bookId)?.let {
      if (!canAccessLibrary(it)) throw ResponseStatusException(HttpStatus.FORBIDDEN)
    } ?: throw ResponseStatusException(HttpStatus.NOT_FOUND)
  }
  if (restrictions.isRestricted)
    bookRepository.getSeriesIdOrNull(bookId)?.let { seriesId ->
      seriesMetadataRepository.findById(seriesId).let {
        if (!isContentAllowed(it.ageRating, it.sharingLabels)) throw ResponseStatusException(HttpStatus.FORBIDDEN)
      }
    } ?: throw ResponseStatusException(HttpStatus.NOT_FOUND)
}
