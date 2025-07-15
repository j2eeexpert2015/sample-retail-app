package com.example.retail.jfr;


import jdk.jfr.Recording;
import jdk.jfr.consumer.RecordingFile;
import jdk.jfr.consumer.RecordedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import jakarta.annotation.PreDestroy;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

/**
 * Spring Boot compatible JFR utility for monitoring virtual threads in Spring applications.
 * Automatically starts recording when the application is ready and stops when shutting down.
 */
@Component
public class SpringJFRUtil {

    private static final Logger logger = LoggerFactory.getLogger(SpringJFRUtil.class);

    @Value("${jfr.enabled:true}")
    private boolean jfrEnabled;

    @Value("${jfr.output.dir:jfr-recordings}")
    private String outputDir;

    @Value("${jfr.recording.name:SpringBootApp}")
    private String recordingName;

    @Value("${jfr.auto.start:true}")
    private boolean autoStart;

    @Value("${jfr.auto.analyze:true}")
    private boolean autoAnalyze;

    @Value("${jfr.max.duration.minutes:30}")
    private int maxDurationMinutes;

    private Recording currentRecording;

    /**
     * Starts JFR recording when Spring Boot application is ready
     */
    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady() {
        if (jfrEnabled && autoStart) {
            logger.info("🚀 Starting JFR recording for Spring Boot application");
            startRecording();
        }
    }

    /**
     * Stops JFR recording when application shuts down
     */
    @PreDestroy
    public void onApplicationShutdown() {
        if (currentRecording != null) {
            logger.info("🛑 Application shutting down, stopping JFR recording");
            Path jfrFile = stopRecording();

            if (jfrFile != null && autoAnalyze) {
                analyzeRecording(jfrFile);
            }
        }
    }

    /**
     * Manually start a JFR recording
     */
    public Recording startRecording() {
        return startRecording(recordingName);
    }

    /**
     * Manually start a JFR recording with custom name
     */
    public Recording startRecording(String name) {
        if (!jfrEnabled) {
            logger.warn("⚠️ JFR is disabled via configuration");
            return null;
        }

        if (currentRecording != null) {
            logger.warn("⚠️ JFR recording already running, stopping previous recording");
            stopRecording();
        }

        try {
            currentRecording = createVirtualThreadRecording(name);
            currentRecording.start();

            logger.info("🎬 JFR recording started: {} (Max duration: {} minutes)",
                    currentRecording.getName(), maxDurationMinutes);
            return currentRecording;

        } catch (Exception e) {
            logger.error("❌ Failed to start JFR recording", e);
            return null;
        }
    }

    /**
     * Manually stop the current recording
     */
    public Path stopRecording() {
        if (currentRecording == null) {
            logger.warn("⚠️ No active JFR recording to stop");
            return null;
        }

        try {
            currentRecording.stop();
            Path savedFile = saveRecording(currentRecording);
            currentRecording.close();
            currentRecording = null;

            logger.info("⏹️ JFR recording stopped and saved to: {}", savedFile.toAbsolutePath());
            return savedFile;

        } catch (IOException e) {
            logger.error("❌ Error saving JFR recording", e);
            return null;
        }
    }

    /**
     * Check if recording is currently active
     */
    public boolean isRecording() {
        return currentRecording != null;
    }

    /**
     * Get current recording info
     */
    public String getRecordingInfo() {
        if (currentRecording == null) {
            return "No active recording";
        }

        return String.format("Recording: %s, Duration: %s",
                currentRecording.getName(),
                currentRecording.getDuration());
    }

    /**
     * Analyze a JFR recording file
     */
    public void analyzeRecording(Path jfrFile) {
        logger.info("📊 Analyzing JFR Recording: {}", jfrFile.getFileName());

        Map<String, Integer> eventCounts = new HashMap<>();

        try (RecordingFile recordingFile = new RecordingFile(jfrFile)) {
            while (recordingFile.hasMoreEvents()) {
                RecordedEvent event = recordingFile.readEvent();
                String eventType = event.getEventType().getName();

                eventCounts.merge(eventType, 1, Integer::sum);

                // Log virtual thread events
                if (eventType.contains("VirtualThread")) {
                    logVirtualThreadEvent(event);
                }
            }

            logSummary(eventCounts);

        } catch (IOException e) {
            logger.error("❌ Error analyzing JFR file: {}", jfrFile, e);
        }
    }

