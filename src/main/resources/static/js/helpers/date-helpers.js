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
  const yyyy = date.getUTCFullYear();
  const mm = (date.getUTCMonth() + 1).toString().padStart(2, '0');
  const dd = date.getUTCDate().toString().padStart(2, '0');

  return `${yyyy}-${mm}-${dd}`;
}

/**
 * Validates a date string in YYYY-MM-DD format
 * @param {string} dateString - The date string to validate
 * @returns {boolean} True if the date is valid, false otherwise
 */
const isValidDateString = (dateString) => {
  if (!dateString) return false;

  // Check format (YYYY-MM-DD)
  if (!/^\d{4}-\d{2}-\d{2}$/.test(dateString)) return false;

  // Parse the date parts
  const [year, month, day] = dateString.split('-').map(Number);

  // Create a date using UTC to avoid timezone issues
  const date = new Date(Date.UTC(year, month - 1, day));

  // Check if it's a valid date
  if (isNaN(date.getTime())) return false;

  // Check if the date parts match what we expect
  // This catches invalid dates like 2023-02-31
  return date.getUTCFullYear() === year && 
         date.getUTCMonth() === month - 1 && 
         date.getUTCDate() === day;
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
  const now = new Date();
  const year = now.getFullYear();
  const month = (now.getMonth() + 1).toString().padStart(2, '0');
  const day = now.getDate().toString().padStart(2, '0');
  return `${year}-${month}-${day}`;
}

/**
 * Gets yesterday's date as a YYYY-MM-DD string
 * @returns {string} Yesterday's date as YYYY-MM-DD
 */
const getYesterdaysDateString = () => {
  const now = new Date();
  const yesterday = new Date(
    now.getFullYear(),
    now.getMonth(),
    now.getDate() - 1
  );
  return `${yesterday.getFullYear()}-${(yesterday.getMonth() + 1).toString().padStart(2, '0')}-${yesterday.getDate().toString().padStart(2, '0')}`;
}

// Export functions for use in other files
if (typeof module !== 'undefined' && module.exports) {
  // For Node.js environment
  module.exports = {
    toYyyyMmDdString,
    isValidDateString,
    getCurrentDate,
    getYesterdaysDate,
    getCurrentDateString,
    getYesterdaysDateString
  };
} else {
  // For browser environment
  window.dateHelpers = {
    toYyyyMmDdString,
    isValidDateString,
    getCurrentDate,
    getYesterdaysDate,
    getCurrentDateString,
    getYesterdaysDateString
  };
}
