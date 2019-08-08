package org.gotson.komga.infrastructure.swagger

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.core.annotation.AuthenticationPrincipal
import springfox.documentation.spi.DocumentationType
import springfox.documentation.spring.web.plugins.Docket
import springfox.documentation.swagger2.annotations.EnableSwagger2

@Configuration
@EnableSwagger2
class SwaggerConfiguration {

  @Bean
  fun getDocket(): Docket {
    return Docket(DocumentationType.SWAGGER_2)
        .ignoredParameterTypes(AuthenticationPrincipal::class.java)
  }
}