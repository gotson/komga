# Docker Setup for Komga with PostgreSQL

## Overview
Docker Compose setup để chạy Komga với PostgreSQL cho development và testing.

**Optimized Build**: Để build nhanh hơn với caching tốt hơn, sử dụng `Dockerfile.local.optimized` và `docker-compose.local.optimized.yml`.

## File Structure

### 1. docker-compose.yml (Production/Standard)
```yaml
version: '3.8'

services:
  postgres:
    image: postgres:16-alpine
    container_name: komga-postgres
    environment:
      POSTGRES_DB: komga
      POSTGRES_USER: komga
      POSTGRES_PASSWORD: komga123
    ports:
      - "5433:5432"  # Port 5433 để tránh conflict với local PostgreSQL
    volumes:
      - postgres_data:/var/lib/postgresql/data
      - ./docker/postgres/init.sql:/docker-entrypoint-initdb.d/init.sql
    healthcheck:
      test: ["CMD-SHELL", "pg_isready -U komga"]
      interval: 10s
      timeout: 5s
      retries: 5

  komga:
    build:
      context: .
      dockerfile: docker/Dockerfile
    container_name: komga-backend
    depends_on:
      postgres:
        condition: service_healthy
    environment:
      SPRING_PROFILES_ACTIVE: docker
      KOMGA_DATABASE_TYPE: postgresql
      KOMGA_DATABASE_URL: jdbc:postgresql://postgres:5432/komga
      KOMGA_DATABASE_USERNAME: komga
      KOMGA_DATABASE_PASSWORD: komga123
      KOMGA_CONFIG_DIR: /config
    ports:
      - "25600:25600"
    volumes:
      - komga_config:/config
      - ./data:/data:ro
    restart: unless-stopped

volumes:
  postgres_data:
  komga_config:
```

### 2. docker-compose.local.yml (Local Development với Build)
```yaml
version: '3.8'

services:
  postgres:
    image: postgres:15-alpine
    container_name: komga-postgres
    environment:
      POSTGRES_DB: komga
      POSTGRES_USER: komga
      POSTGRES_PASSWORD: komga123
    ports:
      - "5433:5432"
    volumes:
      - postgres_data:/var/lib/postgresql/data
      - ./docker/postgres/init.sql:/docker-entrypoint-initdb.d/01-init.sql
    healthcheck:
      test: [ "CMD-SHELL", "pg_isready -U komga" ]
      interval: 10s
      timeout: 5s
      retries: 5

  komga:
    build:
      context: .
      dockerfile: Dockerfile.local
    container_name: komga-backend
    depends_on:
      postgres:
        condition: service_healthy
    environment:
      SPRING_PROFILES_ACTIVE: docker
      KOMGA_DATABASE_TYPE: postgresql
      KOMGA_DATABASE_URL: jdbc:postgresql://postgres:5432/komga
      KOMGA_DATABASE_USERNAME: komga
      KOMGA_DATABASE_PASSWORD: komga123
      KOMGA_CONFIG_DIR: /config
      KOMGA_DATABASE_POOL_SIZE: 10
      KOMGA_DATABASE_MAX_POOL_SIZE: 10
      SPRING_FLYWAY_ENABLED: "true"
      SPRING_FLYWAY_BASELINE_ON_MIGRATE: "true"
      SPRING_FLYWAY_BASELINE_VERSION: "20250730173126"
    ports:
      - "25600:25600"
    volumes:
      - komga_config:/config
      - ./data:/data:ro
    restart: unless-stopped

volumes:
  postgres_data:
  komga_config:
```

**Lưu ý về Dockerfile.local:**
- Build từ source với `./gradlew :komga:prepareThymeLeaf :komga:bootJar`
- Có thể timeout do download Gradle distribution
- Migration fixes đã được áp dụng trong source code

