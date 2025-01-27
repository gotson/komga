package org.gotson.komga.domain.service

import org.gotson.komga.domain.model.BookMetadata
import org.gotson.komga.domain.model.BookMetadataAggregation
import org.springframework.stereotype.Service

@Service
class MetadataAggregator {
  fun aggregate(metadatas: Collection<BookMetadata>): BookMetadataAggregation {
    val authors = metadatas.flatMap { it.authors }.distinctBy { "${it.role}__${it.name}" }
    val tags = metadatas.flatMap { it.tags }.toSet()
    val (summary, summaryNumber) =
      metadatas
        .sortedBy { it.numberSort }
        .find { it.summary.isNotBlank() }
        ?.let {
          it.summary to it.number
        } ?: ("" to "")
    val releaseDate = metadatas.mapNotNull { it.releaseDate }.minOrNull()

    return BookMetadataAggregation(authors = authors, tags = tags, releaseDate = releaseDate, summary = summary, summaryNumber = summaryNumber)
  }
}
