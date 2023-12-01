package org.gotson.komga.interfaces.api.rest.dto

import org.gotson.komga.domain.model.R2Locator

data class R2Positions(
  val total: Int,
  val positions: List<R2Locator>,
)
