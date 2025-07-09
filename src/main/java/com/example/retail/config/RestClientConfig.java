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

@Configuration
public class RestClientConfig {

    private static final Logger logger = LoggerFactory.getLogger(RestClientConfig.class);

    @Value("${spring.threads.virtual.enabled}")
    private boolean isVirtualThreadEnabled;

    @Value("${base.url}")
    private String baseUrl;

    @Bean
    public RestClient restClient() {
        logger.info("base url: {}", baseUrl);
        var builder = RestClient.builder().baseUrl(baseUrl);

        if (isVirtualThreadEnabled) {
            logger.info("Virtual threads enabled - configuring RestClient with HTTP/1.1");
            builder = builder.requestFactory(new JdkClientHttpRequestFactory(
                    HttpClient.newBuilder()
                            .version(HttpClient.Version.HTTP_1_1) // Enforce HTTP/1.1 to bypass HTTP/2 stream limits
                            .executor(Executors.newVirtualThreadPerTaskExecutor())
                            .build()
            ));
        }

        return builder.build();
    }

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
