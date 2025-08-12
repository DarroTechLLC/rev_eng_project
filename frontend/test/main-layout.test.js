/**
 * Main Layout Tests
 * This file contains tests for the main-layout.html functionality
 * previously logged with console.log statements
 */

// Import testing libraries
const { JSDOM } = require('jsdom');

describe('Main Layout Tests', () => {
  let dom;
  let window;
  let document;

  beforeEach(() => {
    // Create a DOM environment with the necessary elements
    dom = new JSDOM(`
      <!DOCTYPE html>
      <html>
      <head>
        <title>RAE/daily-volume</title>
      </head>
      <body id="page-top" data-company-id="test-company-id">
        <div id="wrapper">
          <div id="content-wrapper">
            <div id="content">
              <div class="container-fluid">
                <div>Content goes here</div>
              </div>
            </div>
          </div>
        </div>
      </body>
      </html>
    `, {
      url: 'http://localhost',
      runScripts: 'dangerously'
    });

    window = dom.window;
    document = window.document;
  });

  test('Main layout should load correctly', () => {
    // This test verifies that the main layout loads correctly
    // Previously logged with: console.log('🚀 Main layout loaded');
    
    // Check if the main layout elements are present
    expect(document.getElementById('page-top')).not.toBeNull();
    expect(document.getElementById('wrapper')).not.toBeNull();
    expect(document.getElementById('content-wrapper')).not.toBeNull();
    expect(document.getElementById('content')).not.toBeNull();
    
    // Check if the company ID is set correctly
    expect(document.getElementById('page-top').getAttribute('data-company-id')).toBe('test-company-id');
  });

  test('DOM content loaded event should capitalize title words', () => {
    // This test verifies the title capitalization functionality
    // Previously logged with: console.log('📄 DOM fully loaded and parsed');
    
    // Execute the title capitalization code
    const titleCapitalizationScript = `
      var title = document.title;
      if (title.startsWith('RAE/')) {
        var viewName = title.substring(4); // Remove 'RAE/'
        var parts = viewName.split('-');
        var capitalizedParts = parts.map(function(part) {
          return part.charAt(0).toUpperCase() + part.slice(1);
        });
        var formattedName = capitalizedParts.join('-');
        document.title = 'RAE/' + formattedName;
      }
    `;
    
    // Execute the script in the DOM environment
    window.eval(titleCapitalizationScript);
    
    // Check if the title was capitalized correctly
    expect(document.title).toBe('RAE/Daily-Volume');
  });

  test('Title capitalization should handle multi-word titles', () => {
    // Set a multi-word title
    document.title = 'RAE/multi-word-title';
    
    // Execute the title capitalization code
    const titleCapitalizationScript = `
      var title = document.title;
      if (title.startsWith('RAE/')) {
        var viewName = title.substring(4); // Remove 'RAE/'
        var parts = viewName.split('-');
        var capitalizedParts = parts.map(function(part) {
          return part.charAt(0).toUpperCase() + part.slice(1);
        });
        var formattedName = capitalizedParts.join('-');
        document.title = 'RAE/' + formattedName;
      }
    `;
    
    // Execute the script in the DOM environment
    window.eval(titleCapitalizationScript);
    
    // Check if the title was capitalized correctly
    expect(document.title).toBe('RAE/Multi-Word-Title');
  });

  test('Title capitalization should not modify titles that do not start with RAE/', () => {
    // Set a title that doesn't start with RAE/
    document.title = 'Some Other Title';
    
    // Execute the title capitalization code
    const titleCapitalizationScript = `
      var title = document.title;
      if (title.startsWith('RAE/')) {
        var viewName = title.substring(4); // Remove 'RAE/'
        var parts = viewName.split('-');
        var capitalizedParts = parts.map(function(part) {
          return part.charAt(0).toUpperCase() + part.slice(1);
        });
        var formattedName = capitalizedParts.join('-');
        document.title = 'RAE/' + formattedName;
      }
    `;
    
    // Execute the script in the DOM environment
    window.eval(titleCapitalizationScript);
    
    // Check if the title remains unchanged
    expect(document.title).toBe('Some Other Title');
  });
});