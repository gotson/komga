package org.gotson.komga.infrastructure.security

import io.github.oshai.kotlinlogging.KotlinLogging
import org.gotson.komga.domain.model.AuthenticationActivity
import org.gotson.komga.domain.persistence.AuthenticationActivityRepository
import org.gotson.komga.domain.persistence.KomgaUserRepository
import org.gotson.komga.infrastructure.security.apikey.ApiKeyAuthenticationToken
import org.springframework.context.event.EventListener
import org.springframework.security.authentication.AbstractAuthenticationToken
import org.springframework.security.authentication.RememberMeAuthenticationToken
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.authentication.event.AbstractAuthenticationFailureEvent
import org.springframework.security.authentication.event.AuthenticationFailureProviderNotFoundEvent
import org.springframework.security.authentication.event.AuthenticationSuccessEvent
import org.springframework.security.oauth2.client.authentication.OAuth2LoginAuthenticationToken
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
    val komgaPrincipal = event.authentication.principal as KomgaPrincipal
    val user = komgaPrincipal.user
    val apiKey = komgaPrincipal.apiKey
    val source =
      when (event.source) {
        is OAuth2LoginAuthenticationToken -> "OAuth2:${(event.source as OAuth2LoginAuthenticationToken).clientRegistration.clientName}"
        is ApiKeyAuthenticationToken -> "ApiKey"
        is UsernamePasswordAuthenticationToken -> "Password"
        is RememberMeAuthenticationToken -> "RememberMe"
        else -> null
      }
    val activity =
      AuthenticationActivity(
        userId = user.id,
        email = user.email,
        apiKeyId = apiKey?.id,
        apiKeyComment = apiKey?.comment,
        ip = event.getIp(),
        userAgent = event.getUserAgent(),
        success = true,
        source = source,
      )

    logger.debug { activity }
    authenticationActivityRepository.insert(activity)
  }

  @EventListener
  fun onFailure(event: AbstractAuthenticationFailureEvent) {
    // somehow we get 2 events with bad credentials, so discard this one
    if (event is AuthenticationFailureProviderNotFoundEvent) return
    val source =
      when (event.source) {
        is OAuth2LoginAuthenticationToken -> "OAuth2:${(event.source as OAuth2LoginAuthenticationToken).clientRegistration.clientName}"
        is ApiKeyAuthenticationToken -> "ApiKey"
        is UsernamePasswordAuthenticationToken -> "Password"
        is RememberMeAuthenticationToken -> "RememberMe"
        else -> null
      }
    val principal =
      event.authentication
        ?.principal
        ?.toString()
        .orEmpty()
    val activity =
      AuthenticationActivity(
        userId = userRepository.findByEmailIgnoreCaseOrNull(principal)?.id,
        email = if (event.source !is ApiKeyAuthenticationToken) principal else null,
        apiKeyComment = if (event.source is ApiKeyAuthenticationToken) principal else null,
        ip = event.getIp(),
        userAgent = event.getUserAgent(),
        success = false,
        source = source,
        error = event.exception.message,
      )

    logger.debug { activity }
    authenticationActivityRepository.insert(activity)
  }

  private fun EventObject.getIp(): String? =
    try {
      when (source) {
        is WebAuthenticationDetails -> (source as WebAuthenticationDetails).remoteAddress
        is AbstractAuthenticationToken -> ((source as AbstractAuthenticationToken).details as WebAuthenticationDetails).remoteAddress
        else -> null
      }
    } catch (e: Exception) {
      null
    }

  private fun EventObject.getUserAgent(): String? =
    try {
      when (source) {
        is UserAgentWebAuthenticationDetails -> (source as UserAgentWebAuthenticationDetails).userAgent
        is AbstractAuthenticationToken -> ((source as AbstractAuthenticationToken).details as UserAgentWebAuthenticationDetails).userAgent
        else -> null
      }
    } catch (e: Exception) {
      null
    }
}
