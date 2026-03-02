package pe.com.punamba.backend_punamba.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import pe.com.punamba.backend_punamba.dto.DisputaResponseDTO;
import pe.com.punamba.backend_punamba.entity.Disputa;

@Mapper(componentModel = "spring")
public interface DisputaMapper {

    @Mapping(source = "ordenDetalle.idOrdenDetalle", target = "idOrdenDetalle")
    @Mapping(source = "ordenDetalle.variante.producto.nombre", target = "nombreProducto")
    DisputaResponseDTO toResponse(Disputa disputa);
}