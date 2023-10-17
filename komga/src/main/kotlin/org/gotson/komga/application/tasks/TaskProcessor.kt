package org.gotson.komga.application.tasks

import mu.KotlinLogging
import org.springframework.beans.factory.InitializingBean
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.boot.task.TaskExecutorBuilder
import org.springframework.context.event.EventListener
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor
import org.springframework.stereotype.Service

private val logger = KotlinLogging.logger {}

@Service
class TaskProcessor(
  private val tasksRepository: TasksRepository,
  private val taskHandler: TaskHandler,
  taskExecutorBuilder: TaskExecutorBuilder,
) : InitializingBean {
  val executor: ThreadPoolTaskExecutor =
    taskExecutorBuilder
      .threadNamePrefix("taskProcessor-")
      .corePoolSize(8)
      .build()
      .apply { initialize() }

  var processTasks = false

  override fun afterPropertiesSet() {
    val disowned = tasksRepository.disown()
    if (disowned > 0)
      logger.info { "Reset $disowned tasks that were not finished" }
    processTasks = true
  }

  @EventListener(TaskAddedEvent::class, ApplicationReadyEvent::class)
  fun processAvailableTask() {
    if (processTasks) {
      // fan out while threads are available
      while (tasksRepository.hasAvailable() && executor.activeCount < executor.corePoolSize)
        executor.execute { takeAndProcess() }
    } else {
      logger.debug { "Not processing tasks" }
    }
  }

  private fun takeAndProcess() {
    logger.debug { "Try to process first available task" }
    val task = tasksRepository.takeFirst()
    if (task != null) {
      logger.debug { "Found task to process: $task" }
      taskHandler.handleTask(task)
      logger.debug { "Task processed, remove it from the queue: $task" }
      tasksRepository.delete(task.uniqueId)
      processAvailableTask()
    } else {
      logger.debug { "No available task found" }
    }
  }
}
