# Arguments
# 1: next version
# 2: channel

export DOCKER_CLI_EXPERIMENTAL=enabled
PLATFORMS=linux/amd64,linux/arm/v7,linux/arm64/v8

if [ -z "$2" ]; then
    DOCKER_CHANNEL="latest"
else
    DOCKER_CHANNEL=$2
fi

VERSION_ARRAY=( ${1//./ } )
VERSION_MAJOR="${VERSION_ARRAY[0]}"

echo "DockerHub channel: $DOCKER_CHANNEL"
echo "Major version: $VERSION_MAJOR"
