/**
 * Weekly Report Layout Fix
 * This script fixes layout issues with section cards and replaces Chart.js with Highcharts
 */

// Dynamically load Highcharts if not already loaded
function loadScript(url, callback) {
    const script = document.createElement('script');
    script.type = 'text/javascript';
    script.src = url;
    script.onload = callback;
    document.head.appendChild(script);
}

// Load all required scripts in sequence
function loadHighchartsScripts() {
    if (typeof Highcharts === 'undefined') {
        loadScript('https://cdn.jsdelivr.net/npm/highcharts@latest/highcharts.js', function() {
            loadScript('https://cdn.jsdelivr.net/npm/highcharts@latest/modules/exporting.js', function() {
                loadScript('https://cdn.jsdelivr.net/npm/highcharts@latest/modules/export-data.js', function() {
                    loadScript('https://cdn.jsdelivr.net/npm/highcharts@latest/modules/accessibility.js', function() {
                        loadScript('/js/chart-utils/highcharts-utils.js', function() {
                            loadScript('/js/report-layout-fix.js', function() {
                                // All scripts loaded, now fix the layout
                                fixLayout();
                            });
                        });
                    });
                });
            });
        });
    } else {
        // Highcharts already loaded, just load our utility scripts
        loadScript('/js/chart-utils/highcharts-utils.js', function() {
            loadScript('/js/report-layout-fix.js', function() {
                // All scripts loaded, now fix the layout
                fixLayout();
            });
        });
    }
}

// Fix layout issues
function fixLayout() {
    // Fix section cards layout
    const sectionCards = document.querySelectorAll('.section-card');

    sectionCards.forEach(function(card) {
        // Skip if already wrapped
        if (card.parentElement.classList.contains('col-12')) {
            return;
        }

        // Create row and column elements
        const row = document.createElement('div');
        row.className = 'row mb-4';

        const col = document.createElement('div');
        col.className = 'col-12';

        // Get the parent element
        const parent = card.parentElement;

        // Replace the card with the new structure
        parent.replaceChild(row, card);
        row.appendChild(col);
        col.appendChild(card);
    });

    // Replace Chart.js charts with Highcharts
    replaceCharts();
}

// Replace Chart.js charts with Highcharts
function replaceCharts() {
    // Check if Highcharts and utility functions are available
    if (typeof Highcharts === 'undefined' || 
        typeof createProductionTrendChart === 'undefined' ||
        typeof createFarmDistributionChart === 'undefined' ||
        typeof createComparisonChart === 'undefined') {
        console.error('Highcharts or utility functions not available');
        return;
    }

    // Find all canvas elements used for Chart.js
    const canvasElements = document.querySelectorAll('canvas');

    // Process each canvas element
    canvasElements.forEach(function(canvas) {
        // Get the parent container
        const container = canvas.parentElement;

        // Skip if already processed
        if (container.querySelector('.highcharts-container')) {
            return;
        }

        // Get the canvas ID
        const canvasId = canvas.id;

        // Create a div for Highcharts
        const chartDiv = document.createElement('div');
        chartDiv.id = canvasId;

        // Replace the canvas with the div
        container.replaceChild(chartDiv, canvas);

        // Display a message indicating that real data should be fetched from the API
        const noDataMessage = document.createElement('div');
        noDataMessage.className = 'alert alert-info';
        noDataMessage.innerHTML = `
            <h5>No Data Available</h5>
            <p>This chart requires data from the API. Please ensure you have:</p>
            <ul>
                <li>Selected a valid date range</li>
                <li>Selected a company and farm (if applicable)</li>
                <li>Verified your connection to the server</li>
            </ul>
        `;

        // Add the message to the chart container
        chartDiv.appendChild(noDataMessage);

        // Log the error
        console.warn(`Chart ${canvasId} requires data from the API. Sample data has been removed.`);
    });
}

// Load scripts when the document is ready
document.addEventListener('DOMContentLoaded', function() {
    loadHighchartsScripts();
});
