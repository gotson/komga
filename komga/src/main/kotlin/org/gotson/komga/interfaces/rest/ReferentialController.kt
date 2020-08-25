package org.gotson.komga.interfaces.rest

import org.gotson.komga.domain.persistence.ReferentialRepository
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController


@RestController
@RequestMapping("api/v1", produces = [MediaType.APPLICATION_JSON_VALUE])
class ReferentialController(
  private val referentialRepository: ReferentialRepository
) {

  @GetMapping("/authors")
  fun getAuthors(
    @RequestParam(name = "search", defaultValue = "") search: String
  ): List<String> =
    referentialRepository.findAuthorsByName(search)

  @GetMapping("/genres")
  fun getGenres(): Set<String> =
    referentialRepository.findAllGenres()

  @GetMapping("/tags")
  fun getTags(): Set<String> =
    referentialRepository.findAllTags()

  @GetMapping("/languages")
  fun getLanguages(): Set<String> =
    referentialRepository.findAllLanguages()

  @GetMapping("/publishers")
  fun getPublishers(): Set<String> =
    referentialRepository.findAllPublishers()
}
