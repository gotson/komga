#!/bin/bash
# Start Komga backend with pre-built frontend for local development
set -e

FRONTEND_SOURCE="./frontend-dist"
BACKEND_RESOURCES="./komga/src/main/resources"

echo "=== Komga Local Development with Frontend ==="
echo "Frontend source: $FRONTEND_SOURCE"

# Check if frontend dist exists
if [ ! -d "$FRONTEND_SOURCE" ]; then
    echo "ERROR: Frontend distribution not found at $FRONTEND_SOURCE"
    echo "Build frontend first: ./build-local-docker.sh --export-dist"
    exit 1
fi

# Ensure backend resources directories exist
mkdir -p "$BACKEND_RESOURCES/public"
mkdir -p "$BACKEND_RESOURCES/templates"

echo "Copying static assets to $BACKEND_RESOURCES/public/"
rsync -a --exclude="index.html" "$FRONTEND_SOURCE/" "$BACKEND_RESOURCES/public/" 2>/dev/null || cp -r "$FRONTEND_SOURCE/"* "$BACKEND_RESOURCES/public/" 2>/dev/null

echo "Moving index.html to $BACKEND_RESOURCES/templates/"
if [ -f "$BACKEND_RESOURCES/public/index.html" ]; then
    mv "$BACKEND_RESOURCES/public/index.html" "$BACKEND_RESOURCES/templates/"
elif [ -f "$FRONTEND_SOURCE/index.html" ]; then
    cp "$FRONTEND_SOURCE/index.html" "$BACKEND_RESOURCES/templates/"
fi

# Check if PostgreSQL option is provided
USE_POSTGRES=false
if [[ "$1" == "--postgres" ]]; then
    USE_POSTGRES=true
    echo "Using PostgreSQL database"
fi

if [ "$USE_POSTGRES" = true ]; then
    echo "Starting PostgreSQL container..."
    docker-compose -f docker-compose.local.yml up -d postgres
    
    echo "Waiting for PostgreSQL to be ready..."
    until docker exec komga-postgres pg_isready -U komga -d komga; do
        sleep 1
    done
    
    echo "Setting PostgreSQL environment variables..."
    export KOMGA_DATABASE_DRIVER_CLASS_NAME=org.postgresql.Driver
    export KOMGA_DATABASE_URL="jdbc:postgresql://localhost:5433/komga?user=komga&password=komga123"
    export KOMGA_DATABASE_USERNAME=komga
    export KOMGA_DATABASE_PASSWORD=komga123
    export KOMGA_DATABASE_TYPE=postgresql
    export KOMGA_DATABASE_POOL_SIZE=10
    export KOMGA_DATABASE_MAX_POOL_SIZE=10
    export SPRING_FLYWAY_BASELINE_ON_MIGRATE=true
    export SPRING_FLYWAY_BASELINE_VERSION=20250730173126
    export KOMGA_CONFIG_DIR="$HOME/.komga-postgres"
    export SPRING_PROFILES_ACTIVE=postgresql
fi

echo "Starting Komga backend..."
echo "Note: Frontend build tasks are skipped"
echo "Access at: http://localhost:25600/"
echo "Press Ctrl+C to stop"

# Run backend with excluded frontend tasks
./gradlew :komga:bootRun -x npmInstall -x npmBuild -x copyWebDist -x prepareThymeLeaf

# Cleanup on exit
if [ "$USE_POSTGRES" = true ]; then
    echo "Stopping PostgreSQL container..."
    docker-compose -f docker-compose.local.yml stop postgres
fi