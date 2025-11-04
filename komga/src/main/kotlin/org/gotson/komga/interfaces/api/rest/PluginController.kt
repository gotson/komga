package org.gotson.komga.interfaces.api.rest

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.tags.Tag
import org.gotson.komga.domain.model.Plugin
import org.gotson.komga.domain.service.ChapterCheckService
import org.gotson.komga.domain.service.PluginDownloader
import org.gotson.komga.domain.service.PluginLifecycle
import org.gotson.komga.infrastructure.openapi.OpenApiConfiguration
import org.gotson.komga.infrastructure.security.KomgaPrincipal
import org.gotson.komga.interfaces.api.rest.dto.ChapterCheckResultDto
import org.gotson.komga.interfaces.api.rest.dto.PluginDto
import org.gotson.komga.interfaces.api.rest.dto.PluginRepoDto
import org.gotson.komga.interfaces.api.rest.dto.PluginStatusDto
import org.gotson.komga.interfaces.api.rest.dto.PluginUpdateCheckDto
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile
import org.springframework.web.server.ResponseStatusException
import java.io.File
import java.nio.file.Files
import java.nio.file.StandardCopyOption

@RestController
@RequestMapping("api/v1/plugins", produces = [MediaType.APPLICATION_JSON_VALUE])
@Tag(name = "Plugins")
class PluginController(
  private val pluginLifecycle: PluginLifecycle,
  private val pluginDownloader: PluginDownloader,
  private val chapterCheckService: ChapterCheckService,
) {
  @GetMapping
  @PreAuthorize("hasRole('ADMIN')")
  @Operation(
    summary = "List all plugins",
    description = "List all installed plugins in the system",
  )
  fun getPlugins(
    @AuthenticationPrincipal principal: KomgaPrincipal,
  ): List<PluginDto> = pluginLifecycle.getLoadedPlugins().map { it.toDto() }

  @GetMapping("status")
  @PreAuthorize("hasRole('ADMIN')")
  @Operation(
    summary = "Get plugin system status",
    description = "Get information about the plugin system",
  )
  fun getPluginStatus(
    @AuthenticationPrincipal principal: KomgaPrincipal,
  ): PluginStatusDto {
    val plugins = pluginLifecycle.getLoadedPlugins()
    return PluginStatusDto(
      loaded = plugins.size,
      total = plugins.size,
      pluginDirectory = pluginLifecycle.pluginDirectory,
    )
  }

  @GetMapping("{pluginId}")
  @PreAuthorize("hasRole('ADMIN')")
  @Operation(summary = "Get details for a single plugin")
  fun getPluginById(
    @AuthenticationPrincipal principal: KomgaPrincipal,
    @PathVariable pluginId: String,
  ): PluginDto =
    pluginLifecycle.getLoadedPlugins()
      .find { it.id == pluginId }
      ?.toDto()
      ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Plugin not found")

  @PostMapping("reload")
  @PreAuthorize("hasRole('ADMIN')")
  @ResponseStatus(HttpStatus.ACCEPTED)
  @Operation(
    summary = "Reload all plugins",
    description = "Reload all plugins from the plugin directory",
  )
  fun reloadPlugins(
    @AuthenticationPrincipal principal: KomgaPrincipal,
  ): List<PluginDto> {
    pluginLifecycle.unloadAllPlugins()
    return pluginLifecycle.discoverAndLoadPlugins().map { it.toDto() }
  }

  @PostMapping("upload")
  @PreAuthorize("hasRole('ADMIN')")
  @Operation(
    summary = "Upload a plugin JAR",
    description = "Upload a plugin JAR file to the plugin directory",
  )
  fun uploadPlugin(
    @AuthenticationPrincipal principal: KomgaPrincipal,
    @RequestParam("file") file: MultipartFile,
  ): PluginDto {
    if (file.isEmpty) {
      throw ResponseStatusException(HttpStatus.BAD_REQUEST, "File is empty")
    }

    val originalFilename = file.originalFilename
    if (originalFilename == null || !originalFilename.endsWith(".jar")) {
      throw ResponseStatusException(HttpStatus.BAD_REQUEST, "File must be a JAR file")
    }

    try {
      // Save file to plugin directory
      val pluginDir = File(pluginLifecycle.pluginDirectory)
      pluginDir.mkdirs()

      val targetFile = File(pluginDir, originalFilename)
      file.inputStream.use { input ->
        Files.copy(input, targetFile.toPath(), StandardCopyOption.REPLACE_EXISTING)
      }

      // Load the plugin
      return pluginLifecycle.loadPlugin(targetFile).toDto()
    } catch (e: Exception) {
      throw ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to upload plugin: ${e.message}")
    }
  }

  @DeleteMapping("{pluginId}")
  @PreAuthorize("hasRole('ADMIN')")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @Operation(
    summary = "Unload a plugin",
    description = "Unload a plugin from the system",
  )
  fun unloadPlugin(
    @AuthenticationPrincipal principal: KomgaPrincipal,
    @PathVariable pluginId: String,
  ) {
    try {
      pluginLifecycle.unloadPlugin(pluginId)
    } catch (e: Exception) {
      throw ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to unload plugin: ${e.message}")
    }
  }

  @GetMapping("repositories")
  @PreAuthorize("hasRole('ADMIN')")
  @Operation(
    summary = "List available plugin repositories",
    description = "List built-in plugin repositories that can be downloaded from",
  )
  fun getPluginRepositories(
    @AuthenticationPrincipal principal: KomgaPrincipal,
  ): List<PluginRepoDto> =
    pluginDownloader.getBuiltInRepos().map { (id, repo) ->
      PluginRepoDto(
        id = id,
        name = repo.name,
        owner = repo.owner,
        repo = repo.repo,
        description = repo.description,
      )
    }

  @PostMapping("download/{repoId}")
  @PreAuthorize("hasRole('ADMIN')")
  @Operation(
    summary = "Download and install a plugin from a repository",
    description = "Download a plugin from a built-in repository (e.g., manga-py) and install it",
  )
  fun downloadPlugin(
    @AuthenticationPrincipal principal: KomgaPrincipal,
    @PathVariable repoId: String,
  ): PluginDto {
    try {
      // Download the plugin
      val downloadResult = pluginDownloader.downloadFromBuiltInRepo(repoId)

      if (!downloadResult.success) {
        throw ResponseStatusException(
          HttpStatus.INTERNAL_SERVER_ERROR,
          "Download failed: ${downloadResult.error}",
        )
      }

      // Load the downloaded plugin
      val pluginFile = File(downloadResult.filePath!!)
      val plugin = pluginLifecycle.loadPlugin(pluginFile)

      return plugin.toDto()
    } catch (e: ResponseStatusException) {
      throw e
    } catch (e: Exception) {
      throw ResponseStatusException(
        HttpStatus.INTERNAL_SERVER_ERROR,
        "Failed to download and install plugin: ${e.message}",
      )
    }
  }

  @PostMapping("download-url")
  @PreAuthorize("hasRole('ADMIN')")
  @Operation(
    summary = "Download and install a plugin from a URL",
    description = "Download a plugin from a direct URL and install it",
  )
  fun downloadPluginFromUrl(
    @AuthenticationPrincipal principal: KomgaPrincipal,
    @RequestParam url: String,
    @RequestParam(required = false) fileName: String?,
  ): PluginDto {
    try {
      val actualFileName = fileName ?: url.substringAfterLast("/")

      if (!actualFileName.endsWith(".jar")) {
        throw ResponseStatusException(HttpStatus.BAD_REQUEST, "URL must point to a JAR file")
      }

      // Download the plugin
      val downloadResult = pluginDownloader.downloadFromUrl(url, actualFileName)

      if (!downloadResult.success) {
        throw ResponseStatusException(
          HttpStatus.INTERNAL_SERVER_ERROR,
          "Download failed: ${downloadResult.error}",
        )
      }

      // Load the downloaded plugin
      val pluginFile = File(downloadResult.filePath!!)
      val plugin = pluginLifecycle.loadPlugin(pluginFile)

      return plugin.toDto()
    } catch (e: ResponseStatusException) {
      throw e
    } catch (e: Exception) {
      throw ResponseStatusException(
        HttpStatus.INTERNAL_SERVER_ERROR,
        "Failed to download and install plugin: ${e.message}",
      )
    }
  }

  @PostMapping("check-updates")
  @PreAuthorize("hasRole('ADMIN')")
  @Operation(
    summary = "Check for plugin updates",
    description = "Check all loaded plugins for available updates from their repositories",
  )
  fun checkForUpdates(
    @AuthenticationPrincipal principal: KomgaPrincipal,
  ): List<PluginUpdateCheckDto> {
    val updates = mutableListOf<PluginUpdateCheckDto>()

    pluginLifecycle.getLoadedPlugins().forEach { plugin ->
      // Try to determine the repo from the plugin
      val builtInRepos = pluginDownloader.getBuiltInRepos()
      val repo = builtInRepos.values.find { it.name == plugin.name }

      if (repo != null) {
        val updateCheck = pluginDownloader.checkForUpdate(
          owner = repo.owner,
          repo = repo.repo,
          currentVersion = plugin.version,
        )

        updates.add(
          PluginUpdateCheckDto(
            pluginId = plugin.id,
            pluginName = plugin.name,
            currentVersion = updateCheck.currentVersion,
            latestVersion = updateCheck.latestVersion,
            updateAvailable = updateCheck.updateAvailable,
            releaseUrl = updateCheck.releaseUrl,
            releaseNotes = updateCheck.releaseNotes,
            error = updateCheck.error,
          ),
        )
      }
    }

    return updates
  }

  @PostMapping("check-chapters/{seriesId}")
  @PreAuthorize("hasRole('USER')")
  @Operation(
    summary = "Check for new chapters",
    description = "Check if new chapters are available for a specific series",
  )
  fun checkNewChapters(
    @AuthenticationPrincipal principal: KomgaPrincipal,
    @PathVariable seriesId: String,
  ): List<ChapterCheckResultDto> {
    val results = chapterCheckService.checkNewChapters(seriesId)
    return results.map {
      ChapterCheckResultDto(
        seriesId = it.seriesId,
        seriesTitle = it.seriesTitle,
        hasNewChapters = it.hasNewChapters,
        latestChapterNumber = it.latestChapterNumber,
        latestChapterTitle = it.latestChapterTitle,
        latestChapterUrl = it.latestChapterUrl,
        newChapterCount = it.newChapterCount,
        lastChecked = it.lastChecked,
        source = it.source,
        message = it.message,
      )
    }
  }

  @PostMapping("check-all-chapters")
  @PreAuthorize("hasRole('ADMIN')")
  @Operation(
    summary = "Check for new chapters across all series",
    description = "Check all series for new chapters using available plugins",
  )
  fun checkAllChapters(
    @AuthenticationPrincipal principal: KomgaPrincipal,
  ): Map<String, List<ChapterCheckResultDto>> {
    val results = chapterCheckService.checkAllSeries()
    return results.mapValues { (_, checkResults) ->
      checkResults.map {
        ChapterCheckResultDto(
          seriesId = it.seriesId,
          seriesTitle = it.seriesTitle,
          hasNewChapters = it.hasNewChapters,
          latestChapterNumber = it.latestChapterNumber,
          latestChapterTitle = it.latestChapterTitle,
          latestChapterUrl = it.latestChapterUrl,
          newChapterCount = it.newChapterCount,
          lastChecked = it.lastChecked,
          source = it.source,
          message = it.message,
        )
      }
    }
  }
}

private fun Plugin.toDto() =
  PluginDto(
    id = id,
    name = name,
    version = version,
    description = description,
    author = author,
    enabled = enabled,
    type = type.name,
    capabilities = capabilities,
    jarPath = jarPath,
    loadedAt = loadedAt,
    error = error,
  )
