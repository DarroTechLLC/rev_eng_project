/**
 * Highcharts with Utils - Complete Highcharts Setup
 * Includes Highcharts library and utility functions for the application
 */

// Load Highcharts library safely
function loadHighcharts() {
    return new Promise((resolve, reject) => {
        // Check if Highcharts is already loaded
        if (typeof Highcharts !== 'undefined') {
            console.log('🚀 Highcharts already loaded');
            resolve();
            return;
        }

        // Load Highcharts library
        const script = document.createElement('script');
        script.src = 'https://cdn.jsdelivr.net/npm/highcharts@latest/highcharts.js';
        script.onload = () => {
            console.log('✅ Highcharts library loaded');
            
            // Load additional modules
            const modules = [
                'https://cdn.jsdelivr.net/npm/highcharts@latest/modules/exporting.js',
                'https://cdn.jsdelivr.net/npm/highcharts@latest/modules/export-data.js',
                'https://cdn.jsdelivr.net/npm/highcharts@latest/modules/accessibility.js'
            ];
            
            let loadedModules = 0;
            modules.forEach(moduleSrc => {
                const moduleScript = document.createElement('script');
                moduleScript.src = moduleSrc;
                moduleScript.onload = () => {
                    loadedModules++;
                    if (loadedModules === modules.length) {
                        console.log('✅ All Highcharts modules loaded');
                        resolve();
                    }
                };
                moduleScript.onerror = () => {
                    console.warn(`⚠️ Failed to load Highcharts module: ${moduleSrc}`);
                    loadedModules++;
                    if (loadedModules === modules.length) {
                        resolve();
                    }
                };
                document.head.appendChild(moduleScript);
            });
        };
        script.onerror = () => {
            console.error('❌ Failed to load Highcharts library');
            reject(new Error('Failed to load Highcharts'));
        };
        document.head.appendChild(script);
    });
}

// Load utility functions
function loadHighchartsUtils() {
    return new Promise((resolve, reject) => {
        const script = document.createElement('script');
        script.src = '/js/chart-utils/highcharts-utils.js';
        script.onload = () => {
            console.log('✅ Highcharts utilities loaded successfully');
            resolve();
        };
        script.onerror = () => {
            console.error('❌ Failed to load Highcharts utilities');
            reject(new Error('Failed to load Highcharts utilities'));
        };
        document.head.appendChild(script);
    });
}

// Initialize everything when DOM is ready
document.addEventListener('DOMContentLoaded', function() {
    console.log('🚀 Initializing Highcharts with Utils...');
    
    loadHighcharts()
        .then(() => loadHighchartsUtils())
        .then(() => {
            console.log('✅ Highcharts setup complete');
            
            // Apply theme if available
            if (typeof highchartsTheme !== 'undefined') {
                Highcharts.setOptions(highchartsTheme);
                console.log('🎨 Highcharts theme applied');
            }
            
            // Initialize error handling if available
            if (typeof initializeHighchartsWithErrorHandling === 'function') {
                initializeHighchartsWithErrorHandling();
                console.log('🛡️ Highcharts error handling initialized');
            }
        })
        .catch(error => {
            console.error('❌ Error setting up Highcharts:', error);
        });
}); 