package org.gotson.komga.infrastructure.security

import mu.KotlinLogging
import org.gotson.komga.domain.model.ROLE_ADMIN
import org.gotson.komga.domain.model.ROLE_USER
import org.gotson.komga.infrastructure.configuration.KomgaProperties
import org.springframework.boot.actuate.autoconfigure.security.servlet.EndpointRequest
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.builders.WebSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter
import org.springframework.security.core.session.SessionRegistry
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest
import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService
import org.springframework.security.oauth2.core.OAuth2AuthenticationException
import org.springframework.security.oauth2.core.oidc.user.OidcUser
import org.springframework.security.oauth2.core.user.OAuth2User
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource
import org.springframework.security.web.authentication.rememberme.TokenBasedRememberMeServices

private val logger = KotlinLogging.logger {}

@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
class SecurityConfiguration(
  private val komgaProperties: KomgaProperties,
  private val komgaUserDetailsLifecycle: UserDetailsService,
  private val oauth2UserService: OAuth2UserService<OAuth2UserRequest, OAuth2User>,
  private val oidcUserService: OAuth2UserService<OidcUserRequest, OidcUser>,
  private val sessionCookieName: String,
  private val userAgentWebAuthenticationDetailsSource: WebAuthenticationDetailsSource,
  private val sessionRegistry: SessionRegistry,
  clientRegistrationRepository: InMemoryClientRegistrationRepository?,
) : WebSecurityConfigurerAdapter() {

  private val oauth2Enabled = clientRegistrationRepository != null

  override fun configure(http: HttpSecurity) {
    http
      .cors {}
      .csrf { it.disable() }
      .authorizeRequests {
        // restrict all actuator endpoints to ADMIN only
        it.requestMatchers(EndpointRequest.toAnyEndpoint()).hasRole(ROLE_ADMIN)

        // claim is unprotected
        it.mvcMatchers(
          "/api/v1/claim",
          "/api/v1/oauth2/providers",
          "/set-cookie",
        ).permitAll()

        // all other endpoints are restricted to authenticated users
        it.mvcMatchers(
          "/api/**",
          "/opds/**",
          "/sse/**",
        ).hasRole(ROLE_USER)
      }
      .headers {
        it.cacheControl().disable() // headers are set in WebMvcConfiguration
      }
      .httpBasic {
        it.authenticationDetailsSource(userAgentWebAuthenticationDetailsSource)
      }
      .logout {
        it.logoutUrl("/api/logout")
        it.deleteCookies(sessionCookieName)
        it.invalidateHttpSession(true)
      }
      .sessionManagement { session ->
        session.sessionConcurrency {
          it.sessionRegistry(sessionRegistry)
          it.maximumSessions(-1)
        }
      }

    if (oauth2Enabled) {
      http.oauth2Login { oauth2 ->
        oauth2.userInfoEndpoint {
          it.userService(oauth2UserService)
          it.oidcUserService(oidcUserService)
        }
        oauth2.authenticationDetailsSource(userAgentWebAuthenticationDetailsSource)
        oauth2.loginPage("/login")
          .defaultSuccessUrl("/?server_redirect=Y", true)
          .failureHandler { request, response, exception ->
            val errorMessage = when (exception) {
              is OAuth2AuthenticationException -> exception.error.errorCode
              else -> exception.message
            }
            val url = "/login?server_redirect=Y&error=$errorMessage"
            SimpleUrlAuthenticationFailureHandler(url).onAuthenticationFailure(request, response, exception)
          }
      }
    }

    if (!komgaProperties.rememberMe.key.isNullOrBlank()) {
      logger.info { "RememberMe is active, validity: ${komgaProperties.rememberMe.validity}" }

      http
        .rememberMe {
          it.rememberMeServices(
            TokenBasedRememberMeServices(komgaProperties.rememberMe.key, komgaUserDetailsLifecycle).apply {
              setTokenValiditySeconds(komgaProperties.rememberMe.validity.seconds.toInt())
              setAlwaysRemember(true)
              setAuthenticationDetailsSource(userAgentWebAuthenticationDetailsSource)
            },
          )
        }
    }
  }

  override fun configure(web: WebSecurity) {
    web.ignoring()
      .mvcMatchers(
        "/error**",
        "/css/**",
        "/img/**",
        "/js/**",
        "/favicon.ico",
        "/favicon-16x16.png",
        "/favicon-32x32.png",
        "/mstile-144x144.png",
        "/apple-touch-icon.png",
        "/apple-touch-icon-180x180.png",
        "/android-chrome-192x192.png",
        "/android-chrome-512x512.png",
        "/manifest.json",
        "/",
        "/index.html",
      )
  }
}
