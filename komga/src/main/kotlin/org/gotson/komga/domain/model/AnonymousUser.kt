package org.gotson.komga.domain.model

import java.time.LocalDateTime

/**
 * Represents an anonymous/guest user with limited permissions.
 * Anonymous users can only access non-adult content.
 */
data class AnonymousUser(
  override val id: String = "anonymous",
  val sessionId: String? = null,
  val ipAddress: String? = null,
  override val createdDate: LocalDateTime = LocalDateTime.now(),
  override val lastModifiedDate: LocalDateTime = createdDate,
) : Auditable {
  companion object {
    const val ANONYMOUS_ID = "anonymous"
    const val ANONYMOUS_EMAIL = "anonymous@komga"

    fun create(sessionId: String? = null, ipAddress: String? = null): AnonymousUser {
      return AnonymousUser(
        sessionId = sessionId,
        ipAddress = ipAddress,
      )
    }

    /**
     * Create a KomgaUser representation for anonymous access.
     */
    fun asKomgaUser(): KomgaUser {
      return KomgaUser(
        email = ANONYMOUS_EMAIL,
        password = "",
        roleAdmin = false,
        roleFileDownload = false,
        rolePageStreaming = true, // Allow reading
        sharedLibrariesIds = emptySet(), // Access based on library settings
        id = ANONYMOUS_ID,
      )
    }
  }

  /**
   * Check if this is an anonymous user.
   */
  fun isAnonymous(): Boolean = true

  /**
   * Anonymous users cannot access adult content.
   */
  fun canAccessAdultContent(): Boolean = false

  /**
   * Anonymous users have read-only access.
   */
  fun isReadOnly(): Boolean = true
}

/**
 * Extension to check if a KomgaUser is anonymous.
 */
fun KomgaUser.isAnonymous(): Boolean {
  return this.id == AnonymousUser.ANONYMOUS_ID || this.email == AnonymousUser.ANONYMOUS_EMAIL
}

/**
 * Extension to check if a KomgaUser can access adult content.
 */
fun KomgaUser.canAccessAdultContent(): Boolean {
  return !isAnonymous()
}
