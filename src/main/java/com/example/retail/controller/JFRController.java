package com.example.retail.controller;

import com.example.retail.jfr.JFRVirtualThreadUtil;
import jakarta.servlet.http.HttpServletResponse;
import jdk.jfr.Recording;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

/*
 * JFR Controller - REST API for Manual JFR Recording Control with Backend State Management
 *
 * This controller provides HTTP endpoints for manual control of JFR recordings and maintains
 * server-side state for last recording tracking. Key features:
 *
 * - Manual recording start/stop control via REST API
 * - Backend tracking of last completed recording for analysis
 * - Recording status and information retrieval
 * - File-based recording management for detailed analysis
 * - Simple HTML dashboard for browser-based control
 * - Automatic state persistence across requests
 *
 * Backend State Management:
 * - Tracks current active recording
 * - Maintains reference to last completed recording
 * - Validates file existence before analysis
 * - Provides centralized recording metadata
 *
 * REST Endpoints:
 * - GET /jfr - Simple HTML dashboard redirect
 * - GET /jfr/start - Start JFR recording with optional name
 * - GET /jfr/stop - Stop current recording and save to file
 * - GET /jfr/analyze-last - Analyze last completed recording (backend managed)
 * - GET /jfr/analyze - Analyze custom JFR file by path
 * - GET /jfr/status - Get current recording status and last recording info
 */
@RestController
@RequestMapping("/jfr")
public class JFRController {

    private static final Logger logger = LoggerFactory.getLogger(JFRController.class);

    @Value("${jfr.output.dir:jfr-recordings}")
    private String outputDir;

    // Current active recording state
    private Recording currentRecording;

    // Backend tracking of last completed recording for analysis
    private String lastRecordingPath;
    private String lastRecordingName;
    private Instant lastRecordingTimestamp;

    /*
     * Dashboard redirect endpoint - provides simple HTML interface
     */
    @GetMapping("")
    public void dashboard(HttpServletResponse response) throws IOException {
        response.sendRedirect("/jfr-dashboard.html");
    }

    /*
     * Starts a new JFR recording for manual analysis
     *
     * Creates a new JFR recording configured for virtual thread monitoring
     * and starts capturing events. Only one recording can be active at a time.
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
     * Stops the current JFR recording and saves it to disk
     *
     * Finalizes the recording, saves to configured output directory,
     * and updates backend state to track this as the last recording
     * available for analysis.
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
            String recordingName = currentRecording.getName();
            currentRecording.stop();

            Path savedFile = JFRVirtualThreadUtil.saveRecording(currentRecording, outputDir);

            // Update backend state to track this recording for analysis
            lastRecordingPath = savedFile.toAbsolutePath().toString();
            lastRecordingName = savedFile.getFileName().toString();
            lastRecordingTimestamp = Instant.now();

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
     * Analyzes the last completed recording using backend-tracked state
     *
     * This endpoint uses the server-maintained reference to the most recent
     * recording, eliminating the need for frontend state management.
     * Validates file existence before attempting analysis.
     */
    @GetMapping("/analyze-last")
    public ResponseEntity<Map<String, String>> analyzeLastRecording() {
        Map<String, String> response = new HashMap<>();

        if (lastRecordingPath == null) {
            response.put("status", "error");
            response.put("message", "No recent recording found. Please stop a recording first.");
            return ResponseEntity.badRequest().body(response);
        }

        try {
            Path jfrFile = Path.of(lastRecordingPath);

            // Validate file existence before analysis
            if (!Files.exists(jfrFile)) {
                response.put("status", "error");
                response.put("message", "Recording file not found: " + lastRecordingName);
                return ResponseEntity.badRequest().body(response);
            }

            // Perform analysis using backend-tracked file
            JFRVirtualThreadUtil.analyzeRecording(jfrFile);

            response.put("status", "success");
            response.put("message", "Analysis completed successfully");
            response.put("analyzedFile", lastRecordingName);
            response.put("analyzedPath", lastRecordingPath);
            response.put("recordedAt", lastRecordingTimestamp.toString());
            response.put("note", "Check application logs for detailed analysis results");

            logger.info("📊 Analyzed last recording: {}", lastRecordingName);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", "Failed to analyze recording: " + e.getMessage());
            logger.error("Failed to analyze last recording", e);
            return ResponseEntity.badRequest().body(response);
        }
    }

    /*
     * Analyzes a custom JFR file by explicit file path
     *
     * This endpoint allows analysis of any JFR file by providing
     * the full file path. Useful for analyzing older recordings
     * or files from external sources.
     */
    @GetMapping("/analyze")
    public ResponseEntity<Map<String, String>> analyzeRecording(@RequestParam String filePath) {
        Map<String, String> response = new HashMap<>();

        try {
            Path jfrFile = Path.of(filePath);
            JFRVirtualThreadUtil.analyzeRecording(jfrFile);

            response.put("status", "success");
            response.put("message", "JFR file analyzed successfully");
            response.put("note", "Check application logs for analysis results");
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", "Failed to analyze JFR file: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /*
     * Returns comprehensive JFR system status including last recording info
     *
     * Provides complete state information including:
     * - JFR system capabilities and availability
     * - Current active recording details
     * - Last completed recording metadata with file validation
     * - System configuration details
     */
    @GetMapping("/status")
    public Map<String, Object> getStatus() {
        Map<String, Object> status = new HashMap<>();

        // System capabilities and configuration
        status.put("jfrAvailable", JFRVirtualThreadUtil.isJFRAvailable());
        status.put("outputDirectory", outputDir);
        status.put("hasActiveRecording", currentRecording != null);

        // Current active recording information
        if (currentRecording != null) {
            status.put("recordingInfo", JFRVirtualThreadUtil.getRecordingInfo(currentRecording));
            status.put("recordingName", currentRecording.getName());
            status.put("recordingState", currentRecording.getState().toString());
            status.put("recordingDuration", currentRecording.getDuration().toString());
        } else {
            status.put("recordingInfo", "No active recording");
        }

        // Backend-tracked last recording information
        if (lastRecordingPath != null) {
            status.put("lastRecording", Map.of(
                    "fileName", lastRecordingName,
                    "filePath", lastRecordingPath,
                    "timestamp", lastRecordingTimestamp.toString(),
                    "exists", Files.exists(Path.of(lastRecordingPath))
            ));
        } else {
            status.put("lastRecording", null);
        }

        // System configuration
        status.put("configuration", Map.of(
                "outputDir", outputDir,
                "canCreateRecordings", JFRVirtualThreadUtil.isJFRAvailable()
        ));

        logger.debug("JFR status requested - active: {}, last recording: {}",
                currentRecording != null, lastRecordingName);
        return status;
    }

    /*
     * Emergency cleanup for application shutdown
     *
     * Ensures active recordings are properly saved even during
     * unexpected application termination. Updates backend state
     * to track emergency-saved recordings for later analysis.
     */
    public void emergencyStop() {
        if (currentRecording != null) {
            try {
                logger.warn("Emergency stop - saving active JFR recording");
                Path savedFile = JFRVirtualThreadUtil.saveRecording(currentRecording, outputDir);

                // Update backend state even during emergency shutdown
                lastRecordingPath = savedFile.toAbsolutePath().toString();
                lastRecordingName = savedFile.getFileName().toString();
                lastRecordingTimestamp = Instant.now();

                JFRVirtualThreadUtil.stopRecording(currentRecording);
                currentRecording = null;

                logger.info("Emergency recording saved: {}", lastRecordingName);
            } catch (Exception e) {
                logger.error("Failed to save recording during emergency stop", e);
            }
        }
    }
}