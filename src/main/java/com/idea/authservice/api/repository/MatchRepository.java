package com.idea.authservice.api.repository;

import com.idea.authservice.api.dtos.MatchListDTO;
import com.idea.authservice.api.model.enums.MatchStatus;
import com.idea.authservice.api.model.Match;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface MatchRepository extends JpaRepository <Match, Long> {
    public List<Match> findBySport(String sport);
    
    @Query("select new com.idea.authservice.api.dtos.MatchListDTO(m.id, m.dateTime, m.location, m.region, m.sport, m.creatorId, m.level, m.status, m.joinStrategyKey, size(m.players), (case when (exists (select 1 from m.players p where p.id = :userId)) then true else false end)) from Match m")
    List<MatchListDTO> findAllForList(@Param("userId") java.util.UUID userId);
    
    @Query("select m from Match m left join fetch m.players where m.id = :id")
    Optional<Match> findByIdWithPlayers(@Param("id") Long id);

    @Query("select m from Match m where m.status in :statuses and m.dateTime <= :now")
    List<Match> findToAutoStart(@Param("now") LocalDateTime now, @Param("statuses") java.util.Collection<MatchStatus> statuses);

    @Query("select new com.idea.authservice.api.dtos.MatchListDTO(m.id, m.dateTime, m.location, m.region, m.sport, m.creatorId, m.level, m.status, m.joinStrategyKey, size(m.players), (case when (exists (select 1 from m.players p where p.id = :userId)) then true else false end)) " +
            "from Match m " +
            "where (:region is null or lower(m.region) like lower(concat('%', :region, '%'))) " +
            "and (:sport is null or lower(m.sport) = lower(:sport))")
    List<MatchListDTO> findForListByFilters(@Param("region") String region, @Param("sport") String sport, @Param("userId") java.util.UUID userId);

}
