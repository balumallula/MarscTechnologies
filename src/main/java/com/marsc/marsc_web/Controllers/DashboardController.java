package com.marsc.marsc_web.Controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.marsc.marsc_web.Entities.Contact;
import com.marsc.marsc_web.Repositories.ContactRepository;
import com.marsc.marsc_web.Services.MailService;

import jakarta.validation.Valid;

@Controller
@RequestMapping("/Marsc")
public class DashboardController {

    @Autowired
    private ContactRepository contactRepository;

    @Autowired
    private MailService mailService;

    // MANDATED: Redirects /Marsc to /Marsc/dashboard
    @GetMapping
    public String redirectToDashboard() {
        return "redirect:/Marsc/dashboard";
    }

    // MANDATED: Renders the index page
    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        // Spring automatically transfers flash attributes (like 'message', 'error', 'contact', and 
        // 'BindingResult') from the RedirectAttributes to the Model before rendering the 'index' view.
        
        // Ensure a contact object is in the model for the form if no flash attributes (from failed submission) exist
        if (!model.containsAttribute("contact")) {
            model.addAttribute("contact", new Contact()); // Use no-args constructor
        }
        return "index";
    }

    // MANDATED: Handles the form submission
    @PostMapping("/contact")
    public String submitContactForm(
            @Valid @ModelAttribute("contact") Contact contact,
            BindingResult result,
            RedirectAttributes redirectAttributes) { // Removed unused 'Model model'

        // If validation fails, send back form + errors
        if (result.hasErrors()) {
            // FIX 1: MUST add the BindingResult using this specific key for Thymeleaf to access errors after redirect.
            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.contact", result);
            redirectAttributes.addFlashAttribute("contact", contact); // Repopulate form fields
            return "redirect:/Marsc/dashboard";
        }

        try {
            // FIX 2: Wrap DB and Mail operations in try-catch to prevent unhandled 500 error.
            
            // Save contact info to DB
            contactRepository.save(contact);

            // Send emails
            mailService.sendContactEmail(
                    contact.getEmail(),
                    contact.getName(),
                    contact.getSubject(),
                    contact.getMessage()
            );

            mailService.sendResponseToUser(
                    contact.getEmail(),
                    contact.getName()
            );

            // FIX 3: Set success message after all operations succeed
            redirectAttributes.addFlashAttribute("message", "Thanks! Your message has been sent successfully.");
            
        } catch (Exception e) {
            // Catch unexpected errors (DB, Mail) and redirect with a clean error message
            redirectAttributes.addFlashAttribute("error", "Sorry, an unexpected error occurred while sending your message. Please try again.");
            // You should also log the exception here: logger.error("Contact form submission error:", e);
        }

        // Final destination for both success and error cases
        return "redirect:/Marsc/dashboard";
    }
}