# PostgreSQL Migration Task Tracking

## Overview
Tracking progress against plan: `.kilo/plans/1775535760568-brave-panda.md`

## Sprint 1: Cơ sở hạ tầng và cấu hình (Tuần 1)

### ✅ 1.1. Thêm dependency PostgreSQL
- **Status**: COMPLETED
- **Details**: Added `flyway-database-postgresql` dependency to `build.gradle.kts`
- **Files**: `komga/build.gradle.kts`
- **Notes**: Used `flyway-database-postgresql:11.7.2` instead of `org.postgresql:postgresql` for Flyway support

### ✅ 1.2. Mở rộng KomgaProperties.Database
- **Status**: COMPLETED
- **Details**: DatabaseType enum already existed in `DatabaseType.kt`
- **Files**: `komga/src/main/kotlin/org/gotson/komga/infrastructure/datasource/DatabaseType.kt`
- **Notes**: DatabaseType enum with SQLITE/POSTGRESQL already existed

### ✅ 1.3. Cập nhật DataSourcesConfiguration
- **Status**: COMPLETED
- **Details**: DataSourcesConfiguration already had PostgreSQL support
- **Files**: `komga/src/main/kotlin/org/gotson/komga/infrastructure/datasource/DataSourcesConfiguration.kt`
- **Notes**: Configuration already handles both SQLite and PostgreSQL

### ✅ 1.4. Tạo abstract UDF/Collation provider
- **Status**: COMPLETED
- **Details**: Created `DatabaseUdfProvider` interface with SQLite/PostgreSQL implementations
- **Files**: 
  - `komga/src/main/kotlin/org/gotson/komga/infrastructure/datasource/DatabaseUdfProvider.kt`
  - `komga/src/main/kotlin/org/gotson/komga/infrastructure/datasource/SqliteUdfProvider.kt`
  - `komga/src/main/kotlin/org/gotson/komga/infrastructure/datasource/PostgresUdfProvider.kt`
  - `komga/src/main/kotlin/org/gotson/komga/infrastructure/datasource/DatabaseUdfProviderConfiguration.kt`
- **Notes**: PostgresUdfProvider implementations are currently stubbed

### ✅ 1.5. Cập nhật KomgaJooqConfiguration
- **Status**: COMPLETED
- **Details**: Updated to use dynamic SQLDialect based on database type
- **Files**: `komga/src/main/kotlin/org/gotson/komga/infrastructure/jooq/KomgaJooqConfiguration.kt`
- **Notes**: Dialect set dynamically at runtime

### ✅ 1.6. Cập nhật cấu hình Flyway
- **Status**: COMPLETED
- **Details**: Flyway already uses `{vendor}` placeholder, created PostgreSQL migration directory
- **Files**: `komga/src/flyway/resources/db/migration/postgresql/V20200706141854__initial_migration.sql`
- **Notes**: Only initial migration created, need to convert all 91 SQLite migrations

### ✅ 1.7. Integration testing setup
- **Status**: COMPLETED
- **Details**: Created Testcontainers PostgreSQL integration test
- **Files**: 
  - `komga/src/test/kotlin/org/gotson/komga/infrastructure/datasource/PostgreSQLIntegrationTest.kt`
  - `docker-compose-test.yml`
  - `test-postgresql.sh`
- **Notes**: Test setup complete but not fully verified

### ❌ 1.8. Python API test script
- **Status**: NOT STARTED
- **Details**: Python script to test API endpoints
- **Files**: Not created
- **Notes**: Could be useful but not critical for Sprint 1

### ⚠️ 1.9. Backward compatibility verification
- **Status**: PARTIAL
- **Details**: SQLite backend runs on port 25600, but PostgreSQL connection has timeout issue
- **Files**: `application.yml`, `run-local-with-postgres.sh`
- **Notes**: SQLite works, PostgreSQL connection needs fixing

## Sprint 2: Migration database và JOOQ generation (Tuần 2)

### ❌ 2.1. Tạo thư mục migration PostgreSQL
- **Status**: STARTED
- **Details**: Created directory but only initial migration
- **Files**: `komga/src/flyway/resources/db/migration/postgresql/`
- **Notes**: Need to convert all 91 SQLite migrations

