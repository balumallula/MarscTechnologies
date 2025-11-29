package com.marsc.marsc_web.Services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class MailService {

    @Autowired
    private JavaMailSender mailSender;

    // Remove @Async and @Retryable - we'll handle async and retry in controller
    public void sendContactEmail(String userEmail, String userName, String subject, String message) {
        try {
            SimpleMailMessage mailMessage = new SimpleMailMessage();
            mailMessage.setTo("marsctechnologies@gmail.com");
            mailMessage.setSubject("New Contact Form: " + (subject != null ? subject : "No Subject"));
            mailMessage.setText(
                "New contact form submission:\n\n" +
                "Name: " + userName + "\n" +
                "Email: " + userEmail + "\n" +
                "Subject: " + (subject != null ? subject : "Not provided") + "\n" +
                "Message: " + message + "\n\n" +
                "Timestamp: " + java.time.LocalDateTime.now()
            );
            
            mailSender.send(mailMessage);
            
        } catch (Exception e) {
            System.err.println("Failed to send contact email to admin: " + e.getMessage());
            throw new RuntimeException("Email sending failed", e); // Re-throw for retry logic
        }
    }

    // Remove @Async and @Retryable
    public void sendResponseToUser(String userEmail, String userName) {
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
                "Email: marsctechnologies@gmail.com\n" +
                "Phone: 8008197255"
            );
            
            mailSender.send(mailMessage);
            
        } catch (Exception e) {
            System.err.println("Failed to send auto-response to user: " + e.getMessage());
            throw new RuntimeException("Email sending failed", e); // Re-throw for retry logic
        }
    }
}