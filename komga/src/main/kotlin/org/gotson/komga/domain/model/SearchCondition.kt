package org.gotson.komga.domain.model

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonTypeInfo

class SearchCondition {
  @JsonTypeInfo(use = JsonTypeInfo.Id.DEDUCTION)
  sealed interface Book

  @JsonTypeInfo(use = JsonTypeInfo.Id.DEDUCTION)
  sealed interface Series

  data class AnyOfBook(
    @JsonProperty("anyOf")
    val conditions: List<Book>,
  ) : Book {
    constructor(vararg args: Book) : this(args.toList())
  }

  data class AllOfBook(
    @JsonProperty("allOf")
    val conditions: List<Book>,
  ) : Book {
    constructor(vararg args: Book) : this(args.toList())
  }

  data class AnyOfSeries(
    @JsonProperty("anyOf")
    val conditions: List<Series>,
  ) : Series {
    constructor(vararg args: Series) : this(args.toList())
  }

  data class AllOfSeries(
    @JsonProperty("allOf")
    val conditions: List<Series>,
  ) : Series {
    constructor(vararg args: Series) : this(args.toList())
  }

  data class LibraryId(
    @JsonProperty("libraryId")
    val operator: SearchOperator.Equality<String>,
  ) : Book,
    Series

  data class CollectionId(
    @JsonProperty("collectionId")
    val operator: SearchOperator.Equality<String>,
  ) : Series

  data class ReadListId(
    @JsonProperty("readListId")
    val operator: SearchOperator.Equality<String>,
  ) : Book

  data class SeriesId(
    @JsonProperty("seriesId")
    val operator: SearchOperator.Equality<String>,
  ) : Book

  data class Deleted(
    @JsonProperty("deleted")
    val operator: SearchOperator.Boolean,
  ) : Book,
    Series

  data class Complete(
    @JsonProperty("complete")
    val operator: SearchOperator.Boolean,
  ) : Series

  data class OneShot(
    @JsonProperty("oneShot")
    val operator: SearchOperator.Boolean,
  ) : Book,
    Series

  data class Title(
    @JsonProperty("title")
    val operator: SearchOperator.StringOp,
  ) : Book,
    Series

  data class TitleSort(
    @JsonProperty("titleSort")
    val operator: SearchOperator.StringOp,
  ) : Series

  data class ReleaseDate(
    @JsonProperty("releaseDate")
    val operator: SearchOperator.Date,
  ) : Book,
    Series

  data class NumberSort(
    @JsonProperty("numberSort")
    val operator: SearchOperator.Numeric<Float>,
  ) : Book

  data class Tag(
    @JsonProperty("tag")
    val operator: SearchOperator.Equality<String>,
  ) : Book,
    Series

  data class SharingLabel(
    @JsonProperty("sharingLabel")
    val operator: SearchOperator.Equality<String>,
  ) : Series

  data class Publisher(
    @JsonProperty("publisher")
    val operator: SearchOperator.Equality<String>,
  ) : Series

  data class Language(
    @JsonProperty("language")
    val operator: SearchOperator.Equality<String>,
  ) : Series

  data class Genre(
    @JsonProperty("genre")
    val operator: SearchOperator.Equality<String>,
  ) : Series

  data class AgeRating(
    @JsonProperty("ageRating")
    val operator: SearchOperator.NumericNullable<Int>,
  ) : Series

  data class ReadStatus(
    @JsonProperty("readStatus")
    val operator: SearchOperator.Equality<org.gotson.komga.domain.model.ReadStatus>,
  ) : Book,
    Series

  data class MediaStatus(
    @JsonProperty("mediaStatus")
    val operator: SearchOperator.Equality<Media.Status>,
  ) : Book

  data class SeriesStatus(
    @JsonProperty("seriesStatus")
    val operator: SearchOperator.Equality<SeriesMetadata.Status>,
  ) : Series

  data class MediaProfile(
    @JsonProperty("mediaProfile")
    val operator: SearchOperator.Equality<org.gotson.komga.domain.model.MediaProfile>,
  ) : Book

  data class Author(
    @JsonProperty("author")
    val operator: SearchOperator.Equality<AuthorMatch>,
  ) : Book,
    Series

  @JsonInclude(JsonInclude.Include.NON_NULL)
  data class AuthorMatch(
    val name: String? = null,
    val role: String? = null,
  )

  data class Poster(
    @JsonProperty("poster")
    val operator: SearchOperator.Equality<PosterMatch>,
  ) : Book

  @JsonInclude(JsonInclude.Include.NON_NULL)
  data class PosterMatch(
    val type: Type? = null,
    val selected: Boolean? = null,
  ) {
    enum class Type {
      GENERATED,
      SIDECAR,
      USER_UPLOADED,
    }
  }
}
