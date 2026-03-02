package pe.com.punamba.backend_punamba.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import pe.com.punamba.backend_punamba.dto.LiquidacionResponseDTO;
import pe.com.punamba.backend_punamba.entity.Liquidacion;

@Mapper(componentModel = "spring")
public interface LiquidacionMapper {

    @Mapping(source = "vendedor.idVendedor", target = "idVendedor")
    @Mapping(source = "vendedor.nombreTienda", target = "nombreTienda")
    LiquidacionResponseDTO toResponse(Liquidacion liquidacion);
}