# Daily Volume Chart Implementation

This document provides an overview of the Daily Volume Chart implementation, including its features, architecture, and usage instructions.

## Overview

The Daily Volume Chart is a feature that displays farm volume data from the database. It allows users to view volume data for different farms over time, with options to filter by date range, view type (daily, weekly, monthly), and units (gallons, liters).

## Features

- **Real-time Data Display**: Shows volume data from the database in real-time
- **Sample Data Toggle**: Provides a toggle to switch between real data and sample data
- **Date Range Selection**: Allows users to select a date range for the data
- **View Type Selection**: Supports daily, weekly, and monthly views
- **Unit Conversion**: Supports conversion between gallons and liters
- **Farm Filtering**: Allows users to show/hide specific farms
- **Debug Mode**: Includes a debug panel for troubleshooting

## Architecture

The implementation consists of the following components:

### Backend

1. **ChartMeterDailyView**: Entity representing the database view for daily meter readings
2. **ChartMeterDailyViewRepository**: Repository for accessing the chart data
3. **ChartService**: Service for processing chart data
4. **ChartApiController**: API controller for chart data endpoints
5. **FarmVolumeData**: DTO for transferring farm volume data

### Frontend

1. **daily-volume.html**: HTML template for the chart page
2. **daily-volume.css**: CSS styles for the chart
3. **daily-volume-chart.js**: JavaScript for rendering the chart

## Implementation Details

### Data Flow

1. The user accesses the daily volume dashboard
2. The frontend JavaScript makes an API request to the backend
3. The ChartApiController receives the request and calls the ChartService
4. The ChartService queries the database via the ChartMeterDailyViewRepository
5. The data is transformed and returned to the frontend
6. The frontend JavaScript renders the chart with the data

### Type Conversions

The implementation handles several type conversions:

- **companyId**: String in API, Integer in repository
- **timestamp**: LocalDate in API, Integer (epoch seconds) in repository
- **farmId**: Integer in database, String in DTO

### Error Handling

The implementation includes comprehensive error handling:

- **Backend**: Catches exceptions and returns error responses
- **Frontend**: Handles API errors and falls back to sample data
- **Debug Logging**: Provides detailed logs for troubleshooting

## Usage Instructions

### Viewing the Chart

1. Navigate to the daily volume dashboard
2. Select a date range using the date pickers
3. Choose a view type (daily, weekly, monthly)
4. Select units (gallons or liters)
5. Use the farm toggles to show/hide specific farms

### Using Sample Data

If no real data is available or for demonstration purposes:

1. Toggle the "Use Sample Data" switch
2. The chart will display realistic sample data
3. Sample data respects the selected date range and view type

### Debugging

If issues occur:

1. Toggle the "Show Debug Log" switch
2. The debug panel will display detailed logs
3. Check the browser console for additional logs
4. Look for server-side logs in the application logs

## Testing

### Backend Tests

The backend implementation includes tests for:

1. **ChartApiControllerTest**: Tests the API controller's behavior
   - Verifies correct handling of successful responses
   - Verifies correct handling of errors
   - Verifies correct handling of empty data

### Frontend Tests

The frontend implementation includes tests for:

1. **daily-volume-chart.test.js**: Tests the chart JavaScript
   - Verifies correct initialization
   - Verifies loading real data from API
   - Verifies sample data generation
   - Verifies error handling
   - Verifies farm visibility toggling
   - Verifies unit conversion

## Troubleshooting

### No Data Displayed

If the chart shows no data:

1. Check if the "Use Sample Data" toggle is on
2. Verify the selected date range contains data
3. Check the debug log for API errors
4. Verify the company ID is correctly set
5. Check server logs for database query errors

### Chart Not Rendering

If the chart doesn't render:

1. Check browser console for JavaScript errors
2. Verify Chart.js is loaded correctly
3. Check if the canvas element exists
4. Try refreshing the page

### Incorrect Data

If the chart displays incorrect data:

1. Verify the date range is set correctly
2. Check the unit selection
3. Verify the database contains the expected data
4. Check for type conversion issues in the logs

## Future Improvements

Potential future improvements include:

1. **Data Export**: Add ability to export chart data as CSV
2. **More Chart Types**: Support for pie charts, bar charts, etc.
3. **Comparison View**: Compare data across different time periods
4. **Annotations**: Allow adding annotations to the chart
5. **Alerts**: Set up alerts for unusual data patterns