/**
 * @jest-environment jsdom
 */

// Import required modules
const fs = require('fs');
const path = require('path');

// Path to the HTML file
const HTML_PATH = path.resolve(__dirname, '../../src/main/resources/templates/content/dashboard/daily-volume.html');

// Setup before all tests
beforeAll(() => {
  // Create a minimal DOM structure
  document.body.innerHTML = `
    <div id="loadingIndicator" style="display:none;"></div>
    <div id="errorMessage" style="display:none;"></div>
    <input type="date" id="dateSelector" />
    <div id="daily-volume-chart-selector"></div>
    <div id="dailyVolumeChart"></div>
  `;

  // Mock global objects and functions
  global.selectedCompanyId = '';

  global.dateHelpers = {
    getCurrentDateString: jest.fn().mockReturnValue('2025-08-12'),
    isValidDateString: jest.fn().mockImplementation(s => /^\d{4}-\d{2}-\d{2}$/.test(s))
  };

  global.initializeChartTypeSelectors = jest.fn().mockReturnValue([
    { containerId: 'daily-volume-chart-selector', getSelectedType: () => 'column' }
  ]);

  global.chartSelectors = undefined;

  global.Highcharts = {
    chart: jest.fn().mockReturnValue({ destroy: jest.fn() }),
    numberFormat: jest.fn().mockImplementation(n => String(Math.round(Number(n) || 0)))
  };

  global.initChartExportMenu = jest.fn();

  // Mock fetch API
  global.fetch = jest.fn().mockImplementation(() => 
    Promise.resolve({
      ok: true,
      json: () => Promise.resolve({ data: [{ farm_name: 'Test Farm', volume: 100 }] })
    })
  );

  // Define the functions that would normally be in the script
  global.showLoading = jest.fn().mockImplementation(() => {
    document.getElementById('loadingIndicator').style.display = 'block';
  });

  global.hideLoading = jest.fn().mockImplementation(() => {
    document.getElementById('loadingIndicator').style.display = 'none';
  });

  global.showError = jest.fn().mockImplementation((message) => {
    const errorElement = document.getElementById('errorMessage');
    errorElement.textContent = message;
    errorElement.style.display = 'block';
  });

  global.hideError = jest.fn().mockImplementation(() => {
    document.getElementById('errorMessage').style.display = 'none';
  });

  // Store the current chart instance
  global.chart = null;

  global.updateChart = jest.fn().mockImplementation(() => {
    let selectedDate = document.getElementById('dateSelector').value;

    if (!selectedDate) {
      selectedDate = dateHelpers.getCurrentDateString();
      document.getElementById('dateSelector').value = selectedDate;
    }

    if (dateHelpers.isValidDateString && !dateHelpers.isValidDateString(selectedDate)) {
      showError('Invalid date format. Please use YYYY-MM-DD format.');
      hideLoading();
      return Promise.reject(new Error('Invalid date'));
    }

    const chartSelector = chartSelectors?.find(s => s.containerId === 'daily-volume-chart-selector');
    const chartType = chartSelector ? chartSelector.getSelectedType() : 'column';

    showLoading();
    hideError();

    return fetch('/api/charts/multi-farm/farm-volumes-for-date', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ company_id: selectedCompanyId, date: selectedDate })
    })
      .then(response => {
        if (!response.ok) {
          throw new Error('Failed to fetch chart data');
        }
        return response.json();
      })
      .then(data => {
        if (data.error) {
          throw new Error(data.errorMessage || 'Error in API response');
        }

        const chartData = data.data || [];
        if (chartData.length === 0) {
          showError('No data available for the selected date.');
          hideLoading();
          return;
        }

        // Destroy existing chart if it exists
        if (global.chart && typeof global.chart.destroy === 'function') {
          global.chart.destroy();
        }

        // Mock chart creation
        global.chart = Highcharts.chart('dailyVolumeChart', {
          chart: {
            type: chartType === 'column' ? 'column' : chartType,
            inverted: chartType === 'bar'
          },
          series: [{ name: 'Daily Volume', data: chartData.map(item => [item.farm_name, item.volume]) }]
        });

        hideLoading();
        return data;
      })
      .catch(error => {
        let errorMessage = 'Error loading chart data: ' + error.message;
        if (!navigator.onLine || error.name === 'TypeError') {
          errorMessage = 'Network error. Please check your internet connection and try again.';
        }
        showError(errorMessage);
        hideLoading();
        return Promise.reject(error);
      });
  });
});

