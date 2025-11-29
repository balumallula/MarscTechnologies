package com.marsc.marsc_web.Services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class MailService {

    @Autowired
    private JavaMailSender mailSender;

    public boolean sendContactEmail(String userEmail, String userName, String subject, String message) {
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
            System.out.println("✅ Admin notification sent successfully for: " + userEmail);
            return true; // Return true on success
            
        } catch (Exception e) {
            System.err.println("❌ Failed to send contact email to admin: " + e.getMessage());
            return false; // Return false on failure (don't throw exception)
        }
    }

    public boolean sendResponseToUser(String userEmail, String userName) {
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
            System.out.println("✅ Auto-response sent successfully to: " + userEmail);
            return true; // Return true on success
            
        } catch (Exception e) {
            System.err.println("❌ Failed to send auto-response to user: " + e.getMessage());
            return false; // Return false on failure (don't throw exception)
        }
    }
}