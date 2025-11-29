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
        // FIX: Always add contact object to model
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
            // FIX: Add both contact and binding result to flash attributes
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

            // Success case
            redirectAttributes.addFlashAttribute("message", "Thanks! Your message has been sent successfully.");
            redirectAttributes.addFlashAttribute("contact", new Contact()); // Reset form
            
        } catch (Exception e) {
            // Error case
            redirectAttributes.addFlashAttribute("error", "Sorry, an unexpected error occurred. Please try again.");
            redirectAttributes.addFlashAttribute("contact", contact); // Keep user input
            e.printStackTrace();
        }

        return "redirect:/Marsc/dashboard#contact";
    }
}