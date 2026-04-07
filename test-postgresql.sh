#!/bin/bash

# Script to test Komga with PostgreSQL using Testcontainers

echo "Building Komga..."
./gradlew :komga:build -x test

echo "Running tests with PostgreSQL..."
./gradlew :komga:test --tests "*PostgreSQL*" --info

echo "Running integration tests..."
./gradlew :komga:integrationTest --info

echo "Test completed!"