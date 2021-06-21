package org.gotson.komga.domain.model

import com.github.f4b6a3.tsid.TsidCreator
import java.io.Serializable
import java.time.LocalDateTime
import javax.validation.constraints.Email
import javax.validation.constraints.NotBlank

const val ROLE_USER = "USER"
const val ROLE_ADMIN = "ADMIN"
const val ROLE_FILE_DOWNLOAD = "FILE_DOWNLOAD"
const val ROLE_PAGE_STREAMING = "PAGE_STREAMING"

data class KomgaUser(
  @Email(regexp = ".+@.+\\..+")
  @NotBlank
  val email: String,
  @NotBlank
  val password: String,
  val roleAdmin: Boolean,
  val roleFileDownload: Boolean = true,
  val rolePageStreaming: Boolean = true,
  val sharedLibrariesIds: Set<String> = emptySet(),
  val sharedAllLibraries: Boolean = true,
  val id: String = TsidCreator.getTsid256().toString(),
  override val createdDate: LocalDateTime = LocalDateTime.now(),
  override val lastModifiedDate: LocalDateTime = LocalDateTime.now()
) : Auditable(), Serializable {

  fun roles(): Set<String> {
    val roles = mutableSetOf(ROLE_USER)
    if (roleAdmin) roles.add(ROLE_ADMIN)
    if (roleFileDownload) roles.add(ROLE_FILE_DOWNLOAD)
    if (rolePageStreaming) roles.add(ROLE_PAGE_STREAMING)
    return roles
  }

  /**
   * Return the list of LibraryIds this user is authorized to view, intersecting the provided list of LibraryIds.
   *
   * @return a list of authorised LibraryIds, or null if the user is authorized to see all libraries
   */
  fun getAuthorizedLibraryIds(libraryIds: Collection<String>?): Collection<String>? =
    when {
      // limited user & libraryIds are specified: filter on provided libraries intersecting user's authorized libraries
      !sharedAllLibraries && libraryIds != null -> libraryIds.intersect(sharedLibrariesIds)

      // limited user: filter on user's authorized libraries
      !sharedAllLibraries && libraryIds == null -> sharedLibrariesIds

      // non-limited user & libraryIds are specified: filter on provided libraries
      libraryIds != null -> libraryIds

      // non-limited user & no libraryIds specified: return null, meaning no filtering
      else -> null
    }

  fun canAccessBook(book: Book): Boolean {
    return sharedAllLibraries || sharedLibrariesIds.any { it == book.libraryId }
  }

  fun canAccessSeries(series: Series): Boolean {
    return sharedAllLibraries || sharedLibrariesIds.any { it == series.libraryId }
  }

  fun canAccessLibrary(libraryId: String): Boolean =
    sharedAllLibraries || sharedLibrariesIds.any { it == libraryId }

  fun canAccessLibrary(library: Library): Boolean {
    return sharedAllLibraries || sharedLibrariesIds.any { it == library.id }
  }

  override fun toString(): String {
    return "KomgaUser(email='$email', roleAdmin=$roleAdmin, roleFileDownload=$roleFileDownload, rolePageStreaming=$rolePageStreaming, sharedLibrariesIds=$sharedLibrariesIds, sharedAllLibraries=$sharedAllLibraries, id=$id, createdDate=$createdDate, lastModifiedDate=$lastModifiedDate)"
  }
}
