package org.gotson.komga.interfaces.api.rest.dto

import com.fasterxml.jackson.annotation.JsonFormat
import org.gotson.komga.domain.model.AuthenticationActivity
import java.time.LocalDateTime

data class AuthenticationActivityDto(
  val userId: String?,
  val email: String?,
  val ip: String?,
  val userAgent: String?,
  val success: Boolean,
  val error: String?,
  @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
  val dateTime: LocalDateTime,
  val source: String?,
)

fun AuthenticationActivity.toDto() =
  AuthenticationActivityDto(
    userId = userId,
    email = email,
    ip = ip,
    userAgent = userAgent,
    success = success,
    error = error,
    dateTime = dateTime,
    source = source,
  )
