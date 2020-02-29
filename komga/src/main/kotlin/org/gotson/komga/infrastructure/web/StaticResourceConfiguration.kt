package org.gotson.komga.infrastructure.web

import org.springframework.context.annotation.Configuration
import org.springframework.http.CacheControl
import org.springframework.stereotype.Component
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.servlet.NoHandlerFoundException
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer
import java.util.concurrent.TimeUnit


@Configuration
class StaticResourceConfiguration : WebMvcConfigurer {
  override fun addResourceHandlers(registry: ResourceHandlerRegistry) {
    if (!registry.hasMappingForPattern("/webjars/**")) {
      registry.addResourceHandler("/webjars/**")
        .addResourceLocations("classpath:/META-INF/resources/webjars/")
    }

    if (!registry.hasMappingForPattern("/swagger-ui.html**")) {
      registry
        .addResourceHandler("/swagger-ui.html**")
        .addResourceLocations("classpath:/META-INF/resources/swagger-ui.html")
    }

    registry
      .addResourceHandler(
        "/index.html",
        "/favicon.ico"
      )
      .addResourceLocations(
        "classpath:public/index.html",
        "classpath:public/favicon.ico"
      )
      .setCacheControl(CacheControl.noStore())

    registry
      .addResourceHandler(
        "/css/**",
        "/fonts/**",
        "/img/**",
        "/js/**"
      )
      .addResourceLocations(
        "classpath:public/css/",
        "classpath:public/fonts/",
        "classpath:public/img/",
        "classpath:public/js/"
      )
      .setCacheControl(CacheControl.maxAge(365, TimeUnit.DAYS).cachePublic())
  }
}

@Component
@ControllerAdvice
class Customizer {
  @ExceptionHandler(NoHandlerFoundException::class)
  fun notFound(): String {
    return "forward:/"
  }
}
