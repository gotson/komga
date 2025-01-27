package org.gotson.komga.application.scheduler

import io.github.oshai.kotlinlogging.KotlinLogging
import org.gotson.komga.application.tasks.TaskEmitter
import org.gotson.komga.domain.model.Library
import org.gotson.komga.domain.model.Library.ScanInterval.DAILY
import org.gotson.komga.domain.model.Library.ScanInterval.DISABLED
import org.gotson.komga.domain.model.Library.ScanInterval.EVERY_12H
import org.gotson.komga.domain.model.Library.ScanInterval.EVERY_6H
import org.gotson.komga.domain.model.Library.ScanInterval.HOURLY
import org.gotson.komga.domain.model.Library.ScanInterval.WEEKLY
import org.springframework.scheduling.TaskScheduler
import org.springframework.scheduling.config.FixedRateTask
import org.springframework.scheduling.config.ScheduledTask
import org.springframework.scheduling.config.ScheduledTaskHolder
import org.springframework.scheduling.config.ScheduledTaskRegistrar
import org.springframework.stereotype.Service
import java.time.Duration
import java.util.concurrent.ConcurrentHashMap

private val logger = KotlinLogging.logger {}

@Service
class LibraryScanScheduler(
  taskScheduler: TaskScheduler,
  private val taskEmitter: TaskEmitter,
) : ScheduledTaskHolder {
  // map the libraryId to the scan scheduled task
  private val registry = ConcurrentHashMap<String, ScheduledTask>()

  private val registrar =
    ScheduledTaskRegistrar().apply {
      setTaskScheduler(taskScheduler)
    }

  fun scheduleScan(library: Library) {
    registry.remove(library.id)?.cancel(false)
    if (library.scanInterval != DISABLED) {
      registrar
        .scheduleFixedRateTask(
          FixedRateTask(
            {
              logger.info { "Periodic scan for library: ${library.name}" }
              taskEmitter.scanLibrary(library.id)
            },
            library.scanInterval.toDuration(),
            library.scanInterval.toDuration(),
          ),
        )?.let { registry[library.id] = it }
    }
  }

  // the '/actuator/scheduledtasks' endpoint will pick up any ScheduledTaskHolder and display its tasks
  override fun getScheduledTasks(): MutableSet<ScheduledTask> = registry.values.toMutableSet()
}

private fun Library.ScanInterval.toDuration(): Duration =
  when (this) {
    DISABLED -> throw IllegalArgumentException("Cannot convert DISABLED to Duration")
    HOURLY -> Duration.ofHours(1)
    EVERY_6H -> Duration.ofHours(6)
    EVERY_12H -> Duration.ofHours(12)
    DAILY -> Duration.ofDays(1)
    WEEKLY -> Duration.ofDays(7)
  }
