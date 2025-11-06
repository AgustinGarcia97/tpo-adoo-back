package com.idea.authservice.auth.domain.model;

import com.idea.authservice.auth.domain.model.enums.Role;
import com.idea.authservice.model.Match;
import jakarta.persistence.*;
import lombok.*;
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
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Player implements UserDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
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
    @ManyToOne
    @JoinColumn(name = "match_id") //
    @JsonIgnore
    private Match match;

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
