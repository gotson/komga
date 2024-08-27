package org.gotson.komga.domain.model

data class KomgaSyncToken(
  val version: Int = 1,
  val rawKoboSyncToken: String = "",
  /**
   * Only if a sync is currently ongoing, else null.
   */
  val ongoingSyncPointId: String? = null,
  /**
   * The last successful SyncPoint ID.
   */
  val lastSuccessfulSyncPointId: String? = null,
)
