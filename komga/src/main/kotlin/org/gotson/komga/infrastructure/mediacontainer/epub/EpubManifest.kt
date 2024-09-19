package org.gotson.komga.infrastructure.mediacontainer.epub

import org.gotson.komga.domain.model.BookPage
import org.gotson.komga.domain.model.EpubTocEntry
import org.gotson.komga.domain.model.MediaFile
import org.gotson.komga.domain.model.R2Locator

data class EpubManifest(
  val resources: List<MediaFile>,
  val missingResources: List<MediaFile>,
  val toc: List<EpubTocEntry>,
  val landmarks: List<EpubTocEntry>,
  val pageList: List<EpubTocEntry>,
  val pageCount: Int,
  val isFixedLayout: Boolean,
  val positions: List<R2Locator>,
  val divinaPages: List<BookPage>,
  val isKepub: Boolean,
)