### ❌ 2.2. Chuyển đổi migration scripts
- **Status**: NOT STARTED
- **Details**: Need to convert all SQLite migrations to PostgreSQL
- **Files**: All 91 migration files need conversion
- **Notes**: Major task requiring careful data type mapping

### ❌ 2.3. Cập nhật build.gradle.kts cho JOOQ generation
- **Status**: NOT STARTED
- **Details**: JOOQ generation still hardcoded to SQLite
- **Files**: `build.gradle.kts`
- **Notes**: For Sprint 1, only runtime dialect is dynamic

### ❌ 2.4. Cập nhật code generation workflow
- **Status**: NOT STARTED
- **Details**: JOOQ code generation workflow needs updating
- **Files**: Build configuration
- **Notes**: Can be deferred to Sprint 2

### ✅ 2.5. Cập nhật tasks database
- **Status**: COMPLETED
- **Details**: Decision made to keep tasks database as SQLite
- **Files**: Not applicable
- **Notes**: For simplicity in Sprint 1, tasks database remains SQLite

## Sprint 3: Cập nhật code DAO và testing (Tuần 3)

### ✅ 3.1. Thay thế sử dụng UDF/collation trong DAO
- **Status**: COMPLETED
- **Details**: Updated all DAO classes to use `JooqUdfHelper`
- **Files**:
  - `komga/src/main/kotlin/org/gotson/komga/infrastructure/jooq/main/BookDtoDao.kt`
  - `komga/src/main/kotlin/org/gotson/komga/infrastructure/jooq/main/SeriesDtoDao.kt`
  - `komga/src/main/kotlin/org/gotson/komga/infrastructure/jooq/main/ReadListDao.kt`
  - `komga/src/main/kotlin/org/gotson/komga/infrastructure/jooq/main/SeriesCollectionDao.kt`
  - `komga/src/main/kotlin/org/gotson/komga/infrastructure/jooq/main/ReferentialDao.kt`
  - `komga/src/main/kotlin/org/gotson/komga/infrastructure/jooq/SeriesSearchHelper.kt`
  - `komga/src/main/kotlin/org/gotson/komga/infrastructure/jooq/BookSearchHelper.kt`
  - `komga/src/main/kotlin/org/gotson/komga/infrastructure/jooq/JooqUdfHelper.kt`

### ⚠️ 3.2. Xử lý REGEXP trong queries
- **Status**: PARTIAL
- **Details**: REGEXP handling moved to `JooqUdfHelper` but PostgreSQL implementation stubbed
- **Files**: `PostgresUdfProvider.kt`
- **Notes**: Need to implement PostgreSQL regexp function using `~*` operator

### ✅ 3.3. Cập nhật SqliteUdfDataSource
- **Status**: COMPLETED
- **Details**: Created `DatabaseUdfProvider` abstraction layer
- **Files**: `DatabaseUdfProvider.kt`, `SqliteUdfProvider.kt`, `PostgresUdfProvider.kt`
- **Notes**: Old `SqliteUdfDataSource` references removed from DAOs

### ❌ 3.4. Testing
- **Status**: PARTIAL
- **Details**: Integration test created but not fully verified
- **Files**: `PostgreSQLIntegrationTest.kt`
- **Notes**: Need to run tests with Testcontainers

### ✅ 3.5. Documentation
- **Status**: COMPLETED
- **Details**: Created documentation files
- **Files**: 
  - `ai-docs/postgresql-migration-summary.md`
  - `ai-docs/docker-setup.md`
- **Notes**: Good documentation coverage

### ❌ 3.6. Migration tool cho dữ liệu hiện có
- **Status**: NOT STARTED
- **Details**: Data migration tool not needed for Sprint 1
- **Files**: Not applicable
- **Notes**: Can be developed later if needed

## Critical Issues Blocking Progress

### ⚠️ PostgreSQL Connection Issue
- **Problem**: Backend timeout when connecting to PostgreSQL
- **Status**: NEEDS FIXING
- **Impact**: Blocks Sprint 1 completion
- **Files**: `application.yml`, connection configuration

