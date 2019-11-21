package org.gotson.komga.infrastructure.security

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
import org.springframework.security.core.session.SessionRegistryImpl
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.UrlBasedCorsConfigurationSource


@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
class SecurityConfiguration(
    private val komgaProperties: KomgaProperties
) : WebSecurityConfigurerAdapter() {

  override fun configure(http: HttpSecurity) {
    // @formatter:off

    http
        .addFilterAt(LoggingBasicAuthFilter(this.authenticationManager()), BasicAuthenticationFilter::class.java)
        .cors()
        .and()
          .csrf().disable()

        .authorizeRequests()
          // restrict all actuator endpoints to ADMIN only
          .requestMatchers(EndpointRequest.toAnyEndpoint()).hasRole("ADMIN")

          // restrict H2 console to ADMIN only
          .requestMatchers(PathRequest.toH2Console()).hasRole("ADMIN")

          // all other endpoints are restricted to authenticated users
          .antMatchers(
              "/api/**",
              "/opds/**"
          ).hasRole("USER")

        // authorize frames for H2 console
        .and()
          .headers().frameOptions().sameOrigin()

        .and()
          .httpBasic()

        .and()
          .sessionManagement()
          .maximumSessions(10)
          .sessionRegistry(sessionRegistry())

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
            "/index.html")
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

  @Bean
  fun getEncoder(): PasswordEncoder = BCryptPasswordEncoder()

  @Bean
  fun sessionRegistry(): SessionRegistry {
    return SessionRegistryImpl()
  }
}
