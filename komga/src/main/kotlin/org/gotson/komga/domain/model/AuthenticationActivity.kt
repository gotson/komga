package org.gotson.komga.domain.model

import java.time.LocalDateTime

data class AuthenticationActivity(
  val userId: String? = null,
  val email: String? = null,
  val apiKeyId: String? = null,
  val apiKeyComment: String? = null,
  val ip: String? = null,
  val userAgent: String? = null,
  val success: Boolean,
  val error: String? = null,
  val dateTime: LocalDateTime = LocalDateTime.now(),
  val source: String? = null,
)
