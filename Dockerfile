## Stage 1 : build with maven builder image with native capabilities
FROM quay.io/quarkus/ubi9-quarkus-mandrel-builder-image:jdk-21 AS builder
USER root
RUN microdnf update -y
RUN microdnf install -y findutils dnf
RUN dnf install -y 'dnf-command(copr)'
RUN dnf copr enable @caddy/caddy -y
RUN dnf install -y caddy

COPY --chown=quarkus:quarkus gradlew /code/gradlew
COPY --chown=quarkus:quarkus gradle /code/gradle
COPY --chown=quarkus:quarkus build.gradle.kts /code/
COPY --chown=quarkus:quarkus settings.gradle.kts /code/
COPY --chown=quarkus:quarkus gradle.properties /code/

USER quarkus
WORKDIR /code
COPY src /code/src
RUN ./gradlew build -Dquarkus.native.enabled=true -Dquarkus.package.jar.enabled=false

## Stage 2 : create the docker final image
FROM quay.io/quarkus/ubi9-quarkus-micro-image:2.0
WORKDIR /work/
COPY --from=builder /var/lib/caddy/ /var/lib
COPY --from=builder /code/build/*-runner /work/application
RUN chmod 775 /work
EXPOSE 8080
CMD ["./application", "-Dquarkus.http.host=0.0.0.0"]