package pe.com.punamba.backend_punamba.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import pe.com.punamba.backend_punamba.dto.CuponRequestDTO;
import pe.com.punamba.backend_punamba.dto.CuponResponseDTO;
import pe.com.punamba.backend_punamba.entity.Cupon;

@Mapper(componentModel = "spring")
public interface CuponMapper {

    @Mapping(source = "vendedor.idVendedor", target = "idVendedor")
    CuponResponseDTO toResponse(Cupon cupon);

    @Mapping(target = "idCupon", ignore = true)
    @Mapping(target = "vendedor", ignore = true)
    @Mapping(target = "cantidadUsos", ignore = true)
    @Mapping(target = "fechaCreacion", ignore = true)
    Cupon toEntity(CuponRequestDTO dto);

    @Mapping(target = "idCupon", ignore = true)
    @Mapping(target = "vendedor", ignore = true)
    @Mapping(target = "cantidadUsos", ignore = true)
    @Mapping(target = "fechaCreacion", ignore = true)
    void updateEntityFromDto(CuponRequestDTO dto, @MappingTarget Cupon cupon);
}