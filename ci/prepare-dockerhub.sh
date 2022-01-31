#!/usr/bin/env bash
# Arguments:
# 1: next version
# 2: channel

source "$(dirname "$0")/docker-common.sh" $1 $2

# Unpack fat jar
./gradlew unpack

# Build docker images (no push)
cd komga
docker buildx build \
    --platform $PLATFORMS \
    --cache-from gotson/komga:$DOCKER_CHANNEL \
    --tag gotson/komga:$DOCKER_CHANNEL \
    --tag gotson/komga:$VERSION_MAJOR.x \
    --tag gotson/komga:$1 \
    --file ./Dockerfile .

# Temporary: build legacy adoptopenjdk image
docker buildx build \
    --platform $PLATFORMS \
    --cache-from gotson/komga:$DOCKER_CHANNEL-legacy \
    --tag gotson/komga:$DOCKER_CHANNEL-legacy \
    --tag gotson/komga:$VERSION_MAJOR.x-legacy \
    --tag gotson/komga:$1-legacy \
    --file ./Dockerfile.legacy .
