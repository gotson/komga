package org.gotson.komga.domain.model

data class KomgaSyncToken(
  val version: Int = 1,
  val rawKoboSyncToken: String = "",
)
