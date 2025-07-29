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
    let companyName = '';
    let isAdmin = false;
    let isSuperAdmin = false;

    if (!companyId) {
        console.warn('⚠️ No company ID found for alerts');
        return;
    }

    // Check if there's a debug info element with the selected company ID
    const debugInfoElement = document.getElementById('debug-info');
    if (debugInfoElement) {
        const selectedCompanyIdElement = debugInfoElement.querySelector('span');
        if (selectedCompanyIdElement) {
            const selectedCompanyId = selectedCompanyIdElement.textContent;
            console.log('🏢 Debug info - Selected Company ID:', selectedCompanyId);
        }
    }

    // Try to get company name from the current company display
    const currentCompanyDisplay = document.getElementById('currentCompanyDisplay');
    if (currentCompanyDisplay) {
        const logoImg = currentCompanyDisplay.querySelector('img');
        if (logoImg && logoImg.title) {
            companyName = logoImg.title;
            console.log('🏢 Found company name from logo:', companyName);
        } else if (currentCompanyDisplay.querySelector('.company-logo-placeholder')) {
            const placeholder = currentCompanyDisplay.querySelector('.company-logo-placeholder');
            if (placeholder && placeholder.title) {
                companyName = placeholder.title;
                console.log('🏢 Found company name from placeholder:', companyName);
            }
        }
    }

    console.log('🏢 Loading alerts for company:', companyId, companyName ? `(${companyName})` : '');

    // Store company name in a data attribute for later use
    document.body.setAttribute('data-company-name', companyName);

    // Check if user is admin
    fetch('/api/user-roles/current')
        .then(response => {
            if (!response.ok) {
                throw new Error(`Error ${response.status}: ${response.statusText}`);
            }
            return response.json();
        })
        .then(data => {
            if (data.success) {
                console.log('🔑 User roles loaded for alerts:', data);
                isAdmin = data.isAdmin === true;
                isSuperAdmin = data.isSuperAdmin === true;

                // Store user roles in data attributes for later use
                document.body.setAttribute('data-is-admin', isAdmin);
                document.body.setAttribute('data-is-super-admin', isSuperAdmin);

                console.log(`👤 User is ${isAdmin ? 'an admin' : 'not an admin'} and ${isSuperAdmin ? 'a super admin' : 'not a super admin'}`);

                // Set the href attribute of the alertsDropdown link based on user role
                const alertsDropdown = document.getElementById('alertsDropdown');
                if (alertsDropdown) {
                    if (isAdmin || isSuperAdmin) {
                        // For admin users, make the bell icon clickable to go to the alerts page
                        alertsDropdown.href = '/admin/alerts';
                        console.log('🔔 Bell icon set to link to alerts page for admin user');
                    } else {
                        // For regular users, keep the default '#' to just toggle the dropdown
                        console.log('🔔 Bell icon set to not link anywhere for regular user');
                    }
                }

                // Fetch alerts based on user role
                if (isAdmin || isSuperAdmin) {
                    console.log('👑 Admin user detected - fetching all active alerts');
                    fetchAllActiveAlerts();
                } else {
                    console.log('👤 Regular user - fetching alerts for company:', companyId);
                    fetchActiveAlerts(companyId);
                }
            } else {
                console.warn('⚠️ Not logged in or error fetching roles');
                // Default to company-specific alerts if role check fails
                fetchActiveAlerts(companyId);
            }
        })
        .catch(error => {
            console.error('❌ Error fetching role information:', error);
            // Default to company-specific alerts if role check fails
            fetchActiveAlerts(companyId);
        });

    // Set up tooltip for the alerts icon
    const alertsDropdown = document.getElementById('alertsDropdown');
    if (alertsDropdown && typeof $ !== 'undefined') {
        try {
            if (typeof $.fn.tooltip === 'function') {
                $(alertsDropdown).tooltip();
            } else {
                console.log('[DEBUG_LOG] Tooltip function not available, skipping tooltip initialization');
            }
        } catch (e) {
            console.log('[DEBUG_LOG] Error initializing tooltip:', e);
        }
    }
}

