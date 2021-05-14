package org.gotson.komga.interfaces.rest.persistence

import org.gotson.komga.interfaces.rest.dto.TachiyomiReadProgressDto

interface ReadProgressDtoRepository {
  fun getProgressBySeries(seriesId: String, userId: String,): TachiyomiReadProgressDto
  fun getProgressByReadList(readListId: String, userId: String,): TachiyomiReadProgressDto
}
