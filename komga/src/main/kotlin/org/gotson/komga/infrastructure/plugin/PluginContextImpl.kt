package org.gotson.komga.infrastructure.plugin

import mu.KotlinLogging
import java.io.File
import java.nio.file.Paths

private val logger = KotlinLogging.logger {}

/**
 * Implementation of PluginContext that provides services to plugins.
 */
class PluginContextImpl(
  private val pluginId: String,
  private val pluginDataRoot: String,
) : PluginContext {
  private val configMap = mutableMapOf<String, String>()

  override fun getDataDirectory(): String {
    val dir = File(Paths.get(pluginDataRoot, pluginId).toString())
    if (!dir.exists()) {
      dir.mkdirs()
    }
    return dir.absolutePath
  }

  override fun getConfig(key: String): String? = configMap[key]

  override fun setConfig(key: String, value: String) {
    configMap[key] = value
  }

  override fun log(level: PluginContext.LogLevel, message: String) {
    when (level) {
      PluginContext.LogLevel.DEBUG -> logger.debug { "[$pluginId] $message" }
      PluginContext.LogLevel.INFO -> logger.info { "[$pluginId] $message" }
      PluginContext.LogLevel.WARN -> logger.warn { "[$pluginId] $message" }
      PluginContext.LogLevel.ERROR -> logger.error { "[$pluginId] $message" }
    }
  }

  fun loadPersistedConfig(config: Map<String, String>) {
    configMap.clear()
    configMap.putAll(config)
  }

  fun getPersistedConfig(): Map<String, String> = configMap.toMap()
}
