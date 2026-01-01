package com.waterquality.gateway.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * Global filter for logging all requests passing through the API Gateway.
 * 
 * This filter logs:
 * - Incoming request method and path
 * - Target service being routed to
 * - Response status code
 * 
 * Useful for:
 * - Debugging routing issues
 * - Monitoring traffic
 * - Audit trail
 * - Performance tracking
 * 
 * @author KF7014 Advanced Programming
 * @version 1.0
 */
@Component
@Slf4j
public class LoggingFilter implements GlobalFilter, Ordered {

    /**
     * Filters every request passing through the gateway.
     * Logs request details and response status.
     * 
     * @param exchange the current server exchange
     * @param chain the gateway filter chain
     * @return Mono<Void> representing completion
     */
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String path = exchange.getRequest().getPath().toString();
        String method = exchange.getRequest().getMethod().toString();

        log.info("→ Incoming request: {} {}", method, path);

        // Determine which service this is going to
        String targetService = "Unknown";
        if (path.startsWith("/data")) {
            targetService = "Data Service (8081)";
        } else if (path.startsWith("/rewards")) {
            targetService = "Rewards Service (8082)";
        }

        log.info("  Routing to: {}", targetService);

        // Continue the filter chain and log response
        return chain.filter(exchange).then(Mono.fromRunnable(() -> {
            int statusCode = exchange.getResponse().getStatusCode() != null ?
                    exchange.getResponse().getStatusCode().value() : 0;
            log.info("← Response: {} {} → Status: {}", method, path, statusCode);
        }));
    }

    /**
     * Sets the order of this filter in the filter chain.
     * Lower values have higher priority.
     * 
     * @return order value (-1 means high priority)
     */
    @Override
    public int getOrder() {
        return -1; // High priority - run first
    }
}
