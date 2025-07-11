package com.example.retail.jfr;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import jdk.jfr.consumer.RecordingStream;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.time.Duration;
import java.util.concurrent.TimeUnit;
// Removed: import java.util.concurrent.atomic.AtomicLong;

/**
 * Component that listens to JFR events for virtual threads and exports metrics to Micrometer,
 * including duration (pinnedTimer) and event counts (pinnedEventCounter) without logging.
 */
@Component
public class JFRVirtualThreadMetrics {

    private final MeterRegistry registry; // Injected and stored for use
    private final Counter startCounter;
    private final Counter endCounter;
    private final Timer pinnedTimer;
    private final Counter pinnedEventCounter;
    // Removed: private final AtomicLong activeVirtualThreads = new AtomicLong(0);

    /**
     * Constructor to initialize Micrometer metrics counters and timer.
     */
    public JFRVirtualThreadMetrics(MeterRegistry registry) {
        this.registry = registry; // Store the registry for later use
        this.startCounter = Counter.builder("jfr_virtual_thread_starts_total")
                .description("Total number of virtual thread starts")
                .register(registry);

        this.endCounter = Counter.builder("jfr_virtual_thread_ends_total")
                .description("Total number of virtual thread terminations")
                .register(registry);

        this.pinnedTimer = Timer.builder("jfr_virtual_thread_pinned_seconds")
                .description("Duration of virtual thread pinning events")
                .register(registry);

        this.pinnedEventCounter = Counter.builder("jfr_virtual_thread_pinned_events_total")
                .description("Total number of virtual thread pinning events")
                .register(registry);

        // Removed: registry.gauge("jfr_virtual_thread_active_current", activeVirtualThreads);
    }

    /**
     * Starts a background JFR recording stream that listens to virtual thread lifecycle events.
     */
    @PostConstruct
    public void startJfrStreaming() {
        Thread jfrThread = Thread.startVirtualThread(() -> {
            try (RecordingStream rs = new RecordingStream()) {
                rs.enable("jdk.VirtualThreadStart");
                rs.enable("jdk.VirtualThreadEnd");
                rs.enable("jdk.VirtualThreadPinned").withThreshold(Duration.ofMillis(20));

                rs.onEvent("jdk.VirtualThreadStart", event -> {
                    startCounter.increment();
                    // Removed: activeVirtualThreads.incrementAndGet();
                });
                rs.onEvent("jdk.VirtualThreadEnd", event -> {
                    endCounter.increment();
                    // Removed: activeVirtualThreads.decrementAndGet();
                });
                rs.onEvent("jdk.VirtualThreadPinned", event -> {
                    long durationNanos = event.getDuration().toNanos();
                    pinnedTimer.record(durationNanos, TimeUnit.NANOSECONDS);
                    pinnedEventCounter.increment(); // Increment total pinning events
                });

                rs.start();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        jfrThread.setName("JFR-VirtualThread-Metrics-Collector");
    }
}