package com.idea.authservice.api.patterns.decorator;

import com.idea.authservice.api.patterns.observer.MatchObserver;
import com.idea.authservice.api.model.Match;
import com.idea.authservice.auth.domain.model.Player;


public interface INotifier extends MatchObserver {
    void notify(Match match, String message);
    void notifyUsers(java.util.Collection<Player> recipients, String message);
}
