package com.idea.authservice.patterns.strategy.join;

import com.idea.authservice.auth.domain.model.Player;
import com.idea.authservice.model.Match;
import org.springframework.stereotype.Component;
import lombok.extern.slf4j.Slf4j;

@Component("location")
@Slf4j
public class LocationBased implements JoinStrategy {
    @Override
    public boolean canJoin(Player player, Match match) {
        boolean result = player != null && match != null && player.getLocation() != null && player.getLocation().equals(match.getLocation());
        log.info("[Strategy:location] playerLocation={} matchLocation={} -> {}",
                player != null ? player.getLocation() : null,
                match != null ? match.getLocation() : null,
                result);
        return result;
    }
}
