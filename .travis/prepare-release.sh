#!/usr/bin/env bash
# Arguments:
# 1: next version
# 2: channel

# Update version for Gradle
echo version=$1 >gradle.properties

# Build jar
./gradlew copyWebDist
./gradlew assemble
./gradlew generateOpenApiDocs

# Prepare Dockerhub release
source "$(dirname "$0")/prepare-dockerhub.sh" $1 $2
