package org.gotson.komga.interfaces.api

import org.gotson.komga.domain.model.Book
import org.gotson.komga.domain.model.KomgaUser
import org.gotson.komga.domain.persistence.BookRepository
import org.gotson.komga.domain.persistence.SeriesMetadataRepository
import org.gotson.komga.domain.persistence.SeriesRepository
import org.gotson.komga.domain.persistence.ThumbnailBookRepository
import org.gotson.komga.domain.persistence.ThumbnailSeriesRepository
import org.gotson.komga.interfaces.api.rest.dto.BookDto
import org.gotson.komga.interfaces.api.rest.dto.SeriesDto
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import org.springframework.web.server.ResponseStatusException

@Component
class ContentRestrictionChecker(
  private val seriesMetadataRepository: SeriesMetadataRepository,
  private val bookRepository: BookRepository,
  private val thumbnailBookRepository: ThumbnailBookRepository,
  private val seriesRepository: SeriesRepository,
  private val thumbnailSeriesRepository: ThumbnailSeriesRepository,
) {
  /**
   * Convenience function to check for content restriction.
   * This will retrieve data from repositories if needed.
   *
   * @throws[ResponseStatusException] if the user cannot access the content
   */
  fun checkContentRestrictionBook(
    komgaUser: KomgaUser,
    book: BookDto,
  ) {
    if (!komgaUser.canAccessLibrary(book.libraryId)) throw ResponseStatusException(HttpStatus.FORBIDDEN)
    if (komgaUser.restrictions.isRestricted)
      seriesMetadataRepository.findById(book.seriesId).let {
        if (!komgaUser.isContentAllowed(it.ageRating, it.sharingLabels)) throw ResponseStatusException(HttpStatus.FORBIDDEN)
      }
  }

  /**
   * Convenience function to check for content restriction.
   * This will retrieve data from repositories if needed.
   *
   * @throws[ResponseStatusException] if the user cannot access the content
   */
  fun checkContentRestrictionBook(
    komgaUser: KomgaUser,
    book: Book,
  ) {
    if (!komgaUser.canAccessLibrary(book.libraryId)) throw ResponseStatusException(HttpStatus.FORBIDDEN)
    if (komgaUser.restrictions.isRestricted)
      seriesMetadataRepository.findById(book.seriesId).let {
        if (!komgaUser.isContentAllowed(it.ageRating, it.sharingLabels)) throw ResponseStatusException(HttpStatus.FORBIDDEN)
      }
  }

  /**
   * Convenience function to check for content restriction.
   * This will retrieve data from repositories if needed.
   *
   * @throws[ResponseStatusException] if the user cannot access the content
   */
  fun checkContentRestrictionBook(
    komgaUser: KomgaUser,
    bookId: String,
  ) {
    if (!komgaUser.canAccessAllLibraries()) {
      bookRepository.getLibraryIdOrNull(bookId)?.let {
        if (!komgaUser.canAccessLibrary(it)) throw ResponseStatusException(HttpStatus.FORBIDDEN)
      } ?: throw ResponseStatusException(HttpStatus.NOT_FOUND)
    }
    if (komgaUser.restrictions.isRestricted)
      bookRepository.getSeriesIdOrNull(bookId)?.let { seriesId ->
        seriesMetadataRepository.findById(seriesId).let {
          if (!komgaUser.isContentAllowed(it.ageRating, it.sharingLabels)) throw ResponseStatusException(HttpStatus.FORBIDDEN)
        }
      } ?: throw ResponseStatusException(HttpStatus.NOT_FOUND)
  }

  /**
   * Convenience function to check for content restriction.
   *
   * @throws[ResponseStatusException] if the user cannot access the content
   */
  fun checkContentRestrictionBookThumbnail(
    komgaUser: KomgaUser,
    thumbnailId: String,
  ) {
    if (!komgaUser.canAccessAllLibraries()) {
      thumbnailBookRepository.getLibraryIdOrNull(thumbnailId)?.let {
        if (!komgaUser.canAccessLibrary(it)) throw ResponseStatusException(HttpStatus.FORBIDDEN)
      } ?: throw ResponseStatusException(HttpStatus.NOT_FOUND)
    }
    if (komgaUser.restrictions.isRestricted)
      thumbnailBookRepository.getSeriesIdOrNull(thumbnailId)?.let { seriesId ->
        seriesMetadataRepository.findById(seriesId).let {
          if (!komgaUser.isContentAllowed(it.ageRating, it.sharingLabels)) throw ResponseStatusException(HttpStatus.FORBIDDEN)
        }
      } ?: throw ResponseStatusException(HttpStatus.NOT_FOUND)
  }

  /**
   * Convenience function to check for content restriction.
   *
   * @throws[ResponseStatusException] if the user cannot access the content
   */
  fun checkContentRestrictionSeries(
    komgaUser: KomgaUser,
    series: SeriesDto,
  ) {
    if (!komgaUser.canAccessLibrary(series.libraryId)) throw ResponseStatusException(HttpStatus.FORBIDDEN)
    if (!komgaUser.isContentAllowed(series.metadata.ageRating, series.metadata.sharingLabels)) throw ResponseStatusException(HttpStatus.FORBIDDEN)
  }

  /**
   * Convenience function to check for content restriction.
   *
   * @throws[ResponseStatusException] if the user cannot access the content
   */
  fun checkContentRestrictionSeries(
    komgaUser: KomgaUser,
    seriesId: String,
  ) {
    if (!komgaUser.canAccessAllLibraries()) {
      seriesRepository.getLibraryId(seriesId)?.let {
        if (!komgaUser.canAccessLibrary(it)) throw ResponseStatusException(HttpStatus.FORBIDDEN)
      } ?: throw ResponseStatusException(HttpStatus.NOT_FOUND)
    }
    if (komgaUser.restrictions.isRestricted)
      seriesMetadataRepository.findById(seriesId).let {
        if (!komgaUser.isContentAllowed(it.ageRating, it.sharingLabels)) throw ResponseStatusException(HttpStatus.FORBIDDEN)
      }
  }

  /**
   * Convenience function to check for content restriction.
   *
   * @throws[ResponseStatusException] if the user cannot access the content
   */
  fun checkContentRestrictionSeriesThumbnail(
    komgaUser: KomgaUser,
    thumbnailId: String,
  ) {
    if (!komgaUser.canAccessAllLibraries()) {
      thumbnailSeriesRepository.getLibraryIdOrNull(thumbnailId)?.let {
        if (!komgaUser.canAccessLibrary(it)) throw ResponseStatusException(HttpStatus.FORBIDDEN)
      } ?: throw ResponseStatusException(HttpStatus.NOT_FOUND)
    }
    if (komgaUser.restrictions.isRestricted)
      thumbnailSeriesRepository.getSeriesIdOrNull(thumbnailId)?.let { seriesId ->
        seriesMetadataRepository.findById(seriesId).let {
          if (!komgaUser.isContentAllowed(it.ageRating, it.sharingLabels)) throw ResponseStatusException(HttpStatus.FORBIDDEN)
        }
      } ?: throw ResponseStatusException(HttpStatus.NOT_FOUND)
  }
}
