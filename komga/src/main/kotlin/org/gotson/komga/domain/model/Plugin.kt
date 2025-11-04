package org.gotson.komga.domain.model

import java.time.LocalDateTime

/**
 * Plugin entity representing an installed plugin
 */
data class Plugin(
  val id: String,
  val name: String,
  val version: String,
  val description: String,
  val author: String,
  val enabled: Boolean = false,
  val type: PluginType,
  val capabilities: Set<String> = emptySet(),
  val jarPath: String? = null,
  val loadedAt: LocalDateTime? = null,
  val error: String? = null,
)

enum class PluginType {
  BOOK_METADATA,
  SERIES_METADATA,
  SERIES_FROM_BOOK_METADATA,
}
