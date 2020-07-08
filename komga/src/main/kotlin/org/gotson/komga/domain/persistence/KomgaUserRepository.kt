package org.gotson.komga.domain.persistence

import org.gotson.komga.domain.model.KomgaUser

interface KomgaUserRepository {
  fun count(): Long

  fun findAll(): Collection<KomgaUser>
  fun findByIdOrNull(id: Long): KomgaUser?

  fun insert(user: KomgaUser): KomgaUser
  fun update(user: KomgaUser)

  fun delete(user: KomgaUser)
  fun deleteAll()

  fun existsByEmailIgnoreCase(email: String): Boolean
  fun findByEmailIgnoreCase(email: String): KomgaUser?
}
