package org.gotson.komga.domain.model

import com.github.f4b6a3.tsid.TsidCreator
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import org.gotson.komga.language.lowerNotBlank
import java.time.LocalDateTime

data class KomgaUser(
  @Email(regexp = ".+@.+\\..+")
  @NotBlank
  val email: String,
  @NotBlank
  val password: String,
  val roles: Set<UserRoles> = setOf(UserRoles.FILE_DOWNLOAD, UserRoles.PAGE_STREAMING),
  val sharedLibrariesIds: Set<String> = emptySet(),
  val sharedAllLibraries: Boolean = true,
  val restrictions: ContentRestrictions = ContentRestrictions(),
  val id: String = TsidCreator.getTsid256().toString(),
  override val createdDate: LocalDateTime = LocalDateTime.now(),
  override val lastModifiedDate: LocalDateTime = createdDate,
) : Auditable {
  @delegate:Transient
  val isAdmin: Boolean by lazy {
    roles.contains(UserRoles.ADMIN)
  }

  /**
   * Return the list of LibraryIds this user is authorized to view, intersecting the provided list of LibraryIds.
   * @param libraryIds an optional list of LibraryIds to filter on
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

  fun canAccessAllLibraries(): Boolean = sharedAllLibraries || isAdmin

  fun canAccessLibrary(libraryId: String): Boolean = canAccessAllLibraries() || sharedLibrariesIds.any { it == libraryId }

  fun canAccessLibrary(library: Library): Boolean = canAccessAllLibraries() || sharedLibrariesIds.any { it == library.id }

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

  override fun toString(): String = "KomgaUser(createdDate=$createdDate, email='$email', roles=$roles, sharedLibrariesIds=$sharedLibrariesIds, sharedAllLibraries=$sharedAllLibraries, restrictions=$restrictions, id='$id', lastModifiedDate=$lastModifiedDate)"
}
