package com.example.retail.config;

import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.web.embedded.tomcat.TomcatProtocolHandlerCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.beans.factory.annotation.Value;

@Configuration
public class VirtualThreadConfig {

    private static final Logger logger = LoggerFactory.getLogger(VirtualThreadConfig.class);

    @Value("${spring.threads.virtual.enabled:false}")
    private boolean virtualThreadsEnabled;

    @ConditionalOnProperty(name = "spring.threads.virtual.enabled", havingValue = "true", matchIfMissing = false)
    @Bean
    public TomcatProtocolHandlerCustomizer<?> tomcatExecutorCustomizer() {
        logger.info("🚀 Virtual threads ENABLED - configuring Tomcat to use virtual thread executor");
        return protocolHandler -> {
            logger.info("📝 Setting virtual thread executor for protocol handler: {}",
                    protocolHandler.getClass().getSimpleName());
            protocolHandler.setExecutor(Executors.newVirtualThreadPerTaskExecutor());
        };
    }

    // Add a bean that logs the virtual thread status regardless
    @Bean
    public String virtualThreadStatusLogger() {
        if (virtualThreadsEnabled) {
            logger.info("✅ Virtual threads configuration: ENABLED");
        } else {
            logger.info("❌ Virtual threads configuration: DISABLED");
        }
        return "virtualThreadStatusLogged";
    }
}