package org.gotson.komga.domain.service

import mu.KotlinLogging
import org.gotson.komga.domain.model.BookMetadataPatch
import org.gotson.komga.domain.model.BookMetadataPatchCapability
import org.gotson.komga.domain.model.BookWithMedia
import org.gotson.komga.domain.model.Library
import org.gotson.komga.domain.model.MetadataPatchTarget
import org.gotson.komga.domain.model.Plugin
import org.gotson.komga.domain.model.PluginType
import org.gotson.komga.domain.model.Series
import org.gotson.komga.domain.model.SeriesMetadataPatch
import org.gotson.komga.infrastructure.metadata.BookMetadataProvider
import org.gotson.komga.infrastructure.metadata.SeriesMetadataFromBookProvider
import org.gotson.komga.infrastructure.metadata.SeriesMetadataProvider
import org.gotson.komga.infrastructure.plugin.LoadedPlugin
import org.gotson.komga.infrastructure.plugin.PluginBookMetadataProvider
import org.gotson.komga.infrastructure.plugin.PluginLoader
import org.gotson.komga.infrastructure.plugin.PluginSeriesMetadataFromBookProvider
import org.gotson.komga.infrastructure.plugin.PluginSeriesMetadataProvider
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.io.File
import java.time.LocalDateTime
import javax.annotation.PostConstruct
import javax.annotation.PreDestroy

private val logger = KotlinLogging.logger {}

/**
 * Service responsible for plugin lifecycle management.
 * Handles loading, unloading, enabling, and disabling plugins.
 * Also acts as a delegating provider for all plugin-based metadata providers.
 */
