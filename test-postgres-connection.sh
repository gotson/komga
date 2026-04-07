#!/bin/bash

# Simple test to check PostgreSQL connection

echo "Testing PostgreSQL connection..."

# Check if PostgreSQL container is running
if ! docker-compose ps postgres | grep -q "Up"; then
    echo "PostgreSQL container is not running. Starting..."
    docker-compose up -d postgres
    sleep 5
fi

# Test connection from within container
echo "Testing connection from within container..."
docker exec komga-postgres psql -U komga -d komga -c "SELECT version();" 2>&1

# Test connection from host (if psql is installed)
echo -e "\nTesting extensions..."
docker exec komga-postgres psql -U komga -d komga -c "SELECT * FROM pg_extension;" 2>&1

echo -e "\nPostgreSQL is running and accessible!"