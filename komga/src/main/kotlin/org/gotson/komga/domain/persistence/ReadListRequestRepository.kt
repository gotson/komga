package org.gotson.komga.domain.persistence

import org.gotson.komga.domain.model.ReadListRequestBook
import org.gotson.komga.domain.model.ReadListRequestBookMatches

interface ReadListRequestRepository {
  fun matchBookRequests(requests: Collection<ReadListRequestBook>): Collection<ReadListRequestBookMatches>
}
