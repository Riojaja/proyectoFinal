package pe.com.punamba.backend_punamba.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import pe.com.punamba.backend_punamba.dto.OrdenDetalleResponseDTO;
import pe.com.punamba.backend_punamba.dto.OrdenResponseDTO;
import pe.com.punamba.backend_punamba.entity.Orden;
import pe.com.punamba.backend_punamba.entity.OrdenDetalle;
import pe.com.punamba.backend_punamba.entity.Producto;
import pe.com.punamba.backend_punamba.entity.ProductoImagen;

import java.util.Comparator;
import java.util.List;

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
    @Mapping(target = "imagen", expression = "java(obtenerImagen(detalle))")
    @Mapping(source = "estadoPostventa", target = "estadoPostventa")
    @Mapping(source = "montoReembolsado", target = "montoReembolsado")
    OrdenDetalleResponseDTO toDetalleResponse(OrdenDetalle detalle);

    default String obtenerImagen(OrdenDetalle detalle) {
        if (detalle == null ||
                detalle.getVariante() == null ||
                detalle.getVariante().getProducto() == null) {
            return null;
        }

        Producto producto = detalle.getVariante().getProducto();
        List<ProductoImagen> imagenes = producto.getImagenes();

        if (imagenes == null || imagenes.isEmpty()) {
            return null;
        }

        ProductoImagen principal = imagenes.stream()
                .filter(img -> img != null && img.getUrlImagen() != null && !img.getUrlImagen().isBlank())
                .filter(img -> Boolean.TRUE.equals(img.getEsPrincipal()))
                .findFirst()
                .orElse(null);

        if (principal != null) {
            return principal.getUrlImagen();
        }

        ProductoImagen primera = imagenes.stream()
                .filter(img -> img != null && img.getUrlImagen() != null && !img.getUrlImagen().isBlank())
                .min(Comparator.comparing(img -> img.getOrden() != null ? img.getOrden() : 999999))
                .orElse(null);

        return primera != null ? primera.getUrlImagen() : null;
    }
}