/**
 * Daily Volume Chart Tests
 * This file contains tests for the DailyVolumeController functionality
 * previously logged with console.log statements
 */

// Import testing libraries
const { JSDOM } = require('jsdom');
const dom = new JSDOM(`
<!DOCTYPE html>
<html>
<body>
  <div id="page-top" data-company-id="test-company-id"></div>
  <div id="dailyVolumeChart"></div>
  <div class="chart-loading-state d-none"></div>
  <div class="chart-error-state d-none">
    <div class="alert"></div>
  </div>
  <div id="farmToggles"></div>
  <div id="chart-debug-container">
    <div id="chart-debug-log"></div>
  </div>
  <input id="startDate" type="date" />
  <input id="endDate" type="date" />
  <select id="viewType">
    <option value="daily">Daily</option>
    <option value="weekly">Weekly</option>
  </select>
  <select id="unitType">
    <option value="mmbtu">MMBTUs</option>
    <option value="liters">Liters</option>
  </select>
  <input id="showAllFarms" type="checkbox" />
  <input id="showDebugLog" type="checkbox" />
</body>
</html>
`, {
  url: 'http://localhost',
  runScripts: 'dangerously'
});

// Set up globals
global.document = dom.window.document;
global.window = dom.window;
global.navigator = dom.window.navigator;

// Mock fetch
global.fetch = jest.fn();

// Mock Highcharts
global.Highcharts = {
  chart: jest.fn().mockReturnValue({
    series: [],
    update: jest.fn(),
    options: {
      scales: {
        y: { title: { text: '' } },
        x: { 
          type: '',
          title: { text: '' },
          time: {}
        }
      },
      plugins: {
        tooltip: { callbacks: { label: jest.fn() } },
        title: { text: '' }
      }
    },
    config: { type: 'bar' },
    data: { datasets: [] }
  }),
  numberFormat: jest.fn(val => val.toString()),
  dateFormat: jest.fn(() => 'Jan 1, 2023')
};

// Set up jQuery
const jquery = require('jquery');
global.$ = jquery(window);
global.jQuery = global.$;

// Mock console methods to track calls
const originalConsoleLog = console.log;
const originalConsoleError = console.error;
const consoleLogSpy = jest.spyOn(console, 'log').mockImplementation(() => {});
const consoleErrorSpy = jest.spyOn(console, 'error').mockImplementation(() => {});

// Mock date helpers
window.dateHelpers = {
  getCurrentDateString: jest.fn().mockReturnValue('2023-01-01')
};

