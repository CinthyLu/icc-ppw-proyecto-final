package ec.edu.ups.icc.events.auth.mappers;

import ec.edu.ups.icc.events.auth.dtos.RegisterRequestDto;
import ec.edu.ups.icc.events.users.entities.UserEntity;

public class AuthMapper {

    public static UserEntity toEntity(RegisterRequestDto dto) {
        if (dto == null) {
            return null;
        }
        UserEntity user = new UserEntity();
        user.setEmail(dto.getUsername().trim().toLowerCase());
        // El nombre por defecto es el mismo username/correo o un valor genérico
        user.setName(dto.getUsername().split("@")[0]);
        user.setEnabled(true);
        user.setAccountLocked(false);
        return user;
    }
}
