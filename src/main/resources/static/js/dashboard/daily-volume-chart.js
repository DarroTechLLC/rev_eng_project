/**
 * Daily Volume Chart Controller
 * Handles the initialization and management of the daily volume chart
 */
class DailyVolumeController {
    constructor() {
        this.chart = null;
        this.currentData = null;
        this.farmColors = {};
        this.debugMode = true; // Enable detailed logging
        this.initializeControls();
        this.loadInitialData();

        // Log initialization
        this.log("DailyVolumeController initialized");
    }

    /**
     * Log messages with consistent format for debugging
     * Enhanced with data inspection capabilities
     */
    log(message, data = null, isError = false) {
        if (this.debugMode) {
            const prefix = "[DEBUG_LOG] DailyVolumeChart: ";
            const timestamp = new Date().toISOString();

            // Format message with data if provided
            let fullMessage = message;
            if (data !== null) {
                try {
                    if (typeof data === 'object') {
                        fullMessage += " - Data: " + JSON.stringify(data);
                    } else {
                        fullMessage += " - Data: " + data;
                    }
                } catch (e) {
                    fullMessage += " - Data: [Object could not be stringified]";
                }
            }

            if (isError) {
                console.error(prefix + fullMessage);
            } else {
                console.log(prefix + fullMessage);
            }

            // Add to page for easier debugging if debug element exists
            const debugLogElement = document.getElementById('chart-debug-log');
            if (debugLogElement) {
                const logItem = document.createElement('div');
                logItem.className = isError ? 'error-log' : 'info-log';
                logItem.textContent = timestamp.substr(11, 8) + ': ' + fullMessage;
                debugLogElement.appendChild(logItem);
                // Keep only last 30 messages
                if (debugLogElement.children.length > 30) {
                    debugLogElement.removeChild(debugLogElement.children[0]);
                }
            }
        }
    }

    /**
     * Initialize all UI controls and event listeners
     */
    initializeControls() {
        // Date Range Picker
        const startDate = document.getElementById('startDate');
        const endDate = document.getElementById('endDate');

        // Set default date to today
        const today = new Date();

        startDate.value = today.toISOString().split('T')[0];
        endDate.value = today.toISOString().split('T')[0];

        // Event Listeners
        startDate.addEventListener('change', () => this.refreshData());
        endDate.addEventListener('change', () => this.refreshData());

        document.getElementById('viewType')
            .addEventListener('change', () => this.refreshData());

        document.getElementById('unitType')
            .addEventListener('change', () => this.updateUnits());

        document.getElementById('showAllFarms')
            .addEventListener('change', (e) => this.toggleAllFarms(e.target.checked));


        // Debug toggle
        const debugToggle = document.getElementById('showDebugLog');
        if (debugToggle) {
            debugToggle.checked = this.debugMode;
            debugToggle.addEventListener('change', (e) => {
                this.debugMode = e.target.checked;
                const debugLogContainer = document.getElementById('chart-debug-container');
                if (debugLogContainer) {
                    debugLogContainer.style.display = this.debugMode ? 'block' : 'none';
                }
                this.log(`Debug mode set to: ${this.debugMode}`);
            });

            // Initialize debug log visibility
            const debugLogContainer = document.getElementById('chart-debug-container');
            if (debugLogContainer) {
                debugLogContainer.style.display = this.debugMode ? 'block' : 'none';
            }
        }
    }

