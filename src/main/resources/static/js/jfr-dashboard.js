// JFR Dashboard JavaScript - Backend State Management
//
// This JavaScript manages the JFR dashboard UI and communicates with the backend
// for all recording state management. The backend now maintains the authoritative
// state for last recording tracking, eliminating client-side state dependencies.
//
// Key Features:
// - Frontend recording history for UI display
// - Backend-driven last recording analysis
// - Real-time status updates from server
// - Persistent state across page refreshes
// - Enhanced status display with backend metadata

// Frontend recording history for UI display purposes only
// Backend maintains the authoritative recording state
let recordingHistory = [];

/*
 * Adds timestamped log entries to the status display
 * Provides visual feedback for all JFR operations with appropriate icons
 */
function addToLog(message, type = 'info') {
    const log = document.getElementById('status-log');
    const timestamp = new Date().toLocaleTimeString();
    const prefix = type === 'error' ? '❌' : type === 'success' ? '✅' : '📝';
    log.textContent += `[${timestamp}] ${prefix} ${message}\n`;
    log.scrollTop = log.scrollHeight;
}

/*
 * Handles all JFR action requests (start, stop, custom recordings)
 * Manages button states and provides comprehensive error handling
 * Updates frontend history for UI purposes while backend tracks authoritative state
 */
function handleJFRAction(url, button) {
    addToLog(`Starting JFR action: ${url}`);

    // Provide visual feedback during request processing
    const originalText = button.innerHTML;
    button.disabled = true;
    button.innerHTML = `Processing...<br><small>Please wait</small>`;

    fetch(url)
        .then(response => response.json())
        .then(data => {
            if (data.status === 'success') {
                if (url.includes('/jfr/start')) {
                    // Track recording start in frontend history for UI display
                    const startInfo = {
                        action: 'started',
                        name: data.recordingName,
                        outputDir: data.outputDir,
                        timestamp: new Date().toLocaleString(),
                        status: 'active'
                    };
                    recordingHistory.push(startInfo);

                    addToLog(`✅ Recording started: ${data.recordingName}`, 'success');
                    addToLog(`📁 Output directory: ${data.outputDir}`, 'info');
                    showNotification(`Recording started: ${data.recordingName}`, 'success');
                    updateRecordingHistory();

                } else if (url.includes('/jfr/stop')) {
                    // Track recording completion in frontend history
                    // Backend now maintains authoritative last recording state
                    const stopInfo = {
                        action: 'stopped',
                        name: data.recordingName,
                        fileName: data.fileName,
                        filePath: data.filePath,
                        timestamp: new Date().toLocaleString(),
                        status: 'completed'
                    };
                    recordingHistory.push(stopInfo);

                    // Update frontend history status
                    const lastActive = recordingHistory.findLast(r => r.status === 'active');
                    if (lastActive) {
                        lastActive.status = 'completed';
                        lastActive.fileName = data.fileName;
                        lastActive.filePath = data.filePath;
                        lastActive.completedAt = new Date().toLocaleString();
                    }

                    addToLog(`⏹️ Recording stopped: ${data.recordingName}`, 'success');
                    addToLog(`💾 File saved: ${data.fileName}`, 'success');
                    addToLog(`📁 Full path: ${data.filePath}`, 'info');
                    addToLog(`🎯 Ready for analysis! Backend now tracking for "Analyze Last Recording"`, 'success');
                    showNotification(`Recording saved: ${data.fileName}`, 'success');
                    updateRecordingHistory();

                } else {
                    addToLog(`JFR action completed: ${data.message}`, 'success');
                    showNotification(data.message, 'success');
                }
            } else {
                addToLog(`❌ JFR error: ${data.message}`, 'error');
                showNotification(data.message, 'error');
            }

            // Refresh status to get updated backend state
            setTimeout(() => checkJFRStatus(), 500);
        })
        .catch(error => {
            addToLog(`JFR action failed: ${error.message}`, 'error');
            showNotification('Request failed: ' + error.message, 'error');
        })
        .finally(() => {
            // Restore button to normal state
            button.disabled = false;
            button.innerHTML = originalText;
        });
}

/*
 * Displays frontend recording history for user reference
 * Shows the last 5 recording operations for UI continuity
 */
function updateRecordingHistory() {
    const historySection = `
🗂️ RECORDING HISTORY (Last 5):
${recordingHistory.slice(-5).map(record => {
    if (record.action === 'started') {
        return `   📹 ${record.timestamp}: Started "${record.name}" → ${record.outputDir}/`;
    } else {
        return `   💾 ${record.timestamp}: Saved "${record.fileName}" → ${record.filePath}`;
    }
}).join('\n')}

`;
    addToLog(historySection);
}

/*
 * Analyzes the last recording using backend-managed state
 *
 * This function now delegates to the backend for last recording tracking
 * instead of maintaining client-side state. The backend determines which
 * recording to analyze and validates file existence.
 */
