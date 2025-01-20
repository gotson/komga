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
class BookSearchTest(
  @Autowired private val mapper: ObjectMapper,
) {
  private val writer = mapper.writerWithDefaultPrettyPrinter()

  @Test
  fun `given bookSearch entity when serializing then it looks alright`() {
    val search =
      BookSearch(
        SearchCondition.AllOfBook(
          SearchCondition.AnyOfBook(
            SearchCondition.LibraryId(SearchOperator.Is("library1")),
            SearchCondition.LibraryId(SearchOperator.IsNot("library1")),
          ),
          SearchCondition.SeriesId(SearchOperator.Is("series1")),
          SearchCondition.SeriesId(SearchOperator.IsNot("series1")),
          SearchCondition.ReadListId(SearchOperator.Is("readList1")),
          SearchCondition.ReadListId(SearchOperator.IsNot("readList1")),
          SearchCondition.Deleted(SearchOperator.IsFalse),
          SearchCondition.Deleted(SearchOperator.IsTrue),
          SearchCondition.Title(SearchOperator.BeginsWith("abc")),
          SearchCondition.Title(SearchOperator.EndsWith("abc")),
          SearchCondition.Title(SearchOperator.Is("abc")),
          SearchCondition.Title(SearchOperator.IsNot("abc")),
          SearchCondition.Title(SearchOperator.Contains("abc")),
          SearchCondition.Title(SearchOperator.DoesNotContain("abc")),
          SearchCondition.ReleaseDate(SearchOperator.Before(ZonedDateTime.now(ZoneOffset.UTC).minusMonths(1))),
          SearchCondition.ReleaseDate(SearchOperator.After(ZonedDateTime.now(ZoneOffset.UTC).minusMonths(1))),
          SearchCondition.ReleaseDate(SearchOperator.IsNotInTheLast(Duration.ofDays(5))),
          SearchCondition.ReleaseDate(SearchOperator.IsInTheLast(Duration.ofDays(5))),
          SearchCondition.ReleaseDate(SearchOperator.IsNotNull),
          SearchCondition.ReleaseDate(SearchOperator.IsNull),
          SearchCondition.NumberSort(SearchOperator.GreaterThan(5F)),
          SearchCondition.NumberSort(SearchOperator.LessThan(5F)),
          SearchCondition.NumberSort(SearchOperator.Is(5F)),
          SearchCondition.NumberSort(SearchOperator.IsNot(5F)),
          SearchCondition.Tag(SearchOperator.Is("fiction")),
          SearchCondition.Tag(SearchOperator.IsNot("fantasy")),
          SearchCondition.ReadStatus(SearchOperator.IsNot(ReadStatus.READ)),
          SearchCondition.ReadStatus(SearchOperator.Is(ReadStatus.READ)),
          SearchCondition.MediaStatus(SearchOperator.Is(Media.Status.READY)),
          SearchCondition.MediaStatus(SearchOperator.IsNot(Media.Status.READY)),
          SearchCondition.MediaProfile(SearchOperator.Is(MediaProfile.PDF)),
          SearchCondition.MediaProfile(SearchOperator.IsNot(MediaProfile.PDF)),
          SearchCondition.Author(SearchOperator.Is(AuthorMatch("john", "writer"))),
          SearchCondition.Author(SearchOperator.IsNot(AuthorMatch("jack", "writer"))),
          SearchCondition.Author(SearchOperator.Is(AuthorMatch(role = "writer"))),
          SearchCondition.Author(SearchOperator.IsNot(AuthorMatch(name = "jim"))),
          SearchCondition.Author(SearchOperator.IsNot(AuthorMatch())),
          SearchCondition.OneShot(SearchOperator.IsFalse),
          SearchCondition.OneShot(SearchOperator.IsTrue),
          SearchCondition.Poster(SearchOperator.Is(SearchCondition.PosterMatch(type = SearchCondition.PosterMatch.Type.GENERATED, selected = false))),
          SearchCondition.Poster(SearchOperator.Is(SearchCondition.PosterMatch(selected = true))),
          SearchCondition.Poster(SearchOperator.IsNot(SearchCondition.PosterMatch(type = SearchCondition.PosterMatch.Type.SIDECAR))),
          SearchCondition.Poster(SearchOperator.IsNot(SearchCondition.PosterMatch())),
        ),
      )

    val json = writer.writeValueAsString(search)

    println(json)

    val entity = mapper.readValue<BookSearch>(json)

    assertThat(entity).isEqualTo(search)
  }
}
