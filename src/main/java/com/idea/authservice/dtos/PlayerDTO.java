package com.idea.authservice.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PlayerDTO {
    private UUID id;
    private String username;
    private String password;
    private String phone;
}
