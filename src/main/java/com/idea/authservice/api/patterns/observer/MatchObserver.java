package com.idea.authservice.api.patterns.observer;

import com.idea.authservice.api.model.Match;

public interface MatchObserver {
    void update(Match match, String message);
}
