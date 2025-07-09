package com.example.retail.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestTemplate;

import java.net.http.HttpClient;
import java.util.concurrent.Executors;

//@Configuration
public class RestClientConfig {

    private static final Logger logger = LoggerFactory.getLogger(RestClientConfig.class);

    @Value("${base.url}")
    private String baseUrl;

    @Bean
    public RestClient restClient() {
        logger.info("Configuring RestClient with JDK HttpClient and virtual thread executor");
        logger.info("Base URL: {}", baseUrl);

        HttpClient httpClient = HttpClient.newBuilder()
                .executor(Executors.newVirtualThreadPerTaskExecutor())
                .version(HttpClient.Version.HTTP_1_1)
                .build();

        return RestClient.builder()
                .baseUrl(baseUrl)
                .requestFactory(new JdkClientHttpRequestFactory(httpClient))
                .build();
    }

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
