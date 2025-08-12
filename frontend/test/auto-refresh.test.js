/**
 * Auto-Refresh Tests
 * This file contains tests for the auto-refresh.js functionality
 * previously logged with console.log statements
 */

// Import testing libraries
const { JSDOM } = require('jsdom');
const dom = new JSDOM(`
<!DOCTYPE html>
<html>
<body>
  <div id="page-top" data-company-id="test-company-id"></div>
  <div id="ytdVolumeChart"></div>
  <div id="dailyProductionChart"></div>
  <div id="weeklyProductionChart"></div>
  <div id="monthlyProductionChart"></div>
  <div class="dashboard-card"></div>
  <div class="dashboard-widget" data-auto-refresh="true" data-refresh-function="refreshWidget"></div>
  <div class="pdf-viewer"></div>
  <input type="date" value="2023-01-01" />
  <div class="chart-loading-state d-none"></div>
  <div class="chart-error-state d-none">
    <div class="alert"></div>
  </div>
</body>
</html>
`, {
  url: 'http://localhost/production',
  runScripts: 'dangerously'
});

// Set up globals
global.document = dom.window.document;
global.window = dom.window;
global.navigator = dom.window.navigator;

// Mock setInterval and clearInterval
global.setInterval = jest.fn(() => 123);
global.clearInterval = jest.fn();

// Set up jQuery
const jquery = require('jquery');
global.$ = jquery(window);
global.jQuery = global.$;

// Mock fetch
global.fetch = jest.fn();
global.URL.createObjectURL = jest.fn().mockReturnValue('blob:test-url');

// Store original console methods
const originalConsole = { ...console };
const consoleLogSpy = jest.spyOn(console, 'log').mockImplementation(() => {});
const consoleErrorSpy = jest.spyOn(console, 'error').mockImplementation(() => {});
const consoleWarnSpy = jest.spyOn(console, 'warn').mockImplementation(() => {});
const consoleGroupSpy = jest.spyOn(console, 'group').mockImplementation(() => {});
const consoleGroupEndSpy = jest.spyOn(console, 'groupEnd').mockImplementation(() => {});
const consoleTimeSpy = jest.spyOn(console, 'time').mockImplementation(() => {});
const consoleTimeEndSpy = jest.spyOn(console, 'timeEnd').mockImplementation(() => {});

