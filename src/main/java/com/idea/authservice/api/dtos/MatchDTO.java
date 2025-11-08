package com.idea.authservice.api.dtos;

import com.idea.authservice.api.model.enums.MatchStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MatchDTO {
    private Long id;
    private Set<PlayerDTO> players;
    private LocalDateTime dateTime;
    private String location;
    private String region;
    private String sport;
    private UUID creatorId;
    private String level;
    private MatchStatus status;
    private Integer requiredPlayers;
    private Integer durationMinutes;
    private String joinStrategyKey;
    // solo basket
    private Integer quarters;

}
