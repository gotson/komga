# Build web interface
FROM node:16 AS build
RUN apt-get update && apt-get install -y openjdk-11-jdk
WORKDIR /proj

COPY . ./
RUN ./gradlew copyWebDist

# Copy java source
RUN ./gradlew unpack

RUN find build
RUN find ./komga/build

# Final runtime container
FROM eclipse-temurin:11-jre
VOLUME /tmp
ARG DEPENDENCY=/proj/komga/build/dependency
WORKDIR app
COPY --from=build ${DEPENDENCY}/dependencies/ ./
COPY --from=build ${DEPENDENCY}/spring-boot-loader/ ./
COPY --from=build ${DEPENDENCY}/snapshot-dependencies/ ./
COPY --from=build ${DEPENDENCY}/application/ ./
ENV KOMGA_CONFIGDIR="/config"
RUN mkdir /config
ENV LC_ALL=en_US.UTF-8
ENTRYPOINT ["java", "org.springframework.boot.loader.JarLauncher", "--spring.config.additional-location=file:/config/"]
EXPOSE 8080
LABEL org.opencontainers.image.source="https://github.com/gotson/komga"
