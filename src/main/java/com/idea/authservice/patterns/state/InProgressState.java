package com.idea.authservice.patterns.state;

import com.idea.authservice.model.Match;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.idea.authservice.model.enums.MatchStatus;

@AllArgsConstructor
@Slf4j
public class InProgressState implements MatchState{
    @Override
    public String start(Match match) {
        return "El partido ya está en curso";
    }

    @Override
    public String finish(Match match) {
        log.info("[State:InProgress] finish() -> FinishedState for matchId={}", match.getId());
        match.setState(new FinishedState());
        match.setStatus(MatchStatus.FINISHED);
        return "El partido terminó";
    }

    @Override
    public String cancel(Match match) {
        log.info("[State:InProgress] cancel() -> CancelledState for matchId={}", match.getId());
        match.setState(new CancelledState());
        match.setStatus(MatchStatus.CANCELLED);
        return "Partido cancelado durante el juego";
    }
}
