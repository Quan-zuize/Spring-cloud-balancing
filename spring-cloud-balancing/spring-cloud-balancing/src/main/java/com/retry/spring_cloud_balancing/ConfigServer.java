package com.retry.spring_cloud_balancing;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

@FeignClient(name = "SERVICE-TEST", path = "/test-api", configuration = FeignConfig.class)
public interface ConfigServer {
    @GetMapping("/service-test")
    String getResponse();
}
