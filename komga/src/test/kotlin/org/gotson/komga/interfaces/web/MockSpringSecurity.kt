package org.gotson.komga.interfaces.web

import io.mockk.every
import io.mockk.mockk
import org.gotson.komga.domain.model.KomgaUser
import org.gotson.komga.domain.model.Library
import org.gotson.komga.domain.model.UserRoles
import org.gotson.komga.infrastructure.security.KomgaPrincipal
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContext
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.test.context.support.TestExecutionEvent
import org.springframework.security.test.context.support.WithSecurityContext
import org.springframework.security.test.context.support.WithSecurityContextFactory
import java.net.URL

@Retention(AnnotationRetention.RUNTIME)
@WithSecurityContext(factory = WithMockCustomUserSecurityContextFactory::class, setupBefore = TestExecutionEvent.TEST_EXECUTION)
annotation class WithMockCustomUser(
    val email: String = "user@example.org",
    val roles: Array<UserRoles> = [],
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
            roles = customUser.roles.toMutableSet()
        ).also { user ->
          user.sharedAllLibraries = customUser.sharedAllLibraries
          user.sharedLibraries = customUser.sharedLibraries
              .map {
                val mock = mockk<Library>()
                every { mock.id } returns it
                every { mock.name } returns "Library #$it"
                every { mock.root } returns URL("file:/library/$it")
                mock
              }.toMutableSet()
        }
    )
    val auth = UsernamePasswordAuthenticationToken(principal, "", principal.authorities)
    context.authentication = auth
    return context
  }
}
