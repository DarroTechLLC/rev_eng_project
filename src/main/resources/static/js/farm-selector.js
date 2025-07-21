/**
 * Farm Selector Enhancement
 * Ensures consistent farm navigation using IDs instead of names
 */

// Create a self-executing function to avoid global scope pollution
(function($) {
    'use strict';

    // Store references to important elements and state
    let isNavigating = false;
    let lastSelectedFarmId = null;

    // Function to get the server-side farm selection
    async function getServerFarmSelection() {
        try {
            console.log('🔍 Checking server for farm selection');
            const response = await fetch('/api/farms/selected', {
                method: 'GET',
                credentials: 'same-origin'
            });

            if (!response.ok) {
                throw new Error('Failed to get farm selection from server');
            }

            const result = await response.json();
            console.log('🔄 Server farm selection result:', result);

            if (result.success && result.selected && result.farm) {
                return result.farm.id;
            }

            return null;
        } catch (error) {
            console.error('❌ Error getting farm selection from server:', error);
            return null;
        }
    }

    // Function to sync farm selection with server
    async function syncFarmSelection(farmId) {
        try {
            const response = await fetch('/api/farms/select', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify({ farmId }),
                credentials: 'same-origin'
            });

            if (!response.ok) {
                throw new Error('Failed to sync farm selection');
            }

            const result = await response.json();
            console.log('🔄 Farm selection synced with server:', result);
            return result.success;
        } catch (error) {
            console.error('❌ Error syncing farm selection:', error);
            return false;
        }
    }

    // Function to handle farm selection changes
    async function handleFarmSelection(event) {
        console.group('👆 Farm Selection Change');
        console.log('Event triggered:', {
            type: event.type,
            target: event.currentTarget.id,
            value: $(event.currentTarget).val()
        });

        if (isNavigating) {
            console.warn('🚫 Navigation in progress, ignoring');
            console.groupEnd();
            return false;
        }

        try {
            isNavigating = true;
            const $select = $(event.currentTarget);
            const newFarmId = $select.val();
            const farmName = $select.find('option:selected').text();

            console.log('Selection details:', {
                farmId: newFarmId,
                farmName: farmName,
                previousId: lastSelectedFarmId
            });

            if (!newFarmId) {
                console.warn('⚠️ No farm selected');
                return false;
            }

            if (newFarmId === lastSelectedFarmId) {
                console.log('🔄 Same farm selected, no action needed');
                return false;
            }

            // Get company slug for company-specific storage
            const companySlug = $('#currentCompanyDisplay').data('company-slug') || 
                              window.location.pathname.split('/')[1];

            // Create company-specific localStorage key
            const storageKey = `selectedFarmKey_${companySlug}`;

            // First sync with server
            const synced = await syncFarmSelection(newFarmId);
            if (!synced) {
                throw new Error('Failed to sync farm selection with server');
            }

            // Then update localStorage with company-specific key
            localStorage.setItem(storageKey, newFarmId);
            console.log(`💾 Stored new farm ID for company ${companySlug}:`, newFarmId);

            // Update all other selectors
            $('#farmSelector').not($select).val(newFarmId);

            // Update project links if in projects submenu
            if ($select.closest('#projectsSubmenu').length) {
                updateProjectLinks(newFarmId);
            }

            // Handle forecast if needed
            if ($select.closest('#forecastControls').length && typeof generateForecast === 'function') {
                generateForecast();
            }

            lastSelectedFarmId = newFarmId;
            console.log('✅ Farm selection completed');

            // Reload the page to ensure everything is in sync
            window.location.reload();

            return true;
        } catch (error) {
            console.error('❌ Error in farm selection:', error);
            return false;
        } finally {
            isNavigating = false;
            console.groupEnd();
        }
    }

    // Function to update project links
    function updateProjectLinks(farmId) {
        console.group('🔗 Updating Project Links');
        try {
            const $farmSelector = $('#farmSelector');
            const farmName = $farmSelector.find('option:selected').text().toLowerCase().replace(/\s+/g, '-');
            const companySlug = $('#currentCompanyDisplay').data('company-slug') || 
                              window.location.pathname.split('/')[1];

            if (!companySlug) {
                throw new Error('Company slug not found');
            }

            console.log('Update parameters:', {
                farmId: farmId,
                farmName: farmName,
                companySlug: companySlug
            });

            $('#projectsSubmenu .collapse-item').each(function() {
                const $link = $(this);
                const currentPath = $link.attr('href');
                const pathParts = currentPath.split('/');
                const lastPart = pathParts[pathParts.length - 1];

                const newPath = `/${companySlug}/projects/${farmName}/${lastPart}`;
                $link.attr('href', newPath);

                console.log(`Updated link: ${currentPath} → ${newPath}`);
            });

            console.log('✅ Project links updated');
        } catch (error) {
            console.error('❌ Error updating project links:', error);
        } finally {
            console.groupEnd();
        }
    }

    // Initialize farm selectors
    async function initializeFarmSelectors() {
        console.group('🚜 Farm Selector Initialization');

        try {
            // Find all farm selectors
            const $selectors = $('#farmSelector');
            console.log(`Found ${$selectors.length} farm selector(s)`);

            if ($selectors.length === 0) {
                console.warn('⚠️ No farm selectors found');
                return;
            }

            // Remove existing event handlers
            $selectors.off('change.farmSelector');

            // Get company slug for company-specific storage
            const companySlug = $('#currentCompanyDisplay').data('company-slug') || 
                              window.location.pathname.split('/')[1];

            // Create company-specific localStorage key
            const storageKey = `selectedFarmKey_${companySlug}`;
            console.log('🔑 Using company-specific storage key:', storageKey);

            // First check server for farm selection
            const serverFarmId = await getServerFarmSelection();
            console.log('🌐 Server farm selection:', serverFarmId);

            // Then check localStorage as fallback
            const storedFarmId = localStorage.getItem(storageKey);
            console.log('💾 Retrieved stored farm ID from localStorage:', storedFarmId);

            // Determine which farm ID to use (server takes precedence)
            let farmIdToUse = serverFarmId || storedFarmId;

            // If we have a farm ID from localStorage but not from server, sync it with server
            if (!serverFarmId && storedFarmId) {
                console.log('🔄 Syncing localStorage farm ID with server:', storedFarmId);
                await syncFarmSelection(storedFarmId);
            }

            // If we have a farm ID from server but not in localStorage, update localStorage
            if (serverFarmId && serverFarmId !== storedFarmId) {
                console.log('💾 Updating localStorage with server farm ID:', serverFarmId);
                localStorage.setItem(storageKey, serverFarmId);
            }

            // Initialize each selector
            $selectors.each(function(index) {
                const $select = $(this);
                console.log(`Initializing selector #${index + 1}:`, {
                    id: this.id,
                    value: $select.val(),
                    options: $select.find('option').length
                });

                // Set initial value based on our determined farm ID
                if (farmIdToUse) {
                    $select.val(farmIdToUse);
                    lastSelectedFarmId = farmIdToUse;
                } else if ($select.val()) {
                    // If no stored value but selector has a value (from server-side rendering),
                    // save that value to both server and localStorage
                    lastSelectedFarmId = $select.val();
                    localStorage.setItem(storageKey, lastSelectedFarmId);
                    syncFarmSelection(lastSelectedFarmId);
                    console.log('🔄 Using server-rendered value and syncing:', lastSelectedFarmId);
                }

                // Attach change handler
                $select.on('change.farmSelector', handleFarmSelection);
            });

            console.log('✅ Farm selectors initialized');
        } catch (error) {
            console.error('❌ Error initializing farm selectors:', error);
        } finally {
            console.groupEnd();
        }
    }

    // Initialize when DOM is ready
    $(document).ready(function() {
        console.log('🚀 DOM ready, initializing farm selector...');
        // Wait for other scripts to initialize
        setTimeout(async function() {
            await initializeFarmSelectors();
        }, 500);
    });

})(jQuery); 
