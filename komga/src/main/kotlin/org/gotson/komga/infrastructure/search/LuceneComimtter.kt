package org.gotson.komga.infrastructure.search

interface LuceneCommitter {
  fun commitAndMaybeRefresh()
}
