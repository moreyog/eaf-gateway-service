package com.eaf.gateway.config;

import com.eaf.gateway.filter.RateLimitFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GatewayConfiguration {

//    @Autowired
//    private RateLimitFilter rateLimitFilter;
//
//    @Bean
//    public RouteLocator routeLocator(RouteLocatorBuilder builder) {
//        return builder.routes()
//                .route("identity-service", r -> r.path("/auth/token")
//                        .filters(f -> f.filter(rateLimitFilter))
//                        .uri("http://localhost:8080"))
//                .build();
//    }
}
