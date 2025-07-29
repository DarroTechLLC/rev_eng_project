/**
 * Auto-Refresh functionality
 * - Automatically refreshes data every 30 seconds
 * - Updates UI without page reload
 * - Provides seamless data updates
 */
(function($) {
    "use strict";
    
    console.log('🔄 Initializing auto-refresh.js');
    
    // Configuration
    const REFRESH_INTERVAL = 30000; // 30 seconds
    let refreshTimer = null;
    let lastRefreshTime = new Date();
    
    // Page type detection
    let currentPageType = 'unknown';
    
    // Initialize when DOM is ready
    document.addEventListener('DOMContentLoaded', function() {
        detectPageType();
        initAutoRefresh();
        
        console.log(`✅ Auto-refresh initialized for ${currentPageType} page`);
    });
    
    /**
     * Detect the current page type
     */
    function detectPageType() {
        // Check for chart containers
        if (document.getElementById('ytdVolumeChart') || 
            document.getElementById('dailyProductionChart') ||
            document.getElementById('weeklyProductionChart') ||
            document.getElementById('monthlyProductionChart')) {
            currentPageType = 'chart';
        }
        // Check for report containers
        else if (document.querySelector('.pdf-viewer') || 
                document.querySelector('.pdf-container')) {
            currentPageType = 'report';
        }
        // Check for dashboard elements
        else if (document.querySelector('.dashboard-card') ||
                document.querySelector('.dashboard-widget')) {
            currentPageType = 'dashboard';
        }
        
        console.log(`🔍 Detected page type: ${currentPageType}`);
    }
    
    /**
     * Initialize auto-refresh functionality
     */
    function initAutoRefresh() {
        // Clear any existing timer
        if (refreshTimer) {
            clearInterval(refreshTimer);
        }
        
        // Set up the refresh timer
        refreshTimer = setInterval(function() {
            refreshData();
            
            // Update last refresh time
            lastRefreshTime = new Date();
            console.log(`🔄 Data refreshed at ${lastRefreshTime.toLocaleTimeString()}`);
        }, REFRESH_INTERVAL);
        
        console.log(`⏱️ Auto-refresh timer set for ${REFRESH_INTERVAL/1000} seconds`);
    }
    
    /**
     * Refresh data based on page type
     */
    function refreshData() {
        switch(currentPageType) {
            case 'chart':
                refreshChartData();
                break;
            case 'report':
                refreshReportData();
                break;
            case 'dashboard':
                refreshDashboardData();
                break;
            default:
                console.log('ℹ️ No specific refresh handler for this page type');
                break;
        }
    }
    
    /**
     * Refresh chart data
     */
    function refreshChartData() {
        console.log('📊 Refreshing chart data');
        
        // Check for YTD Volume chart
        if (document.getElementById('ytdVolumeChart') && window.updateChart) {
            console.log('📊 Refreshing YTD Volume chart');
            window.updateChart();
        }
        
        // Check for Weekly Report charts
        if (document.getElementById('dailyProductionChart') && window.initCharts) {
            console.log('📊 Refreshing Weekly Report charts');
            window.initCharts();
        }
        
        // For other charts, look for their update functions
        const chartUpdateFunctions = [
            'updateDailyVolumeChart',
            'updateMTDVolumeChart',
            'updateYTDVolumeChart',
            'updateProductionHeadcountChart'
        ];
        
        chartUpdateFunctions.forEach(funcName => {
            if (typeof window[funcName] === 'function') {
                console.log(`📊 Calling chart update function: ${funcName}`);
                window[funcName]();
            }
        });
    }
    
    /**
     * Refresh report data
     */
    function refreshReportData() {
        console.log('📄 Refreshing report data');
        
        // Get current date from input if available
        const dateInput = document.querySelector('input[type="date"]');
        if (dateInput) {
            const currentDate = dateInput.value;
            
            // Check if we have an updateReport function (daily-report.html)
            if (typeof window.updateReport === 'function') {
                console.log(`📄 Refreshing report for date: ${currentDate}`);
                
                // Instead of navigating to a new URL, fetch the data via AJAX
                fetchReportData(currentDate);
            }
        }
    }
    
    /**
     * Fetch report data via AJAX
     * @param {string} date - The date to fetch data for
     */
    function fetchReportData(date) {
        // Get company ID
        const companyId = document.body.getAttribute('data-company-id');
        if (!companyId) {
            console.warn('⚠️ No company ID found for report refresh');
            return;
        }
        
        // Get company name from URL path
        const pathParts = window.location.pathname.split('/');
        let companyName = '';
        if (pathParts.length > 1) {
            companyName = pathParts[1];
        }
        
        // Construct the API URL
        const apiUrl = `/api/reports/daily-pdf/${companyName}?company_id=${companyId}&date=${date}`;
        
        // Fetch the report data
        fetch(apiUrl)
            .then(response => {
                if (!response.ok) {
                    if (response.headers.get('content-type').includes('application/json')) {
                        return response.json().then(errorData => {
                            throw new Error(errorData.error || 'Failed to load report');
                        });
                    }
                    throw new Error('Failed to load report');
                }
                return response.blob();
            })
            .then(blob => {
                // Create object URL for the PDF
                const pdfUrl = URL.createObjectURL(blob);
                
                // Update the iframe source
                const iframe = document.querySelector('.pdf-viewer');
                if (iframe) {
                    iframe.src = pdfUrl;
                    console.log('✅ PDF viewer updated with new data');
                }
            })
            .catch(error => {
                console.error('❌ Error refreshing report:', error);
            });
    }
    
    /**
     * Refresh dashboard data
     */
    function refreshDashboardData() {
        console.log('🏠 Refreshing dashboard data');
        
        // Refresh all charts on the dashboard
        refreshChartData();
        
        // Refresh any widgets with data-auto-refresh attribute
        document.querySelectorAll('[data-auto-refresh="true"]').forEach(widget => {
            const refreshFunction = widget.getAttribute('data-refresh-function');
            if (refreshFunction && typeof window[refreshFunction] === 'function') {
                console.log(`🔄 Refreshing widget with function: ${refreshFunction}`);
                window[refreshFunction]();
            }
        });
    }
    
    // Expose functions to global scope for debugging
    window.autoRefresh = {
        refreshNow: refreshData,
        getLastRefreshTime: function() { return lastRefreshTime; },
        getRefreshInterval: function() { return REFRESH_INTERVAL; }
    };
    
    console.log('✅ auto-refresh.js fully initialized');
})(jQuery);