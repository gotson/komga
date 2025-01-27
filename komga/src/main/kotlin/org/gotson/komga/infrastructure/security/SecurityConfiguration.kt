package org.gotson.komga.infrastructure.security

import io.github.oshai.kotlinlogging.KotlinLogging
import jakarta.servlet.Filter
import org.gotson.komga.domain.model.UserRoles
import org.gotson.komga.infrastructure.configuration.KomgaSettingsProvider
import org.gotson.komga.infrastructure.security.apikey.ApiKeyAuthenticationFilter
import org.gotson.komga.infrastructure.security.apikey.ApiKeyAuthenticationProvider
import org.gotson.komga.infrastructure.security.apikey.HeaderApiKeyAuthenticationConverter
import org.gotson.komga.infrastructure.security.apikey.UriRegexApiKeyAuthenticationConverter
import org.springframework.boot.actuate.autoconfigure.security.servlet.EndpointRequest
import org.springframework.boot.actuate.health.HealthEndpoint
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.annotation.Order
import org.springframework.security.authentication.AuthenticationEventPublisher
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.ProviderManager
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.invoke
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.core.session.SessionRegistry
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest
import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService
import org.springframework.security.oauth2.core.OAuth2AuthenticationException
import org.springframework.security.oauth2.core.oidc.user.OidcUser
import org.springframework.security.oauth2.core.user.OAuth2User
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.AnonymousAuthenticationFilter
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource
import org.springframework.security.web.authentication.rememberme.TokenBasedRememberMeServices
import org.springframework.security.web.util.matcher.AntPathRequestMatcher

