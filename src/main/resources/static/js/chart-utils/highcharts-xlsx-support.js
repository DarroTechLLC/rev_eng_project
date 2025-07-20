/**
 * Enhanced XLSX Export Support for Highcharts
 * Provides proper Excel file export with formatting
 * Matches Next.js implementation functionality
 */

// Load XLSX library if not already loaded
function loadXLSXLibrary() {
    return new Promise((resolve, reject) => {
        if (window.XLSX) {
            resolve(window.XLSX);
            return;
        }

        const script = document.createElement('script');
        script.src = 'https://cdnjs.cloudflare.com/ajax/libs/xlsx/0.18.5/xlsx.full.min.js';
        script.onload = () => resolve(window.XLSX);
        script.onerror = reject;
        document.head.appendChild(script);
    });
}

// Load FileSaver library if not already loaded
function loadFileSaverLibrary() {
    return new Promise((resolve, reject) => {
        if (window.saveAs) {
            resolve(window.saveAs);
            return;
        }

        const script = document.createElement('script');
        script.src = 'https://cdnjs.cloudflare.com/ajax/libs/FileSaver.js/2.0.5/FileSaver.min.js';
        script.onload = () => resolve(window.saveAs);
        script.onerror = reject;
        document.head.appendChild(script);
    });
}

/**
 * Export chart to XLSX with proper formatting
 * @param {Object} chart - Highcharts chart instance
 * @param {string} fileName - Optional filename without extension
 */
async function exportChartToXLSX(chart, fileName) {
    try {
        console.log('📊 Starting XLSX export...'); // Debug log
        
        // Load required libraries
        const [XLSX, saveAs] = await Promise.all([
            loadXLSXLibrary(),
            loadFileSaverLibrary()
        ]);

        // Get default filename from chart title (matching Next.js logic)
        const defaultFileName = fileName || (chart.title ? chart.title.textStr : 'chart-data') || 'chart-data';
        console.log('📝 Using filename:', defaultFileName); // Debug log

        // Get chart data in tabular format
        const rows = chart.getDataRows();
        if (!rows || rows.length === 0) {
            console.error('❌ No data available for export');
            return;
        }

        const headers = rows[0];
        console.log('📋 Headers:', headers); // Debug log

        // Create data array for XLSX (matching Next.js implementation)
        const data = rows.slice(1).map(row => {
            const obj = {};
            headers.forEach((header, i) => {
                obj[header] = row[i];
            });
            return obj;
        });

        console.log('📊 Data rows:', data.length); // Debug log

        // Create worksheet
        const worksheet = XLSX.utils.json_to_sheet(data);

        // Add headers with specific styling (matching Next.js)
        XLSX.utils.sheet_add_aoa(worksheet, [headers], { origin: 'A1' });

        // Create workbook and append worksheet
        const workbook = XLSX.utils.book_new();
        XLSX.utils.book_append_sheet(workbook, worksheet, 'Chart Data');

        // Generate buffer
        const excelBuffer = XLSX.write(workbook, {
            bookType: 'xlsx',
            type: 'array'
        });

        // Create and save file
        const blob = new Blob([excelBuffer], {
            type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet'
        });
        
        saveAs(blob, `${defaultFileName}.xlsx`);
        
        console.log('✅ XLSX file exported successfully:', `${defaultFileName}.xlsx`);
    } catch (error) {
        console.error('❌ Error exporting XLSX:', error);
    }
}

/**
 * Setup custom XLSX export for Highcharts
 * Extends Highcharts Chart prototype with custom downloadXLSX method
 */
function setupHighchartsXLSXExport() {
    if (typeof Highcharts === 'undefined') {
        console.error('❌ Highcharts not loaded');
        return;
    }

    // Fix Highcharts filename issues by overriding the getFilename function
    if (Highcharts.exporting && Highcharts.exporting.Exporter) {
        const originalGetFilename = Highcharts.exporting.Exporter.prototype.getFilename;
        if (originalGetFilename) {
            Highcharts.exporting.Exporter.prototype.getFilename = function() {
                try {
                    const filename = originalGetFilename.call(this);
                    // Ensure filename is a string and handle undefined/null cases
                    if (typeof filename === 'string' && filename.trim()) {
                        return filename;
                    } else {
                        // Fallback to chart title or default
                        const chartTitle = this.chart && this.chart.title ? this.chart.title.textStr : null;
                        return chartTitle || 'chart-export';
                    }
                } catch (error) {
                    console.warn('⚠️ Error in getFilename, using fallback:', error);
                    return 'chart-export';
                }
            };
        }
    }

    // Extend Highcharts Chart prototype with custom XLSX method (matching Next.js)
    Highcharts.Chart.prototype.downloadXLSX = function(fileName) {
        console.log('🔧 Custom XLSX export called'); // Debug log
        exportChartToXLSX(this, fileName);
    };

    console.log('✅ Highcharts XLSX export setup complete');
}

// Initialize when DOM is ready
if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', setupHighchartsXLSXExport);
} else {
    setupHighchartsXLSXExport();
} 