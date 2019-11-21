package org.gotson.komga.infrastructure.web

import org.springframework.context.annotation.Configuration
import org.springframework.http.CacheControl
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry
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
}
