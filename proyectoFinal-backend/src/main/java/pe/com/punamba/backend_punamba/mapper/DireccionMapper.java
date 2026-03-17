package pe.com.punamba.backend_punamba.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import pe.com.punamba.backend_punamba.dto.DireccionRequestDTO;
import pe.com.punamba.backend_punamba.dto.DireccionResponseDTO;
import pe.com.punamba.backend_punamba.entity.Direccion;

@Mapper(componentModel = "spring")
public interface DireccionMapper {

    @Mapping(source = "ubigeo.codigoUbigeo", target = "codigoUbigeo")
    @Mapping(source = "ubigeo.departamento", target = "departamento")
    @Mapping(source = "ubigeo.provincia", target = "provincia")
    @Mapping(source = "ubigeo.distrito", target = "distrito")
    DireccionResponseDTO toResponse(Direccion direccion);

    @Mapping(target = "idDireccion", ignore = true)
    @Mapping(target = "usuario", ignore = true)
    @Mapping(target = "ubigeo", ignore = true)
    @Mapping(target = "fechaCreacion", ignore = true)
    Direccion toEntity(DireccionRequestDTO request);
}