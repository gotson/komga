package org.gotson.komga.domain.persistence

import org.gotson.komga.domain.model.AuthenticationActivity
import org.gotson.komga.domain.model.KomgaUser
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import java.time.LocalDateTime

interface AuthenticationActivityRepository {
  fun findAll(pageable: Pageable): Page<AuthenticationActivity>

  fun findAllByUser(
    user: KomgaUser,
    pageable: Pageable,
  ): Page<AuthenticationActivity>

  fun findMostRecentByUser(
    user: KomgaUser,
    apiKeyId: String?,
  ): AuthenticationActivity?

  fun insert(activity: AuthenticationActivity)

  fun deleteByUser(user: KomgaUser)

  fun deleteOlderThan(dateTime: LocalDateTime)
}
