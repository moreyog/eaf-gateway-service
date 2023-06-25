package com.eaf.gateway.filter;

import com.eaf.gateway.entity.RateLimit;
import com.eaf.gateway.repository.RateLimitRepository;
import com.eaf.gateway.service.JwtService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.Date;

@Component
public class RateLimitFilter extends AbstractGatewayFilterFactory<RateLimitFilter.Config> {

    @Autowired
    private RateLimitRepository rateLimitRepository;

    @Autowired
    private JwtService jwtService;

    @Autowired
    public RateLimitFilter() {
        super(Config.class);
        //this.rateLimitRepository = rateLimitRepository;
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            // Get IP address from the request
            String ipAddress =  exchange.getRequest().getRemoteAddress().getAddress().getHostAddress();

            // Get user ID and IP address from the request - OPEN SAVE USER NAME
//            String username = null;
//            String authHeader = exchange.getRequest().getHeaders().get(HttpHeaders.AUTHORIZATION) != null ?  exchange.getRequest().getHeaders().get(HttpHeaders.AUTHORIZATION).get(0): null;
//            if (authHeader != null && authHeader.startsWith("Bearer ")) {
//                String token = authHeader.substring(7);
//                username = jwtService.extractUsername(token);
//            } else {
//                // Means public end point called - ex. auth, token - check for default ip based limit
//            }

            // Calculate the current date
            Date currentDate = java.sql.Date.valueOf(LocalDate.now());
            System.out.println("rateLimitRepository " + rateLimitRepository);

            // Find the rate limit entity in the database
            RateLimit rateLimitEntity = rateLimitRepository
                    .findByIpAddressAndRequestDate(ipAddress, currentDate);

//            RateLimit rateLimitEntity = rateLimitRepository
//                    .findByUserIdAndIpAddressAndRequestDate(username, ipAddress, currentDate);

            // If the rate limit entity doesn't exist, create a new one
            if (rateLimitEntity == null) {
                rateLimitEntity = new RateLimit();
                //rateLimitEntity.setUserId(username);
                rateLimitEntity.setIpAddress(ipAddress);
                rateLimitEntity.setRequestDate(currentDate);
                rateLimitEntity.setRequestCount(1);
            } else {
                // If the rate limit entity exists, check if the request count exceeds the limit
                if (rateLimitEntity.getRequestCount() >= config.getRequestLimit()) {
                    // Return an error response or take appropriate action
                    exchange.getResponse().setStatusCode(HttpStatus.TOO_MANY_REQUESTS);
                    return exchange.getResponse().setComplete();
                }

                // Increment the request count
                rateLimitEntity.setRequestCount(rateLimitEntity.getRequestCount() + 1);
            }

            // Save the rate limit entity in the database
            rateLimitRepository.save(rateLimitEntity);

            // Proceed with the request
            return chain.filter(exchange);
        };
    }

    public static class Config {
        private String userPlan;

        public int getRequestLimit() {
            // Retrieve the request limit based on user plan
            if ("gold".equalsIgnoreCase(userPlan)) {
                return 200; // Gold plan request limit
            } else if ("silver".equalsIgnoreCase(userPlan)) {
                return 50; // Silver plan request limit
            } else {
                return 10; // Default request limit (fallback value) for open end point
            }
        }

        public String getUserPlan() {
            return userPlan;
        }

        public void setUserPlan(String userPlan) {
            this.userPlan = userPlan;
        }
    }

// Add for ref. Other apporach based on ur

//
//    @Id
//    @Column(length = 50)
//    private String routeId; //url
//
//    private int limitForMinutes;
//    private int requestCount;
//    private LocalDateTime lastRequestTimestamp;


//    private boolean isRateLimitExceeded(RateLimit rateLimit) {
//        LocalDateTime currentTime = LocalDateTime.now();
//        int limitForMinutes = rateLimit.getLimitForMinutes();
//        int requestCount = rateLimit.getRequestCount();
//
//        LocalDateTime lastRequestTimestamp = rateLimit.getLastRequestTimestamp();
//        LocalDateTime limitExpiryTime = lastRequestTimestamp.plusMinutes(limitForMinutes);
//
//        if (requestCount >= limitForMinutes && currentTime.isBefore(limitExpiryTime)) {
//            return true;
//        } else if (limitExpiryTime.isBefore(currentTime)) {
//            // Reset the requestCount when the limitExpiryTime is in the past
//            requestCount = 0;
//            rateLimit.setRequestCount(requestCount);
//            rateLimitRepository.save(rateLimit);
//        }
//
//        return false;
//    }
}
