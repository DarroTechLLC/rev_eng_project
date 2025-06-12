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

            // First sync with server
            const synced = await syncFarmSelection(newFarmId);
            if (!synced) {
                throw new Error('Failed to sync farm selection with server');
            }

            // Then update localStorage
            localStorage.setItem('selectedFarmKey', newFarmId);
            console.log('💾 Stored new farm ID:', newFarmId);

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
    function initializeFarmSelectors() {
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

            // Get stored farm ID
            const storedFarmId = localStorage.getItem('selectedFarmKey');
            console.log('💾 Retrieved stored farm ID:', storedFarmId);

            // Initialize each selector
            $selectors.each(function(index) {
                const $select = $(this);
                console.log(`Initializing selector #${index + 1}:`, {
                    id: this.id,
                    value: $select.val(),
                    options: $select.find('option').length
                });

                // Set initial value if stored
                if (storedFarmId) {
                    $select.val(storedFarmId);
                    lastSelectedFarmId = storedFarmId;
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
        setTimeout(initializeFarmSelectors, 500);
    });

})(jQuery); 