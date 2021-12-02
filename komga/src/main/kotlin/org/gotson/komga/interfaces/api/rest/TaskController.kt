package org.gotson.komga.interfaces.api.rest

import org.gotson.komga.domain.model.ROLE_ADMIN
import org.gotson.komga.infrastructure.jms.JmsQueueLifecycle
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping(produces = [MediaType.APPLICATION_JSON_VALUE])
class TaskController(
  private val jmsQueueLifecycle: JmsQueueLifecycle,
) {

  @DeleteMapping("api/v1/tasks")
  @ResponseStatus(HttpStatus.OK)
  @PreAuthorize("hasRole('$ROLE_ADMIN')")
  fun emptyTaskQueue(): Int =
    jmsQueueLifecycle.emptyTaskQueue()
}
