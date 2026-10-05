package org.gotson.komga.interfaces.mvc

import jakarta.servlet.ServletContext
import org.gotson.komga.infrastructure.configuration.KomgaSettingsProvider
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping

@Controller
class IndexController(
  servletContext: ServletContext,
  val komgaSettingsProvider: KomgaSettingsProvider,
) {
  private val baseUrl: String = "${servletContext.contextPath}/"

  @GetMapping("/")
  fun index(
    model: Model,
  ): String {
    model.addAttribute("baseUrl", baseUrl)

    return if (komgaSettingsProvider.nextUiAsDefault) "index-next" else "index"
  }

  @GetMapping("/classic")
  fun indexClassic(model: Model): String {
    model.addAttribute("baseUrl", baseUrl)
    return "index"
  }

  @GetMapping("/next")
  fun indexNext(model: Model): String {
    model.addAttribute("baseUrl", baseUrl)
    return "index-next"
  }
}
