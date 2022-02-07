package org.gotson.komga.domain.model

import java.io.Serializable

class BookPageNumbered(
  fileName: String,
  mediaType: String,
  dimension: Dimension? = null,
  fileHash: String = "",
  fileSize: Long? = null,
  val pageNumber: Int,
) : BookPage(
  fileName = fileName,
  mediaType = mediaType,
  dimension = dimension,
  fileHash = fileHash,
  fileSize = fileSize,
),
  Serializable {
  override fun toString(): String = "BookPageNumbered(fileName='$fileName', mediaType='$mediaType', dimension=$dimension, fileHash='$fileHash', fileSize=$fileSize, pageNumber=$pageNumber)"
}
