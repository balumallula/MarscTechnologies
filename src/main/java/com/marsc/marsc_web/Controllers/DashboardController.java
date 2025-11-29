package com.marsc.marsc_web.Controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
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
import java.time.LocalDateTime;

@Controller
@RequestMapping("/Marsc")
public class DashboardController {

    @Autowired
    private ContactRepository contactRepository;

    @Autowired
    private MailService mailService;

    @Value("${app.environment:unknown}")
    private String environment;

    @GetMapping
    public String redirectToDashboard() {
        return "redirect:/Marsc/dashboard";
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        if (!model.containsAttribute("contact")) {
            model.addAttribute("contact", new Contact());
        }
        model.addAttribute("environment", environment);
        return "index";
    }

    @PostMapping("/contact")
    public String submitContactForm(
            @Valid @ModelAttribute("contact") Contact contact,
            BindingResult result,
            RedirectAttributes redirectAttributes) {

        if (result.hasErrors()) {
            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.contact", result);
            redirectAttributes.addFlashAttribute("contact", contact);
            return "redirect:/Marsc/dashboard#contact";
        }

        try {
            // Save to database with timestamp
            Contact savedContact = contactRepository.save(contact);

            // Process emails in background
            processContactAsync(savedContact);

            // Always show success to user regardless of email outcome
            String successMessage = "Thank you, " + contact.getName() + "! " +
                ("production".equals(environment) 
                    ? "Your message has been received. We'll get back to you soon at " + contact.getEmail() + "."
                    : "Your message has been sent successfully. We'll contact you soon!");

            redirectAttributes.addFlashAttribute("message", successMessage);
            redirectAttributes.addFlashAttribute("contact", new Contact());
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", 
                "Sorry, we couldn't process your message. Please try again or email us directly at marsctechnologies@gmail.com");
            redirectAttributes.addFlashAttribute("contact", contact);
            System.err.println("Database error: " + e.getMessage());
        }

        return "redirect:/Marsc/dashboard#contact";
    }

    @Async
    private void processContactAsync(Contact contact) {
        System.out.println("🚀 Processing contact in " + environment + " environment");

        try {
            boolean adminNotified = mailService.sendContactEmail(
                contact.getEmail(),
                contact.getName(),
                contact.getSubject(),
                contact.getMessage()
            );

            boolean userNotified = mailService.sendResponseToUser(
                contact.getEmail(),
                contact.getName()
            );

            if (adminNotified && userNotified) {
                System.out.println("✅ All emails sent successfully from " + environment);
            } else {
                System.out.println("⚠️ Some emails failed in " + environment + 
                    " - Admin: " + adminNotified + ", User: " + userNotified);
            }
        } catch (Exception e) {
            System.err.println("💥 Critical error in email processing: " + e.getMessage());
        }
    }
}