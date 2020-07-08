FROM adoptopenjdk:11-jre-hotspot
VOLUME /tmp
ARG DEPENDENCY=build/dependency
COPY ${DEPENDENCY}/BOOT-INF/lib /app/lib
COPY ${DEPENDENCY}/META-INF /app/META-INF
COPY ${DEPENDENCY}/BOOT-INF/classes /app
ENV KOMGA_DATABASE_BACKUP_PATH="/config/database-backup.zip"
ENV KOMGA_DATABASE_FILE="/config/database.sqlite"
ENV SPRING_DATASOURCE_URL="jdbc:h2:/config/database.h2"
ENV SPRING_ARTEMIS_EMBEDDED_DATA_DIRECTORY="/config/artemis"
ENV LOGGING_FILE_NAME="/config/logs/komga.log"
ENTRYPOINT ["java","-cp","app:app/lib/*","org.gotson.komga.ApplicationKt"]
EXPOSE 8080
