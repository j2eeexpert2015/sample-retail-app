package com.example.retail.controller;

import com.example.retail.jfr.SpringJFRUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

/**
 * REST controller for managing JFR recordings in Spring Boot applications.
 * Provides endpoints to start, stop, and query JFR recording status.
 */
@RestController
@RequestMapping("/jfr")
public class JFRController {

    @Autowired
    private SpringJFRUtil jfrUtil;

    /**
     * Start a new JFR recording
     */
    @PostMapping("/start")
    public ResponseEntity<Map<String, String>> startRecording(
            @RequestParam(defaultValue = "ManualRecording") String name) {

        Map<String, String> response = new HashMap<>();

        if (jfrUtil.startRecording(name) != null) {
            response.put("status", "success");
            response.put("message", "JFR recording started: " + name);
            response.put("recordingInfo", jfrUtil.getRecordingInfo());
            return ResponseEntity.ok(response);
        } else {
            response.put("status", "error");
            response.put("message", "Failed to start JFR recording");
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * Stop the current JFR recording
     */
    @PostMapping("/stop")
    public ResponseEntity<Map<String, String>> stopRecording() {
        Map<String, String> response = new HashMap<>();

        Path jfrFile = jfrUtil.stopRecording();

        if (jfrFile != null) {
            response.put("status", "success");
            response.put("message", "JFR recording stopped");
            response.put("filePath", jfrFile.toAbsolutePath().toString());
            return ResponseEntity.ok(response);
        } else {
            response.put("status", "error");
            response.put("message", "No active recording to stop");
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * Get current JFR recording status
     */
    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getStatus() {
        Map<String, Object> status = new HashMap<>();

        status.put("enabled", jfrUtil.isJfrEnabled());
        status.put("recording", jfrUtil.isRecording());
        status.put("recordingInfo", jfrUtil.getRecordingInfo());
        status.put("outputDir", jfrUtil.getOutputDir());
        status.put("autoStart", jfrUtil.isAutoStart());
        status.put("autoAnalyze", jfrUtil.isAutoAnalyze());

        return ResponseEntity.ok(status);
    }

    /**
     * Analyze an existing JFR file
     */
    @PostMapping("/analyze")
    public ResponseEntity<Map<String, String>> analyzeRecording(
            @RequestParam String filePath) {

        Map<String, String> response = new HashMap<>();

        try {
            Path jfrFile = Path.of(filePath);
            jfrUtil.analyzeRecording(jfrFile);

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
}