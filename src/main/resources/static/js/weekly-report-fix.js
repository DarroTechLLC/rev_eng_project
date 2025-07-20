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
        loadScript('https://code.highcharts.com/highcharts.js', function() {
            loadScript('https://code.highcharts.com/modules/exporting.js', function() {
                loadScript('https://code.highcharts.com/modules/export-data.js', function() {
                    loadScript('https://code.highcharts.com/modules/accessibility.js', function() {
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
        
        // Initialize Highcharts based on the chart type
        if (canvasId.includes('dailyProductionChart')) {
            // For daily production chart
            const sampleData = generateSampleTimeSeriesData(7, 10, 40);
            createProductionTrendChart(canvasId, sampleData, 'Daily Production Volume');
        } else if (canvasId.includes('weeklyProductionChart')) {
            // For weekly production chart
            const sampleData = generateSampleTimeSeriesData(5, 20, 60);
            createProductionTrendChart(canvasId, sampleData, 'Weekly Production Trend');
        } else if (canvasId.includes('monthlyProductionChart')) {
            // For monthly production chart
            const categories = ['Jan', 'Feb', 'Mar', 'Apr', 'May'];
            const series = [
                {
                    name: 'Production',
                    data: [30, 35, 32, 38, 34]
                },
                {
                    name: 'Budget',
                    data: [32, 34, 33, 35, 36]
                }
            ];
            createComparisonChart(canvasId, categories, series, 'Monthly Production Comparison');
        } else if (canvasId.includes('ytdProductionPieChart')) {
            // For YTD production pie chart
            const farmNames = ['Farm A', 'Farm B', 'Farm C', 'Farm D'];
            const farmData = generateSampleFarmDistribution(farmNames);
            createFarmDistributionChart(canvasId, farmData, 'YTD Production by Farm');
        } else if (canvasId.includes('lagoonInflationChart')) {
            // For lagoon inflation chart
            const categories = ['Farm A', 'Farm B', 'Farm C', 'Farm D'];
            const series = [
                {
                    name: 'Current Week',
                    data: [75, 85, 65, 80]
                },
                {
                    name: 'Previous Week',
                    data: [70, 80, 60, 75]
                }
            ];
            createComparisonChart(canvasId, categories, series, 'Lagoon Inflation Levels');
        } else if (canvasId.includes('ftAboveBermChart')) {
            // For FT above berm chart
            const categories = ['Farm A', 'Farm B', 'Farm C', 'Farm D'];
            const series = [
                {
                    name: 'Current Week',
                    data: [5.2, 4.8, 5.5, 4.9]
                },
                {
                    name: 'Previous Week',
                    data: [5.0, 4.5, 5.3, 4.7]
                }
            ];
            createComparisonChart(canvasId, categories, series, 'FT Above the Berm Measurements');
        } else {
            // For other charts
            const sampleData = generateSampleTimeSeriesData(7, 10, 40);
            createProductionTrendChart(canvasId, sampleData, 'Production Chart');
        }
    });
}

// Load scripts when the document is ready
document.addEventListener('DOMContentLoaded', function() {
    loadHighchartsScripts();
});