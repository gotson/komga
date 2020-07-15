package org.gotson.komga.infrastructure.jooq

import org.assertj.core.api.Assertions.within
import org.assertj.core.data.TemporalUnitOffset
import java.time.temporal.ChronoUnit

val offset: TemporalUnitOffset = within(3, ChronoUnit.SECONDS)
