package org.gotson.komga.domain.service

import mu.KotlinLogging
import org.gotson.komga.domain.model.BookMetadata
import org.gotson.komga.domain.model.BookMetadataPatch
import org.gotson.komga.domain.model.SeriesMetadata
import org.gotson.komga.domain.model.SeriesMetadataPatch
import org.springframework.stereotype.Service

private val logger = KotlinLogging.logger {}

@Service
class MetadataApplier {

  private fun <T> getIfNotLocked(original: T, patched: T?, lock: Boolean): T =
    if (patched != null && !lock) patched
    else original

  fun apply(patch: BookMetadataPatch, metadata: BookMetadata): BookMetadata =
    with(metadata) {
      copy(
        title = getIfNotLocked(title, patch.title, titleLock),
        summary = getIfNotLocked(summary, patch.summary, summaryLock),
        number = getIfNotLocked(number, patch.number, numberLock),
        numberSort = getIfNotLocked(numberSort, patch.numberSort, numberSortLock),
        readingDirection = getIfNotLocked(readingDirection, patch.readingDirection, readingDirectionLock),
        releaseDate = getIfNotLocked(releaseDate, patch.releaseDate, releaseDateLock),
        ageRating = getIfNotLocked(ageRating, patch.ageRating, ageRatingLock),
        publisher = getIfNotLocked(publisher, patch.publisher, publisherLock),
        authors = getIfNotLocked(authors, patch.authors, authorsLock)
      )
    }

  fun apply(patch: SeriesMetadataPatch, metadata: SeriesMetadata): SeriesMetadata =
    with(metadata) {
      copy(
        status = getIfNotLocked(status, patch.status, statusLock),
        title = getIfNotLocked(title, patch.title, titleLock),
        titleSort = getIfNotLocked(titleSort, patch.titleSort, titleSortLock)
      )
    }

}
