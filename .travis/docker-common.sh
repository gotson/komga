# Arguments
# 1: next version
# 2: channel

export DOCKER_CLI_EXPERIMENTAL=enabled
PLATFORMS=linux/amd64,linux/arm/v7,linux/arm64/v8,linux/ppc64le,linux/s390x

if [ -z "$2" ]; then
    DOCKER_CHANNEL="latest"
else
    DOCKER_CHANNEL=$2
fi

echo "DockerHub channel: $DOCKER_CHANNEL"
