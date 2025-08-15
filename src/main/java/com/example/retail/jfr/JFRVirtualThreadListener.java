package com.example.retail.jfr;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import jdk.jfr.consumer.RecordingStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import jakarta.annotation.PreDestroy;
import java.util.concurrent.TimeUnit;

/*
 * JFR Virtual Thread Listener - Spring Boot Integration
 *
 * Spring Boot component that integrates JFR virtual thread monitoring with the application lifecycle.
 * Uses JFRVirtualThreadUtil for core JFR operations and focuses on:
 * - Spring Boot application lifecycle integration
 * - Prometheus metrics export
 * - Automatic startup/shutdown handling
 * - Configuration via Spring properties
 *
 * This listener automatically:
 * - Starts JFR monitoring when Spring Boot application is ready
 * - Exports virtual thread metrics to Prometheus
 * - Handles graceful shutdown during application termination
 * - Respects configuration properties for enabling/disabling
 *
 * Key Features:
 * - Automatic lifecycle management tied to Spring Boot
 * - Real-time metrics export to Prometheus/Micrometer
 * - Configuration via application.properties
 * - Graceful error handling and logging
 * - No manual intervention required
 *
 * Exported Prometheus Metrics:
 * - jfr_virtual_thread_starts_total - Total VT creations
 * - jfr_virtual_thread_ends_total - Total VT completions
 * - jfr_virtual_thread_pinned_seconds - VT pinning duration histogram
 * - jfr_virtual_thread_pinned_events_total - Total pinning events
 */
@Component
public class JFRVirtualThreadListener {

    private static final Logger logger = LoggerFactory.getLogger(JFRVirtualThreadListener.class);

    @Value("${jfr.enabled:true}")
    private boolean jfrEnabled;

    // Prometheus metrics for virtual thread monitoring
    private final Counter startCounter;
    private final Counter endCounter;
    private final Timer pinnedTimer;
    private final Counter pinnedEventCounter;

    // JFR event stream for live monitoring
    private RecordingStream liveStream;

    /*
     * Constructor - initializes Prometheus metrics registry.
     *
     * Called by: Spring framework during application startup (dependency injection)
     * When: During Spring context initialization, before any other methods
     * Purpose: Sets up Micrometer metrics that will be exported to Prometheus
     * Integration: Metrics are automatically scraped by Prometheus via /actuator/prometheus
     */
    public JFRVirtualThreadListener(MeterRegistry registry) {
        this.startCounter = Counter.builder("jfr_virtual_thread_starts_total")
                .description("Total number of virtual thread start events detected by JFR")
                .register(registry);

        this.endCounter = Counter.builder("jfr_virtual_thread_ends_total")
                .description("Total number of virtual thread end events detected by JFR")
                .register(registry);

        this.pinnedTimer = Timer.builder("jfr_virtual_thread_pinned_seconds")
                .description("Duration histogram of virtual thread pinning events")
                .register(registry);

        this.pinnedEventCounter = Counter.builder("jfr_virtual_thread_pinned_events_total")
                .description("Total number of virtual thread pinning events detected")
                .register(registry);

        logger.debug("JFR Virtual Thread Listener initialized with Prometheus metrics");
    }

    /*
     * Automatically starts JFR live streaming when Spring Boot application is fully ready.
     *
     * Called by: Spring framework automatically via event system
     * When: After all Spring beans are initialized and application context is fully ready
     * Trigger: ApplicationReadyEvent is published by Spring Boot
     * Purpose: Ensures JFR monitoring starts only after application is operational
     * Configuration: Respects jfr.enabled property to allow disabling if needed
     */
    @EventListener(ApplicationReadyEvent.class)
    public void startJFRMonitoring() {
        if (!jfrEnabled) {
            logger.info("JFR monitoring disabled via configuration (jfr.enabled=false)");
            return;
        }

        if (!JFRVirtualThreadUtil.isJFRAvailable()) {
            logger.warn("JFR is not available in this JVM - virtual thread monitoring disabled");
            return;
        }

        logger.info("🚀 Starting JFR Virtual Thread monitoring");
        startLiveEventStreaming();
    }

    /*
     * Automatically stops JFR monitoring during application shutdown.
     *
     * Called by: Spring framework automatically during shutdown
     * When: Application shutdown sequence, before Spring context is destroyed
     * Trigger: JVM shutdown hooks, Spring context close, or application termination
     * Purpose: Ensures clean resource cleanup and prevents memory leaks
     * Implementation: Safely closes JFR streams even if errors occur
     */
    @PreDestroy
    public void stopJFRMonitoring() {
        logger.info("🛑 Stopping JFR Virtual Thread monitoring");

        if (liveStream != null) {
            JFRVirtualThreadUtil.closeStream(liveStream);
            liveStream = null;
        }

        logger.debug("JFR monitoring cleanup completed");
    }

