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
KOMGA_DATABASE_URL="jdbc:postgresql://localhost:5433/komga?sslmode=disable&socketTimeout=10" \
KOMGA_DATABASE_USERNAME=komga \
KOMGA_DATABASE_PASSWORD=komga123 \
KOMGA_CONFIG_DIR="$HOME/.komga-postgres" \
./gradlew :komga:bootRun

echo "Komga is running at http://localhost:25600"
echo "PostgreSQL is running at localhost:5433"
echo "To stop: docker-compose down"