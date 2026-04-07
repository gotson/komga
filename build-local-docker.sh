#!/bin/bash

# Build local Docker image for Komga
set -e

echo "Building Komga Docker image locally..."

# Get version from gradle.properties
VERSION=$(grep 'version=' gradle.properties | cut -d'=' -f2)
echo "Building version: $VERSION"

# Build Docker image with Node.js 18 support
docker build -f Dockerfile.local -t komga-local:$VERSION .

echo ""
echo "Docker image built successfully:"
echo "  Image: komga-local:$VERSION"
echo "  Node.js: 18.20.4 (from Alpine 3.18 repository)"
echo "  Java: 21 (Temurin)"
echo ""
echo "To run the container:"
echo "  docker run -p 25600:25600 -v /path/to/config:/config komga-local:$VERSION"
echo ""
echo "To tag with different name:"
echo "  docker tag komga-local:$VERSION komga:latest"