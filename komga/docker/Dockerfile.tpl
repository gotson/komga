FROM eclipse-temurin:19-jre as builder
ARG JAR={{distributionArtifactFile}}
COPY assembly/* /
RUN java -Djarmode=layertools -jar ${JAR} extract

FROM eclipse-temurin:19-jre
# Install libjxl on x64 only. Homebrew is not available on other architectures.
RUN if [ "`uname -m`" = "x86_64" ]; then \
    apt -y update && \
    apt -y install git gcc && \
    NONINTERACTIVE=1 /bin/bash -c "$(curl -fsSL https://raw.githubusercontent.com/Homebrew/install/HEAD/install.sh)" && \
    eval "$(/home/linuxbrew/.linuxbrew/bin/brew shellenv)" && \
    brew install jpeg-xl; fi

VOLUME /tmp
VOLUME /config
WORKDIR app
COPY --from=builder dependencies/ ./
COPY --from=builder spring-boot-loader/ ./
COPY --from=builder snapshot-dependencies/ ./
COPY --from=builder application/ ./
ENV KOMGA_CONFIGDIR="/config"
ENV LC_ALL=en_US.UTF-8
ENV LD_LIBRARY_PATH="${LD_LIBRARY_PATH}:/home/linuxbrew/.linuxbrew/lib/"
ENTRYPOINT ["java", "--enable-preview", "--enable-native-access=ALL-UNNAMED", "org.springframework.boot.loader.JarLauncher", "--spring.config.additional-location=file:/config/"]
EXPOSE 25600
LABEL org.opencontainers.image.source="https://github.com/gotson/komga"
