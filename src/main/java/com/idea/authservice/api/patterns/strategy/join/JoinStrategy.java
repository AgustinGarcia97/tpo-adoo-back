package com.idea.authservice.api.patterns.strategy.join;

import com.idea.authservice.auth.domain.model.Player;
import com.idea.authservice.api.model.Match;

public interface JoinStrategy {
    public boolean canJoin(Player player, Match match);
}
