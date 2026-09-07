# ── Build stage ──────────────────────────────────────────
# Builds on JDK 17 (the project's target), so this works no matter
# which Java the host has.
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn -q -B dependency:go-offline
COPY src ./src
RUN mvn -q -B package -DskipTests

# ── Runtime stage ────────────────────────────────────────
FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /app/target/tiokamp-*.jar app.jar

# All persistent state (H2 database + uploaded profile pictures) lives in /data —
# mount it as a volume so it survives container rebuilds.
ENV SPRING_DATASOURCE_URL="jdbc:h2:file:/data/tiokampdb;DB_CLOSE_DELAY=-1" \
    APP_UPLOAD_DIR=/data/uploads \
    SPRING_H2_CONSOLE_ENABLED=false \
    SERVER_PORT=8080 \
    SERVER_FORWARD_HEADERS_STRATEGY=native \
    TZ=Europe/Stockholm

VOLUME /data
EXPOSE 8080

ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75.0", "-jar", "/app/app.jar"]
