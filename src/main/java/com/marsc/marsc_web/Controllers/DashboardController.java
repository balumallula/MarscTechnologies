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
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

@Controller
@RequestMapping("/Marsc")
public class DashboardController {

    @Autowired
    private ContactRepository contactRepository;

    @Autowired
    private MailService mailService;

    @Autowired
    private Executor taskExecutor;

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
            // Save contact info to DB immediately (fast operation)
            Contact savedContact = contactRepository.save(contact);

            // Send emails ASYNC (non-blocking) - don't wait for completion
            CompletableFuture.runAsync(() -> {
                sendEmailsWithRetry(savedContact);
            }, taskExecutor);

            // Immediate success response
            redirectAttributes.addFlashAttribute("message", "Thanks! Your message has been sent successfully. We'll get back to you soon!");
            redirectAttributes.addFlashAttribute("contact", new Contact()); // Reset form
            
        } catch (Exception e) {
            // Only handle database errors (fast to check)
            redirectAttributes.addFlashAttribute("error", "Sorry, we couldn't save your message. Please try again.");
            redirectAttributes.addFlashAttribute("contact", contact);
            System.err.println("Database error: " + e.getMessage());
        }

        return "redirect:/Marsc/dashboard#contact";
    }

    private void sendEmailsWithRetry(Contact contact) {
        int maxRetries = 2;
        int retryCount = 0;
        
        while (retryCount < maxRetries) {
            try {
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
                
                System.out.println("Emails sent successfully for: " + contact.getEmail());
                break; // Success, exit retry loop
                
            } catch (Exception e) {
                retryCount++;
                System.err.println("Email attempt " + retryCount + " failed for: " + contact.getEmail());
                
                if (retryCount >= maxRetries) {
                    System.err.println("All email attempts failed for: " + contact.getEmail());
                    break;
                }
                
                // Wait before retry (1 second, then 2 seconds)
                try {
                    Thread.sleep(retryCount * 1000);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }
    }
}