package org.gotson.komga.infrastructure.jooq.tasks

import com.fasterxml.jackson.databind.ObjectMapper
import io.github.oshai.kotlinlogging.KotlinLogging
import org.gotson.komga.application.tasks.Task
import org.gotson.komga.application.tasks.TasksRepository
import org.gotson.komga.jooq.tasks.Tables
import org.jooq.DSLContext
import org.jooq.Query
import org.jooq.Record2
import org.jooq.impl.DSL
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.DependsOn
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.time.ZoneId

private val logger = KotlinLogging.logger {}

@Component
@DependsOn("flywaySecondaryMigrationInitializer")
class TasksDao(
  @Qualifier("tasksDslContextRW") private val dslRW: DSLContext,
  @Qualifier("tasksDslContextRO") private val dslRO: DSLContext,
  @param:Value("#{@komgaProperties.tasksDb.batchChunkSize}") private val batchSize: Int,
  private val objectMapper: ObjectMapper,
) : TasksRepository {
  private val t = Tables.TASK

  private val tasksAvailableCondition =
    t.OWNER.isNull
      .and(
        t.GROUP_ID
          .notIn(
            DSL
              .select(t.GROUP_ID)
              .from(t)
              .where(t.OWNER.isNotNull)
              .and(t.GROUP_ID.isNotNull),
          ).or(t.GROUP_ID.isNull),
      )

  override fun hasAvailable(): Boolean =
    dslRO.fetchExists(
      t,
      tasksAvailableCondition,
    )

  @Transactional
  override fun takeFirst(owner: String): Task? {
    val task =
      dslRW
        .selectBase()
        .where(tasksAvailableCondition)
        .orderBy(t.PRIORITY.desc(), t.LAST_MODIFIED_DATE)
        .limit(1)
        .fetchOne()
        ?.let {
          try {
            objectMapper.readValue(it.value2(), Class.forName(it.value1())) as Task
          } catch (e: Exception) {
            logger.error(e) { "Could not deserialize object of type: ${it.value1()}" }
            null
          }
        } ?: return null

    dslRW
      .update(t)
      .set(t.OWNER, owner)
      .where(t.ID.eq(task.uniqueId))
      .execute()

    return task
  }

  override fun findAll(): List<Task> =
    dslRO
      .selectBase()
      .fetch()
      .mapNotNull { it.toDomain() }

  override fun findAllGroupedByOwner(): Map<String?, List<Task>> =
    dslRO
      .select(t.OWNER, t.CLASS, t.PAYLOAD)
      .from(t)
      .fetch()
      .mapNotNull {
        it.into(t.CLASS, t.PAYLOAD).toDomain()?.let { task -> it.value1() to task }
      }.groupBy({ it.first }, { it.second })

  private fun DSLContext.selectBase() =
    this
      .select(t.CLASS, t.PAYLOAD)
      .from(t)

  private fun Record2<String, String>.toDomain(): Task? =
    try {
      objectMapper.readValue(value2(), Class.forName(value1())) as Task
    } catch (e: Exception) {
      logger.error(e) { "Could not deserialize object of type: ${value1()}" }
      null
    }

  override fun count(): Int = dslRO.fetchCount(t)

  override fun countBySimpleType(): Map<String, Int> =
    dslRO
      .select(t.SIMPLE_TYPE, DSL.count(t.SIMPLE_TYPE))
      .from(t)
      .groupBy(t.SIMPLE_TYPE)
      .fetch()
      .associate { it.value1() to it.value2() }

  override fun save(task: Task) {
    task.toQuery(dslRW).execute()
  }

  override fun save(tasks: Collection<Task>) {
    tasks
      .map { it.toQuery(dslRW) }
      .chunked(batchSize)
      .forEach { chunk -> dslRW.batch(chunk).execute() }
  }

  override fun disown(): Int =
    dslRW
      .update(t)
      .set(t.OWNER, null as String?)
      .where(t.OWNER.isNotNull)
      .execute()

  override fun delete(taskId: String) {
    dslRW.deleteFrom(t).where(t.ID.eq(taskId)).execute()
  }

  override fun deleteAll() {
    dslRW.deleteFrom(t).execute()
  }

  override fun deleteAllWithoutOwner(): Int = dslRW.deleteFrom(t).where(t.OWNER.isNull).execute()

  private fun Task.toQuery(dsl: DSLContext): Query =
    dsl
      .insertInto(
        t,
        t.ID,
        t.PRIORITY,
        t.GROUP_ID,
        t.CLASS,
        t.SIMPLE_TYPE,
        t.PAYLOAD,
      ).values(
        uniqueId,
        priority,
        groupId,
        javaClass.typeName,
        javaClass.simpleName,
        objectMapper.writeValueAsString(this),
      ).onDuplicateKeyUpdate()
      .set(t.GROUP_ID, groupId)
      .set(t.PRIORITY, priority)
      .set(t.CLASS, javaClass.typeName)
      .set(t.SIMPLE_TYPE, javaClass.simpleName)
      .set(t.PAYLOAD, objectMapper.writeValueAsString(this))
      .set(t.LAST_MODIFIED_DATE, LocalDateTime.now(ZoneId.of("Z")))
}
