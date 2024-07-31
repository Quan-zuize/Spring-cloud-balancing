package com.retry.spring_cloud_balancing;

import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;
import reactor.util.retry.RetryBackoffSpec;

import java.time.Duration;

@Service
public class WebClientService {
    private final WebClient.Builder webClientBuilder;
    private final long INITIAL_INTERVAL = 500;
    private final double MULTIPLIER = 1.5;
    private final int MAX_RETRIES = 10;
    private final double JITTER = 0.5;

    public WebClientService(WebClient.Builder webClientBuilder) {
        this.webClientBuilder = webClientBuilder;
    }

    public Mono<String> getResponseAsync() {
        return webClientBuilder.build()
                .get()
                .uri("http://SERVICE-TEST/test-api/service-test")
                .retrieve()
                .bodyToMono(String.class)
                .retryWhen(retrySpec());
    }

    private RetryBackoffSpec retrySpec() {
        return Retry.backoff(MAX_RETRIES, Duration.ofMillis(INITIAL_INTERVAL))
                .maxBackoff(Duration.ofMillis(INITIAL_INTERVAL * (long) Math.pow(MULTIPLIER, MAX_RETRIES)))
                .jitter(JITTER)
                .filter(throwable -> throwable instanceof WebClientResponseException)
                .onRetryExhaustedThrow((_, retrySignal) -> retrySignal.failure());
    }
}