    /**
     * Creates a JFR recording configured for virtual thread monitoring
     */
    private Recording createVirtualThreadRecording(String name) {
        Recording recording = new Recording();

        // Virtual thread lifecycle events
        recording.enable("jdk.VirtualThreadStart")
                .withStackTrace()
                .withThreshold(Duration.ZERO);

        recording.enable("jdk.VirtualThreadEnd")
                .withStackTrace()
                .withThreshold(Duration.ZERO);

        // Virtual thread specific events
        recording.enable("jdk.VirtualThreadPinned")
                .withStackTrace()
                .withThreshold(Duration.ofMillis(1));

        recording.enable("jdk.VirtualThreadSubmitFailed")
                .withStackTrace();

        // General thread events
        recording.enable("jdk.ThreadStart").withStackTrace();
        recording.enable("jdk.ThreadEnd").withStackTrace();
        recording.enable("jdk.ThreadSleep").withThreshold(Duration.ofMillis(10));

        // Spring-relevant events
        recording.enable("jdk.JavaMonitorEnter").withThreshold(Duration.ofMillis(5));
        recording.enable("jdk.JavaMonitorWait").withThreshold(Duration.ofMillis(5));

        recording.setMaxAge(Duration.ofMinutes(maxDurationMinutes));
        recording.setName(name + "-" + Instant.now().getEpochSecond());

        return recording;
    }

    /**
     * Saves the recording to a file
     */
    private Path saveRecording(Recording recording) throws IOException {
        String timestamp = String.valueOf(Instant.now().getEpochSecond());
        String filename = String.format("%s-%s.jfr",
                recording.getName().replaceAll("[^a-zA-Z0-9-]", ""),
                timestamp);

        Path outputPath = Paths.get(outputDir, filename);
        outputPath.getParent().toFile().mkdirs();

        recording.dump(outputPath);
        return outputPath;
    }

    /**
     * Logs virtual thread event information
     */
    private void logVirtualThreadEvent(RecordedEvent event) {
        String eventType = event.getEventType().getName();
        String threadName = event.getThread() != null ? event.getThread().getJavaName() : "Unknown";
        long threadId = event.getThread() != null ? event.getThread().getJavaThreadId() : -1;

        String emoji = getEventEmoji(eventType);
        logger.debug("{} {}: {} (ID: {})", emoji, eventType, threadName, threadId);
    }

    /**
     * Logs summary statistics
     */
    private void logSummary(Map<String, Integer> eventCounts) {
        logger.info("📈 JFR Event Summary:");

        eventCounts.entrySet().stream()
                .filter(entry -> entry.getKey().contains("Thread"))
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .forEach(entry ->
                        logger.info("  {}: {}", entry.getKey(), entry.getValue()));

        int virtualStarted = eventCounts.getOrDefault("jdk.VirtualThreadStart", 0);
        int virtualEnded = eventCounts.getOrDefault("jdk.VirtualThreadEnd", 0);

        logger.info("🎯 Virtual Thread Metrics:");
        logger.info("  Virtual Threads Created: {}", virtualStarted);
        logger.info("  Virtual Threads Completed: {}", virtualEnded);

        if (virtualStarted > 0) {
            double completionRate = (double) virtualEnded / virtualStarted * 100;
            logger.info("  Completion Rate: {:.1f}%", completionRate);
        }
    }

    /**
     * Returns emoji for event types
     */
    private String getEventEmoji(String eventType) {
        return switch (eventType) {
            case "jdk.VirtualThreadStart" -> "🟢";
            case "jdk.VirtualThreadEnd" -> "🔴";
            case "jdk.VirtualThreadPinned" -> "📌";
            case "jdk.VirtualThreadSubmitFailed" -> "❌";
            default -> "🧵";
        };
    }

    // Getters for configuration (useful for actuator endpoints)
    public boolean isJfrEnabled() { return jfrEnabled; }
    public String getOutputDir() { return outputDir; }
    public String getRecordingName() { return recordingName; }
    public boolean isAutoStart() { return autoStart; }
    public boolean isAutoAnalyze() { return autoAnalyze; }
}
