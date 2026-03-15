package pe.com.punamba.backend_punamba.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import pe.com.punamba.backend_punamba.dto.OrdenDetalleResponseDTO;
import pe.com.punamba.backend_punamba.dto.OrdenResponseDTO;
import pe.com.punamba.backend_punamba.entity.Orden;
import pe.com.punamba.backend_punamba.entity.OrdenDetalle;

@Mapper(componentModel = "spring")
public interface OrdenMapper {

    @Mapping(source = "estado.nombre", target = "estadoOrden")
    @Mapping(source = "estado.color", target = "colorEstado")
    @Mapping(source = "direccion.nombreDestinatario", target = "nombreDestinatario")
    @Mapping(target = "direccionEntrega", expression = "java(orden.getDireccion().getDireccionLinea1() + (orden.getDireccion().getDireccionLinea2() != null ? \" \" + orden.getDireccion().getDireccionLinea2() : \"\"))")
    OrdenResponseDTO toResponse(Orden orden);

    @Mapping(source = "variante.idVariante", target = "idVariante")
    @Mapping(source = "variante.producto.nombre", target = "nombreProducto")
    @Mapping(source = "vendedor.nombreTienda", target = "nombreVendedor")
    @Mapping(source = "metodoEnvio.nombre", target = "metodoEnvio")
    OrdenDetalleResponseDTO toDetalleResponse(OrdenDetalle detalle);
}