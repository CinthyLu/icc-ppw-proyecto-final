package ec.edu.ups.icc.events.auth.dtos;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Respuesta de autenticación que contiene el token JWT")
public class AuthResponseDto {

    @Schema(description = "Token de acceso JWT en formato Bearer", example = "eyJhbGciOiJIUzI1NiJ9...")
    private String token;

    @Schema(description = "Token de refresco de larga duración", example = "d2d7c58f-410c...")
    private String refreshToken;

    public AuthResponseDto() {
    }

    public AuthResponseDto(String token) {
        this.token = token;
    }

    public AuthResponseDto(String token, String refreshToken) {
        this.token = token;
        this.refreshToken = refreshToken;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }
}
