/**
 * Chart Export Menu - Enhanced functionality matching Next.js implementation
 * Provides custom file naming and formatting options
 */

// XLSX Export Function (matching NextJS implementation)
function exportChartToXLSX(chart, fileName) {
    // Get the default filename from chart title if not provided
    const defaultFileName = fileName || (chart.title && chart.title.textStr) || 'chart-data';

    // Get the chart data in tabular format
    const rows = chart.getDataRows();
    const headers = rows[0];

    // Create data array for XLSX
    // Skip row 0 as it contains headers
    const data = rows.slice(1).map(row => {
        // Create an object where keys are headers and values from the row
        return headers.reduce((obj, header, i) => {
            obj[header] = row[i];
            return obj;
        }, {});
    });

    // Create worksheet
    const worksheet = XLSX.utils.json_to_sheet(data);

    // Add headers with specific styling
    XLSX.utils.sheet_add_aoa(worksheet, [headers], { origin: 'A1' });

    // Create workbook and append worksheet
    const workbook = XLSX.utils.book_new();
    XLSX.utils.book_append_sheet(workbook, worksheet, 'Chart Data');

    // Generate buffer
    const excelBuffer = XLSX.write(workbook, {
        bookType: 'xlsx',
        type: 'array'
    });

    // Create and save the file
    const blob = new Blob([excelBuffer], {
        type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet'
    });
    saveAs(blob, `${defaultFileName}.xlsx`);
}

class ChartExportMenu {
    constructor(chart, container) {
        this.chart = chart;
        this.container = container;
        this.showOptions = false;
        this.init();
    }

    init() {
        // Create export button
        this.createExportButton();
        // Create dropdown menu
        this.createExportMenu();
        // Add event listeners
        this.addEventListeners();
    }

    createExportButton() {
        const button = document.createElement('div');
        button.className = 'chart-export-button';
        button.innerHTML = '<img src="/svg/download.svg" alt="Download" style="width: 24px; height: 24px;">';
        button.style.cssText = `
            cursor: pointer;
            border-radius: 6px;
            padding: 4px;
            position: absolute;
            top: 10px;
            right: 10px;
            z-index: 1002;
            background: rgba(255, 255, 255, 0.95);
            box-shadow: 0 2px 8px rgba(0,0,0,0.08);
            transition: background-color 0.2s ease, box-shadow 0.2s ease;
        `;
        button.setAttribute('tabindex', '0');
        // Responsive adjustment via CSS class
        if (!document.getElementById('chart-export-menu-style')) {
            const style = document.createElement('style');
            style.id = 'chart-export-menu-style';
            style.innerHTML = `
                .chart-export-button { box-sizing: border-box; }
                @media (max-width: 600px) {
                    .chart-export-button {
                        top: 24px !important;
                        right: 10px !important;
                        padding: 2px !important;
                    }
                    .chart-export-button img {
                        width: 18px !important;
                        height: 18px !important;
                    }
                }
            `;
            document.head.appendChild(style);
        }
        button.addEventListener('mouseenter', () => {
            button.style.backgroundColor = '#eee';
            button.style.boxShadow = '0 4px 16px rgba(0,0,0,0.12)';
        });
        button.addEventListener('mouseleave', () => {
            button.style.backgroundColor = 'rgba(255, 255, 255, 0.95)';
            button.style.boxShadow = '0 2px 8px rgba(0,0,0,0.08)';
        });
        this.exportButton = button;
        this.container.appendChild(button);
    }

