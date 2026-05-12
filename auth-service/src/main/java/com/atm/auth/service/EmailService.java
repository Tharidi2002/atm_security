package com.atm.auth.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

@Slf4j
@Service
public class EmailService {
    
    private final JavaMailSender mailSender;
    
    @Value("${spring.mail.username}")
    private String fromEmail;
    
    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }
    
    public void sendVerificationEmail(String toEmail, String token) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);
            
            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject("Verify your ATM Security System account");
            
            String verificationUrl = "http://localhost:8081/api/auth/verify-email?token=" + token;
            
            String htmlContent = """
                <html>
                <body>
                    <h2>Welcome to ATM Security System!</h2>
                    <p>Thank you for registering. Please click the link below to verify your email address:</p>
                    <p><a href="%s">Verify Email</a></p>
                    <p>If you cannot click the link, please copy and paste this URL into your browser:</p>
                    <p>%s</p>
                    <p>This link will expire in 24 hours.</p>
                    <br>
                    <p>Best regards,<br>ATM Security System Team</p>
                </body>
                </html>
                """.formatted(verificationUrl, verificationUrl);
            
            helper.setText(htmlContent, true);
            
            mailSender.send(message);
            log.info("Verification email sent to: {}", toEmail);
            
        } catch (MessagingException e) {
            log.error("Error sending verification email to: {}", toEmail, e);
            // For development, log the verification URL
            log.info("Verification URL for {}: http://localhost:8081/api/auth/verify-email?token={}", toEmail, token);
        }
    }
    
    public void sendPasswordResetEmail(String toEmail, String token) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);
            
            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject("Reset your ATM Security System password");
            
            String resetUrl = "http://localhost:3000/reset-password?token=" + token;
            
            String htmlContent = """
                <html>
                <body>
                    <h2>Password Reset Request</h2>
                    <p>You requested to reset your password. Click the link below to proceed:</p>
                    <p><a href="%s">Reset Password</a></p>
                    <p>If you cannot click the link, please copy and paste this URL into your browser:</p>
                    <p>%s</p>
                    <p>This link will expire in 1 hour.</p>
                    <p>If you did not request a password reset, please ignore this email.</p>
                    <br>
                    <p>Best regards,<br>ATM Security System Team</p>
                </body>
                </html>
                """.formatted(resetUrl, resetUrl);
            
            helper.setText(htmlContent, true);
            
            mailSender.send(message);
            log.info("Password reset email sent to: {}", toEmail);
            
        } catch (MessagingException e) {
            log.error("Error sending password reset email to: {}", toEmail, e);
            // For development, log the reset URL
            log.info("Password reset URL for {}: http://localhost:3000/reset-password?token={}", toEmail, token);
        }
    }
    
    public void sendSimpleEmail(String toEmail, String subject, String text) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject(subject);
            message.setText(text);
            
            mailSender.send(message);
            log.info("Simple email sent to: {}", toEmail);
            
        } catch (Exception e) {
            log.error("Error sending simple email to: {}", toEmail, e);
            log.info("Email would be sent to: {} with subject: {} and text: {}", toEmail, subject, text);
        }
    }
    
    public void sendAlertEmail(String toEmail, String alertTitle, String alertMessage) {
        String subject = "🚨 ATM Security Alert: " + alertTitle;
        
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);
            
            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject(subject);
            
            String htmlContent = """
                <html>
                <body style="font-family: Arial, sans-serif; background-color: #f4f4f4; margin: 0; padding: 20px;">
                    <div style="max-width: 600px; margin: 0 auto; background-color: white; padding: 20px; border-radius: 8px; box-shadow: 0 2px 4px rgba(0,0,0,0.1);">
                        <div style="background-color: #dc3545; color: white; padding: 10px; text-align: center; border-radius: 8px 8px 0 0;">
                            <h1 style="margin: 0; font-size: 24px;">🚨 SECURITY ALERT</h1>
                        </div>
                        <div style="padding: 20px;">
                            <h2 style="color: #dc3545; margin-top: 0;">%s</h2>
                            <p style="line-height: 1.6;">%s</p>
                            <div style="background-color: #f8f9fa; padding: 15px; border-left: 4px solid #dc3545; margin: 20px 0;">
                                <p style="margin: 0;"><strong>Immediate Action Required:</strong></p>
                                <p style="margin: 5px 0;">Please log in to the ATM Security System to investigate this alert.</p>
                                <p style="margin: 5px 0;"><a href="http://localhost:3000/login" style="color: #007bff; text-decoration: none;">Access Security System</a></p>
                            </div>
                            <p style="color: #6c757d; font-size: 12px; margin-top: 20px;">
                                This is an automated security alert. Please do not reply to this email.
                            </p>
                        </div>
                    </div>
                </body>
                </html>
                """.formatted(alertTitle, alertMessage);
            
            helper.setText(htmlContent, true);
            
            mailSender.send(message);
            log.info("Alert email sent to: {}", toEmail);
            
        } catch (MessagingException e) {
            log.error("Error sending alert email to: {}", toEmail, e);
            log.info("Alert email would be sent to: {} - {}: {}", toEmail, alertTitle, alertMessage);
        }
    }
}
