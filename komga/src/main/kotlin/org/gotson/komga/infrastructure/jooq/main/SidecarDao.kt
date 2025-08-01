package org.gotson.komga.infrastructure.jooq.main

import org.gotson.komga.domain.model.Sidecar
import org.gotson.komga.domain.model.SidecarStored
import org.gotson.komga.domain.persistence.SidecarRepository
import org.gotson.komga.infrastructure.jooq.TempTable.Companion.withTempTable
import org.gotson.komga.jooq.main.Tables
import org.gotson.komga.jooq.main.tables.records.SidecarRecord
import org.jooq.DSLContext
import org.jooq.impl.DSL
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.net.URL

@Component
class SidecarDao(
  private val dslRW: DSLContext,
  @Qualifier("dslContextRO") private val dslRO: DSLContext,
  @param:Value("#{@komgaProperties.database.batchChunkSize}") private val batchSize: Int,
) : SidecarRepository {
  private val sc = Tables.SIDECAR

  override fun findAll(): Collection<SidecarStored> = dslRO.selectFrom(sc).fetch().map { it.toDomain() }

  override fun save(
    libraryId: String,
    sidecar: Sidecar,
  ) {
    dslRW
      .insertInto(sc)
      .values(
        sidecar.url.toString(),
        sidecar.parentUrl.toString(),
        sidecar.lastModifiedTime,
        libraryId,
      ).onDuplicateKeyUpdate()
      .set(sc.LAST_MODIFIED_TIME, sidecar.lastModifiedTime)
      .set(sc.PARENT_URL, sidecar.parentUrl.toString())
      .set(sc.LIBRARY_ID, libraryId)
      .execute()
  }

  @Transactional
  override fun deleteByLibraryIdAndUrls(
    libraryId: String,
    urls: Collection<URL>,
  ) {
    dslRW.withTempTable(batchSize, urls.map { it.toString() }).use {
      dslRW
        .deleteFrom(sc)
        .where(sc.LIBRARY_ID.eq(libraryId))
        .and(sc.URL.`in`(it.selectTempStrings()))
        .execute()
    }
  }

  override fun deleteByLibraryId(libraryId: String) {
    dslRW
      .deleteFrom(sc)
      .where(sc.LIBRARY_ID.eq(libraryId))
      .execute()
  }

  override fun countGroupedByLibraryId(): Map<String, Int> =
    dslRO
      .select(sc.LIBRARY_ID, DSL.count(sc.URL))
      .from(sc)
      .groupBy(sc.LIBRARY_ID)
      .fetchMap(sc.LIBRARY_ID, DSL.count(sc.URL))

  private fun SidecarRecord.toDomain() =
    SidecarStored(
      url = URL(url),
      parentUrl = URL(parentUrl),
      lastModifiedTime = lastModifiedTime,
      libraryId = libraryId,
    )
}
