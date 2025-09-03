package com.proxy.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.reactive.function.client.ClientResponse;
import reactor.core.publisher.Mono;

import java.time.Duration;

@Slf4j
public class RateLimiter {
	
	private static final int DEFAULT_RETRY_SECONDS = 10;

	public static Mono<? extends Throwable> handleRateLimiting(ClientResponse response) {
	    String retryAfterHeader = response.headers().asHttpHeaders().getFirst("Retry-After");

	    long waitSeconds = DEFAULT_RETRY_SECONDS;
	    try {
	        if (retryAfterHeader != null) {
				double retryAfterDouble = Double.parseDouble(retryAfterHeader);
				waitSeconds = (long) Math.ceil(retryAfterDouble);
	        }
	    } catch (NumberFormatException e) {
	        log.warn("Invalid Retry-After header: '{}'. Using default {}s", retryAfterHeader, DEFAULT_RETRY_SECONDS);
	    }

	    long finalDelay = waitSeconds;
	    log.warn("Rate limit hit (429). Will delay {} seconds then retry...", finalDelay);

	    return Mono.delay(Duration.ofSeconds(finalDelay))
	        .then(Mono.error(new RuntimeException("Rate limited: retrying after " + finalDelay + "s")));
	}

}
