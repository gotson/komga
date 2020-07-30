FROM adoptopenjdk:11-jre-hotspot
VOLUME /tmp
ARG DEPENDENCY=build/dependency
WORKDIR app
COPY ${DEPENDENCY}/dependencies/ ./
COPY ${DEPENDENCY}/spring-boot-loader/ ./
COPY ${DEPENDENCY}/snapshot-dependencies/ ./
COPY ${DEPENDENCY}/application/ ./
ENV KOMGA_DATABASE_BACKUP_PATH="/config/database-backup.zip"
ENV KOMGA_DATABASE_FILE="/config/database.sqlite"
ENV SPRING_DATASOURCE_URL="jdbc:h2:/config/database.h2"
ENV SPRING_ARTEMIS_EMBEDDED_DATA_DIRECTORY="/config/artemis"
ENV LOGGING_FILE_NAME="/config/logs/komga.log"
ENTRYPOINT ["java", "org.springframework.boot.loader.JarLauncher"]
EXPOSE 8080
