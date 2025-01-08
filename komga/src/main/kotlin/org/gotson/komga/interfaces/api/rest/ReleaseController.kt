package org.gotson.komga.interfaces.api.rest

import com.github.benmanes.caffeine.cache.Caffeine
import org.gotson.komga.infrastructure.security.KomgaPrincipal
import org.gotson.komga.interfaces.api.rest.dto.GithubReleaseDto
import org.gotson.komga.interfaces.api.rest.dto.ReleaseDto
import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.server.ResponseStatusException
import java.util.concurrent.TimeUnit

private const val GITHUB_API = "https://api.github.com/repos/gotson/komga/releases"

@RestController
@PreAuthorize("hasRole('ADMIN')")
@RequestMapping("api/v1/releases", produces = [MediaType.APPLICATION_JSON_VALUE])
class ReleaseController(
  webClientBuilder: WebClient.Builder,
) {
  private val webClient = webClientBuilder.baseUrl(GITHUB_API).build()

  private val cache =
    Caffeine
      .newBuilder()
      .expireAfterAccess(1, TimeUnit.DAYS)
      .build<String, List<GithubReleaseDto>>()

  @GetMapping
  @PreAuthorize("hasRole('ADMIN')")
  fun getAnnouncements(
    @AuthenticationPrincipal principal: KomgaPrincipal,
  ): List<ReleaseDto> =
    cache
      .get("releases") { fetchGitHubReleases() }
      ?.let { releases ->
        releases.mapIndexed { index, ghRel ->
          ReleaseDto(
            ghRel.tagName,
            ghRel.publishedAt,
            ghRel.htmlUrl,
            index == 0,
            ghRel.prerelease,
            ghRel.body,
          )
        }
      }
      ?: throw ResponseStatusException(HttpStatus.NOT_FOUND)

  fun fetchGitHubReleases(): List<GithubReleaseDto>? {
    val response =
      webClient
        .get()
        .uri {
          it.queryParam("per_page", 20).build()
        }.retrieve()
        .toEntity(object : ParameterizedTypeReference<List<GithubReleaseDto>>() {})
        .block()
    return response?.body
  }
}