    createExportMenu() {
        const menu = document.createElement('div');
        menu.className = 'chart-export-menu';
        menu.style.cssText = `
            position: absolute;
            top: 40px;
            right: 10px;
            background: white;
            border: 1px solid #ddd;
            border-radius: 6px;
            box-shadow: 0 2px 10px rgba(0,0,0,0.1);
            z-index: 1001;
            display: none;
            min-width: 200px;
            max-width: 250px;
            font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif;
            overflow: hidden; /* Prevent content from overflowing */
        `;

        const menuItems = [
            { label: 'Download PNG Image', action: 'png' },
            { label: 'Download JPEG Image', action: 'jpg' },
            { label: 'Download SVG Image', action: 'svg' },
            { separator: true },
            { label: 'Download PDF Document', action: 'pdf' },
            { label: 'Download CSV Document', action: 'csv' },
            { label: 'Download XLSX Document', action: 'xlsx' }
        ];

        menuItems.forEach(item => {
            if (item.separator) {
                const separator = document.createElement('hr');
                separator.style.cssText = `
                    margin: 4px 0;
                    border: none;
                    border-top: 1px solid #eee;
                `;
                menu.appendChild(separator);
            } else {
                const menuItem = document.createElement('div');
                menuItem.className = 'chart-export-menu-item';
                menuItem.textContent = item.label;
                menuItem.style.cssText = `
                    cursor: pointer;
                    padding: 12px 20px;
                    border: solid 1px #fff;
                    color: #444;
                    transition: background-color 0.2s ease, border-color 0.2s ease, color 0.2s ease;
                `;
                menuItem.addEventListener('mouseenter', () => {
                    menuItem.style.backgroundColor = '#f9f9f9';
                    menuItem.style.borderColor = '#ddd';
                    menuItem.style.color = '#000';
                });
                menuItem.addEventListener('mouseleave', () => {
                    menuItem.style.backgroundColor = '#fff';
                    menuItem.style.borderColor = '#fff';
                    menuItem.style.color = '#444';
                });
                menuItem.addEventListener('click', () => {
                    this.exportChart(item.action);
                    this.hideMenu();
                });
                menu.appendChild(menuItem);
            }
        });

        this.exportMenu = menu;
        this.container.appendChild(menu);
    }

    addEventListeners() {
        // Toggle menu on button click
        this.exportButton.addEventListener('click', (e) => {
            e.stopPropagation();
            this.toggleMenu();
        });

        // Close menu when clicking outside
        document.addEventListener('click', (e) => {
            if (!this.container.contains(e.target)) {
                this.hideMenu();
            }
        });

        // Close menu on escape key
        document.addEventListener('keydown', (e) => {
            if (e.key === 'Escape') {
                this.hideMenu();
            }
        });
    }

    toggleMenu() {
        if (this.showOptions) {
            this.hideMenu();
        } else {
            this.showMenu();
        }
    }

    showMenu() {
        this.exportMenu.style.display = 'block';
        this.showOptions = true;
    }

    hideMenu() {
        this.exportMenu.style.display = 'none';
        this.showOptions = false;
    }

    getDefaultFileName() {
        // Get filename from chart title or use default
        const title = this.chart.title && this.chart.title.textStr;
        if (title) {
            // Clean the title for use as filename
            return title.replace(/[^a-zA-Z0-9\s-]/g, '').replace(/\s+/g, '-').toLowerCase();
        }
        return 'chart-data';
    }

    exportChart(type) {
        try {
            switch (type) {
                case 'png':
                    this.chart.exportChartLocal({ type: 'image/png' });
                    break;
                case 'jpg':
                    this.chart.exportChartLocal({ type: 'image/jpeg' });
                    break;
                case 'svg':
                    this.chart.exportChartLocal({ type: 'image/svg+xml' });
                    break;
                case 'pdf':
                    this.chart.exportChartLocal({ type: 'application/pdf' });
                    break;
                case 'csv':
                    this.chart.downloadCSV();
                    break;
                case 'xlsx':
                    // Use custom XLSX export function
                    exportChartToXLSX(this.chart, this.getDefaultFileName());
                    break;
                default:
                    console.error('❌ Unknown export type:', type);
            }
        } catch (error) {
            console.error('❌ Export failed:', error);
        }
    }
}

/**
 * Initialize chart export menu for a given chart and container
 * @param {Object} chart - Highcharts chart instance
 * @param {HTMLElement} container - Chart container element
 * @returns {ChartExportMenu} - Export menu instance
 */
function initChartExportMenu(chart, container) {
    if (!chart) {
        console.error('❌ Chart instance is required');
        return null;
    }

    if (!container) {
        console.error('❌ Container element is required');
        return null;
    }

    // Ensure container has relative positioning for absolute positioning of export button
    if (getComputedStyle(container).position === 'static') {
        container.style.position = 'relative';
    }

    try {
        const exportMenu = new ChartExportMenu(chart, container);
        return exportMenu;
    } catch (error) {
        console.error('❌ Failed to initialize chart export menu:', error);
        return null;
    }
}

// Make functions available globally
window.initChartExportMenu = initChartExportMenu;
window.ChartExportMenu = ChartExportMenu;

// Export for CommonJS
exports.ChartExportMenu = ChartExportMenu;
exports.initChartExportMenu = initChartExportMenu;