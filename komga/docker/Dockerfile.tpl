FROM eclipse-temurin:17-jre as builder
ARG JAR={{distributionArtifactFile}}
COPY assembly/* /
RUN java -Djarmode=layertools -jar ${JAR} extract

# amd64 builder: uses ubuntu:22.04, as libjxl is not available on more recent versions
FROM ubuntu:22.04 as build-amd64
ENV JAVA_HOME=/opt/java/openjdk
COPY --from=eclipse-temurin:21-jre $JAVA_HOME $JAVA_HOME
ENV PATH="${JAVA_HOME}/bin:${PATH}"
RUN apt -y update && \
    apt -y install ca-certificates locales software-properties-common wget libwebp-dev libarchive-dev && \
    echo "en_US.UTF-8 UTF-8" >> /etc/locale.gen && \
    locale-gen en_US.UTF-8 && \
    add-apt-repository -y ppa:strukturag/libheif && \
    add-apt-repository -y ppa:strukturag/libde265 && \
    apt -y update && apt install -y libheif-dev && \
    wget "https://github.com/libjxl/libjxl/releases/download/v0.8.2/jxl-debs-amd64-ubuntu-22.04-v0.8.2.tar.gz" && \
    tar -xzf jxl-debs-amd64-ubuntu-22.04-v0.8.2.tar.gz && \
    apt -y install ./jxl_0.8.2_amd64.deb ./libjxl_0.8.2_amd64.deb ./libjxl-dev_0.8.2_amd64.deb && \
    rm *.deb && rm *.ddeb && rm jxl-debs-amd64-ubuntu-22.04-v0.8.2.tar.gz && \
    apt -y remove wget software-properties-common && apt -y autoremove && rm -rf /var/lib/apt/lists/*
ENV LD_LIBRARY_PATH="${LD_LIBRARY_PATH}:/usr/lib/x86_64-linux-gnu"

# arm64 builder: uses ubuntu23.10 which has libheif 1.16
FROM ubuntu:23.10 as build-arm64
ENV JAVA_HOME=/opt/java/openjdk
COPY --from=eclipse-temurin:21-jre $JAVA_HOME $JAVA_HOME
ENV PATH="${JAVA_HOME}/bin:${PATH}"
RUN apt -y update && \
    apt -y install ca-certificates locales libheif-dev libwebp-dev libarchive-dev && \
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
