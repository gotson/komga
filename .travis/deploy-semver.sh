#!/usr/bin/env bash

vergte() {
    [  "$1" = "$(echo -e "$1\n$2" | sort -rV | head -n1)" ]
}

vergt() {
    [ "$1" = "$2" ] && return 1 || vergte $1 $2
}

HIGHEST_TAG=$(curl -sk https://api.github.com/repos/gotson/komga/tags | jq -r '.[].name' | grep ^v | tr -d v | sort -rV | head -n1)
CURRENT_TAG=$(echo ${TRAVIS_TAG} | tr -d v)

echo HIGHEST_TAG version: ${HIGHEST_TAG}
echo CURRENT_TAG version: ${CURRENT_TAG}

if vergte ${CURRENT_TAG} ${HIGHEST_TAG}
then
  echo Current tag is highest, deploying SemVer and Latest
  ./gradlew dockerPushLatest dockerPushSemVer
else
  echo Current tag is not highest, deploying SemVer only
  ./gradlew dockerPushSemVer
fi