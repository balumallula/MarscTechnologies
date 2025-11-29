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
        // If redirected back with errors, "contact" will already be in the model (from flash attributes)
        if (!model.containsAttribute("contact")) {
            model.addAttribute("contact", new Contact()); // Use no-args constructor
        }
        return "index";
    }

    @PostMapping("/contact")
    public String submitContactForm(
            @Valid @ModelAttribute("contact") Contact contact,
            BindingResult result,
            Model model,
            RedirectAttributes redirectAttributes) {

        // If validation fails, send back form + errors
        if (result.hasErrors()) {
            // Use flash attributes so data & errors survive redirect
           // redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.contact", result);
            redirectAttributes.addFlashAttribute("contact", contact);
            return "redirect:/Marsc/dashboard";
        }

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

        redirectAttributes.addFlashAttribute("message", "Thanks! Your message has been sent.");
        return "redirect:/Marsc/dashboard";
    }
}
