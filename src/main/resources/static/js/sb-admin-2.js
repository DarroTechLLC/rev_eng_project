(function($) {
  "use strict"; // Start of use strict


  // CSRF Protection for AJAX requests
  function addCsrfToAjax() {
    // Get CSRF token and header from meta tags
    const csrfToken = $('meta[name="_csrf"]').attr('content');
    const csrfHeader = $('meta[name="_csrf_header"]').attr('content');

    if (csrfToken && csrfHeader) {
      // Add CSRF token to all AJAX requests
      $.ajaxPrefilter(function(options, originalOptions, jqXHR) {
        // Only add CSRF for non-GET requests to non-API endpoints
        if (options.type && options.type.toUpperCase() !== 'GET' && 
            (!options.url || !options.url.startsWith('/api/'))) {
          jqXHR.setRequestHeader(csrfHeader, csrfToken);
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
        }

        return originalFetch.call(this, url, options);
      };
    } else {
      // CSRF token not found in meta tags
    }
  }

  // Initialize CSRF protection
  $(document).ready(function() {
    addCsrfToAjax();
  });

  // Toggle the side navigation or open mobile menu
  $("#sidebarToggle, #sidebarToggleTop").on('click', function(e) {
    e.preventDefault();

    // On mobile devices, open the hamburger menu
    if ($(window).width() <= 768) {
      $('#mobileMenuOverlay').addClass('active');
      $('body').addClass('mobile-menu-open');
    } else {
      // On desktop, toggle section visibility (optional)
    }
  });

  // Handle window resize events
  $(window).resize(function() {
    // If transitioning from mobile to desktop
    if ($(window).width() > 768) {
      // Close mobile menu if open
      $('#mobileMenuOverlay').removeClass('active');
      $('body').removeClass('mobile-menu-open');

      // Ensure all desktop sidebar sections are visible
      $('.sidebar .section-content').show();
    }
  });

  // Prevent the content wrapper from scrolling when the fixed side navigation hovered over
  $('body.fixed-nav .sidebar').on('mousewheel DOMMouseScroll wheel', function(e) {
    if ($(window).width() > 768) {
      var e0 = e.originalEvent,
        delta = e0.wheelDelta || -e0.detail;
      this.scrollTop += (delta < 0 ? 1 : -1) * 30;
      e.preventDefault();
    }
  });

  // Scroll to top button appear
  $(document).on('scroll', function() {
    var scrollDistance = $(this).scrollTop();
    if (scrollDistance > 100 && !$('.scroll-to-top').is(':visible')) {
      $('.scroll-to-top').fadeIn();
    } else if (scrollDistance <= 100 && $('.scroll-to-top').is(':visible')) {
      $('.scroll-to-top').fadeOut();
    }
  });

  // Smooth scrolling using jQuery easing
  $(document).on('click', 'a.scroll-to-top', function(e) {
    var $anchor = $(this);
    var targetElement = $($anchor.attr('href'));
    var targetPosition = targetElement.offset().top;

    $('html, body').stop().animate({
      scrollTop: targetPosition
    }, 1000, 'easeInOutExpo');

    e.preventDefault();
  });
})(jQuery); // End of use strict
