package org.gotson.komga.interfaces.api.rest

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.gotson.komga.infrastructure.swagger.OpenApiConfiguration
import org.springframework.http.MediaType
import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("api/v1/oauth2", produces = [MediaType.APPLICATION_JSON_VALUE])
@Tag(name = OpenApiConfiguration.TagNames.OAUTH2)
class OAuth2Controller(
  clientRegistrationRepository: InMemoryClientRegistrationRepository?,
) {
  val registrationIds =
    clientRegistrationRepository?.map {
      OAuth2ClientDto(it.clientName, it.registrationId)
    } ?: emptyList()

  @GetMapping("providers")
  @Operation(summary = "List registered OAuth2 providers")
  fun getProviders() = registrationIds
}

data class OAuth2ClientDto(
  val name: String,
  val registrationId: String,
)
