package com.idea.authservice.patterns.strategy.join;

import com.idea.authservice.auth.domain.model.Player;
import com.idea.authservice.model.Match;

public interface JoinStrategy {
    public boolean canJoin(Player player, Match match);
}
