package com.marsc.marsc_web.Services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class MailService {

    @Autowired
    private JavaMailSender mailSender;

    public void sendContactEmail(String from, String name, String subject, String message) {
        SimpleMailMessage mailMessage = new SimpleMailMessage();
        mailMessage.setTo("marsctechnologies@gmail.com"); // where you want to receive it
        mailMessage.setSubject("New Contact Form Message: " + subject);
        mailMessage.setText("From: " + name + "\nEmail: " + from + "\n\nMessage:\n" + message);
        mailMessage.setFrom(from); // optional: shows sender's email

        mailSender.send(mailMessage);
    }
    
    // Response email to user
    public void sendResponseToUser(String to, String name) {
        SimpleMailMessage responseMail = new SimpleMailMessage();
        responseMail.setTo(to); // User's email
        responseMail.setSubject("Thank you for contacting us!");
        responseMail.setText("Hi " + name + ",\n\n" +
        	    "Thanks for reaching out! We'll get back to you soon.\n\n" +
        	    "Best regards,\n" +
        	    "The Mars-C Team");
 
        responseMail.setFrom("marsctechnologies@gmail.com"); // Must match spring.mail.username

        mailSender.send(responseMail);
    }
}