/**
 * Fetch all active alerts (for admin users)
 */
function fetchAllActiveAlerts() {
    fetch('/api/admin/website-alerts/list', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        }
    })
    .then(response => {
        if (!response.ok) {
            throw new Error(`HTTP error! Status: ${response.status}`);
        }
        return response.json();
    })
    .then(result => {
        console.log('🔔 All alerts response (admin view):', result);

        if (result.error) {
            console.error('❌ Error fetching all alerts:', result.error);
            return;
        }

        // Filter to only include active alerts
        const allAlerts = result.data || [];
        const activeAlerts = allAlerts.filter(alert => alert.is_active === true);

        console.log('👑 Admin view - Total alerts:', allAlerts.length, 'Active alerts:', activeAlerts.length);

        // Process the active alerts
        processAlerts(activeAlerts, true);
    })
    .catch(error => {
        console.error('❌ Failed to load all alerts:', error);
        // Show empty state
        processAlerts([], true);
    });
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

        // Enhanced debugging: Log each alert with its ID and active status
        console.log('[DEBUG_LOG] ===== ALERT DEBUGGING INFORMATION =====');
        console.log('[DEBUG_LOG] Total alerts received from API:', alerts.length);

        // Count active alerts explicitly
        const activeAlerts = alerts.filter(alert => alert.is_active === true);
        console.log('[DEBUG_LOG] Active alerts count:', activeAlerts.length);

        // Log details of each alert
        alerts.forEach((alert, index) => {
            console.log(`[DEBUG_LOG] Alert #${index + 1} - ID: ${alert.id}, Active: ${alert.is_active}, Message: "${alert.message.substring(0, 30)}${alert.message.length > 30 ? '...' : ''}"`);
        });

        // Process the alerts (false = not admin view)
        processAlerts(alerts, false);
    })
    .catch(error => {
        console.error('❌ Failed to load alerts:', error);
        // Show empty state
        processAlerts([], false);
    });
}

/**
 * Process alerts before displaying
 * @param {Array} alerts - The alerts to process
 * @param {boolean} isAdminView - Whether this is an admin view (all alerts)
 */
function processAlerts(alerts, isAdminView) {
    // Filter to only include active alerts
    const activeAlerts = alerts.filter(alert => alert.is_active === true);

    console.log(`🔔 Processing ${isAdminView ? 'all' : 'company'} alerts:`, alerts);
    console.log(`🔢 Total alerts: ${alerts.length}, Active alerts: ${activeAlerts.length}`);

    // Check if user is admin or super admin
    const isAdmin = document.body.getAttribute('data-is-admin') === 'true';
    const isSuperAdmin = document.body.getAttribute('data-is-super-admin') === 'true';
    const isAdminUser = isAdmin || isSuperAdmin;

    // Display the active alerts
    displayAlerts(activeAlerts, isAdminView, isAdminUser);

    // Update the tooltip to show counts
    updateAlertTooltip(activeAlerts, isAdminView);
}

/**
 * Display alerts in the dropdown and mobile menu
 * @param {Array} activeAlerts - The active alerts to display
 * @param {boolean} isAdminView - Whether this is an admin view (all alerts)
 * @param {boolean} isAdminUser - Whether the current user is an admin or super admin
 */
function displayAlerts(activeAlerts, isAdminView, isAdminUser) {
    const alertCount = document.getElementById('alertCount');
    const companyName = document.body.getAttribute('data-company-name') || '';

    console.log('🔔 Displaying alerts:', activeAlerts);
    console.log('🔢 Number of active alerts:', activeAlerts.length);
    console.log('👤 Is admin user:', isAdminUser);

    // Update alert count - show the actual number of active alerts
    if (alertCount) {
        alertCount.textContent = activeAlerts.length > 0 ? activeAlerts.length : '';
        console.log('[DEBUG_LOG] Bell count has been updated to show:', alertCount.textContent);
    }

    // Update desktop dropdown panel
    updateDesktopAlerts(activeAlerts, isAdminView, isAdminUser, companyName);

    // Update mobile alerts in hamburger menu
    updateMobileAlerts(activeAlerts, isAdminView, isAdminUser, companyName);
}

