package ec.edu.ups.icc.events.auth.services;

import ec.edu.ups.icc.events.auth.dtos.AuthResponseDto;
import ec.edu.ups.icc.events.auth.dtos.LoginRequestDto;
import ec.edu.ups.icc.events.auth.dtos.RegisterRequestDto;

public interface AuthService {
    AuthResponseDto register(RegisterRequestDto dto);
    AuthResponseDto login(LoginRequestDto dto, String ipAddress);
    AuthResponseDto refresh(String authHeader);
    void logout(String authHeader);
}
