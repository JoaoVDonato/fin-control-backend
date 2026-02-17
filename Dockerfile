FROM eclipse-temurin:21-jre AS builder
WORKDIR /application
COPY target/fin-control-backend-*.jar application.jar
ADD https://github.com/open-telemetry/opentelemetry-java-instrumentation/releases/download/v2.7.0/opentelemetry-javaagent.jar agent.jar
RUN ["java", "-Djarmode=layertools", "-jar", "application.jar", "extract"]

FROM eclipse-temurin:21-jre
WORKDIR /application
COPY --from=builder /application/agent.jar ./
COPY --from=builder /application/dependencies/ ./
COPY --from=builder /application/spring-boot-loader/ ./
COPY --from=builder /application/snapshot-dependencies/ ./
COPY --from=builder /application/application/ ./
WORKDIR /data
VOLUME /data
EXPOSE 8080/tcp
CMD ["java", "-javaagent:/application/agent.jar", "-cp", "/application", "org.springframework.boot.loader.launch.JarLauncher"]
