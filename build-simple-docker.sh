#!/bin/bash

# Build Komga locally and create Docker image
set -e

echo "Building Komga Docker image from existing JAR..."

# Get version from gradle.properties
VERSION=$(grep 'version=' gradle.properties | cut -d'=' -f2)
echo "Building Docker image for version: $VERSION"

# Check if JAR exists
if [ ! -f "komga/build/libs/komga-$VERSION.jar" ]; then
    echo "Error: JAR file not found. Run './gradlew bootJar' first."
    exit 1
fi

# Build Docker image
docker build -f Dockerfile.simple-local -t komga-local:$VERSION .

echo ""
echo "Docker image built successfully:"
echo "  Image: komga-local:$VERSION"
echo "  Size: $(docker images --format "{{.Size}}" komga-local:$VERSION)"
echo "  Java: 21 (Temurin JRE)"
echo "  Entrypoint: java -jar komga.jar"
echo ""
echo "To run the container:"
echo "  docker run -p 25600:25600 -v /path/to/config:/config komga-local:$VERSION"
echo ""
echo "Note: Assumes frontend was already built (via prepareThymeLeaf)"