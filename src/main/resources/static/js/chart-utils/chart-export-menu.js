/**
 * Chart Export Menu - Enhanced functionality matching Next.js implementation
 * Provides custom file naming and formatting options
 */

class ChartExportMenu {
    constructor(chart, container) {
        this.chart = chart;
        this.container = container;
        this.showOptions = false;
        this.init();
    }

    init() {
        console.log('🔧 Initializing Chart Export Menu'); // Debug log
        
        // Create export button
        this.createExportButton();
        // Create dropdown menu
        this.createExportMenu();
        // Add event listeners
        this.addEventListeners();
        
        console.log('✅ Chart Export Menu initialized'); // Debug log
    }

    createExportButton() {
        const button = document.createElement('div');
        button.className = 'chart-export-button';
        button.innerHTML = '<img src="/svg/download.svg" alt="Download" style="width: 25px;">';
        button.style.cssText = `
            cursor: pointer;
            border-radius: 6px;
            padding: 2px;
            position: absolute;
            top: 10px;
            right: 10px;
            z-index: 1000;
            background: rgba(255, 255, 255, 0.9);
            transition: background-color 0.2s ease;
        `;
        
        button.addEventListener('mouseenter', () => {
            button.style.backgroundColor = '#eee';
        });
        
        button.addEventListener('mouseleave', () => {
            button.style.backgroundColor = 'rgba(255, 255, 255, 0.9)';
        });

        this.exportButton = button;
        this.container.appendChild(button);
        
        console.log('📥 Export button created'); // Debug log
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
            font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif;
        `;

        const menuItems = [
            { label: 'Download PNG Image', action: 'png' },
            { label: 'Download JPEG Image', action: 'jpg' },
            { label: 'Download SVG Image', action: 'svg' },
            { separator: true },
            { label: 'Download PDF Document', action: 'pdf' },
            { label: 'Download CSV Document', action: 'csv' },
            { label: 'Download XLS Document', action: 'xls' }
        ];

        menuItems.forEach(item => {
            if (item.separator) {
                const separator = document.createElement('hr');
                separator.style.cssText = 'margin: 5px 0; border: none; border-top: 1px solid #eee;';
                menu.appendChild(separator);
            } else {
                const menuItem = document.createElement('div');
                menuItem.className = 'chart-export-menu-item';
                menuItem.textContent = item.label;
                menuItem.style.cssText = `
                    padding: 8px 12px;
                    cursor: pointer;
                    font-size: 14px;
                    transition: background-color 0.2s ease;
                `;
                
                menuItem.addEventListener('mouseenter', () => {
                    menuItem.style.backgroundColor = '#f8f9fa';
                });
                
                menuItem.addEventListener('mouseleave', () => {
                    menuItem.style.backgroundColor = 'white';
                });
                
                menuItem.addEventListener('click', () => {
                    this.exportChart(item.action);
                    this.toggleMenu();
                });
                
                menu.appendChild(menuItem);
            }
        });

        this.exportMenu = menu;
        this.container.appendChild(menu);
        
        console.log('📋 Export menu created'); // Debug log
    }

    addEventListeners() {
        this.exportButton.addEventListener('click', () => {
            this.toggleMenu();
        });

        // Close menu when clicking outside
        document.addEventListener('click', (e) => {
            if (!this.container.contains(e.target)) {
                this.hideMenu();
            }
        });
        
        console.log('🎧 Event listeners added'); // Debug log
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
        console.log('📂 Export menu shown'); // Debug log
    }

    hideMenu() {
        this.exportMenu.style.display = 'none';
        this.showOptions = false;
        console.log('📂 Export menu hidden'); // Debug log
    }

    getDefaultFileName() {
        try {
            // Get filename from chart title, matching Next.js implementation
            const title = this.chart.title ? this.chart.title.textStr : 'chart-data';
            
            // Ensure title is a string and handle edge cases
            if (typeof title !== 'string' || !title.trim()) {
                console.warn('⚠️ Invalid chart title, using default filename');
                return 'chart-data';
            }
            
            const sanitizedTitle = title.replace(/[^a-zA-Z0-9\s-]/g, '').replace(/\s+/g, '-').toLowerCase();
            console.log('📝 Generated filename:', sanitizedTitle); // Debug log
            return sanitizedTitle;
        } catch (error) {
            console.warn('⚠️ Error generating filename, using default:', error);
            return 'chart-data';
        }
    }

    exportChart(type) {
        try {
            const fileName = this.getDefaultFileName();
            console.log('📊 Exporting chart as:', type); // Debug log
            
            switch(type) {
                case 'png':
                    this.chart.exportChartLocal({
                        type: 'image/png',
                        filename: fileName
                    });
                    break;
                case 'jpg':
                    this.chart.exportChartLocal({
                        type: 'image/jpeg',
                        filename: fileName
                    });
                    break;
                case 'svg':
                    this.chart.exportChartLocal({
                        type: 'image/svg+xml',
                        filename: fileName
                    });
                    break;
                case 'pdf':
                    this.chart.exportChartLocal({
                        type: 'application/pdf',
                        filename: fileName
                    });
                    break;
                case 'csv':
                    if (typeof this.chart.downloadCSV === 'function') {
                        this.chart.downloadCSV();
                    } else {
                        console.warn('⚠️ CSV export not available');
                    }
                    break;
                case 'xls':
                    // Use custom XLSX export if available, otherwise fallback to default
                    if (this.chart.downloadXLSX) {
                        console.log('🔧 Using custom XLSX export'); // Debug log
                        this.chart.downloadXLSX();
                    } else if (typeof this.chart.downloadXLS === 'function') {
                        console.log('📄 Using default XLS export'); // Debug log
                        this.chart.downloadXLS();
                    } else {
                        console.warn('⚠️ XLS export not available');
                    }
                    break;
                default:
                    console.error('❌ Unknown export type:', type);
            }
        } catch (error) {
            console.error('❌ Error during chart export:', error);
        }
    }
}

// Global function to initialize export menu for any chart
window.initChartExportMenu = function(chart, container) {
    console.log('🚀 Initializing chart export menu'); // Debug log
    return new ChartExportMenu(chart, container);
}; 