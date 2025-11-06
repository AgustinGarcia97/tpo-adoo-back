package com.idea.authservice.patterns.observer;

import com.idea.authservice.auth.domain.model.Player;
import com.idea.authservice.model.Match;
import com.idea.authservice.patterns.decorator.INotifier;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.AllArgsConstructor;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import lombok.extern.slf4j.Slf4j;

import java.util.List;


@AllArgsConstructor
@Slf4j
public class EmailNotifier implements INotifier {
    private final INotifier wrappee;
    private final JavaMailSender mailSender;
    private final String fromAddress;

    @Override
    public void update(Match match, String message) {
        this.wrappee.notify(match, message);
        sendEmails(match.getPlayers().stream().map(Player::getUsername).toList(), message);

    }

    private void sendEmails(List<String> emails, String message) {
        for (String email : emails) {
            try {
                MimeMessage mimeMessage = mailSender.createMimeMessage();
                MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, false);
                if (fromAddress != null && !fromAddress.isEmpty()) {
                    helper.setFrom(fromAddress);
                }
                helper.setTo(email);
                helper.setSubject("Notificación de partido ");
                helper.setText(message, false);
                mailSender.send(mimeMessage);
                log.info("[Notifier:Email] Email enviado a {}", email);
            } catch (MessagingException e) {
                log.error("[Notifier:Email] Error enviando email a {}: {}", email, e.getMessage());
            }
        }
    }

    @Override
    public void notify(Match match, String message) {
        this.wrappee.notify(match, message);
    }
}
