package org.gotson.komga.infrastructure.web

import org.springframework.context.annotation.Configuration
import org.springframework.http.CacheControl
import org.springframework.stereotype.Component
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.method.support.HandlerMethodArgumentResolver
import org.springframework.web.servlet.NoHandlerFoundException
import org.springframework.web.servlet.config.annotation.InterceptorRegistry
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer
import org.springframework.web.servlet.mvc.WebContentInterceptor
import java.util.concurrent.TimeUnit

@Configuration
class WebMvcConfiguration : WebMvcConfigurer {
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
        "/favicon.ico",
        "/favicon-16x16.png",
        "/favicon-32x32.png",
        "/mstile-144x144.png",
        "/apple-touch-icon.png",
        "/apple-touch-icon-180x180.png",
        "/android-chrome-192x192.png",
        "/android-chrome-512x512.png",
        "/manifest.json"
      )
      .addResourceLocations(
        "classpath:public/index.html",
        "classpath:public/favicon.ico",
        "classpath:public/favicon-16x16.png",
        "classpath:public/favicon-32x32.png",
        "classpath:public/mstile-144x144.png",
        "classpath:public/apple-touch-icon.png",
        "classpath:public/apple-touch-icon-180x180.png",
        "classpath:public/android-chrome-192x192.png",
        "classpath:public/android-chrome-512x512.png",
        "classpath:public/manifest.json"
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

  override fun addInterceptors(registry: InterceptorRegistry) {
    registry.addInterceptor(
      WebContentInterceptor().apply {
        addCacheMapping(
          cachePrivate,
          "/api/**", "/opds/**"
        )
      }
    )
  }

  override fun addArgumentResolvers(resolvers: MutableList<HandlerMethodArgumentResolver>) {
    resolvers.add(AuthorsHandlerMethodArgumentResolver())
    resolvers.add(DelimitedPairHandlerMethodArgumentResolver())
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
