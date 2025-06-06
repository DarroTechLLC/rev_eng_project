/**
 * Highcharts Utility Functions
 * Provides common functionality for Highcharts charts
 */

/**
 * Format numbers with thousands separators
 * @param {number} number - The number to format
 * @param {number} decimals - Number of decimal places
 * @param {string} decPoint - Decimal point character
 * @param {string} thousandsSep - Thousands separator character
 * @returns {string} Formatted number
 */
function formatNumber(number, decimals = 0, decPoint = '.', thousandsSep = ',') {
    const n = !isFinite(+number) ? 0 : +number;
    const prec = !isFinite(+decimals) ? 0 : Math.abs(decimals);
    
    // Fix for IE parseFloat(0.55).toFixed(0) = 0;
    const toFixedFix = function(n, prec) {
        const k = Math.pow(10, prec);
        return '' + Math.round(n * k) / k;
    };
    
    // Format the number
    let s = (prec ? toFixedFix(n, prec) : '' + Math.round(n)).split('.');
    if (s[0].length > 3) {
        s[0] = s[0].replace(/\B(?=(?:\d{3})+(?!\d))/g, thousandsSep);
    }
    if ((s[1] || '').length < prec) {
        s[1] = s[1] || '';
        s[1] += new Array(prec - s[1].length + 1).join('0');
    }
    return s.join(decPoint);
}

/**
 * Convert hex color to rgba
 * @param {string} hex - Hex color code
 * @param {number} alpha - Alpha value (0-1)
 * @returns {string} RGBA color string
 */
function hexToRgba(hex, alpha) {
    if (!hex) return 'rgba(0, 0, 0, ' + alpha + ')';
    
    // Remove # if present
    hex = hex.replace('#', '');
    
    // Convert 3-digit hex to 6-digit
    if (hex.length === 3) {
        hex = hex[0] + hex[0] + hex[1] + hex[1] + hex[2] + hex[2];
    }
    
    // Parse the hex values
    const r = parseInt(hex.substring(0, 2), 16);
    const g = parseInt(hex.substring(2, 4), 16);
    const b = parseInt(hex.substring(4, 6), 16);
    
    return 'rgba(' + r + ', ' + g + ', ' + b + ', ' + alpha + ')';
}

/**
 * Generate a random color
 * @returns {string} Hex color code
 */
function getRandomColor() {
    const letters = '0123456789ABCDEF';
    let color = '#';
    for (let i = 0; i < 6; i++) {
        color += letters[Math.floor(Math.random() * 16)];
    }
    return color;
}

/**
 * Create a Highcharts tooltip formatter
 * @param {string} unit - Unit to display (e.g., 'MMBTUs')
 * @returns {Function} Tooltip formatter function
 */
function createTooltipFormatter(unit) {
    return function() {
        return `<b>${this.series.name}</b><br/>
                ${this.key}: ${Highcharts.numberFormat(this.y, 0)} ${unit}`;
    };
}

/**
 * Create a Highcharts title with total
 * @param {string} title - Chart title
 * @param {number} total - Total value to display
 * @param {string} unit - Unit to display (e.g., 'MMBTUs')
 * @returns {Object} Highcharts title configuration
 */
function createTitleWithTotal(title, total, unit) {
    return {
        text: `${title}: (${Math.round(total).toLocaleString()} ${unit})`,
        style: {
            fontSize: '16px',
            fontWeight: 'bold'
        },
        margin: 20
    };
}