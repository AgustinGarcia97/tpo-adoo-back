package com.idea.authservice.api.patterns.decorator;

import com.idea.authservice.api.model.Match;
import com.idea.authservice.auth.domain.model.Player;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@AllArgsConstructor
@Slf4j
public class WhatsAppNotifier implements INotifier {
    private final INotifier wrappee;

    @Override
    public void notify(Match match, String message) {
        wrappee.notify(match, message);
        log.info("[Notifier:WhatsApp] {}", message);
    }

    @Override
    public void notifyUsers(java.util.Collection<Player> recipients, String message) {
        wrappee.notifyUsers(recipients, message);
        if (recipients != null) {
            recipients.forEach(player ->
                    log.info("[WhatsApp] Simulado envío a {}: {}", player.getPhone(), message)
            );
        } else {
            log.info("[WhatsApp] Simulado envío: {}", message);
        }
    }

    @Override
    public void update(Match match, String message) {
        wrappee.update(match, message);
        if (match.getPlayers() != null) {
            match.getPlayers().forEach(player ->
                    log.info("[WhatsApp] Simulado envío a {}: {}", player.getPhone(), message)
            );
        } else {
            log.info("[WhatsApp] Simulado envío: {}", message);
        }
    }
}


