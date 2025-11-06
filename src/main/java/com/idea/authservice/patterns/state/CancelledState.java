package com.idea.authservice.patterns.state;

import com.idea.authservice.model.Match;
import lombok.extern.slf4j.Slf4j;
import com.idea.authservice.model.enums.MatchStatus;

@Slf4j
public class CancelledState implements MatchState {
    @Override
    public String start(Match match) {
        return "No se puede cancelar un partido finalizado.";
    }

    @Override
    public String finish(Match match) {
        return "No se puede terminar un partido finalizado.";
    }

    @Override
    public String cancel(Match match) {
        log.info("[State:Cancelled] cancel() called for matchId={}", match.getId());
        match.setStatus(MatchStatus.CANCELLED);
        return "No se puede cancelar un partido cancelado.";
    }
}
