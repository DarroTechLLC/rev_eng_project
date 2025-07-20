/**
 * Report Layout Fix
 * This script fixes layout issues with section cards and replaces Chart.js with Highcharts
 */

// Execute immediately to ensure it runs as soon as possible
(function() {
    console.log('Report layout fix script executing');

    // Function to initialize everything once the DOM is ready
    function initialize() {
        console.log('Report layout fix initializing');

        // Fix layout first
        fixSectionCardsLayout();

        // Then check if Highcharts is available
        if (typeof Highcharts === 'undefined') {
            console.error('Highcharts not available, loading required scripts');
            loadHighchartsScripts();
        } else {
            // Highcharts is available, initialize charts
            initializeHighcharts();
        }
    }

    // Wait for the DOM to be fully loaded
    if (document.readyState === 'loading') {
        document.addEventListener('DOMContentLoaded', initialize);
    } else {
        // DOM already loaded, run immediately
        initialize();
    }

    // Also run on window load to ensure all resources are loaded
    window.addEventListener('load', function() {
        console.log('Window loaded, ensuring fixes are applied');
        fixSectionCardsLayout();
        if (typeof Highcharts !== 'undefined') {
            initializeHighcharts();
        }
    });
})();

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
 * Wraps all section cards in Bootstrap grid rows with col-12 classes
 * to prevent overlapping
 */
function fixSectionCardsLayout() {
    console.log('Fixing section cards layout');

    // Get all section cards
    const sectionCards = document.querySelectorAll('.section-card');
    console.log('Found', sectionCards.length, 'section cards');

    // Process each section card
    sectionCards.forEach(function(card, index) {
        // Skip if already wrapped
        if (card.parentElement.classList.contains('col-12')) {
            console.log('Card', index, 'already wrapped, skipping');
            return;
        }

        // Skip if parent is a row
        if (card.parentElement.classList.contains('row')) {
            console.log('Card', index, 'parent is a row, skipping');
            return;
        }

        // Create row and column elements
        const row = document.createElement('div');
        row.className = 'row mb-4';

        const col = document.createElement('div');
        col.className = 'col-12';

        // Get the parent element
        const parent = card.parentElement;

        // Clone the card to avoid reference issues
        const cardClone = card.cloneNode(true);

        // Insert the new structure
        try {
            // Remove the original card
            parent.removeChild(card);

            // Add the new structure
            parent.appendChild(row);
            row.appendChild(col);
            col.appendChild(cardClone);

            console.log('Fixed layout for section card', index);
        } catch (e) {
            console.error('Error fixing layout for card', index, e);
        }
    });
}

/**
 * Initialize Highcharts for all chart containers
 */
function initializeHighcharts() {
    console.log('Initializing Highcharts');

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

    // First, replace any existing Chart.js canvas elements
    replaceChartJsWithHighcharts();

    // Then, initialize any empty Highcharts containers
    initializeEmptyChartContainers();
}

/**
 * Replaces Chart.js charts with Highcharts
 */
function replaceChartJsWithHighcharts() {
    console.log('Replacing Chart.js with Highcharts');

    // Find all canvas elements used for Chart.js
    const canvasElements = document.querySelectorAll('canvas');
    console.log('Found', canvasElements.length, 'canvas elements');

    // Process each canvas element
    canvasElements.forEach(function(canvas, index) {
        try {
            // Get the parent container
            const container = canvas.parentElement;

            // Skip if already processed
            if (container.querySelector('.highcharts-container')) {
                console.log('Canvas', index, 'already processed, skipping');
                return;
            }

            // Get the canvas ID
            const canvasId = canvas.id || 'chart-' + index;
            console.log('Converting chart:', canvasId);

            // Create a div for Highcharts
            const chartDiv = document.createElement('div');
            chartDiv.id = canvasId;

            // Replace the canvas with the div
            container.replaceChild(chartDiv, canvas);

            // Initialize the chart
            initializeChart(chartDiv);

            console.log('Converted chart:', canvasId);
        } catch (e) {
            console.error('Error converting canvas', index, e);
        }
    });
}

/**
 * Initialize any empty chart containers
 */
function initializeEmptyChartContainers() {
    console.log('Initializing empty chart containers');

    // Find all chart containers that don't have Highcharts initialized
    const chartContainers = document.querySelectorAll('.chart-container > div:not(:has(.highcharts-container))');
    console.log('Found', chartContainers.length, 'empty chart containers');

    // Process each container
    chartContainers.forEach(function(container, index) {
        try {
            // Skip if already has Highcharts
            if (container.querySelector('.highcharts-container')) {
                return;
            }

            console.log('Initializing chart container:', container.id);

            // Initialize the chart
            initializeChart(container);

            console.log('Initialized chart container:', container.id);
        } catch (e) {
            console.error('Error initializing chart container', index, e);
        }
    });
}

/**
 * Initialize a chart based on its ID
 */
function initializeChart(chartElement) {
    const chartId = chartElement.id;

    // Initialize Highcharts based on the chart type
    if (chartId.includes('Trend') || chartId.includes('trend')) {
        // For trend charts
        const sampleData = generateSampleTimeSeriesData(7, 10, 40);
        createProductionTrendChart(chartId, sampleData, 'Production Trend');
    } else if (chartId.includes('Distribution') || chartId.includes('Pie')) {
        // For distribution/pie charts
        const farmNames = ['Farm A', 'Farm B', 'Farm C', 'Farm D'];
        const farmData = generateSampleFarmDistribution(farmNames);
        createFarmDistributionChart(chartId, farmData, 'Production by Farm');
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
        createComparisonChart(chartId, categories, series, 'Production Comparison');
    }
}
