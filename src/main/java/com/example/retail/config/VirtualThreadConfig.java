package com.example.retail.config;

import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.web.embedded.tomcat.TomcatProtocolHandlerCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class VirtualThreadConfig {

    private static final Logger logger = LoggerFactory.getLogger(VirtualThreadConfig.class);

    @ConditionalOnProperty(name = "spring.threads.virtual.enabled", havingValue = "true")
    @Bean
    public TomcatProtocolHandlerCustomizer<?> tomcatExecutorCustomizer() {
        logger.info("Virtual threads enabled - configuring Tomcat to use virtual thread executor");
        return protocolHandler -> {
            logger.debug("Setting virtual thread executor for protocol handler: {}", protocolHandler.getClass().getSimpleName());
            protocolHandler.setExecutor(Executors.newVirtualThreadPerTaskExecutor());
        };
    }

}