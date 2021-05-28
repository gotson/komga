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

      // claim is unprotected
      .antMatchers("/api/v1/claim").permitAll()

      // all other endpoints are restricted to authenticated users
      .antMatchers(
        "/api/**",
        "/opds/**"
      ).hasRole(ROLE_USER)

      .and()
      .headers {
        it.cacheControl().disable() // headers are set in WebMvcConfiguration
      }

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
        "/favicon-16x16.png",
        "/favicon-32x32.png",
        "/mstile-144x144.png",
        "/apple-touch-icon.png",
        "/apple-touch-icon-180x180.png",
        "/",
        "/index.html"
      )
  }
}
