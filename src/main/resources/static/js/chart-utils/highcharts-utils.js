/**
 * Highcharts Utility Functions
 * Provides common functionality for Highcharts charts
 */

/**
 * Format numbers with thousands separators
 * @param {number} number - The number to format
 * @param {number} decimals - Number of decimal places
 * @param {string} decPoint - Decimal point character
 * @param {string} thousandsSep - Thousands separator character
 * @returns {string} Formatted number
 */
function formatNumber(number, decimals = 0, decPoint = '.', thousandsSep = ',') {
    const n = !isFinite(+number) ? 0 : +number;
    const prec = !isFinite(+decimals) ? 0 : Math.abs(decimals);

    // Fix for IE parseFloat(0.55).toFixed(0) = 0;
    const toFixedFix = function(n, prec) {
        const k = Math.pow(10, prec);
        return '' + Math.round(n * k) / k;
    };

    // Format the number
    let s = (prec ? toFixedFix(n, prec) : '' + Math.round(n)).split('.');
    if (s[0].length > 3) {
        s[0] = s[0].replace(/\B(?=(?:\d{3})+(?!\d))/g, thousandsSep);
    }
    if ((s[1] || '').length < prec) {
        s[1] = s[1] || '';
        s[1] += new Array(prec - s[1].length + 1).join('0');
    }
    return s.join(decPoint);
}
/**
 * Highcharts utility functions for the application
 */

// Common Highcharts theme for consistent styling
const highchartsTheme = {
    colors: ['#4e73df', '#1cc88a', '#36b9cc', '#f6c23e', '#e74a3b', '#858796'],
    chart: {
        backgroundColor: '#fff',
        style: {
            fontFamily: '"Nunito", -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, "Helvetica Neue", Arial, sans-serif',
            fontSize: '12px'
        }
    },
    title: {
        style: {
            fontSize: '14px',
            fontWeight: 'bold',
            color: '#5a5c69'
        }
    },
    subtitle: {
        style: {
            fontSize: '12px',
            color: '#858796'
        }
    },
    xAxis: {
        gridLineWidth: 0,
        gridLineColor: '#e3e6f0',
        lineColor: '#e3e6f0',
        tickColor: '#e3e6f0',
        labels: {
            style: {
                color: '#858796',
                fontSize: '10px'
            }
        },
        title: {
            style: {
                color: '#858796',
                fontSize: '12px'
            }
        }
    },
    yAxis: {
        gridLineColor: '#e3e6f0',
        lineColor: '#e3e6f0',
        tickColor: '#e3e6f0',
        tickWidth: 1,
        labels: {
            style: {
                color: '#858796',
                fontSize: '10px'
            }
        },
        title: {
            style: {
                color: '#858796',
                fontSize: '12px'
            }
        }
    },
    tooltip: {
        backgroundColor: 'rgba(255, 255, 255, 0.95)',
        borderColor: '#e3e6f0',
        style: {
            color: '#5a5c69'
        }
    },
    plotOptions: {
        series: {
            fillOpacity: 0.1,
            lineWidth: 2,
            marker: {
                lineWidth: 1,
                lineColor: '#ffffff'
            }
        },
        column: {
            borderRadius: 2
        }
    },
    legend: {
        itemStyle: {
            fontWeight: 'normal',
            color: '#5a5c69'
        },
        itemHoverStyle: {
            color: '#000000'
        }
    },
    credits: {
        enabled: false
    },
    exporting: {
        enabled: true,
        buttons: {
            contextButton: {
                symbolStroke: '#858796',
                theme: {
                    fill: 'transparent'
                },
                menuItems: [
                    'viewFullscreen',
                    'printChart',
                    'separator',
                    'downloadPNG',
                    'downloadJPEG',
                    'downloadPDF',
                    'downloadSVG',
                    'separator',
                    'downloadCSV',
                    'downloadXLS',
                    'viewData'
                ]
            }
        },
        // Enhanced filename handling matching Next.js implementation
        filename: function(chart) {
            const title = chart.title ? chart.title.textStr : 'chart-data';
            return title.replace(/[^a-zA-Z0-9\s-]/g, '').replace(/\s+/g, '-').toLowerCase();
        }
    }
};

// Apply theme globally
Highcharts.setOptions(highchartsTheme);

