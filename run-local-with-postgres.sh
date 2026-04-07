#!/bin/bash

# Configuration
COMPOSE_FILE="docker-compose.local.yml"
DB_PASSWORD="komga123"

echo "Starting PostgreSQL container..."
docker compose -f "$COMPOSE_FILE" up -d postgres

echo "Waiting for PostgreSQL to be ready..."
until docker exec komga-postgres pg_isready -U komga -d komga; do
  sleep 1
done

echo "Manually applying the consolidated PostgreSQL schema..."
# We apply the big script manually to the DB
docker exec -i komga-postgres psql -U komga -d komga < komga/src/flyway/resources/db/migration/postgresql/V001__initial_migration.sql

echo "Running Komga with PostgreSQL..."
# Note: Use KOMGA_DATABASE_URL and other matching properties
export KOMGA_DATABASE_DRIVER_CLASS_NAME=org.postgresql.Driver
export KOMGA_DATABASE_URL="jdbc:postgresql://localhost:5433/komga?user=komga&password=$DB_PASSWORD"
export KOMGA_DATABASE_USERNAME=komga
export KOMGA_DATABASE_PASSWORD=$DB_PASSWORD
export KOMGA_DATABASE_TYPE=postgresql
export KOMGA_DATABASE_POOL_SIZE=10
export KOMGA_DATABASE_MAX_POOL_SIZE=10
export SPRING_FLYWAY_BASELINE_ON_MIGRATE=true
export SPRING_FLYWAY_BASELINE_VERSION=20250730173126
export KOMGA_CONFIG_DIR="$HOME/.komga-postgres"

./gradlew clean :komga:bootRun

echo "Komga is running at http://localhost:25600"
echo "PostgreSQL is running at localhost:5433"
echo "To stop: docker compose -f $COMPOSE_FILE stop postgres"