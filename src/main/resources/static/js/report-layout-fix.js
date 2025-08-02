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

        // Insert the new structure without cloning
        try {
            // Insert the row before the card
            parent.insertBefore(row, card);

            // Move the card into the column and the column into the row
            col.appendChild(card);
            row.appendChild(col);

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
    console.group('🔄 Chart Initialization');
    console.log('Current Time:', new Date().toISOString());
    
    // Log available chart containers
    const chartContainers = document.querySelectorAll('[id$="Chart"]');
    console.log('Found Chart Containers:', Array.from(chartContainers).map(el => el.id));
    
    // Check for date range inputs
    const dateInputs = document.querySelectorAll('input[type="date"], input[type="datetime-local"]');
    if (dateInputs.length > 0) {
        console.log('Date Inputs Found:', Array.from(dateInputs).map(input => ({
            id: input.id,
            value: input.value
        })));
    } else {
        console.log('No date input elements found on page');
    }

    // Check if Highcharts utility functions are available
    if (typeof Highcharts === 'undefined') {
        console.error('Highcharts not available');
        return;
    }

    if (typeof createProductionTrendChart === 'undefined' ||
        typeof createFarmDistributionChart === 'undefined' ||
        typeof createComparisonChart === 'undefined') {
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

    // Create a no-data message
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
    chartElement.appendChild(noDataMessage);

    // Log the error
    console.warn(`Chart ${chartId} requires data from the API. Sample data has been removed.`);
}
