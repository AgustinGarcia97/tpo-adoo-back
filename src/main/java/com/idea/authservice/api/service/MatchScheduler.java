package com.idea.authservice.api.service;

import com.idea.authservice.api.patterns.decorator.INotifier;
import com.idea.authservice.api.model.Match;
import com.idea.authservice.api.model.enums.MatchStatus;
import com.idea.authservice.api.repository.MatchRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class MatchScheduler {

    private final MatchRepository matchRepository;
    private final INotifier notifier;


    @Scheduled(fixedDelay = 60_000L)
    public void autoStartDueMatches() {
        List<MatchStatus> statuses = Arrays.asList(MatchStatus.ARMED, MatchStatus.CONFIRMED);
        List<Match> due = matchRepository.findToAutoStart(LocalDateTime.now(), statuses);
        if (due.isEmpty()) {
            return;
        }
        log.info("[Scheduler] Encontrados {} partidos vencidos para auto-iniciar", due.size());
        for (Match m : due) {
            try {
                m.addObserver(notifier);
                m.start(); // usa State -> pasa a IN_PROGRESS y notifica
                matchRepository.save(m);
                log.info("[Scheduler] Partido {} auto-iniciado", m.getId());
            } catch (Exception ex) {
                log.warn("[Scheduler] No se pudo auto-iniciar partido {}: {}", m.getId(), ex.getMessage());
            }
        }
    }
}


