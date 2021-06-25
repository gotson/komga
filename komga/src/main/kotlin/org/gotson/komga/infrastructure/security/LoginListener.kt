package org.gotson.komga.infrastructure.security

import mu.KotlinLogging
import org.gotson.komga.domain.model.AuthenticationActivity
import org.gotson.komga.domain.persistence.AuthenticationActivityRepository
import org.gotson.komga.domain.persistence.KomgaUserRepository
import org.springframework.context.event.EventListener
import org.springframework.security.authentication.AbstractAuthenticationToken
import org.springframework.security.authentication.event.AbstractAuthenticationFailureEvent
import org.springframework.security.authentication.event.AuthenticationSuccessEvent
import org.springframework.security.web.authentication.WebAuthenticationDetails
import org.springframework.stereotype.Component
import java.util.EventObject

private val logger = KotlinLogging.logger {}

@Component
class LoginListener(
  private val authenticationActivityRepository: AuthenticationActivityRepository,
  private val userRepository: KomgaUserRepository,
) {

  @EventListener
  fun onSuccess(event: AuthenticationSuccessEvent) {
    val user = (event.authentication.principal as KomgaPrincipal).user
    val activity = AuthenticationActivity(
      userId = user.id,
      email = user.email,
      ip = event.getIp(),
      userAgent = event.getUserAgent(),
      success = true,
    )

    logger.info { activity }
    authenticationActivityRepository.insert(activity)
  }

  @EventListener
  fun onFailure(event: AbstractAuthenticationFailureEvent) {
    val user = event.authentication.principal.toString()
    val activity = AuthenticationActivity(
      userId = userRepository.findByEmailIgnoreCaseOrNull(user)?.id,
      email = user,
      ip = event.getIp(),
      userAgent = event.getUserAgent(),
      success = false,
      error = event.exception.message,
    )

    logger.info { activity }
    authenticationActivityRepository.insert(activity)
  }

  private fun EventObject.getIp(): String? =
    when (source) {
      is WebAuthenticationDetails -> (source as WebAuthenticationDetails).remoteAddress
      is AbstractAuthenticationToken -> ((source as AbstractAuthenticationToken).details as WebAuthenticationDetails).remoteAddress
      else -> null
    }

  private fun EventObject.getUserAgent(): String? =
    when (source) {
      is UserAgentWebAuthenticationDetails -> (source as UserAgentWebAuthenticationDetails).userAgent
      is AbstractAuthenticationToken -> ((source as AbstractAuthenticationToken).details as UserAgentWebAuthenticationDetails).userAgent
      else -> null
    }
}
