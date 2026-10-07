FROM eclipse-temurin:17-jdk AS builder
WORKDIR /source
COPY gradlew gradlew.bat build.gradle.kts settings.gradle.kts ./
COPY gradle ./gradle
COPY src ./src
RUN ./gradlew --no-daemon clean bootJar

FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=builder /source/build/libs/secu_qna-0.0.1-SNAPSHOT.jar /app/app.jar
USER 10001:10001
EXPOSE 8080
CMD ["java", "-jar", "/app/app.jar"]
