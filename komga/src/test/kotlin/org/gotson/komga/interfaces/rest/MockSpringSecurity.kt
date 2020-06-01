package org.gotson.komga.interfaces.rest

import org.gotson.komga.domain.model.KomgaUser
import org.gotson.komga.infrastructure.security.KomgaPrincipal
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
  val roles: Array<String> = [],
  val sharedAllLibraries: Boolean = true,
  val sharedLibraries: LongArray = []
)

class WithMockCustomUserSecurityContextFactory : WithSecurityContextFactory<WithMockCustomUser> {
  override fun createSecurityContext(customUser: WithMockCustomUser): SecurityContext {
    val context = SecurityContextHolder.createEmptyContext()

    val principal = KomgaPrincipal(
      KomgaUser(
        email = customUser.email,
        password = "",
        roleAdmin = customUser.roles.contains("ADMIN"),
        sharedAllLibraries = customUser.sharedAllLibraries,
        sharedLibrariesIds = customUser.sharedLibraries.toSet()
      )
    )
    val auth = UsernamePasswordAuthenticationToken(principal, "", principal.authorities)
    context.authentication = auth
    return context
  }
}
