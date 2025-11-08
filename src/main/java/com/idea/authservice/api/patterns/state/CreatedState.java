package com.idea.authservice.api.patterns.state;

import com.idea.authservice.api.model.Match;
import lombok.extern.slf4j.Slf4j;
import com.idea.authservice.api.model.enums.MatchStatus;

@Slf4j
public class CreatedState implements MatchState{
    @Override
    public String start(Match match) {
        log.info("[State:Created] start() -> InProgressState for matchId={}", match.getId());
        match.setState(new InProgressState());
        match.setStatus(MatchStatus.IN_PROGRESS);
        match.notifyObservers("El partido " + match.getId() + " ha comenzado en " + match.getLocation());
        return "El partido ya ha iniciado";
    }

    @Override
    public String finish(Match match) {
        return "No se puede finalizar un partido que no comenzó.";
    }

    @Override
    public String cancel(Match match) {
        log.info("[State:Created] cancel() -> CancelledState for matchId={}", match.getId());
        match.setState(new CancelledState());
        match.setStatus(MatchStatus.CANCELLED);
        match.notifyObservers("El partido fue cancelado antes de comenzar.");
        return "Partido cancelado antes de empezar.";
    }
}
