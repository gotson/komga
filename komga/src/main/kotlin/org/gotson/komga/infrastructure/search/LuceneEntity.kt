package org.gotson.komga.infrastructure.search

import org.apache.lucene.document.DateTools
import org.apache.lucene.document.Document
import org.apache.lucene.document.Field
import org.apache.lucene.document.StringField
import org.apache.lucene.document.TextField
import org.gotson.komga.domain.model.ReadList
import org.gotson.komga.domain.model.SeriesCollection
import org.gotson.komga.interfaces.api.rest.dto.BookDto
import org.gotson.komga.interfaces.api.rest.dto.SeriesDto
import org.gotson.komga.language.toDate

enum class LuceneEntity(
  val type: String,
  val id: String,
  val defaultFields: Array<String>,
) {
  Book("book", "book_id", arrayOf("title", "isbn")),
  Series("series", "series_id", arrayOf("title")),
  Collection("collection", "collection_id", arrayOf("name")),
  ReadList("readlist", "readlist_id", arrayOf("name")),
  ;

  companion object {
    const val TYPE = "type"
  }
}

fun BookDto.toDocument() =
  Document().apply {
    add(TextField("title", metadata.title, Field.Store.NO))
    add(TextField("isbn", metadata.isbn, Field.Store.NO))
    metadata.tags.forEach {
      add(TextField("tag", it, Field.Store.NO))
    }
    metadata.authors.forEach {
      add(TextField("author", it.name, Field.Store.NO))
      add(TextField(it.role, it.name, Field.Store.NO))
    }
    if (metadata.releaseDate != null) add(TextField("release_date", DateTools.dateToString(metadata.releaseDate.toDate(), DateTools.Resolution.YEAR), Field.Store.NO))
    add(TextField("status", media.status, Field.Store.NO))
    add(TextField("deleted", deleted.toString(), Field.Store.NO))
    add(TextField("oneshot", oneshot.toString(), Field.Store.NO))

    add(StringField(LuceneEntity.TYPE, LuceneEntity.Book.type, Field.Store.NO))
    add(StringField(LuceneEntity.Book.id, id, Field.Store.YES))
  }

fun SeriesDto.toDocument() =
  Document().apply {
    add(TextField("title", metadata.title, Field.Store.NO))
    if (metadata.titleSort != metadata.title) add(TextField("title", metadata.titleSort, Field.Store.NO))
    metadata.alternateTitles.forEach { add(TextField("title", it.title, Field.Store.NO)) }
    add(TextField("publisher", metadata.publisher, Field.Store.NO))
    add(TextField("status", metadata.status, Field.Store.NO))
    add(TextField("reading_direction", metadata.readingDirection, Field.Store.NO))
    if (metadata.ageRating != null) add(TextField("age_rating", metadata.ageRating.toString(), Field.Store.NO))
    if (metadata.language.isNotBlank()) add(TextField("language", metadata.language, Field.Store.NO))
    metadata.tags.forEach {
      add(TextField("series_tag", it, Field.Store.NO))
      add(TextField("tag", it, Field.Store.NO))
    }
    booksMetadata.tags.forEach {
      add(TextField("book_tag", it, Field.Store.NO))
      add(TextField("tag", it, Field.Store.NO))
    }
    metadata.genres.forEach {
      add(TextField("genre", it, Field.Store.NO))
    }
    metadata.sharingLabels.forEach {
      add(TextField("sharing_label", it, Field.Store.NO))
    }
    if (metadata.totalBookCount != null) add(TextField("total_book_count", metadata.totalBookCount.toString(), Field.Store.NO))
    add(TextField("book_count", booksCount.toString(), Field.Store.NO))
    booksMetadata.authors.forEach {
      add(TextField("author", it.name, Field.Store.NO))
      add(TextField(it.role, it.name, Field.Store.NO))
    }
    if (booksMetadata.releaseDate != null) add(TextField("release_date", DateTools.dateToString(booksMetadata.releaseDate.toDate(), DateTools.Resolution.YEAR), Field.Store.NO))
    add(TextField("deleted", deleted.toString(), Field.Store.NO))
    add(TextField("oneshot", oneshot.toString(), Field.Store.NO))
    if (metadata.totalBookCount != null) add(TextField("complete", (metadata.totalBookCount == booksCount).toString(), Field.Store.NO))

    add(StringField(LuceneEntity.TYPE, LuceneEntity.Series.type, Field.Store.NO))
    add(StringField(LuceneEntity.Series.id, id, Field.Store.YES))
  }

fun SeriesDto.oneshotDocument(document: Document) =
  document.apply {
    add(TextField("publisher", metadata.publisher, Field.Store.NO))
    add(TextField("status", metadata.status, Field.Store.NO))
    add(TextField("reading_direction", metadata.readingDirection, Field.Store.NO))
    if (metadata.ageRating != null) add(TextField("age_rating", metadata.ageRating.toString(), Field.Store.NO))
    if (metadata.language.isNotBlank()) add(TextField("language", metadata.language, Field.Store.NO))
    metadata.genres.forEach {
      add(TextField("genre", it, Field.Store.NO))
    }
    metadata.sharingLabels.forEach {
      add(TextField("sharing_label", it, Field.Store.NO))
    }
    add(TextField("complete", "true", Field.Store.NO))
  }

fun SeriesCollection.toDocument() =
  Document().apply {
    add(TextField("name", name, Field.Store.NO))
    add(StringField(LuceneEntity.TYPE, LuceneEntity.Collection.type, Field.Store.NO))
    add(StringField(LuceneEntity.Collection.id, id, Field.Store.YES))
  }

fun ReadList.toDocument() =
  Document().apply {
    add(TextField("name", name, Field.Store.NO))
    add(StringField(LuceneEntity.TYPE, LuceneEntity.ReadList.type, Field.Store.NO))
    add(StringField(LuceneEntity.ReadList.id, id, Field.Store.YES))
  }
