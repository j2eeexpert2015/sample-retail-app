package com.example.retail.jfr;

import jdk.jfr.Recording;
import jdk.jfr.consumer.RecordingStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.Instant;
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
 *
 * This utility can be used in any Java application, not just Spring Boot.
 * It encapsulates all JFR-specific logic and provides a clean API for:
 * - Starting/stopping JFR recordings
 * - Configuring event handlers
 * - Managing recording streams
 * - Saving recordings to disk
 *
 * Key Features:
 * - Framework agnostic - no Spring dependencies
 * - Event-driven architecture with customizable handlers
 * - Proper resource management and cleanup
 * - Configurable thresholds and settings
 * - Thread-safe operations
 */
public class JFRVirtualThreadUtil {

    private static final Logger logger = LoggerFactory.getLogger(JFRVirtualThreadUtil.class);

    // Default configuration values
    private static final Duration DEFAULT_PINNING_THRESHOLD = Duration.ofMillis(20);
    private static final String DEFAULT_OUTPUT_DIR = "jfr-recordings";

    /*
     * Creates a new JFR recording configured for virtual thread monitoring.
     *
     * Called by: Any component that needs to start JFR recording
     * When: User wants to capture VT events to a file for analysis
     * Purpose: Sets up JFR recording with appropriate VT events enabled
     * Returns: Configured Recording object ready to be started
     */
    public static Recording createVirtualThreadRecording(String name) {
        Recording recording = new Recording();

        // Enable virtual thread lifecycle events
        recording.enable("jdk.VirtualThreadStart")
                .withStackTrace()
                .withThreshold(Duration.ZERO);

        recording.enable("jdk.VirtualThreadEnd")
                .withStackTrace()
                .withThreshold(Duration.ZERO);

        // Enable pinning events with low threshold for detection
        recording.enable("jdk.VirtualThreadPinned")
                .withStackTrace()
                .withThreshold(Duration.ofMillis(1));

        // Enable submit failed events (when VT scheduler is overwhelmed)
        recording.enable("jdk.VirtualThreadSubmitFailed")
                .withStackTrace();

        // Set recording metadata
        recording.setName(name + "-" + Instant.now().getEpochSecond());
        recording.setMaxAge(Duration.ofMinutes(30)); // Prevent excessive memory usage

        logger.debug("Created JFR recording: {}", recording.getName());
        return recording;
    }

    /*
     * Creates and starts a JFR RecordingStream for real-time event processing.
     *
     * Called by: Components that need live JFR event streaming
     * When: Application startup or when live monitoring is required
     * Purpose: Sets up continuous JFR event stream for real-time processing
     * Implementation: Runs in caller's thread context - caller responsible for threading
     */
    public static RecordingStream createEventStream(
            Consumer<jdk.jfr.consumer.RecordedEvent> onStart,
            Consumer<jdk.jfr.consumer.RecordedEvent> onEnd,
            Consumer<jdk.jfr.consumer.RecordedEvent> onPinned) {

        RecordingStream stream = new RecordingStream();

        // Configure events with appropriate thresholds
        stream.enable("jdk.VirtualThreadStart");
        stream.enable("jdk.VirtualThreadEnd");
        stream.enable("jdk.VirtualThreadPinned").withThreshold(DEFAULT_PINNING_THRESHOLD);

        // Register event handlers
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
     * Called by: When stopping a JFR recording
     * When: User wants to persist recording data for analysis
     * Purpose: Writes JFR binary data to disk for tools like JDK Mission Control
     * File format: Standard .jfr format readable by JFR analysis tools
     */
    public static Path saveRecording(Recording recording, String outputDir) throws Exception {
        if (recording == null) {
            throw new IllegalArgumentException("Recording cannot be null");
        }

        // Generate filename with recording name and timestamp
        String filename = recording.getName() + ".jfr";
        Path outputPath = Paths.get(outputDir != null ? outputDir : DEFAULT_OUTPUT_DIR, filename);

        // Ensure output directory exists
        outputPath.getParent().toFile().mkdirs();

        // Save recording to disk
        recording.dump(outputPath);

        logger.info("JFR recording saved: {}", outputPath.toAbsolutePath());
        return outputPath;
    }

    /*
     * Safely closes a RecordingStream with proper error handling.
     *
     * Called by: Components during shutdown or cleanup
     * When: Application shutdown or when stopping live monitoring
     * Purpose: Ensures proper resource cleanup and prevents memory leaks
     * Implementation: Handles exceptions gracefully to prevent shutdown issues
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
     * Safely stops and closes a JFR recording with proper error handling.
     *
     * Called by: Components when finishing recording
     * When: User stops recording or application shutdown
     * Purpose: Ensures recording is properly finalized and resources are released
     * Implementation: Handles recording lifecycle transitions safely
     */
    public static void stopRecording(Recording recording) {
        if (recording == null) {
            return;
        }

        try {
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
     * Validates if JFR is available and enabled in the current JVM.
     *
     * Called by: Components before attempting JFR operations
     * When: Application startup or before creating recordings/streams
     * Purpose: Prevents runtime errors when JFR is not available
     * Returns: true if JFR operations are supported, false otherwise
     */
    public static boolean isJFRAvailable() {
        try {
            // Try to create a simple recording to test JFR availability
            Recording testRecording = new Recording();
            testRecording.close();
            return true;
        } catch (Exception e) {
            logger.warn("JFR is not available: {}", e.getMessage());
            return false;
        }
    }

    /*
     * Gets recording information as a formatted string.
     *
     * Called by: Components that need to display recording status
     * When: Status checks or logging recording information
     * Purpose: Provides human-readable recording state information
     * Returns: Formatted string with recording details or "No active recording"
     */
    public static String getRecordingInfo(Recording recording) {
        if (recording == null) {
            return "No active recording";
        }

        return String.format("Recording: %s, State: %s, Duration: %s",
                recording.getName(),
                recording.getState(),
                recording.getDuration());
    }
}