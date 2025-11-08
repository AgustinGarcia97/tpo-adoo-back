package com.idea.authservice.api.repository;

import com.idea.authservice.auth.domain.model.Player;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface PlayerRepository extends JpaRepository<Player, UUID> {
}