// Add global error handling for Highcharts
if (typeof Highcharts !== 'undefined') {
    // Override Highcharts error handling to prevent .replace errors
    const originalError = Highcharts.error;
    Highcharts.error = function(code, stop) {
        console.warn('Highcharts error:', code);
        
        // Handle specific exporting errors
        if (code === 16) { // Export server error
            console.warn('Highcharts export server error - falling back to client-side export');
            return false; // Don't stop execution
        }
        
        // For other errors, log but don't stop
        if (stop) {
            console.error('Highcharts error (stopping):', code);
        }
        return false;
    };
    
    // Override the getFilename function to prevent .replace errors
    if (Highcharts.exporting && Highcharts.exporting.Exporter) {
        const originalGetFilename = Highcharts.exporting.Exporter.prototype.getFilename;
        if (originalGetFilename) {
            Highcharts.exporting.Exporter.prototype.getFilename = function() {
                try {
                    const filename = originalGetFilename.call(this);
                    return typeof filename === 'string' ? filename : 'chart-export';
                } catch (error) {
                    console.warn('Error getting filename, using default:', error);
                    return 'chart-export';
                }
            };
        }
    }
    
    console.log('✅ Highcharts error handling initialized');
}

/**
 * Initialize Highcharts with error handling for exporting
 */
function initializeHighchartsWithErrorHandling() {
    // Override Highcharts error handling for exporting
    if (typeof Highcharts !== 'undefined') {
        // Add error handling for exporting
        Highcharts.error = function(code, stop) {
            console.warn('Highcharts error:', code);
            
            // Handle specific exporting errors
            if (code === 16) { // Export server error
                console.warn('Highcharts export server error - falling back to client-side export');
                return false; // Don't stop execution
            }
            
            // For other errors, log but don't stop
            if (stop) {
                console.error('Highcharts error (stopping):', code);
            }
            return false;
        };
        
        // Override the getFilename function to prevent .replace errors
        if (Highcharts.exporting && Highcharts.exporting.Exporter) {
            const originalGetFilename = Highcharts.exporting.Exporter.prototype.getFilename;
            if (originalGetFilename) {
                Highcharts.exporting.Exporter.prototype.getFilename = function() {
                    try {
                        const filename = originalGetFilename.call(this);
                        return typeof filename === 'string' ? filename : 'chart-export';
                    } catch (error) {
                        console.warn('Error getting filename, using default:', error);
                        return 'chart-export';
                    }
                };
            }
        }
        
        console.log('✅ Highcharts error handling initialized');
    }
}

/**
 * Create a chart with enhanced error handling
 * @param {string} containerId - Chart container ID
 * @param {Object} options - Chart options
 * @returns {Object} Highcharts chart instance
 */
function createChartWithErrorHandling(containerId, options) {
    try {
        // Initialize error handling
        initializeHighchartsWithErrorHandling();
        
        // Verify container exists
        const container = document.getElementById(containerId);
        if (!container) {
            throw new Error(`Chart container with ID '${containerId}' not found`);
        }
        
        // Create the chart
        const chart = Highcharts.chart(containerId, options);
        
        // Verify chart was created successfully
        if (!chart || typeof chart !== 'object') {
            throw new Error('Chart creation failed - invalid chart object returned');
        }
        
        // Add error event listener only if the chart has the 'on' method
        if (chart && typeof chart.on === 'function') {
            chart.on('error', function(e) {
                console.warn('Chart error:', e);
            });
        } else {
            console.warn('⚠️ Chart object does not have "on" method, skipping error listener');
        }
        
        console.log('✅ Chart created successfully with error handling');
        return chart;
    } catch (error) {
        console.error('❌ Error creating chart:', error);
        throw error;
    }
}

/**
 * Create a line chart showing daily production trends
 * @param {string} containerId - The ID of the container element
 * @param {Array} data - Array of data points [timestamp, value]
 * @param {string} title - Chart title
 */
