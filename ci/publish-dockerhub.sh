#!/usr/bin/env bash
# Arguments:
# 1: next version
# 2: channel

source "$(dirname "$0")/docker-common.sh" $1 $2

echo "$DOCKER_PASSWORD" | docker login -u "$DOCKER_USERNAME" --password-stdin

# Push docker images (built previously)
cd komga
docker buildx build \
    --platform $PLATFORMS \
    --cache-from gotson/komga:$DOCKER_CHANNEL \
    --tag gotson/komga:$DOCKER_CHANNEL \
    --tag gotson/komga:$1 \
    --file ./Dockerfile . \
    --push
