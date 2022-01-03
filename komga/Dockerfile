FROM eclipse-temurin:11-jre
VOLUME /tmp
ARG DEPENDENCY=build/dependency
WORKDIR app
COPY ${DEPENDENCY}/dependencies/ ./
COPY ${DEPENDENCY}/spring-boot-loader/ ./
COPY ${DEPENDENCY}/snapshot-dependencies/ ./
COPY ${DEPENDENCY}/application/ ./
ENV KOMGA_DATABASE_FILE="/config/database.sqlite"
ENV KOMGA_LUCENE_DATA_DIRECTORY="/config/lucene"
ENV SPRING_ARTEMIS_EMBEDDED_DATA_DIRECTORY="/config/artemis"
ENV LOGGING_FILE_NAME="/config/logs/komga.log"
ENV LC_ALL=en_US.UTF-8
ENTRYPOINT ["java", "org.springframework.boot.loader.JarLauncher", "--spring.config.additional-location=file:/config/"]
EXPOSE 8080
