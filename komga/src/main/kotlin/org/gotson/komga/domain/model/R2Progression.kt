package org.gotson.komga.domain.model

import org.gotson.komga.language.toZonedDateTime
import java.time.ZonedDateTime

data class R2Progression(
  val modified: ZonedDateTime,
  val device: R2Device,
  val locator: R2Locator,
)

fun ReadProgress.toR2Progression() =
  R2Progression(
    modified = readDate.toZonedDateTime(),
    device = R2Device(deviceId, deviceName),
    locator = locator ?: R2Locator("", ""),
  )
