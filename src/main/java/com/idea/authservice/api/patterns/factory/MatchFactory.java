package com.idea.authservice.api.patterns.factory;

import com.idea.authservice.api.dtos.MatchDTO;
import com.idea.authservice.api.model.Match;
import org.springframework.stereotype.Component;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Component
@Slf4j
public class MatchFactory {

    private final Map<String, Function<MatchDTO, Match>> creators = new HashMap<>();

    public MatchFactory() {
        creators.put("futbol", dto -> {
            FutbolMatch m = new FutbolMatch();
            fillCommon(m, dto);
            return m;
        });
        creators.put("basket", dto -> {
            BasketballMatch m = new BasketballMatch();
            fillCommon(m, dto);
            if (dto.getQuarters() != null) {
                m.setQuarters(dto.getQuarters());
            }
            return m;
        });
    }

    public Match CreateMatch(MatchDTO matchDTO){
        if (matchDTO == null || matchDTO.getSport() == null) {
            throw new IllegalArgumentException("Datos de partido inválidos");
        }
        log.info("[Factory] Creando partido para deporte: {}", matchDTO.getSport());
        Function<MatchDTO, Match> creator = creators.get(matchDTO.getSport().toLowerCase());
        if (creator == null) {
            throw new IllegalArgumentException("Deporte no soportado: " + matchDTO.getSport());
        }
        Match m = creator.apply(matchDTO);
        log.info("[Factory] Partido creado: tipo={}, location={}, level={} ", m.getClass().getSimpleName(), m.getLocation(), m.getLevel());
        return m;



    }

    private void fillCommon(Match m, MatchDTO dto) {
        m.setDateTime(dto.getDateTime());
        m.setLocation(dto.getLocation());
        m.setRegion(dto.getRegion());
        m.setSport(dto.getSport());
        m.setCreatorId(dto.getCreatorId());
        m.setLevel(dto.getLevel());
        m.setRequiredPlayers(dto.getRequiredPlayers());
        m.setDurationMinutes(dto.getDurationMinutes());
        m.setJoinStrategyKey(dto.getJoinStrategyKey());
        log.debug("[Factory] Campos comunes seteados para deporte={} creatorId={}", dto.getSport(), dto.getCreatorId());
    }
}
