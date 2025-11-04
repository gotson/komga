package org.gotson.komga.infrastructure.plugin

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import mu.KotlinLogging
import java.io.File
import java.net.URLClassLoader
import java.util.jar.JarFile

private val logger = KotlinLogging.logger {}

/**
 * Plugin loader responsible for loading plugins from JAR files.
 */
class PluginLoader(
  private val pluginDirectory: String,
  private val pluginDataRoot: String,
) {
  private val objectMapper = jacksonObjectMapper()

  /**
   * Load a plugin from a JAR file.
   * @param jarFile The JAR file containing the plugin
   * @return Loaded plugin instance
   * @throws PluginLoadException if loading fails
   */
  fun loadPlugin(jarFile: File): LoadedPlugin {
    try {
      logger.info { "Loading plugin from ${jarFile.absolutePath}" }

      // Extract plugin descriptor
      val descriptor = extractPluginDescriptor(jarFile)
      logger.info { "Found plugin: ${descriptor.name} v${descriptor.version}" }

      // Create plugin classloader
      val classLoader = URLClassLoader(
        arrayOf(jarFile.toURI().toURL()),
        this.javaClass.classLoader,
      )

      // Load plugin entry point class
      val pluginClass = classLoader.loadClass(descriptor.entryPoint)
      val pluginInstance = pluginClass.getDeclaredConstructor().newInstance()

      if (pluginInstance !is KomgaPlugin) {
        throw PluginLoadException("Plugin entry point must implement KomgaPlugin interface")
      }

      // Create plugin context
      val context = PluginContextImpl(descriptor.id, pluginDataRoot)

      // Initialize plugin
      pluginInstance.onLoad(context)

      logger.info { "Successfully loaded plugin: ${descriptor.name}" }

      return LoadedPlugin(
        descriptor = descriptor,
        plugin = pluginInstance,
        context = context,
        classLoader = classLoader,
        jarPath = jarFile.absolutePath,
      )
    } catch (e: Exception) {
      logger.error(e) { "Failed to load plugin from ${jarFile.absolutePath}" }
      throw PluginLoadException("Failed to load plugin: ${e.message}", e)
    }
  }

  /**
   * Extract plugin descriptor from JAR file.
   */
  private fun extractPluginDescriptor(jarFile: File): PluginDescriptor {
    JarFile(jarFile).use { jar ->
      val entry = jar.getJarEntry("komga-plugin.json")
        ?: throw PluginLoadException("komga-plugin.json not found in JAR")

      jar.getInputStream(entry).use { inputStream ->
        return objectMapper.readValue(inputStream)
      }
    }
  }

  /**
   * Discover all plugin JAR files in the plugin directory.
   */
  fun discoverPlugins(): List<File> {
    val dir = File(pluginDirectory)
    if (!dir.exists() || !dir.isDirectory) {
      logger.warn { "Plugin directory does not exist: $pluginDirectory" }
      return emptyList()
    }

    return dir.listFiles { file -> file.extension == "jar" }?.toList() ?: emptyList()
  }

  /**
   * Unload a plugin.
   */
  fun unloadPlugin(loadedPlugin: LoadedPlugin) {
    try {
      loadedPlugin.plugin.onUnload()
      (loadedPlugin.classLoader as? URLClassLoader)?.close()
      logger.info { "Unloaded plugin: ${loadedPlugin.descriptor.name}" }
    } catch (e: Exception) {
      logger.error(e) { "Error unloading plugin: ${loadedPlugin.descriptor.name}" }
    }
  }
}

/**
 * Loaded plugin with all associated resources.
 */
data class LoadedPlugin(
  val descriptor: PluginDescriptor,
  val plugin: KomgaPlugin,
  val context: PluginContextImpl,
  val classLoader: ClassLoader,
  val jarPath: String,
)

class PluginLoadException(message: String, cause: Throwable? = null) : Exception(message, cause)
