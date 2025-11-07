package com.idea.authservice.model;

import com.idea.authservice.auth.domain.model.Player;
import com.idea.authservice.patterns.observer.MatchObserver;
import com.idea.authservice.patterns.observer.MatchSubject;
import com.idea.authservice.patterns.state.MatchState;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.ToString;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.idea.authservice.patterns.strategy.join.JoinStrategy;
import com.idea.authservice.patterns.state.CreatedState;
import com.idea.authservice.model.enums.MatchStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name="matches")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "match_type")
@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@AllArgsConstructor
@NoArgsConstructor
@Slf4j
public class Match implements MatchSubject {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;
    private LocalDateTime dateTime;
    private String location;
    private String region;
    private UUID creatorId;
    @Transient
    @JsonIgnore
    @ToString.Exclude
    private MatchState state;
    private String level;
    private String msg;
    private String sport;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MatchStatus status;
    @ManyToMany
    @JoinTable(name = "match_players",
            joinColumns = @JoinColumn(name = "match_id"),
            inverseJoinColumns = @JoinColumn(name = "player_id"))
    private java.util.Set<Player> players = new java.util.HashSet<>();
    @Transient
    @ToString.Exclude
    private final List<MatchObserver> observers = new ArrayList<>();

    @Transient
    @JsonIgnore
    @ToString.Exclude
    private JoinStrategy joinStrategy;

    public void setState(MatchState state) {
        this.state = state;
        notifyObservers("El partido cambió de estado a " + state.getClass().getSimpleName());
    }

    public void start() {
        this.msg = state.start(this);

    }

    public void finish() {
        this.msg = state.finish(this);
    }

    public void cancel() {
        this.msg = state.cancel(this);
    }

    public void addObserver(MatchObserver observer) {
        observers.add(observer);
    }

    public void notifyObservers(String message) {
        log.info("[Observer] Notificando {} observer(s): {}", observers.size(), message);
        for (MatchObserver observer : observers) {
            observer.update(this, message);
        }
    }

    @PostLoad
    @PostPersist
    private void ensureStateInitialized() {
        if (this.state == null) {
            this.state = new CreatedState();
            log.info("[State] Inicializado estado CreatedState para matchId={}", this.id);
        }
        if (this.status == null) {
            this.status = MatchStatus.CREATED;
        }
    }
}
