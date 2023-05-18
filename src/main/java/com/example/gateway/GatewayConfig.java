package com.example.gateway;

import org.springframework.cloud.gateway.filter.factory.DedupeResponseHeaderGatewayFilterFactory.Strategy;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GatewayConfig {

    final AuthenticationFilter jwtAuthFilter;

    public GatewayConfig(AuthenticationFilter jwtAuthFilter) {
        this.jwtAuthFilter = jwtAuthFilter;
    }

    @Bean
    public RouteLocator myRoutes(RouteLocatorBuilder builder) {
        return builder.routes()
                .route("user-service", r -> r.path("/users/**")
                        .filters(f -> f
                                        .filter(jwtAuthFilter)
                                        .dedupeResponseHeader("Access-Control-Allow-Origin", String.valueOf(Strategy.RETAIN_FIRST))
                                        .rewritePath("/users/(?<segment>.*)", "/users/${segment}")
                                //.addRequestHeader("userID", "")
                        ).uri("http://localhost:8002"))
                .route("image-service", r -> r.path("/images/**")
                        .filters(f -> f
                                //.filter(jwtAuthFilter)
                                .dedupeResponseHeader("Access-Control-Allow-Origin", String.valueOf(Strategy.RETAIN_FIRST))
                                .dedupeResponseHeader("Access-Control-Request-Method", String.valueOf(Strategy.RETAIN_FIRST))
                                .dedupeResponseHeader("Access-Control-Request-Headers", String.valueOf(Strategy.RETAIN_FIRST)))
                        .uri("http://localhost:8001"))

                .route("auth-service", r -> r.path("/auth")
                        .filters(f -> f
                                .filter(jwtAuthFilter)
                                .dedupeResponseHeader("Access-Control-Allow-Origin", String.valueOf(Strategy.RETAIN_FIRST))
                                .rewritePath("/auth", "/api/auth/login"))
                        .uri("http://localhost:8003"))
                .route("auth-service", r -> r.path("/register")
                        .filters(f -> f
                                .filter(jwtAuthFilter)
                                .dedupeResponseHeader("Access-Control-Allow-Origin", String.valueOf(Strategy.RETAIN_FIRST))
                                .rewritePath("/register", "/api/auth"))
                        .uri("http://localhost:8003")).build();

    }
}
