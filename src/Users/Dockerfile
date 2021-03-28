## Stage 1 : build with maven builder image with native capabilities
FROM ghcr.io/graalvm/graalvm-ce:21.0.0 AS build

COPY . /usr/src/app
WORKDIR /usr/src/app

RUN gu install native-image
RUN ./mvnw -f /usr/src/app/pom.xml clean compile package -P prod

## Stage 2 : create the docker base image
FROM registry.access.redhat.com/ubi8/ubi AS ubi-micro-build

RUN mkdir -p /mnt/rootfs
RUN yum install --installroot /mnt/rootfs --releasever 8 --setopt install_weak_deps=false --nodocs -y \
    coreutils-single \
    glibc-minimal-langpack \
    libstdc++ \
    zlib
    
RUN rm -rf /mnt/rootfs/var/cache/*
RUN rm -rf /mnt/rootfs/var/lib/rpm
RUN mkdir /mnt/rootfs/app
RUN chown daemon:daemon /mnt/rootfs/app

## Stage 3 : create the docker final image
FROM scratch AS runner

COPY --from=ubi-micro-build /mnt/rootfs/ /
COPY --from=build /usr/src/app/target/*-runner /app/worker

USER daemon

ENV PORT 8080
EXPOSE 8080

CMD /app/worker -Dquarkus.http.host=0.0.0.0 -Dquarkus.http.port=$PORT