    /*
     * Starts real-time JFR event streaming in a background virtual thread.
     *
     * Called by: startJFRMonitoring() method during application startup
     * When: Application is ready and JFR monitoring is enabled
     * Purpose: Sets up continuous monitoring of virtual thread events
     * Implementation: Uses JFRVirtualThreadUtil for core JFR operations
     * Threading: Runs in dedicated virtual thread to avoid blocking application startup
     * Event Processing: Converts JFR events to Prometheus metrics in real-time
     */
    private void startLiveEventStreaming() {
        Thread.startVirtualThread(() -> {
            try {
                logger.debug("Starting JFR event stream processing thread");

                // Create event stream with metric-updating handlers
                liveStream = JFRVirtualThreadUtil.createEventStream(
                        this::handleVirtualThreadStart,
                        this::handleVirtualThreadEnd,
                        this::handleVirtualThreadPinned
                );

                logger.info("📊 JFR live event streaming started - monitoring virtual thread events");

                // Start streaming (this blocks until stream is closed)
                liveStream.start();

            } catch (Exception e) {
                logger.error("❌ Failed to start JFR event streaming: {}", e.getMessage(), e);
            }
        });
    }

    /*
     * Handles virtual thread start events from JFR stream.
     *
     * Called by: JFR event stream when jdk.VirtualThreadStart event occurs
     * When: Each time a new virtual thread is created in the application
     * Purpose: Increments Prometheus counter for virtual thread creations
     * Frequency: High - called for every VT creation (potentially thousands per second)
     */
    private void handleVirtualThreadStart(jdk.jfr.consumer.RecordedEvent event) {
        startCounter.increment();

        // Log only occasionally to avoid log spam
        if (startCounter.count() % 1000 == 0) {
            logger.debug("Virtual thread starts: {}", (long) startCounter.count());
        }
    }

    /*
     * Handles virtual thread end events from JFR stream.
     *
     * Called by: JFR event stream when jdk.VirtualThreadEnd event occurs
     * When: Each time a virtual thread completes execution
     * Purpose: Increments Prometheus counter for virtual thread completions
     * Monitoring: Helps track VT lifecycle and detect potential leaks
     */
    private void handleVirtualThreadEnd(jdk.jfr.consumer.RecordedEvent event) {
        endCounter.increment();

        // Log only occasionally to avoid log spam
        if (endCounter.count() % 1000 == 0) {
            logger.debug("Virtual thread ends: {}", (long) endCounter.count());
        }
    }

    /*
     * Handles virtual thread pinning events from JFR stream.
     *
     * Called by: JFR event stream when jdk.VirtualThreadPinned event occurs
     * When: Virtual thread becomes pinned to carrier thread (performance concern)
     * Purpose: Records pinning duration and increments event counter
     * Performance Impact: Pinning reduces VT efficiency - important to monitor
     * Metrics: Updates both duration histogram and event counter
     */
    private void handleVirtualThreadPinned(jdk.jfr.consumer.RecordedEvent event) {
        // Extract pinning duration and record in timer histogram
        long durationNanos = event.getDuration().toNanos();
        pinnedTimer.record(durationNanos, TimeUnit.NANOSECONDS);

        // Increment total pinning event counter
        pinnedEventCounter.increment();

        // Log pinning events as they indicate potential performance issues
        long durationMs = durationNanos / 1_000_000;
        logger.debug("📌 Virtual thread pinned for {}ms (event #{})",
                durationMs, (long) pinnedEventCounter.count());

        // Warn for long pinning events that may impact performance
        if (durationMs > 100) {
            logger.warn("⚠️ Long virtual thread pinning detected: {}ms - consider reviewing synchronized blocks", durationMs);
        }
    }

    /*
     * Gets current monitoring statistics for health checks or status reporting.
     *
     * Called by: Other components that need monitoring status
     * When: Health checks, status endpoints, or diagnostic purposes
     * Purpose: Provides current metric values and monitoring state
     * Returns: Formatted string with current statistics
     */
    public String getMonitoringStats() {
        if (liveStream == null) {
            return "JFR monitoring not active";
        }

        return String.format(
                "JFR Monitoring: Active | Starts: %d | Ends: %d | Pinning Events: %d",
                (long) startCounter.count(),
                (long) endCounter.count(),
                (long) pinnedEventCounter.count()
        );
    }

    /*
     * Checks if JFR monitoring is currently active.
     *
     * Called by: Components that need to verify monitoring status
     * When: Status checks or conditional logic based on monitoring state
     * Purpose: Provides boolean indication of monitoring activity
     * Returns: true if live stream is active, false otherwise
     */
    public boolean isMonitoring() {
        return liveStream != null;
    }
}
