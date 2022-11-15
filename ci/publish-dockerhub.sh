#!/usr/bin/env bash
# Arguments:
# 1: next version
# 2: channel

source "$(dirname "$0")/docker-common.sh" $1 $2

# Push docker images (built previously)
cd komga
docker buildx build \
    --platform $PLATFORMS \
    --cache-from gotson/komga:$DOCKER_CHANNEL \
    --tag gotson/komga:$DOCKER_CHANNEL \
    --tag gotson/komga:$VERSION_MAJOR.x \
    --tag gotson/komga:$1 \
    --tag ghcr.io/gotson/komga:$DOCKER_CHANNEL \
    --tag ghcr.io/gotson/komga:$VERSION_MAJOR.x \
    --tag ghcr.io/gotson/komga:$1 \
    --file ./Dockerfile . \
    --push
