
  // --- Mobile Menu Toggle ---
  const mobileMenuBtn = document.querySelector('.mobile-menu');
  const navMenu = document.querySelector('.nav-menu');

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

  // ====================================================
  // ✅ Handle page load and form submission redirects
  // ====================================================
  function handleInitialScroll() {
    const url = new URL(window.location.href);
    const hash = url.hash;
    
    // Check if we have flash messages (form was submitted)
    const successMessage = document.querySelector('.contact-form .alert-success');
    const errorMessage = document.querySelector('.contact-form .alert-danger');
    
    if (hash === '#contact' || successMessage || errorMessage) {
      // Scroll to contact section if we have messages or hash
      const contactSection = document.getElementById('contact');
      if (contactSection) {
        contactSection.scrollIntoView({
          behavior: 'smooth',
          block: 'start'
        });
      }

      // Reset form button state
      const formButton = document.querySelector('.contact-form button[type="submit"]');
      if (formButton && formButton.dataset.originalText) {
        formButton.disabled = false;
        formButton.textContent = formButton.dataset.originalText;
      }
    }  
  }

  // Run on first load
  document.addEventListener('DOMContentLoaded', handleInitialScroll);

  // Run on browser back/forward navigation
  window.addEventListener('pageshow', function(event) {
    if (event.persisted) {
      handleInitialScroll();
    }
  });
