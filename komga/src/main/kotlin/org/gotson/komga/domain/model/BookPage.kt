package org.gotson.komga.domain.model

import java.io.Serializable

open class BookPage(
  val fileName: String,
  val mediaType: String,
  val dimension: Dimension? = null,
  val fileHash: String = "",
  val fileSize: Long? = null,
) : Serializable {
  override fun toString(): String = "BookPage(fileName='$fileName', mediaType='$mediaType', dimension=$dimension, fileHash='$fileHash', fileSize=$fileSize)"

  fun copy(
    fileName: String = this.fileName,
    mediaType: String = this.mediaType,
    dimension: Dimension? = this.dimension,
    fileHash: String = this.fileHash,
    fileSize: Long? = this.fileSize,
  ) = BookPage(
    fileName = fileName,
    mediaType = mediaType,
    dimension = dimension,
    fileHash = fileHash,
    fileSize = fileSize,
  )
}

fun Collection<BookPage>.restoreHashFrom(restoreFrom: Collection<BookPage>): List<BookPage> =
  this.map { newPage ->
    restoreFrom.find {
      it.fileSize == newPage.fileSize &&
        it.mediaType == newPage.mediaType &&
        it.fileName == newPage.fileName &&
        it.fileHash.isNotBlank()
    }?.let { newPage.copy(fileHash = it.fileHash) }
      ?: newPage
  }
