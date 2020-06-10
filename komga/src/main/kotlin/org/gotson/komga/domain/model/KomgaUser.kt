package org.gotson.komga.domain.model

import java.time.LocalDateTime
import javax.validation.constraints.Email
import javax.validation.constraints.NotBlank

const val ROLE_USER = "USER"
const val ROLE_ADMIN = "ADMIN"
const val ROLE_FILE_DOWNLOAD = "FILE_DOWNLOAD"
const val ROLE_PAGE_STREAMING = "PAGE_STREAMING"

data class KomgaUser(
  @Email
  @NotBlank
  val email: String,
  @NotBlank
  val password: String,
  val roleAdmin: Boolean,
  val roleFileDownload: Boolean = true,
  val rolePageStreaming: Boolean = true,
  val sharedLibrariesIds: Set<Long> = emptySet(),
  val sharedAllLibraries: Boolean = true,
  val id: Long = 0,
  override val createdDate: LocalDateTime = LocalDateTime.now(),
  override val lastModifiedDate: LocalDateTime = LocalDateTime.now()
) : Auditable() {

  fun roles(): Set<String> {
    val roles = mutableSetOf(ROLE_USER)
    if (roleAdmin) roles.add(ROLE_ADMIN)
    if (roleFileDownload) roles.add(ROLE_FILE_DOWNLOAD)
    if (rolePageStreaming) roles.add(ROLE_PAGE_STREAMING)
    return roles
  }

  fun getAuthorizedLibraryIds(libraryIds: Collection<Long>?) =
    when {
      // limited user & libraryIds are specified: filter on provided libraries intersecting user's authorized libraries
      !sharedAllLibraries && !libraryIds.isNullOrEmpty() -> libraryIds.intersect(sharedLibrariesIds)

      // limited user: filter on user's authorized libraries
      !sharedAllLibraries -> sharedLibrariesIds

      // non-limited user: filter on provided libraries
      !libraryIds.isNullOrEmpty() -> libraryIds

      else -> emptyList()
    }

  fun canAccessBook(book: Book): Boolean {
    return sharedAllLibraries || sharedLibrariesIds.any { it == book.libraryId }
  }

  fun canAccessSeries(series: Series): Boolean {
    return sharedAllLibraries || sharedLibrariesIds.any { it == series.libraryId }
  }

  fun canAccessLibrary(libraryId: Long): Boolean =
    sharedAllLibraries || sharedLibrariesIds.any { it == libraryId }

  fun canAccessLibrary(library: Library): Boolean {
    return sharedAllLibraries || sharedLibrariesIds.any { it == library.id }
  }

  override fun toString(): String {
    return "KomgaUser(email='$email', roleAdmin=$roleAdmin, roleFileDownload=$roleFileDownload, rolePageStreaming=$rolePageStreaming, sharedLibrariesIds=$sharedLibrariesIds, sharedAllLibraries=$sharedAllLibraries, id=$id, createdDate=$createdDate, lastModifiedDate=$lastModifiedDate)"
  }
}
