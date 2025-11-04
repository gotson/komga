package org.gotson.komga.domain.service

import io.github.oshai.kotlinlogging.KotlinLogging
import org.gotson.komga.domain.model.AgeRating
import org.gotson.komga.domain.model.Book
import org.gotson.komga.domain.model.KomgaUser
import org.gotson.komga.domain.model.Library
import org.gotson.komga.domain.model.Series
import org.gotson.komga.domain.model.SeriesMetadata
import org.gotson.komga.domain.model.canAccessAdultContent
import org.gotson.komga.domain.model.isAnonymous
import org.gotson.komga.domain.persistence.LibraryRepository
import org.gotson.komga.domain.persistence.SeriesMetadataRepository
import org.springframework.stereotype.Service

private val logger = KotlinLogging.logger {}

/**
 * Service for filtering content based on age restrictions and user authentication.
 * Handles anonymous access and adult content filtering.
 */
@Service
class ContentRestrictionService(
  private val libraryRepository: LibraryRepository,
  private val seriesMetadataRepository: SeriesMetadataRepository,
) {
  /**
   * Check if a user can access a library.
   */
  fun canAccessLibrary(user: KomgaUser, library: Library): Boolean {
    // Anonymous users can only access libraries that allow anonymous access
    if (user.isAnonymous()) {
      return library.allowAnonymousAccess
    }

    // Authenticated users follow normal access rules
    return user.canAccessAllLibraries() || library.id in user.sharedLibrariesIds
  }

  /**
   * Check if a user can access a series based on age rating.
   */
  fun canAccessSeries(user: KomgaUser, series: Series): Boolean {
    val library = libraryRepository.findById(series.libraryId)

    // Check library access first
    if (!canAccessLibrary(user, library)) {
      return false
    }

    // If user is authenticated, they can access all content in accessible libraries
    if (!user.isAnonymous()) {
      return true
    }

    // For anonymous users, check age restrictions
    if (!library.hideAdultContentForAnonymous) {
      // Library allows all content for anonymous users
      return true
    }

    // Check series age rating
    val metadata = try {
      seriesMetadataRepository.findById(series.id)
    } catch (e: Exception) {
      logger.warn { "Failed to get series metadata for ${series.id}: ${e.message}" }
      return true // Default to allowing access if metadata unavailable
    }

    return isContentAccessible(
      ageRating = metadata.ageRating,
      maxAgeForAnonymous = library.ageRestrictionForAnonymous,
    )
  }

  /**
   * Check if a user can access a book based on age rating.
   */
  fun canAccessBook(user: KomgaUser, book: Book): Boolean {
    val library = libraryRepository.findById(book.libraryId)

    // Check library access first
    if (!canAccessLibrary(user, library)) {
      return false
    }

    // If user is authenticated, they can access all content
    if (!user.isAnonymous()) {
      return true
    }

    // For anonymous users, check age restrictions
    if (!library.hideAdultContentForAnonymous) {
      return true
    }

    // Get series metadata for age rating
    val metadata = try {
      seriesMetadataRepository.findById(book.seriesId)
    } catch (e: Exception) {
      logger.warn { "Failed to get series metadata for book ${book.id}: ${e.message}" }
      return true
    }

    return isContentAccessible(
      ageRating = metadata.ageRating,
      maxAgeForAnonymous = library.ageRestrictionForAnonymous,
    )
  }

  /**
   * Filter a list of series based on user access rights.
   */
  fun <T : Any> filterSeriesByAccess(
    user: KomgaUser,
    series: List<Series>,
    transform: (Series) -> T,
  ): List<T> {
    return series
      .filter { canAccessSeries(user, it) }
      .map(transform)
  }

  /**
   * Filter a list of books based on user access rights.
   */
  fun <T : Any> filterBooksByAccess(
    user: KomgaUser,
    books: List<Book>,
    transform: (Book) -> T,
  ): List<T> {
    return books
      .filter { canAccessBook(user, it) }
      .map(transform)
  }

  /**
   * Get accessible libraries for a user.
   */
  fun getAccessibleLibraries(user: KomgaUser): List<Library> {
    val allLibraries = libraryRepository.findAll()

    return allLibraries.filter { library ->
      canAccessLibrary(user, library)
    }
  }

  /**
   * Check if content is accessible based on age rating.
   */
  private fun isContentAccessible(
    ageRating: Int?,
    maxAgeForAnonymous: Int,
  ): Boolean {
    // If no age rating specified, allow access
    if (ageRating == null) {
      return true
    }

    // Check if age rating is within acceptable range
    return ageRating <= maxAgeForAnonymous
  }

  /**
   * Check if content with AgeRating enum requires authentication.
   */
  fun requiresAuthentication(ageRating: AgeRating): Boolean {
    return ageRating.requiresAuthentication()
  }

  /**
   * Get the effective age rating for filtering.
   */
  fun getEffectiveAgeRating(user: KomgaUser, library: Library): Int {
    return if (user.isAnonymous()) {
      library.ageRestrictionForAnonymous
    } else {
      999 // Authenticated users can access all content
    }
  }

  /**
   * Create a content filter for queries.
   */
  fun createContentFilter(user: KomgaUser): ContentFilter {
    return ContentFilter(
      isAnonymous = user.isAnonymous(),
      canAccessAdultContent = user.canAccessAdultContent(),
      accessibleLibraryIds = getAccessibleLibraries(user).map { it.id },
    )
  }
}

/**
 * Content filter for database queries.
 */
data class ContentFilter(
  val isAnonymous: Boolean,
  val canAccessAdultContent: Boolean,
  val accessibleLibraryIds: List<String>,
  val maxAgeRating: Int? = if (isAnonymous) 16 else null,
)
