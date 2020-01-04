#!/usr/bin/env bash

echo "$DOCKER_PASSWORD" | docker login -u "$DOCKER_USERNAME" --password-stdin
./gradlew copyWebDist && ./gradlew dockerPushBeta
