/**
 * Company Selector Enhancement
 * Ensures consistent company display without blinking during navigation
 * Based on the same pattern as the Farm Selector
 */

// Create a self-executing function to avoid global scope pollution
(function($) {
    'use strict';

    // Store references to important elements and state
    let isNavigating = false;
    let lastSelectedCompanyId = null;
    let cachedCompanyData = null;

    // Function to get the server-side company selection
    async function getServerCompanySelection() {
        try {
            console.log('🔍 Checking server for company selection');
            const response = await fetch('/api/companies', {
                method: 'GET',
                credentials: 'same-origin'
            });

            if (!response.ok) {
                throw new Error('Failed to get company selection from server');
            }

            const result = await response.json();
            console.log('🔄 Server company selection result:', result);

            if (result.success && result.companies && result.companies.length > 0) {
                // Cache the company data
                cachedCompanyData = result;

                // Return the selected company ID
                return result.selectedCompanyId || result.companies[0].companyId;
            }

            return null;
        } catch (error) {
            console.error('❌ Error getting company selection from server:', error);
            return null;
        }
    }

    // Function to display company logo
    function displayCompanyLogo(container, company, logoOnly = false) {
        container.innerHTML = '';

        if (company.logoUrl) {
            const logo = document.createElement('img');
            logo.src = company.logoUrl;
            logo.alt = company.companyName;
            logo.className = logoOnly ? 'company-only-logo' : 'company-logo';
            logo.title = company.companyName;
            container.appendChild(logo);
        } else {
            // If no logo, show a placeholder or initials
            const placeholder = document.createElement('div');
            placeholder.className = 'company-logo-placeholder';
            placeholder.textContent = company.displayName || company.companyName.charAt(0);
            placeholder.title = company.companyName;
            container.appendChild(placeholder);
        }

        // Only add the name if not in logoOnly mode
        if (!logoOnly) {
            const nameSpan = document.createElement('span');
            nameSpan.textContent = company.companyName;
            nameSpan.className = 'company-name';
            container.appendChild(nameSpan);
        }
    }

    // Function to update the company selector UI
    function updateCompanySelectorUI(data) {
        console.log('📊 Updating company selector UI with data:', data);

        // Get references to the elements
        const companyList = document.getElementById('companyList');
        const currentDisplay = document.getElementById('currentCompanyDisplay');

        if (!companyList || !currentDisplay) {
            console.warn('⚠️ Company selector elements not found in DOM');
            return;
        }

        // Clear loading messages
        companyList.innerHTML = '';

        if (data.success && data.companies && data.companies.length > 0) {
            // Find selected company
            const selectedCompany = data.companies.find(c => c.companyId === data.selectedCompanyId) || data.companies[0];
            console.log('🏢 Selected company:', selectedCompany);

            // Add company slug as data attribute
            currentDisplay.setAttribute('data-company-slug', 
                selectedCompany.companyName.toLowerCase().replace(/\s+/g, '-'));
            console.log('🔗 Company slug:', currentDisplay.getAttribute('data-company-slug'));

            // Update current selection display - logo only
            displayCompanyLogo(currentDisplay, selectedCompany, true);

            // Create dropdown items
            data.companies.forEach(company => {
                const item = document.createElement('div');
                item.className = 'dropdown-item logo-wrapper d-flex align-items-center justify-content-center';

                // Highlight selected company
                if (data.selectedCompanyId === company.companyId) {
                    item.classList.add('active');
                }

                // Create logo display - logo only
                displayCompanyLogo(item, company, true);

                // Add click handler
                item.addEventListener('click', function(e) {
                    e.preventDefault();
                    e.stopPropagation();
                    selectCompany(company.companyId, company.companyName);
                });

                companyList.appendChild(item);
            });
        } else {
            console.warn('⚠️ No companies found in data:', data);
            companyList.innerHTML = '<div class="text-center p-2">No companies available</div>';
        }
    }

    // Helper function to intelligently update URL when company changes
    function updateUrlForCompanyChange(companySlug) {
        // Get current URL path segments
        const path = window.location.pathname;
        const pathSegments = path.split('/').filter(segment => segment.length > 0);
        const queryParams = window.location.search;

        console.log('🔍 Current path segments:', pathSegments);
        console.log('🔗 Current query params:', queryParams);

        // Special handling for known problematic companies
        if (companySlug.toLowerCase() === 'tuls' || 
            companySlug.toLowerCase() === 'texhoma' ||
            companySlug.toLowerCase() === 'vendor') {
            console.log('⚠️ Special handling for company: ' + companySlug);

            // Always redirect to dashboard for these companies
            const safeUrl = `/${companySlug.toLowerCase()}/dashboard/daily-volume`;
            console.log(`🛡️ Using safe URL for special company: ${safeUrl}`);
            return safeUrl;
        }

        // Check if this is an admin path
        if (pathSegments.length > 0 && pathSegments[0] === 'admin') {
            console.log('👨‍💼 Admin page detected, keeping admin context');
            // For admin pages, we maintain the current URL but refresh to apply the session change
            console.log('🔄 Admin route - refreshing page to maintain state');
            return window.location.href; // Return current URL to maintain query params
        }

        // If we're on a regular company page
        let newUrl = '';

        if (pathSegments.length > 0) {
            // Handle analytics routes - maintain the analytics page for the new company
            if (pathSegments.includes('analytics') && pathSegments.length > 2) {
                const analyticsType = pathSegments[2]; // e.g., 'trend-analysis', 'anomaly-detection', 'production-forecasting'
                newUrl = `/${companySlug}/analytics/${analyticsType}`;
                console.log(`📊 Analytics page detected - maintaining analytics type: ${newUrl}`);
            } else if (pathSegments.includes('projects')) {
                // Always redirect to the company's projects root when in a project view
                // This ensures we get the correct default farm for the new company
                newUrl = `/${companySlug}/projects`;
                console.log(`🔄 Project page detected - redirecting to company projects root: ${newUrl}`);
                console.log(`ℹ️ The server will select a default farm for company: ${companySlug}`);
            } else if (pathSegments.length > 1 && pathSegments[1] === 'dashboard' && pathSegments.length > 2) {
                // For dashboard pages with a specific view, maintain that view
                newUrl = `/${companySlug}/dashboard/${pathSegments[2]}`;
                console.log(`🔄 Dashboard with view - maintaining view: ${newUrl}`);
            } else {
                // Default to daily-volume dashboard for all other cases
                newUrl = `/${companySlug}/dashboard/daily-volume`;
                console.log(`🔄 Using default dashboard: ${newUrl}`);
            }
        } else {
            // If we're on the root page, go to the company's dashboard
            newUrl = `/${companySlug}/dashboard/daily-volume`;
            console.log(`🔄 Root page - redirecting to company dashboard: ${newUrl}`);
        }

        // Keep any query parameters
        if (queryParams) {
            newUrl += queryParams;
            console.log(`🔗 Added query params to URL: ${newUrl}`);
        }

        console.log(`🚀 Final navigation: ${path} → ${newUrl}`);
        return newUrl;
    }

    // Function to select company
    function selectCompany(companyId, companyName) {
        if (isNavigating) {
            console.warn('🚫 Navigation in progress, ignoring');
            return false;
        }

        try {
            isNavigating = true;

            // Close dropdown first
            document.querySelector('.dropdown-content').classList.remove('show');

            // Show loading state
            const currentDisplay = document.getElementById('currentCompanyDisplay');
            const originalContent = currentDisplay.innerHTML;
            currentDisplay.innerHTML = '<div class="d-flex align-items-center"><div class="spinner-border spinner-border-sm text-primary mr-2" role="status"></div></div>';

            console.log('🏢 Selecting company:', companyId, companyName);

            // Get CSRF token from meta tag
            const csrfToken = document.querySelector('meta[name="_csrf"]')?.getAttribute('content');
            const csrfHeader = document.querySelector('meta[name="_csrf_header"]')?.getAttribute('content');

            // Prepare headers
            const headers = {
                'Content-Type': 'application/json'
            };

            // Add CSRF token if available
            if (csrfToken && csrfHeader) {
                headers[csrfHeader] = csrfToken;
            }

            // Store the selection in localStorage
            localStorage.setItem('selectedCompanyId', companyId);
            localStorage.setItem('selectedCompanyName', companyName);

            // Use the companies-all endpoint which is guaranteed to work
            fetch('/api/companies-all/select/' + companyId, {
                method: 'POST',
                headers: headers,
                credentials: 'same-origin'
            })
            .then(response => {
                console.log('📊 Company selection response status:', response.status);
                if (!response.ok) {
                    return response.json().then(data => {
                        console.error('❌ API error response:', data);
                        throw new Error(data.message || `Error ${response.status}: Failed to switch company`);
                    });
                }
                return response.json();
            })
            .then(data => {
                console.log('✅ Company selection response:', data);
                if (data.success) {
                    // Dispatch custom event for other components
                    const companySelectedEvent = new CustomEvent('companySelected', {
                        detail: {
                            companyId: companyId,
                            companyName: companyName,
                            logoUrl: data.logoUrl || ''
                        },
                        bubbles: true
                    });
                    document.dispatchEvent(companySelectedEvent);
                    console.log('📢 Dispatched companySelected event');

                    // Create a slug format of the company name for URL
                    const companySlug = companyName.toLowerCase().replace(/\s+/g, '-');

                    // Determine the new URL based on current location
                    const newUrl = updateUrlForCompanyChange(companySlug);

                    // Navigate to the new URL
                    window.location.href = newUrl;
                } else {
                    // Restore original display
                    currentDisplay.innerHTML = originalContent;
                    console.error('❌ Failed to switch company:', data.message);
                    isNavigating = false;
                }
            })
            .catch(error => {
                // Restore original display
                currentDisplay.innerHTML = originalContent;
                console.error('❌ Error switching company:', error);
                isNavigating = false;
            });

            return true;
        } catch (error) {
            console.error('❌ Error in company selection:', error);
            isNavigating = false;
            return false;
        }
    }

    // Function to restore company selector from cache
    function restoreFromCache() {
        console.log('🔄 Attempting to restore company selector from cache');

        // Try to get cached data from localStorage
        const cachedData = localStorage.getItem('cachedCompanyData');
        const timestamp = localStorage.getItem('cachedCompanyDataTimestamp');
        const selectedCompanyId = localStorage.getItem('selectedCompanyId');

        // Check if cache is valid (less than 1 hour old)
        const now = new Date().getTime();
        const cacheAge = timestamp ? now - parseInt(timestamp) : Infinity;
        const cacheValid = cacheAge < 3600000; // 1 hour in milliseconds

        if (cachedData && cacheValid) {
            try {
                const data = JSON.parse(cachedData);
                console.log('💾 Restored company data from cache:', data);

                // If we have a selected company ID, make sure it's reflected in the data
                if (selectedCompanyId) {
                    data.selectedCompanyId = selectedCompanyId;
                }

                // Update the UI with cached data
                updateCompanySelectorUI(data);

                // Store the last selected company ID
                lastSelectedCompanyId = data.selectedCompanyId;

                return true;
            } catch (error) {
                console.error('❌ Error parsing cached company data:', error);
            }
        } else {
            console.log('⚠️ No valid cache found or cache expired');
        }

        return false;
    }

    // Function to load companies from server
    async function loadCompanies() {
        console.log('📊 Loading companies from server...');

        try {
            // Get company data from server
            const serverCompanyId = await getServerCompanySelection();
            console.log('🌐 Server company selection:', serverCompanyId);

            // If we have cached data, use it to update the UI
            if (cachedCompanyData) {
                console.log('💾 Using cached company data from API call');

                // Store in localStorage for future page loads
                localStorage.setItem('cachedCompanyData', JSON.stringify(cachedCompanyData));
                localStorage.setItem('cachedCompanyDataTimestamp', new Date().getTime().toString());

                // Update the UI
                updateCompanySelectorUI(cachedCompanyData);

                // Store the last selected company ID
                lastSelectedCompanyId = cachedCompanyData.selectedCompanyId;

                return;
            }

            // If no cached data, make a direct API call
            console.log('🔄 No cached data, fetching from API');

            // Show loading state
            const companyList = document.getElementById('companyList');
            const currentDisplay = document.getElementById('currentCompanyDisplay');

            if (companyList && currentDisplay.innerHTML.trim() === '') {
                companyList.innerHTML = '<div class="text-center p-2">Loading companies...</div>';
                currentDisplay.innerHTML = '<div class="d-flex align-items-center"><div class="spinner-border spinner-border-sm text-primary mr-2" role="status"></div></div>';
            }

            const response = await fetch('/api/companies');

            if (!response.ok) {
                throw new Error(`Error ${response.status}: Could not load companies`);
            }

            const data = await response.json();
            console.log('✅ Companies loaded successfully:', data);

            // Cache the data
            localStorage.setItem('cachedCompanyData', JSON.stringify(data));
            localStorage.setItem('cachedCompanyDataTimestamp', new Date().getTime().toString());

            // Update the UI
            updateCompanySelectorUI(data);

            // Store the last selected company ID
            lastSelectedCompanyId = data.selectedCompanyId;

        } catch (error) {
            console.error('❌ Error loading companies:', error);

            // Show error state
            const companyList = document.getElementById('companyList');
            const currentDisplay = document.getElementById('currentCompanyDisplay');

            if (companyList) {
                companyList.innerHTML = '<div class="text-danger p-2">Failed to load companies</div>';
            }

            if (currentDisplay && currentDisplay.innerHTML.includes('spinner')) {
                currentDisplay.innerHTML = '<span class="company-name text-danger">Error loading companies</span>';
            }
        }
    }

    // No visibility functions needed - CSS is in the HTML

    // Initialize company selector
    function initializeCompanySelector() {
        console.group('🏢 Company Selector Initialization');

        try {
            // Try to restore from cache first
            const restored = restoreFromCache();

            // Then load from server (will update if needed)
            loadCompanies();

            console.log('✅ Company selector initialized');
        } catch (error) {
            console.error('❌ Error initializing company selector:', error);
        } finally {
            console.groupEnd();
        }
    }

    // Initialize when DOM is ready
    $(document).ready(function() {
        console.log('🚀 DOM ready, initializing company selector...');
        // Wait for other scripts to initialize
        setTimeout(function() {
            initializeCompanySelector();
        }, 500); // Same timeout as farm selector
    });

})(jQuery);
