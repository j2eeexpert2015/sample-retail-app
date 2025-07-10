package com.example.retail.jfr;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import jdk.jfr.consumer.RecordingStream;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.time.Duration;
import java.util.concurrent.TimeUnit;

/**
 * Component that listens to JFR events for virtual threads and exports metrics to Micrometer.
 */
@Component
public class JFRVirtualThreadMetrics {

    private final Counter startCounter;
    private final Counter endCounter;
    private final Timer pinnedTimer;

    /**
     * Constructor to initialize Micrometer metrics counters and timer.
     */
    public JFRVirtualThreadMetrics(MeterRegistry registry) {
        this.startCounter = Counter.builder("jfr_virtual_thread_starts_total")
                .description("Total number of virtual thread starts")
                .register(registry);

        this.endCounter = Counter.builder("jfr_virtual_thread_ends_total")
                .description("Total number of virtual thread terminations")
                .register(registry);

        this.pinnedTimer = Timer.builder("jfr_virtual_thread_pinned_seconds")
                .description("Duration of virtual thread pinning events")
                .register(registry);
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

                rs.onEvent("jdk.VirtualThreadStart", event -> startCounter.increment());
                rs.onEvent("jdk.VirtualThreadEnd", event -> endCounter.increment());
                rs.onEvent("jdk.VirtualThreadPinned", event -> {
                    long durationNanos = event.getDuration().toNanos();
                    pinnedTimer.record(durationNanos, TimeUnit.NANOSECONDS);
                });

                rs.start();
            }
        });

        jfrThread.setName("JFR-VirtualThread-Metrics-Collector");
    }
}
