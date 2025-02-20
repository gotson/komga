package org.gotson.komga.interfaces.api.rest

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.gotson.komga.domain.persistence.SyncPointRepository
import org.gotson.komga.infrastructure.openapi.OpenApiConfiguration
import org.gotson.komga.infrastructure.security.KomgaPrincipal
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("api/v1/syncpoints", produces = [MediaType.APPLICATION_JSON_VALUE])
@Tag(name = OpenApiConfiguration.TagNames.SYNCPOINTS)
class SyncPointController(
  private val syncPointRepository: SyncPointRepository,
) {
  @DeleteMapping("me")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @Operation(
    summary = "Delete all sync points",
    description = "If an API Key ID is passed, deletes only the sync points associated with that API Key. Deleting sync points will allow a Kobo to sync from scratch upon the next sync.",
  )
  fun deleteMySyncPointsByApiKey(
    @AuthenticationPrincipal principal: KomgaPrincipal,
    @RequestParam(required = false, name = "key_id") keyIds: Collection<String>?,
  ) {
    if (keyIds.isNullOrEmpty())
      syncPointRepository.deleteByUserId(principal.user.id)
    else
      syncPointRepository.deleteByUserIdAndApiKeyIds(principal.user.id, keyIds)
  }
}
