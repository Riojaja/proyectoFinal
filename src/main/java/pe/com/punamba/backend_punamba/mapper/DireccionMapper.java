package pe.com.punamba.backend_punamba.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import pe.com.punamba.backend_punamba.dto.DireccionRequestDTO;
import pe.com.punamba.backend_punamba.dto.DireccionResponseDTO;
import pe.com.punamba.backend_punamba.entity.Direccion;

@Mapper(componentModel = "spring")
public interface DireccionMapper {

    @Mapping(source = "ubigeo", target = "ubigeo")
    DireccionResponseDTO toResponse(Direccion direccion);

    @Mapping(source = "codigoUbigeo", target = "codigoUbigeo")
    @Mapping(source = "departamento", target = "departamento")
    @Mapping(source = "provincia", target = "provincia")
    @Mapping(source = "distrito", target = "distrito")
    DireccionResponseDTO.UbigeoDTO toUbigeoDto(pe.com.punamba.backend_punamba.entity.Ubigeo ubigeo);

    @Mapping(target = "idDireccion", ignore = true)
    @Mapping(target = "usuario", ignore = true)
    @Mapping(target = "ubigeo", ignore = true)
    @Mapping(target = "fechaCreacion", ignore = true)
    Direccion toEntity(DireccionRequestDTO request);
}