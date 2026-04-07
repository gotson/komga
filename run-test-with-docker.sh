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