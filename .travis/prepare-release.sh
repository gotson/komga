#!/usr/bin/env bash

# Update version for Gradle
echo version=$1 >gradle.properties

# Build jar
./gradlew copyWebDist
./gradlew assemble

# Prepare Dockerhub release
source "$(dirname "$0")/prepare-dockerhub.sh" $1
