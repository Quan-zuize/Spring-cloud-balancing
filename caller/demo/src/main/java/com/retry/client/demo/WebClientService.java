package com.retry.client.demo;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;
import reactor.util.retry.RetryBackoffSpec;

import java.time.Duration;

@Service
@Slf4j
public class WebClientService {
    private final WebClient.Builder webClientBuilder;
    private final long INITIAL_INTERVAL = 50;
    private final int MAX_RETRIES = 3;
    private final double JITTER = 0.5;


    public WebClientService(WebClient.Builder webClientBuilder) {
        this.webClientBuilder = webClientBuilder;
    }

    public Mono<String> getResponseAsync() {
        return webClientBuilder
                .build()
                .get()
                .uri("http://SERVICE-TEST/test-api/service-test")
                .retrieve()
                .bodyToMono(String.class)
                .retryWhen(retrySpec())
                .doOnError(RuntimeException.class, (msg) -> {
                    log.error("{}", msg.getMessage());
                });
    }

    private RetryBackoffSpec retrySpec() {
        return Retry.backoff(MAX_RETRIES, Duration.ofMillis(INITIAL_INTERVAL))
//                .maxBackoff(Duration.ofSeconds(1))
                .minBackoff(Duration.ZERO)
                .jitter(JITTER)
                .doAfterRetry(retrySignal -> log.info("Retried {}", retrySignal.totalRetries()))
                .onRetryExhaustedThrow((_, _)
                        -> new RuntimeException("External Service failed to process after max retries, error code " + HttpStatus.SERVICE_UNAVAILABLE.value())
                );
    }

//    @Scheduled(fixedRate = 5000)
//    public void printInstances() {
//        List<String> services = discoveryClient.getServices();
//        services.forEach(serviceName -> {
//            this.discoveryClient.getInstances(serviceName).forEach(instance ->{
//                log.debug("Instance ID: {}, Port: {}", instance.getInstanceId(), instance.getPort());
//            });
//        });
//    }
}
