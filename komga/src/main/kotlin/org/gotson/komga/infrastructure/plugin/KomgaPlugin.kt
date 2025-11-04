package org.gotson.komga.infrastructure.plugin

/**
 * Base interface for all Komga plugins.
 * All plugins must implement this interface.
 */
interface KomgaPlugin {
  /**
   * Called when the plugin is loaded and initialized.
   * @param context Plugin context providing access to Komga services
   */
  fun onLoad(context: PluginContext)

  /**
   * Called when the plugin is unloaded.
   */
  fun onUnload()

  /**
   * Get the plugin descriptor.
   */
  fun getDescriptor(): PluginDescriptor
}

/**
 * Plugin context provides access to Komga services and utilities.
 */
interface PluginContext {
  /**
   * Get the plugin's data directory for storing configuration and cache.
   */
  fun getDataDirectory(): String

  /**
   * Get a configuration value.
   */
  fun getConfig(key: String): String?

  /**
   * Set a configuration value.
   */
  fun setConfig(key: String, value: String)

  /**
   * Log a message.
   */
  fun log(level: LogLevel, message: String)

  enum class LogLevel {
    DEBUG, INFO, WARN, ERROR
  }
}
