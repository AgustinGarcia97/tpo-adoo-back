package com.idea.authservice.api.patterns.observer;

import com.idea.authservice.api.model.Match;
import com.idea.authservice.api.patterns.decorator.INotifier;
import com.idea.authservice.auth.domain.model.Player;
import lombok.extern.slf4j.Slf4j;


@Slf4j
public class BasicNotifier implements INotifier {



    @Override
    public void notify(Match match,String message) {
        log.info("[Notifier:Basic] {}", message);
    }

    @Override
    public void notifyUsers(java.util.Collection<Player> recipients, String message) {
        log.info("[Notifier:Basic] {} destinatarios - {}", recipients != null ? recipients.size() : 0, message);
    }

    public void update(Match match, String message) {
        notify(match, message);
    }
}
