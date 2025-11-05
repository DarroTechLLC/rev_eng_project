/**
 * Highcharts Export Modules Loader
 * This script ensures that all necessary Highcharts export modules are loaded
 */

// Execute immediately to ensure it runs as soon as possible
(function() {
    // Check if Highcharts is loaded
    if (typeof Highcharts === 'undefined') {
        console.error('Highcharts not loaded. Please load Highcharts before this script.');
        return;
    }

    // Function to load a script and execute callback when loaded
    function loadScript(url, callback) {
        // Skip if script is already loaded
        if (document.querySelector(`script[src="${url}"]`)) {
            if (callback) callback();
            return;
        }

        const script = document.createElement('script');
        script.type = 'text/javascript';
        script.src = url;
        script.onload = callback;
        document.head.appendChild(script);
    }

    // Load export modules in sequence if they're not already loaded
    function loadExportModules() {
        // Check if exporting module is already loaded
        if (Highcharts.exporting && Highcharts.exporting.Exporter) {
            console.log('Highcharts export modules already loaded');
            return;
        }

        console.log('Loading Highcharts export modules');
        
        // Load modules in sequence
        loadScript('https://cdn.jsdelivr.net/npm/highcharts@latest/modules/exporting.js', function() {
            loadScript('https://cdn.jsdelivr.net/npm/highcharts@latest/modules/export-data.js', function() {
                loadScript('https://cdn.jsdelivr.net/npm/highcharts@latest/modules/accessibility.js', function() {
                    console.log('All Highcharts export modules loaded');
                    
                    // Apply error handling after modules are loaded
                    if (typeof Highcharts !== 'undefined') {
                        // Override the getFilename function to prevent .replace errors
                        if (Highcharts.exporting && Highcharts.exporting.Exporter) {
                            const originalGetFilename = Highcharts.exporting.Exporter.prototype.getFilename;
                            if (originalGetFilename) {
                                Highcharts.exporting.Exporter.prototype.getFilename = function() {
                                    try {
                                        const filename = originalGetFilename.call(this);
                                        return typeof filename === 'string' ? filename : 'chart-export';
                                    } catch (error) {
                                        console.warn('Error getting filename, using default:', error);
                                        return 'chart-export';
                                    }
                                };
                            }
                        }
                        console.log('✅ Export modules error handling applied');
                    }
                });
            });
        });
    }

    // Load export modules
    loadExportModules();
})();