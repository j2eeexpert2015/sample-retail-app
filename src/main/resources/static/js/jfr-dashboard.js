// JFR Dashboard JavaScript - Backend State Management

let recordingHistory = [];

function addToLog(message, type = 'info') {
    const log = document.getElementById('status-log');
    const timestamp = new Date().toLocaleTimeString();
    const prefix = type === 'error' ? '❌' : type === 'success' ? '✅' : '📝';
    log.textContent += `[${timestamp}] ${prefix} ${message}\n`;
    log.scrollTop = log.scrollHeight;
}

function handleJFRAction(url, button) {
    addToLog(`Starting JFR action: ${url}`);

    const originalText = button.innerHTML;
    button.disabled = true;
    button.innerHTML = `Processing...<br><small>Please wait</small>`;

    fetch(url)
        .then(response => response.json())
        .then(data => {
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

            setTimeout(() => checkJFRStatus(), 500);
        })
        .catch(error => {
            addToLog(`JFR action failed: ${error.message}`, 'error');
            showNotification('Request failed: ' + error.message, 'error');
        })
        .finally(() => {
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

function analyzeLastRecording() {
    addToLog(`🔍 Requesting analysis of last recording from backend...`, 'info');

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

    const title = document.querySelector('.header h1');
    if (status.hasActiveRecording) {
        title.innerHTML = '🎬 JFR Recording Control <span style="color: #28a745;">● RECORDING</span>';
    } else {
        title.innerHTML = '🎬 JFR Recording Control';
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

    setTimeout(() => notification.classList.add('show'), 100);

    setTimeout(() => {
        notification.classList.remove('show');
        setTimeout(() => notification.remove(), 300);
    }, 4000);
}

document.addEventListener('DOMContentLoaded', function() {
    addToLog('🎬 JFR Dashboard loaded with backend state management', 'success');
    addToLog('💡 TIP: Backend now tracks last recording automatically across sessions', 'info');
    addToLog('🔄 Analysis operations use server-maintained recording references', 'info');
    checkJFRStatus();
});