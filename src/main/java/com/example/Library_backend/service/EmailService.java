package com.example.Library_backend.service;

import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    public void sendEmail(String toEmail, String subject, String body) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(toEmail);
            message.setSubject(subject);
            message.setText(body);
            mailSender.send(message);
        } catch (Exception e) {
            // Log error but don't crash the app
            System.out.println("Email sending failed: " + e.getMessage());
        }
    }

    // Send verification email
    public void sendVerificationEmail(String toEmail, String token) {
        String subject = "Verify Your Email - College Library";
        String body = "Hello!\n\n"
                + "Please verify your email by clicking the link below:\n\n"
                + "http://localhost:8080/api/auth/verify-email/" + token + "\n\n"
                + "This link is valid for 24 hours.\n\n"
                + "College Library Team";
        sendEmail(toEmail, subject, body);
    }

    // Send password reset email
    public void sendPasswordResetEmail(String toEmail, String token) {
        String subject = "Password Reset - College Library";
        String body = "Hello!\n\n"
                + "You requested a password reset. Use this token:\n\n"
                + token + "\n\n"
                + "This token expires in 1 hour.\n\n"
                + "If you did not request this, ignore this email.\n\n"
                + "College Library Team";
        sendEmail(toEmail, subject, body);
    }
}