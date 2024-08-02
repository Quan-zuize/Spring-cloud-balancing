package com.retry.spring_cloud_balancing;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
@EnableEurekaServer
@EnableFeignClients
@RestController
public class SpringCloudBalancingApplication {

    @Autowired
    ConfigServer configServer;



    public static void main(String[] args) {
        SpringApplication.run(SpringCloudBalancingApplication.class, args);
    }

//    @GetMapping("service-call")
//    public String getResponse() {
//        return configServer.getResponse();
//    }
}
