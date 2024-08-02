package com.retry.client.demo;

import okhttp3.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

@SpringBootApplication
@EnableDiscoveryClient
@RestController
@EnableScheduling
public class CallerApplication {
	@Autowired
	WebClientService webClientService;

	public static void main(String[] args) {
		SpringApplication.run(CallerApplication.class, args);
	}


	@GetMapping("service-call")
	public Mono<String> serviceCall() {
		return webClientService.getResponseAsync();
	}

	AtomicLong atomicLong = new AtomicLong(0);
	private final OkHttpClient okHttpClient = new OkHttpClient.Builder()
			.connectTimeout(30, TimeUnit.SECONDS) // Thời gian chờ kết nối
			.readTimeout(60, TimeUnit.SECONDS)    // Thời gian chờ đọc dữ liệu
			.writeTimeout(30, TimeUnit.SECONDS)   // Thời gian chờ ghi dữ liệu
			.build();	private final ExecutorService executorService = Executors.newFixedThreadPool(2);

//	@Scheduled(fixedDelay = 20000)
	public void scheduled() throws InterruptedException {
		atomicLong.set(0);
		for (int i = 0; i < 5000; i++) {
			executorService.submit(this::makeAsyncCall);
		}
		for (int i = 1; i < 16; i++) {
			System.out.println("Sec: "+ i);
			Thread.sleep(1000);
		}
	}

	private void makeAsyncCall() {
		Request request = new Request.Builder()
				.url("http://localhost:9099/service-call")
				.get()
				.build();

		okHttpClient.newCall(request).enqueue(new Callback() {
			@Override
			public void onFailure(Call call, IOException e) {
				e.printStackTrace();
			}

			@Override
			public void onResponse(Call call, Response response) throws IOException {
				try (ResponseBody responseBody = response.body()) {
					if (!response.isSuccessful()) {
						throw new IOException("Unexpected code " + response);
					}
					System.out.println(atomicLong.incrementAndGet() + ": " + responseBody.string());
				}
			}
		});
	}
}
