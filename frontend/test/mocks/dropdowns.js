/**
 * Dropdown functionality for the application
 * - User dropdown menu
 * - Company selector dropdown
 */

// Initialize all dropdowns when the DOM is loaded
function initAllDropdowns() {
    // Add consistent styles for all dropdowns
    addDropdownStyles();

    // Initialize user dropdown
    initUserDropdown();

    // Initialize company selector with Bootstrap compatibility
    initCompanySelector();

    // Initialize all other dropdowns with data-toggle="dropdown"
    initOtherDropdowns();

    // Global handler to close dropdowns when clicking outside
    document.addEventListener('click', function(e) {
        // Only process if no dropdown toggle was clicked
        if (!e.target.closest('[data-toggle="dropdown"]') && 
            !e.target.closest('.dropdown-toggle')) {

            // Close all open dropdowns
            document.querySelectorAll('.dropdown-menu.show, .dropdown-content.show, .dropdown-list.show').forEach(function(menu) {
                menu.classList.remove('show');
            });
        }
    });
}

/**
 * Add consistent styles for all dropdowns
 */
function addDropdownStyles() {
    const style = document.createElement('style');
    style.textContent = `
        /* Ensure consistent dropdown visibility */
        .dropdown-menu.show, 
        .dropdown-content.show,
        .dropdown-list.show {
            display: block !important;
            z-index: 1000 !important;
        }

        /* Smooth transitions for dropdowns */
        .dropdown-menu, 
        .dropdown-content,
        .dropdown-list {
            transition: opacity 0.2s, transform 0.2s;
            opacity: 0;
            transform: translateY(-10px);
            display: none;
        }

        .dropdown-menu.show,
        .dropdown-content.show,
        .dropdown-list.show {
            opacity: 1;
            transform: translateY(0);
        }

        /* Better styling for dropdown items */
        .dropdown-item {
            padding: 0.5rem 1.5rem;
            transition: background-color 0.2s;
            cursor: pointer !important;
        }

        .dropdown-item:hover {
            background-color: #f8f9fa !important;
        }

        .dropdown-item.active {
            background-color: #e9ecef !important;
            font-weight: bold;
        }

        /* Fix for admin-only items */
        .admin-only, .super-admin-only {
            display: none;
        }
    `;
    document.head.appendChild(style);
}

/**
 * Initialize the user dropdown menu
 */
function initUserDropdown() {
    const userDropdownToggle = document.querySelector('#userDropdown');
    const userDropdownMenu = document.querySelector('[aria-labelledby="userDropdown"]');

    if (!userDropdownToggle || !userDropdownMenu) {
        return;
    }

    // Toggle dropdown when clicking on the user avatar
    userDropdownToggle.addEventListener('click', function(e) {
        e.preventDefault();
        e.stopPropagation();

        // Close any open company dropdown or other dropdowns first
        document.querySelectorAll('.dropdown-content.show, .dropdown-list.show').forEach(function(el) {
            el.classList.remove('show');
        });

        // Toggle this dropdown
        userDropdownMenu.classList.toggle('show');
    });
}

/**
 * Initialize the company selector dropdown to work with Bootstrap
 */
function initCompanySelector() {
    const dropdownContent = document.querySelector('.dropdown-content');
    const currentCompanyDisplay = document.getElementById('currentCompanyDisplay');

    if (!currentCompanyDisplay || !dropdownContent) {
        return;
    }

    // Make the company display toggle work like a Bootstrap dropdown
    currentCompanyDisplay.setAttribute('data-toggle', 'dropdown');
    // It should already have the dropdown-toggle class

    // Mark the parent for consistent styling
    const companySelector = document.querySelector('.company-selector');
    if (companySelector) {
        companySelector.classList.add('dropdown');
    }

    // Make the dropdown content work like a Bootstrap dropdown menu
    dropdownContent.classList.add('dropdown-menu');

    // Set up toggle handlers that prevent conflict with user dropdown
    const toggleCompanyDropdown = function(e) {
        if (e) {
            e.preventDefault();
            e.stopPropagation();
        }

        // Close any open user dropdown or other dropdowns first
        document.querySelectorAll('.dropdown-menu.show, .dropdown-list.show').forEach(function(el) {
            if (el !== dropdownContent) {
                el.classList.remove('show');
            }
        });

        // Toggle company dropdown
        dropdownContent.classList.toggle('show');
    };

    // Company display should trigger the dropdown
    if (currentCompanyDisplay) {
        currentCompanyDisplay.addEventListener('click', toggleCompanyDropdown);
    }
}

/**
 * Initialize all other dropdowns with data-toggle="dropdown"
 */
function initOtherDropdowns() {
    // Find all dropdown toggles that aren't already handled
    const dropdownToggles = document.querySelectorAll('[data-toggle="dropdown"]');

    dropdownToggles.forEach(function(toggle) {
        // Skip if it's the user dropdown or company selector which are handled separately
        if (toggle.id === 'userDropdown' || toggle.id === 'currentCompanyDisplay') {
            return;
        }

        const dropdownMenu = document.querySelector('[aria-labelledby="' + toggle.id + '"]');

        if (!dropdownMenu) {
            return;
        }

        // Set up toggle handler
        toggle.addEventListener('click', function(e) {
            e.preventDefault();
            e.stopPropagation();

            // Close any other open dropdowns
            document.querySelectorAll('.dropdown-menu.show, .dropdown-content.show, .dropdown-list.show').forEach(function(menu) {
                if (menu !== dropdownMenu) {
                    menu.classList.remove('show');
                }
            });

            // Toggle this dropdown
            dropdownMenu.classList.toggle('show');
        });
    });
}

/**
 * Initialize role-based menu visibility
 */
function initRoleBasedMenu() {
    fetch('/api/user-roles/current')
        .then(response => {
            if (!response.ok) {
                throw new Error(`Error ${response.status}: ${response.statusText}`);
            }
            return response.json();
        })
        .then(data => {
            if (data.success) {
                // Show admin menu items if user is an admin
                if (data.isAdmin === true) {
                    document.querySelectorAll('.admin-only').forEach(function(el) {
                        el.style.display = 'block';
                    });
                }

                // Show super admin menu items if user is a super admin
                if (data.isSuperAdmin === true) {
                    document.querySelectorAll('.super-admin-only').forEach(function(el) {
                        el.style.display = 'block';
                    });
                }
            }
        })
        .catch(error => {
            console.error('Error fetching role information:', error);
        });
}

// Initialize when the DOM is loaded
document.addEventListener('DOMContentLoaded', function() {
    initAllDropdowns();
    initRoleBasedMenu();
});

// Export for CommonJS
exports.initAllDropdowns = initAllDropdowns;
exports.initRoleBasedMenu = initRoleBasedMenu;
exports.initUserDropdown = initUserDropdown;
exports.initCompanySelector = initCompanySelector;
exports.initOtherDropdowns = initOtherDropdowns;