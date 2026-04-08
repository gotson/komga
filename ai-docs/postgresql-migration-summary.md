# PostgreSQL Migration - Sprint 1 Summary

## Mục tiêu
Implement PostgreSQL database support cho Komga while maintaining backward compatibility với SQLite.

## Kiến trúc đã triển khai

### 1. Database Abstraction Layer
- **DatabaseType enum**: `SQLITE`, `POSTGRESQL`
- **DatabaseUdfProvider interface**: Abstract UDF/collation functions
  - `SqliteUdfProvider`: SQLite implementation (REGEXP, UDF_STRIP_ACCENTS, COLLATION_UNICODE_3)
  - `PostgresUdfProvider`: PostgreSQL implementation (pg_trgm, unaccent extension)
- **JooqUdfHelper**: Spring component cung cấp database-agnostic extension methods

### 2. Cấu hình DataSource
- **DataSourcesConfiguration**: Tạo DataSource dựa trên database type
- **Dynamic JOOQ dialect**: `KomgaJooqConfiguration` sử dụng `SQLDialect` động
- **Flyway vendor detection**: Tự động sử dụng `{vendor}` directory (sqlite/postgresql)

### 3. Migration Strategy
- **Two-phase bean registration**: Sprint 1 tập trung infrastructure, UDF implementations có thể stub
- **Backward compatibility**: SQLite vẫn là default
- **Tasks database**: Giữ nguyên SQLite cho tasks database (đơn giản hóa Sprint 1)

## Các file đã tạo/sửa

### Created:
- `komga/src/flyway/resources/db/migration/postgresql/V20200706141854__initial_migration.sql`
- `komga/src/main/kotlin/org/gotson/komga/infrastructure/datasource/DatabaseType.kt`
- `komga/src/main/kotlin/org/gotson/komga/infrastructure/datasource/DatabaseUdfProvider.kt`
- `komga/src/main/kotlin/org/gotson/komga/infrastructure/datasource/DatabaseUdfProviderConfiguration.kt`
- `komga/src/main/kotlin/org/gotson/komga/infrastructure/datasource/PostgresUdfProvider.kt`
- `komga/src/main/kotlin/org/gotson/komga/infrastructure/datasource/SqliteUdfProvider.kt`
- `komga/src/main/kotlin/org/gotson/komga/infrastructure/jooq/JooqUdfHelper.kt`

### Modified DAO classes:
- `BookDtoDao.kt`: Added `jooqUdfHelper` constructor parameter, updated sorts map
- `SeriesDtoDao.kt`: Added `jooqUdfHelper` constructor parameter, updated sorts map  
- `ReadListDao.kt`: Added `jooqUdfHelper` constructor parameter, updated sorts map
- `SeriesCollectionDao.kt`: Added `jooqUdfHelper` constructor parameter, updated sorts map
- `ReferentialDao.kt`: Updated all 28 references to use `jooqUdfHelper`

### Helper classes:
- `SeriesSearchHelper.kt`: Added `jooqUdfHelper` parameter, updated UDF references
- `BookSearchHelper.kt`: Added `jooqUdfHelper` parameter, updated UDF references

### Configuration:
- `KomgaJooqConfiguration.kt`: Updated to use dynamic SQLDialect
- `DataSourcesConfiguration.kt`: Already had PostgreSQL support
- `KomgaProperties.kt`: Already had database type/URL fields

## Cấu hình PostgreSQL

### Application Properties:
```yaml
komga:
  database:
    type: postgresql
    url: jdbc:postgresql://localhost:5432/komga
    username: komga
    password: komga123
  tasks-db:
    file: ${komga.config-dir}/tasks.sqlite  # Vẫn dùng SQLite cho tasks
```

### PostgreSQL Extensions cần thiết:
```sql
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE EXTENSION IF NOT EXISTS "pg_trgm";     -- For text search/pattern matching
CREATE EXTENSION IF NOT EXISTS "unaccent";    -- For accent removal (similar to UDF_STRIP_ACCENTS)
```

## Docker Setup

### docker-compose.yml:
```yaml
services:
  postgres:
    image: postgres:16-alpine
    ports: ["5433:5432"]  # Port 5433 để tránh conflict với local PostgreSQL
    environment:
      POSTGRES_DB: komga
      POSTGRES_USER: komga
      POSTGRES_PASSWORD: komga123
    volumes:
      - ./docker/postgres/init.sql:/docker-entrypoint-initdb.d/init.sql
```

### Scripts:
- `run-local-with-postgres.sh`: Chạy backend với PostgreSQL
- `run-test-with-docker.sh`: Chạy tests với Testcontainers PostgreSQL
- `test-postgresql.sh`: Script test tổng quát

