package org.gotson.komga.interfaces.api.rest

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.gotson.komga.application.tasks.TasksRepository
import org.gotson.komga.infrastructure.openapi.OpenApiConfiguration
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping(produces = [MediaType.APPLICATION_JSON_VALUE])
@Tag(name = OpenApiConfiguration.TagNames.TASKS)
class TaskController(
  private val tasksRepository: TasksRepository,
) {
  @DeleteMapping("api/v1/tasks")
  @ResponseStatus(HttpStatus.OK)
  @PreAuthorize("hasRole('ADMIN')")
  @Operation(summary = "Clear task queue", description = "Cancel all tasks queued")
  fun emptyTaskQueue(): Int = tasksRepository.deleteAllWithoutOwner()
}
