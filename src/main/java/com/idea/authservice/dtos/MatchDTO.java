package com.idea.authservice.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MatchDTO {
    private Long id;
    private List<PlayerDTO> players;
    private LocalDateTime dateTime;
    private String location;
    private String region;
    private String sport;
    private UUID creatorId;
    private String level;
    private String status;

}
