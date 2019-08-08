package org.gotson.komga.infrastructure.security

import org.springframework.boot.actuate.autoconfigure.security.servlet.EndpointRequest
import org.springframework.boot.autoconfigure.security.servlet.PathRequest
import org.springframework.context.annotation.Bean
import org.springframework.http.HttpMethod
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.builders.WebSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.UrlBasedCorsConfigurationSource

@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
class SecurityConfiguration : WebSecurityConfigurerAdapter() {
  override fun configure(http: HttpSecurity) {
    http
        .cors().and()
        .csrf().disable()
        .authorizeRequests()

        // unrestricted endpoints
        .requestMatchers(PathRequest.toStaticResources().atCommonLocations()).permitAll()

        // restrict all actuator endpoints to ADMIN only
        .requestMatchers(EndpointRequest.toAnyEndpoint()).hasRole("ADMIN")

        // restrict H2 console to ADMIN only
        .requestMatchers(PathRequest.toH2Console()).hasRole("ADMIN")

        // all other endpoints are restricted to authenticated users
        .antMatchers("/api/**").hasRole("USER")

        // authorize frames for H2 console
        .and().headers().frameOptions().sameOrigin()

        .and().httpBasic()
//        .and().sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
  }

  override fun configure(web: WebSecurity) {
    web.ignoring()
        .antMatchers(
            "/error**",
            "/",
            "/index.html")
  }

  override fun configure(auth: AuthenticationManagerBuilder) {
    auth.inMemoryAuthentication()
        .withUser("admin").password("{noop}admin").roles("ADMIN", "USER")
  }

  @Bean
  fun corsConfigurationSource(): UrlBasedCorsConfigurationSource =
      UrlBasedCorsConfigurationSource().apply {
        registerCorsConfiguration(
            "/**",
            CorsConfiguration().applyPermitDefaultValues().apply {
              addExposedHeader("Authorization")
              HttpMethod.values().forEach { addAllowedMethod(it) }
            }
        )
      }
}