package org.gotson.komga.infrastructure.security.oauth2

import org.gotson.komga.domain.persistence.KomgaUserRepository
import org.gotson.komga.infrastructure.security.KomgaPrincipal
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService
import org.springframework.security.oauth2.core.OAuth2AuthenticationException
import org.springframework.security.oauth2.core.oidc.user.OidcUser
import org.springframework.security.oauth2.core.user.OAuth2User

@Configuration
class KomgaOAuth2UserServiceConfiguration(
  private val userRepository: KomgaUserRepository,
) {

  @Bean
  fun oauth2UserService(): OAuth2UserService<OAuth2UserRequest, OAuth2User> {
    val defaultDelegate = DefaultOAuth2UserService()
    val githubDelegate = GithubOAuth2UserService()

    return OAuth2UserService { userRequest: OAuth2UserRequest ->
      val delegate = when (userRequest.clientRegistration.registrationId.lowercase()) {
        "github" -> githubDelegate
        else -> defaultDelegate
      }

      val oAuth2User = delegate.loadUser(userRequest)

      val email = oAuth2User.getAttribute<String>("email")
        ?: throw OAuth2AuthenticationException("ERR_1024")

      userRepository.findByEmailIgnoreCaseOrNull(email)?.let {
        KomgaPrincipal(it, oAuth2User = oAuth2User)
      } ?: throw OAuth2AuthenticationException("ERR_1025")
    }
  }

  @Bean
  fun oidcUserService(): OAuth2UserService<OidcUserRequest, OidcUser> {
    val delegate = OidcUserService()
    return OAuth2UserService { userRequest: OidcUserRequest ->
      val oidcUser = delegate.loadUser(userRequest)

      if (!oidcUser.emailVerified) throw OAuth2AuthenticationException("ERR_1026")

      userRepository.findByEmailIgnoreCaseOrNull(oidcUser.email)?.let {
        KomgaPrincipal(it, oidcUser)
      } ?: throw OAuth2AuthenticationException("ERR_1025")
    }
  }
}
