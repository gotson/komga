package org.gotson.komga.infrastructure.security

import mu.KotlinLogging
import org.gotson.komga.domain.model.ROLE_ADMIN
import org.gotson.komga.domain.model.ROLE_USER
import org.gotson.komga.infrastructure.configuration.KomgaProperties
import org.springframework.boot.actuate.autoconfigure.security.servlet.EndpointRequest
import org.springframework.boot.autoconfigure.security.servlet.PathRequest
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Profile
import org.springframework.http.HttpMethod
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.builders.WebSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter
import org.springframework.security.core.session.SessionRegistry
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.UrlBasedCorsConfigurationSource

private val logger = KotlinLogging.logger {}

@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
class SecurityConfiguration(
  private val komgaProperties: KomgaProperties,
  private val komgaUserDetailsLifecycle: UserDetailsService,
  private val sessionRegistry: SessionRegistry
) : WebSecurityConfigurerAdapter() {

  override fun configure(http: HttpSecurity) {
    // @formatter:off

    http
      .cors()
      .and()
      .csrf().disable()

      .authorizeRequests()
      // restrict all actuator endpoints to ADMIN only
      .requestMatchers(EndpointRequest.toAnyEndpoint()).hasRole(ROLE_ADMIN)

      // restrict H2 console to ADMIN only
      .requestMatchers(PathRequest.toH2Console()).hasRole(ROLE_ADMIN)

      // claim is unprotected
      .antMatchers("/api/v1/claim").permitAll()

      // all other endpoints are restricted to authenticated users
      .antMatchers(
        "/api/**",
        "/opds/**"
      ).hasRole(ROLE_USER)

      // authorize frames for H2 console
      .and()
      .headers().frameOptions().sameOrigin()

      .and()
      .httpBasic()

      .and()
      .logout()
      .logoutUrl("/api/v1/users/logout")
      .deleteCookies("JSESSIONID")

      .and()
      .sessionManagement()
      .maximumSessions(10)
      .sessionRegistry(sessionRegistry)

    if (!komgaProperties.rememberMe.key.isNullOrBlank()) {
      logger.info { "RememberMe is active, validity: ${komgaProperties.rememberMe.validity}s" }

      http
        .rememberMe()
        .key(komgaProperties.rememberMe.key)
        .tokenValiditySeconds(komgaProperties.rememberMe.validity)
        .alwaysRemember(true)
        .userDetailsService(komgaUserDetailsLifecycle)
    }
    // @formatter:on
  }

  override fun configure(web: WebSecurity) {
    web.ignoring()
      .antMatchers(
        "/error**",
        "/css/**",
        "/img/**",
        "/js/**",
        "/favicon.ico",
        "/",
        "/index.html"
      )
  }

  @Bean
  @Profile("dev")
  fun corsConfigurationSource(): UrlBasedCorsConfigurationSource =
    UrlBasedCorsConfigurationSource().apply {
      registerCorsConfiguration(
        "/**",
        CorsConfiguration().applyPermitDefaultValues().apply {
          allowedOrigins = listOf("http://localhost:8081")
          allowedMethods = HttpMethod.values().map { it.name }
          allowCredentials = true
        }
      )
    }
}