### 2a. docker-compose.local.optimized.yml (Local Development với Build Optimized)
```yaml
version: '3.8'

services:
  postgres:
    image: postgres:15-alpine
    container_name: komga-postgres
    environment:
      POSTGRES_DB: komga
      POSTGRES_USER: komga
      POSTGRES_PASSWORD: komga123
    ports:
      - "5433:5432"
    volumes:
      - postgres_data:/var/lib/postgresql/data
      - ./docker/postgres/init.sql:/docker-entrypoint-initdb.d/01-init.sql
    healthcheck:
      test: [ "CMD-SHELL", "pg_isready -U komga" ]
      interval: 10s
      timeout: 5s
      retries: 5

  komga:
    build:
      context: .
      dockerfile: Dockerfile.local.optimized
    container_name: komga-backend
    depends_on:
      postgres:
        condition: service_healthy
    environment:
      SPRING_PROFILES_ACTIVE: docker
      KOMGA_DATABASE_TYPE: postgresql
      KOMGA_DATABASE_URL: jdbc:postgresql://postgres:5432/komga?connectTimeout=30000&socketTimeout=60000
      KOMGA_DATABASE_USERNAME: komga
      KOMGA_DATABASE_PASSWORD: komga123
      KOMGA_CONFIG_DIR: /config
      KOMGA_DATABASE_POOL_SIZE: 10
      KOMGA_DATABASE_MAX_POOL_SIZE: 10
      SPRING_FLYWAY_ENABLED: "true"
      SPRING_FLYWAY_BASELINE_ON_MIGRATE: "true"
      SPRING_FLYWAY_BASELINE_VERSION: "20250730173126"
    ports:
      - "25600:25600"
    volumes:
      - komga_config:/config
      - ./data:/data:ro
    restart: unless-stopped

volumes:
  postgres_data:
  komga_config:
```

**Lưu ý về Dockerfile.local.optimized:**
- Multi-stage build tách biệt frontend và backend để tối ưu caching
- Frontend (`npm install`, `npm run build`) được cache độc lập
- Thay đổi backend không trigger rebuild frontend
- Build nhanh hơn đáng kể cho incremental builds
- Sử dụng: `docker-compose -f docker-compose.local.optimized.yml up -d`

### 4. docker-compose-test.yml (cho testing)
```yaml
version: '3.8'

services:
  postgres-test:
    image: postgres:16-alpine
    container_name: komga-postgres-test
    environment:
      POSTGRES_DB: komga_test
      POSTGRES_USER: postgres
      POSTGRES_PASSWORD: postgres
    ports:
      - "5433:5432"
    volumes:
      - postgres_test_data:/var/lib/postgresql/data
      - ./docker/postgres/init.sql:/docker-entrypoint-initdb.d/init.sql
    healthcheck:
      test: ["CMD-SHELL", "pg_isready -U postgres"]
      interval: 10s
      timeout: 5s
      retries: 5
    command: ["postgres", "-c", "log_statement=all"]  # Log all SQL for debugging

volumes:
  postgres_test_data:
```

### 5. docker/postgres/init.sql
```sql
-- PostgreSQL initialization script for Komga
-- Creates necessary extensions and sets up database

-- Enable required extensions
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE EXTENSION IF NOT EXISTS "pg_trgm"; -- For text search/pattern matching
CREATE EXTENSION IF NOT EXISTS "unaccent"; -- For accent removal (similar to UDF_STRIP_ACCENTS)
```

## Environment Variables

### PostgreSQL Container:
- `POSTGRES_DB`: Database name (default: komga)
- `POSTGRES_USER`: Database user (default: komga)
- `POSTGRES_PASSWORD`: Database password (default: komga123)