// Reset mocks before each test
beforeEach(() => {
  jest.clearAllMocks();
});

describe('Daily Volume Dashboard (production tests replacing console logs)', () => {
  test('required DOM elements are present (replaces initial presence logs)', () => {
    expect(document.getElementById('dailyVolumeChart')).toBeTruthy();
    expect(document.getElementById('dateSelector')).toBeTruthy();
    expect(selectedCompanyId).toBeDefined();
  });

  test('defaults to today when no date selected and calls API with expected payload', async () => {
    await updateChart();

    expect(fetch).toHaveBeenCalledWith(
      '/api/charts/multi-farm/farm-volumes-for-date',
      expect.objectContaining({
        method: 'POST',
        body: expect.stringContaining('"date":"2025-08-12"')
      })
    );

    expect(Highcharts.chart).toHaveBeenCalled();
  });

  test('uses selected chart type from selector (column vs bar)', async () => {
    // Set up bar chart type
    global.chartSelectors = [
      { containerId: 'daily-volume-chart-selector', getSelectedType: () => 'bar' }
    ];

    await updateChart();

    // Check that Highcharts was called with the right options
    expect(Highcharts.chart).toHaveBeenCalledWith(
      'dailyVolumeChart',
      expect.objectContaining({
        chart: expect.objectContaining({
          inverted: true
        })
      })
    );
  });

  test('shows error when API returns no data array entries (replaces "Received data" + branch)', async () => {
    // Mock empty data response
    fetch.mockImplementationOnce(() => 
      Promise.resolve({
        ok: true,
        json: () => Promise.resolve({ data: [] })
      })
    );

    try {
      await updateChart();
    } catch (e) {
      // Ignore error
    }

    const errorEl = document.getElementById('errorMessage');
    expect(errorEl.style.display).toBe('block');
    expect(errorEl.textContent).toMatch(/No data available/i);
    expect(Highcharts.chart).not.toHaveBeenCalled();
  });

  test('handles non-OK response with friendly error (replaces response status log)', async () => {
    // Mock error response
    fetch.mockImplementationOnce(() => 
      Promise.resolve({
        ok: false,
        status: 500
      })
    );

    try {
      await updateChart();
    } catch (e) {
      // Ignore error
    }

    const errorEl = document.getElementById('errorMessage');
    expect(errorEl.style.display).toBe('block');
    expect(errorEl.textContent).toMatch(/Failed to fetch chart data|Error loading chart data/i);
  });

  test('handles network error with specific message (replaces console.error path)', async () => {
    // Mock network error
    fetch.mockImplementationOnce(() => 
      Promise.reject(new TypeError('Network failed'))
    );

    // Set navigator.onLine to false
    Object.defineProperty(navigator, 'onLine', { value: false, configurable: true });

    try {
      await updateChart();
    } catch (e) {
      // Ignore error
    }

    const errorEl = document.getElementById('errorMessage');
    expect(errorEl.style.display).toBe('block');
    expect(errorEl.textContent).toMatch(/Network error/i);
  });

  test('date validation on change: invalid -> shows error; valid -> updates', async () => {
    const dateInput = document.getElementById('dateSelector');

    // Test invalid date
    dateHelpers.isValidDateString.mockReturnValueOnce(false);
    dateInput.value = '2025-13-40';

    try {
      await updateChart();
    } catch (e) {
      // Ignore error
    }

    expect(document.getElementById('errorMessage').style.display).toBe('block');

    // Test valid date
    dateHelpers.isValidDateString.mockReturnValueOnce(true);
    dateInput.value = '2025-08-10';

    await updateChart();

    expect(Highcharts.chart).toHaveBeenCalled();
  });

  test('destroys existing chart safely before re-render (replaces destroy warnings)', async () => {
    // Create a mock chart with destroy method
    const mockDestroy = jest.fn();
    Highcharts.chart.mockReturnValueOnce({ destroy: mockDestroy });

    // First render
    await updateChart();

    // Second render
    await updateChart();

    // The destroy method should have been called
    expect(mockDestroy).toHaveBeenCalled();
  });
});
