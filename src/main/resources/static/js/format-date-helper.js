/**
 * Format a date string consistently across the application
 * Handles both string dates and numeric timestamps
 * 
 * @param {string|number} dateString - The date string or timestamp to format
 * @returns {string} The formatted date string
 */
function formatDateHelper(dateString) {
    if (!dateString) return '';
    
    // Convert seconds to milliseconds by multiplying by 1000 if it's a number
    const timestamp = typeof dateString === 'number' ? dateString * 1000 : dateString;
    const date = new Date(timestamp);
    return date.toLocaleString();
}

// Make the function available globally
if (typeof window !== 'undefined') {
    window.formatDateHelper = formatDateHelper;
}