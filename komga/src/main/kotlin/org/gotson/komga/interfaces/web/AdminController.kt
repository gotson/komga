package org.gotson.komga.interfaces.web

import org.gotson.komga.domain.service.LibraryManager
import org.springframework.http.HttpStatus
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("api/v1/admin")
@PreAuthorize("hasRole('ROLE_ADMIN')")
class AdminController(
    private val libraryManager: LibraryManager
) {

  @PostMapping("rpc/thumbnails/regenerateall")
  @ResponseStatus(HttpStatus.ACCEPTED)
  fun regenerateAllThumbnails() {
    libraryManager.regenerateAllThumbnails()
  }
}