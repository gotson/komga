package org.gotson.komga.infrastructure.jooq

/**
 * An indication that some tables need to be joined for query conditions to work
 */
sealed class RequiredJoin {
  data object BookMetadata : RequiredJoin()

  data object Media : RequiredJoin()

  data class ReadProgress(
    val userId: String,
  ) : RequiredJoin()

  data class ReadList(
    val readListId: String,
  ) : RequiredJoin()

  data class Collection(
    val collectionId: String,
  ) : RequiredJoin()

  data object BookMetadataAggregation : RequiredJoin()

  data object SeriesMetadata : RequiredJoin()
}
