#!/usr/bin/env bash
source "$(dirname "$0")/docker-common.sh"

echo "$DOCKER_PASSWORD" | docker login -u "$DOCKER_USERNAME" --password-stdin

# Push docker images (built previously)
cd komga
docker buildx build \
--platform $PLATFORMS \
--cache-from gotson/komga:latest \
--tag gotson/komga:latest \
--tag gotson/komga:$1 \
--file ./Dockerfile . \
--push
