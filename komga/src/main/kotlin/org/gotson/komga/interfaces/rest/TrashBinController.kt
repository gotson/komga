package org.gotson.komga.interfaces.rest

import org.gotson.komga.application.tasks.TaskReceiver
import org.gotson.komga.domain.model.ROLE_ADMIN
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("api/v1/trashbin", produces = [MediaType.APPLICATION_JSON_VALUE])
class TrashBinController(
  private val taskReceiver: TaskReceiver
) {

  @PostMapping("/empty")
  @PreAuthorize("hasRole('$ROLE_ADMIN')")
  @ResponseStatus(HttpStatus.ACCEPTED)
  fun emptyTrash() {
    taskReceiver.emptyTrash()
  }
}
