package org.gotson.komga.domain.persistence

import org.gotson.komga.domain.model.KomgaUser
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Repository
interface KomgaUserRepository : CrudRepository<KomgaUser, Long> {
  fun existsByEmail(email: String): Boolean
  fun findByEmail(email: String): KomgaUser?
}
