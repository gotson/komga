package org.gotson.komga.domain.persistence

import org.gotson.komga.domain.model.ApiKey
import org.gotson.komga.domain.model.KomgaUser

interface KomgaUserRepository {
  fun count(): Long

  fun findByIdOrNull(id: String): KomgaUser?

  fun findByEmailIgnoreCaseOrNull(email: String): KomgaUser?

  fun findByApiKeyOrNull(apiKey: String): Pair<KomgaUser, ApiKey>?

  fun findAll(): Collection<KomgaUser>

  fun findApiKeyByUserId(userId: String): Collection<ApiKey>

  fun existsByEmailIgnoreCase(email: String): Boolean

  fun existsApiKeyByIdAndUserId(
    apiKeyId: String,
    userId: String,
  ): Boolean

  fun existsApiKeyByCommentAndUserId(
    comment: String,
    userId: String,
  ): Boolean

  fun insert(user: KomgaUser)

  fun insert(apiKey: ApiKey)

  fun update(user: KomgaUser)

  fun delete(userId: String)

  fun deleteAll()

  fun deleteApiKeyByIdAndUserId(
    apiKeyId: String,
    userId: String,
  )

  fun deleteApiKeyByUserId(userId: String)

  fun findAnnouncementIdsReadByUserId(userId: String): Set<String>

  fun saveAnnouncementIdsRead(
    user: KomgaUser,
    announcementIds: Set<String>,
  )
}
