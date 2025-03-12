package org.gotson.komga.interfaces.api.rest

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.gotson.komga.infrastructure.configuration.KomgaSettingsProvider
import org.gotson.komga.infrastructure.kobo.KepubConverter
import org.gotson.komga.infrastructure.openapi.OpenApiConfiguration
import org.gotson.komga.infrastructure.web.WebServerEffectiveSettings
import org.gotson.komga.interfaces.api.rest.dto.SettingMultiSource
import org.gotson.komga.interfaces.api.rest.dto.SettingsDto
import org.gotson.komga.interfaces.api.rest.dto.SettingsUpdateDto
import org.gotson.komga.interfaces.api.rest.dto.toDomain
import org.gotson.komga.interfaces.api.rest.dto.toDto
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import kotlin.time.Duration.Companion.days

@RestController
@RequestMapping(value = ["api/v1/settings"], produces = [MediaType.APPLICATION_JSON_VALUE])
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = OpenApiConfiguration.TagNames.SERVER_SETTINGS)
class SettingsController(
  private val komgaSettingsProvider: KomgaSettingsProvider,
  @Value("\${server.port:#{null}}") private val configServerPort: Int?,
  @Value("\${server.servlet.context-path:#{null}}") private val configServerContextPath: String?,
  private val serverSettings: WebServerEffectiveSettings,
  private val kepubConverter: KepubConverter,
) {
  @GetMapping
  @Operation(summary = "Retrieve server settings")
  fun getServerSettings(): SettingsDto =
    SettingsDto(
      komgaSettingsProvider.deleteEmptyCollections,
      komgaSettingsProvider.deleteEmptyReadLists,
      komgaSettingsProvider.rememberMeDuration.inWholeDays,
      komgaSettingsProvider.thumbnailSize.toDto(),
      komgaSettingsProvider.taskPoolSize,
      SettingMultiSource(configServerPort, komgaSettingsProvider.serverPort, serverSettings.effectiveServerPort),
      SettingMultiSource(configServerContextPath, komgaSettingsProvider.serverContextPath, serverSettings.effectiveServletContextPath),
      komgaSettingsProvider.koboProxy,
      komgaSettingsProvider.koboPort,
      SettingMultiSource(kepubConverter.kepubifyConfigurationPath, komgaSettingsProvider.kepubifyPath, kepubConverter.kepubifyPath?.toString()),
    )

  @PatchMapping
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @Operation(summary = "Update server settings", description = "You can omit fields you don't want to update")
  fun updateServerSettings(
    @Valid @RequestBody
    @Parameter(description = "Fields to update. You can omit fields you don't want to update.")
    newSettings: SettingsUpdateDto,
  ) {
    newSettings.deleteEmptyCollections?.let { komgaSettingsProvider.deleteEmptyCollections = it }
    newSettings.deleteEmptyReadLists?.let { komgaSettingsProvider.deleteEmptyReadLists = it }
    newSettings.rememberMeDurationDays?.let { komgaSettingsProvider.rememberMeDuration = it.days }
    if (newSettings.renewRememberMeKey == true) komgaSettingsProvider.renewRememberMeKey()
    newSettings.thumbnailSize?.let { komgaSettingsProvider.thumbnailSize = it.toDomain() }
    newSettings.taskPoolSize?.let { komgaSettingsProvider.taskPoolSize = it }

    if (newSettings.isSet("serverPort")) komgaSettingsProvider.serverPort = newSettings.serverPort
    if (newSettings.isSet("serverContextPath")) komgaSettingsProvider.serverContextPath = newSettings.serverContextPath

    newSettings.koboProxy?.let { komgaSettingsProvider.koboProxy = it }
    if (newSettings.isSet("koboPort")) komgaSettingsProvider.koboPort = newSettings.koboPort
    if (newSettings.isSet("kepubifyPath")) komgaSettingsProvider.kepubifyPath = newSettings.kepubifyPath
  }
}
