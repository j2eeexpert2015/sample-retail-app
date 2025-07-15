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
 * All endpoints use GET for easy browser access.
 */
@RestController
@RequestMapping("/jfr")
public class JFRController {

    @Autowired
    private SpringJFRUtil jfrUtil;

    /**
     * Start a new JFR recording
     * GET /jfr/start?name=MyRecording
     */
    @GetMapping("/start")
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
     * GET /jfr/stop
     */
    @GetMapping("/stop")
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
     * GET /jfr/status
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
     * GET /jfr/analyze?filePath=/path/to/file.jfr
     */
    @GetMapping("/analyze")
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

    /**
     * Simple HTML dashboard for easy browser usage
     * GET /jfr
     */
    @GetMapping("")
    public ResponseEntity<String> dashboard() {
        String html = """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <title>JFR Controller Dashboard</title>
                <style>
                    body { 
                        font-family: Arial, sans-serif; 
                        margin: 40px; 
                        background-color: #f5f5f5;
                    }
                    .container { max-width: 800px; margin: 0 auto; }
                    h1 { 
                        color: #333; 
                        text-align: center; 
                        background: white; 
                        padding: 20px; 
                        border-radius: 10px; 
                        box-shadow: 0 2px 5px rgba(0,0,0,0.1);
                    }
                    .endpoint { 
                        margin: 20px 0; 
                        padding: 20px; 
                        border: 1px solid #ddd; 
                        border-radius: 8px; 
                        background: white;
                        box-shadow: 0 2px 4px rgba(0,0,0,0.1);
                    }
                    .endpoint h3 { 
                        margin-top: 0; 
                        color: #333; 
                        border-bottom: 2px solid #eee;
                        padding-bottom: 10px;
                    }
                    a { 
                        color: #007bff; 
                        text-decoration: none; 
                        font-weight: bold;
                        padding: 5px 10px;
                        background: #f8f9fa;
                        border-radius: 4px;
                        display: inline-block;
                        margin: 2px;
                    }
                    a:hover { 
                        text-decoration: underline; 
                        background: #e9ecef;
                    }
                    .status { border-left: 4px solid #17a2b8; }
                    .action { border-left: 4px solid #28a745; }
                    .workflow { border-left: 4px solid #ffc107; }
                    .analyze { border-left: 4px solid #6f42c1; }
                    ol { line-height: 1.8; }
                    .url-example { 
                        background: #f8f9fa; 
                        padding: 8px; 
                        border-radius: 4px; 
                        font-family: monospace;
                        margin: 5px 0;
                    }
                </style>
            </head>
            <body>
                <div class="container">
                    <h1>🎬 JFR Recording Controller</h1>
                    
                    <div class="endpoint status">
                        <h3>📊 Check Status</h3>
                        <p><a href="/jfr/status" target="_blank">GET /jfr/status</a></p>
                        <p>View current JFR recording status and configuration</p>
                    </div>
                    
                    <div class="endpoint action">
                        <h3>🚀 Start Recording</h3>
                        <p>
                            <a href="/jfr/start" target="_blank">Start with default name</a>
                            <a href="/jfr/start?name=MyTest" target="_blank">Start with custom name</a>
                        </p>
                        <div class="url-example">GET /jfr/start?name=YourRecordingName</div>
                        <p>Start a new JFR recording to monitor virtual threads</p>
                    </div>
                    
                    <div class="endpoint action">
                        <h3>⏹️ Stop Recording</h3>
                        <p><a href="/jfr/stop" target="_blank">Stop Current Recording</a></p>
                        <div class="url-example">GET /jfr/stop</div>
                        <p>Stop current recording and save to file with analysis</p>
                    </div>
                    
                    <div class="endpoint analyze">
                        <h3>📈 Analyze Recording</h3>
                        <div class="url-example">GET /jfr/analyze?filePath=path/to/your/file.jfr</div>
                        <p>Analyze an existing JFR file (check application logs for results)</p>
                    </div>
                    
                    <div class="endpoint workflow">
                        <h3>🎯 Typical Workflow</h3>
                        <ol>
                            <li><a href="/jfr/status" target="_blank">Check Status</a> - See if recording is active</li>
                            <li><a href="/jfr/start?name=Demo" target="_blank">Start Recording</a> - Begin monitoring</li>
                            <li><strong>Run your virtual thread workload</strong> - Execute your demo/application</li>
                            <li><a href="/jfr/stop" target="_blank">Stop Recording</a> - Save and analyze results</li>
                            <li><strong>Check application logs</strong> - View detailed analysis</li>
                        </ol>
                    </div>
                    
                    <div class="endpoint">
                        <h3>💡 Tips</h3>
                        <ul>
                            <li>Always check status before starting a new recording</li>
                            <li>Use descriptive names for your recordings</li>
                            <li>Recording files are saved in the configured output directory</li>
                            <li>Analysis results appear in your application logs</li>
                        </ul>
                    </div>
                </div>
            </body>
            </html>
            """;

        return ResponseEntity.ok()
                .header("Content-Type", "text/html; charset=UTF-8")
                .body(html);
    }
}