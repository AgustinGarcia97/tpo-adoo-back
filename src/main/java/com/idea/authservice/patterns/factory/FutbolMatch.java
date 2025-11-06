package com.idea.authservice.patterns.factory;

import com.idea.authservice.model.Match;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

@Entity
@DiscriminatorValue("FUTBOL")
public class FutbolMatch extends Match {
    private int teamSize;
    private String fieldType;
}
