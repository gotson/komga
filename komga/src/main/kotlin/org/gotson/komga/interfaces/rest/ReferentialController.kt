package org.gotson.komga.interfaces.rest

import org.gotson.komga.domain.persistence.BookMetadataRepository
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController


@RestController
@RequestMapping("api/v1", produces = [MediaType.APPLICATION_JSON_VALUE])
class ReferentialController(
  private val bookMetadataRepository: BookMetadataRepository
) {

  @GetMapping("/authors")
  fun getAuthors(
    @RequestParam(name = "search", defaultValue = "") search: String
  ): List<String> =
    bookMetadataRepository.findAuthorsByName(search)
}
