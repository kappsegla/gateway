FROM ghcr.io/graalvm/jdk:22.3.2 AS build
WORKDIR /gateway
COPY ./ ./
RUN mvn -Pnative native:compile

FROM gcr.io/distroless/base
EXPOSE 8080
COPY --from=build /gateway/target/gateway /app
ENTRYPOINT ["/app"]
