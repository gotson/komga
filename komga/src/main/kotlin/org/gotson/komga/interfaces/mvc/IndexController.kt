package org.gotson.komga.interfaces.mvc

import jakarta.servlet.ServletContext
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping

@Controller
class IndexController(
  servletContext: ServletContext,
) {
  private val baseUrl: String = "${servletContext.contextPath}/"

  @GetMapping("/")
  fun index(model: Model): String {
    model.addAttribute("baseUrl", baseUrl)
    return "index"
  }
}
