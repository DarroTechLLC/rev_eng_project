(function($) {
  "use strict"; // Start of use strict
  
  console.log('🚀 Initializing sb-admin-2.js');

  // Toggle the side navigation
  $("#sidebarToggle, #sidebarToggleTop").on('click', function(e) {
    console.log('👆 Sidebar toggle clicked');
    $("body").toggleClass("sidebar-toggled");
    $(".sidebar").toggleClass("toggled");
    
    const isToggled = $(".sidebar").hasClass("toggled");
    console.log(`📂 Sidebar is now ${isToggled ? 'collapsed' : 'expanded'}`);
    
    if (isToggled) {
      $('.sidebar .collapse').collapse('hide');
      console.log('🔄 Collapsing all sidebar sub-menus');
    };
  });

  // Close any open menu accordions when window is resized below 768px
  $(window).resize(function() {
    console.log(`🖥️ Window resized to ${$(window).width()}px width`);
    
    if ($(window).width() < 768) {
      $('.sidebar .collapse').collapse('hide');
      console.log('📱 Window width < 768px: Collapsing sidebar sub-menus');
    };
    
    // Toggle the side navigation when window is resized below 480px
    if ($(window).width() < 480 && !$(".sidebar").hasClass("toggled")) {
      $("body").addClass("sidebar-toggled");
      $(".sidebar").addClass("toggled");
      $('.sidebar .collapse').collapse('hide');
      console.log('📱 Window width < 480px: Auto-collapsing sidebar');
    };
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
