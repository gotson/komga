FROM openjdk:11-jre-slim
VOLUME /tmp
ARG DEPENDENCY=target/dependency
COPY ${DEPENDENCY}/BOOT-INF/lib /app/lib
COPY ${DEPENDENCY}/META-INF /app/META-INF
COPY ${DEPENDENCY}/BOOT-INF/classes /app
ENV SPRING_PROFILES_ACTIVE=prod,docker
ENTRYPOINT ["java","-cp","app:app/lib/*","org.gotson.komga.ApplicationKt"]
