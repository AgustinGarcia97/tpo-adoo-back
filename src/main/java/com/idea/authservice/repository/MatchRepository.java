package com.idea.authservice.repository;

import com.idea.authservice.model.Match;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MatchRepository extends JpaRepository <Match, Long> {
    public List<Match> findBySport(String sport);

}