### Komga Container:
- `SPRING_PROFILES_ACTIVE`: Spring profile (docker)
- `KOMGA_DATABASE_TYPE`: Database type (postgresql)
- `KOMGA_DATABASE_URL`: JDBC URL (jdbc:postgresql://postgres:5432/komga)
- `KOMGA_DATABASE_USERNAME`: Database username
- `KOMGA_DATABASE_PASSWORD`: Database password
- `KOMGA_CONFIG_DIR`: Configuration directory (/config)

## Usage Commands

### 1. Start PostgreSQL only:
```bash
docker-compose up -d postgres
```

### 2. Start full stack (PostgreSQL + Komga):
```bash
docker-compose up -d
```

### 3. Stop all services:
```bash
docker-compose down
```

### 4. Stop and remove volumes:
```bash
docker-compose down -v
```

### 5. View logs:
```bash
# PostgreSQL logs
docker-compose logs postgres

# Komga logs
docker-compose logs komga

# All logs
docker-compose logs -f
```

### 6. Access PostgreSQL:
```bash
# Connect via psql
docker-compose exec postgres psql -U komga -d komga

# Connect from host
psql -h localhost -p 5433 -U komga -d komga
```

## Scripts

### run-local-with-postgres.sh
```bash
#!/bin/bash
# Script to run Komga locally with PostgreSQL

set -e

echo "Starting PostgreSQL container..."
docker-compose up -d postgres

echo "Waiting for PostgreSQL to be ready..."
sleep 5

echo "Building Komga..."
./gradlew :komga:build -x test

echo "Running Komga with PostgreSQL..."
SPRING_PROFILES_ACTIVE=docker \
KOMGA_DATABASE_TYPE=postgresql \
KOMGA_DATABASE_URL="jdbc:postgresql://localhost:5433/komga" \
KOMGA_DATABASE_USERNAME=komga \
KOMGA_DATABASE_PASSWORD=komga123 \
KOMGA_CONFIG_DIR="$HOME/.komga-postgres" \
./gradlew :komga:bootRun

echo "Komga is running at http://localhost:25600"
echo "PostgreSQL is running at localhost:5433"
echo "To stop: docker-compose down"
```

### run-test-with-docker.sh
```bash
#!/bin/bash
# Script to run Komga tests with PostgreSQL using Docker Compose

set -e

echo "Starting PostgreSQL test container..."
docker-compose -f docker-compose-test.yml up -d postgres-test

echo "Waiting for PostgreSQL to be ready..."
sleep 10

echo "Building Komga..."
./gradlew :komga:build -x test

echo "Running tests with PostgreSQL..."
./gradlew :komga:test --tests "*PostgreSQL*" --info

echo "Running integration tests..."
./gradlew :komga:integrationTest --info

echo "Stopping test containers..."
docker-compose -f docker-compose-test.yml down

echo "Test completed!"
```

## Port Configuration

### Default Ports:
- **PostgreSQL**: 5433 (host) → 5432 (container)
- **Komga API**: 25600 (host) → 25600 (container)

### Change Ports:
Để thay đổi ports, sửa file `docker-compose.yml`:
```yaml
services:
  postgres:
    ports:
      - "NEW_PORT:5432"  # Thay NEW_PORT bằng port mong muốn
  
  komga:
    ports:
      - "NEW_PORT:25600"  # Thay NEW_PORT bằng port mong muốn
```

## Volume Persistence

### PostgreSQL Data:
- Volume: `postgres_data`
- Location: `/var/lib/postgresql/data` (container)
- Persists: Database data, tables, indexes

### Komga Configuration:
- Volume: `komga_config`
- Location: `/config` (container)
- Contains: Application configuration, logs, Lucene indexes

### Host Mounts:
- `./data:/data:ro`: Read-only comic/manga library directory
- `./docker/postgres/init.sql:/docker-entrypoint-initdb.d/init.sql`: Init script

## Health Checks

### PostgreSQL Health Check:
```yaml
healthcheck:
  test: ["CMD-SHELL", "pg_isready -U komga"]
  interval: 10s
  timeout: 5s
  retries: 5
```

### Dependency Management:
Komga service sẽ đợi PostgreSQL healthy trước khi start:
```yaml
depends_on:
  postgres:
    condition: service_healthy
```

## Troubleshooting

### 1. Port Already in Use:
```bash
# Check what's using port 5433
lsof -i :5433

# Kill process using port
kill -9 $(lsof -t -i:5433)
```

### 2. PostgreSQL Connection Issues:
```bash
# Check PostgreSQL logs
docker-compose logs postgres

# Test connection
docker-compose exec postgres pg_isready -U komga
```

### 3. Reset Database:
```bash
# Stop and remove volumes
docker-compose down -v

# Start fresh
docker-compose up -d
```

### 4. Backup Database:
```bash
# Backup to file
docker-compose exec postgres pg_dump -U komga komga > backup.sql

# Restore from file
cat backup.sql | docker-compose exec -T postgres psql -U komga -d komga
```

## Development Notes

### 1. Local Development vs Docker:
- **Local**: Chạy backend với `./gradlew :komga:bootRun`, PostgreSQL trong Docker
- **Docker**: Chạy cả backend và PostgreSQL trong Docker

### 2. Test Configuration:
- Test containers sử dụng `docker-compose-test.yml`
- Database name: `komga_test`
- User: `postgres` (default PostgreSQL user)

### 3. Extensions Required:
- `uuid-ossp`: UUID generation
- `pg_trgm`: Text search and pattern matching
- `unaccent`: Accent removal (thay thế UDF_STRIP_ACCENTS)

### 4. Performance Considerations:
- Adjust `shared_buffers` và `work_mem` trong production
- Consider connection pooling với PgBouncer
- Monitor với `pg_stat_statements` extension