private val logger = KotlinLogging.logger {}

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
class SecurityConfiguration(
  private val komgaSettingsProvider: KomgaSettingsProvider,
  private val komgaUserDetailsService: UserDetailsService,
  private val apiKeyAuthenticationProvider: ApiKeyAuthenticationProvider,
  private val oauth2UserService: OAuth2UserService<OAuth2UserRequest, OAuth2User>,
  private val oidcUserService: OAuth2UserService<OidcUserRequest, OidcUser>,
  private val sessionCookieName: String,
  private val userAgentWebAuthenticationDetailsSource: WebAuthenticationDetailsSource,
  private val theSessionRegistry: SessionRegistry,
  private val opdsAuthenticationEntryPoint: OpdsAuthenticationEntryPoint,
  private val authenticationEventPublisher: AuthenticationEventPublisher,
  private val tokenEncoder: TokenEncoder,
  clientRegistrationRepository: InMemoryClientRegistrationRepository?,
) {
  private val oauth2Enabled = clientRegistrationRepository != null

  @Order(1)
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
      }.authorizeHttpRequests {
        // allow unauthorized access to actuator health endpoint
        // this will only show limited details as `management.endpoint.health.show-details` is set to `when-authorized`
        it.requestMatchers(EndpointRequest.to(HealthEndpoint::class.java)).permitAll()
        // restrict all other actuator endpoints to ADMIN only
        it.requestMatchers(EndpointRequest.toAnyEndpoint()).hasRole(UserRoles.ADMIN.name)

        it
          .requestMatchers(
            // to claim server before any account is created
            "/api/v1/claim",
            // used by webui
            "/api/v1/oauth2/providers",
            // epub resources - fonts are always requested anonymously, so we check for authorization within the controller method directly
            "/api/v1/books/{bookId}/resource/**",
            // dynamic fonts
            "/api/v1/fonts/resource/**",
            // OPDS authentication document
            "/opds/v2/auth",
            // KOReader user creation
            "/koreader/users/create",
          ).permitAll()

        // all other endpoints are restricted to authenticated users
        it
          .requestMatchers(
            "/api/**",
            "/opds/**",
            "/sse/**",
          ).authenticated()
      }.headers { headersConfigurer ->
        headersConfigurer.cacheControl { it.disable() } // headers are set in WebMvcConfiguration
        headersConfigurer.frameOptions { it.sameOrigin() } // for epubreader iframes
      }.userDetailsService(komgaUserDetailsService)
      .httpBasic {
        it.authenticationDetailsSource(userAgentWebAuthenticationDetailsSource)
      }.logout {
        it.logoutUrl("/api/logout")
        it.deleteCookies(sessionCookieName)
        it.invalidateHttpSession(true)
      }.sessionManagement { session ->
        session.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
        session.sessionConcurrency {
          it.sessionRegistry(theSessionRegistry)
          it.maximumSessions(-1)
        }
      }.exceptionHandling {
        it.defaultAuthenticationEntryPointFor(opdsAuthenticationEntryPoint, AntPathRequestMatcher("/opds/v2/**"))
      }

    if (oauth2Enabled) {
      http.oauth2Login { oauth2 ->
        oauth2.userInfoEndpoint {
          it.userService(oauth2UserService)
          it.oidcUserService(oidcUserService)
        }
        oauth2.authenticationDetailsSource(userAgentWebAuthenticationDetailsSource)
        oauth2
          .loginPage("/login")
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
        oauth2.redirectionEndpoint {
        }
      }
    }

    http
      .rememberMe {
        it.rememberMeServices(
          TokenBasedRememberMeServices(komgaSettingsProvider.rememberMeKey, komgaUserDetailsService).apply {
            setTokenValiditySeconds(komgaSettingsProvider.rememberMeDuration.inWholeSeconds.toInt())
            setAuthenticationDetailsSource(userAgentWebAuthenticationDetailsSource)
          },
        )
      }

    return http.build()
  }

  @Bean
  fun koboFilterChain(
    http: HttpSecurity,
    encoder: PasswordEncoder,
  ): SecurityFilterChain {
    http {
      cors {}

      csrf { disable() }
      formLogin { disable() }
      httpBasic { disable() }
      logout { disable() }

      securityMatcher("/kobo/**")
      authorizeHttpRequests {
        authorize(anyRequest, hasRole(UserRoles.KOBO_SYNC.name))
      }

      headers {
        cacheControl { disable() }
      }

      // somehow the Kobo gets a Json issue when receiving the session ID in a cookie header
      // this happens when requesting /v1/user/profile
      // Kobo error: packetdump.warning) Invalid JSON script: QVariant(Invalid) "illegal value"
//      sessionManagement {
//        sessionCreationPolicy = SessionCreationPolicy.IF_REQUIRED
//        sessionConcurrency {
//          sessionRegistry = theSessionRegistry
//          maximumSessions = -1
//        }
//      }

      addFilterBefore<AnonymousAuthenticationFilter>(koboAuthenticationFilter())
    }

    return http.build()
  }

  @Bean
  fun kosyncFilterChain(
    http: HttpSecurity,
    encoder: PasswordEncoder,
  ): SecurityFilterChain {
    http {
      cors {}

      csrf { disable() }
      formLogin { disable() }
      httpBasic { disable() }
      logout { disable() }

      securityMatcher("/koreader/**")
      authorizeHttpRequests {
        authorize(anyRequest, hasRole(UserRoles.KOREADER_SYNC.name))
      }

      headers {
        cacheControl { disable() }
      }

      sessionManagement {
        sessionCreationPolicy = SessionCreationPolicy.IF_REQUIRED
        sessionConcurrency {
          sessionRegistry = theSessionRegistry
          maximumSessions = -1
        }
      }

      addFilterBefore<AnonymousAuthenticationFilter>(kosyncAuthenticationFilter())
    }

    return http.build()
  }

  fun koboAuthenticationFilter(): Filter =
    ApiKeyAuthenticationFilter(
      apiKeyAuthenticationProvider(),
      UriRegexApiKeyAuthenticationConverter(Regex("""\/kobo\/([\w-]+)"""), tokenEncoder, userAgentWebAuthenticationDetailsSource),
    )

  fun kosyncAuthenticationFilter(): Filter =
    ApiKeyAuthenticationFilter(
      apiKeyAuthenticationProvider(),
      HeaderApiKeyAuthenticationConverter("X-Auth-User", tokenEncoder, userAgentWebAuthenticationDetailsSource),
    )

  fun apiKeyAuthenticationProvider(): AuthenticationManager =
    ProviderManager(apiKeyAuthenticationProvider).apply {
      setAuthenticationEventPublisher(authenticationEventPublisher)
    }
}
