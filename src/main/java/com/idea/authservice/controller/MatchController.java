package com.idea.authservice.controller;



import com.idea.authservice.dtos.MatchDTO;
import com.idea.authservice.service.MatchService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/matches")
@RequiredArgsConstructor
public class MatchController {

    private final MatchService matchService;

    // -------------------------------
    // 🔹 1. Crear un nuevo partido (Factory)
    // -------------------------------
    @PostMapping
    public ResponseEntity<MatchDTO> createMatch(@RequestBody MatchDTO matchDTO) {
        MatchDTO created = matchService.createMatch(matchDTO);
        return ResponseEntity.ok(created);
    }

    // -------------------------------
    // 🔹 2. Unirse a un partido (Strategy)
    // -------------------------------
    @PostMapping("/{matchId}/join")
    public ResponseEntity<?> joinMatch(
            @PathVariable Long matchId,
            @RequestBody Map<String, Object> requestBody
    ) {
        try {
            UUID playerId = UUID.fromString(requestBody.get("playerId").toString());
            String strategy = requestBody.get("strategy").toString();

            boolean joined = matchService.joinMatch(matchId, playerId, strategy);
            if (joined) {
                return ResponseEntity.ok(Map.of("message", "Jugador unido correctamente"));
            } else {
                return ResponseEntity.badRequest().body(Map.of("message", "No se pudo unir al partido"));
            }
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // -------------------------------
    // 🔹 3. Cambiar estados (State)
    // -------------------------------
    @PutMapping("/{matchId}/start")
    public ResponseEntity<?> startMatch(@PathVariable Long matchId) {
        String msg = matchService.startMatch(matchId);
        return ResponseEntity.ok(Map.of("message", msg));
    }

    @PutMapping("/{matchId}/cancel")
    public ResponseEntity<?> cancelMatch(@PathVariable Long matchId) {
        String msg = matchService.cancelMatch(matchId);
        return ResponseEntity.ok(Map.of("message", msg));
    }

    @PutMapping("/{matchId}/finish")
    public ResponseEntity<?> finishMatch(@PathVariable Long matchId) {
        String msg = matchService.finishMatch(matchId);
        return ResponseEntity.ok(Map.of("message", msg));
    }

    // -------------------------------
    // 🔹 4. Consultas generales
    // -------------------------------
    @GetMapping
    public ResponseEntity<List<MatchDTO>> getAllMatches() {
        return ResponseEntity.ok(matchService.getAllMatches());
    }

    @GetMapping("/{id}")
    public ResponseEntity<MatchDTO> getMatchById(@PathVariable Long id) {
        MatchDTO match = matchService.getMatchById(id);
        return match != null ? ResponseEntity.ok(match) : ResponseEntity.notFound().build();
    }

    @GetMapping("/sport/{sport}")
    public ResponseEntity<List<MatchDTO>> getMatchesBySport(@PathVariable String sport) {
        return ResponseEntity.ok(matchService.getMatchesBySport(sport));
    }

}

