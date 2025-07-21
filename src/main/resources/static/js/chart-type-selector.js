/**
 * 📊 Chart Type Selector - Uniform Implementation
 * Provides consistent chart type selection across all templates
 */

class ChartTypeSelector {
    constructor(containerId, options = {}) {
        this.containerId = containerId;
        this.container = document.getElementById(containerId);
        this.options = {
            defaultType: 'line',
            enabledTypes: ['line', 'column', 'bar'],
            onTypeChange: null,
            showTooltips: true,
            ...options
        };
        
        this.selectedType = this.options.defaultType;
        this.init();
    }

    init() {
        if (!this.container) {
            console.error(`❌ Chart type selector container not found: ${this.containerId}`);
            return;
        }

        console.log(`🎨 Initializing chart type selector: ${this.containerId}`);
        this.render();
        this.attachEventListeners();
        console.log(`✅ Chart type selector initialized: ${this.containerId}`);
    }

    render() {
        const chartTypes = {
            'line': { icon: 'line-chart.png', tooltip: 'Line Chart' },
            'column': { icon: 'column-chart.png', tooltip: 'Column Chart' },
            'bar': { icon: 'bar-chart.png', tooltip: 'Bar Chart' },
            'area': { icon: 'line-chart.png', tooltip: 'Area Chart' }, // Using line icon for area
            'spline': { icon: 'line-chart.png', tooltip: 'Spline Chart' }
        };

        let html = '<div class="items">';
        
        this.options.enabledTypes.forEach(type => {
            const isSelected = type === this.selectedType;
            const typeClass = isSelected ? 'type selected' : 'type';
            const chartType = chartTypes[type] || { icon: 'line-chart.png', tooltip: type };
            
            html += `
                <div class="${typeClass}" 
                     data-type="${type}" 
                     data-tooltip="${this.options.showTooltips ? chartType.tooltip : ''}"
                     title="${chartType.tooltip}">
                    <img src="/img/chart-types/${chartType.icon}" 
                         alt="${type}" 
                         loading="lazy">
                </div>
            `;
        });
        
        html += '</div>';
        this.container.innerHTML = html;
    }

    attachEventListeners() {
        const typeButtons = this.container.querySelectorAll('.type');
        
        typeButtons.forEach(button => {
            button.addEventListener('click', (e) => {
                e.preventDefault();
                const newType = button.dataset.type;
                this.selectType(newType);
            });

            // Add hover effects for better UX
            button.addEventListener('mouseenter', () => {
                button.style.transform = 'translateY(-1px)';
            });

            button.addEventListener('mouseleave', () => {
                if (!button.classList.contains('selected')) {
                    button.style.transform = 'translateY(0)';
                }
            });
        });
    }

    selectType(type) {
        if (type === this.selectedType) {
            return; // No change needed
        }

        console.log(`🔄 Chart type changed from ${this.selectedType} to ${type}`);
        
        // Update visual state
        const buttons = this.container.querySelectorAll('.type');
        buttons.forEach(btn => {
            btn.classList.remove('selected');
            if (btn.dataset.type === type) {
                btn.classList.add('selected');
            }
        });

        this.selectedType = type;

        // Call callback if provided
        if (this.options.onTypeChange && typeof this.options.onTypeChange === 'function') {
            this.options.onTypeChange(type);
        }

        // Dispatch custom event for other components
        const event = new CustomEvent('chartTypeChanged', {
            detail: { type, containerId: this.containerId }
        });
        document.dispatchEvent(event);
    }

    getSelectedType() {
        return this.selectedType;
    }

    setEnabledTypes(types) {
        this.options.enabledTypes = types;
        this.render();
        this.attachEventListeners();
    }

    destroy() {
        const buttons = this.container.querySelectorAll('.type');
        buttons.forEach(button => {
            button.removeEventListener('click', null);
            button.removeEventListener('mouseenter', null);
            button.removeEventListener('mouseleave', null);
        });
        this.container.innerHTML = '';
    }
}

/**
 * 🚀 Initialize all chart type selectors on the page
 */
function initializeChartTypeSelectors() {
    console.log('🎨 Initializing all chart type selectors...');
    
    const selectors = document.querySelectorAll('.chart-type-selector');
    const initializedSelectors = [];

    selectors.forEach(selector => {
        const containerId = selector.id;
        if (!containerId) {
            console.warn('⚠️ Chart type selector without ID found, skipping...');
            return;
        }

        // Determine enabled types based on container ID
        let enabledTypes = ['line', 'column', 'bar'];
        if (containerId.includes('lagoon')) {
            enabledTypes = ['line', 'column', 'area'];
        } else if (containerId.includes('head-weight')) {
            enabledTypes = ['line', 'column'];
        }

        const chartSelector = new ChartTypeSelector(containerId, {
            defaultType: 'line',
            enabledTypes: enabledTypes,
            onTypeChange: (type) => {
                console.log(`📊 Chart type changed for ${containerId}: ${type}`);
                // Trigger chart reload based on container ID
                triggerChartReload(containerId, type);
            }
        });

        initializedSelectors.push(chartSelector);
    });

    console.log(`✅ Initialized ${initializedSelectors.length} chart type selectors`);
    return initializedSelectors;
}

/**
 * 🔄 Trigger chart reload based on selector ID
 */
function triggerChartReload(containerId, chartType) {
    // Map container IDs to chart reload functions
    const chartReloadMap = {
        // Production project charts
        'monthly-chart-selector': () => loadMonthlyProductionChart(chartType),
        'cumulative-chart-selector': () => loadCumulativeProductionChart(chartType),
        'ytd-chart-selector': () => loadProductionYTDChart(chartType),
        'budget-chart-selector': () => loadProductionBudgetChart(chartType),
        
        // Livestock project charts
        'headcount-chart-selector': () => loadAnimalHeadcountChart(chartType),
        'head-weight-chart-selector': () => loadHeadWeightChart(chartType),
        
        // Digesters project charts
        'lagoon-chart-selector': () => loadLagoonLevelsChart(chartType),
        
        // Dashboard charts - Volume charts
        'daily-volume-chart-selector': () => updateChart(),
        'mtd-volume-chart-selector': () => updateChart(),
        'ytd-volume-chart-selector': () => updateChart(),
        
        // Dashboard charts - Headcount charts
        'animal-headcount-chart-selector': () => updateChart(),
        'production-headcount-chart-selector': () => updateChart(),
        
        // Metrics project charts
        'temperature-chart-selector': () => initTemperatureChart(farmId),
        
        // Enhanced chart example
        'enhanced-chart-selector': () => updateChart()
    };

    const reloadFunction = chartReloadMap[containerId];
    if (reloadFunction && typeof reloadFunction === 'function') {
        console.log(`🔄 Reloading chart for ${containerId} with type: ${chartType}`);
        reloadFunction();
    } else {
        console.warn(`⚠️ No reload function found for ${containerId}`);
    }
}

// 🎯 Export for global use
window.ChartTypeSelector = ChartTypeSelector;
window.initializeChartTypeSelectors = initializeChartTypeSelectors; 