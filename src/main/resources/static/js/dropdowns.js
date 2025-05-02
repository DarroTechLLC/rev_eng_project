/**
 * Dropdown functionality for the application
 * - User dropdown menu
 * - Company selector dropdown
 */
document.addEventListener('DOMContentLoaded', function() {
    console.log('🔄 Initializing dropdowns.js');
    initAllDropdowns();
    initRoleBasedMenu();
});

/**
 * Initialize all dropdowns with a consistent approach
 */
function initAllDropdowns() {
    console.log('🔽 Initializing all dropdowns');
    
    // Add consistent styles for all dropdowns
    addDropdownStyles();
    
    // Initialize user dropdown
    initUserDropdown();
    
    // Initialize company selector with Bootstrap compatibility
    initCompanySelector();
    
    // Global handler to close dropdowns when clicking outside
    document.addEventListener('click', function(e) {
        // Only process if no dropdown toggle was clicked
        if (!e.target.closest('[data-toggle="dropdown"]') && 
            !e.target.closest('.dropdown-toggle')) {
            
            // Close all open dropdowns
            document.querySelectorAll('.dropdown-menu.show, .dropdown-content.show').forEach(function(menu) {
                menu.classList.remove('show');
            });
            
            console.log('🔒 All dropdowns closed (outside click)');
        }
    });
    
    console.log('✅ All dropdowns initialized');
}

/**
 * Add consistent styles for all dropdowns
 */
function addDropdownStyles() {
    const style = document.createElement('style');
    style.textContent = `
        /* Ensure consistent dropdown visibility */
        .dropdown-menu.show, 
        .dropdown-content.show {
            display: block !important;
            z-index: 1000 !important;
        }
        
        /* Smooth transitions for dropdowns */
        .dropdown-menu, 
        .dropdown-content {
            transition: opacity 0.2s, transform 0.2s;
            opacity: 0;
            transform: translateY(-10px);
            display: none;
        }
        
        .dropdown-menu.show,
        .dropdown-content.show {
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
    console.log('💅 Dropdown styles added');
}

/**
 * Initialize the user dropdown menu
 */
function initUserDropdown() {
    console.log('👤 Initializing user dropdown');
    const userDropdownToggle = document.querySelector('#userDropdown');
    const userDropdownMenu = document.querySelector('[aria-labelledby="userDropdown"]');
    
    if (!userDropdownToggle || !userDropdownMenu) {
        console.error('❌ User dropdown elements not found');
        return;
    }
    
    // Toggle dropdown when clicking on the user avatar
    userDropdownToggle.addEventListener('click', function(e) {
        e.preventDefault();
        e.stopPropagation();
        
        // Close any open company dropdown first
        document.querySelectorAll('.dropdown-content.show').forEach(function(el) {
            el.classList.remove('show');
        });
        
        // Toggle this dropdown
        userDropdownMenu.classList.toggle('show');
        console.log(`${userDropdownMenu.classList.contains('show') ? '🔓' : '🔒'} User dropdown ${userDropdownMenu.classList.contains('show') ? 'opened' : 'closed'}`);
    });
    
    console.log('✅ User dropdown initialized');
}

/**
 * Initialize the company selector dropdown to work with Bootstrap
 */
function initCompanySelector() {
    console.log('🏢 Initializing company selector with Bootstrap compatibility');
    
    const toggleButton = document.querySelector('.toggle-button');
    const dropdownContent = document.querySelector('.dropdown-content');
    const currentCompanyDisplay = document.getElementById('currentCompanyDisplay');
    
    if (!toggleButton || !dropdownContent) {
        console.error('❌ Company selector elements not found');
        return;
    }
    
    // Make the company selector toggle work like a Bootstrap dropdown
    toggleButton.setAttribute('data-toggle', 'dropdown');
    toggleButton.classList.add('dropdown-toggle');
    
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
        
        // Close any open user dropdown first
        document.querySelectorAll('.dropdown-menu.show').forEach(function(el) {
            if (el !== dropdownContent) {
                el.classList.remove('show');
            }
        });
        
        // Toggle company dropdown
        dropdownContent.classList.toggle('show');
        console.log(`${dropdownContent.classList.contains('show') ? '🔓' : '🔒'} Company selector ${dropdownContent.classList.contains('show') ? 'opened' : 'closed'}`);
    };
    
    // Both toggle button and company display should trigger the dropdown
    toggleButton.addEventListener('click', toggleCompanyDropdown);
    if (currentCompanyDisplay) {
        currentCompanyDisplay.addEventListener('click', toggleCompanyDropdown);
    }
    
    console.log('✅ Company selector initialized with Bootstrap compatibility');
}

/**
 * Initialize role-based menu visibility
 */
function initRoleBasedMenu() {
    console.log('🔑 Initializing role-based menu visibility');
    
    fetch('/api/user-roles/current')
        .then(response => {
            if (!response.ok) {
                throw new Error(`Error ${response.status}: ${response.statusText}`);
            }
            return response.json();
        })
        .then(data => {
            if (data.success) {
                console.log('🔑 User roles loaded:', data);
                
                // Show admin menu items if user is an admin
                if (data.isAdmin === true) {
                    document.querySelectorAll('.admin-only').forEach(function(el) {
                        el.style.display = 'block';
                    });
                    console.log('👔 Admin menu items enabled');
                }
                
                // Show super admin menu items if user is a super admin
                if (data.isSuperAdmin === true) {
                    document.querySelectorAll('.super-admin-only').forEach(function(el) {
                        el.style.display = 'block';
                    });
                    console.log('👑 Super Admin menu items enabled');
                }
            } else {
                console.warn('⚠️ Not logged in or error fetching roles');
            }
        })
        .catch(error => {
            console.error('❌ Error fetching role information:', error);
        });
    
    console.log('✅ Role-based menu initialized');
} 