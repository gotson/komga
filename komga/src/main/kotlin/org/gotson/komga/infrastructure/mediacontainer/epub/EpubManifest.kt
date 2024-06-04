package org.gotson.komga.infrastructure.mediacontainer.epub

import org.gotson.komga.domain.model.EpubTocEntry
import org.gotson.komga.domain.model.MediaFile

data class EpubManifest(
  val resources: List<MediaFile>,
  val toc: List<EpubTocEntry>,
  val landmarks: List<EpubTocEntry>,
  val pageList: List<EpubTocEntry>,
  val pageCount: Int,
)
