/**
 * Icons for the application
 * This file contains SVG icons that can be used inline in HTML
 * to avoid external file requests
 */
document.addEventListener('DOMContentLoaded', function() {
    console.log('🎨 Initializing icons.js');
    replaceIconPlaceholders();
});

/**
 * Replace img tags with inline SVG icons
 */
function replaceIconPlaceholders() {
    // Find all image tags with src containing '/svg/'
    const iconImages = document.querySelectorAll('img[src*="/svg/"]');
    
    iconImages.forEach(function(img) {
        const iconName = img.src.split('/').pop().replace('.svg', '');
        const svgIcon = getSvgIcon(iconName);
        
        if (svgIcon) {
            // Create a new span to hold the SVG
            const iconSpan = document.createElement('span');
            iconSpan.className = 'svg-icon ' + (img.className || '');
            iconSpan.innerHTML = svgIcon;
            iconSpan.setAttribute('aria-hidden', 'true');
            
            // Copy important attributes
            if (img.alt) {
                iconSpan.setAttribute('title', img.alt);
            }
            
            // Replace the img with the span
            img.parentNode.replaceChild(iconSpan, img);
            console.log('🔄 Replaced icon image: ' + iconName);
        } else {
            console.warn('⚠️ Icon not found: ' + iconName);
        }
    });
    
    console.log('✅ Icons initialized');
}

/**
 * Get an SVG icon by name
 * @param {string} name - The name of the icon
 * @returns {string} The SVG markup
 */
function getSvgIcon(name) {
    const icons = {
        'down-arrow': '<svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M6 9l6 6 6-6"/></svg>',
        
        // Add more icons here as needed
        'user': '<svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"></path><circle cx="12" cy="7" r="4"></circle></svg>',
        'settings': '<svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="3"></circle><path d="M19.4 15a1.65 1.65 0 0 0 .33 1.82l.06.06a2 2 0 0 1 0 2.83 2 2 0 0 1-2.83 0l-.06-.06a1.65 1.65 0 0 0-1.82-.33 1.65 1.65 0 0 0-1 1.51V21a2 2 0 0 1-2 2 2 2 0 0 1-2-2v-.09A1.65 1.65 0 0 0 9 19.4a1.65 1.65 0 0 0-1.82.33l-.06.06a2 2 0 0 1-2.83 0 2 2 0 0 1 0-2.83l.06-.06a1.65 1.65 0 0 0 .33-1.82 1.65 1.65 0 0 0-1.51-1H3a2 2 0 0 1-2-2 2 2 0 0 1 2-2h.09A1.65 1.65 0 0 0 4.6 9a1.65 1.65 0 0 0-.33-1.82l-.06-.06a2 2 0 0 1 0-2.83 2 2 0 0 1 2.83 0l.06.06a1.65 1.65 0 0 0 1.82.33H9a1.65 1.65 0 0 0 1-1.51V3a2 2 0 0 1 2-2 2 2 0 0 1 2 2v.09a1.65 1.65 0 0 0 1 1.51 1.65 1.65 0 0 0 1.82-.33l.06-.06a2 2 0 0 1 2.83 0 2 2 0 0 1 0 2.83l-.06.06a1.65 1.65 0 0 0-.33 1.82V9a1.65 1.65 0 0 0 1.51 1H21a2 2 0 0 1 2 2 2 2 0 0 1-2 2h-.09a1.65 1.65 0 0 0-1.51 1z"></path></svg>',
        'refresh': '<svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M23 4v6h-6"></path><path d="M1 20v-6h6"></path><path d="M3.51 9a9 9 0 0 1 14.85-3.36L23 10M1 14l4.64 4.36A9 9 0 0 0 20.49 15"></path></svg>'
    };
    
    return icons[name] || null;
} 