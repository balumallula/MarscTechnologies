package com.marsc.marsc_web.Services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class MailService {

    @Autowired(required = false)
    private JavaMailSender mailSender;

    @Value("${app.environment:unknown}")
    private String environment;

    public boolean sendContactEmail(String userEmail, String userName, String subject, String message) {
        // Always log the contact first
        logContactSubmission("CONTACT_FORM", userEmail, userName, subject, message);

        if (mailSender == null) {
            System.out.println("❌ Mail sender not available - skipping email");
            return false;
        }

        try {
            SimpleMailMessage mailMessage = new SimpleMailMessage();
            mailMessage.setTo("marsctechnologies@gmail.com");
            mailMessage.setSubject("🌐 New Contact Form: " + (subject != null ? subject : "No Subject"));
            mailMessage.setText(
                "New contact form submission from " + environment.toUpperCase() + ":\n\n" +
                "👤 Name: " + userName + "\n" +
                "📧 Email: " + userEmail + "\n" +
                "📝 Subject: " + (subject != null ? subject : "Not provided") + "\n" +
                "💬 Message: " + message + "\n\n" +
                "🕒 Timestamp: " + LocalDateTime.now() + "\n" +
                "🌍 Environment: " + environment
            );
            
            mailSender.send(mailMessage);
            System.out.println("✅ Admin notification sent successfully from " + environment);
            return true;
            
        } catch (Exception e) {
            System.err.println("❌ Failed to send admin email in " + environment + ": " + e.getMessage());
            // Don't throw exception - just return false
            return false;
        }
    }

    public boolean sendResponseToUser(String userEmail, String userName) {
        // Always log the auto-response
        logContactSubmission("AUTO_RESPONSE", userEmail, userName, "Thank you email", "Would send auto-response");

        if (mailSender == null) {
            System.out.println("❌ Mail sender not available - skipping auto-response");
            return false;
        }

        try {
            SimpleMailMessage mailMessage = new SimpleMailMessage();
            mailMessage.setTo(userEmail);
            mailMessage.setSubject("Thank you for contacting Mars-C Technologies");
            mailMessage.setText(
                "Dear " + userName + ",\n\n" +
                "Thank you for reaching out to Mars-C Technologies!\n\n" +
                "We have received your message and our team will get back to you within 24 hours.\n\n" +
                "Best regards,\n" +
                "Mars-C Technologies Team\n" +
                "📧 Email: marsctechnologies@gmail.com\n" +
                "📞 Phone: 8008197255\n\n" +
                "This is an automated response from our " + environment + " environment."
            );
            
            mailSender.send(mailMessage);
            System.out.println("✅ Auto-response sent successfully from " + environment);
            return true;
            
        } catch (Exception e) {
            System.err.println("❌ Failed to send user email in " + environment + ": " + e.getMessage());
            // Don't throw exception - just return false
            return false;
        }
    }

    private void logContactSubmission(String type, String email, String name, String subject, String message) {
        System.out.println("=".repeat(70));
        System.out.println("📋 " + type + " - " + environment.toUpperCase());
        System.out.println("👤 Name: " + name);
        System.out.println("📧 Email: " + email);
        System.out.println("📝 Subject: " + subject);
        System.out.println("💬 Message Preview: " + (message.length() > 100 ? message.substring(0, 100) + "..." : message));
        System.out.println("🕒 Time: " + LocalDateTime.now());
        System.out.println("=".repeat(70));
    }
}