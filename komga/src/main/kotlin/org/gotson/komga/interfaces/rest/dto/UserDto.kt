package org.gotson.komga.interfaces.rest.dto

import org.gotson.komga.domain.model.KomgaUser
import org.gotson.komga.infrastructure.security.KomgaPrincipal
import org.gotson.komga.interfaces.rest.UserDto

fun KomgaUser.toDto() =
  UserDto(
    id = id,
    email = email,
    roles = roles.map { it.name }
  )

fun KomgaPrincipal.toDto() = user.toDto()
