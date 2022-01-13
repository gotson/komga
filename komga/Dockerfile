FROM eclipse-temurin:11-jre
VOLUME /tmp
ARG DEPENDENCY=build/dependency
WORKDIR app
COPY ${DEPENDENCY}/dependencies/ ./
COPY ${DEPENDENCY}/spring-boot-loader/ ./
COPY ${DEPENDENCY}/snapshot-dependencies/ ./
COPY ${DEPENDENCY}/application/ ./
ENV KOMGA_CONFIGDIR="/config"
ENV LC_ALL=en_US.UTF-8
ENTRYPOINT ["java", "org.springframework.boot.loader.JarLauncher", "--spring.config.additional-location=file:/config/"]
EXPOSE 8080
