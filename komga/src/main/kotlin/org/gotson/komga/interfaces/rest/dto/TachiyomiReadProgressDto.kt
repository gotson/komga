package org.gotson.komga.interfaces.rest.dto

data class TachiyomiReadProgressDto(
  val booksCount: Int,
  val booksReadCount: Int,
  val booksUnreadCount: Int,
  val booksInProgressCount: Int,
  val lastReadContinuousIndex: Int,
)
