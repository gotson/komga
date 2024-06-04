package org.gotson.komga.domain.model

data class EpubTocEntry(
  val title: String,
  val href: String?,
  val children: List<EpubTocEntry> = emptyList(),
)
