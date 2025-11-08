package com.idea.authservice.auth.application.request.auth;




import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RegisterRequest {


    private String password;
    private String username;
    private String nivelJugador;
    private String location;
    private String phone;
    private String favoriteSport;

}
