package pe.com.punamba.backend_punamba.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import pe.com.punamba.backend_punamba.dto.TiendaRequestDTO;
import pe.com.punamba.backend_punamba.dto.TiendaResponseDTO;
import pe.com.punamba.backend_punamba.entity.Tienda;

@Mapper(componentModel = "spring")
public interface TiendaMapper {

    @Mapping(source = "vendedor.idVendedor", target = "idVendedor")
    TiendaResponseDTO toResponse(Tienda tienda);

    @Mapping(target = "idTienda", ignore = true)
    @Mapping(target = "vendedor", ignore = true)
    @Mapping(target = "fechaCreacion", ignore = true)
    Tienda toEntity(TiendaRequestDTO request);

    @Mapping(target = "idTienda", ignore = true)
    @Mapping(target = "vendedor", ignore = true)
    @Mapping(target = "fechaCreacion", ignore = true)
    void updateEntityFromDto(TiendaRequestDTO request, @MappingTarget Tienda tienda);
}