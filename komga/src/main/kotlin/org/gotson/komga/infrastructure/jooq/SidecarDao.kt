package org.gotson.komga.infrastructure.jooq

import org.gotson.komga.domain.model.Sidecar
import org.gotson.komga.domain.model.SidecarStored
import org.gotson.komga.domain.persistence.SidecarRepository
import org.gotson.komga.jooq.Tables
import org.gotson.komga.jooq.tables.records.SidecarRecord
import org.jooq.DSLContext
import org.jooq.impl.DSL
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.net.URL

@Component
class SidecarDao(
  private val dsl: DSLContext,
  @Value("#{@komgaProperties.database.batchChunkSize}") private val batchSize: Int,
) : SidecarRepository {
  private val sc = Tables.SIDECAR
  private val l = Tables.LIBRARY

  override fun findAll(): Collection<SidecarStored> =
    dsl.selectFrom(sc).fetch().map { it.toDomain() }

  override fun save(libraryId: String, sidecar: Sidecar) {
    dsl.insertInto(sc)
      .values(
        sidecar.url.toString(),
        sidecar.parentUrl.toString(),
        sidecar.lastModifiedTime,
        libraryId,
      )
      .onDuplicateKeyUpdate()
      .set(sc.LAST_MODIFIED_TIME, sidecar.lastModifiedTime)
      .set(sc.PARENT_URL, sidecar.parentUrl.toString())
      .set(sc.LIBRARY_ID, libraryId)
      .execute()
  }

  @Transactional
  override fun deleteByLibraryIdAndUrls(libraryId: String, urls: Collection<URL>) {
    dsl.insertTempStrings(batchSize, urls.map { it.toString() })

    dsl.deleteFrom(sc)
      .where(sc.LIBRARY_ID.eq(libraryId))
      .and(sc.URL.`in`(dsl.selectTempStrings()))
      .execute()
  }

  override fun deleteByLibraryId(libraryId: String) {
    dsl.deleteFrom(sc)
      .where(sc.LIBRARY_ID.eq(libraryId))
      .execute()
  }

  override fun countGroupedByLibraryName(): Map<String, Int> =
    dsl.select(l.NAME, DSL.count(sc.URL))
      .from(l)
      .leftJoin(sc).on(l.ID.eq(sc.LIBRARY_ID))
      .groupBy(l.NAME)
      .fetchMap(l.NAME, DSL.count(sc.URL))

  private fun SidecarRecord.toDomain() =
    SidecarStored(
      url = URL(url),
      parentUrl = URL(parentUrl),
      lastModifiedTime = lastModifiedTime,
      libraryId = libraryId,
    )
}
