/**
 * Website Alerts functionality
 * - Fetches active alerts for the current company
 * - Displays them in the topbar
 * - Updates the alert count badge
 */
document.addEventListener('DOMContentLoaded', function() {
    console.log('🔔 Initializing website alerts');
    initWebsiteAlerts();
});

/**
 * Initialize website alerts functionality
 */
function initWebsiteAlerts() {
    // Get the company ID from the body data attribute
    const companyId = document.body.getAttribute('data-company-id');
    
    if (!companyId) {
        console.warn('⚠️ No company ID found for alerts');
        return;
    }
    
    console.log('🏢 Loading alerts for company:', companyId);
    
    // Fetch active alerts for this company
    fetchActiveAlerts(companyId);
    
    // Set up tooltip for the alerts icon
    const alertsDropdown = document.getElementById('alertsDropdown');
    if (alertsDropdown && typeof $ !== 'undefined') {
        $(alertsDropdown).tooltip();
    }
}

/**
 * Fetch active alerts for a company
 * @param {string} companyId - The company ID
 */
function fetchActiveAlerts(companyId) {
    fetch('/api/website-alerts/list-active', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify({ companyId: companyId })
    })
    .then(response => {
        if (!response.ok) {
            throw new Error(`HTTP error! Status: ${response.status}`);
        }
        return response.json();
    })
    .then(result => {
        console.log('🔔 Alerts response:', result);
        
        if (result.error) {
            console.error('❌ Error fetching alerts:', result.error);
            return;
        }
        
        const alerts = result.data || [];
        
        // Display the alerts
        displayAlerts(alerts);
        
        // Update the tooltip to show counts
        updateAlertTooltip(alerts);
    })
    .catch(error => {
        console.error('❌ Failed to load alerts:', error);
    });
}

/**
 * Display alerts in the dropdown
 * @param {Array} alerts - The alerts to display
 */
function displayAlerts(alerts) {
    const alertCount = document.getElementById('alertCount');
    
    // Update alert count
    alertCount.textContent = alerts.length > 0 ? alerts.length + (alerts.length > 3 ? '+' : '') : '';
    
    // Update messages panel
    const messagesPanel = document.getElementById('alertMessagesPanel');
    messagesPanel.innerHTML = `
        <h6 class="dropdown-header">Website Alerts</h6>
    `;
    
    // Add each alert message
    alerts.forEach(alert => {
        const messageRow = document.createElement('div');
        messageRow.className = 'dropdown-item d-flex align-items-center';
        
        const iconDiv = document.createElement('div');
        iconDiv.className = 'mr-3';
        iconDiv.innerHTML = `
            <div class="icon-circle bg-warning">
                <i class="fas fa-exclamation-triangle text-white"></i>
            </div>
        `;
        
        const textDiv = document.createElement('div');
        textDiv.innerHTML = `
            <span class="font-weight-bold">${alert.message}</span>
        `;
        
        messageRow.appendChild(iconDiv);
        messageRow.appendChild(textDiv);
        messagesPanel.appendChild(messageRow);
    });
    
    // Add "View All Alerts" link if there are alerts and user has permission
    if (alerts.length > 0) {
        const viewAllLink = document.createElement('a');
        viewAllLink.className = 'dropdown-item text-center small text-gray-500';
        viewAllLink.href = '/admin/alerts';
        viewAllLink.textContent = 'View All Alerts';
        messagesPanel.appendChild(viewAllLink);
    }
}

/**
 * Update the tooltip on the alerts icon to show counts
 * @param {Array} alerts - The alerts
 */
function updateAlertTooltip(alerts) {
    const alertsDropdown = document.getElementById('alertsDropdown');
    if (!alertsDropdown || typeof $ === 'undefined') return;
    
    const activeCount = alerts.length;
    const inactiveCount = 0; // We don't have this information, could be fetched separately
    
    // Update the tooltip title
    $(alertsDropdown).attr('title', `${activeCount} active alert${activeCount !== 1 ? 's' : ''}`);
    
    // Refresh the tooltip
    $(alertsDropdown).tooltip('dispose');
    $(alertsDropdown).tooltip();
}