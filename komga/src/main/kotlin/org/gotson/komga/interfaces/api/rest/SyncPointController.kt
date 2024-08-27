package org.gotson.komga.interfaces.api.rest

import org.gotson.komga.domain.persistence.SyncPointRepository
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
class SyncPointController(
  private val syncPointRepository: SyncPointRepository,
) {
  @DeleteMapping("me")
  @ResponseStatus(HttpStatus.NO_CONTENT)
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
