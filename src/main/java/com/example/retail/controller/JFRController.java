package com.example.retail.controller;

import com.example.retail.jfr.JFRVirtualThreadUtil;
import jakarta.servlet.http.HttpServletResponse;
import jdk.jfr.Recording;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

/*
 * JFR Controller - REST API for Manual JFR Recording Control
 *
 * Provides HTTP endpoints for manual control of JFR recordings independent of automatic monitoring.
 * Uses JFRVirtualThreadUtil for core JFR operations and focuses on:
 * - Manual recording start/stop control via REST API
 * - Recording status and information retrieval
 * - File-based recording management for detailed analysis
 * - Simple HTML dashboard for browser-based control
 *
 * This controller is separate from automatic monitoring and allows users to:
 * - Start on-demand JFR recordings for specific analysis periods
 * - Stop recordings and save them to disk for analysis tools
 * - Check current recording status and configuration
 * - Access a simple web interface for recording control
 *
 * Key Features:
 * - Independent of automatic monitoring (can work without JFRVirtualThreadListener)
 * - Manual control via REST API for integration with other tools
 * - File-based recordings suitable for JDK Mission Control analysis
 * - Configuration via Spring properties for output directory
 * - Simple HTML dashboard for manual testing
 *
 * REST Endpoints:
 * - GET /jfr - Simple HTML dashboard
 * - GET /jfr/start - Start JFR recording with optional name
 * - GET /jfr/stop - Stop current recording and save to file
 * - GET /jfr/status - Get current recording status and information
 *
 * Use Cases:
 * - Capturing specific test scenarios for detailed analysis
 * - Manual recordings during performance testing
 * - On-demand troubleshooting of virtual thread issues
 * - Integration with automated testing tools
 */
@RestController
@RequestMapping("/jfr")
public class JFRController {

    private static final Logger logger = LoggerFactory.getLogger(JFRController.class);

    @Value("${jfr.output.dir:jfr-recordings}")
    private String outputDir;

    // Current manual recording state (separate from automatic monitoring)
    private Recording currentRecording;

    /*
     * Provides simple HTML dashboard for manual JFR recording control.
     *
     * Called by: HTTP GET request to /jfr
     * When: User visits http://localhost:8080/jfr in browser
     * Purpose: Offers basic web interface for manual recording operations
     * UI Elements: Start/stop/status links and connection to Grafana
     * Target Users: Developers, testers, operations staff
     */

    @GetMapping("")
    public void dashboard(HttpServletResponse response) throws IOException {
        response.sendRedirect("/jfr-dashboard.html");
    }
    /*
     * Starts a new JFR recording for manual analysis and saves to file when stopped.
     *
     * Called by: HTTP GET request to /jfr/start
     * When: User wants to start capturing VT events for detailed analysis
     * Purpose: Creates file-based JFR recording for tools like JDK Mission Control
     * Concurrency: Only one manual recording can be active at a time
     * Configuration: Uses configured output directory for file storage
     * Duration: Recording continues until manually stopped or application shutdown
     */
    @GetMapping("/start")
    public Map<String, String> startRecording(@RequestParam(defaultValue = "Manual-Recording") String name) {
        Map<String, String> response = new HashMap<>();

        if (!JFRVirtualThreadUtil.isJFRAvailable()) {
            response.put("status", "error");
            response.put("message", "JFR is not available in this JVM");
            return response;
        }

        if (currentRecording != null) {
            response.put("status", "error");
            response.put("message", "Recording already active: " + currentRecording.getName());
            response.put("currentRecording", JFRVirtualThreadUtil.getRecordingInfo(currentRecording));
            return response;
        }

        try {
            // Create and start new recording using utility
            currentRecording = JFRVirtualThreadUtil.createVirtualThreadRecording(name);
            currentRecording.start();

            response.put("status", "success");
            response.put("message", "JFR recording started successfully");
            response.put("recordingName", currentRecording.getName());
            response.put("outputDir", outputDir);

            logger.info("📹 Manual JFR recording started: {}", currentRecording.getName());

        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", "Failed to start recording: " + e.getMessage());
            logger.error("Failed to start JFR recording", e);
        }

        return response;
    }

