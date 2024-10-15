package be.iccbxl.tfe.Driveshare.service.serviceImpl;

import be.iccbxl.tfe.Driveshare.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender emailSender;

    @Autowired
    private UserService userService;

    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);

    public void sendNotificationEmail(String to, String subject, String text) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(to);
            message.setSubject(subject);
            message.setText(text);
            emailSender.send(message);
            logger.info("Email sent successfully to {}", to);
        } catch (Exception e) {
            logger.error("Failed to send email to {}: {}", to, e.getMessage());
        }
    }

    public void sendSimpleMessage(String email, String passwordResetRequest, String s) {

    }

    public void sendNotificationEmail(String[] adminEmails, String subject, String body) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(adminEmails);
            message.setSubject(subject);
            message.setText(body);
            emailSender.send(message);
            logger.info("Email sent successfully to {}", adminEmails);
        } catch (Exception e) {
            logger.error("Failed to send email to {}: {}", adminEmails, e.getMessage());
        }
    }

    public void sendEmailToUser(Long userId, String subject, String message) {
        // Récupérer l'email de l'utilisateur par son ID (ajustez selon votre logique)
        User user = userService.getUserById(userId);

        String toEmail = user.getEmail(); // Supposons que User a une méthode getEmail()

        // Créer un objet SimpleMailMessage
        SimpleMailMessage email = new SimpleMailMessage();
        email.setTo(toEmail);
        email.setSubject(subject);
        email.setText(message);
        email.setFrom("driveshare.admin@gmail.com"); // Changez ceci par votre adresse e-mail

        // Envoyer l'e-mail
        emailSender.send(email);
    }
}
