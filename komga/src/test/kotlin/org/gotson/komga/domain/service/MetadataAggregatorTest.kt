package org.gotson.komga.domain.service

import org.assertj.core.api.Assertions.assertThat
import org.gotson.komga.domain.model.Author
import org.gotson.komga.domain.model.BookMetadata
import org.junit.jupiter.api.Test
import java.time.LocalDate

class MetadataAggregatorTest {
  private val aggregator = MetadataAggregator()

  @Test
  fun `given metadatas when aggregating then aggregation is relevant`() {
    val metadatas =
      listOf(
        BookMetadata(title = "ignored", summary = "summary 1", number = "1", numberSort = 1F, authors = listOf(Author("author1", "role1"), Author("author2", "role2")), releaseDate = LocalDate.of(2020, 1, 1), tags = setOf("tag1")),
        BookMetadata(title = "ignored", summary = "summary 2", number = "2", numberSort = 2F, authors = listOf(Author("author3", "role3"), Author("author2", "role3")), releaseDate = LocalDate.of(2021, 1, 1), tags = setOf("tag2")),
      )

    val aggregation = aggregator.aggregate(metadatas)

    assertThat(aggregation.authors).hasSize(4)
    assertThat(aggregation.tags).hasSize(2)
    assertThat(aggregation.releaseDate?.year).isEqualTo(2020)
    assertThat(aggregation.summary).isEqualTo("summary 1")
    assertThat(aggregation.summaryNumber).isEqualTo("1")
  }

  @Test
  fun `given metadatas with summary only on second book when aggregating then aggregation has second book's summary`() {
    val metadatas =
      listOf(
        BookMetadata(title = "ignored", number = "1", numberSort = 1F),
        BookMetadata(title = "ignored", summary = "summary 2", number = "2", numberSort = 2F),
      )

    val aggregation = aggregator.aggregate(metadatas)

    assertThat(aggregation.summary).isEqualTo("summary 2")
    assertThat(aggregation.summaryNumber).isEqualTo("2")
  }

  @Test
  fun `given metadatas with second book with earlier release date when aggregating then aggregation has release date from second book`() {
    val metadatas =
      listOf(
        BookMetadata(title = "ignored", number = "1", numberSort = 1F, releaseDate = LocalDate.of(2020, 1, 1)),
        BookMetadata(title = "ignored", number = "2", numberSort = 2F, releaseDate = LocalDate.of(2019, 1, 1)),
      )

    val aggregation = aggregator.aggregate(metadatas)

    assertThat(aggregation.releaseDate?.year).isEqualTo(2019)
  }

  @Test
  fun `given metadatas with duplicate authors or tags when aggregating then aggregation has no duplicates`() {
    val metadatas =
      listOf(
        BookMetadata(title = "ignored", number = "1", numberSort = 1F, authors = listOf(Author("author1", "role1"), Author("author2", "role2")), tags = setOf("tag1", "tag2")),
        BookMetadata(title = "ignored", number = "2", numberSort = 2F, authors = listOf(Author("author1", "role1"), Author("author2", "role2")), tags = setOf("tag1")),
      )

    val aggregation = aggregator.aggregate(metadatas)

    assertThat(aggregation.authors).hasSize(2)
    assertThat(aggregation.tags).hasSize(2)
  }
}
