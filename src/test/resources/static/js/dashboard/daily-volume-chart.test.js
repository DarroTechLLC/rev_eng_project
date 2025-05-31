/**
 * Test script for daily-volume-chart.js
 * 
 * This script tests the functionality of the daily volume chart,
 * including data loading, chart rendering, and sample data generation.
 */

// Mock Chart.js
window.Chart = class Chart {
    constructor(ctx, config) {
        this.type = config.type;
        this.data = config.data;
        this.options = config.options;
        this.config = config;
        this.updated = false;
    }

    update() {
        this.updated = true;
    }

    setDatasetVisibility(index, visible) {
        if (!this.visibilityState) this.visibilityState = {};
        this.visibilityState[index] = visible;
    }
};

// Mock fetch API
const originalFetch = window.fetch;
window.fetch = jest.fn().mockImplementation((url) => {
    if (url.includes('/api/charts/multi-farm/farm-volumes-for-date')) {
        return Promise.resolve({
            ok: true,
            json: () => Promise.resolve({
                data: [
                    {
                        farmId: "1",
                        farmName: "Test Farm 1",
                        volume: 1000.0
                    },
                    {
                        farmId: "2",
                        farmName: "Test Farm 2",
                        volume: 2000.0
                    }
                ]
            })
        });
    }
    return Promise.reject(new Error('Unexpected URL'));
});

// Mock DOM elements
document.getElementById = jest.fn().mockImplementation((id) => {
    if (id === 'dailyVolumeChart') {
        return {
            getContext: () => ({})
        };
    }
    if (id === 'startDate') {
        return { value: '2025-05-30' };
    }
    if (id === 'endDate') {
        return { value: '2025-06-30' };
    }
    if (id === 'viewType') {
        return { value: 'daily', addEventListener: jest.fn() };
    }
    if (id === 'unitType') {
        return { value: 'gallons', addEventListener: jest.fn() };
    }
    if (id === 'showAllFarms') {
        return { checked: true, addEventListener: jest.fn() };
    }
    if (id === 'useFakeData') {
        return { checked: false, addEventListener: jest.fn() };
    }
    if (id === 'showDebugLog') {
        return { checked: true, addEventListener: jest.fn() };
    }
    if (id === 'farmToggles') {
        return { innerHTML: '', appendChild: jest.fn() };
    }
    if (id === 'chart-debug-log') {
        return { appendChild: jest.fn(), children: [] };
    }
    if (id === 'chart-debug-container') {
        return { style: { display: 'none' } };
    }
    if (id === 'page-top') {
        return { getAttribute: () => '1' };
    }
    return null;
});

document.querySelector = jest.fn().mockImplementation((selector) => {
    if (selector === '.chart-loading-state') {
        return { classList: { add: jest.fn(), remove: jest.fn() } };
    }
    if (selector === '.chart-error-state') {
        return { 
            classList: { add: jest.fn(), remove: jest.fn() },
            querySelector: () => ({ innerHTML: '' })
        };
    }
    if (selector.includes('meta[name="_csrf"]')) {
        return { getAttribute: () => 'token' };
    }
    if (selector.includes('meta[name="_csrf_header"]')) {
        return { getAttribute: () => 'X-CSRF-TOKEN' };
    }
    return null;
});

document.createElement = jest.fn().mockImplementation((tag) => {
    return {
        className: '',
        style: {},
        dataset: {},
        addEventListener: jest.fn(),
        appendChild: jest.fn(),
        textContent: ''
    };
});

document.querySelectorAll = jest.fn().mockImplementation(() => []);

// Test the DailyVolumeController
describe('DailyVolumeController', () => {
    let controller;

    beforeEach(() => {
        // Reset mocks
        jest.clearAllMocks();
        
        // Create a new controller instance
        controller = new DailyVolumeController();
    });

    test('should initialize correctly', () => {
        expect(controller).toBeDefined();
        expect(controller.chart).toBeDefined();
        expect(controller.debugMode).toBe(true);
    });

    test('should load real data from API', async () => {
        // Set up controller to use real data
        controller.useFakeData = false;
        
        // Call loadInitialData
        await controller.loadInitialData();
        
        // Verify fetch was called with correct URL
        expect(window.fetch).toHaveBeenCalledWith(
            expect.stringContaining('/api/charts/multi-farm/farm-volumes-for-date'),
            expect.any(Object)
        );
        
        // Verify data was processed correctly
        expect(controller.currentData).toBeDefined();
        expect(controller.currentData.farms).toHaveLength(2);
        expect(controller.currentData.farms[0].name).toBe('Test Farm 1');
        expect(controller.currentData.farms[1].name).toBe('Test Farm 2');
    });

    test('should generate sample data when toggle is on', async () => {
        // Set up controller to use fake data
        controller.useFakeData = true;
        
        // Call loadInitialData
        await controller.loadInitialData();
        
        // Verify fetch was NOT called
        expect(window.fetch).not.toHaveBeenCalled();
        
        // Verify sample data was generated
        expect(controller.currentData).toBeDefined();
        expect(controller.currentData.farms.length).toBeGreaterThan(0);
        
        // Verify sample data has the expected structure
        const farm = controller.currentData.farms[0];
        expect(farm.id).toBeDefined();
        expect(farm.name).toBeDefined();
        expect(farm.volumes).toBeDefined();
        expect(farm.volumes.length).toBeGreaterThan(0);
        expect(farm.volumes[0].date).toBeInstanceOf(Date);
        expect(typeof farm.volumes[0].value).toBe('number');
    });

    test('should handle API errors gracefully', async () => {
        // Set up controller to use real data
        controller.useFakeData = false;
        
        // Mock fetch to return an error
        window.fetch.mockImplementationOnce(() => Promise.reject(new Error('API Error')));
        
        // Call loadInitialData
        await controller.loadInitialData();
        
        // Verify error was handled and fallback to sample data
        expect(controller.currentData).toBeDefined();
        expect(controller.currentData.farms.length).toBeGreaterThan(0);
    });

    test('should toggle farm visibility correctly', () => {
        // Initialize controller with data
        controller.currentData = {
            farms: [
                { id: '1', name: 'Farm 1', volumes: [] },
                { id: '2', name: 'Farm 2', volumes: [] }
            ]
        };
        
        // Toggle visibility of a farm
        controller.toggleFarmVisibility('1', false);
        
        // Verify chart visibility was updated
        expect(controller.chart.visibilityState).toBeDefined();
        expect(controller.chart.visibilityState[0]).toBe(false);
        
        // Toggle visibility back on
        controller.toggleFarmVisibility('1', true);
        
        // Verify chart visibility was updated
        expect(controller.chart.visibilityState[0]).toBe(true);
    });

    test('should update units correctly', () => {
        // Mock unitType element
        document.getElementById.mockImplementationOnce(() => ({ value: 'liters' }));
        
        // Call updateUnits
        controller.updateUnits();
        
        // Verify chart options were updated
        expect(controller.chart.options.scales.y.title.text).toBe('Liters');
        
        // Verify chart was updated
        expect(controller.chart.updated).toBe(true);
    });
});