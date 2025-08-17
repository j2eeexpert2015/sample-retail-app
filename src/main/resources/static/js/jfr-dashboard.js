// JFR Dashboard JavaScript - Matching VT Test Runner Style

// Persistent storage for recording history
let recordingHistory = [];
let lastRecordingPath = null;

function addToLog(message, type = 'info') {
    const log = document.getElementById('status-log');
    const timestamp = new Date().toLocaleTimeString();
    const prefix = type === 'error' ? '❌' : type === 'success' ? '✅' : '📝';
    log.textContent += `[${timestamp}] ${prefix} ${message}\n`;
    log.scrollTop = log.scrollHeight;
}

function handleJFRAction(url, button) {
    addToLog(`Starting JFR action: ${url}`);

    // Disable button temporarily
    const originalText = button.innerHTML;
    button.disabled = true;
    button.innerHTML = `Processing...<br><small>Please wait</small>`;

    fetch(url)
        .then(response => response.json())
        .then(data => {
            // Enhanced logging for different JFR actions
            if (data.status === 'success') {
                if (url.includes('/jfr/start')) {
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
                    const stopInfo = {
                        action: 'stopped',
                        name: data.recordingName,
                        fileName: data.fileName,
                        filePath: data.filePath,
                        timestamp: new Date().toLocaleString(),
                        status: 'completed'
                    };
                    recordingHistory.push(stopInfo);

                    // Update the last active recording status
                    const lastActive = recordingHistory.findLast(r => r.status === 'active');
                    if (lastActive) {
                        lastActive.status = 'completed';
                        lastActive.fileName = data.fileName;
                        lastActive.filePath = data.filePath;
                        lastActive.completedAt = new Date().toLocaleString();
                    }

                    addToLog(`⏹️ Recording stopped: ${data.recordingName}`, 'success');
                    addToLog(`💾 File saved: ${data.fileName}`, 'success');
                    lastRecordingPath = data.filePath;
                    addToLog(`📁 Full path: ${data.filePath}`, 'info');
                    addToLog(`🎯 REMEMBER: Your recording is saved as "${data.fileName}"`, 'success');
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

            // Refresh status after action
            setTimeout(() => {
                checkJFRStatus();
            }, 500);
        })
        .catch(error => {
            addToLog(`JFR action failed: ${error.message}`, 'error');
            showNotification('Request failed: ' + error.message, 'error');
        })
        .finally(() => {
            // Re-enable button
            button.disabled = false;
            button.innerHTML = originalText;
        });
}

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

function updateStatusDisplay(status) {
    const currentActiveRecording = recordingHistory.findLast(r => r.status === 'active');
    const lastCompletedRecording = recordingHistory.findLast(r => r.status === 'completed');

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

${lastCompletedRecording ? `🎯 LAST SAVED RECORDING:
   File: ${lastCompletedRecording.fileName}
   Location: ${lastCompletedRecording.filePath}
   Completed: ${lastCompletedRecording.completedAt || lastCompletedRecording.timestamp}

` : ''}`}`;

    addToLog(statusInfo);

    // Update header with status indicator
    const title = document.querySelector('.header h1');
    if (status.hasActiveRecording) {
        title.innerHTML = '🎬 JFR Recording Control <span style="color: #28a745;">● RECORDING</span>';
    } else {
        title.innerHTML = '🎬 JFR Recording Control <span style="color: #6c757d;">○ IDLE</span>';
    }
}

function refreshStatus() {
    document.getElementById('status-log').textContent = 'Refreshing status...\n';
    if (recordingHistory.length > 0) {
        updateRecordingHistory();
    }
    checkJFRStatus();
}

function showNotification(message, type) {
    const notification = document.createElement('div');
    notification.textContent = message;
    notification.className = `notification ${type === 'success' ? 'success' : 'error'}`;

    document.body.appendChild(notification);

    // Show notification
    setTimeout(() => {
        notification.classList.add('show');
    }, 100);

    // Remove after 4 seconds (longer for file path messages)
    setTimeout(() => {
        notification.classList.remove('show');
        setTimeout(() => {
            notification.remove();
        }, 300);
    }, 4000);
}

function analyzeLastRecording() {
    if (!lastRecordingPath) {
        addToLog('❌ No recent recording found. Please stop a recording first.', 'error');
        showNotification('No recording to analyze', 'error');
        return;
    }

    addToLog(`🔍 Starting analysis of: ${lastRecordingPath}`, 'info');

    fetch(`/jfr/analyze?filePath=${encodeURIComponent(lastRecordingPath)}`)
        .then(response => response.json())
        .then(data => {
            if (data.status === 'success') {
                addToLog('✅ Analysis completed! Check server logs for detailed results', 'success');
                showNotification('Analysis completed - check server logs', 'success');
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

// Initialize when page loads
document.addEventListener('DOMContentLoaded', function() {
    addToLog('🎬 JFR Dashboard loaded. Checking initial status...', 'success');
    addToLog('💡 TIP: Recording names and file locations are preserved in this session', 'info');
    checkJFRStatus();
});