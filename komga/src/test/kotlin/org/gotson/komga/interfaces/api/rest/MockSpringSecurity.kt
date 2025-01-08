package org.gotson.komga.interfaces.api.rest

import org.gotson.komga.domain.model.AgeRestriction
import org.gotson.komga.domain.model.AllowExclude
import org.gotson.komga.domain.model.ContentRestrictions
import org.gotson.komga.domain.model.KomgaUser
import org.gotson.komga.domain.model.UserRoles
import org.gotson.komga.infrastructure.security.KomgaPrincipal
import org.gotson.komga.infrastructure.security.apikey.ApiKeyAuthenticationToken
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContext
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.test.context.support.TestExecutionEvent
import org.springframework.security.test.context.support.WithSecurityContext
import org.springframework.security.test.context.support.WithSecurityContextFactory

@Retention(AnnotationRetention.RUNTIME)
@WithSecurityContext(factory = WithMockCustomUserSecurityContextFactory::class, setupBefore = TestExecutionEvent.TEST_EXECUTION)
annotation class WithMockCustomUser(
  val email: String = "user@example.org",
  val roles: Array<String> = ["PAGE_STREAMING", "FILE_DOWNLOAD", "KOBO_SYNC"],
  val sharedAllLibraries: Boolean = true,
  val sharedLibraries: Array<String> = [],
  val id: String = "0",
  val allowAgeUnder: Int = -1,
  val excludeAgeOver: Int = -1,
  val allowLabels: Array<String> = [],
  val excludeLabels: Array<String> = [],
  val apiKey: String = "",
)

class WithMockCustomUserSecurityContextFactory : WithSecurityContextFactory<WithMockCustomUser> {
  override fun createSecurityContext(customUser: WithMockCustomUser): SecurityContext {
    val context = SecurityContextHolder.createEmptyContext()

    val principal =
      KomgaPrincipal(
        KomgaUser(
          email = customUser.email,
          password = "",
          roles = UserRoles.valuesOf(customUser.roles.toList()),
          sharedLibrariesIds = customUser.sharedLibraries.toSet(),
          sharedAllLibraries = customUser.sharedAllLibraries,
          restrictions =
            ContentRestrictions(
              ageRestriction =
                if (customUser.allowAgeUnder >= 0)
                  AgeRestriction(customUser.allowAgeUnder, AllowExclude.ALLOW_ONLY)
                else if (customUser.excludeAgeOver >= 0)
                  AgeRestriction(customUser.excludeAgeOver, AllowExclude.EXCLUDE)
                else
                  null,
              labelsAllow = customUser.allowLabels.toSet(),
              labelsExclude = customUser.excludeLabels.toSet(),
            ),
          id = customUser.id,
        ),
      )
    val auth =
      if (customUser.apiKey.isNotEmpty())
        ApiKeyAuthenticationToken.authenticated(customUser.apiKey, customUser.apiKey, principal.authorities)
      else
        UsernamePasswordAuthenticationToken(principal, "", principal.authorities)
    context.authentication = auth
    return context
  }
}
