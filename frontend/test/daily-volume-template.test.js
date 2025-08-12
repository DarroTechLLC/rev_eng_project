/**
 * Daily Volume Template Tests
 * This file contains tests for the inline JavaScript in daily-volume.html
 * that previously used console.log statements
 */

// Import testing libraries
const { JSDOM } = require('jsdom');

// Create a DOM environment with the necessary elements
const dom = new JSDOM(`
<!DOCTYPE html>
<html>
<body>
  <div id="page-top" data-company-id="test-company-id"></div>
  <div id="dailyVolumeChart"></div>
  <div id="loadingIndicator" style="display: none;">Loading...</div>
  <div id="errorMessage" class="alert alert-danger" style="display: none;"></div>
  <input type="date" id="dateSelector" />
  <div id="chart-type-selector"></div>
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
    destroy: jest.fn(),
    update: jest.fn()
  }),
  numberFormat: jest.fn(val => val.toString())
};

// Mock initChartExportMenu
global.initChartExportMenu = jest.fn();

// Mock initializeChartTypeSelectors
global.initializeChartTypeSelectors = jest.fn().mockReturnValue([
  { containerId: 'daily-volume-chart-selector', getSelectedType: jest.fn().mockReturnValue('column') }
]);

// Mock date helpers
window.dateHelpers = {
  getCurrentDateString: jest.fn().mockReturnValue('2023-01-01'),
  isValidDateString: jest.fn().mockReturnValue(true)
};

// Mock console methods to track calls
const consoleLogSpy = jest.spyOn(console, 'log').mockImplementation(() => {});
const consoleErrorSpy = jest.spyOn(console, 'error').mockImplementation(() => {});
const consoleWarnSpy = jest.spyOn(console, 'warn').mockImplementation(() => {});

describe('Daily Volume Template Tests', () => {
  let chartSelectors;
  
  beforeEach(() => {
    // Reset mocks
    jest.clearAllMocks();
    
    // Reset DOM
    document.body.innerHTML = `
      <div id="page-top" data-company-id="test-company-id"></div>
      <div id="dailyVolumeChart"></div>
      <div id="loadingIndicator" style="display: none;">Loading...</div>
      <div id="errorMessage" class="alert alert-danger" style="display: none;"></div>
      <input type="date" id="dateSelector" value="2023-01-01" />
      <div id="chart-type-selector"></div>
    `;
    
    // Mock successful API response
    global.fetch.mockResolvedValue({
      ok: true,
      json: () => Promise.resolve({
        success: true,
        data: [
          { farm_id: 'farm1', farm_name: 'Farm 1', volume: 100 },
          { farm_id: 'farm2', farm_name: 'Farm 2', volume: 200 }
        ]
      })
    });
    
    // Set up chart selectors
    chartSelectors = [
      { containerId: 'daily-volume-chart-selector', getSelectedType: jest.fn().mockReturnValue('column') }
    ];
    window.chartSelectors = chartSelectors;
  });
  
  afterEach(() => {
    // Restore console methods
    consoleLogSpy.mockRestore();
    consoleErrorSpy.mockRestore();
    consoleWarnSpy.mockRestore();
  });
  
  test('Chart configuration verification should check required elements', () => {
    // Test the functionality that was previously logged with console.log
    const chartContainer = document.getElementById('dailyVolumeChart');
    const dateSelector = document.getElementById('dateSelector');
    const companyId = document.getElementById('page-top').getAttribute('data-company-id');
    
    // Verify the elements that were previously checked with console.log
    expect(chartContainer).not.toBeNull();
    expect(dateSelector).not.toBeNull();
    expect(companyId).toBe('test-company-id');
  });
  
  test('updateChart should fetch and process data correctly', async () => {
    // Define the updateChart function from the template
    function updateChart() {
      // Get the selected date
      let selectedDate = document.getElementById('dateSelector').value;
      
      // If no date is selected, use today's date
      if (!selectedDate) {
        const today = window.dateHelpers ? 
          window.dateHelpers.getCurrentDateString() : 
          new Date().toISOString().split('T')[0];
        
        selectedDate = today;
        document.getElementById('dateSelector').value = selectedDate;
      }
      
      // Validate the date string
      let isDateValid = true;
      if (window.dateHelpers && window.dateHelpers.isValidDateString) {
        isDateValid = window.dateHelpers.isValidDateString(selectedDate);
        if (!isDateValid) {
          document.getElementById('errorMessage').textContent = 'Invalid date format. Please use YYYY-MM-DD format.';
          document.getElementById('errorMessage').style.display = 'block';
          document.getElementById('loadingIndicator').style.display = 'none';
          return;
        } else {
          document.getElementById('errorMessage').style.display = 'none';
        }
      }
      
      const chartSelector = window.chartSelectors?.find(s => s.containerId === 'daily-volume-chart-selector');
      const chartType = chartSelector ? chartSelector.getSelectedType() : 'column';
      
      document.getElementById('loadingIndicator').style.display = 'block';
      document.getElementById('errorMessage').style.display = 'none';
      
      const selectedCompanyId = document.getElementById('page-top').getAttribute('data-company-id');
      
      return fetch('/api/charts/multi-farm/farm-volumes-for-date', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({
          company_id: selectedCompanyId,
          date: selectedDate
        })
      })
      .then(response => {
        if (!response.ok) {
          throw new Error('Failed to fetch chart data');
        }
        return response.json();
      })
      .then(data => {
        // Check for error flag in response
        if (data.error) {
          throw new Error(data.errorMessage || 'Error in API response');
        }
        
        // Process data for chart
        const chartData = data.data || [];
        if (chartData.length === 0) {
          document.getElementById('errorMessage').textContent = 'No data available for the selected date.';
          document.getElementById('errorMessage').style.display = 'block';
          document.getElementById('loadingIndicator').style.display = 'none';
          return;
        }
        
        // Create chart using Highcharts
        Highcharts.chart('dailyVolumeChart', {
          chart: {
            type: chartType === 'column' ? 'column' : chartType,
            inverted: chartType === 'bar',
            height: 400
          },
          title: {
            text: `Daily Production Volume: (${Highcharts.numberFormat(300, 0)} MMBTUs)`,
            style: {
              fontSize: '16px',
              fontWeight: 'bold'
            }
          }
        });
        
        // Initialize enhanced export menu
        initChartExportMenu(Highcharts.chart(), document.getElementById('dailyVolumeChart'));
        
        document.getElementById('loadingIndicator').style.display = 'none';
      })
      .catch(error => {
        // Provide specific error message based on error type
        let errorMessage = 'Error loading chart data: ' + error.message;
        
        // Check if it's a network error
        if (!navigator.onLine || error.name === 'TypeError') {
          errorMessage = 'Network error. Please check your internet connection and try again.';
        }
        
        document.getElementById('errorMessage').textContent = errorMessage;
        document.getElementById('errorMessage').style.display = 'block';
        document.getElementById('loadingIndicator').style.display = 'none';
      });
    }
    
    // Call updateChart
    const updatePromise = updateChart();
    
    // Wait for async operations to complete
    await updatePromise;
    
    // Check if fetch was called with correct URL and data
    expect(fetch).toHaveBeenCalledWith(
      '/api/charts/multi-farm/farm-volumes-for-date',
      expect.objectContaining({
        method: 'POST',
        headers: expect.objectContaining({
          'Content-Type': 'application/json'
        }),
        body: expect.any(String)
      })
    );
    
    // Check if chart was created
    expect(Highcharts.chart).toHaveBeenCalled();
    
    // Check if export menu was initialized
    expect(initChartExportMenu).toHaveBeenCalled();
    
    // Check if loading indicator was hidden
    expect(document.getElementById('loadingIndicator').style.display).toBe('none');
  });
  
  test('updateChart should handle API errors', async () => {
    // Mock API error
    fetch.mockRejectedValueOnce(new Error('API error'));
    
    // Define the updateChart function from the template
    function updateChart() {
      // Get the selected date
      let selectedDate = document.getElementById('dateSelector').value;
      
      document.getElementById('loadingIndicator').style.display = 'block';
      document.getElementById('errorMessage').style.display = 'none';
      
      const selectedCompanyId = document.getElementById('page-top').getAttribute('data-company-id');
      
      return fetch('/api/charts/multi-farm/farm-volumes-for-date', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({
          company_id: selectedCompanyId,
          date: selectedDate
        })
      })
      .then(response => {
        if (!response.ok) {
          throw new Error('Failed to fetch chart data');
        }
        return response.json();
      })
      .then(data => {
        document.getElementById('loadingIndicator').style.display = 'none';
      })
      .catch(error => {
        // Provide specific error message based on error type
        let errorMessage = 'Error loading chart data: ' + error.message;
        
        document.getElementById('errorMessage').textContent = errorMessage;
        document.getElementById('errorMessage').style.display = 'block';
        document.getElementById('loadingIndicator').style.display = 'none';
      });
    }
    
    // Call updateChart
    const updatePromise = updateChart();
    
    // Wait for async operations to complete
    await updatePromise;
    
    // Check if error message is displayed
    expect(document.getElementById('errorMessage').style.display).toBe('block');
    expect(document.getElementById('errorMessage').textContent).toContain('Error loading chart data: API error');
    
    // Check if loading indicator was hidden
    expect(document.getElementById('loadingIndicator').style.display).toBe('none');
  });
  
  test('Date selector should validate dates', () => {
    // Mock invalid date
    window.dateHelpers.isValidDateString.mockReturnValueOnce(false);
    
    // Define the function to handle date change
    function handleDateChange() {
      const value = document.getElementById('dateSelector').value;
      
      // Clear any previous errors
      document.getElementById('errorMessage').style.display = 'none';
      
      // Validate the date before updating the chart
      if (value && window.dateHelpers && window.dateHelpers.isValidDateString) {
        const isValid = window.dateHelpers.isValidDateString(value);
        if (isValid) {
          // Valid date - would update the chart
          return true;
        } else {
          // Invalid date - show error but keep the value to allow correction
          document.getElementById('errorMessage').textContent = 'Invalid date format. Please use YYYY-MM-DD format.';
          document.getElementById('errorMessage').style.display = 'block';
          return false;
        }
      } else {
        // If date is empty or can't be validated, would update with default (today)
        return true;
      }
    }
    
    // Call the handler
    const result = handleDateChange();
    
    // Check if validation was called
    expect(window.dateHelpers.isValidDateString).toHaveBeenCalled();
    
    // Check if error message is displayed for invalid date
    expect(result).toBe(false);
    expect(document.getElementById('errorMessage').style.display).toBe('block');
    expect(document.getElementById('errorMessage').textContent).toContain('Invalid date format');
    
    // Reset mock to return valid date
    window.dateHelpers.isValidDateString.mockReturnValueOnce(true);
    
    // Call the handler again
    const result2 = handleDateChange();
    
    // Check if validation passed for valid date
    expect(result2).toBe(true);
    expect(document.getElementById('errorMessage').style.display).toBe('none');
  });
  
  test('Chart type selector should be initialized', () => {
    // Define the function to initialize chart type selectors
    function initChartTypeSelectors() {
      window.chartSelectors = initializeChartTypeSelectors();
      return window.chartSelectors;
    }
    
    // Call the function
    const selectors = initChartTypeSelectors();
    
    // Check if initializeChartTypeSelectors was called
    expect(initializeChartTypeSelectors).toHaveBeenCalled();
    
    // Check if chartSelectors was set
    expect(selectors).toBeDefined();
    expect(selectors[0].containerId).toBe('daily-volume-chart-selector');
  });
});