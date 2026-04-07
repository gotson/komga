#!/bin/bash

# Build Komga Docker image locally with multi-stage build
set -e

echo "Building Komga Docker image locally..."

# Get version from gradle.properties
VERSION=$(grep 'version=' gradle.properties | cut -d'=' -f2)
echo "Building version: $VERSION"

# Build Docker image with multi-stage build (Node.js 18 + Java 21)
docker build -f Dockerfile.local -t komga-local:$VERSION .

echo ""
echo "Docker image built successfully:"
echo "  Image: komga-local:$VERSION"
echo "  Build: Multi-stage (Node.js 18 + Java 21)"
echo "  Runtime: Java 21 (Temurin JRE)"
echo ""
echo "To run standalone container:"
echo "  docker run -p 25600:25600 -v /path/to/config:/config komga-local:$VERSION"
echo ""
echo "To run with docker-compose:"
echo "  docker-compose up -d"
echo ""
echo "To tag with different name:"
echo "  docker tag komga-local:$VERSION komga:latest"