@Service
class PluginLifecycle(
  @Value("\${komga.plugins.directory:#{systemProperties['user.home']}/.komga/plugins}")
  val pluginDirectory: String,
  @Value("\${komga.plugins.data-directory:#{systemProperties['user.home']}/.komga/plugin-data}")
  private val pluginDataDirectory: String,
) : BookMetadataProvider, SeriesMetadataProvider, SeriesMetadataFromBookProvider {
  private val pluginLoader = PluginLoader(pluginDirectory, pluginDataDirectory)
  private val loadedPlugins = mutableMapOf<String, LoadedPlugin>()

  // Delegating provider implementations
  override val capabilities: Set<BookMetadataPatchCapability>
    get() = loadedPlugins.values
      .mapNotNull { it.plugin as? PluginBookMetadataProvider }
      .flatMap { it.getCapabilities() }
      .toSet()

  override val supportsAppendVolume: Boolean
    get() = loadedPlugins.values
      .mapNotNull { it.plugin as? PluginSeriesMetadataFromBookProvider }
      .any { it.supportsAppendVolume() }

  override fun shouldLibraryHandlePatch(library: Library, target: MetadataPatchTarget): Boolean {
    // Return true if any plugin is enabled for this library
    return loadedPlugins.values.any { loadedPlugin ->
      library.pluginConfigurations[loadedPlugin.descriptor.id]?.enabled == true
    }
  }

  override fun getBookMetadataFromBook(book: BookWithMedia): BookMetadataPatch? {
    // Delegate to all loaded book metadata plugins
    loadedPlugins.values.forEach { loadedPlugin ->
      val plugin = loadedPlugin.plugin
      if (plugin is PluginBookMetadataProvider) {
        try {
          val patch = plugin.getBookMetadata(book)
          if (patch != null) {
            logger.debug { "Plugin ${loadedPlugin.descriptor.name} provided book metadata" }
            return patch
          }
        } catch (e: Exception) {
          logger.error(e) { "Plugin ${loadedPlugin.descriptor.name} failed to extract book metadata" }
        }
      }
    }
    return null
  }

  override fun getSeriesMetadata(series: Series): SeriesMetadataPatch? {
    // Delegate to all loaded series metadata plugins
    loadedPlugins.values.forEach { loadedPlugin ->
      val plugin = loadedPlugin.plugin
      if (plugin is PluginSeriesMetadataProvider) {
        try {
          val patch = plugin.getSeriesMetadata(series)
          if (patch != null) {
            logger.debug { "Plugin ${loadedPlugin.descriptor.name} provided series metadata" }
            return patch
          }
        } catch (e: Exception) {
          logger.error(e) { "Plugin ${loadedPlugin.descriptor.name} failed to extract series metadata" }
        }
      }
    }
    return null
  }

  override fun getSeriesMetadataFromBook(book: BookWithMedia, appendVolumeToTitle: Boolean): SeriesMetadataPatch? {
    // Delegate to all loaded series-from-book metadata plugins
    loadedPlugins.values.forEach { loadedPlugin ->
      val plugin = loadedPlugin.plugin
      if (plugin is PluginSeriesMetadataFromBookProvider) {
        try {
          val patch = plugin.getSeriesMetadataFromBook(book, appendVolumeToTitle)
          if (patch != null) {
            logger.debug { "Plugin ${loadedPlugin.descriptor.name} provided series metadata from book" }
            return patch
          }
        } catch (e: Exception) {
          logger.error(e) { "Plugin ${loadedPlugin.descriptor.name} failed to extract series metadata from book" }
        }
      }
    }
    return null
  }

  @PostConstruct
  fun initialize() {
    logger.info { "Initializing plugin system" }
    logger.info { "Plugin directory: $pluginDirectory" }
    logger.info { "Plugin data directory: $pluginDataDirectory" }

    // Create directories if they don't exist
    File(pluginDirectory).mkdirs()
    File(pluginDataDirectory).mkdirs()

    // Auto-load plugins on startup
    discoverAndLoadPlugins()
  }

  @PreDestroy
  fun shutdown() {
    logger.info { "Shutting down plugin system" }
    unloadAllPlugins()
  }

  /**
   * Discover and load all plugins from the plugin directory.
   */
  fun discoverAndLoadPlugins(): List<Plugin> {
    val pluginFiles = pluginLoader.discoverPlugins()
    logger.info { "Discovered ${pluginFiles.size} plugin JAR files" }

    val plugins = mutableListOf<Plugin>()
    for (jarFile in pluginFiles) {
      try {
        val plugin = loadPlugin(jarFile)
        plugins.add(plugin)
      } catch (e: Exception) {
        logger.error(e) { "Failed to load plugin from ${jarFile.absolutePath}" }
        plugins.add(
          Plugin(
            id = jarFile.nameWithoutExtension,
            name = jarFile.nameWithoutExtension,
            version = "unknown",
            description = "Failed to load",
            author = "unknown",
            enabled = false,
            type = PluginType.BOOK_METADATA,
            jarPath = jarFile.absolutePath,
            error = e.message,
          ),
        )
      }
    }
    return plugins
  }

  /**
   * Load a plugin from a JAR file.
   */
  fun loadPlugin(jarFile: File): Plugin {
    try {
      val loadedPlugin = pluginLoader.loadPlugin(jarFile)
      val descriptor = loadedPlugin.descriptor

      // Register plugin
      loadedPlugins[descriptor.id] = loadedPlugin

      logger.info { "Plugin ${descriptor.name} loaded and registered" }

      return Plugin(
        id = descriptor.id,
        name = descriptor.name,
        version = descriptor.version,
        description = descriptor.description,
        author = descriptor.author,
        enabled = true,
        type = mapPluginType(descriptor.type),
        capabilities = descriptor.capabilities.toSet(),
        jarPath = jarFile.absolutePath,
        loadedAt = LocalDateTime.now(),
      )
    } catch (e: Exception) {
      logger.error(e) { "Failed to load plugin from ${jarFile.absolutePath}" }
      throw e
    }
  }

  /**
   * Unload a plugin by ID.
   */
  fun unloadPlugin(pluginId: String) {
    val loadedPlugin = loadedPlugins.remove(pluginId)
    if (loadedPlugin != null) {
      pluginLoader.unloadPlugin(loadedPlugin)
      logger.info { "Plugin $pluginId unloaded" }
    }
  }

  /**
   * Unload all plugins.
   */
  fun unloadAllPlugins() {
    loadedPlugins.keys.toList().forEach { unloadPlugin(it) }
  }

  /**
   * Get all loaded plugins.
   */
  fun getLoadedPlugins(): List<Plugin> =
    loadedPlugins.values.map { loadedPlugin ->
      val descriptor = loadedPlugin.descriptor
      Plugin(
        id = descriptor.id,
        name = descriptor.name,
        version = descriptor.version,
        description = descriptor.description,
        author = descriptor.author,
        enabled = true,
        type = mapPluginType(descriptor.type),
        capabilities = descriptor.capabilities.toSet(),
        jarPath = loadedPlugin.jarPath,
        loadedAt = LocalDateTime.now(),
      )
    }

  private fun mapPluginType(type: String): PluginType =
    when (type.lowercase()) {
      "book-metadata" -> PluginType.BOOK_METADATA
      "series-metadata" -> PluginType.SERIES_METADATA
      "series-from-book-metadata" -> PluginType.SERIES_FROM_BOOK_METADATA
      else -> PluginType.BOOK_METADATA
    }
}
