package com.example.retail.jfr;

import jdk.jfr.Recording;
import jdk.jfr.consumer.RecordedEvent;
import jdk.jfr.consumer.RecordingFile;
import jdk.jfr.consumer.RecordingStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

/*
 * JFR Virtual Thread Utility Class
 *
 * Pure Java utility for JFR virtual thread operations with no Spring dependencies.
 * Provides core functionality for:
 * - Creating and managing JFR recordings
 * - Setting up JFR event streaming
 * - Handling virtual thread lifecycle events
 * - File operations for JFR recordings
 * - Null-safe recording information retrieval
 *
 * This utility can be used in any Java application, not just Spring Boot.
 * It encapsulates all JFR-specific logic and provides a clean API for:
 * - Starting/stopping JFR recordings
 * - Configuring event handlers
 * - Managing recording streams
 * - Saving recordings to disk
 * - Safe handling of recording state transitions
 *
 * Key Features:
 * - Framework agnostic - no Spring dependencies
 * - Event-driven architecture with customizable handlers
 * - Proper resource management and cleanup
 * - Configurable thresholds and settings
 * - Thread-safe operations
 * - Null-safe duration handling for active recordings
 */
public class JFRVirtualThreadUtil {

    private static final Logger logger = LoggerFactory.getLogger(JFRVirtualThreadUtil.class);

    // Default configuration values
    private static final Duration DEFAULT_PINNING_THRESHOLD = Duration.ofMillis(1);
    private static final String DEFAULT_OUTPUT_DIR = "jfr-recordings";

    /*
     * Creates a new JFR recording configured for virtual thread monitoring.
     *
     * Sets up a JFR recording with all relevant virtual thread events enabled
     * including lifecycle events, pinning detection, and scheduler failures.
     * The recording is configured with appropriate thresholds and metadata.
     */
    public static Recording createVirtualThreadRecording(String name) {
        Recording recording = new Recording();

        // Enable virtual thread lifecycle events with zero threshold for complete capture
        recording.enable("jdk.VirtualThreadStart")
                .withStackTrace()
                .withThreshold(Duration.ZERO);

        recording.enable("jdk.VirtualThreadEnd")
                .withStackTrace()
                .withThreshold(Duration.ZERO);

        // Enable pinning events with minimal threshold for early detection
        recording.enable("jdk.VirtualThreadPinned")
                .withStackTrace()
                .withThreshold(Duration.ofMillis(1));

        // Enable submit failed events to detect scheduler overwhelm
        recording.enable("jdk.VirtualThreadSubmitFailed")
                .withStackTrace();

        // Set recording metadata with timestamp for uniqueness
        recording.setName(name + "-" + Instant.now().getEpochSecond());
        recording.setMaxAge(Duration.ofMinutes(30)); // Prevent excessive memory usage

        logger.debug("Created JFR recording: {}", recording.getName());
        return recording;
    }

    /*
     * Creates and configures a JFR RecordingStream for real-time event processing.
     *
     * Sets up continuous event streaming with customizable handlers for different
     * virtual thread events. The stream processes events as they occur, enabling
     * real-time monitoring and metrics collection.
     */
    public static RecordingStream createEventStream(
            Consumer<jdk.jfr.consumer.RecordedEvent> onStart,
            Consumer<jdk.jfr.consumer.RecordedEvent> onEnd,
            Consumer<jdk.jfr.consumer.RecordedEvent> onPinned) {

        RecordingStream stream = new RecordingStream();

        // Configure events with appropriate thresholds for real-time processing
        stream.enable("jdk.VirtualThreadStart");
        stream.enable("jdk.VirtualThreadEnd");
        stream.enable("jdk.VirtualThreadPinned").withThreshold(DEFAULT_PINNING_THRESHOLD);

        // Register event handlers with null safety
        if (onStart != null) {
            stream.onEvent("jdk.VirtualThreadStart", onStart);
        }
        if (onEnd != null) {
            stream.onEvent("jdk.VirtualThreadEnd", onEnd);
        }
        if (onPinned != null) {
            stream.onEvent("jdk.VirtualThreadPinned", onPinned);
        }

        logger.debug("Created JFR event stream with handlers");
        return stream;
    }

