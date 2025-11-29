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

    // FIXED: Corrected the mapping path - removed duplicate "/Marsc"
    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        // FIXED: Only add new contact if not already present in model/flash attributes
        if (!model.containsAttribute("contact")) {
            model.addAttribute("contact", new Contact());
        }
        return "index";
    }

    // MANDATED: Handles the form submission
    @PostMapping("/contact")
    public String submitContactForm(
            @Valid @ModelAttribute("contact") Contact contact,
            BindingResult result,
            RedirectAttributes redirectAttributes) {

        // If validation fails, send back form + errors
        if (result.hasErrors()) {
            // FIXED: Add both the binding result and contact object to flash attributes
            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.contact", result);
            redirectAttributes.addFlashAttribute("contact", contact); // Repopulate form fields
            return "redirect:/Marsc/dashboard";
        }

        try {
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

            // FIXED: Clear any previous errors and set success message
            redirectAttributes.addFlashAttribute("message", "Thanks! Your message has been sent successfully.");
            // FIXED: Add a fresh contact object to clear the form
            redirectAttributes.addFlashAttribute("contact", new Contact());
            
        } catch (Exception e) {
            // FIXED: Clear any previous binding results and set error message
            redirectAttributes.addFlashAttribute("error", "Sorry, an unexpected error occurred while sending your message. Please try again.");
            // FIXED: Keep the user's input for correction
            redirectAttributes.addFlashAttribute("contact", contact);
            // You should also log the exception here: logger.error("Contact form submission error:", e);
        }

        // Final destination for both success and error cases
        return "redirect:/Marsc/dashboard";
    }
}