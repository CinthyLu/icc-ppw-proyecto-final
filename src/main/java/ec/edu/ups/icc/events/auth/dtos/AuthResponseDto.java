package ec.edu.ups.icc.events.auth.dtos;

public class AuthResponseDto {

    private String token;
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