    /*
     * Saves a JFR recording to disk with proper file naming and directory structure.
     *
     * Creates the output directory if it doesn't exist and generates a unique
     * filename based on the recording name. The resulting .jfr file can be
     * opened with JDK Mission Control or other JFR analysis tools.
     */
    public static Path saveRecording(Recording recording, String outputDir) throws Exception {
        if (recording == null) {
            throw new IllegalArgumentException("Recording cannot be null");
        }

        // Generate filename with recording name and ensure .jfr extension
        String filename = recording.getName() + ".jfr";
        Path outputPath = Paths.get(outputDir != null ? outputDir : DEFAULT_OUTPUT_DIR, filename);

        // Ensure output directory exists
        outputPath.getParent().toFile().mkdirs();

        // Save recording to disk in standard JFR format
        recording.dump(outputPath);

        logger.info("JFR recording saved: {}", outputPath.toAbsolutePath());
        return outputPath;
    }

    /*
     * Safely closes a RecordingStream with comprehensive error handling.
     *
     * Ensures proper resource cleanup during application shutdown or when
     * stopping live monitoring. Handles exceptions gracefully to prevent
     * shutdown issues and resource leaks.
     */
    public static void closeStream(RecordingStream stream) {
        if (stream == null) {
            return;
        }

        try {
            stream.close();
            logger.debug("JFR recording stream closed successfully");
        } catch (Exception e) {
            logger.warn("Error closing JFR recording stream: {}", e.getMessage());
        }
    }

    /*
     * Safely stops and closes a JFR recording with proper state management.
     *
     * Handles recording lifecycle transitions safely, ensuring recordings
     * are properly finalized before resource cleanup. Includes state validation
     * to prevent errors during recording termination.
     */
    public static void stopRecording(Recording recording) {
        if (recording == null) {
            return;
        }

        try {
            // Only stop if recording is actually running
            if (recording.getState().name().equals("RUNNING")) {
                recording.stop();
                logger.debug("JFR recording stopped: {}", recording.getName());
            }
            recording.close();
            logger.debug("JFR recording closed: {}", recording.getName());
        } catch (Exception e) {
            logger.warn("Error stopping JFR recording: {}", e.getMessage());
        }
    }

    /*
     * Validates JFR availability and functionality in the current JVM.
     *
     * Performs a simple JFR operation test to ensure the JVM supports
     * JFR operations and that JFR is enabled. This prevents runtime
     * errors when attempting to create recordings or streams.
     */
    public static boolean isJFRAvailable() {
        try {
            // Test JFR availability with minimal overhead
            Recording testRecording = new Recording();
            testRecording.close();
            return true;
        } catch (Exception e) {
            logger.warn("JFR is not available: {}", e.getMessage());
            return false;
        }
    }

    /*
     * Gets recording information with null-safe duration handling.
     *
     * Provides human-readable recording state information while safely
     * handling cases where duration might be null (such as freshly started
     * recordings or recordings in transition states).
     */
    public static String getRecordingInfo(Recording recording) {
        if (recording == null) {
            return "No active recording";
        }

        // Handle null duration for freshly started recordings
        Duration duration = recording.getDuration();
        String durationStr = duration != null ? duration.toString() : "Running (no duration yet)";

        return String.format("Recording: %s, State: %s, Duration: %s",
                recording.getName(),
                recording.getState(),
                durationStr);
    }

    /*
     * Analyzes a JFR recording file and provides comprehensive virtual thread metrics.
     *
     * Processes all events in the recording file, counts different event types,
     * and provides detailed analysis of virtual thread behavior including
     * lifecycle metrics, pinning events, and performance indicators.
     */
    public static void analyzeRecording(Path jfrFile) {
        logger.info("📊 Analyzing JFR Recording: {}", jfrFile.getFileName());

        Map<String, Integer> eventCounts = new HashMap<>();

        try (RecordingFile recordingFile = new RecordingFile(jfrFile)) {
            while (recordingFile.hasMoreEvents()) {
                RecordedEvent event = recordingFile.readEvent();
                String eventType = event.getEventType().getName();

                eventCounts.merge(eventType, 1, Integer::sum);

                // Log significant virtual thread events for detailed analysis
                if (eventType.contains("VirtualThread")) {
                    logVirtualThreadEvent(event);
                }
            }

            // Provide comprehensive summary of recording contents
            logSummary(eventCounts);

        } catch (IOException e) {
            logger.error("❌ Error analyzing JFR file: {}", jfrFile, e);
        }
    }

