// src/main/java/com/example/retail/config/RestClientConfig.java
package com.example.retail.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import java.net.http.HttpClient;
import java.time.Duration;
import java.util.concurrent.Executors;

/*
 JDK HttpClient-backed RestClient.
 - If spring.threads.virtual.enabled=true, attach a newVirtualThreadPerTaskExecutor to run outbound calls on VTs.
 - If false, use the default executor (no extra VT layer).
 The bean name avoids implying VT is always in use.
*/
@Configuration
public class RestClientConfig {

    private static final Logger logger = LoggerFactory.getLogger(RestClientConfig.class);

    @Value("${base.url}")
    private String baseUrl;

    @Value("${spring.threads.virtual.enabled:false}")
    private boolean springVirtualThreadsEnabled;

    @Bean("restClient")
    public RestClient jdkHttpRestClient() {
        logger.info("Configuring JDK HttpClient RestClient. baseUrl={}, springVT={}", baseUrl, springVirtualThreadsEnabled);

        HttpClient.Builder builder = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(5))
                .version(HttpClient.Version.HTTP_1_1);

        if (springVirtualThreadsEnabled) {
            // Apply VT-per-task only when Spring VT is enabled, per request.
            builder.executor(Executors.newVirtualThreadPerTaskExecutor());
        }

        HttpClient httpClient = builder.build();

        return RestClient.builder()
                .baseUrl(baseUrl)
                .requestFactory(new JdkClientHttpRequestFactory(httpClient))
                .build();
    }
}
