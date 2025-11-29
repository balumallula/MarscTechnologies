
  
    // --- Mobile Menu Toggle ---
    const mobileMenuBtn = document.querySelector('.mobile-menu');
    const navMenu = document.querySelector('.nav-menu');
    const mainNavbar = document.getElementById('main-navbar'); 

    mobileMenuBtn.addEventListener('click', () => {
      const isExpanded = navMenu.classList.toggle('active');
      mobileMenuBtn.setAttribute('aria-expanded', isExpanded);
      mobileMenuBtn.innerHTML = isExpanded 
        ? '<i class="fas fa-times"></i>' 
        : '<i class="fas fa-bars"></i>';
    });

    // Close menu when clicking a link
    document.querySelectorAll('.nav-link').forEach(link => {
      link.addEventListener('click', () => {
        navMenu.classList.remove('active');
        mobileMenuBtn.innerHTML = '<i class="fas fa-bars"></i>';
        mobileMenuBtn.setAttribute('aria-expanded', 'false');
      });
    });

    // Header scroll effect
    window.addEventListener('scroll', () => {
      const header = document.querySelector('.header');
      header.classList.toggle('scrolled', window.scrollY > 50);
    });

    // Set active nav link based on scroll position
    const sections = document.querySelectorAll('section');
    const navLinks = document.querySelectorAll('.nav-link');

    window.addEventListener('scroll', () => {
      let current = '';
      
      sections.forEach(section => {
        const sectionTop = section.offsetTop;
        if (window.scrollY >= sectionTop - 300) { 
          current = section.getAttribute('id');
        }
      });
      
      navLinks.forEach(link => {
        link.classList.remove('active');
        if (link.getAttribute('href') === `#${current}`) {
          link.classList.add('active');
        }
      });
    });
    
    // --- Contact Form UX Script ---
    function handleContactSubmit(event) {
      const form = event.target;
      const button = form.querySelector('button[type="submit"]');
      if (!button) return;

      button.disabled = true;
      button.dataset.originalText = button.textContent;
      button.textContent = 'Sending...';
    }

    // ===============================================
    // ✅ FIX: CONDITIONAL SCROLL ON PAGE LOAD/REFRESH
    // ===============================================
    window.onload = function() {
        const contactSection = document.getElementById('contact');
        const successMessage = document.querySelector('.contact-form .alert-success');
        const errorMessage = document.querySelector('.contact-form .alert-danger');
        const validationMessage = document.querySelector('.contact-form .alert-warning');
        
        // Check if any message is present (Post-submission scenario)
        if (contactSection && (successMessage || errorMessage || validationMessage)) {
            // SCROLL TO MESSAGE: User submitted the form, scroll down to see the result.
            contactSection.scrollIntoView({
                behavior: 'smooth',
                block: 'start'
            });
            
            // Re-enable the button if it failed client-side validation (though server should handle this)
            const formButton = contactSection.querySelector('button[type="submit"]');
            if (formButton && formButton.dataset.originalText) {
                formButton.disabled = false;
                formButton.textContent = formButton.dataset.originalText;
            }
            
        } else {
            // SCROLL TO TOP: Normal refresh or first visit, always go to the top.
            // This resets the scroll position remembered by the browser.
            window.scrollTo({
                top: 0,
                behavior: 'instant' // Instant jump to the top
            });
        }
    };
 