    /*
     * Stops the current JFR recording and saves it to disk for analysis.
     *
     * Called by: HTTP GET request to /jfr/stop
     * When: User wants to finalize recording and save data
     * Purpose: Stops recording, saves .jfr file, and cleans up resources
     * Output: Creates timestamped .jfr file in configured output directory
     * Analysis: Generated files can be opened in JDK Mission Control
     * Cleanup: Releases recording resources and resets state
     */
    @GetMapping("/stop")
    public Map<String, String> stopRecording() {
        Map<String, String> response = new HashMap<>();

        if (currentRecording == null) {
            response.put("status", "error");
            response.put("message", "No active recording to stop");
            return response;
        }

        try {
            // Stop recording and save to file
            String recordingName = currentRecording.getName();
            currentRecording.stop();

            Path savedFile = JFRVirtualThreadUtil.saveRecording(currentRecording, outputDir);

            // Clean up recording resources
            JFRVirtualThreadUtil.stopRecording(currentRecording);
            currentRecording = null;

            response.put("status", "success");
            response.put("message", "Recording stopped and saved successfully");
            response.put("recordingName", recordingName);
            response.put("filePath", savedFile.toAbsolutePath().toString());
            response.put("fileName", savedFile.getFileName().toString());

            logger.info("💾 Manual JFR recording saved: {}", savedFile.toAbsolutePath());

        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", "Failed to stop recording: " + e.getMessage());
            logger.error("Failed to stop JFR recording", e);
        }

        return response;
    }

    /*
     * Returns current JFR recording status and system information.
     *
     * Called by: HTTP GET request to /jfr/status
     * When: User wants to check recording state or system capabilities
     * Purpose: Provides status information for monitoring and troubleshooting
     * Information: Recording state, JFR availability, configuration details
     * Format: JSON response suitable for both human reading and API consumption
     */
    @GetMapping("/status")
    public Map<String, Object> getStatus() {
        Map<String, Object> status = new HashMap<>();

        // System capabilities
        status.put("jfrAvailable", JFRVirtualThreadUtil.isJFRAvailable());
        status.put("outputDirectory", outputDir);

        // Recording state
        status.put("hasActiveRecording", currentRecording != null);

        if (currentRecording != null) {
            status.put("recordingInfo", JFRVirtualThreadUtil.getRecordingInfo(currentRecording));
            status.put("recordingName", currentRecording.getName());
            status.put("recordingState", currentRecording.getState().toString());
            status.put("recordingDuration", currentRecording.getDuration().toString());
        } else {
            status.put("recordingInfo", "No active recording");
        }

        // Configuration
        status.put("configuration", Map.of(
                "outputDir", outputDir,
                "canCreateRecordings", JFRVirtualThreadUtil.isJFRAvailable()
        ));

        logger.debug("JFR status requested - active recording: {}", currentRecording != null);
        return status;
    }

    /*
     * Emergency stop for cleaning up recording during application shutdown.
     * This method is NOT exposed as REST endpoint - internal cleanup only.
     *
     * Called by: Application shutdown hooks or error recovery
     * When: Application termination with active recording
     * Purpose: Prevents data loss by saving recording before shutdown
     */
    public void emergencyStop() {
        if (currentRecording != null) {
            try {
                logger.warn("Emergency stop - saving active JFR recording");
                JFRVirtualThreadUtil.saveRecording(currentRecording, outputDir);
                JFRVirtualThreadUtil.stopRecording(currentRecording);
                currentRecording = null;
            } catch (Exception e) {
                logger.error("Failed to save recording during emergency stop", e);
            }
        }
    }
}