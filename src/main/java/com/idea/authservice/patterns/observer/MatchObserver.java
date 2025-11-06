package com.idea.authservice.patterns.observer;

import com.idea.authservice.model.Match;

public interface MatchObserver {
    void update(Match match, String message);
}
