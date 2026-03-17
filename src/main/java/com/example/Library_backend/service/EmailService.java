package com.example.Library_backend.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${app.frontend-url}")
    private String frontendUrl;

    // ─── Generic send email ──────────────────────────────
    public void sendEmail(String toEmail,
                          String subject,
                          String body) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(toEmail);
            message.setSubject(subject);
            message.setText(body);
            mailSender.send(message);
            System.out.println(
                    "✅ Email sent successfully to: " + toEmail);
        } catch (Exception e) {
            System.out.println(
                    "❌ Email sending failed: " + e.getMessage());
        }
    }

    // ─── Send verification email ─────────────────────────
    public void sendVerificationEmail(String toEmail,
                                      String token) {
        String subject = "Verify Your Email - College Library";
        String link = "http://localhost:8080"
                + "/api/auth/verify-email/" + token;
        String body =
                "Hello!\n\n"
                        + "Thank you for registering at College Library.\n\n"
                        + "Please verify your email by clicking the link below:\n\n"
                        + link + "\n\n"
                        + "This link is valid for 24 hours.\n\n"
                        + "If you did not register, please ignore this email.\n\n"
                        + "College Library Team";

        sendEmail(toEmail, subject, body);
    }

    // ─── Send password reset email ───────────────────────
    public void sendPasswordResetEmail(String toEmail,
                                       String token,
                                       String fullName) {
        String subject = "Password Reset Request - College Library";

        // Reset link goes to frontend page
        String resetLink = frontendUrl
                + "/reset-password?token=" + token;

        String body =
                "Hello " + fullName + "!\n\n"
                        + "We received a request to reset your password.\n\n"
                        + "Your password reset token is:\n\n"
                        + "Token: " + token + "\n\n"
                        + "Or click this link:\n"
                        + resetLink + "\n\n"
                        + "⚠️  This token expires in 60 minutes.\n\n"
                        + "If you did NOT request a password reset,\n"
                        + "please ignore this email. "
                        + "Your password will not change.\n\n"
                        + "College Library Team";

        sendEmail(toEmail, subject, body);
    }

    // ─── Send password changed confirmation ──────────────
    public void sendPasswordChangedEmail(String toEmail,
                                         String fullName) {
        String subject = "Password Changed - College Library";
        String body =
                "Hello " + fullName + "!\n\n"
                        + "Your password has been changed successfully.\n\n"
                        + "If you did not make this change, "
                        + "please contact us immediately.\n\n"
                        + "College Library Team";

        sendEmail(toEmail, subject, body);
    }
}