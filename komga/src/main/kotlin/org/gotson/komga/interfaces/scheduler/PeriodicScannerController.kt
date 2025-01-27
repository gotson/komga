package org.gotson.komga.interfaces.scheduler

import io.github.oshai.kotlinlogging.KotlinLogging
import org.gotson.komga.application.scheduler.LibraryScanScheduler
import org.gotson.komga.application.tasks.TaskEmitter
import org.gotson.komga.domain.persistence.LibraryRepository
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.annotation.Profile
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component

private val logger = KotlinLogging.logger {}

@Profile("!test")
@Component
class PeriodicScannerController(
  private val taskEmitter: TaskEmitter,
  private val libraryRepository: LibraryRepository,
  private val libraryScanScheduler: LibraryScanScheduler,
) {
  @EventListener(classes = [ApplicationReadyEvent::class])
  fun scanOnStartup() {
    libraryRepository
      .findAll()
      .filter { it.scanOnStartup }
      .forEach {
        logger.info { "Scan on startup for library: ${it.name}" }
        taskEmitter.scanLibrary(it.id)
      }
  }

  @EventListener(classes = [ApplicationReadyEvent::class])
  fun scheduleScans() {
    libraryRepository
      .findAll()
      .forEach { libraryScanScheduler.scheduleScan(it) }
  }
}
