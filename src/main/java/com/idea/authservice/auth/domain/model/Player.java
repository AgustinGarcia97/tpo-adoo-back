package com.idea.authservice.auth.domain.model;

import com.idea.authservice.auth.domain.model.enums.Role;
import com.idea.authservice.api.model.Match;
import jakarta.persistence.*;
import lombok.*;
import lombok.ToString;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import com.fasterxml.jackson.annotation.JsonIgnore;


import java.util.Collection;
import java.util.List;
import java.util.UUID;


@Entity
@Table(name="users")
@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Player implements UserDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @EqualsAndHashCode.Include
    private UUID id;
    @Column(unique=true, nullable=false)
    private String username;
    @Column(nullable=false)
    @JsonIgnore
    private String password;
    @Enumerated(EnumType.STRING)
    @Column(nullable=false)
    private Role role;
    @Column(nullable=false, name="nivel_jugador")
    private String nivelJugador;
    @Column(nullable=false)
    private String location;
    private String phone;
    @Column(name="favorite_sport")
    private String favoriteSport;
    @ManyToMany(mappedBy = "players")
    @ToString.Exclude
    @JsonIgnore
    private java.util.Set<Match> matches = new java.util.HashSet<>();

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority(role.name()));
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return UserDetails.super.isAccountNonExpired();
    }

    @Override
    public boolean isAccountNonLocked() {
        return UserDetails.super.isAccountNonLocked();
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return UserDetails.super.isCredentialsNonExpired();
    }

    @Override
    public boolean isEnabled() {
        return UserDetails.super.isEnabled();
    }
}
