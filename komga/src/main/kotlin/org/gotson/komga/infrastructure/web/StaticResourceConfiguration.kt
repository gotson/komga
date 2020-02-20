package org.gotson.komga.infrastructure.web

import org.springframework.boot.web.server.ErrorPage
import org.springframework.boot.web.server.WebServerFactoryCustomizer
import org.springframework.boot.web.servlet.server.ConfigurableServletWebServerFactory
import org.springframework.context.annotation.Configuration
import org.springframework.http.CacheControl
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer


@Configuration
class StaticResourceConfiguration : WebMvcConfigurer {
  override fun addResourceHandlers(registry: ResourceHandlerRegistry) {
    registry//.setOrder(Ordered.HIGHEST_PRECEDENCE)
      .addResourceHandler(
        "/index.html",
        "/favicon.ico"
      )
      .addResourceLocations(
        "classpath:public/index.html",
        "classpath:public/favicon.ico"
      )
      .setCacheControl(CacheControl.noStore())
  }

  override fun addViewControllers(registry: ViewControllerRegistry) {
    registry.addViewController("/notFound")
      .setStatusCode(HttpStatus.OK)
      .setViewName("forward:/")
  }
}

@Component
class CustomContainer : WebServerFactoryCustomizer<ConfigurableServletWebServerFactory> {
  override fun customize(factory: ConfigurableServletWebServerFactory) {
    factory.addErrorPages(ErrorPage(HttpStatus.NOT_FOUND, "/notFound"))
  }
}