## Testing

### Integration Test:
```kotlin
@Testcontainers
@SpringBootTest
@ActiveProfiles("test")
class PostgreSQLIntegrationTest {
    @Container
    val postgres = PostgreSQLContainer("postgres:16-alpine")
    
    @Test
    fun `should connect to PostgreSQL database`() {
        // Test database connection
    }
    
    @Test  
    fun `should use PostgreSQL UDF provider`() {
        // Test UDF provider selection
    }
}
```

## UDF/Collation Mapping

### SQLite → PostgreSQL:
- `REGEXP` → PostgreSQL regex operators (`~`, `~*`)
- `UDF_STRIP_ACCENTS` → `unaccent()` function
- `COLLATION_UNICODE_3` → `COLLATE "C"` hoặc custom collation

### JooqUdfHelper methods:
```kotlin
class JooqUdfHelper(
    private val databaseUdfProvider: DatabaseUdfProvider
) {
    fun <T> Field<T>.collateUnicode3(): Field<T> = 
        databaseUdfProvider.collateUnicode3(this)
    
    fun Field<String>.stripAccents(): Field<String> =
        databaseUdfProvider.stripAccents(this)
    
    fun Field<String>.likeRegex(pattern: String): Condition =
        databaseUdfProvider.likeRegex(this, pattern)
}
```

## Các bước tiếp theo (Sprint 2)

1. **Complete UDF implementations**: Hoàn thiện `PostgresUdfProvider` implementations
2. **JOOQ code generation**: Tạo JOOQ code cho PostgreSQL schema
3. **Comprehensive testing**: Test tất cả API endpoints với PostgreSQL
4. **Performance optimization**: Tối ưu queries cho PostgreSQL
5. **Documentation**: Hướng dẫn migration từ SQLite sang PostgreSQL

## Status hiện tại
✅ **Build thành công** với SQLite  
✅ **Backend khởi động** và chạy Flyway migrations  
✅ **PostgreSQL infrastructure** đã sẵn sàng  
✅ **Docker setup** hoàn chỉnh  
✅ **Migration case sensitivity fixes** đã áp dụng  
⏳ **Integration tests** cần chạy với Testcontainers

## Migration Case Sensitivity Fixes (2026-04-08)

### Vấn đề
PostgreSQL migrations có case sensitivity issues:
- V001 tạo tables với quoted identifiers (`"TABLE_NAME"`) - case-sensitive
- Older migrations reference tables without quotes (`TABLE_NAME`) - case-insensitive
- PostgreSQL treats `"TABLE_NAME"` và `TABLE_NAME` (becomes `table_name`) là khác nhau

### Giải pháp đã áp dụng

#### 1. Fixed Java/Kotlin Migrations (5 files):
- `V20200810154730__thumbnails_part_2.kt`: Added column check, fixed quotes
- `V20200820150923__metadata_fields_part_2.kt`: Added column check, fixed quotes  
- `V20210624165023__missing_series_metadata.kt`: Fixed quotes
- `V20230801104436__fix_incorrect_language_codes.kt`: Added column check, fixed quotes
- `V20240422132621__fix_read_progress_locators.kt`: Added column check, fixed quotes

**Changes made:**
- Sử dụng quoted identifiers (`"TABLE_NAME"`, `"COLUMN_NAME"`)
- Thêm column existence checks cho migrations reference columns không có trong V001
- Fixed result set column name access (lowercase khi dùng quoted identifiers)

#### 2. Created PostgreSQL SQL Migrations (2 files):
- `V20200730135746__image_dimension.sql`: New PostgreSQL version với quoted identifiers
- `V20220106143755__page_file_size.sql`: New PostgreSQL version với quoted identifiers

#### 3. V001 giữ nguyên với quoted identifiers:
- Cần thiết cho reserved keyword `USER`
- Tất cả tables được tạo với `"TABLE_NAME"` format

### Kết quả
✅ **All 8 PostgreSQL migrations apply successfully**  
✅ **Application starts on port 25600**  
✅ **Database schema created correctly**  
✅ **Services run with docker-compose.local.yml**

## Lưu ý quan trọng
- **Backward compatibility**: SQLite vẫn là default database
- **Tasks database**: Vẫn dùng SQLite cho đơn giản
- **Flyway**: Tự động detect vendor và sử dụng migration directory phù hợp
- **JOOQ**: Runtime dialect dynamic, code generation vẫn dùng SQLite (Sprint 2 sẽ update)
- **Case sensitivity**: Tất cả PostgreSQL migrations giờ sử dụng quoted identifiers nhất quán với V001