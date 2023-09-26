FROM eclipse-temurin:19-jre as builder
ARG JAR={{distributionArtifactFile}}
COPY assembly/* /
RUN java -Djarmode=layertools -jar ${JAR} extract

FROM eclipse-temurin:19-jre
# Install libjxl on x64 only. It is not available on other architectures.
RUN if [ "`uname -m`" = "x86_64" ]; then \
    apt -y update && \
    apt -y install wget && \
    wget "https://github.com/libjxl/libjxl/releases/download/v0.8.2/jxl-debs-amd64-ubuntu-22.04-v0.8.2.tar.gz" && \
    tar -xzf jxl-debs-amd64-ubuntu-22.04-v0.8.2.tar.gz && \
    apt -y install ./jxl_0.8.2_amd64.deb ./libjxl_0.8.2_amd64.deb ./libjxl-dev_0.8.2_amd64.deb && \
    rm *.deb && rm *.ddeb && rm jxl-debs-amd64-ubuntu-22.04-v0.8.2.tar.gz && \
    apt -y remove wget && apt -y clean && rm -rf /var/lib/apt/lists/*; fi

VOLUME /tmp
VOLUME /config
WORKDIR app
COPY --from=builder dependencies/ ./
COPY --from=builder spring-boot-loader/ ./
COPY --from=builder snapshot-dependencies/ ./
COPY --from=builder application/ ./
ENV KOMGA_CONFIGDIR="/config"
ENV LC_ALL=en_US.UTF-8
ENV LD_LIBRARY_PATH="${LD_LIBRARY_PATH}:/usr/lib/x86_64-linux-gnu"
ENTRYPOINT ["java", "--enable-preview", "--enable-native-access=ALL-UNNAMED", "org.springframework.boot.loader.JarLauncher", "--spring.config.additional-location=file:/config/"]
EXPOSE 25600
LABEL org.opencontainers.image.source="https://github.com/gotson/komga"
