(function($) {
  "use strict"; // Start of use strict

  console.log('🚀 Initializing sb-admin-2.js');

  // CSRF Protection for AJAX requests
  function addCsrfToAjax() {
    console.log('🔒 Setting up CSRF protection for AJAX requests');

    // Get CSRF token and header from meta tags
    const csrfToken = $('meta[name="_csrf"]').attr('content');
    const csrfHeader = $('meta[name="_csrf_header"]').attr('content');

    if (csrfToken && csrfHeader) {
      console.log('✅ CSRF token found, setting up AJAX prefilter');

      // Add CSRF token to all AJAX requests
      $.ajaxPrefilter(function(options, originalOptions, jqXHR) {
        // Only add CSRF for non-GET requests to non-API endpoints
        if (options.type && options.type.toUpperCase() !== 'GET' && 
            (!options.url || !options.url.startsWith('/api/'))) {
          jqXHR.setRequestHeader(csrfHeader, csrfToken);
          console.log(`🔒 Added CSRF token to ${options.url || 'request'}`);
        }
      });

      // Add CSRF token to fetch requests
      const originalFetch = window.fetch;
      window.fetch = function(url, options = {}) {
        // Only add CSRF for non-GET requests to non-API endpoints
        // Always add CSRF for WebAuthn endpoints
        const urlString = url.toString();
        if (options.method && options.method.toUpperCase() !== 'GET' && 
            (!urlString || !urlString.startsWith('/api/') || urlString.startsWith('/webauthn/'))) {
          if (!options.headers) {
            options.headers = {};
          }

          options.headers[csrfHeader] = csrfToken;
          console.log(`🔒 Added CSRF token to fetch request: ${url}`);
        }

        return originalFetch.call(this, url, options);
      };
    } else {
      console.warn('⚠️ CSRF token not found in meta tags');
    }
  }

  // Initialize CSRF protection
  $(document).ready(function() {
    addCsrfToAjax();
  });

  // Toggle the side navigation or open mobile menu
  $("#sidebarToggle, #sidebarToggleTop").on('click', function(e) {
    console.log('👆 Sidebar/hamburger toggle clicked');
    e.preventDefault();

    // On mobile devices, open the hamburger menu
    if ($(window).width() <= 768) {
      $('#mobileMenuOverlay').addClass('active');
      $('body').addClass('mobile-menu-open');
      console.log('🍔 Mobile hamburger menu opened');
    } else {
      // On desktop, toggle section visibility (optional)
      console.log('🖥️ Desktop sidebar toggle - no action needed as sidebar is always visible');
    }
  });

  // Handle window resize events
  $(window).resize(function() {
    console.log(`🖥️ Window resized to ${$(window).width()}px width`);

    // If transitioning from mobile to desktop
    if ($(window).width() > 768) {
      // Close mobile menu if open
      $('#mobileMenuOverlay').removeClass('active');
      $('body').removeClass('mobile-menu-open');
      console.log('🖥️ Window width > 768px: Closing mobile menu if open');

      // Ensure all desktop sidebar sections are visible
      $('.sidebar .section-content').show();
      console.log('🖥️ Ensuring all desktop sidebar sections are visible');
    }
  });

  // Prevent the content wrapper from scrolling when the fixed side navigation hovered over
  $('body.fixed-nav .sidebar').on('mousewheel DOMMouseScroll wheel', function(e) {
    if ($(window).width() > 768) {
      console.log('🖱️ Mousewheel on sidebar detected');
      var e0 = e.originalEvent,
        delta = e0.wheelDelta || -e0.detail;
      this.scrollTop += (delta < 0 ? 1 : -1) * 30;
      console.log(`🔄 Adjusted sidebar scroll position by ${(delta < 0 ? 1 : -1) * 30}px`);
      e.preventDefault();
    }
  });

  // Scroll to top button appear
  $(document).on('scroll', function() {
    var scrollDistance = $(this).scrollTop();
    if (scrollDistance > 100 && !$('.scroll-to-top').is(':visible')) {
      $('.scroll-to-top').fadeIn();
      console.log('⬆️ Scroll-to-top button shown (scroll > 100px)');
    } else if (scrollDistance <= 100 && $('.scroll-to-top').is(':visible')) {
      $('.scroll-to-top').fadeOut();
      console.log('⬇️ Scroll-to-top button hidden (scroll <= 100px)');
    }
  });

  // Smooth scrolling using jQuery easing
  $(document).on('click', 'a.scroll-to-top', function(e) {
    console.log('👆 Scroll-to-top button clicked');
    var $anchor = $(this);
    var targetElement = $($anchor.attr('href'));
    var targetPosition = targetElement.offset().top;

    console.log(`🔄 Smooth scrolling to element: ${$anchor.attr('href')}, position: ${targetPosition}px`);

    $('html, body').stop().animate({
      scrollTop: targetPosition
    }, 1000, 'easeInOutExpo');
    console.log('✅ Smooth scroll animation started');

    e.preventDefault();
  });

  console.log('✅ sb-admin-2.js fully initialized');
})(jQuery); // End of use strict
