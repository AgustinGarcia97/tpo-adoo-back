package com.idea.authservice.api.patterns.strategy.join;

import com.idea.authservice.auth.domain.model.Player;
import com.idea.authservice.api.model.Match;
import org.springframework.stereotype.Component;
import lombok.extern.slf4j.Slf4j;

@Component("skill")
@Slf4j
public class SkillBased implements JoinStrategy {
    @Override
    public boolean canJoin(Player player, Match match) {
        boolean result = player != null && match != null
                && player.getNivelJugador() != null && match.getLevel() != null
                && player.getNivelJugador().trim().equalsIgnoreCase(match.getLevel().trim());
        log.info("[Strategy:skill] playerLevel={} matchLevel={} -> {}",
                player != null ? player.getNivelJugador() : null,
                match != null ? match.getLevel() : null,
                result);
        return result;
    }
}
