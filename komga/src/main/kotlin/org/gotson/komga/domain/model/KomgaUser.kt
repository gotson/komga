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
import javax.persistence.Table
import javax.validation.constraints.Email
import javax.validation.constraints.NotBlank

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
}

enum class UserRoles {
  ADMIN
}
