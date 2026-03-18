package pe.com.punamba.backend_punamba.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import pe.com.punamba.backend_punamba.dto.DisputaResponseDTO;
import pe.com.punamba.backend_punamba.entity.Disputa;

@Mapper(componentModel = "spring")
public interface DisputaMapper {

    @Mapping(source = "ordenDetalle.orden.idOrden", target = "idOrden")
    @Mapping(source = "ordenDetalle.orden.numeroOrden", target = "numeroOrden")
    @Mapping(source = "ordenDetalle.idOrdenDetalle", target = "idOrdenDetalle")
    @Mapping(source = "ordenDetalle.variante.sku", target = "sku")
    @Mapping(source = "usuario.email", target = "usuarioEmail")
    @Mapping(expression = "java(disputa.getUsuario().getNombre() + \" \" + disputa.getUsuario().getApellido())", target = "clienteNombre")
    @Mapping(source = "ordenDetalle.vendedor.nombreTienda", target = "vendedorNombre")
    @Mapping(source = "ordenDetalle.vendedor.ruc", target = "vendedorRuc")
    @Mapping(source = "ordenDetalle.variante.producto.nombre", target = "nombreProducto")
    DisputaResponseDTO toResponse(Disputa disputa);
}