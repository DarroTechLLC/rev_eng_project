/**
 * Daily Report Layout Fix
 * This script ensures proper layout for the daily report page
 */

// Execute when the document is ready
document.addEventListener('DOMContentLoaded', function() {
    console.log('Daily report layout fix initializing');

    // Fix double scrollbar issue
    fixDoubleScrollbarIssue();

    // Load Highcharts if not already loaded
    if (typeof Highcharts === 'undefined') {
        loadHighchartsScripts();
    } else {
        // Highcharts is available, initialize charts
        initializeHighcharts();
    }
});

/**
 * Fix the double scrollbar issue by adjusting CSS properties
 */
function fixDoubleScrollbarIssue() {
    console.log('Fixing double scrollbar issue');

    // Add CSS to fix the double scrollbar issue
    const style = document.createElement('style');
    style.textContent = `
        html, body {
            overflow-y: hidden !important;
        }
        #content-wrapper {
            overflow-y: auto !important;
            height: 100vh !important;
        }
        #content {
            overflow: visible !important;
        }
        .container-fluid {
            overflow: visible !important;
        }
    `;
    document.head.appendChild(style);

    // Remove any extra container-fluid wrappers that might be causing the issue
    const containerFluids = document.querySelectorAll('.container-fluid .container-fluid');
    containerFluids.forEach(function(container) {
        console.log('Removing extra container-fluid wrapper');
        const parent = container.parentElement;
        while (container.firstChild) {
            parent.insertBefore(container.firstChild, container);
        }
        parent.removeChild(container);
    });
}

/**
 * Load Highcharts scripts if not already loaded
 */
function loadHighchartsScripts() {
    // Function to load a script and execute callback when loaded
    function loadScript(url, callback) {
        const script = document.createElement('script');
        script.type = 'text/javascript';
        script.src = url;
        script.onload = callback;
        document.head.appendChild(script);
    }

    // Load scripts in sequence
    loadScript('https://code.highcharts.com/highcharts.js', function() {
        loadScript('https://code.highcharts.com/modules/exporting.js', function() {
            loadScript('https://code.highcharts.com/modules/export-data.js', function() {
                loadScript('https://code.highcharts.com/modules/accessibility.js', function() {
                    loadScript('/js/chart-utils/highcharts-utils.js', function() {
                        // All scripts loaded, now initialize Highcharts
                        console.log('All Highcharts scripts loaded');
                        initializeHighcharts();
                    });
                });
            });
        });
    });
}

/**
 * Initialize Highcharts for all chart containers
 */
function initializeHighcharts() {
    console.log('Initializing Highcharts for daily report');

    // Check if Highcharts utility functions are available
    if (typeof Highcharts === 'undefined') {
        console.error('Highcharts not available');
        return;
    }

    if (typeof createProductionTrendChart === 'undefined' ||
        typeof createFarmDistributionChart === 'undefined' ||
        typeof generateSampleTimeSeriesData === 'undefined') {
        console.error('Highcharts utility functions not available');
        return;
    }

    // Initialize daily trend chart
    if (document.getElementById('dailyTrendChart')) {
        // Generate sample data
        const sampleData = generateSampleTimeSeriesData(7, 10, 40);
        const chart = createProductionTrendChart('dailyTrendChart', sampleData, 'Daily Production Trend');
        
        // Add export functionality if chart was created successfully
        if (chart && typeof initChartExportMenu === 'function') {
            initChartExportMenu(chart, document.getElementById('dailyTrendChart'));
        }
    }

    // Initialize farm distribution chart
    if (document.getElementById('farmDistributionChart')) {
        // Generate sample farm data
        const farmNames = ['Farm A', 'Farm B', 'Farm C', 'Farm D'];
        const farmData = generateSampleFarmDistribution(farmNames);
        const chart = createFarmDistributionChart('farmDistributionChart', farmData, 'Production by Farm');
        
        // Add export functionality if chart was created successfully
        if (chart && typeof initChartExportMenu === 'function') {
            initChartExportMenu(chart, document.getElementById('farmDistributionChart'));
        }
    }
}

// Also run on window load to ensure all resources are loaded
window.addEventListener('load', function() {
    console.log('Window loaded, ensuring charts are initialized');
    if (typeof Highcharts !== 'undefined') {
        initializeHighcharts();
    }
});
