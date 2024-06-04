package org.gotson.komga.domain.model

interface MediaExtension

data class MediaExtensionEpub(
  val toc: List<EpubTocEntry> = emptyList(),
  val landmarks: List<EpubTocEntry> = emptyList(),
  val pageList: List<EpubTocEntry> = emptyList(),
) : MediaExtension
