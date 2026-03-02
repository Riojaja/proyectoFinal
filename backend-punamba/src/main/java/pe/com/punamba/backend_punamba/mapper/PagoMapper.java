package pe.com.punamba.backend_punamba.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import pe.com.punamba.backend_punamba.dto.MetodoPagoDTO;
import pe.com.punamba.backend_punamba.dto.TransaccionResponseDTO;
import pe.com.punamba.backend_punamba.entity.MetodoPago;
import pe.com.punamba.backend_punamba.entity.Transaccion;

@Mapper(componentModel = "spring")
public interface PagoMapper {
    
    MetodoPagoDTO toMetodoDTO(MetodoPago metodo);

    @Mapping(source = "orden.numeroOrden", target = "numeroOrden")
    @Mapping(source = "metodoPago.nombre", target = "metodoPagoNombre")
    TransaccionResponseDTO toTransaccionDTO(Transaccion transaccion);
}