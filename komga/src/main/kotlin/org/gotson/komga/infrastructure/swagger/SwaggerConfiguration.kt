package org.gotson.komga.infrastructure.swagger

import io.swagger.v3.oas.models.Components
import io.swagger.v3.oas.models.ExternalDocumentation
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Info
import io.swagger.v3.oas.models.info.License
import io.swagger.v3.oas.models.security.SecurityScheme
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class SwaggerConfiguration {
  @Bean
  fun openApi(): OpenAPI =
    OpenAPI()
      .info(
        Info()
          .title("Komga API")
          .version("v1.0")
          .description(
            """
            Komga offers 2 APIs: REST and OPDS.

            Both APIs are secured using HTTP Basic Authentication.
            """.trimIndent(),
          ).license(License().name("MIT").url("https://github.com/gotson/komga/blob/master/LICENSE")),
      ).externalDocs(
        ExternalDocumentation()
          .description("Komga documentation")
          .url("https://komga.org"),
      ).components(
        Components()
          .addSecuritySchemes(
            "basicAuth",
            SecurityScheme().type(SecurityScheme.Type.HTTP).scheme("basic"),
          ),
      )
}
