FROM eclipse-temurin:17-jdk-jammy AS builder
WORKDIR /app

COPY . .
RUN chmod +x mvnw && ./mvnw -q -DskipTests clean package

FROM eclipse-temurin:17-jre-jammy
WORKDIR /app
ENV TZ=Asia/Shanghai

COPY --from=builder /app/target/*.jar /app/app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
