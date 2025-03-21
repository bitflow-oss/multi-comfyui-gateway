## Stage 1 : build with maven builder image with native capabilities
FROM quay.io/quarkus/ubi9-quarkus-mandrel-builder-image:jdk-21 AS builder

USER root

RUN echo "Start installing dependencies"
RUN microdnf update -y
RUN microdnf install findutils dnf 'dnf-command(copr)' -y
RUN dnf copr enable @caddy/caddy -y
RUN dnf install caddy -y
RUN echo "Installing dependencies finished"

COPY --chown=quarkus:quarkus gradlew /code/gradlew
COPY --chown=quarkus:quarkus gradle /code/gradle
COPY --chown=quarkus:quarkus build.gradle.kts /code/
COPY --chown=quarkus:quarkus settings.gradle.kts /code/
COPY --chown=quarkus:quarkus gradle.properties /code/

USER quarkus
WORKDIR /code
COPY src /code/src
RUN ./gradlew build -x test -Dquarkus.native.enabled=true -Dquarkus.package.jar.enabled=false
RUN echo "Quarkus Native build finished"

## Stage 2 : create the docker final image
FROM quay.io/quarkus/ubi9-quarkus-micro-image:2.0
WORKDIR /work/
RUN mkdir /work/workflow
RUN mkdir /work/caddy

COPY caddy/Caddyfile /work/caddy/
COPY --from=builder /usr/bin/caddy /usr/bin/
COPY --from=builder /code/build/*-runner /work/application

RUN caddy start --config /work/caddy/Caddyfile
RUN echo "Copying modules and compiled executable to runtime image has finished"

RUN chmod 775 /work
EXPOSE 8080
CMD ["./application", "-Dquarkus.http.host=0.0.0.0"]