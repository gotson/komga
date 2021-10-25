package org.gotson.komga.interfaces.rest.persistence

import org.gotson.komga.interfaces.rest.dto.TachiyomiReadProgressDto
import org.gotson.komga.interfaces.rest.dto.TachiyomiReadProgressV2Dto

interface ReadProgressDtoRepository {
  fun findProgressBySeries(seriesId: String, userId: String,): TachiyomiReadProgressDto
  fun findProgressV2BySeries(seriesId: String, userId: String,): TachiyomiReadProgressV2Dto
  fun findProgressByReadList(readListId: String, userId: String,): TachiyomiReadProgressDto
}
