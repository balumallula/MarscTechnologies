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

    @GetMapping
    public String redirectToDashboard() {
        return "redirect:/Marsc/dashboard";
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        // Always ensure contact object is available
        if (!model.containsAttribute("contact")) {
            model.addAttribute("contact", new Contact());
        }
        return "index";
    }

    @PostMapping("/contact")
    public String submitContactForm(
            @Valid @ModelAttribute("contact") Contact contact,
            BindingResult result,
            RedirectAttributes redirectAttributes) {

        if (result.hasErrors()) {
            // Add binding result with correct attribute name
            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.contact", result);
            redirectAttributes.addFlashAttribute("contact", contact);
            return "redirect:/Marsc/dashboard#contact";
        }

        try {
            // Save to database
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

            // Success case - clear form and show message
            redirectAttributes.addFlashAttribute("message", "Thanks! Your message has been sent successfully.");
            redirectAttributes.addFlashAttribute("contact", new Contact());
            
        } catch (Exception e) {
            // Error case - show error message but keep form data
            redirectAttributes.addFlashAttribute("error", "Sorry, an unexpected error occurred. Please try again.");
            redirectAttributes.addFlashAttribute("contact", contact);
            e.printStackTrace(); // Add proper logging in production
        }

        return "redirect:/Marsc/dashboard#contact";
    }
}