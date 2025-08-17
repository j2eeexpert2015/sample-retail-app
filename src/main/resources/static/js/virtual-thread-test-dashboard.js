// Virtual Thread Test Runner - JavaScript

function addToLog(message, type = 'info') {
    const log = document.getElementById('results-log');
    const timestamp = new Date().toLocaleTimeString();
    const prefix = type === 'error' ? '❌' : type === 'success' ? '✅' : '📝';
    log.textContent += `[${timestamp}] ${prefix} ${message}\n`;
    log.scrollTop = log.scrollHeight;
}

function runTest(endpoint, params) {
    const url = `/vt-demo/${endpoint}${params ? '?' + params : ''}`;
    const testName = endpoint.charAt(0).toUpperCase() + endpoint.slice(1);

    addToLog(`Starting ${testName} test...`);

    // Disable button temporarily
    const button = event.target;
    const originalText = button.innerHTML;
    button.disabled = true;
    button.innerHTML = `Running...<br><small>Please wait</small>`;

    fetch(url)
        .then(response => response.text())
        .then(data => {
            addToLog(`${testName} completed: ${data.substring(0, 80)}...`, 'success');
        })
        .catch(error => {
            addToLog(`${testName} failed: ${error.message}`, 'error');
        })
        .finally(() => {
            // Re-enable button
            button.disabled = false;
            button.innerHTML = originalText;
        });
}

function clearResults() {
    document.getElementById('results-log').textContent = 'Results cleared. Ready for new tests...\n';
}

// Check if server is running when page loads
document.addEventListener('DOMContentLoaded', function() {
    fetch('/vt-demo/burst?count=1')
        .then(() => addToLog('Connected to server successfully', 'success'))
        .catch(error => addToLog(`Server connection failed: ${error.message}`, 'error'));
});