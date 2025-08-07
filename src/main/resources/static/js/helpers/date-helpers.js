/**
 * Date Helper Functions
 * Provides consistent date handling across the application
 * Based on the @roeslein project implementation
 */

/**
 * Formats a date as YYYY-MM-DD
 * @param {Date} date - The date to format
 * @returns {string} The formatted date string
 */
const toYyyyMmDdString = (date) => {
  const yyyy = date.getFullYear();
  const mm = (date.getMonth() + 1).toString().padStart(2, '0');
  const dd = date.getDate().toString().padStart(2, '0');

  return `${yyyy}-${mm}-${dd}`;
}

/**
 * Gets the current date
 * @returns {Date} The current date
 */
const getCurrentDate = () => {
  return new Date();
}

/**
 * Gets yesterday's date
 * @returns {Date} Yesterday's date
 */
const getYesterdaysDate = () => {
  let d = new Date();
  d.setDate(d.getDate() - 1);
  return d;
}

/**
 * Gets the current date as a YYYY-MM-DD string
 * @returns {string} The current date as YYYY-MM-DD
 */
const getCurrentDateString = () => {
  const today = new Date();
  return toYyyyMmDdString(today);
}

/**
 * Gets yesterday's date as a YYYY-MM-DD string
 * @returns {string} Yesterday's date as YYYY-MM-DD
 */
const getYesterdaysDateString = () => {
  return toYyyyMmDdString(getYesterdaysDate());
}

// Export functions for use in other files
if (typeof module !== 'undefined' && module.exports) {
  // For Node.js environment
  module.exports = {
    toYyyyMmDdString,
    getCurrentDate,
    getYesterdaysDate,
    getCurrentDateString,
    getYesterdaysDateString
  };
} else {
  // For browser environment
  window.dateHelpers = {
    toYyyyMmDdString,
    getCurrentDate,
    getYesterdaysDate,
    getCurrentDateString,
    getYesterdaysDateString
  };
}
