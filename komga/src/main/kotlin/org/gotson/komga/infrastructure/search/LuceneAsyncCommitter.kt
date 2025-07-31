package org.gotson.komga.infrastructure.search

import org.apache.lucene.index.IndexWriter
import org.apache.lucene.search.SearcherManager
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Profile
import org.springframework.scheduling.TaskScheduler
import org.springframework.stereotype.Component
import java.time.Duration
import java.time.ZonedDateTime
import java.util.concurrent.ScheduledFuture

@Profile("!test")
@Component
class LuceneAsyncCommitter(
  private val indexWriter: IndexWriter,
  private val searcherManager: SearcherManager,
  private val taskScheduler: TaskScheduler,
  @param:Value("#{@komgaProperties.lucene.commitDelay}")
  private val commitDelay: Duration,
) : LuceneCommitter {
  @Volatile
  private var commitFuture: ScheduledFuture<*>? = null

  private val commitRunnable =
    Runnable {
      indexWriter.commit()
      searcherManager.maybeRefresh()
    }

  override fun commitAndMaybeRefresh() {
    if (commitFuture == null || commitFuture!!.isDone)
      commitFuture = taskScheduler.schedule(commitRunnable, ZonedDateTime.now().plus(commitDelay).toInstant())
  }
}
