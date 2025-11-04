package org.gotson.komga.infrastructure.plugin

/**
 * Plugin descriptor containing metadata about a plugin.
 * This should be provided by the plugin via a komga-plugin.json file or programmatically.
 */
data class PluginDescriptor(
  val id: String,
  val name: String,
  val version: String,
  val description: String,
  val author: String,
  val komgaVersion: String, // Minimum compatible Komga version
  val entryPoint: String, // Fully qualified class name of the plugin entry point
  val type: String, // book-metadata, series-metadata, series-from-book-metadata
  val capabilities: List<String> = emptyList(),
)