function createProductionTrendChart(containerId, data, title = 'Daily Production Trend') {
    return Highcharts.chart(containerId, {
        chart: {
            type: 'line',
            height: 300
        },
        title: {
            text: title
        },
        xAxis: {
            type: 'datetime',
            title: {
                text: 'Date'
            },
            labels: {
                format: '{value:%b %d}'
            }
        },
        yAxis: {
            min: 0,
            title: {
                text: 'Production (MMBTUs)'
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
                        ${Highcharts.dateFormat('%A, %b %d', this.x)}: ${Highcharts.numberFormat(this.y, 2)} MMBTUs`;
            }
        },
        legend: {
            enabled: true,
            align: 'center',
            verticalAlign: 'bottom'
        },
        series: [{
            name: 'Daily Production',
            data: data,
            color: '#4e73df'
        }]
    });
}

/**
 * Create a pie chart showing distribution of production across farms
 * @param {string} containerId - The ID of the container element
 * @param {Array} data - Array of {name, y} objects
 * @param {string} title - Chart title
 */
function createFarmDistributionChart(containerId, data, title = 'Farm Distribution') {
    return Highcharts.chart(containerId, {
        chart: {
            type: 'pie',
            height: 300
        },
        title: {
            text: title
        },
        tooltip: {
            pointFormat: '{series.name}: <b>{point.percentage:.1f}%</b>'
        },
        accessibility: {
            point: {
                valueSuffix: '%'
            }
        },
        plotOptions: {
            pie: {
                allowPointSelect: true,
                cursor: 'pointer',
                dataLabels: {
                    enabled: true,
                    format: '<b>{point.name}</b>: {point.percentage:.1f} %',
                    style: {
                        color: '#5a5c69'
                    }
                }
            }
        },
        series: [{
            name: 'Production',
            colorByPoint: true,
            data: data
        }]
    });
}

/**
 * Create a column chart showing comparison data
 * @param {string} containerId - The ID of the container element
 * @param {Array} categories - Array of category names
 * @param {Array} series - Array of series objects
 * @param {string} title - Chart title
 */
function createComparisonChart(containerId, categories, series, title = 'Production Comparison') {
    return Highcharts.chart(containerId, {
        chart: {
            type: 'column',
            height: 300
        },
        title: {
            text: title
        },
        xAxis: {
            categories: categories,
            crosshair: true
        },
        yAxis: {
            min: 0,
            title: {
                text: 'Production (MMBTUs)'
            },
            labels: {
                formatter: function() {
                    return Highcharts.numberFormat(this.value, 0);
                }
            }
        },
        tooltip: {
            headerFormat: '<span style="font-size:10px">{point.key}</span><table>',
            pointFormat: '<tr><td style="color:{series.color};padding:0">{series.name}: </td>' +
                '<td style="padding:0"><b>{point.y:.2f} MMBTUs</b></td></tr>',
            footerFormat: '</table>',
            shared: true,
            useHTML: true
        },
        plotOptions: {
            column: {
                pointPadding: 0.2,
                borderWidth: 0
            }
        },
        series: series
    });
}

/**
 * Generate sample data for charts when real data is not available
 * @param {number} days - Number of days to generate data for
 * @param {number} min - Minimum value
 * @param {number} max - Maximum value
 * @returns {Array} Array of [timestamp, value] pairs
 */
function generateSampleTimeSeriesData(days = 7, min = 10, max = 40) {
    const data = [];
    const now = new Date();

    for (let i = days - 1; i >= 0; i--) {
        const date = new Date();
        date.setDate(date.getDate() - i);
        data.push([
            date.getTime(),
            Math.floor(Math.random() * (max - min + 1)) + min
        ]);
    }

    return data;
}

/**
 * Generate sample farm distribution data
 * @param {Array} farmNames - Array of farm names
 * @returns {Array} Array of {name, y} objects
 */
function generateSampleFarmDistribution(farmNames = ['Farm A', 'Farm B', 'Farm C', 'Farm D']) {
    return farmNames.map(name => {
        return {
            name: name,
            y: Math.floor(Math.random() * 30) + 10
        };
    });
}
/**
 * Convert hex color to rgba
 * @param {string} hex - Hex color code
 * @param {number} alpha - Alpha value (0-1)
 * @returns {string} RGBA color string
 */
function hexToRgba(hex, alpha) {
    if (!hex) return 'rgba(0, 0, 0, ' + alpha + ')';

    // Remove # if present
    hex = hex.replace('#', '');

    // Convert 3-digit hex to 6-digit
    if (hex.length === 3) {
        hex = hex[0] + hex[0] + hex[1] + hex[1] + hex[2] + hex[2];
    }

    // Parse the hex values
    const r = parseInt(hex.substring(0, 2), 16);
    const g = parseInt(hex.substring(2, 4), 16);
    const b = parseInt(hex.substring(4, 6), 16);

    return 'rgba(' + r + ', ' + g + ', ' + b + ', ' + alpha + ')';
}

/**
 * Generate a random color
 * @returns {string} Hex color code
 */
function getRandomColor() {
    const letters = '0123456789ABCDEF';
    let color = '#';
    for (let i = 0; i < 6; i++) {
        color += letters[Math.floor(Math.random() * 16)];
    }
    return color;
}

/**
 * Create a Highcharts tooltip formatter
 * @param {string} unit - Unit to display (e.g., 'MMBTUs')
 * @returns {Function} Tooltip formatter function
 */
function createTooltipFormatter(unit) {
    const safeUnit = String(unit || '');
    
    return function() {
        const seriesName = this.series && this.series.name ? this.series.name : 'Series';
        const key = this.key || 'Unknown';
        const value = typeof this.y === 'number' && !isNaN(this.y) ? this.y : 0;
        
        return `<b>${seriesName}</b><br/>
                ${key}: ${Highcharts.numberFormat(value, 0)} ${safeUnit}`;
    };
}

/**
 * Create a Highcharts title with total
 * @param {string} title - Chart title
 * @param {number} total - Total value to display
 * @param {string} unit - Unit to display (e.g., 'MMBTUs')
 * @returns {Object} Highcharts title configuration
 */
function createTitleWithTotal(title, total, unit) {
    // Validate and sanitize inputs
    const safeTitle = String(title || 'Chart');
    const safeTotal = typeof total === 'number' && !isNaN(total) ? total : 0;
    const safeUnit = String(unit || '');
    
    // Format the total with proper number formatting
    const formattedTotal = Math.round(safeTotal).toLocaleString();
    
    return {
        text: `${safeTitle}: (${formattedTotal} ${safeUnit})`,
        style: {
            fontSize: '16px',
            fontWeight: 'bold'
        },
        margin: 20
    };
}

/**
 * Safely format chart data for Highcharts
 * @param {Array} data - Raw data array
 * @param {string} nameKey - Key for the name property
 * @param {string} valueKey - Key for the value property
 * @returns {Array} Formatted data array for Highcharts
 */
function safelyFormatChartData(data, nameKey = 'name', valueKey = 'value') {
    if (!Array.isArray(data)) {
        console.warn('⚠️ Data is not an array:', data);
        return [];
    }
    
    return data
        .filter(item => item && typeof item === 'object')
        .map(item => {
            const name = item[nameKey] || item.farm_name || item.farm_id || 'Unknown';
            const value = parseFloat(item[valueKey] || item.volume || 0);
            
            return [
                String(name),
                isNaN(value) ? 0 : value
            ];
        })
        .filter(([name, value]) => name && value >= 0);
}

/**
 * Create a safe Highcharts chart configuration
 * @param {string} containerId - Chart container ID
 * @param {Object} options - Chart options
 * @returns {Object} Safe Highcharts configuration
 */
function createSafeChartConfig(containerId, options = {}) {
    const defaultConfig = {
        chart: {
            type: 'column',
            height: 400
        },
        title: {
            text: 'Chart',
            style: {
                fontSize: '16px',
                fontWeight: 'bold'
            },
            margin: 20
        },
        xAxis: {
            type: 'category',
            title: {
                text: 'Category'
            }
        },
        yAxis: {
            min: 0,
            title: {
                text: 'Value'
            },
            labels: {
                formatter: function() {
                    return Highcharts.numberFormat(this.value, 0);
                }
            }
        },
        tooltip: {
            formatter: function() {
                const seriesName = this.series && this.series.name ? this.series.name : 'Series';
                const key = this.key || 'Unknown';
                const value = typeof this.y === 'number' && !isNaN(this.y) ? this.y : 0;
                
                return `<b>${seriesName}</b><br/>
                        ${key}: ${Highcharts.numberFormat(value, 0)}`;
            }
        },
        legend: {
            enabled: false
        },
        series: [],
        exporting: {
            enabled: true,
            filename: 'chart-export',
            buttons: {
                contextButton: {
                    menuItems: ['downloadPNG', 'downloadPDF', 'downloadCSV', 'downloadXLS']
                }
            }
        }
    };
    
    // Merge options with defaults
    return Object.assign({}, defaultConfig, options);
}