    /*
     * Logs individual virtual thread events with intelligent filtering.
     *
     * Focuses on significant events that indicate performance issues or
     * system behavior. Start/end events are skipped to reduce log volume
     * while pinning and scheduler failures are highlighted as they indicate
     * potential performance problems.
     */
    private static void logVirtualThreadEvent(RecordedEvent event) {
        String eventType = event.getEventType().getName();
        String emoji = getEventEmoji(eventType);

        switch (eventType) {
            case "jdk.VirtualThreadStart":
            case "jdk.VirtualThreadEnd":
                // Skip logging these high-frequency events to reduce noise
                // Summary statistics are provided in logSummary instead
                break;

            case "jdk.VirtualThreadPinned":
                long durationMs = event.getDuration().toMillis();
                logger.info("{} VT Pinned for {}ms", emoji, durationMs);

                // Highlight long pinning events as potential performance issues
                if (durationMs > 100) {
                    logger.warn("⚠️ Long pinning event detected: {}ms - may impact performance", durationMs);
                }
                break;

            case "jdk.VirtualThreadSubmitFailed":
                logger.warn("{} VT Submit Failed - Scheduler may be overwhelmed!", emoji);
                break;
        }
    }

    /*
     * Provides comprehensive summary statistics for JFR recording analysis.
     *
     * Calculates and displays key virtual thread metrics including creation
     * and completion rates, pinning statistics, and scheduler health indicators.
     * Includes performance warnings and recommendations based on observed patterns.
     */
    private static void logSummary(Map<String, Integer> eventCounts) {
        logger.info("📈 JFR Event Summary:");

        // Display all thread-related events sorted by frequency
        eventCounts.entrySet().stream()
                .filter(entry -> entry.getKey().contains("Thread"))
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .forEach(entry ->
                        logger.info("  {}: {}", entry.getKey(), entry.getValue()));

        // Extract virtual thread specific metrics
        int virtualStarted = eventCounts.getOrDefault("jdk.VirtualThreadStart", 0);
        int virtualEnded = eventCounts.getOrDefault("jdk.VirtualThreadEnd", 0);
        int pinningEvents = eventCounts.getOrDefault("jdk.VirtualThreadPinned", 0);
        int submitFailed = eventCounts.getOrDefault("jdk.VirtualThreadSubmitFailed", 0);

        logger.info("🎯 Virtual Thread Metrics:");
        logger.info("  Virtual Threads Created: {}", virtualStarted);
        logger.info("  Virtual Threads Completed: {}", virtualEnded);
        logger.info("  Still Running: {}", virtualStarted - virtualEnded);
        logger.info("  Pinning Events: {}", pinningEvents);
        logger.info("  Submit Failures: {}", submitFailed);

        // Calculate and display completion rate if threads were created
        if (virtualStarted > 0) {
            double completionRate = (double) virtualEnded / virtualStarted * 100;
            logger.info("  Completion Rate: %.1f%%", completionRate);
        }

        // Provide performance warnings and recommendations
        if (pinningEvents > 0) {
            logger.warn("⚠️ {} pinning events detected - consider reviewing synchronized blocks", pinningEvents);
            logger.info("💡 TIP: Use ReentrantLock or other non-blocking constructs to reduce pinning");
        }

        if (submitFailed > 0) {
            logger.error("❌ {} submit failures - virtual thread scheduler may be overwhelmed", submitFailed);
            logger.info("💡 TIP: Consider reducing concurrent virtual thread creation rate");
        }

        // Provide overall assessment
        if (pinningEvents == 0 && submitFailed == 0) {
            logger.info("✅ Good virtual thread performance - no pinning or scheduler issues detected");
        }
    }

    /*
     * Returns appropriate emoji indicators for different virtual thread event types.
     *
     * Provides visual cues in logs to quickly identify event types and their
     * significance for virtual thread performance analysis.
     */
    private static String getEventEmoji(String eventType) {
        return switch (eventType) {
            case "jdk.VirtualThreadStart" -> "🟢";
            case "jdk.VirtualThreadEnd" -> "🔴";
            case "jdk.VirtualThreadPinned" -> "📌";
            case "jdk.VirtualThreadSubmitFailed" -> "❌";
            default -> "🧵";
        };
    }
}