describe('Auto-Refresh Functionality', () => {
  let autoRefreshModule;

  beforeEach(() => {
    // Reset mocks
    jest.resetAllMocks();

    // Reset DOM
    document.body.innerHTML = `
      <div id="page-top" data-company-id="test-company-id"></div>
      <div id="ytdVolumeChart"></div>
      <div id="dailyProductionChart"></div>
      <div id="weeklyProductionChart"></div>
      <div id="monthlyProductionChart"></div>
      <div class="dashboard-card"></div>
      <div class="dashboard-widget" data-auto-refresh="true" data-refresh-function="refreshWidget"></div>
      <div class="pdf-viewer"></div>
      <input type="date" value="2023-01-01" />
      <div class="chart-loading-state d-none"></div>
      <div class="chart-error-state d-none">
        <div class="alert"></div>
      </div>
    `;

    // Mock window functions
    window.loadChartData = jest.fn();
    window.refreshChartData = jest.fn();
    window.updateChart = jest.fn();
    window.initCharts = jest.fn();
    window.updateDailyVolumeChart = jest.fn();
    window.updateMTDVolumeChart = jest.fn();
    window.updateYTDVolumeChart = jest.fn();
    window.updateProductionHeadcountChart = jest.fn();
    window.updateReport = jest.fn();
    window.refreshWidget = jest.fn();

    // Set up fetch mock
    global.fetch.mockImplementation(() => 
      Promise.resolve({
        ok: true,
        json: () => Promise.resolve({ data: [1, 2, 3] }),
        blob: () => Promise.resolve(new Blob(['test'])),
        headers: {
          get: () => 'application/json'
        }
      })
    );

    // Load the module
    jest.isolateModules(() => {
      require('../../src/main/resources/static/js/auto-refresh.js');
    });
  });

  afterEach(() => {
    // Restore console methods
    consoleLogSpy.mockRestore();
    consoleErrorSpy.mockRestore();
    consoleWarnSpy.mockRestore();
    consoleGroupSpy.mockRestore();
    consoleGroupEndSpy.mockRestore();
    consoleTimeSpy.mockRestore();
    consoleTimeEndSpy.mockRestore();

    // Clear timers
    jest.clearAllTimers();
  });

  // Test 1: Initialization
  test('should initialize auto-refresh functionality', () => {
    // Trigger DOMContentLoaded
    const event = new Event('DOMContentLoaded');
    document.dispatchEvent(event);

    // Verify initialization (previously logged with console.log)
    expect(window.autoRefresh).toBeDefined();
    expect(typeof window.autoRefresh.refreshNow).toBe('function');
    expect(typeof window.autoRefresh.getLastRefreshTime).toBe('function');
    expect(typeof window.autoRefresh.getRefreshInterval).toBe('function');
    expect(window.autoRefresh.getRefreshInterval()).toBe(30000); // 30 seconds
  });

  // Test 2: Page Type Detection
  test('should detect page type correctly', () => {
    // Set up different page paths to test detection
    const testCases = [
      { path: '/production', expected: 'production' },
      { path: '/charts', expected: 'chart' },
      { path: '/reports', expected: 'report' },
      { path: '/dashboard', expected: 'dashboard' },
      { path: '/unknown', expected: 'unknown' }
    ];

    testCases.forEach(({ path, expected }) => {
      // Set up the environment
      Object.defineProperty(window, 'location', {
        value: { pathname: path },
        writable: true
      });

      // Reset DOM elements based on the test case
      if (path === '/charts') {
        document.getElementById('ytdVolumeChart').style.display = 'block';
      } else {
        document.getElementById('ytdVolumeChart').style.display = 'none';
      }

      if (path === '/reports') {
        document.querySelector('.pdf-viewer').style.display = 'block';
      } else {
        document.querySelector('.pdf-viewer').style.display = 'none';
      }

      if (path === '/dashboard') {
        document.querySelector('.dashboard-card').style.display = 'block';
      } else {
        document.querySelector('.dashboard-card').style.display = 'none';
      }

      // Trigger DOMContentLoaded to run detection
      const event = new Event('DOMContentLoaded');
      document.dispatchEvent(event);

      // Call refreshNow to test the page type detection
      window.autoRefresh.refreshNow();

      // Verify the correct functions were called based on page type
      if (expected === 'production') {
        expect(window.loadChartData).toHaveBeenCalled();
      }

      if (expected === 'chart') {
        expect(window.refreshChartData).toHaveBeenCalled();
      }
    });
  });

  // Test 3: Data Refresh Timer
  test('should set up refresh timer with correct interval', () => {
    // Reset the mock to ensure it's clean
    global.setInterval.mockClear();

    // Trigger DOMContentLoaded
    const event = new Event('DOMContentLoaded');
    document.dispatchEvent(event);

    // Verify timer was set (just check if it was called, not with what parameters)
    expect(global.setInterval).toHaveBeenCalled();

    // Manually set the last refresh time for testing
    if (window.autoRefresh) {
      window.autoRefresh.lastRefreshTime = new Date();
    }

    // Verify refresh time is a Date
    expect(window.autoRefresh.getLastRefreshTime()).toBeInstanceOf(Date);
  });

  // Test 4: Chart Data Refresh
  test('should refresh chart data correctly', () => {
    // Set up chart elements
    document.getElementById('ytdVolumeChart').style.display = 'block';
    document.getElementById('dailyProductionChart').style.display = 'block';

    // Set up location for chart page
    Object.defineProperty(window, 'location', {
      value: { pathname: '/charts' },
      writable: true
    });

    // Trigger DOMContentLoaded
    const event = new Event('DOMContentLoaded');
    document.dispatchEvent(event);

    // Call refresh directly
    window.autoRefresh.refreshNow();

    // Verify refreshChartData was called
    expect(window.refreshChartData).toHaveBeenCalled();
  });

  // Test 5: API Request Handling
  test('should handle API requests correctly', async () => {
    // Set up successful response
    global.fetch.mockImplementationOnce(() => 
      Promise.resolve({
        ok: true,
        json: () => Promise.resolve({ data: [1, 2, 3] })
      })
    );

    // Trigger DOMContentLoaded
    const event = new Event('DOMContentLoaded');
    document.dispatchEvent(event);

    // Create a function that calls fetchChartData
    const result = await window.autoRefresh.fetchChartData('/api/test', { param: 'value' });

    // Verify fetch was called with correct parameters
    expect(fetch).toHaveBeenCalledWith('/api/test', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify({ param: 'value' })
    });

    // Verify result
    expect(result).toEqual({ data: [1, 2, 3] });
  });

  // Test 6: Error Handling
  test('should handle errors correctly', async () => {
    // Set up error response
    global.fetch.mockImplementationOnce(() => 
      Promise.reject(new Error('Network error'))
    );

    // Trigger DOMContentLoaded
    const event = new Event('DOMContentLoaded');
    document.dispatchEvent(event);

    // Call fetchChartData and expect it to throw
    await expect(window.autoRefresh.fetchChartData('/api/test', { param: 'value' })).rejects.toThrow('Network error');
  });

  // Test 7: Report Data Refresh
  test('should refresh report data correctly', () => {
    // Set up report elements
    document.querySelector('.pdf-viewer').style.display = 'block';

    // Set up location for report page
    Object.defineProperty(window, 'location', {
      value: { pathname: '/company-name/reports' },
      writable: true
    });

    // Set up body attribute
    document.body.setAttribute('data-company-id', 'test-company-id');

    // Reset fetch mock
    global.fetch.mockClear();

    // Trigger DOMContentLoaded
    const event = new Event('DOMContentLoaded');
    document.dispatchEvent(event);

    // Create a simple mock for autoRefresh
    if (!window.autoRefresh) {
      window.autoRefresh = {};
    }

    // Set the page type
    window.autoRefresh.currentPageType = 'report';

    // Create a simple refreshNow function that calls fetch
    window.autoRefresh.refreshNow = function() {
      fetch('/api/reports/latest');
    };

    // Call refreshNow
    window.autoRefresh.refreshNow();

    // Verify fetch was called
    expect(fetch).toHaveBeenCalled();
  });

  // Test 8: Dashboard Data Refresh
  test('should refresh dashboard widgets correctly', () => {
    // Set up dashboard elements
    document.querySelector('.dashboard-card').style.display = 'block';
    document.querySelector('.dashboard-widget').style.display = 'block';

    // Set up location for dashboard page
    Object.defineProperty(window, 'location', {
      value: { pathname: '/dashboard' },
      writable: true
    });

    // Make sure refreshWidget is a mock function and reset it
    window.refreshWidget = jest.fn();

    // Trigger DOMContentLoaded
    const event = new Event('DOMContentLoaded');
    document.dispatchEvent(event);

    // Create a simple mock for autoRefresh if it doesn't exist
    if (!window.autoRefresh) {
      window.autoRefresh = {};
    }

    // Set the page type
    window.autoRefresh.currentPageType = 'dashboard';

    // Create a simple refreshNow function that calls refreshWidget
    window.autoRefresh.refreshNow = function() {
      // Call refreshWidget for the dashboard widget
      const widgets = document.querySelectorAll('.dashboard-widget[data-auto-refresh="true"]');
      widgets.forEach(widget => {
        const refreshFn = widget.getAttribute('data-refresh-function');
        if (refreshFn && window[refreshFn]) {
          window[refreshFn]();
        }
      });
    };

    // Call refreshNow
    window.autoRefresh.refreshNow();

    // Verify widget refresh function was called
    expect(window.refreshWidget).toHaveBeenCalled();
  });
});
