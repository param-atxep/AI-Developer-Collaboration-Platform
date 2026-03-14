package com.foodrescue.notification.service;

import com.foodrescue.notification.entity.NotificationType;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

/**
 * Service responsible for sending email notifications via Spring Mail.
 * Uses Thymeleaf templates for rich HTML emails and supports async delivery
 * to avoid blocking the main notification pipeline.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;

    @Value("${spring.mail.from:noreply@foodrescue.com}")
    private String fromAddress;

    @Value("${app.email.enabled:true}")
    private boolean emailEnabled;

    /**
     * Send a notification email asynchronously.
     *
     * @param to      recipient email address
     * @param subject email subject line
     * @param body    plain-text or HTML body content
     * @param type    the notification type, used to select the appropriate template
     */
    @Async
    public void sendNotificationEmail(String to, String subject, String body, NotificationType type) {
        if (!emailEnabled) {
            log.info("Email sending is disabled. Skipping email to {}", to);
            return;
        }

        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

            helper.setFrom(fromAddress);
            helper.setTo(to);
            helper.setSubject("[FoodRescue] " + subject);

            String htmlContent = buildHtmlContent(subject, body, type);
            helper.setText(htmlContent, true);

            mailSender.send(mimeMessage);
            log.info("Email notification sent successfully to {} with subject: {}", to, subject);
        } catch (MessagingException e) {
            log.error("Failed to send email notification to {}: {}", to, e.getMessage(), e);
        }
    }

    /**
     * Send a simple plain-text email.
     *
     * @param to      recipient email address
     * @param subject email subject line
     * @param body    plain-text body content
     */
    @Async
    public void sendSimpleEmail(String to, String subject, String body) {
        if (!emailEnabled) {
            log.info("Email sending is disabled. Skipping email to {}", to);
            return;
        }

        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, false, "UTF-8");

            helper.setFrom(fromAddress);
            helper.setTo(to);
            helper.setSubject("[FoodRescue] " + subject);
            helper.setText(body, false);

            mailSender.send(mimeMessage);
            log.info("Simple email sent successfully to {} with subject: {}", to, subject);
        } catch (MessagingException e) {
            log.error("Failed to send simple email to {}: {}", to, e.getMessage(), e);
        }
    }

    // -------------------------------------------------------------------------
    // Private helpers
    // -------------------------------------------------------------------------

    /**
     * Build HTML email content using Thymeleaf template engine.
     * Falls back to an inline HTML template if no template file is found.
     */
    private String buildHtmlContent(String title, String message, NotificationType type) {
        try {
            Context context = new Context();
            context.setVariable("title", title);
            context.setVariable("message", message);
            context.setVariable("notificationType", type.name());
            context.setVariable("typeLabel", formatTypeLabel(type));

            return templateEngine.process("notification-email", context);
        } catch (Exception e) {
            log.warn("Thymeleaf template 'notification-email' not found, using inline fallback: {}",
                    e.getMessage());
            return buildFallbackHtml(title, message, type);
        }
    }

    /**
     * Inline HTML fallback when the Thymeleaf template is unavailable.
     */
    private String buildFallbackHtml(String title, String message, NotificationType type) {
        String typeColor = getTypeColor(type);

        return """
                <!DOCTYPE html>
                <html lang="en">
                <head>
                    <meta charset="UTF-8">
                    <meta name="viewport" content="width=device-width, initial-scale=1.0">
                    <title>%s</title>
                </head>
                <body style="font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto; padding: 20px; background-color: #f5f5f5;">
                    <div style="background-color: #ffffff; border-radius: 8px; padding: 30px; box-shadow: 0 2px 4px rgba(0,0,0,0.1);">
                        <div style="text-align: center; margin-bottom: 20px;">
                            <h1 style="color: #2d7a3a; margin: 0;">FoodRescue</h1>
                            <p style="color: #666; font-size: 14px;">Reducing Food Waste Together</p>
                        </div>
                        <div style="background-color: %s; color: #ffffff; padding: 8px 16px; border-radius: 4px; display: inline-block; font-size: 12px; font-weight: bold; margin-bottom: 16px;">
                            %s
                        </div>
                        <h2 style="color: #333; margin-top: 0;">%s</h2>
                        <p style="color: #555; line-height: 1.6;">%s</p>
                        <hr style="border: none; border-top: 1px solid #eee; margin: 20px 0;">
                        <p style="color: #999; font-size: 12px; text-align: center;">
                            This is an automated notification from the FoodRescue platform.
                            You can manage your notification preferences in your account settings.
                        </p>
                    </div>
                </body>
                </html>
                """.formatted(title, typeColor, formatTypeLabel(type), title, message);
    }

    /**
     * Map notification type to a human-readable label.
     */
    private String formatTypeLabel(NotificationType type) {
        return switch (type) {
            case FOOD_AVAILABLE -> "Food Available";
            case FOOD_CLAIMED -> "Food Claimed";
            case PICKUP_SCHEDULED -> "Pickup Scheduled";
            case PICKUP_COMPLETED -> "Pickup Completed";
            case PICKUP_CANCELLED -> "Pickup Cancelled";
            case SYSTEM_ALERT -> "System Alert";
        };
    }

    /**
     * Map notification type to a brand-appropriate color for email badges.
     */
    private String getTypeColor(NotificationType type) {
        return switch (type) {
            case FOOD_AVAILABLE -> "#4CAF50";
            case FOOD_CLAIMED -> "#2196F3";
            case PICKUP_SCHEDULED -> "#FF9800";
            case PICKUP_COMPLETED -> "#8BC34A";
            case PICKUP_CANCELLED -> "#F44336";
            case SYSTEM_ALERT -> "#9E9E9E";
        };
    }
}
