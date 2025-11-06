package com.idea.authservice.patterns.decorator;

import com.idea.authservice.model.Match;
import com.idea.authservice.patterns.observer.MatchObserver;
import org.springframework.stereotype.Component;


public interface INotifier extends MatchObserver {
    void notify(Match match, String message);
}
