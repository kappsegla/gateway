package com.example.gateway;

import org.springframework.cloud.gateway.filter.factory.DedupeResponseHeaderGatewayFilterFactory.Strategy;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;

@Configuration
public class GatewayConfig {

    final AuthenticationFilter jwtAuthFilter;

    public GatewayConfig(AuthenticationFilter jwtAuthFilter) {
        this.jwtAuthFilter = jwtAuthFilter;
    }

    @Bean
    public RouteLocator myRoutes(RouteLocatorBuilder builder) {
        return builder.routes()
                .route("post-service", r -> r.path("/posts/**")
                        .filters(f -> f
                                .filter(jwtAuthFilter)
                                .dedupeResponseHeader("Access-Control-Allow-Origin", String.valueOf(Strategy.RETAIN_FIRST))
                        ).uri("http://postservice:8000"))

                .route("user-service", r -> r.path("/users/**")
                        .filters(f -> f
                                .filter(jwtAuthFilter)
                                .dedupeResponseHeader("Access-Control-Allow-Origin", String.valueOf(Strategy.RETAIN_FIRST))
                                .rewritePath("/users/(?<segment>.*)", "/users/${segment}")
                        ).uri("http://userservice:8002"))

                .route("image-service", r -> r.path("/images/**")
                        .filters(f -> f
                                .filter(jwtAuthFilter)
                                .dedupeResponseHeader("Access-Control-Allow-Origin", String.valueOf(Strategy.RETAIN_FIRST))
                                .removeRequestHeader("Origin")
                        ).uri("http://imageservice:8001"))

                .route("auth-service", r -> r.path("/auth")
                        .filters(f -> f
                                .filter(jwtAuthFilter)
                                .dedupeResponseHeader("Access-Control-Allow-Origin", String.valueOf(Strategy.RETAIN_FIRST))
                                .rewritePath("/auth", "/api/auth/login"))
                        .uri("http://authservice:8080"))

                .route("auth-register-service", r -> r.path("/register")
                        .filters(f -> f
                                .filter(jwtAuthFilter)
                                .dedupeResponseHeader("Access-Control-Allow-Origin", String.valueOf(Strategy.RETAIN_FIRST))
                                .rewritePath("/register", "/api/auth"))
                        .uri("http://authservice:8080"))

                .route("post-short-link-service", r -> r.path("/short").and().method(HttpMethod.POST)
                        .filters(f -> f
                                .filter(jwtAuthFilter)
                                .dedupeResponseHeader("Access-Control-Allow-Origin", String.valueOf(Strategy.RETAIN_FIRST))
                                .rewritePath("/short", "/urlShortener")
                        ).uri("http://shorturl-service:8080"))

                .route("get-short-link-service", r -> r.path("/urlShortener/**").and().method(HttpMethod.GET)
                        .filters(f -> f
                                .dedupeResponseHeader("Access-Control-Allow-Origin", String.valueOf(Strategy.RETAIN_FIRST))
                               // .rewritePath("/urlShortener/(?<segment>.*)", "/${segment}")
                        ).uri("http://shorturl-service:8080"))

                .route("like-service", r -> r.path("/like/**")
                        .filters(f -> f
                                .filter(jwtAuthFilter)
                                .dedupeResponseHeader("Access-Control-Allow-Origin", String.valueOf(Strategy.RETAIN_FIRST)))
                        .uri("http://likesservice:8005")).build();
    }
}
