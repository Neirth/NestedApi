## Stage 1 : build with maven builder image with native capabilities
FROM quay.io/quarkus/centos-quarkus-maven:20.1.0-java8 AS build

USER root

COPY ./src /usr/src/app/src 
COPY ./pom.xml /usr/src/app

RUN mvn -f /usr/src/app/pom.xml -B de.qaware.maven:go-offline-maven-plugin:1.2.5:resolve-dependencies

RUN mvn -f /usr/src/app/pom.xml clean \
    && mvn -f /usr/src/app/pom.xml avro:protocol \
    && mvn -f /usr/src/app/pom.xml compile \
    && mvn -f /usr/src/app/pom.xml package -Pnative

## Stage 2 : create the docker base image
FROM registry.access.redhat.com/ubi8/ubi AS ubi-micro-build

MAINTAINER Scott McCarty smccarty@redhat.com

RUN mkdir -p /mnt/rootfs
RUN yum install --installroot /mnt/rootfs --releasever 8 --setopt install_weak_deps=false --nodocs -y \
    coreutils-single \
    glibc-minimal-langpack \
    libstdc++ \
    zlib
    
RUN rm -rf /mnt/rootfs/var/cache/*
RUN rm -rf /mnt/rootfs/var/lib/rpm
RUN mkdir /mnt/rootfs/app

## Stage 3 : create the docker final image
FROM scratch AS runner

COPY --from=ubi-micro-build /mnt/rootfs/ /
COPY --from=build /usr/src/app/target/*-runner /app/worker

ENV PORT 8080
EXPOSE 8080

CMD /app/worker -Dquarkus.http.host=0.0.0.0 -Dquarkus.http.port=$PORT