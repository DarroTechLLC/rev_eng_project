# Highcharts Export Functionality

This document explains how to use the Highcharts export functionality that has been added to the application.

## Overview

Highcharts has built-in capabilities to export charts in various formats such as CSV, Excel, PNG, JPEG, PDF, and SVG. This functionality has been enabled for all charts in the application.

## Implementation Details

The following changes have been made to enable export functionality:

1. **Enhanced Export Options in Theme**: The `highcharts-utils.js` file has been updated to include comprehensive export options in the Highcharts theme.

2. **Export Modules Loader**: A new script `highcharts-export-loader.js` has been created to automatically load the necessary Highcharts export modules if they're not already loaded.

3. **Thymeleaf Fragments**: New Thymeleaf fragments have been created in `fragments/highcharts-scripts.html` to simplify including Highcharts and its export functionality in templates.

## How to Use

### In HTML Templates

To include Highcharts with export functionality in your templates, use one of the following Thymeleaf fragments:

#### Basic Highcharts with Export Functionality

```html
<div th:replace="fragments/highcharts-scripts :: highcharts-basic"></div>
```

This includes:
- Highcharts core
- Export modules loader

#### Highcharts with Utils and Export Functionality

```html
<div th:replace="fragments/highcharts-scripts :: highcharts-with-utils"></div>
```

This includes:
- Highcharts core
- Export modules loader
- Highcharts utils (for common chart configurations)

#### Highcharts with All Modules Directly

```html
<div th:replace="fragments/highcharts-scripts :: highcharts-all-modules"></div>
```

This includes:
- Highcharts core
- All export modules directly (without the loader)
- Highcharts utils

### Export Options

With these changes, all charts in the application will now have an export button (three horizontal lines) in the top-right corner. Clicking this button will show a menu with the following options:

- View in full screen
- Print chart
- Download as PNG
- Download as JPEG
- Download as PDF
- Download as SVG
- Download CSV
- Download XLS
- View data table

## Updating Existing Templates

To update existing templates that use Highcharts:

1. Find the script tags that include Highcharts
2. Replace them with one of the Thymeleaf fragments mentioned above

Example:

```html
<!-- Before -->
<script src="https://code.highcharts.com/highcharts.js"></script>
<script src="/js/chart-utils/highcharts-utils.js"></script>

<!-- After -->
<div th:replace="fragments/highcharts-scripts :: highcharts-with-utils"></div>
```

## Troubleshooting

If the export button doesn't appear or doesn't work:

1. Check the browser console for errors
2. Ensure that the Highcharts export modules are being loaded correctly
3. Verify that the chart is being created after the export modules are loaded