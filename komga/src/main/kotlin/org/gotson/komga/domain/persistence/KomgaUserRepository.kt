package org.gotson.komga.domain.persistence

import org.gotson.komga.domain.model.KomgaUser

interface KomgaUserRepository {
  fun count(): Long

  fun findByIdOrNull(id: String): KomgaUser?
  fun findByEmailIgnoreCase(email: String): KomgaUser?

  fun findAll(): Collection<KomgaUser>

  fun existsByEmailIgnoreCase(email: String): Boolean

  fun insert(user: KomgaUser)
  fun update(user: KomgaUser)

  fun delete(userId: String)
  fun deleteAll()
}
