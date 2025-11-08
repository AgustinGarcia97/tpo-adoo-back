package com.idea.authservice.api.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import com.idea.authservice.api.model.enums.MatchStatus;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MatchListDTO {
    private Long id;
    private LocalDateTime dateTime;
    private String location;
    private String region;
    private String sport;
    private UUID creatorId;
    private String level;
    private MatchStatus status;
    private String joinStrategyKey;
    private Integer playersCount;
    private Boolean joined;
}