    /**
     * Load initial chart data from the server
     */
    async loadInitialData() {
        try {
            this.showLoading();
            this.log("Loading initial chart data");

            let data;

            try {
                // Get the selected company ID from the page-top data attribute
                const pageTopElement = document.getElementById('page-top');
                const companyId = pageTopElement ? pageTopElement.getAttribute('data-company-id') : null;
                this.log("Retrieved company ID from page-top element", companyId);

                if (!companyId || companyId === 'default' || companyId === 'null') {
                    this.log("Invalid company ID detected", companyId, true);
                    throw new Error("Invalid company ID: " + companyId);
                }

                // Use today's date
                const today = window.dateHelpers ? 
                    window.dateHelpers.getCurrentDateString() : 
                    new Date().toISOString().split('T')[0];

                this.log("Using today's date for data fetch", today);

                // Get CSRF token for POST request
                const csrfToken = document.querySelector('meta[name="_csrf"]')?.getAttribute('content');
                const csrfHeader = document.querySelector('meta[name="_csrf_header"]')?.getAttribute('content');
                this.log("CSRF token retrieved", csrfToken ? "Token found" : "Token missing");

                // Make API request to get data
                const apiUrl = `/api/charts/multi-farm/farm-volumes-for-date?company_id=${companyId}&date=${today}`;
                this.log("Making API request", apiUrl);

                const response = await fetch(apiUrl, {
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/json',
                        ...(csrfHeader && csrfToken ? { [csrfHeader]: csrfToken } : {})
                    }
                });

                this.log("API response status", response.status);

                if (!response.ok) {
                    this.log(`API request failed with status ${response.status}`, null, true);
                    throw new Error(`API request failed with status ${response.status}`);
                }

                const responseData = await response.json();
                this.log("API response data received", {
                    dataLength: responseData.data ? responseData.data.length : 0,
                    hasError: responseData.error || false
                });

                // Check for error flag in response
                if (responseData.error) {
                    this.log("API returned error", responseData.errorMessage, true);
                    throw new Error(responseData.errorMessage || "Unknown API error");
                }

                // Check if we have any data
                if (!responseData.data || responseData.data.length === 0) {
                    this.log("No data returned from API", null, true);
                    throw new Error("No data available for the selected date. Please try a different date.");
                }

                // Log the first data item for debugging
                this.log("First data item from API", responseData.data[0]);

                // Transform data to match the format expected by the chart
                data = {
                    farms: responseData.data.map(item => ({
                        id: item.farmId,
                        name: item.farmName,
                        volumes: [{
                            date: new Date(today),
                            value: item.volume
                        }]
                    }))
                };
                this.log(`Successfully processed data for ${data.farms.length} farms`);
            } catch (apiError) {
                this.log(`API error: ${apiError.message}`, apiError.stack, true);
                throw apiError; // Re-throw the error to be caught by the outer try-catch
            }

            this.currentData = data;
            this.log(`Initializing chart with ${data.farms.length} farms`, {
                farmNames: data.farms.map(f => f.name).join(', ')
            });

            this.initializeChart(data);
            this.initializeFarmToggles(data.farms);
            this.log("Chart initialization complete");

        } catch (error) {
            this.log(`Error in loadInitialData: ${error.message}`, error.stack, true);
            console.error('Error loading data:', error);
            this.showError(error);
        } finally {
            this.hideLoading();
        }
    }

    /**
     * Initialize the Highcharts chart with the provided data
     */
    initializeChart(data) {
        // Generate consistent colors for farms
        data.farms.forEach(farm => {
            if (!this.farmColors[farm.id]) {
                this.farmColors[farm.id] = this.getRandomColor();
            }
            farm.color = this.farmColors[farm.id];
        });

        // Create datasets from farm data
        const datasets = data.farms.map(farm => ({
            label: farm.name,
            data: farm.volumes.map(v => ({
                x: new Date(v.date),
                y: v.value
            })),
            borderColor: farm.color,
            backgroundColor: this.hexToRgba(farm.color, 0.1),
            borderWidth: 2,
            tension: 0.1,
            pointRadius: 3,
            pointHoverRadius: 5
        }));

        // Calculate total volume
        const totalVolume = data.farms.reduce((total, farm) => {
            return total + farm.volumes.reduce((farmTotal, v) => farmTotal + v.value, 0);
        }, 0);

        // Determine if we should use a line chart (for time series data) or bar chart
        const hasMultipleDates = this.hasMultipleDatesPerFarm(data);
        const chartType = hasMultipleDates || this.useFakeData ? 'line' : 'bar';

        this.log(`Using chart type: ${chartType} (multiple dates: ${hasMultipleDates}, fake data: ${this.useFakeData})`);

        // Create the chart with Highcharts
        this.chart = Highcharts.chart('dailyVolumeChart', {
            chart: {
                type: chartType,
                height: 400
            },
            title: {
                text: `Daily Production Volume: (${Math.round(totalVolume).toLocaleString()} MMBTUs)`,
                style: {
                    fontSize: '16px',
                    fontWeight: 'bold'
                }
            },
            xAxis: {
                type: hasMultipleDates || this.useFakeData ? 'datetime' : 'category',
                title: {
                    text: hasMultipleDates || this.useFakeData ? 'Date' : 'Farm'
                },
                labels: hasMultipleDates || this.useFakeData ? {
                    format: '{value:%b %d}'
                } : {}
            },
            yAxis: {
                min: 0,
                title: {
                    text: 'MMBTUs'
                },
                labels: {
                    formatter: function() {
                        return Highcharts.numberFormat(this.value, 0);
                    }
                }
            },
            tooltip: {
                formatter: function() {
                    return `<b>${this.series.name}</b><br/>
                            ${hasMultipleDates ? Highcharts.dateFormat('%b %d, %Y', this.x) : this.x}: 
                            ${Highcharts.numberFormat(this.y, 0)} MMBTUs`;
                }
            },
            series: datasets.map(ds => ({
                name: ds.label,
                data: hasMultipleDates || this.useFakeData ? 
                    ds.data.map(d => [d.x instanceof Date ? d.x.getTime() : d.x, d.y]) : 
                    ds.data.map(d => d.y),
                color: ds.borderColor
            })),
            legend: {
                enabled: true,
                align: 'center',
                verticalAlign: 'bottom'
            },
            credits: {
                enabled: false
            }
        });
    }

    /**
     * Initialize farm toggle buttons for showing/hiding farms
     */
    initializeFarmToggles(farms) {
        const container = document.getElementById('farmToggles');
        container.innerHTML = '';

        farms.forEach(farm => {
            const toggle = document.createElement('div');
            toggle.className = 'farm-toggle active';
            toggle.style.borderColor = farm.color;
            toggle.style.backgroundColor = this.hexToRgba(farm.color, 0.1);
            toggle.textContent = farm.name;
            toggle.dataset.farmId = farm.id;

            toggle.addEventListener('click', () => {
                toggle.classList.toggle('active');
                this.toggleFarmVisibility(farm.id, toggle.classList.contains('active'));
            });

            container.appendChild(toggle);
        });
    }

    /**
     * Toggle visibility of a specific farm in the chart
     */
    toggleFarmVisibility(farmId, visible) {
        const seriesIndex = this.chart.series.findIndex(
            series => series.name === this.currentData.farms.find(f => f.id === farmId).name
        );

        if (seriesIndex !== -1) {
            const series = this.chart.series[seriesIndex];
            if (visible) {
                series.show();
            } else {
                series.hide();
            }
        }
    }

    /**
     * Toggle visibility of all farms
     */
    toggleAllFarms(visible) {
        const toggles = document.querySelectorAll('.farm-toggle');

        toggles.forEach(toggle => {
            const isActive = visible;
            if (isActive) {
                toggle.classList.add('active');
            } else {
                toggle.classList.remove('active');
            }

            const farmId = toggle.dataset.farmId;
            this.toggleFarmVisibility(farmId, isActive);
        });
    }

    /**
     * Update the chart units (MMBTUs/liters)
     */
    updateUnits() {
        const unitType = document.getElementById('unitType').value;
        const conversionFactor = unitType === 'liters' ? 3.78541 : 1; // 1 gallon = 3.78541 liters
        const unitLabel = unitType === 'liters' ? 'Liters' : 'MMBTUs';

        // Update y-axis label
        this.chart.options.scales.y.title.text = unitLabel;

        // Update tooltip
        this.chart.options.plugins.tooltip.callbacks.label = (context) => {
            const value = context.parsed.y * (unitType === 'liters' ? conversionFactor : 1);
            return `${context.dataset.label}: ${new Intl.NumberFormat().format(value)} ${unitLabel.toLowerCase()}`;
        };

        // Update chart title
        const totalVolume = this.calculateTotalVolume() * (unitType === 'liters' ? conversionFactor : 1);
        this.chart.options.plugins.title.text = 
            `Daily Production Volume: (${Math.round(totalVolume).toLocaleString()} ${unitLabel})`;

        this.chart.update();
    }

    /**
     * Calculate the total volume across all farms
     */
    calculateTotalVolume() {
        let total = 0;
        this.currentData.farms.forEach(farm => {
            farm.volumes.forEach(v => {
                total += v.value;
            });
        });
        return total;
    }

    /**
     * Refresh data based on current filter settings
     */
    async refreshData() {
        try {
            this.showLoading();
            this.log("Refreshing chart data");

            let data;

            try {
                // Get the selected company ID from the page-top data attribute
                const pageTopElement = document.getElementById('page-top');
                const companyId = pageTopElement ? pageTopElement.getAttribute('data-company-id') : null;
                this.log("Retrieved company ID from page-top element", companyId);

                if (!companyId || companyId === 'default' || companyId === 'null') {
                    this.log("Invalid company ID detected", companyId, true);
                    throw new Error("Invalid company ID: " + companyId);
                }

                // Use today's date
                const today = window.dateHelpers ? 
                    window.dateHelpers.getCurrentDateString() : 
                    new Date().toISOString().split('T')[0];

                this.log("Using today's date for data fetch", today);

                // Get CSRF token for POST request
                const csrfToken = document.querySelector('meta[name="_csrf"]')?.getAttribute('content');
                const csrfHeader = document.querySelector('meta[name="_csrf_header"]')?.getAttribute('content');
                this.log("CSRF token retrieved", csrfToken ? "Token found" : "Token missing");

                // Make API request to get data
                const apiUrl = `/api/charts/multi-farm/farm-volumes-for-date?company_id=${companyId}&date=${today}`;
                this.log("Making API request", apiUrl);

                const response = await fetch(apiUrl, {
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/json',
                        ...(csrfHeader && csrfToken ? { [csrfHeader]: csrfToken } : {})
                    }
                });

                this.log("API response status", response.status);

                if (!response.ok) {
                    this.log(`API request failed with status ${response.status}`, null, true);
                    throw new Error(`API request failed with status ${response.status}`);
                }

                const responseData = await response.json();
                this.log("API response data received", {
                    dataLength: responseData.data ? responseData.data.length : 0,
                    hasError: responseData.error || false
                });

                // Check for error flag in response
                if (responseData.error) {
                    this.log("API returned error", responseData.errorMessage, true);
                    throw new Error(responseData.errorMessage || "Unknown API error");
                }

                // Check if we have any data
                if (!responseData.data || responseData.data.length === 0) {
                    this.log("No data returned from API", null, true);
                    throw new Error("No data available for the selected date. Please try a different date.");
                }

                // Log the first data item for debugging
                this.log("First data item from API", responseData.data[0]);

                // Transform data to match the format expected by the chart
                data = {
                    farms: responseData.data.map(item => ({
                        id: item.farmId,
                        name: item.farmName,
                        volumes: [{
                            date: new Date(today),
                            value: item.volume
                        }]
                    }))
                };
                this.log(`Successfully processed data for ${data.farms.length} farms`);
            } catch (apiError) {
                this.log(`API error: ${apiError.message}`, apiError.stack, true);
                throw apiError; // Re-throw the error to be caught by the outer try-catch
            }

            this.currentData = data;

            // Update chart data
            this.log("Updating chart with new data");
            this.chart.data.datasets = data.farms.map(farm => {
                if (!this.farmColors[farm.id]) {
                    this.farmColors[farm.id] = this.getRandomColor();
                }
                farm.color = this.farmColors[farm.id];

                return {
                    label: farm.name,
                    data: farm.volumes.map(v => ({
                        x: new Date(v.date),
                        y: v.value
                    })),
                    borderColor: farm.color,
                    backgroundColor: this.hexToRgba(farm.color, 0.1),
                    borderWidth: 2,
                    tension: 0.1,
                    pointRadius: 3,
                    pointHoverRadius: 5
                };
            });

            // Check if we need to update chart type based on data
            const hasMultipleDates = this.hasMultipleDatesPerFarm(data);
            const shouldBeLineChart = hasMultipleDates;

            // Update chart type if needed
            if ((shouldBeLineChart && this.chart.config.type !== 'line') || 
                (!shouldBeLineChart && this.chart.config.type !== 'bar')) {

                const newType = shouldBeLineChart ? 'line' : 'bar';
                this.log(`Changing chart type from ${this.chart.config.type} to ${newType}`);
                this.chart.config.type = newType;

                // Update x-axis configuration
                this.chart.options.scales.x.type = shouldBeLineChart ? 'time' : 'category';
                this.chart.options.scales.x.time = shouldBeLineChart ? {
                    unit: 'day',
                    displayFormats: {
                        day: 'MMM d'
                    },
                    tooltipFormat: 'MMM d, yyyy'
                } : undefined;
                this.chart.options.scales.x.title.text = shouldBeLineChart ? 'Date' : 'Farm';
            }

            // Update chart title
            const totalVolume = this.calculateTotalVolume();
            const unitType = document.getElementById('unitType').value;
            const unitLabel = unitType === 'liters' ? 'Liters' : 'MMBTUs';
            const conversionFactor = unitType === 'liters' ? 3.78541 : 1;

            this.chart.options.plugins.title.text = 
                `Daily Production Volume: (${Math.round(totalVolume * conversionFactor).toLocaleString()} ${unitLabel})`;

            // Update farm toggles
            this.initializeFarmToggles(data.farms);

            this.chart.update();
            this.log("Chart refresh complete");
        } catch (error) {
            this.log(`Error in refreshData: ${error.message}`, error.stack, true);
            console.error('Error refreshing data:', error);
            this.showError(error);
        } finally {
            this.hideLoading();
        }
    }

    /**
     * Show loading state
     */
    showLoading() {
        document.querySelector('.chart-loading-state').classList.remove('d-none');
    }

    /**
     * Hide loading state
     */
    hideLoading() {
        document.querySelector('.chart-loading-state').classList.add('d-none');
    }

    /**
     * Show error state
     */
    showError(error) {
        // Log the error with detailed information
        this.log(`Chart error: ${error.message || 'Unknown error'}`, error.stack, true);

        // Show error message in the UI
        const errorElement = document.querySelector('.chart-error-state');
        errorElement.classList.remove('d-none');

        // Update error message with more details
        const errorMessage = `Error loading chart data: ${error.message || 'Unknown error'}. 
            Please try a different date or check your connection.`;

        errorElement.querySelector('.alert').innerHTML = errorMessage;

        // Make sure debug log is visible when there's an error
        const debugToggle = document.getElementById('showDebugLog');
        if (debugToggle && !this.debugMode) {
            debugToggle.click(); // Enable debug mode to show logs
            this.log("Automatically enabled debug log due to error", true);
        }
    }


    /**
     * Check if the data contains multiple dates per farm
     * Used to determine whether to use a line chart with time-based x-axis
     */
    hasMultipleDatesPerFarm(data) {
        if (!data || !data.farms || data.farms.length === 0) {
            return false;
        }

        // Check if any farm has more than one volume entry
        return data.farms.some(farm => farm.volumes && farm.volumes.length > 1);
    }

    /**
     * Generate a random color for a farm
     */
    getRandomColor() {
        const letters = '0123456789ABCDEF';
        let color = '#';
        for (let i = 0; i < 6; i++) {
            color += letters[Math.floor(Math.random() * 16)];
        }
        return color;
    }

    /**
     * Convert hex color to rgba for transparency
     */
    hexToRgba(hex, alpha) {
        const r = parseInt(hex.slice(1, 3), 16);
        const g = parseInt(hex.slice(3, 5), 16);
        const b = parseInt(hex.slice(5, 7), 16);
        return `rgba(${r}, ${g}, ${b}, ${alpha})`;
    }
}

// Initialize the controller when the DOM is fully loaded
document.addEventListener('DOMContentLoaded', () => {
    new DailyVolumeController();
});
