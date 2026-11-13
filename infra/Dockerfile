# Single production image: Spring Boot back end + built React front end.
# Build context is the repository root: docker build -f infra/Dockerfile .

# --- Stage 1: build the front end -------------------------------------------
FROM node:24-alpine AS frontend-build
WORKDIR /frontend
COPY src/frontend/package.json src/frontend/package-lock.json ./
RUN npm ci
COPY src/frontend/ ./
RUN npm run build

# --- Stage 2: build the back end jar with the front end embedded --------------
FROM eclipse-temurin:25-jdk-alpine AS backend-build
WORKDIR /backend
COPY src/backend/.mvn .mvn
COPY src/backend/mvnw src/backend/pom.xml ./
RUN chmod +x mvnw && ./mvnw -B -q dependency:go-offline
COPY src/backend/src src
# Vite output goes to the classpath static folder served by StaticFrontendConfig.
COPY --from=frontend-build /frontend/dist src/main/resources/static
# Tests already ran in CI on the pull request; the image only packages.
RUN ./mvnw -B -q package -DskipTests && mv target/backend-*.jar target/app.jar

# --- Stage 3: minimal runtime --------------------------------------------------
FROM eclipse-temurin:25-jre-alpine
RUN addgroup -S app && adduser -S app -G app
USER app
WORKDIR /app
COPY --from=backend-build /backend/target/app.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
