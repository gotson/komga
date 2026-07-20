package org.gotson.komga.domain.model

import org.gotson.komga.language.toUTCZoned
import java.time.ZonedDateTime

data class R2Progression(
  val modified: ZonedDateTime,
  val device: R2Device,
  val locator: R2Locator,
)

fun ReadProgress.toR2Progression() =
  R2Progression(
    modified = readDate.toUTCZoned(),
    device = R2Device(deviceId, deviceName),
    locator = locator ?: R2Locator("", ""),
  )
