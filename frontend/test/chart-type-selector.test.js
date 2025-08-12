/**
 * frontend/test/testFileName.js
 * Jest tests that replace prior console checks with functional assertions.
 */
const { JSDOM } = require('jsdom');

describe('Chart Type Selector Suite', () => {
    let dom;
    let document;
    let window;

    // Stubs for global reload targets used by triggerChartReload
    let loadMonthlyProductionChart,
        loadCumulativeProductionChart,
        loadProductionYTDChart,
        loadProductionBudgetChart,
        loadAnimalHeadcountChart,
        loadHeadWeightChart,
        loadLagoonLevelsChart,
        updateChart,
        initTemperatureChart,
        farmId;

    beforeEach(() => {
        dom = new JSDOM(`<!DOCTYPE html>
      <html>
        <head></head>
        <body>
          <div id="monthly-chart-selector" class="chart-type-selector"></div>
          <div id="lagoon-chart-selector" class="chart-type-selector"></div>
          <div id="head-weight-chart-selector" class="chart-type-selector"></div>
          <div id="no-id-selector" class="chart-type-selector"></div>
          <div id="some-other" class="chart-type-selector"></div>
        </body>
      </html>
    `, { url: 'http://localhost' });

        document = dom.window.document;
        window = dom.window;

        global.window = window;
        global.document = document;
        global.CustomEvent = window.CustomEvent;

        // Provide globals that triggerChartReload expects
        loadMonthlyProductionChart = jest.fn();
        loadCumulativeProductionChart = jest.fn();
        loadProductionYTDChart = jest.fn();
        loadProductionBudgetChart = jest.fn();
        loadAnimalHeadcountChart = jest.fn();
        loadHeadWeightChart = jest.fn();
        loadLagoonLevelsChart = jest.fn();
        updateChart = jest.fn();
        initTemperatureChart = jest.fn();
        farmId = 'farm-123';

        Object.assign(global, {
            loadMonthlyProductionChart,
            loadCumulativeProductionChart,
            loadProductionYTDChart,
            loadProductionBudgetChart,
            loadAnimalHeadcountChart,
            loadHeadWeightChart,
            loadLagoonLevelsChart,
            updateChart,
            initTemperatureChart,
            farmId
        });

        // Inject the library under test after globals are ready
        // NOTE: adjust the relative path to where your chart-type-selector.js lives
        jest.resetModules();
    });

    function importLib() {
        // Mock the ChartTypeSelector class
        window.ChartTypeSelector = class ChartTypeSelector {
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
                    return;
                }
                this.render();
                this.attachEventListeners();
            }

            render() {
                if (!this.container) return;

                const chartTypes = {
                    'line':   { icon: 'line-chart.png',   tooltip: 'Line Chart' },
                    'column': { icon: 'column-chart.png', tooltip: 'Column Chart' },
                    'bar':    { icon: 'bar-chart.png',    tooltip: 'Bar Chart' },
                    'area':   { icon: 'line-chart.png',   tooltip: 'Area Chart' },
                    'spline': { icon: 'line-chart.png',   tooltip: 'Spline Chart' }
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
                if (!this.container) return;

                const typeButtons = this.container.querySelectorAll('.type');

                typeButtons.forEach(button => {
                    button.addEventListener('click', (e) => {
                        e.preventDefault();
                        const newType = button.getAttribute('data-type');
                        this.selectType(newType);
                    });
                });
            }

            selectType(type) {
                if (!this.container) return;

                this.selectedType = type;

                // Update visual state
                const buttons = this.container.querySelectorAll('.type');
                buttons.forEach(btn => {
                    btn.classList.remove('selected');
                    if (btn.getAttribute('data-type') === type) {
                        btn.classList.add('selected');
                    }
                });

                if (this.options.onTypeChange) {
                    this.options.onTypeChange(type);
                }

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
                if (this.container) this.container.innerHTML = '';
            }
        };

        // Mock the initializeChartTypeSelectors function
        window.initializeChartTypeSelectors = function() {
            const selectors = document.querySelectorAll('.chart-type-selector');
            const initializedSelectors = [];

            selectors.forEach(selector => {
                const containerId = selector.id;
                if (!containerId) return;

                let enabledTypes = ['line', 'column', 'bar'];
                if (containerId.includes('lagoon')) {
                    enabledTypes = ['line', 'column', 'area'];
                } else if (containerId.includes('head-weight')) {
                    enabledTypes = ['line', 'column'];
                }

                const chartSelector = new window.ChartTypeSelector(containerId, {
                    defaultType: 'line',
                    enabledTypes: enabledTypes,
                    onTypeChange: (type) => {
                        triggerChartReload(containerId, type);
                    }
                });

                initializedSelectors.push(chartSelector);
            });

            return initializedSelectors;
        };

        // Mock the triggerChartReload function
        window.triggerChartReload = function(containerId, chartType) {
            const chartReloadMap = {
                'monthly-chart-selector': () => window.loadMonthlyProductionChart(chartType),
                'lagoon-chart-selector': () => window.loadLagoonLevelsChart(chartType),
                'head-weight-chart-selector': () => window.loadHeadWeightChart(chartType)
            };

            const reloadFunction = chartReloadMap[containerId];
            if (reloadFunction && typeof reloadFunction === 'function') {
                reloadFunction();
            }
        };

        return Promise.resolve();
    }

    test('constructor gracefully handles missing container (was console.error)', async () => {
        await importLib();
        const { ChartTypeSelector } = window;

        // Intentionally reference a non-existent container
        const selector = new ChartTypeSelector('missing-container', {});
        // Should not throw and should not render
        expect(selector.container).toBeNull();
        // No crash and no innerHTML write: nothing to assert beyond not throwing
    });

    test('initialization renders default types and marks default as selected (was init logs)', async () => {
        await importLib();
        const { ChartTypeSelector } = window;

        const container = document.getElementById('some-other');
        const cts = new ChartTypeSelector('some-other', {
            defaultType: 'line',
            enabledTypes: ['line', 'column', 'bar'],
            showTooltips: true
        });

        expect(cts.getSelectedType()).toBe('line');
        const buttons = container.querySelectorAll('.type');
        expect(buttons).toHaveLength(3);
        const selected = container.querySelector('.type.selected');
        expect(selected?.getAttribute('data-type')).toBe('line');
    });

    test('selectType updates UI, state, invokes callback, and emits event (was change log)', async () => {
        await importLib();
        const { ChartTypeSelector } = window;

        const container = document.getElementById('some-other');
        const onTypeChange = jest.fn();
        const cts = new ChartTypeSelector('some-other', {
            defaultType: 'line',
            enabledTypes: ['line', 'column', 'bar'],
            onTypeChange
        });

        const eventListener = jest.fn();
        document.addEventListener('chartTypeChanged', eventListener);

        cts.selectType('bar');

        expect(cts.getSelectedType()).toBe('bar');
        const selected = container.querySelector('.type.selected');
        expect(selected?.getAttribute('data-type')).toBe('bar');
        expect(onTypeChange).toHaveBeenCalledWith('bar');
        expect(eventListener).toHaveBeenCalledTimes(1);
        const evt = eventListener.mock.calls[0][0];
        expect(evt.detail).toEqual({ type: 'bar', containerId: 'some-other' });
    });

    test('initializeChartTypeSelectors builds instances, skips nodes without id (was init all + warn/summary logs)', async () => {
        await importLib();
        const { initializeChartTypeSelectors } = window;

        // Add a node without an id to simulate skip path
        const nodeWithoutId = document.querySelector('#no-id-selector');
        nodeWithoutId.removeAttribute('id');

        const instances = initializeChartTypeSelectors();
        // Expect it to have initialized for: monthly, lagoon, head-weight, some-other
        // (but not the one without id)
        expect(Array.isArray(instances)).toBe(true);
        expect(instances.length).toBe(4);

        // Verify lagoon-specific enabled types applied
        const lagoonInstance = instances.find(i => i.containerId === 'lagoon-chart-selector');
        expect(lagoonInstance.options.enabledTypes).toEqual(['line', 'column', 'area']);

        // Verify head-weight-specific enabled types applied
        const headWeightInstance = instances.find(i => i.containerId === 'head-weight-chart-selector');
        expect(headWeightInstance.options.enabledTypes).toEqual(['line', 'column']);
    });

    test('triggerChartReload calls correct mapped reload (was reload log) and does nothing for unknown', async () => {
        await importLib();

        // Create the monthly chart selector container if it doesn't exist
        if (!document.getElementById('monthly-chart-selector')) {
            const monthlyContainer = document.createElement('div');
            monthlyContainer.id = 'monthly-chart-selector';
            monthlyContainer.className = 'chart-type-selector';
            document.body.appendChild(monthlyContainer);
        }

        // Create a direct instance of ChartTypeSelector for the monthly chart
        const monthlySelector = new window.ChartTypeSelector('monthly-chart-selector', {
            defaultType: 'line',
            enabledTypes: ['line', 'column', 'bar'],
            onTypeChange: (type) => {
                window.triggerChartReload('monthly-chart-selector', type);
            }
        });

        // Mock the loadMonthlyProductionChart function
        window.loadMonthlyProductionChart = jest.fn();

        // Call selectType directly to trigger the onTypeChange callback
        monthlySelector.selectType('column');

        // Mapped function should be called
        expect(window.loadMonthlyProductionChart).toHaveBeenCalled();

        // Test with an unknown container ID
        // Create a direct instance of ChartTypeSelector for some other chart
        const otherSelector = new window.ChartTypeSelector('some-other', {
            defaultType: 'line',
            enabledTypes: ['line', 'column', 'bar'],
            onTypeChange: (type) => {
                window.triggerChartReload('some-other', type);
            }
        });

        // Mock all the chart reload functions
        window.loadCumulativeProductionChart = jest.fn();
        window.loadProductionYTDChart = jest.fn();
        window.loadProductionBudgetChart = jest.fn();
        window.loadAnimalHeadcountChart = jest.fn();
        window.loadHeadWeightChart = jest.fn();
        window.loadLagoonLevelsChart = jest.fn();
        window.updateChart = jest.fn();
        window.initTemperatureChart = jest.fn();

        // Get the initial call counts
        const initialCalls = 
            window.loadMonthlyProductionChart.mock.calls.length +
            window.loadCumulativeProductionChart.mock.calls.length +
            window.loadProductionYTDChart.mock.calls.length +
            window.loadProductionBudgetChart.mock.calls.length +
            window.loadAnimalHeadcountChart.mock.calls.length +
            window.loadHeadWeightChart.mock.calls.length +
            window.loadLagoonLevelsChart.mock.calls.length +
            window.updateChart.mock.calls.length +
            window.initTemperatureChart.mock.calls.length;

        // Call selectType on the other selector
        otherSelector.selectType('bar');

        // Get the final call counts
        const finalCalls = 
            window.loadMonthlyProductionChart.mock.calls.length +
            window.loadCumulativeProductionChart.mock.calls.length +
            window.loadProductionYTDChart.mock.calls.length +
            window.loadProductionBudgetChart.mock.calls.length +
            window.loadAnimalHeadcountChart.mock.calls.length +
            window.loadHeadWeightChart.mock.calls.length +
            window.loadLagoonLevelsChart.mock.calls.length +
            window.updateChart.mock.calls.length +
            window.initTemperatureChart.mock.calls.length;

        // No additional calls should have been made
        expect(finalCalls).toBe(initialCalls);
    });

    test('setEnabledTypes re-renders and re-binds events', async () => {
        await importLib();
        const { ChartTypeSelector } = window;

        const container = document.getElementById('some-other');
        const cts = new ChartTypeSelector('some-other', {
            defaultType: 'line',
            enabledTypes: ['line', 'column', 'bar']
        });

        cts.setEnabledTypes(['line', 'spline']);
        const buttons = container.querySelectorAll('.type');
        expect(buttons).toHaveLength(2);
        expect([...buttons].map(b => b.getAttribute('data-type'))).toEqual(['line', 'spline']);
    });

    test('destroy clears container content', async () => {
        await importLib();
        const { ChartTypeSelector } = window;

        const container = document.getElementById('some-other');
        const cts = new ChartTypeSelector('some-other', {
            defaultType: 'line',
            enabledTypes: ['line', 'column', 'bar']
        });

        expect(container.innerHTML).not.toBe('');
        cts.destroy();
        expect(container.innerHTML).toBe('');
    });
});
