package org.gotson.komga.domain.model

import javax.persistence.Column
import javax.persistence.Embeddable
import javax.validation.constraints.NotBlank

@Embeddable
class Author {
  constructor(name: String, role: String) {
    this.name = name
    this.role = role
  }

  @NotBlank
  @Column(name = "name", nullable = false)
  var name: String
    set(value) {
      require(value.isNotBlank()) { "name must not be blank" }
      field = value.trim()
    }

  @NotBlank
  @Column(name = "role", nullable = false)
  var role: String
    set(value) {
      require(value.isNotBlank()) { "role must not be blank" }
      field = value.trim().toLowerCase()
    }

}