function analyzeLastRecording() {
    addToLog(`🔍 Requesting analysis of last recording from backend...`, 'info');

    // Use backend endpoint that manages last recording state
    fetch('/jfr/analyze-last')
        .then(response => response.json())
        .then(data => {
            if (data.status === 'success') {
                addToLog(`✅ Analysis completed for: ${data.analyzedFile}`, 'success');
                addToLog(`📁 Analyzed file: ${data.analyzedPath}`, 'info');
                addToLog(`⏰ Recorded at: ${data.recordedAt}`, 'info');
                addToLog('📊 Check server logs for detailed analysis results', 'success');
                showNotification(`Analysis completed: ${data.analyzedFile}`, 'success');
            } else {
                addToLog(`❌ Analysis failed: ${data.message}`, 'error');
                showNotification('Analysis failed: ' + data.message, 'error');
            }
        })
        .catch(error => {
            addToLog(`❌ Analysis request failed: ${error.message}`, 'error');
            showNotification('Analysis request failed', 'error');
        });
}

/*
 * Retrieves and displays current JFR system status
 * Now includes backend-tracked last recording information
 */
function checkJFRStatus() {
    addToLog('Checking JFR status...');

    fetch('/jfr/status')
        .then(response => response.json())
        .then(data => {
            updateStatusDisplay(data);
            addToLog('JFR status updated successfully', 'success');
        })
        .catch(error => {
            addToLog(`Failed to check JFR status: ${error.message}`, 'error');
        });
}

/*
 * Updates the status display with comprehensive system information
 *
 * Displays both frontend history (for UI continuity) and backend state
 * (authoritative recording information). Backend state takes precedence
 * for analysis operations.
 */
function updateStatusDisplay(status) {
    const currentActiveRecording = recordingHistory.findLast(r => r.status === 'active');
    const lastCompletedRecording = recordingHistory.findLast(r => r.status === 'completed');

    // Build backend last recording information display
    let backendLastRecording = '';
    if (status.lastRecording) {
        backendLastRecording = `
🎯 LAST RECORDING (Backend Tracked - Used for Analysis):
   File: ${status.lastRecording.fileName}
   Timestamp: ${status.lastRecording.timestamp}
   File Exists: ${status.lastRecording.exists ? '✅ Yes' : '❌ No'}
   Ready for Analysis: ${status.lastRecording.exists ? '✅ Yes' : '❌ File Missing'}
`;
    }

    // Comprehensive status information display
    const statusInfo = `
📊 JFR SYSTEM STATUS:
   Available: ${status.jfrAvailable ? '✅ Yes' : '❌ No'}
   Active Recording: ${status.hasActiveRecording ? '✅ Yes' : '❌ No'}
   Output Directory: ${status.outputDirectory}
   Can Create Recordings: ${status.configuration?.canCreateRecordings ? '✅ Yes' : '❌ No'}

${status.hasActiveRecording ? `
🎬 CURRENT RECORDING:
   Name: ${status.recordingName}
   State: ${status.recordingState}
   Duration: ${status.recordingDuration}
   Started: ${currentActiveRecording ? currentActiveRecording.timestamp : 'Unknown'}

` : `📁 No active recording

${lastCompletedRecording ? `🎯 LAST SAVED RECORDING (Frontend History):
   File: ${lastCompletedRecording.fileName}
   Location: ${lastCompletedRecording.filePath}
   Completed: ${lastCompletedRecording.completedAt || lastCompletedRecording.timestamp}

` : ''}`}${backendLastRecording}`;

    addToLog(statusInfo);

    // Update header with visual recording status indicator
    const title = document.querySelector('.header h1');
    if (status.hasActiveRecording) {
        title.innerHTML = '🎬 JFR Recording Control <span style="color: #28a745;">● RECORDING</span>';
    } else {
        title.innerHTML = '🎬 JFR Recording Control <span style="color: #6c757d;">○ IDLE</span>';
    }
}

/*
 * Refreshes all status information and recording history
 * Provides a clean slate for status display while preserving session history
 */
function refreshStatus() {
    document.getElementById('status-log').textContent = 'Refreshing status...\n';
    if (recordingHistory.length > 0) {
        updateRecordingHistory();
    }
    checkJFRStatus();
}

/*
 * Displays temporary notification messages for user feedback
 * Provides non-intrusive success and error notifications
 */
function showNotification(message, type) {
    const notification = document.createElement('div');
    notification.textContent = message;
    notification.className = `notification ${type === 'success' ? 'success' : 'error'}`;

    document.body.appendChild(notification);

    // Animate notification appearance
    setTimeout(() => notification.classList.add('show'), 100);

    // Auto-remove notification after display period
    setTimeout(() => {
        notification.classList.remove('show');
        setTimeout(() => notification.remove(), 300);
    }, 4000);
}

/*
 * Initialize dashboard when page loads
 * Sets up initial state and establishes connection to backend
 */
document.addEventListener('DOMContentLoaded', function() {
    addToLog('🎬 JFR Dashboard loaded with backend state management', 'success');
    addToLog('💡 TIP: Backend now tracks last recording automatically across sessions', 'info');
    addToLog('🔄 Analysis operations use server-maintained recording references', 'info');
    checkJFRStatus();
});