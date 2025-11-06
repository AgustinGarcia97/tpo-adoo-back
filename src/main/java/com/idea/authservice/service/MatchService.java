package com.idea.authservice.service;

import com.idea.authservice.auth.domain.model.Player;
import com.idea.authservice.dtos.MatchDTO;
import com.idea.authservice.model.Match;
import com.idea.authservice.patterns.factory.MatchFactory;
import com.idea.authservice.patterns.strategy.join.JoinStrategy;
import com.idea.authservice.repository.MatchRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
@AllArgsConstructor
@Slf4j
public class MatchService {

    private final MatchFactory matchFactory;
    private final Map<String, JoinStrategy> strategies;
    private final MatchRepository matchRepository;
    private final PlayerService playerService;
    private ModelMapper modelMapper;
    private final com.idea.authservice.patterns.decorator.INotifier notifier;


    //LLAMA A FACTORY
    public MatchDTO createMatch(MatchDTO matchDTO) {
        log.info("[Service] Solicitud de creación de partido sport={} location={}", matchDTO.getSport(), matchDTO.getLocation());
        Match m = matchFactory.CreateMatch(matchDTO);
        // add observer for notifications
        m.addObserver(notifier);
        matchRepository.save(m);
        log.info("[Service] Partido creado id={}", m.getId());
        return modelMapper.map(m, MatchDTO.class);
    }

    //LLAMA A STRATEGY
    public boolean joinMatch(Long matchId, UUID playerId, String strategy) {
        if (strategy == null) {
            throw new IllegalArgumentException("Estrategia requerida");
        }
        Player p = playerService.findById(playerId);
        if (p == null) {
            throw new IllegalArgumentException("Jugador no encontrado");
        }
        Match match = matchRepository.findById(matchId)
                .orElseThrow(() -> new IllegalArgumentException("Partido no encontrado"));

        JoinStrategy strategySelected = strategies.get(strategy.toLowerCase());
        if (strategySelected == null) {
            throw new IllegalArgumentException("Estrategia no soportada: " + strategy);
        }
        log.info("[Service] Intento de join: matchId={} playerId={} strategy={}", matchId, playerId, strategy);

        if (match.getPlayers() == null) {
            match.setPlayers(new java.util.ArrayList<>());
        }

        // assign strategy transiently to the match (not persisted) for demonstration
        match.setJoinStrategy(strategySelected);
        log.info("[Service] Strategy asignada al match: {}", strategy);

        if (match.getJoinStrategy().canJoin(p, match)) {
            p.setMatch(match);
            match.getPlayers().add(p);
            matchRepository.save(match);
            log.info("[Service] Join aceptado para player={} en match={}", p.getId(), match.getId());
            return true;
        }

        log.info("[Service] Join rechazado para player={} en match={}", p.getId(), match.getId());
        return false;
    }


    //PATRON STATE
    public String startMatch(Long matchId) {
        Optional<Match> searched = getObjectMatchById(matchId);
        searched.ifPresent(m -> {
            m.addObserver(notifier);
            m.start();
        });
        matchRepository.save(searched.orElseThrow());
        return searched.get().getMsg();

    }

    public String cancelMatch(Long matchId) {
        Optional<Match> searched = getObjectMatchById(matchId);
        searched.ifPresent(m -> {
            m.addObserver(notifier);
            m.cancel();
        });
        matchRepository.save(searched.orElseThrow());
        return searched.get().getMsg();
    }

    public String finishMatch(Long matchId) {
        Optional<Match> searched = getObjectMatchById(matchId);
        searched.ifPresent(m -> {
            m.addObserver(notifier);
            m.finish();
        });
        matchRepository.save(searched.orElseThrow());
        return searched.get().getMsg();
    }


    public List<MatchDTO> getAllMatches() {
        List<Match> matches = matchRepository.findAll();
        return matches
                .stream()
                .map(match -> modelMapper.map(match, MatchDTO.class))
                .toList();
    }

    public MatchDTO getMatchById(Long id) {
        return matchRepository.findById(id)
                .map(match -> modelMapper.map(match, MatchDTO.class))
                .orElse(null);
    }

    public Optional<Match> getObjectMatchById(Long id) {
        return matchRepository.findById(id);
    }

    public List<MatchDTO> getMatchesBySport(String sport) {
        List<Match> matchesBySport = matchRepository.findBySport(sport);
        return matchesBySport
                .stream()
                .map(match -> modelMapper.map(match, MatchDTO.class))
                .toList();

    }

}
