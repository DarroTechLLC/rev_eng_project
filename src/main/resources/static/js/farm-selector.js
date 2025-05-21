/**
 * Farm Selector Enhancement
 * Ensures consistent farm navigation using IDs instead of names
 */
document.addEventListener('DOMContentLoaded', function() {
    console.log('🚜 Farm selector enhancement loaded');
    
    const farmSelector = document.getElementById('farmSelector');
    if (!farmSelector) {
        console.warn('⚠️ Farm selector not found on this page');
        return;
    }
    
    // Attempt to select the farm from localStorage if available
    const storedFarmId = localStorage.getItem('selectedFarmKey');
    if (storedFarmId) {
        // Find the option with this farm ID
        const options = Array.from(farmSelector.options);
        const matchingOption = options.find(option => option.value === storedFarmId);
        
        if (matchingOption) {
            console.log(`🔄 Restoring selected farm from localStorage: ${matchingOption.text} (ID: ${storedFarmId})`);
            farmSelector.value = storedFarmId;
            
            // Update URL links if needed
            if (typeof updateProjectLinks === 'function') {
                updateProjectLinks(storedFarmId);
            }
        }
    }
    
    // Initialize correct project links based on currently selected farm
    if (typeof updateProjectLinks === 'function' && farmSelector.value) {
        console.log(`📋 Initializing project links with farm ID: ${farmSelector.value}`);
        updateProjectLinks(farmSelector.value);
    }
    
    // Add event listener to log farm selection for debugging
    farmSelector.addEventListener('change', function(event) {
        const selectedOption = farmSelector.options[farmSelector.selectedIndex];
        console.log(`👆 User selected farm: ${selectedOption.text} (ID: ${selectedOption.value})`);
    });
}); 