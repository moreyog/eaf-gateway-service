package com.eaf.gateway.filter;

import com.eaf.gateway.entity.RateLimit;
import com.eaf.gateway.repository.RateLimitRepository;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.Optional;

@Component
public class RateLimitFilter implements GlobalFilter, Ordered, GatewayFilter {

    private final RateLimitRepository rateLimitRepository;

    public RateLimitFilter(RateLimitRepository rateLimitRepository) {
        this.rateLimitRepository = rateLimitRepository;
    }


    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String urlPath = exchange.getRequest().getURI().getPath();
        Optional<RateLimit> rateLimitOptional = rateLimitRepository.findByRouteId(urlPath);


        if (rateLimitOptional.isPresent()) {
            RateLimit rateLimit = rateLimitOptional.get();
            int requestCount = rateLimit.getRequestCount();

            if (isRateLimitExceeded(rateLimit)) {
                exchange.getResponse().setStatusCode(HttpStatus.TOO_MANY_REQUESTS);
                return exchange.getResponse().setComplete();
            } else {
                return chain.filter(exchange).then(Mono.fromRunnable(() -> {
                    rateLimit.setRequestCount(requestCount + 1);
                    rateLimit.setLastRequestTimestamp(LocalDateTime.now());
                    rateLimitRepository.save(rateLimit);
                }));
            }
        }

        return chain.filter(exchange);
    }



    private boolean isRateLimitExceeded(RateLimit rateLimit) {
        LocalDateTime currentTime = LocalDateTime.now();
        int limitForMinutes = rateLimit.getLimitForMinutes();
        int requestCount = rateLimit.getRequestCount();

        LocalDateTime lastRequestTimestamp = rateLimit.getLastRequestTimestamp();
        LocalDateTime limitExpiryTime = lastRequestTimestamp.plusMinutes(limitForMinutes);

        if (requestCount >= limitForMinutes && currentTime.isBefore(limitExpiryTime)) {
            return true;
        } else if (limitExpiryTime.isBefore(currentTime)) {
            // Reset the requestCount when the limitExpiryTime is in the past
            requestCount = 0;
            rateLimit.setRequestCount(requestCount);
            rateLimitRepository.save(rateLimit);
        }

        return false;
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }

    @Override
    public ShortcutType shortcutType() {
        return GatewayFilter.super.shortcutType();
    }
}
