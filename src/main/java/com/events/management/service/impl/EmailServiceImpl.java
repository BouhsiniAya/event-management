package com.events.management.service.impl;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class EmailServiceImpl {

    private static final Logger log =
            LoggerFactory.getLogger(EmailServiceImpl.class);

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    public EmailServiceImpl(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    @Async
    public void sendTicketConfirmation(
            String toEmail,
            String userName,
            String eventTitle,
            String ticketCode,
            byte[] qrCodeBytes) {

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper =
                    new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject("🎫 Votre billet pour " + eventTitle);
            helper.setText(
                buildEmailHtml(userName, eventTitle, ticketCode), true
            );

            helper.addAttachment(
                    "qrcode_" + ticketCode + ".png",
                    new ByteArrayResource(qrCodeBytes),
                    "image/png"
            );

            mailSender.send(message);
            log.info("Email envoyé à : {}", toEmail);

        } catch (MessagingException e) {
            log.error("Erreur email : {}", e.getMessage());
        }
    }

    private String buildEmailHtml(String userName,
                                   String eventTitle,
                                   String ticketCode) {
        return "<!DOCTYPE html><html><body style='font-family:Arial;'>"
                + "<div style='background:#667eea;padding:20px;text-align:center;'>"
                + "<h1 style='color:white;'>🎫 Votre Billet</h1></div>"
                + "<div style='padding:30px;'>"
                + "<h2>Bonjour " + userName + ",</h2>"
                + "<p>Réservation confirmée pour <strong>"
                + eventTitle + "</strong></p>"
                + "<div style='background:#f0f4ff;padding:20px;"
                + "border-radius:10px;border-left:4px solid #667eea;'>"
                + "<p><strong>Code :</strong></p>"
                + "<p style='font-size:18px;font-weight:bold;"
                + "color:#667eea;letter-spacing:2px;'>"
                + ticketCode + "</p></div>"
                + "<p>QR Code en pièce jointe.</p>"
                + "</div></body></html>";
    }
}