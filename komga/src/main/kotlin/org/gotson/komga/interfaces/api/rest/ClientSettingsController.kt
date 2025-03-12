package org.gotson.komga.interfaces.api.rest

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.ExampleObject
import io.swagger.v3.oas.annotations.security.SecurityRequirements
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Pattern
import org.gotson.komga.infrastructure.jooq.main.ClientSettingsDtoDao
import org.gotson.komga.infrastructure.openapi.OpenApiConfiguration
import org.gotson.komga.infrastructure.security.KomgaPrincipal
import org.gotson.komga.interfaces.api.rest.dto.ClientSettingDto
import org.gotson.komga.interfaces.api.rest.dto.ClientSettingGlobalUpdateDto
import org.gotson.komga.interfaces.api.rest.dto.ClientSettingUserUpdateDto
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import io.swagger.v3.oas.annotations.parameters.RequestBody as OASRequestBody

private const val KEY_REGEX = """^[a-z](?:[a-z0-9_-]*[a-z0-9])*(?:\.[a-z0-9](?:[a-z0-9_-]*[a-z0-9])*)*$"""

@RestController
@RequestMapping(value = ["api/v1/client-settings"], produces = [MediaType.APPLICATION_JSON_VALUE])
@Tag(name = OpenApiConfiguration.TagNames.CLIENT_SETTINGS)
@Validated
class ClientSettingsController(
  private val clientSettingsDtoDao: ClientSettingsDtoDao,
) {
  @GetMapping("global/list")
  @Operation(summary = "Retrieve global client settings", description = "For unauthenticated users, only settings with 'allowUnauthorized=true' will be returned.")
  @SecurityRequirements
  fun getGlobalSettings(
    @AuthenticationPrincipal principal: KomgaPrincipal?,
  ): Map<String, ClientSettingDto> = clientSettingsDtoDao.findAllGlobal(principal == null)

  @GetMapping("user/list")
  @Operation(summary = "Retrieve user client settings")
  fun getUserSettings(
    @AuthenticationPrincipal principal: KomgaPrincipal,
  ): Map<String, ClientSettingDto> = clientSettingsDtoDao.findAllUser(principal.user.id)

  @PatchMapping("global")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @PreAuthorize("hasRole('ADMIN')")
  @Operation(summary = "Save global settings", description = "Setting key should be a valid lowercase namespace string like 'application.domain.key'")
  @OASRequestBody(
    content = [
      Content(
        examples = [
          //language=JSON
          ExampleObject(
            value =
              """{
              "application.key1": {
                "value": "a string value",
                "allowUnauthorized": true
              },
              "application.key2": {
                "value": "{\"json\":\"object\"}",
                "allowUnauthorized": false
              }
            }
            """,
          ),
        ],
      ),
    ],
  )
  fun saveGlobalSetting(
    @RequestBody newSettings: Map<
      @Pattern(regexp = KEY_REGEX)
      String,
      @NotNull @Valid
      ClientSettingGlobalUpdateDto,
    >,
  ) {
    newSettings.forEach { (key, setting) ->
      clientSettingsDtoDao.saveGlobal(key, setting.value, setting.allowUnauthorized)
    }
  }

  @PatchMapping("user")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @Operation(summary = "Save user settings", description = "Setting key should be a valid lowercase namespace string like 'application.domain.key'")
  @OASRequestBody(
    content = [
      Content(
        examples = [
          //language=JSON
          ExampleObject(
            value =
              """{
              "application.key1": {
                "value": "a string value"
              },
              "application.key2": {
                "value": "{\"json\":\"object\"}"
              }
            }
            """,
          ),
        ],
      ),
    ],
  )
  fun saveUserSetting(
    @AuthenticationPrincipal principal: KomgaPrincipal,
    @RequestBody newSettings: Map<
      @Pattern(regexp = KEY_REGEX)
      String,
      @NotNull @Valid
      ClientSettingUserUpdateDto,
    >,
  ) {
    newSettings.forEach { (key, setting) ->
      clientSettingsDtoDao.saveForUser(principal.user.id, key, setting.value)
    }
  }

  @DeleteMapping("global")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @PreAuthorize("hasRole('ADMIN')")
  @Operation(summary = "Delete global settings", description = "Setting key should be a valid lowercase namespace string like 'application.domain.key'")
  @OASRequestBody(
    content = [
      Content(
        examples = [
          //language=JSON
          ExampleObject(value = """["application.key1", "application.key2"]"""),
        ],
      ),
    ],
  )
  fun deleteGlobalSettings(
    @RequestBody keysToDelete: Set<
      @Pattern(regexp = KEY_REGEX)
      String,
    >,
  ) {
    clientSettingsDtoDao.deleteGlobalByKeys(keysToDelete)
  }

  @DeleteMapping("user")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @Operation(summary = "Delete user settings", description = "Setting key should be a valid lowercase namespace string like 'application.domain.key'")
  @OASRequestBody(
    content = [
      Content(
        examples = [
          //language=JSON
          ExampleObject(value = """["application.key1", "application.key2"]"""),
        ],
      ),
    ],
  )
  fun deleteUserSettings(
    @AuthenticationPrincipal principal: KomgaPrincipal,
    @RequestBody keysToDelete: Set<
      @Pattern(regexp = KEY_REGEX)
      String,
    >,
  ) {
    clientSettingsDtoDao.deleteByUserIdAndKeys(principal.user.id, keysToDelete)
  }
}
