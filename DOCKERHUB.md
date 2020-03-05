[![Build Status](https://travis-ci.com/gotson/komga.svg?branch=master)](https://travis-ci.com/gotson/komga)
[![GitHub release (latest SemVer)](https://img.shields.io/github/v/release/gotson/komga?color=blue&label=download&sort=semver)](https://github.com/gotson/komga/releases)
[![Docker Pulls](https://img.shields.io/docker/pulls/gotson/komga)](https://hub.docker.com/r/gotson/komga)

# ![app icon](https://github.com/gotson/komga/raw/master/.github/readme-images/app-icon.png) Komga

[Komga](https://github.com/gotson/komga) is a free and open source comics/mangas/magazines server.

## Version tags

This image provides various versions that are available via tags.

**WARNING**: the `beta` tag has been removed, use `latest` instead.

|**Tag** |**Description**         |
|:------:|------------------------|
|`latest`  |build from latest commit|
|`x.y.z` |version `x.y.z`         |

## Usage

Here are some example snippets to help you get started creating a container.

### docker

```
docker create \
  --name=komga \
  --user 1000:1000 \
  -p 8080:8080 \
  --mount type=bind,source=/path/to/config,target=/config \
  --mount type=bind,source=/path/to/books,target=/books \
  --restart unless-stopped \
  gotson/komga
```

### docker-compose

```
---
version: '3.3'
services:
  komga:
    image: gotson/komga
    container_name: komga
    volumes:
      - type: bind
        source: /path/to/data
        target: /config
      - type: bind
        source: /path/to/books
        target: /books
      - type: bind
        source: /etc/timezone
        target: /etc/timezone
        read_only: true
    ports:
      - 8080:8080
    user: "1000:1000"
    restart: unless-stopped
```

## Parameters

Container images are configured using parameters passed at runtime (such as those above).
These parameters are separated by a colon and indicate `external:internal` respectively.
For example, `-p 8080:80` would expose port `80` from inside the container to be accessible from the host's IP on port `8080` outside the container.

|                          Parameter                        | Function                                                              |
|:---------------------------------------------------------:|-----------------------------------------------------------------------|
| `-p 8080`                                                 | The port for the Komga APIs and web interface                         |
| `--user: 1000:1000`                                       | User:Group identifier - see below for explanation                     |
| `--mount type=bind,source=/path/to/config,target=/config` | Database and Komga configurations                                     |
| `--mount type=bind,source=/path/to/books,target=/books`   | Location of books library on disk                                     |
| `-e ENV_VAR=value`            | Any [configuration](https://komga.org/configuration/) environment variable |


## User / Group Identifiers

When using volumes (`-v` flags) permissions issues can arise between the host OS and the container, we avoid this issue by allowing you to specify the user ID and group ID.

Ensure any volume directories on the host are owned by the same user you specify and any permissions issues will vanish like magic.

In this instance `UID=1000` and `GID=1000`, to find yours use `id <your_user>` as below:

```
$ id <your_user>
  uid=1000(jdoe) gid=1000(jdgroup) groups=1000(jdgroup)
```

## Support info

- Shell access whilst the container is running: `docker exec -it komga /bin/bash`
- To monitor the logs of the container in realtime: `docker logs -f komga`

## Updating

Below are the instructions for updating containers:

### Via Docker Run/Create

- Update the image: `docker pull gotson/komga`
- Stop the running container: `docker stop komga`
- Delete the container: `docker rm komga`
- Recreate a new container with the same docker create parameters as instructed above (if mapped correctly to a host folder, your `/config` folder and settings will be preserved)
- Start the new container: `docker start komga`
- You can also remove the old dangling images: `docker image prune`

### Via Docker Compose

- Update all images: `docker-compose pull`
  - or update a single image: `docker-compose pull komga`
- Let compose update all containers as necessary: `docker-compose up -d`
  - or update a single container: `docker-compose up -d komga`
- You can also remove the old dangling images: `docker image prune`
