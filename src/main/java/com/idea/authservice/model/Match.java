package com.idea.authservice.model;

import com.idea.authservice.auth.domain.model.Player;
import com.idea.authservice.patterns.observer.MatchObserver;
import com.idea.authservice.patterns.observer.MatchSubject;
import com.idea.authservice.patterns.state.MatchState;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.idea.authservice.patterns.strategy.join.JoinStrategy;
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
@AllArgsConstructor
@NoArgsConstructor
@Slf4j
public class Match implements MatchSubject {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private LocalDateTime dateTime;
    private String location;
    private String region;
    private UUID creatorId;
    @Transient
    @JsonIgnore
    private MatchState state;
    private String level;
    private String msg;
    private String sport;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MatchStatus status;
    @OneToMany(mappedBy = "match", cascade = CascadeType.ALL)
    private List<Player> players = new ArrayList<>();
    @Transient
    private final List<MatchObserver> observers = new ArrayList<>();

    @Transient
    @JsonIgnore
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
            this.state = new com.idea.authservice.patterns.state.CreatedState();
            log.info("[State] Inicializado estado CreatedState para matchId={}", this.id);
        }
        if (this.status == null) {
            this.status = MatchStatus.CREATED;
        }
    }
}
