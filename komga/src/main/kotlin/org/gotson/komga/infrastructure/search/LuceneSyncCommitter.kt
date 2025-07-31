package org.gotson.komga.infrastructure.search

import org.apache.lucene.index.IndexWriter
import org.apache.lucene.search.SearcherManager
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component

@Profile("test")
@Component
class LuceneSyncCommitter(
  private val indexWriter: IndexWriter,
  private val searcherManager: SearcherManager,
) : LuceneCommitter {
  override fun commitAndMaybeRefresh() {
    indexWriter.commit()
    searcherManager.maybeRefreshBlocking()
  }
}
