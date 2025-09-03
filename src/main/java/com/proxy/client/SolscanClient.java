package com.proxy.client;

import java.time.Duration;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.util.retry.Retry;

@Slf4j
@Component
@RequiredArgsConstructor
public class SolscanClient {

    @Value("${solscan.api.token.holders}")
    private String tokenHolders;

    private final WebClient webClient;

    public String getTokenHolders(String address, Integer page, Integer pageSize, String fromAmount,
            String toAmount, String token) {
        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                    .path(tokenHolders)
                    .queryParamIfPresent("address", Optional.ofNullable(address))
                    .queryParamIfPresent("page", Optional.ofNullable(page))
                    .queryParamIfPresent("pageSize", Optional.ofNullable(pageSize))            
                    .queryParamIfPresent("fromAmount", Optional.ofNullable(fromAmount))
                    .queryParamIfPresent("toAmount", Optional.ofNullable(toAmount))
                    .build())
                .headers(headers -> {
                    if (token != null && !token.trim().isEmpty()) {
                        headers.set("token", token);
                    }
                })
                .retrieve()
                .onStatus(status -> status.value() == 429, RateLimiter::handleRateLimiting)
                .bodyToMono(String.class)
                .retryWhen(Retry.backoff(3, Duration.ofSeconds(5))
                        .filter(throwable -> throwable.getMessage().contains("Rate limited"))
                        .onRetryExhaustedThrow((spec, signal) -> signal.failure()))
                .block();
    }


}
