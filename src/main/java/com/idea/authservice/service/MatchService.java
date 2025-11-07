package com.idea.authservice.service;

import com.idea.authservice.auth.domain.model.Player;
import com.idea.authservice.auth.domain.repository.UserRepository;
import com.idea.authservice.dtos.MatchDTO;
import com.idea.authservice.model.Match;
import com.idea.authservice.patterns.factory.MatchFactory;
import com.idea.authservice.patterns.strategy.join.JoinStrategy;
import com.idea.authservice.repository.MatchRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.idea.authservice.model.enums.MatchStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;
import com.idea.authservice.model.enums.MatchStatus;
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
    private final UserRepository userRepository;


    //LLAMA A FACTORY
    public MatchDTO createMatch(MatchDTO matchDTO) {
        log.info("[Service] Solicitud de creación de partido sport={} location={}", matchDTO.getSport(), matchDTO.getLocation());
        Match m = matchFactory.CreateMatch(matchDTO);
        // add observer for notifications
        m.addObserver(notifier);
        // include creator as first player if the creator also plays
        if (matchDTO.getCreatorId() != null) {
            Player creator = playerService.findById(matchDTO.getCreatorId());
            if (creator != null) {
                if (m.getPlayers() == null) {
                    m.setPlayers(new java.util.HashSet<>());
                }
                m.getPlayers().add(creator);
            }
        }
        // ensure initial status persisted
        if (m.getStatus() == null) {
            m.setStatus(MatchStatus.CREATED);
        }
        matchRepository.save(m);
        log.info("[Service] Partido creado id={}", m.getId());
        return modelMapper.map(m, MatchDTO.class);
    }

    //LLAMA A STRATEGY
    @Transactional
    public boolean joinMatch(Long matchId, UUID playerId, String strategy) {
        if (strategy == null) {
            throw new IllegalArgumentException("Estrategia requerida");
        }
        Player p = playerService.findById(playerId);
        if (p == null) {
            throw new IllegalArgumentException("Jugador no encontrado");
        }
        Match match = matchRepository.findByIdWithPlayers(matchId)
                .orElseThrow(() -> new IllegalArgumentException("Partido no encontrado"));

        JoinStrategy strategySelected = strategies.get(strategy.toLowerCase());
        if (strategySelected == null) {
            throw new IllegalArgumentException("Estrategia no soportada: " + strategy);
        }
        log.info("[Service] Intento de join: matchId={} playerId={} strategy={}", matchId, playerId, strategy);

        if (match.getPlayers() == null) {
            match.setPlayers(new java.util.HashSet<>());
        }

        // assign strategy transiently to the match (not persisted) for demonstration
        match.setJoinStrategy(strategySelected);
        log.info("[Service] Strategy asignada al match: {}", strategy);

        // evitar doble unión
        if (match.getPlayers().contains(p)) {
            throw new IllegalArgumentException("El jugador ya está unido a este partido");
        }

        if (match.getJoinStrategy().canJoin(p, match)) {
            match.getPlayers().add(p);
            matchRepository.save(match);
            log.info("[Service] Join aceptado para player={} en match={}", p.getId(), match.getId());
            return true;
        }

        // construir motivo específico según estrategia
        String reason;
        if ("skill".equalsIgnoreCase(strategy)) {
            reason = "No coincide skill (jugador=" + p.getNivelJugador() + ", partido=" + match.getLevel() + ")";
        } else if ("location".equalsIgnoreCase(strategy)) {
            reason = "No coincide ubicación (jugador=" + p.getLocation() + ", partido=" + match.getLocation() + ")";
        } else {
            reason = "Criterio de unión no cumplido";
        }
        log.info("[Service] Join rechazado para player={} en match={}, motivo: {}", p.getId(), match.getId(), reason);
        throw new IllegalArgumentException(reason);
    }


    //PATRON STATE
    @Transactional
    public String startMatch(Long matchId) {
        Match match = matchRepository.findByIdWithPlayers(matchId)
                .orElseThrow(() -> new IllegalArgumentException("Partido no encontrado"));

        // regla: solo el creador puede iniciar
        String currentUsername = SecurityContextHolder.getContext().getAuthentication().getName();
        Player current = userRepository.findByUsername(currentUsername)
                .orElseThrow(() -> new AccessDeniedException("Usuario no encontrado"));
        if (!current.getId().equals(match.getCreatorId())) {
            throw new AccessDeniedException("Solo el creador puede iniciar el partido");
        }

        // regla: deben ser al menos 2 jugadores (>=2)
        int count = match.getPlayers() != null ? match.getPlayers().size() : 0;
        if (count < 2) {
            throw new IllegalArgumentException("Se requieren al menos 2 jugadores para iniciar");
        }

        if (match.getStatus() == MatchStatus.CANCELLED
                || match.getStatus() == MatchStatus.FINISHED) {
            return match.getMsg();
        }

        match.addObserver(notifier);
        match.start();
        matchRepository.save(match);
        return match.getMsg();
    }

    public String cancelMatch(Long matchId) {
        Optional<Match> searched = getObjectMatchById(matchId);
        searched.ifPresent(m -> {
            if (m.getStatus() == com.idea.authservice.model.enums.MatchStatus.CANCELLED
                    || m.getStatus() == com.idea.authservice.model.enums.MatchStatus.FINISHED) {
                return;
            }
            m.addObserver(notifier);
            m.cancel();
        });
        matchRepository.save(searched.orElseThrow());
        return searched.get().getMsg();
    }

    @Transactional
    public String finishMatch(Long matchId) {
        Match match = matchRepository.findByIdWithPlayers(matchId)
                .orElseThrow(() -> new IllegalArgumentException("Partido no encontrado"));

        // solo el creador puede finalizar
        String currentUsername = SecurityContextHolder.getContext().getAuthentication().getName();
        Player current = userRepository.findByUsername(currentUsername)
                .orElseThrow(() -> new AccessDeniedException("Usuario no encontrado"));
        if (!current.getId().equals(match.getCreatorId())) {
            throw new AccessDeniedException("Solo el creador puede finalizar el partido");
        }

        if (match.getStatus() == MatchStatus.CANCELLED
                || match.getStatus() == MatchStatus.FINISHED) {
            return match.getMsg();
        }

        match.addObserver(notifier);
        match.finish();
        matchRepository.save(match);
        return match.getMsg();
    }


    public List<com.idea.authservice.dtos.MatchListDTO> getAllMatches() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        UUID userId = userRepository.findByUsername(username).map(Player::getId).orElse(null);
        if (userId == null) {
            return matchRepository.findAllForList(UUID.randomUUID());
        }
        return matchRepository.findAllForList(userId);
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
