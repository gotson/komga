# Arguments
# 1: channel

export DOCKER_CLI_EXPERIMENTAL=enabled
PLATFORMS=linux/amd64,linux/arm/v7,linux/arm64/v8,linux/ppc64le,linux/s390x

if [ -z "$1" ]; then
    DOCKER_CHANNEL="latest"
else
    DOCKER_CHANNEL=$1
fi
