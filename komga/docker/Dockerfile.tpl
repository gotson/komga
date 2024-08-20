FROM eclipse-temurin:17-jre as builder
ARG JAR={{distributionArtifactFile}}
COPY assembly/* /
RUN java -Djarmode=layertools -jar ${JAR} extract

# amd64 builder: uses ubuntu:24.04, as both libjxl, libheif and libwebp is available
FROM ubuntu:24.04 as build-amd64
RUN apt -y update && \
    apt -y install openjdk-21-jre-headless ca-certificates locales libwebp-dev libarchive-dev libheif-dev libjxl-dev && \
    echo "en_US.UTF-8 UTF-8" >> /etc/locale.gen && \
    locale-gen en_US.UTF-8
ENV LD_LIBRARY_PATH="${LD_LIBRARY_PATH}:/usr/lib/x86_64-linux-gnu"

# arm64 builder: uses ubuntu:24.04, as both libjxl, libheif and libwebp is available
FROM ubuntu:24.04 as build-arm64
RUN apt -y update && \
    apt -y install openjdk-21-jre-headless ca-certificates locales libwebp-dev libarchive-dev libheif-dev libjxl-dev && \
    echo "en_US.UTF-8 UTF-8" >> /etc/locale.gen && \
    locale-gen en_US.UTF-8
ENV LD_LIBRARY_PATH="${LD_LIBRARY_PATH}:/usr/lib/aarch64-linux-gnu"

# arm builder: uses temurin-17, as arm32 support was dropped in JDK 21
FROM eclipse-temurin:17-jre as build-arm

FROM build-${TARGETARCH} AS runner
VOLUME /tmp
VOLUME /config
WORKDIR app
COPY --from=builder dependencies/ ./
COPY --from=builder spring-boot-loader/ ./
COPY --from=builder snapshot-dependencies/ ./
COPY --from=builder application/ ./
ENV KOMGA_CONFIGDIR="/config"
ENV LANG='en_US.UTF-8' LANGUAGE='en_US:en' LC_ALL='en_US.UTF-8'
ENTRYPOINT ["java", "--enable-preview", "--enable-native-access=ALL-UNNAMED", "org.springframework.boot.loader.launch.JarLauncher", "--spring.config.additional-location=file:/config/"]
EXPOSE 25600
LABEL org.opencontainers.image.source="https://github.com/gotson/komga"
