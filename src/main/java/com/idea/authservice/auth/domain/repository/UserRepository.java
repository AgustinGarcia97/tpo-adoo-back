package com.idea.authservice.auth.domain.repository;

import com.idea.authservice.auth.domain.model.Player;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<Player, UUID> {

    Optional<Player> findByUsername(String username);

    List<Player> findByFavoriteSportIgnoreCase(String favoriteSport);
}
