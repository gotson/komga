#!/bin/bash

# Build Komga Docker image locally with multi-stage build
set -e

# Default to optimized build
OPTIMIZED=true
DOCKERFILE="Dockerfile.local.optimized"
BUILD_TYPE="Optimized multi-stage"
EXPORT_DIST=false
EXPORT_DIST_DIR="./frontend-dist"

# Parse command line arguments
while [[ $# -gt 0 ]]; do
  case $1 in
    --no-optimized)
      OPTIMIZED=false
      DOCKERFILE="Dockerfile.local"
      BUILD_TYPE="Standard"
      shift
      ;;
    --export-dist)
      EXPORT_DIST=true
      if [[ -n "$2" && "$2" != --* ]]; then
        EXPORT_DIST_DIR="$2"
        shift
      else
        EXPORT_DIST_DIR="./frontend-dist"
      fi
      shift
      ;;
    --help|-h)
      echo "Usage: $0 [--no-optimized] [--export-dist [DIR]]"
      echo ""
      echo "Build Komga Docker image locally."
      echo ""
      echo "Options:"
      echo "  --no-optimized    Use the standard Dockerfile.local instead of optimized version"
      echo "  --export-dist [DIR]  Export frontend dist files to directory (default: ./frontend-dist)"
      echo "  --help, -h        Show this help message"
      echo ""
      echo "By default, uses Dockerfile.local.optimized for better build caching."
      exit 0
      ;;
    *)
      echo "Unknown option: $1"
      echo "Use --help for usage information"
      exit 1
      ;;
  esac
done

echo "Building Komga Docker image locally..."
echo "Build type: $BUILD_TYPE"
echo "Dockerfile: $DOCKERFILE"
echo ""

# Get version from gradle.properties
VERSION=$(grep 'version=' gradle.properties | cut -d'=' -f2)
echo "Building version: $VERSION"

# Build Docker image
docker build -f $DOCKERFILE -t komga-local:$VERSION .

# Export frontend dist files if requested
if [ "$EXPORT_DIST" = true ]; then
  if [ "$OPTIMIZED" = true ]; then
    echo "Exporting frontend dist files to $EXPORT_DIST_DIR"
    mkdir -p "$EXPORT_DIST_DIR"
    # Build frontend-builder stage and extract dist files
    docker build --target frontend-builder -t komga-frontend-builder-$VERSION -f $DOCKERFILE .
    docker run --rm komga-frontend-builder-$VERSION tar -czf - -C /app/dist . | tar -xzf - -C "$EXPORT_DIST_DIR"
    echo "Dist files exported to $EXPORT_DIST_DIR"
    docker rmi komga-frontend-builder-$VERSION 2>/dev/null || true
  else
    echo "Export dist is only supported with optimized build (--no-optimized not supported)"
  fi
fi

echo ""
echo "Docker image built successfully:"
echo "  Image: komga-local:$VERSION"
echo "  Build: $BUILD_TYPE (Node.js 18 + Java 21)"
echo "  Runtime: Java 21 (Temurin JRE)"
echo ""
echo "To run standalone container:"
echo "  docker run -p 25600:25600 -v /path/to/config:/config komga-local:$VERSION"
echo ""
echo "To run with docker-compose:"
if [ "$OPTIMIZED" = true ]; then
  echo "  docker-compose -f docker-compose.local.optimized.yml up -d"
else
  echo "  docker-compose -f docker-compose.local.yml up -d"
fi
echo ""
echo "To tag with different name:"
echo "  docker tag komga-local:$VERSION komga:latest"