package org.gotson.komga.interfaces.api.rest

import com.github.benmanes.caffeine.cache.Caffeine
import org.gotson.komga.domain.model.ROLE_ADMIN
import org.gotson.komga.domain.persistence.KomgaUserRepository
import org.gotson.komga.infrastructure.security.KomgaPrincipal
import org.gotson.komga.interfaces.api.rest.dto.JsonFeedDto
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.server.ResponseStatusException
import java.util.concurrent.TimeUnit

private const val WEBSITE = "https://komga.org"

@RestController
@RequestMapping("api/v1/announcements", produces = [MediaType.APPLICATION_JSON_VALUE])
class AnnouncementController(
  private val userRepository: KomgaUserRepository,
  webClientBuilder: WebClient.Builder,
) {
  private val webClient = webClientBuilder.baseUrl("$WEBSITE/blog/feed.json").build()

  private val cache =
    Caffeine
      .newBuilder()
      .expireAfterAccess(1, TimeUnit.DAYS)
      .build<String, JsonFeedDto>()

  @GetMapping
  @PreAuthorize("hasRole('$ROLE_ADMIN')")
  fun getAnnouncements(
    @AuthenticationPrincipal principal: KomgaPrincipal,
  ): JsonFeedDto =
    cache
      .get("announcements") { fetchWebsiteAnnouncements() }
      ?.let { feed ->
        val read = userRepository.findAnnouncementIdsReadByUserId(principal.user.id)
        feed.copy(items = feed.items.map { item -> item.copy(komgaExtension = JsonFeedDto.KomgaExtensionDto(read.contains(item.id))) })
      }
      ?: throw ResponseStatusException(HttpStatus.NOT_FOUND)

  @PreAuthorize("hasRole('$ROLE_ADMIN')")
  @PutMapping
  @ResponseStatus(HttpStatus.NO_CONTENT)
  fun markAnnouncementsRead(
    @AuthenticationPrincipal principal: KomgaPrincipal,
    @RequestBody announcementIds: Set<String>,
  ) {
    userRepository.saveAnnouncementIdsRead(principal.user, announcementIds)
  }

  fun fetchWebsiteAnnouncements(): JsonFeedDto? {
    val response =
      webClient
        .get()
        .retrieve()
        .toEntity(JsonFeedDto::class.java)
        .block()
    return response?.body
  }
}
