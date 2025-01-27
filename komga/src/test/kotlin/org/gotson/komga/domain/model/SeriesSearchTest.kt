package org.gotson.komga.domain.model

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.assertj.core.api.Assertions.assertThat
import org.gotson.komga.domain.model.SearchCondition.AuthorMatch
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import java.time.Duration
import java.time.ZoneOffset
import java.time.ZonedDateTime

@SpringBootTest
class SeriesSearchTest(
  @Autowired private val mapper: ObjectMapper,
) {
  private val writer = mapper.writerWithDefaultPrettyPrinter()

  @Test
  fun `given seriesSearch entity when serializing then it looks alright`() {
    val search =
      SeriesSearch(
        SearchCondition.AllOfSeries(
          SearchCondition.AnyOfSeries(
            emptyList(),
          ),
          SearchCondition.AnyOfSeries(
            SearchCondition.LibraryId(SearchOperator.Is("library1")),
            SearchCondition.LibraryId(SearchOperator.IsNot("library1")),
          ),
          SearchCondition.CollectionId(SearchOperator.Is("collection1")),
          SearchCondition.CollectionId(SearchOperator.IsNot("collection1")),
          SearchCondition.Deleted(SearchOperator.IsFalse),
          SearchCondition.Deleted(SearchOperator.IsTrue),
          SearchCondition.Complete(SearchOperator.IsTrue),
          SearchCondition.Complete(SearchOperator.IsFalse),
          SearchCondition.ReleaseDate(SearchOperator.Before(ZonedDateTime.now(ZoneOffset.UTC).minusMonths(1))),
          SearchCondition.ReleaseDate(SearchOperator.After(ZonedDateTime.now(ZoneOffset.UTC).minusMonths(1))),
          SearchCondition.ReleaseDate(SearchOperator.IsNotInTheLast(Duration.ofDays(5))),
          SearchCondition.ReleaseDate(SearchOperator.IsInTheLast(Duration.ofDays(5))),
          SearchCondition.ReleaseDate(SearchOperator.IsNotNull),
          SearchCondition.ReleaseDate(SearchOperator.IsNull),
          SearchCondition.AgeRating(SearchOperator.LessThan(5)),
          SearchCondition.AgeRating(SearchOperator.Is(5)),
          SearchCondition.AgeRating(SearchOperator.IsNullT()),
          SearchCondition.AgeRating(SearchOperator.IsNot(5)),
          SearchCondition.AgeRating(SearchOperator.IsNotNullT()),
          SearchCondition.Tag(SearchOperator.Is("fiction")),
          SearchCondition.Tag(SearchOperator.IsNot("fantasy")),
          SearchCondition.SharingLabel(SearchOperator.Is("label1")),
          SearchCondition.SharingLabel(SearchOperator.IsNot("label1")),
          SearchCondition.Publisher(SearchOperator.Is("publisher1")),
          SearchCondition.Publisher(SearchOperator.IsNot("publisher1")),
          SearchCondition.Language(SearchOperator.Is("en")),
          SearchCondition.Language(SearchOperator.IsNot("en")),
          SearchCondition.Genre(SearchOperator.Is("genre1")),
          SearchCondition.Genre(SearchOperator.IsNot("genre1")),
          SearchCondition.ReadStatus(SearchOperator.IsNot(ReadStatus.READ)),
          SearchCondition.ReadStatus(SearchOperator.Is(ReadStatus.READ)),
          SearchCondition.SeriesStatus(SearchOperator.Is(SeriesMetadata.Status.ENDED)),
          SearchCondition.SeriesStatus(SearchOperator.IsNot(SeriesMetadata.Status.ONGOING)),
          SearchCondition.Author(SearchOperator.Is(AuthorMatch("john", "writer"))),
          SearchCondition.Author(SearchOperator.IsNot(AuthorMatch("jack", "writer"))),
          SearchCondition.Author(SearchOperator.Is(AuthorMatch(role = "writer"))),
          SearchCondition.Author(SearchOperator.IsNot(AuthorMatch(name = "jim"))),
          SearchCondition.Author(SearchOperator.IsNot(AuthorMatch())),
          SearchCondition.OneShot(SearchOperator.IsFalse),
          SearchCondition.OneShot(SearchOperator.IsTrue),
          SearchCondition.Title(SearchOperator.Is("abc")),
          SearchCondition.Title(SearchOperator.IsNot("abc")),
          SearchCondition.Title(SearchOperator.Contains("abc")),
          SearchCondition.Title(SearchOperator.DoesNotContain("abc")),
          SearchCondition.Title(SearchOperator.BeginsWith("abc")),
          SearchCondition.Title(SearchOperator.DoesNotBeginWith("abc")),
          SearchCondition.Title(SearchOperator.EndsWith("abc")),
          SearchCondition.Title(SearchOperator.DoesNotEndWith("abc")),
          SearchCondition.TitleSort(SearchOperator.Is("abc")),
          SearchCondition.TitleSort(SearchOperator.IsNot("abc")),
          SearchCondition.TitleSort(SearchOperator.Contains("abc")),
          SearchCondition.TitleSort(SearchOperator.DoesNotContain("abc")),
          SearchCondition.TitleSort(SearchOperator.BeginsWith("abc")),
          SearchCondition.TitleSort(SearchOperator.DoesNotBeginWith("abc")),
          SearchCondition.TitleSort(SearchOperator.EndsWith("abc")),
          SearchCondition.TitleSort(SearchOperator.DoesNotEndWith("abc")),
        ),
      )

    val json = writer.writeValueAsString(search)

    println(json)

    val entity = mapper.readValue<SeriesSearch>(json)

    assertThat(entity).isEqualTo(search)
  }
}
