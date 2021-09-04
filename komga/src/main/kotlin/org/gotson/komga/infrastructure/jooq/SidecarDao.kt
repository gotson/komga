package org.gotson.komga.infrastructure.jooq

import org.gotson.komga.domain.model.Sidecar
import org.gotson.komga.domain.model.SidecarStored
import org.gotson.komga.domain.persistence.SidecarRepository
import org.gotson.komga.jooq.Tables
import org.gotson.komga.jooq.tables.records.SidecarRecord
import org.jooq.DSLContext
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.net.URL

@Component
class SidecarDao(
  private val dsl: DSLContext
) : SidecarRepository {

  private val sc = Tables.SIDECAR
  private val u = Tables.TEMP_URL_LIST

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
    // insert urls in a temporary table, else the select size can exceed the statement limit
    dsl.deleteFrom(u).execute()

    if (urls.isNotEmpty()) {
      dsl.batch(
        dsl.insertInto(u, u.URL).values(null as String?)
      ).also { step ->
        urls.forEach {
          step.bind(it.toString())
        }
      }.execute()
    }

    dsl.deleteFrom(sc)
      .where(sc.LIBRARY_ID.eq(libraryId))
      .and(sc.URL.`in`(dsl.select(u.URL).from(u)))
      .execute()
  }

  override fun deleteByLibraryId(libraryId: String) {
    dsl.deleteFrom(sc)
      .where(sc.LIBRARY_ID.eq(libraryId))
      .execute()
  }

  private fun SidecarRecord.toDomain() =
    SidecarStored(
      url = URL(url),
      parentUrl = URL(parentUrl),
      lastModifiedTime = lastModifiedTime,
      libraryId = libraryId,
    )
}
