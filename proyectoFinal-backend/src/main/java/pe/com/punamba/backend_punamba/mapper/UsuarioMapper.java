package pe.com.punamba.backend_punamba.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import pe.com.punamba.backend_punamba.dto.RegisterRequest;
import pe.com.punamba.backend_punamba.dto.UsuarioResponseDTO; // Importar el nuevo DTO
import pe.com.punamba.backend_punamba.entity.Usuario;

@Mapper(componentModel = "spring")
public interface UsuarioMapper {

    @Mapping(target = "idUsuario", ignore = true)
    @Mapping(target = "passwordHash", ignore = true)
    @Mapping(target = "roles", ignore = true)
    @Mapping(target = "estado", ignore = true)
    @Mapping(target = "emailVerificado", ignore = true)
    @Mapping(target = "codigoVerificacion", ignore = true)
    @Mapping(target = "fechaVerificacion", ignore = true)
    @Mapping(target = "fechaNacimiento", ignore = true)
    @Mapping(target = "fechaRegistro", ignore = true)
    @Mapping(target = "ultimaConexion", ignore = true)
    @Mapping(target = "authorities", ignore = true)
    @Mapping(target = "enabled", ignore = true)
    @Mapping(target = "accountNonExpired", ignore = true)
    @Mapping(target = "accountNonLocked", ignore = true)
    @Mapping(target = "credentialsNonExpired", ignore = true)
    @Mapping(target = "username", ignore = true)
    @Mapping(target = "password", ignore = true)
    Usuario toEntity(RegisterRequest request);

    @Mapping(target = "idUsuario", ignore = true)
    @Mapping(target = "passwordHash", ignore = true)
    @Mapping(target = "email", ignore = true)
    @Mapping(target = "roles", ignore = true)
    @Mapping(target = "fechaRegistro", ignore = true)
    @Mapping(target = "numeroDocumento", ignore = true)
    @Mapping(target = "authorities", ignore = true)
    @Mapping(target = "enabled", ignore = true)
    @Mapping(target = "accountNonExpired", ignore = true)
    @Mapping(target = "accountNonLocked", ignore = true)
    @Mapping(target = "credentialsNonExpired", ignore = true)
    @Mapping(target = "username", ignore = true)
    @Mapping(target = "password", ignore = true)
    void updateEntityFromDto(Usuario detalles, @MappingTarget Usuario entidadExistente);

    @Mapping(target = "roles", expression = "java(usuario.getRoles().stream().map(r -> r.getNombre()).collect(java.util.stream.Collectors.toSet()))")
    UsuarioResponseDTO toResponse(Usuario usuario);
}