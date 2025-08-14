// src/main/java/com/example/retail/config/RestClientApacheHttpConfig.java
package com.example.retail.config;

import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager;
import org.apache.hc.core5.util.TimeValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestTemplate;

/*
 RestClient configuration using Apache HttpClient 5 with connection pooling.
 Allows tuning of max connections and idle connection eviction for high-throughput scenarios.
*/
@Configuration
public class RestClientApacheHttpConfig {

    private static final Logger logger = LoggerFactory.getLogger(RestClientApacheHttpConfig.class);

    @Value("${base.url}")
    private String baseUrl;

    @Bean("pooledHttpRestClient")
    public RestClient pooledHttpRestClient() {
        logger.info("Configuring RestClient (Apache HttpClient pooled, VT-safe)");
        logger.info("Base URL: {}", baseUrl);

        PoolingHttpClientConnectionManager connManager = new PoolingHttpClientConnectionManager();
        connManager.setMaxTotal(8000);
        connManager.setDefaultMaxPerRoute(4000);
        connManager.setValidateAfterInactivity(TimeValue.ofSeconds(5));

        CloseableHttpClient httpClient = HttpClients.custom()
                .setConnectionManager(connManager)
                .evictExpiredConnections()
                .evictIdleConnections(TimeValue.ofMinutes(2))
                .build();

        return RestClient.builder()
                .baseUrl(baseUrl)
                .requestFactory(new HttpComponentsClientHttpRequestFactory(httpClient))
                .build();
    }

}