/**
 * Update alerts in the desktop dropdown
 * @param {Array} activeAlerts - The active alerts to display
 * @param {boolean} isAdminView - Whether this is an admin view (all alerts)
 * @param {boolean} isAdminUser - Whether the current user is an admin or super admin
 * @param {string} companyName - The company name
 */
function updateDesktopAlerts(activeAlerts, isAdminView, isAdminUser, companyName) {
    // Update messages panel
    const messagesPanel = document.getElementById('alertMessagesPanel');
    if (!messagesPanel) return;

    messagesPanel.innerHTML = `
        <h6 class="dropdown-header">Website Alerts</h6>
    `;

    // Check if there are any alerts to display
    if (activeAlerts.length === 0) {
        // No alerts - show a message
        const noAlertsMessage = document.createElement('div');
        noAlertsMessage.className = 'dropdown-item text-center';

        if (isAdminView) {
            noAlertsMessage.textContent = 'No active alerts in the system';
        } else {
            noAlertsMessage.textContent = `No alerts for ${companyName || 'this company'}`;
        }

        messagesPanel.appendChild(noAlertsMessage);
        console.log(`[DEBUG_LOG] No alerts message added to desktop: "${noAlertsMessage.textContent}"`);
        return;
    }

    // Add each active alert message to the dropdown
    activeAlerts.forEach(alert => {
        // For admin users, create a clickable link to edit the alert
        // For regular users, create a non-clickable div
        const messageRow = document.createElement(isAdminUser ? 'a' : 'div');
        messageRow.className = 'dropdown-item d-flex align-items-center';

        // Only set href for admin users
        if (isAdminUser) {
            messageRow.href = '/admin/alerts';
            console.log('[DEBUG_LOG] Created clickable alert for admin user (links to alerts page)');
        } else {
            console.log('[DEBUG_LOG] Created non-clickable alert for regular user');
        }

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

    console.log('[DEBUG_LOG] Added ' + activeAlerts.length + ' active alerts to desktop dropdown');

    // Add "View All Alerts" link if there are active alerts and user has permission
    if (isAdminUser) {
        const viewAllLink = document.createElement('a');
        viewAllLink.className = 'dropdown-item text-center small text-gray-500';
        viewAllLink.href = '/admin/alerts';
        viewAllLink.textContent = 'View All Alerts';
        messagesPanel.appendChild(viewAllLink);
        console.log('[DEBUG_LOG] Added "View All Alerts" link to desktop dropdown (admin user)');
    } else {
        console.log('[DEBUG_LOG] Skipped "View All Alerts" link for non-admin user');
    }
}

/**
 * Update alerts in the mobile hamburger menu
 * @param {Array} activeAlerts - The active alerts to display
 * @param {boolean} isAdminView - Whether this is an admin view (all alerts)
 * @param {boolean} isAdminUser - Whether the current user is an admin or super admin
 * @param {string} companyName - The company name
 */
function updateMobileAlerts(activeAlerts, isAdminView, isAdminUser, companyName) {
    // Get the mobile alerts container
    const mobileAlertMessages = document.getElementById('mobileAlertMessages');
    if (!mobileAlertMessages) {
        console.log('[DEBUG_LOG] Mobile alerts container not found, skipping mobile update');
        return;
    }

    console.log('[DEBUG_LOG] Updating mobile alerts in hamburger menu');

    // Clear existing content
    mobileAlertMessages.innerHTML = '';

    // Check if there are any alerts to display
    if (activeAlerts.length === 0) {
        // No alerts - show a message
        const noAlertsMessage = document.createElement('div');
        noAlertsMessage.className = 'mobile-link no-alerts-message';

        if (isAdminView) {
            noAlertsMessage.innerHTML = '<i class="fas fa-fw fa-info-circle"></i> No active alerts in the system';
        } else {
            noAlertsMessage.innerHTML = `<i class="fas fa-fw fa-info-circle"></i> No alerts for ${companyName || 'this company'}`;
        }

        mobileAlertMessages.appendChild(noAlertsMessage);
        console.log(`[DEBUG_LOG] No alerts message added to mobile: "${noAlertsMessage.textContent}"`);
        return;
    }

    // Add each active alert message to the mobile menu
    activeAlerts.forEach(alert => {
        const alertLink = document.createElement(isAdminUser ? 'a' : 'div');
        alertLink.className = 'mobile-link';

        // Only set href for admin users
        if (isAdminUser) {
            alertLink.href = '/admin/alerts';
        }

        alertLink.innerHTML = `
            <i class="fas fa-fw fa-exclamation-triangle"></i> ${alert.message}
        `;

        mobileAlertMessages.appendChild(alertLink);
    });

    console.log('[DEBUG_LOG] Added ' + activeAlerts.length + ' active alerts to mobile menu');

    // Add "View All Alerts" link if user has permission
    if (isAdminUser) {
        const viewAllLink = document.createElement('a');
        viewAllLink.className = 'mobile-link view-all-link';
        viewAllLink.href = '/admin/alerts';
        viewAllLink.innerHTML = '<i class="fas fa-fw fa-list"></i> View All Alerts';
        mobileAlertMessages.appendChild(viewAllLink);
        console.log('[DEBUG_LOG] Added "View All Alerts" link to mobile menu (admin user)');
    }
}

/**
 * Update the tooltip on the alerts icon to show counts
 * @param {Array} activeAlerts - The active alerts
 * @param {boolean} isAdminView - Whether this is an admin view (all alerts)
 */
function updateAlertTooltip(activeAlerts, isAdminView) {
    const alertsDropdown = document.getElementById('alertsDropdown');
    if (!alertsDropdown || typeof $ === 'undefined') return;

    const activeCount = activeAlerts.length;

    // Get company name from data attribute
    const companyName = document.body.getAttribute('data-company-name') || '';

    console.log('🔔 Updating tooltip with active count:', activeCount);
    console.log('[DEBUG_LOG] Tooltip - Active alerts count:', activeCount);
    console.log('[DEBUG_LOG] Tooltip - Company name:', companyName);
    console.log('[DEBUG_LOG] Tooltip - Admin view:', isAdminView);

    // Create tooltip text based on context
    let tooltipText;

    if (activeCount === 0) {
        // No alerts case
        if (isAdminView) {
            tooltipText = 'No active alerts in the system';
        } else {
            tooltipText = `No alerts for ${companyName || 'this company'}`;
        }
    } else {
        // With alerts case
        if (isAdminView) {
            tooltipText = `${activeCount} active alert${activeCount !== 1 ? 's' : ''} system-wide`;
        } else {
            tooltipText = `${activeCount} active alert${activeCount !== 1 ? 's' : ''}`;
            if (companyName) {
                tooltipText += `\n${companyName}`;
            }
        }
    }

    $(alertsDropdown).attr('title', tooltipText);
    console.log('[DEBUG_LOG] Tooltip text set to:', tooltipText);

    // Refresh the tooltip
    try {
        if (typeof $.fn.tooltip === 'function') {
            $(alertsDropdown).tooltip('dispose');
            $(alertsDropdown).tooltip();
        } else {
            console.log('[DEBUG_LOG] Tooltip function not available, skipping tooltip refresh');
        }
    } catch (e) {
        console.log('[DEBUG_LOG] Error refreshing tooltip:', e);
    }
}
