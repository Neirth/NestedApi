## Stage 1 : build with maven builder image with native capabilities
FROM quay.io/quarkus/centos-quarkus-maven:20.1.0-java8 AS build

USER root

COPY ./src /usr/src/app/src 
COPY ./pom.xml /usr/src/app

RUN mvn -f /usr/src/app/pom.xml -B de.qaware.maven:go-offline-maven-plugin:1.2.5:resolve-dependencies

RUN mvn -f /usr/src/app/pom.xml clean \
    && mvn -f /usr/src/app/pom.xml compile \
    && mvn -f /usr/src/app/pom.xml package -Pnative

## Stage 2 : create the docker final image
FROM cescoffier/native-base:latest as runner
COPY --from=build /usr/src/app/target/*-runner /application

ENV PORT 8080
EXPOSE 8080

CMD /application -Dquarkus.http.host=0.0.0.0 -Dquarkus.http.port=$PORT