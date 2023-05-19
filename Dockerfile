FROM ghcr.io/graalvm/native-image:22.3.2 AS build
ENV MAVEN_VERSION=3.9.2
ENV MAVEN_HOME="/opt/apache-maven-$MAVEN_VERSION"
ENV PATH="$JAVA_HOME/bin:$MAVEN_HOME/bin:$PATH"
#Install gzip
RUN microdnf -y install gzip
#Install maven
RUN curl -s -L "https://maven.apache.org/download.cgi?action=download&filename=maven/maven-3/$MAVEN_VERSION/binaries/apache-maven-$MAVEN_VERSION-bin.tar.gz" -o "apache-maven-$MAVEN_VERSION-bin.tar.gz" \
  && curl -s -L "https://downloads.apache.org/maven/maven-3/$MAVEN_VERSION/binaries/apache-maven-$MAVEN_VERSION-bin.tar.gz.sha512" -o "apache-maven-$MAVEN_VERSION-bin.tar.gz.sha512" \
  && echo "$(cat apache-maven-$MAVEN_VERSION-bin.tar.gz.sha512)  apache-maven-$MAVEN_VERSION-bin.tar.gz" | sha512sum -c
RUN tar -xzf "apache-maven-$MAVEN_VERSION-bin.tar.gz" -C /opt

WORKDIR /app
COPY ./ .
RUN mvn verify
RUN mvn package -Pnative -Dmaven.test.skip=true -DskipTests
RUN cd / && ls -la

FROM frolvlad/alpine-glibc:glibc-2.34
#This image is small and with glibc 2.34 needed by our statically linked native-image
EXPOSE 8080
COPY --from=build /app/target/gateway /app
ENTRYPOINT ["/app"]
