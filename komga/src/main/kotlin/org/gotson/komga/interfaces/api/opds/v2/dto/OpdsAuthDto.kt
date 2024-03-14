package org.gotson.komga.interfaces.api.opds.v2.dto

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonValue
import org.gotson.komga.interfaces.api.dto.WPLinkDto

/**
 * https://drafts.opds.io/authentication-for-opds-1.0.html#23-syntax
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
data class AuthenticationDocumentDto(
  /**
   * A list of supported Authentication Flows as defined in section 3. Authentication Flows.
   */
  val authentication: List<AuthenticationFlowDto>,
  /**
   * Title of the Catalog being accessed.
   */
  val title: String,
  /**
   * Unique identifier for the Catalog provider and canonical location for the Authentication Document.
   */
  val id: String,
  /**
   * A description of the service being displayed to the user.
   */
  val description: String? = null,
  /**
   * An Authentication Document may also contain a links object.
   * This is used to associate the Authentication Document with resources that are not locally available.
   */
  val links: List<WPLinkDto> = emptyList(),
)

/**
 * In addition to the Authentication Document, this specification also defines multiple scenarios to handle how the client is authenticated.
 * Each Authentication Document contains at least one Authentication Object that describes how a client can leverage an Authentication Flow.
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
data class AuthenticationFlowDto(
  val type: AuthenticationType,
  val labels: LabelsDto? = null,
  val links: List<WPLinkDto> = emptyList(),
)

@JsonInclude(JsonInclude.Include.NON_NULL)
data class LabelsDto(
  val login: String? = null,
  val password: String? = null,
)

enum class AuthenticationType(
  @get:JsonValue val value: String,
) {
  BASIC("http://opds-spec.org/auth/basic"),
  OAUTH2_IMPLICIT("http://opds-spec.org/auth/oauth/implicit"),
  OAUTH2_PASSWORD("http://opds-spec.org/auth/oauth/password"),
}
