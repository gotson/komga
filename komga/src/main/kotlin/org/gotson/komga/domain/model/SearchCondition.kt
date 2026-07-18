package org.gotson.komga.domain.model

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonTypeInfo
import io.swagger.v3.oas.annotations.media.Schema

class SearchCondition {
  @Schema(
    name = "SearchConditionBook",
    oneOf = [AnyOfBook::class, AllOfBook::class, LibraryId::class, ReadListId::class, SeriesId::class, Deleted::class, OneShot::class, Title::class, ReleaseDate::class, Tag::class, NumberSort::class, ReadStatus::class, MediaStatus::class, MediaProfile::class, Author::class, Poster::class],
  )
  @JsonTypeInfo(use = JsonTypeInfo.Id.DEDUCTION)
  sealed interface Book

  @Schema(
    name = "SearchConditionSeries",
    oneOf = [AnyOfSeries::class, AllOfSeries::class, LibraryId::class, CollectionId::class, Deleted::class, Complete::class, OneShot::class, Title::class, TitleSort::class, ReleaseDate::class, Tag::class, SharingLabel::class, Publisher::class, Language::class, Genre::class, AgeRating::class, ReadStatus::class, SeriesStatus::class, Author::class],
  )
  @JsonTypeInfo(use = JsonTypeInfo.Id.DEDUCTION)
  sealed interface Series

  @Schema(name = "SearchConditionAnyOfBook")
  data class AnyOfBook(
    @JsonProperty("anyOf")
    val conditions: List<Book>,
  ) : Book {
    constructor(vararg args: Book) : this(args.toList())
  }

  @Schema(name = "SearchConditionAllOfBook")
  data class AllOfBook(
    @JsonProperty("allOf")
    val conditions: List<Book>,
  ) : Book {
    constructor(vararg args: Book) : this(args.toList())
  }

  @Schema(name = "SearchConditionAnyOfSeries")
  data class AnyOfSeries(
    @JsonProperty("anyOf")
    val conditions: List<Series>,
  ) : Series {
    constructor(vararg args: Series) : this(args.toList())
  }

  @Schema(name = "SearchConditionAllOfSeries")
  data class AllOfSeries(
    @JsonProperty("allOf")
    val conditions: List<Series>,
  ) : Series {
    constructor(vararg args: Series) : this(args.toList())
  }

  @Schema(name = "SearchConditionLibraryId")
  data class LibraryId(
    @JsonProperty("libraryId")
    val operator: SearchOperator.Equality<String>,
  ) : Book,
    Series

  @Schema(name = "SearchConditionCollectionId")
  data class CollectionId(
    @JsonProperty("collectionId")
    val operator: SearchOperator.Equality<String>,
  ) : Series

  @Schema(name = "SearchConditionReadListId")
  data class ReadListId(
    @JsonProperty("readListId")
    val operator: SearchOperator.Equality<String>,
  ) : Book

  @Schema(name = "SearchConditionSeriesId")
  data class SeriesId(
    @JsonProperty("seriesId")
    val operator: SearchOperator.Equality<String>,
  ) : Book

  @Schema(name = "SearchConditionDeleted")
  data class Deleted(
    @JsonProperty("deleted")
    val operator: SearchOperator.Boolean,
  ) : Book,
    Series

  @Schema(name = "SearchConditionComplete")
  data class Complete(
    @JsonProperty("complete")
    val operator: SearchOperator.Boolean,
  ) : Series

  @Schema(name = "SearchConditionOneShot")
  data class OneShot(
    @JsonProperty("oneShot")
    val operator: SearchOperator.Boolean,
  ) : Book,
    Series

  @Schema(name = "SearchConditionTitle")
  data class Title(
    @JsonProperty("title")
    val operator: SearchOperator.StringOp,
  ) : Book,
    Series

  @Schema(name = "SearchConditionTitleSort")
  data class TitleSort(
    @JsonProperty("titleSort")
    val operator: SearchOperator.StringOp,
  ) : Series

  @Schema(name = "SearchConditionReleaseDate")
  data class ReleaseDate(
    @JsonProperty("releaseDate")
    val operator: SearchOperator.Date,
  ) : Book,
    Series

  @Schema(name = "SearchConditionNumberSort")
  data class NumberSort(
    @JsonProperty("numberSort")
    val operator: SearchOperator.Numeric<Float>,
  ) : Book

  @Schema(name = "SearchConditionTag")
  data class Tag(
    @JsonProperty("tag")
    val operator: SearchOperator.EqualityNullable<String>,
  ) : Book,
    Series

  @Schema(name = "SearchConditionSharingLabel")
  data class SharingLabel(
    @JsonProperty("sharingLabel")
    val operator: SearchOperator.EqualityNullable<String>,
  ) : Series

  @Schema(name = "SearchConditionPublisher")
  data class Publisher(
    @JsonProperty("publisher")
    val operator: SearchOperator.Equality<String>,
  ) : Series

  @Schema(name = "SearchConditionLanguage")
  data class Language(
    @JsonProperty("language")
    val operator: SearchOperator.Equality<String>,
  ) : Series

  @Schema(name = "SearchConditionGenre")
  data class Genre(
    @JsonProperty("genre")
    val operator: SearchOperator.EqualityNullable<String>,
  ) : Series

  @Schema(name = "SearchConditionAgeRating")
  data class AgeRating(
    @JsonProperty("ageRating")
    val operator: SearchOperator.NumericNullable<Int>,
  ) : Series

  @Schema(name = "SearchConditionReadStatus")
  data class ReadStatus(
    @JsonProperty("readStatus")
    val operator: SearchOperator.Equality<org.gotson.komga.domain.model.ReadStatus>,
  ) : Book,
    Series

  @Schema(name = "SearchConditionMediaStatus")
  data class MediaStatus(
    @JsonProperty("mediaStatus")
    val operator: SearchOperator.Equality<Media.Status>,
  ) : Book

  @Schema(name = "SearchConditionSeriesStatus")
  data class SeriesStatus(
    @JsonProperty("seriesStatus")
    val operator: SearchOperator.Equality<SeriesMetadata.Status>,
  ) : Series

  @Schema(name = "SearchConditionMediaProfile")
  data class MediaProfile(
    @JsonProperty("mediaProfile")
    val operator: SearchOperator.Equality<org.gotson.komga.domain.model.MediaProfile>,
  ) : Book

  @Schema(name = "SearchConditionAuthor")
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

  @Schema(name = "SearchConditionPoster")
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
