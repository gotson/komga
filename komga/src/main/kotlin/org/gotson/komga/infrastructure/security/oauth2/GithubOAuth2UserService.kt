package org.gotson.komga.infrastructure.security.oauth2

import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.RequestEntity
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest
import org.springframework.security.oauth2.core.user.DefaultOAuth2User
import org.springframework.security.oauth2.core.user.OAuth2User
import org.springframework.web.client.RestTemplate
import org.springframework.web.util.UriComponentsBuilder

private val logger = KotlinLogging.logger {}

class GithubOAuth2UserService : DefaultOAuth2UserService() {
  private val emailScopes = listOf("user:email", "user")

  private val parameterizedResponseType = object : ParameterizedTypeReference<List<Map<String, Any>>>() {}

  override fun loadUser(userRequest: OAuth2UserRequest?): OAuth2User {
    requireNotNull(userRequest) { "userRequest cannot be null" }

    var oAuth2User = super.loadUser(userRequest)

    if (userRequest.clientRegistration.scopes
        .intersect(emailScopes)
        .isNotEmpty() &&
      oAuth2User.getAttribute<String>("email") == null
    ) {
      try {
        val email =
          RestTemplate()
            .exchange(
              RequestEntity<Any>(
                HttpHeaders().apply { setBearerAuth(userRequest.accessToken.tokenValue) },
                HttpMethod.GET,
                UriComponentsBuilder.fromUriString("${userRequest.clientRegistration.providerDetails.userInfoEndpoint.uri}/emails").build().toUri(),
              ),
              parameterizedResponseType,
            ).body
            ?.let { emails ->
              emails
                .filter { it["verified"] == true }
                .filter { it["primary"] == true }
                .firstNotNullOfOrNull { it["email"].toString() }
            }
        oAuth2User =
          DefaultOAuth2User(
            oAuth2User.authorities,
            oAuth2User.attributes.toMutableMap().apply { put("email", email) },
            userRequest.clientRegistration.providerDetails.userInfoEndpoint.userNameAttributeName,
          )
      } catch (e: Exception) {
        logger.warn { "Could not retrieve emails" }
      }
    }

    return oAuth2User
  }
}
