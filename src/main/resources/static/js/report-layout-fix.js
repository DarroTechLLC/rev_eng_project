/**
 * Report Layout Fix
 * This script fixes layout issues with section cards and replaces Chart.js with Highcharts
 */

document.addEventListener('DOMContentLoaded', function() {
    // Fix section cards layout
    fixSectionCardsLayout();
    
    // Replace Chart.js with Highcharts
    replaceChartJsWithHighcharts();
});

/**
 * Wraps all section cards in Bootstrap grid rows with col-12 classes
 * to prevent overlapping
 */
function fixSectionCardsLayout() {
    // Get all section cards
    const sectionCards = document.querySelectorAll('.section-card');
    
    // Process each section card
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
}

/**
 * Replaces Chart.js charts with Highcharts
 * Uses the utility functions from highcharts-utils.js
 */
function replaceChartJsWithHighcharts() {
    // Check if Highcharts is available
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
        
        // Remove the canvas
        container.removeChild(canvas);
        
        // Create a div for Highcharts
        const chartDiv = document.createElement('div');
        chartDiv.id = canvasId;
        container.appendChild(chartDiv);
        
        // Initialize Highcharts based on the chart type
        if (canvasId.includes('Trend') || canvasId.includes('trend')) {
            // For trend charts
            const sampleData = generateSampleTimeSeriesData(7, 10, 40);
            createProductionTrendChart(canvasId, sampleData, 'Production Trend');
        } else if (canvasId.includes('Distribution') || canvasId.includes('Pie')) {
            // For distribution/pie charts
            const farmNames = ['Farm A', 'Farm B', 'Farm C', 'Farm D'];
            const farmData = generateSampleFarmDistribution(farmNames);
            createFarmDistributionChart(canvasId, farmData, 'Production by Farm');
        } else {
            // For other charts (comparison, etc.)
            const categories = ['Jan', 'Feb', 'Mar', 'Apr', 'May'];
            const series = [
                {
                    name: 'Production',
                    data: [10, 15, 12, 18, 14]
                },
                {
                    name: 'Budget',
                    data: [12, 14, 13, 15, 16]
                }
            ];
            createComparisonChart(canvasId, categories, series, 'Production Comparison');
        }
    });
}