### ⚠️ PostgresUdfProvider Implementations
- **Problem**: UDF/collation implementations are stubbed
- **Status**: NEEDS COMPLETION
- **Impact**: PostgreSQL queries won't work correctly
- **Files**: `PostgresUdfProvider.kt`

### ❌ Migration Conversion
- **Problem**: 91 SQLite migrations need PostgreSQL equivalents
- **Status**: MAJOR TASK REMAINING
- **Impact**: Blocks Sprint 2 progress
- **Files**: All migration files

## Summary

### Sprint 1 Progress: 70% Complete
- ✅ Infrastructure and configuration mostly done
- ✅ DAO migration completed
- ✅ Docker setup created
- ⚠️ PostgreSQL connection issue needs fixing
- ⚠️ PostgresUdfProvider needs implementation
- ❌ Python API test script not created

### Sprint 2 Progress: 10% Complete
- ✅ Directory structure created
- ❌ Migration conversion not started (major task)
- ❌ JOOQ generation not updated
- ✅ Tasks database decision made

### Sprint 3 Progress: 60% Complete
- ✅ DAO updates completed
- ⚠️ REGEXP handling partially done
- ✅ Documentation created
- ❌ Testing needs completion

## Next Priority Tasks
1. Fix PostgreSQL connection timeout issue
2. Complete PostgresUdfProvider implementations
3. Run integration tests with Testcontainers
4. Start migration conversion (Sprint 2)

## Files Created/Modified Summary

### Created:
- `komga/src/flyway/resources/db/migration/postgresql/V20200706141854__initial_migration.sql`
- `komga/src/main/kotlin/org/gotson/komga/infrastructure/datasource/DatabaseType.kt`
- `komga/src/main/kotlin/org/gotson/komga/infrastructure/datasource/DatabaseUdfProvider.kt`
- `komga/src/main/kotlin/org/gotson/komga/infrastructure/datasource/DatabaseUdfProviderConfiguration.kt`
- `komga/src/main/kotlin/org/gotson/komga/infrastructure/datasource/PostgresUdfProvider.kt`
- `komga/src/main/kotlin/org/gotson/komga/infrastructure/datasource/SqliteUdfProvider.kt`
- `komga/src/main/kotlin/org/gotson/komga/infrastructure/jooq/JooqUdfHelper.kt`
- `docker-compose.yml`
- `docker-compose-test.yml`
- `docker/postgres/init.sql`
- `run-local-with-postgres.sh`
- `run-test-with-docker.sh`
- `test-postgresql.sh`
- `test-postgres-connection.sh`
- `ai-docs/postgresql-migration-summary.md`
- `ai-docs/docker-setup.md`

### Modified:
- `komga/build.gradle.kts` (added `flyway-database-postgresql` dependency)
- `komga/src/main/kotlin/org/gotson/komga/infrastructure/jooq/KomgaJooqConfiguration.kt` (dynamic SQLDialect)
- `komga/src/main/kotlin/org/gotson/komga/infrastructure/jooq/main/BookDtoDao.kt`
- `komga/src/main/kotlin/org/gotson/komga/infrastructure/jooq/main/SeriesDtoDao.kt`
- `komga/src/main/kotlin/org/gotson/komga/infrastructure/jooq/main/ReadListDao.kt`
- `komga/src/main/kotlin/org/gotson/komga/infrastructure/jooq/main/SeriesCollectionDao.kt`
- `komga/src/main/kotlin/org/gotson/komga/infrastructure/jooq/main/ReferentialDao.kt`
- `komga/src/main/kotlin/org/gotson/komga/infrastructure/jooq/SeriesSearchHelper.kt`
- `komga/src/main/kotlin/org/gotson/komga/infrastructure/jooq/BookSearchHelper.kt`
- `komga/src/main/resources/application.yml` (fixed template issues, added port 25600)
- `komga/src/test/kotlin/org/gotson/komga/infrastructure/datasource/PostgreSQLIntegrationTest.kt`
- `komga/src/test/resources/application-postgresql-test.yml` (fixed template issues)

Last updated: 2026-04-07T14:05:54+07:00