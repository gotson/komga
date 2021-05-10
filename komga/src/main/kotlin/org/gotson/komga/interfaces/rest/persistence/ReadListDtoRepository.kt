package org.gotson.komga.interfaces.rest.persistence

import org.gotson.komga.interfaces.rest.dto.ReadListProgressDto

interface ReadListDtoRepository {
  fun getProgress(readListId: String, userId: String,): ReadListProgressDto
}
