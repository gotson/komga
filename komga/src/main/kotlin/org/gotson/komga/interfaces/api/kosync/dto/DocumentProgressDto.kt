package org.gotson.komga.interfaces.api.kosync.dto

import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
data class DocumentProgressDto(
  /**
   * The document hash, computed using the KOReader partial MD5 algorithm.
   */
  val document: String,
  /**
   * Total progress percentage in the document, between 0 and 1.
   */
  val percentage: Float,
  /**
   * Current progress.
   *
   * For PDF and CBZ, contains the current page number (starting from 1).
   *
   * For EPUB, it can contain a position in the following forms
   * - '/body/DocFragment[10]/body/div/p[1]/text().0', where 10 correspond to the 1-based index of the resource in the manifest
   * - '#_doc_fragment_44_ c37' (after clicking in the TOC), where 44 correspond to the 0-based index of the resource in the manifest
   */
  val progress: String,
  val device: String,
  val deviceId: String,
)
