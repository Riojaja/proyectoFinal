package pe.com.punamba.backend_punamba.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import pe.com.punamba.backend_punamba.dto.ValoracionResponseDTO;
import pe.com.punamba.backend_punamba.entity.Valoracion;

@Mapper(componentModel = "spring")
public interface ValoracionMapper {

    @Mapping(source = "usuario.nombre", target = "nombreUsuario")
    ValoracionResponseDTO toResponse(Valoracion valoracion);
}