package com.idea.authservice.api.patterns.factory;

import com.idea.authservice.api.model.Match;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@DiscriminatorValue("BASKET")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class BasketballMatch extends Match {
    private int quarters;
}