describe('DailyVolumeController Tests', () => {
  let DailyVolumeController;
  let controller;

  beforeEach(() => {
    // Reset mocks
    jest.clearAllMocks();

    // Reset DOM
    document.body.innerHTML = `
      <div id="page-top" data-company-id="test-company-id"></div>
      <div id="dailyVolumeChart"></div>
      <div class="chart-loading-state d-none"></div>
      <div class="chart-error-state d-none">
        <div class="alert"></div>
      </div>
      <div id="farmToggles"></div>
      <div id="chart-debug-container">
        <div id="chart-debug-log"></div>
      </div>
      <input id="startDate" type="date" />
      <input id="endDate" type="date" />
      <select id="viewType">
        <option value="daily">Daily</option>
        <option value="weekly">Weekly</option>
      </select>
      <select id="unitType">
        <option value="mmbtu">MMBTUs</option>
        <option value="liters">Liters</option>
      </select>
      <input id="showAllFarms" type="checkbox" />
      <input id="showDebugLog" type="checkbox" />
    `;

    // Mock successful API response
    global.fetch.mockResolvedValue({
      ok: true,
      json: () => Promise.resolve({
        success: true,
        data: [
          { farmId: 'farm1', farmName: 'Farm 1', volume: 100 },
          { farmId: 'farm2', farmName: 'Farm 2', volume: 200 }
        ]
      })
    });

    // Import the DailyVolumeController class
    DailyVolumeController = require('../../src/main/resources/static/js/dashboard/daily-volume-chart.js').DailyVolumeController;

    // Create a new controller instance
    controller = new DailyVolumeController();
  });

  afterEach(() => {
    // Restore console methods
    consoleLogSpy.mockRestore();
    consoleErrorSpy.mockRestore();
  });

  test('Controller should initialize correctly', () => {
    // Check if controller was initialized
    expect(controller).toBeDefined();
    expect(controller.chart).not.toBeNull();
    expect(controller.debugMode).toBe(true);
  });

  test('log method should format messages correctly', () => {
    // Test regular log message
    controller.log('Test message');

    // Test log with data
    controller.log('Test with data', { key: 'value' });

    // Test error log
    controller.log('Test error', null, true);

    // Verify that the log method doesn't throw errors
    expect(() => controller.log('Test message')).not.toThrow();
  });

  test('Debug log should be added to DOM when debug element exists', () => {
    // Call log method
    controller.log('Test debug log');

    // Check if log was added to debug element
    const debugLogElement = document.getElementById('chart-debug-log');
    expect(debugLogElement.children.length).toBeGreaterThan(0);
    expect(debugLogElement.lastChild.textContent).toContain('Test debug log');
  });

  test('Debug mode should be toggleable', () => {
    // Get debug toggle
    const debugToggle = document.getElementById('showDebugLog');

    // Initial state
    expect(controller.debugMode).toBe(true);

    // Toggle debug mode off
    debugToggle.checked = false;
    debugToggle.dispatchEvent(new Event('change'));

    // Check if debug mode was updated
    expect(controller.debugMode).toBe(false);

    // Check if debug container visibility was updated
    const debugContainer = document.getElementById('chart-debug-container');
    expect(debugContainer.style.display).toBe('none');
  });

  test('loadInitialData should fetch and process data correctly', async () => {
    // Reset controller to test loadInitialData from scratch
    controller = null;

    // Create new controller (which calls loadInitialData)
    controller = new DailyVolumeController();

    // Wait for async operations to complete
    await new Promise(resolve => setTimeout(resolve, 0));

    // Check if fetch was called with correct URL
    expect(fetch).toHaveBeenCalledWith(
      expect.stringContaining('/api/charts/multi-farm/farm-volumes-for-date'),
      expect.any(Object)
    );

    // Check if chart was initialized
    expect(Highcharts.chart).toHaveBeenCalled();

    // Check if farm toggles were created
    const farmToggles = document.getElementById('farmToggles');
    expect(farmToggles.children.length).toBeGreaterThan(0);
  });

  test('loadInitialData should handle API errors', async () => {
    // Mock API error
    fetch.mockRejectedValueOnce(new Error('API error'));

    // Reset controller
    controller = null;

    // Create new controller (which calls loadInitialData)
    controller = new DailyVolumeController();

    // Wait for async operations to complete
    await new Promise(resolve => setTimeout(resolve, 0));

    // Check if error state was shown
    const errorElement = document.querySelector('.chart-error-state');
    expect(errorElement.classList.contains('d-none')).toBe(false);

    // Check if error message was set
    const alertElement = errorElement.querySelector('.alert');
    expect(alertElement.innerHTML).toContain('Error loading chart data');
  });

  test('refreshData should update chart with new data', async () => {
    // Wait for initial data load
    await new Promise(resolve => setTimeout(resolve, 0));

    // Mock new API response
    fetch.mockResolvedValueOnce({
      ok: true,
      json: () => Promise.resolve({
        success: true,
        data: [
          { farmId: 'farm1', farmName: 'Farm 1', volume: 150 },
          { farmId: 'farm2', farmName: 'Farm 2', volume: 250 },
          { farmId: 'farm3', farmName: 'Farm 3', volume: 300 }
        ]
      })
    });

    // Call refreshData
    await controller.refreshData();

    // Check if fetch was called again
    expect(fetch).toHaveBeenCalledTimes(2);

    // Check if chart was updated
    expect(controller.chart.update).toHaveBeenCalled();

    // Check if farm toggles were updated
    const farmToggles = document.getElementById('farmToggles');
    expect(farmToggles.children.length).toBe(3); // Should have 3 farms now
  });

  test('refreshData should handle API errors', async () => {
    // Wait for initial data load
    await new Promise(resolve => setTimeout(resolve, 0));

    // Mock API error for refresh
    fetch.mockRejectedValueOnce(new Error('Refresh error'));

    // Call refreshData
    await controller.refreshData();

    // Check if error state was shown
    const errorElement = document.querySelector('.chart-error-state');
    expect(errorElement.classList.contains('d-none')).toBe(false);

    // Check if error message was set
    const alertElement = errorElement.querySelector('.alert');
    expect(alertElement.innerHTML).toContain('Error loading chart data');
  });

  test('toggleFarmVisibility should show/hide farm data', async () => {
    // Wait for initial data load
    await new Promise(resolve => setTimeout(resolve, 0));

    // Mock chart series
    controller.chart.series = [
      { name: 'Farm 1', show: jest.fn(), hide: jest.fn() },
      { name: 'Farm 2', show: jest.fn(), hide: jest.fn() }
    ];

    // Set current data
    controller.currentData = {
      farms: [
        { id: 'farm1', name: 'Farm 1' },
        { id: 'farm2', name: 'Farm 2' }
      ]
    };

    // Toggle farm 1 visibility (hide)
    controller.toggleFarmVisibility('farm1', false);

    // Check if hide was called
    expect(controller.chart.series[0].hide).toHaveBeenCalled();

    // Toggle farm 1 visibility (show)
    controller.toggleFarmVisibility('farm1', true);

    // Check if show was called
    expect(controller.chart.series[0].show).toHaveBeenCalled();
  });

  test('toggleAllFarms should toggle all farm visibilities', async () => {
    // Wait for initial data load
    await new Promise(resolve => setTimeout(resolve, 0));

    // Create farm toggles
    const farmToggles = document.getElementById('farmToggles');
    farmToggles.innerHTML = `
      <div class="farm-toggle active" data-farm-id="farm1">Farm 1</div>
      <div class="farm-toggle active" data-farm-id="farm2">Farm 2</div>
    `;

    // Mock chart series
    controller.chart.series = [
      { name: 'Farm 1', show: jest.fn(), hide: jest.fn() },
      { name: 'Farm 2', show: jest.fn(), hide: jest.fn() }
    ];

    // Set current data
    controller.currentData = {
      farms: [
        { id: 'farm1', name: 'Farm 1' },
        { id: 'farm2', name: 'Farm 2' }
      ]
    };

    // Toggle all farms off
    controller.toggleAllFarms(false);

    // Check if all toggles were updated
    const togglesAfterHide = document.querySelectorAll('.farm-toggle.active');
    expect(togglesAfterHide.length).toBe(0);

    // Check if hide was called for all farms
    expect(controller.chart.series[0].hide).toHaveBeenCalled();
    expect(controller.chart.series[1].hide).toHaveBeenCalled();

    // Toggle all farms on
    controller.toggleAllFarms(true);

    // Check if all toggles were updated
    const togglesAfterShow = document.querySelectorAll('.farm-toggle.active');
    expect(togglesAfterShow.length).toBe(2);

    // Check if show was called for all farms
    expect(controller.chart.series[0].show).toHaveBeenCalled();
    expect(controller.chart.series[1].show).toHaveBeenCalled();
  });

  test('updateUnits should convert between MMBTUs and liters', async () => {
    // Wait for initial data load
    await new Promise(resolve => setTimeout(resolve, 0));

    // Set up chart options
    controller.chart.options = {
      scales: {
        y: { title: { text: 'MMBTUs' } }
      },
      plugins: {
        tooltip: { callbacks: { label: jest.fn() } },
        title: { text: 'Daily Production Volume: (300 MMBTUs)' }
      }
    };

    // Mock calculateTotalVolume
    controller.calculateTotalVolume = jest.fn().mockReturnValue(300);

    // Change unit type to liters
    document.getElementById('unitType').value = 'liters';

    // Call updateUnits
    controller.updateUnits();

    // Check if chart was updated
    expect(controller.chart.update).toHaveBeenCalled();
  });

  test('showError should display error message', () => {
    // Show error
    controller.showError(new Error('Test error'));

    // Check if error element is visible
    const errorElement = document.querySelector('.chart-error-state');
    expect(errorElement.classList.contains('d-none')).toBe(false);

    // Check if error message was set
    const alertElement = errorElement.querySelector('.alert');
    expect(alertElement.innerHTML).toContain('Test error');
  });

  test('hasMultipleDatesPerFarm should detect multiple dates', () => {
    // Test with single date per farm
    const singleDateData = {
      farms: [
        { 
          volumes: [{ date: '2023-01-01', value: 100 }]
        }
      ]
    };
    expect(controller.hasMultipleDatesPerFarm(singleDateData)).toBe(false);

    // Test with multiple dates per farm
    const multipleDateData = {
      farms: [
        { 
          volumes: [
            { date: '2023-01-01', value: 100 },
            { date: '2023-01-02', value: 150 }
          ]
        }
      ]
    };
    expect(controller.hasMultipleDatesPerFarm(multipleDateData)).toBe(true);
  });

  test('getRandomColor should return a valid hex color', () => {
    const color = controller.getRandomColor();
    expect(color).toMatch(/^#[0-9A-F]{6}$/);
  });

  test('hexToRgba should convert hex to rgba', () => {
    const rgba = controller.hexToRgba('#FF0000', 0.5);
    expect(rgba).toBe('rgba(255, 0, 0, 0.5)');
  });
});
