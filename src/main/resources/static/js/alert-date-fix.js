/**
 * Fix for date formatting in alerts edit view
 * This script overrides the formatDate function to handle numeric timestamps correctly
 */
document.addEventListener('DOMContentLoaded', function() {
    // Wait for the original formatDate function to be defined
    setTimeout(function() {
        // Store the original function reference
        const originalFormatDate = window.formatDate;
        
        // Override the formatDate function with our fixed version
        window.formatDate = function(dateString) {
            if (!dateString) return '';
            
            // Convert seconds to milliseconds by multiplying by 1000 if it's a number
            const timestamp = typeof dateString === 'number' ? dateString * 1000 : dateString;
            const date = new Date(timestamp);
            return date.toLocaleString();
        };
        
        console.log("📅 Date formatting function has been fixed to handle numeric timestamps");
        
        // If we're on the edit page and the timestamps container is visible, refresh the displayed dates
        const timestampsContainer = document.getElementById('timestampsContainer');
        if (timestampsContainer && timestampsContainer.style.display !== 'none') {
            // Get the alert data from the page
            const alertId = parseInt(document.getElementById('alertId').value) || 0;
            if (alertId > 0) {
                console.log("📅 Refreshing displayed dates for alert ID:", alertId);
                // Reload the alert data to refresh the displayed dates
                loadAlertData(alertId);
            }
        }
    }, 500); // Wait 500ms to ensure the original function is defined
});