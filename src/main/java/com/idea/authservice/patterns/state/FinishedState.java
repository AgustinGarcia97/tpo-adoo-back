package com.idea.authservice.patterns.state;

import com.idea.authservice.model.Match;
import lombok.extern.slf4j.Slf4j;
import com.idea.authservice.model.enums.MatchStatus;

@Slf4j
public class FinishedState implements MatchState{
    @Override
    public String start(Match match) {
        return "No se puede empezar algo que ya terminó...";
    }

    @Override
    public String finish(Match match) {
        log.info("[State:Finished] finish() called for matchId={}", match.getId());
        match.setStatus(MatchStatus.FINISHED);
        return "El partido ya está finalizado";
    }

    @Override
    public String cancel(Match match) {
        return "No se puede cancelar un partido finalizado";
    }
}
