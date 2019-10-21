package org.gotson.komga.domain.model

import javax.persistence.CollectionTable
import javax.persistence.Column
import javax.persistence.ElementCollection
import javax.persistence.Entity
import javax.persistence.EnumType
import javax.persistence.Enumerated
import javax.persistence.FetchType
import javax.persistence.GeneratedValue
import javax.persistence.Id
import javax.persistence.JoinColumn
import javax.persistence.JoinTable
import javax.persistence.ManyToMany
import javax.persistence.Table
import javax.validation.constraints.Email
import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotNull

@Entity
@Table(name = "user")
class KomgaUser(
    @Email
    @NotBlank
    @Column(name = "email", nullable = false, unique = true)
    var email: String,

    @NotBlank
    @Column(name = "password", nullable = false)
    var password: String,

    @Enumerated(EnumType.STRING)
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "user_role", joinColumns = [JoinColumn(name = "user_id")])
    var roles: MutableSet<UserRoles> = mutableSetOf()

) : AuditableEntity() {

  @Id
  @GeneratedValue
  @Column(name = "id", nullable = false)
  var id: Long = 0

  @ManyToMany(fetch = FetchType.EAGER)
  @JoinTable(
      name = "user_library_sharing",
      joinColumns = [JoinColumn(name = "user_id", referencedColumnName = "id")],
      inverseJoinColumns = [JoinColumn(name = "library_id", referencedColumnName = "id")]
  )
  var sharedLibraries: MutableSet<Library> = mutableSetOf()

  @NotNull
  @Column(name = "shared_all_libraries", nullable = false)
  var sharedAllLibraries: Boolean = true
    get() = if (roles.contains(UserRoles.ADMIN)) true else field
    set(value) {
      field = if (roles.contains(UserRoles.ADMIN)) true else value
    }


  fun canAccessSeries(series: Series): Boolean {
    return sharedAllLibraries || sharedLibraries.any { it.id == series.library.id }
  }

  fun canAccessLibrary(library: Library): Boolean {
    return sharedAllLibraries || sharedLibraries.any { it.id == library.id }
  }
}

enum class UserRoles {
  ADMIN
}
