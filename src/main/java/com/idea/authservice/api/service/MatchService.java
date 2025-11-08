package com.idea.authservice.api.service;

import com.idea.authservice.api.dtos.MatchListDTO;
import com.idea.authservice.api.model.enums.MatchStatus;
import com.idea.authservice.api.patterns.decorator.INotifier;
import com.idea.authservice.api.patterns.factory.MatchFactory;
import com.idea.authservice.api.patterns.strategy.join.JoinStrategy;
import com.idea.authservice.auth.domain.model.Player;
import com.idea.authservice.auth.domain.repository.UserRepository;
import com.idea.authservice.api.dtos.MatchDTO;
import com.idea.authservice.api.model.Match;
import com.idea.authservice.api.repository.MatchRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;
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
    private final INotifier notifier;
    private final UserRepository userRepository;


    //LLAMA A FACTORY
    public MatchDTO createMatch(MatchDTO matchDTO) {
        log.info("[Service] Solicitud de creación de partido sport={} location={}", matchDTO.getSport(), matchDTO.getLocation());
        Match m = matchFactory.CreateMatch(matchDTO);

        m.addObserver(notifier);

        if (matchDTO.getCreatorId() != null) {
            Player creator = playerService.findById(matchDTO.getCreatorId());
            if (creator != null) {
                if (m.getPlayers() == null) {
                    m.setPlayers(new java.util.HashSet<>());
                }
                m.getPlayers().add(creator);
            }
        }

        if (m.getStatus() == null) {
            m.setStatus(MatchStatus.CREATED);
        }
        matchRepository.save(m);
        log.info("[Service] Partido creado id={}", m.getId());

        // Notificar a jugadores con deporte favorito
        if (m.getSport() != null) {
            List<Player> interested = userRepository.findByFavoriteSportIgnoreCase(m.getSport());
            if (interested != null && !interested.isEmpty()) {
                String creationMsg = "Nuevo partido de tu deporte favorito (" + m.getSport() + ") en " + m.getLocation() +
                        " el " + String.valueOf(m.getDateTime()) + ".";
                notifier.notifyUsers(interested, creationMsg);
            }
        }
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

        // Si el partido tiene estrategia fija, forzarla
        String effectiveStrategyKey = match.getJoinStrategyKey() != null ? match.getJoinStrategyKey() : strategy.toLowerCase();
        JoinStrategy strategySelected = strategies.get(effectiveStrategyKey);
        if (strategySelected == null) {
            throw new IllegalArgumentException("Estrategia no soportada: " + effectiveStrategyKey);
        }
        log.info("[Service] Intento de join: matchId={} playerId={} strategy={} (effective={})", matchId, playerId, strategy, effectiveStrategyKey);

        if (match.getPlayers() == null) {
            match.setPlayers(new java.util.HashSet<>());
        }


        match.setJoinStrategy(strategySelected);
        log.info("[Service] Strategy asignada al match: {}", effectiveStrategyKey);


        if (match.getPlayers().contains(p)) {
            throw new IllegalArgumentException("El jugador ya está unido a este partido");
        }

        if (match.getJoinStrategy().canJoin(p, match)) {
            match.getPlayers().add(p);
            // Si se alcanzo el cupo requerido, marcar como ARMED
            Integer required = match.getRequiredPlayers();
            int current = match.getPlayers() != null ? match.getPlayers().size() : 0;
            if (required != null && required > 0 && current >= required && match.getStatus() == MatchStatus.CREATED) {
                log.info("[Service] Cupo alcanzado ({} / {}), marcando partido {} como ARMED", current, required, match.getId());
                match.setStatus(MatchStatus.ARMED);
                // Al rearmar desde JPA hay que volver a adjuntar el notifier
                match.addObserver(notifier);
                match.notifyObservers("Se alcanzó el cupo de jugadores. El partido está armado.");
            }
            matchRepository.save(match);
            log.info("[Service] Join aceptado para player={} en match={}", p.getId(), match.getId());
            return true;
        }

        //construir motivo específico segun estrategia
        String reason;
        if ("skill".equalsIgnoreCase(effectiveStrategyKey)) {
            reason = "No coincide skill (jugador=" + p.getNivelJugador() + ", partido=" + match.getLevel() + ")";
        } else if ("location".equalsIgnoreCase(effectiveStrategyKey)) {
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

        //solo el creador puede iniciar
        String currentUsername = SecurityContextHolder.getContext().getAuthentication().getName();
        Player current = userRepository.findByUsername(currentUsername)
                .orElseThrow(() -> new AccessDeniedException("Usuario no encontrado"));
        if (!current.getId().equals(match.getCreatorId())) {
            throw new AccessDeniedException("Solo el creador puede iniciar el partido");
        }

        //deben ser al menos 2 jugadores (>=2)
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

    @Transactional
    public String confirmMatch(Long matchId) {
        Match match = matchRepository.findByIdWithPlayers(matchId)
                .orElseThrow(() -> new IllegalArgumentException("Partido no encontrado"));

        // solo el creador puede confirmar
        String currentUsername = SecurityContextHolder.getContext().getAuthentication().getName();
        Player current = userRepository.findByUsername(currentUsername)
                .orElseThrow(() -> new AccessDeniedException("Usuario no encontrado"));
        if (!current.getId().equals(match.getCreatorId())) {
            throw new AccessDeniedException("Solo el creador puede confirmar el partido");
        }

        if (match.getStatus() == MatchStatus.CANCELLED
                || match.getStatus() == MatchStatus.FINISHED) {
            return match.getMsg();
        }

        match.addObserver(notifier);
        match.setStatus(MatchStatus.CONFIRMED);
        match.notifyObservers("El partido fue confirmado por el organizador.");
        matchRepository.save(match);
        return "Partido confirmado";
    }
    public String cancelMatch(Long matchId) {
        Optional<Match> searched = getObjectMatchById(matchId);
        searched.ifPresent(m -> {
            if (m.getStatus() == MatchStatus.CANCELLED
                    || m.getStatus() == MatchStatus.FINISHED) {
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


    public List<MatchListDTO> getAllMatches() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        UUID userId = userRepository.findByUsername(username).map(Player::getId).orElse(null);
        if (userId == null) {
            return matchRepository.findAllForList(UUID.randomUUID());
        }
        return matchRepository.findAllForList(userId);
    }

    public List<MatchListDTO> searchMatches(String region, String sport, String strategy, Boolean availableOnly) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        Player me = userRepository.findByUsername(username).orElse(null);
        UUID userId = me != null ? me.getId() : UUID.randomUUID();
        List<MatchListDTO> base = matchRepository.findForListByFilters(region, sport, userId);
        //filtrar por disponibles y estrategia (skill/location) si corresponde
        return base.stream()
                .filter(m -> {
                    // disponibles no cancelados-terminados, no creados por mi no joined
                    if (Boolean.TRUE.equals(availableOnly)) {
                        boolean openStatus = m.getStatus() != com.idea.authservice.api.model.enums.MatchStatus.CANCELLED
                                && m.getStatus() != com.idea.authservice.api.model.enums.MatchStatus.FINISHED;
                        boolean notMine = me == null || !m.getCreatorId().equals(me.getId());
                        boolean notJoined = m.getJoined() == null || !m.getJoined();
                        if (!(openStatus && notMine && notJoined)) {
                            return false;
                        }
                    }
                    if (strategy == null || strategy.isBlank()) return true;
                    if (me == null) return true;
                    if ("skill".equalsIgnoreCase(strategy)) {
                        return me.getNivelJugador() != null && me.getNivelJugador().equalsIgnoreCase(m.getLevel());
                    }
                    if ("location".equalsIgnoreCase(strategy)) {
                        return me.getLocation() != null && me.getLocation().equalsIgnoreCase(m.getLocation());
                    }
                    return true;
                })
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
