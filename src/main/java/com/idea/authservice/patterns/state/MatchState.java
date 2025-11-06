package com.idea.authservice.patterns.state;

import com.idea.authservice.model.Match;

public interface MatchState {
    public String start(Match match);
    public String finish(Match match);
    public String cancel(Match match);
}
