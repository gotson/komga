package org.gotson.komga.domain.model

import com.github.f4b6a3.tsid.TsidCreator
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import org.gotson.komga.language.lowerNotBlank
import java.time.LocalDateTime

const val ROLE_USER = "USER"
const val ROLE_ADMIN = "ADMIN"
const val ROLE_FILE_DOWNLOAD = "FILE_DOWNLOAD"
const val ROLE_PAGE_STREAMING = "PAGE_STREAMING"
const val ROLE_KOBO_SYNC = "KOBO_SYNC"

data class KomgaUser(
  @Email(regexp = ".+@.+\\..+")
  @NotBlank
  val email: String,
  @NotBlank
  val password: String,
  val roleAdmin: Boolean,
  val roleFileDownload: Boolean = true,
  val rolePageStreaming: Boolean = true,
  val roleKoboSync: Boolean = false,
  val sharedLibrariesIds: Set<String> = emptySet(),
  val sharedAllLibraries: Boolean = true,
  val restrictions: ContentRestrictions = ContentRestrictions(),
  val id: String = TsidCreator.getTsid256().toString(),
  override val createdDate: LocalDateTime = LocalDateTime.now(),
  override val lastModifiedDate: LocalDateTime = createdDate,
) : Auditable {
  @delegate:Transient
  val roles: Set<String> by lazy {
    buildSet {
      add(ROLE_USER)
      if (roleAdmin) add(ROLE_ADMIN)
      if (roleFileDownload) add(ROLE_FILE_DOWNLOAD)
      if (rolePageStreaming) add(ROLE_PAGE_STREAMING)
      if (roleKoboSync) add(ROLE_KOBO_SYNC)
    }
  }

  /**
   * Return the list of LibraryIds this user is authorized to view, intersecting the provided list of LibraryIds.
   *
   * @return a list of authorised LibraryIds, or null if the user is authorized to see all libraries
   */
  fun getAuthorizedLibraryIds(libraryIds: Collection<String>?): Collection<String>? =
    when {
      // limited user & libraryIds are specified: filter on provided libraries intersecting user's authorized libraries
      !canAccessAllLibraries() && libraryIds != null -> libraryIds.intersect(sharedLibrariesIds)

      // limited user: filter on user's authorized libraries
      !canAccessAllLibraries() && libraryIds == null -> sharedLibrariesIds

      // non-limited user & libraryIds are specified: filter on provided libraries
      libraryIds != null -> libraryIds

      // non-limited user & no libraryIds specified: return null, meaning no filtering
      else -> null
    }

  fun canAccessAllLibraries(): Boolean = sharedAllLibraries || roleAdmin

  fun canAccessLibrary(libraryId: String): Boolean =
    canAccessAllLibraries() || sharedLibrariesIds.any { it == libraryId }

  fun canAccessLibrary(library: Library): Boolean =
    canAccessAllLibraries() || sharedLibrariesIds.any { it == library.id }

  fun isContentAllowed(
    ageRating: Int? = null,
    sharingLabels: Set<String> = emptySet(),
  ): Boolean {
    val labels = sharingLabels.lowerNotBlank().toSet()

    val ageAllowed =
      if (restrictions.ageRestriction?.restriction == AllowExclude.ALLOW_ONLY)
        ageRating != null && ageRating <= restrictions.ageRestriction.age
      else
        null

    val labelAllowed =
      if (restrictions.labelsAllow.isNotEmpty())
        restrictions.labelsAllow.intersect(labels).isNotEmpty()
      else
        null

    val allowed =
      when {
        ageAllowed == null -> labelAllowed != false
        labelAllowed == null -> ageAllowed != false
        else -> ageAllowed != false || labelAllowed != false
      }
    if (!allowed) return false

    val ageDenied =
      if (restrictions.ageRestriction?.restriction == AllowExclude.EXCLUDE)
        ageRating != null && ageRating >= restrictions.ageRestriction.age
      else
        false

    val labelDenied =
      if (restrictions.labelsExclude.isNotEmpty())
        restrictions.labelsExclude.intersect(labels).isNotEmpty()
      else
        false

    return !ageDenied && !labelDenied
  }

  override fun toString(): String =
    "KomgaUser(email='$email', roleAdmin=$roleAdmin, roleFileDownload=$roleFileDownload, rolePageStreaming=$rolePageStreaming, roleKoboSync=$roleKoboSync, sharedLibrariesIds=$sharedLibrariesIds, sharedAllLibraries=$sharedAllLibraries, restrictions=$restrictions, id='$id', createdDate=$createdDate, lastModifiedDate=$lastModifiedDate)"
}
