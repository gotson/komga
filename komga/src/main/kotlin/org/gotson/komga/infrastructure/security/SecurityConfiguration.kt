package org.gotson.komga.infrastructure.security

import io.github.oshai.kotlinlogging.KotlinLogging
import org.gotson.komga.domain.model.ROLE_ADMIN
import org.gotson.komga.domain.model.ROLE_USER
import org.gotson.komga.infrastructure.configuration.KomgaProperties
import org.gotson.komga.infrastructure.configuration.KomgaSettingsProvider
import org.springframework.boot.actuate.autoconfigure.security.servlet.EndpointRequest
import org.springframework.boot.actuate.health.HealthEndpoint
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.core.session.SessionRegistry
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest
import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService
import org.springframework.security.oauth2.core.OAuth2AuthenticationException
import org.springframework.security.oauth2.core.oidc.user.OidcUser
import org.springframework.security.oauth2.core.user.OAuth2User
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource
import org.springframework.security.web.authentication.rememberme.TokenBasedRememberMeServices
import org.springframework.security.web.util.matcher.AntPathRequestMatcher

private val logger = KotlinLogging.logger {}

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
class SecurityConfiguration(
  private val komgaProperties: KomgaProperties,
  private val komgaSettingsProvider: KomgaSettingsProvider,
  private val komgaUserDetailsLifecycle: UserDetailsService,
  private val oauth2UserService: OAuth2UserService<OAuth2UserRequest, OAuth2User>,
  private val oidcUserService: OAuth2UserService<OidcUserRequest, OidcUser>,
  private val sessionCookieName: String,
  private val userAgentWebAuthenticationDetailsSource: WebAuthenticationDetailsSource,
  private val sessionRegistry: SessionRegistry,
  private val opdsAuthenticationEntryPoint: OpdsAuthenticationEntryPoint,
  clientRegistrationRepository: InMemoryClientRegistrationRepository?,
) {
  private val oauth2Enabled = clientRegistrationRepository != null

  @Bean
  fun filterChain(http: HttpSecurity): SecurityFilterChain {
    http
      .cors {}
      .csrf { it.disable() }
      .securityMatchers {
        // only apply security to those endpoints
        it.requestMatchers(
          "/api/**",
          "/opds/**",
          "/sse/**",
          "/oauth2/authorization/**",
          "/login/oauth2/code/**",
        )
        it.requestMatchers(EndpointRequest.toAnyEndpoint())
      }
      .authorizeHttpRequests {
        // allow unauthorized access to actuator health endpoint
        // this will only show limited details as `management.endpoint.health.show-details` is set to `when-authorized`
        it.requestMatchers(EndpointRequest.to(HealthEndpoint::class.java)).permitAll()
        // restrict all other actuator endpoints to ADMIN only
        it.requestMatchers(EndpointRequest.toAnyEndpoint()).hasRole(ROLE_ADMIN)

        it.requestMatchers(
          // to claim server before any account is created
          "/api/v1/claim",
          // used by webui
          "/api/v1/oauth2/providers",
          // epub resources - fonts are always requested anonymously, so we check for authorization within the controller method directly
          "api/v1/books/{bookId}/resource/**",
          // OPDS authentication document
          "/opds/v2/auth",
        ).permitAll()

        // all other endpoints are restricted to authenticated users
        it.requestMatchers(
          "/api/**",
          "/opds/**",
          "/sse/**",
        ).hasRole(ROLE_USER)
      }
      .headers { headersConfigurer ->
        headersConfigurer.cacheControl { it.disable() } // headers are set in WebMvcConfiguration
        headersConfigurer.frameOptions { it.sameOrigin() } // for epubreader iframes
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
        session.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
        session.sessionConcurrency {
          it.sessionRegistry(sessionRegistry)
          it.maximumSessions(-1)
        }
      }
      .exceptionHandling {
        it.defaultAuthenticationEntryPointFor(opdsAuthenticationEntryPoint, AntPathRequestMatcher("/opds/v2/**"))
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
            val errorMessage =
              when (exception) {
                is OAuth2AuthenticationException -> exception.error.errorCode
                else -> exception.message
              }
            val url = "/login?server_redirect=Y&error=$errorMessage"
            SimpleUrlAuthenticationFailureHandler(url).onAuthenticationFailure(request, response, exception)
          }
      }
    }

    http
      .rememberMe {
        it.rememberMeServices(
          TokenBasedRememberMeServices(komgaSettingsProvider.rememberMeKey, komgaUserDetailsLifecycle).apply {
            setTokenValiditySeconds(komgaSettingsProvider.rememberMeDuration.inWholeSeconds.toInt())
            setAuthenticationDetailsSource(userAgentWebAuthenticationDetailsSource)
          },
        )
      }

    return http.build()
  }
}
