package org.gotson.komga.interfaces.rest.persistence

import org.gotson.komga.interfaces.rest.dto.TachiyomiReadProgressDto

interface ReadProgressDtoRepository {
  fun findProgressBySeries(seriesId: String, userId: String,): TachiyomiReadProgressDto
  fun findProgressByReadList(readListId: String, userId: String,): TachiyomiReadProgressDto
}
