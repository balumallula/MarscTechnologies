package com.marsc.marsc_web.Controllers;

import org.springframework.beans.factory.annotation.Autowired;
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
            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.contact", result);
            redirectAttributes.addFlashAttribute("contact", contact);
            return "redirect:/Marsc/dashboard#contact";
        }

        try {
            // Save contact info to DB immediately
            Contact savedContact = contactRepository.save(contact);

            // Send emails in background - user doesn't wait
            sendEmailsAsync(savedContact);

            // Immediate success response
            redirectAttributes.addFlashAttribute("message", "Thank you! Your message has been received. We'll contact you soon.");
            redirectAttributes.addFlashAttribute("contact", new Contact());
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Sorry, we couldn't process your message. Please try again.");
            redirectAttributes.addFlashAttribute("contact", contact);
            System.err.println("Database error: " + e.getMessage());
        }

        return "redirect:/Marsc/dashboard#contact";
    }

    @Async
    private void sendEmailsAsync(Contact contact) {
        System.out.println("🚀 Starting email process for: " + contact.getEmail());
        
        boolean adminEmailSent = false;
        boolean userEmailSent = false;
        int attempt = 1;
        int maxAttempts = 2;

        // Retry logic for emails
        while (attempt <= maxAttempts && (!adminEmailSent || !userEmailSent)) {
            System.out.println("📧 Email attempt " + attempt + " for: " + contact.getEmail());
            
            if (!adminEmailSent) {
                adminEmailSent = mailService.sendContactEmail(
                    contact.getEmail(),
                    contact.getName(),
                    contact.getSubject(),
                    contact.getMessage()
                );
            }

            if (!userEmailSent) {
                userEmailSent = mailService.sendResponseToUser(
                    contact.getEmail(),
                    contact.getName()
                );
            }

            if (!adminEmailSent || !userEmailSent) {
                attempt++;
                if (attempt <= maxAttempts) {
                    try {
                        Thread.sleep(2000); // Wait 2 seconds before retry
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            }
        }

        // Log final status
        if (adminEmailSent && userEmailSent) {
            System.out.println("✅ All emails sent successfully for: " + contact.getEmail());
        } else {
            System.out.println("❌ Email status for " + contact.getEmail() + 
                " - Admin: " + (adminEmailSent ? "✅" : "❌") +
                ", User: " + (userEmailSent ? "✅" : "❌"));
        }
    }
}