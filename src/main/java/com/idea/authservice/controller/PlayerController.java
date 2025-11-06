package com.idea.authservice.controller;

import com.idea.authservice.auth.domain.model.Player;
import com.idea.authservice.service.PlayerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/players")
@RequiredArgsConstructor
public class PlayerController {

    private final PlayerService playerService;

    // Creación de jugadores se realiza vía /api/v1/auth/register

    // -------------------------------
    // 🔹 2. Obtener jugador por ID
    // -------------------------------
    @GetMapping("/{id}")
    public ResponseEntity<Player> getPlayerById(@PathVariable UUID id) {
        Player player = playerService.findById(id);
        return player != null ? ResponseEntity.ok(player) : ResponseEntity.notFound().build();
    }

    // -------------------------------
    // 🔹 3. Obtener todos los jugadores
    // -------------------------------
    @GetMapping
    public ResponseEntity<List<Player>> getAllPlayers() {
        List<Player> players = playerService.getAllPlayers();
        return ResponseEntity.ok(players);
    }

    // -------------------------------
    // 🔹 4. Eliminar jugador
    // -------------------------------
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deletePlayer(@PathVariable UUID id) {
        boolean deleted = playerService.deletePlayer(id);
        if (deleted) {
            return ResponseEntity.ok().body("Jugador eliminado correctamente");
        } else {
            return ResponseEntity.badRequest().body("No se encontró el jugador con ID: " + id);
        }
    }
}
