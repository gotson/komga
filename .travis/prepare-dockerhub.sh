#!/usr/bin/env bash
source "$(dirname "$0")/docker-common.sh"

# Update Docker CE
curl -fsSL https://download.docker.com/linux/ubuntu/gpg | sudo apt-key add -
sudo add-apt-repository "deb [arch=amd64] https://download.docker.com/linux/ubuntu $(lsb_release -cs) stable"
sudo apt-get update
sudo apt-get -y -o Dpkg::Options::="--force-confnew" install docker-ce

# Enable buildx
docker run --rm --privileged multiarch/qemu-user-static --reset -p yes
docker buildx create --name mybuilder --driver docker-container --use
docker buildx inspect --bootstrap

# Unpack fat jar
./gradlew unpack

# Build docker images (no push)
cd komga
docker buildx build \
--platform $PLATFORMS \
--cache-from gotson/komga:latest \
--tag gotson/komga:latest \
--tag gotson/komga:$1 \
--file ./Dockerfile .
