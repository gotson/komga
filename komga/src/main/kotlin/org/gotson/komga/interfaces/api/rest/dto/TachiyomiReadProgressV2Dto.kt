package org.gotson.komga.interfaces.api.rest.dto

data class TachiyomiReadProgressV2Dto(
  val booksCount: Int,
  val booksReadCount: Int,
  val booksUnreadCount: Int,
  val booksInProgressCount: Int,
  val lastReadContinuousNumberSort: Float,
  val maxNumberSort: Float,
)
