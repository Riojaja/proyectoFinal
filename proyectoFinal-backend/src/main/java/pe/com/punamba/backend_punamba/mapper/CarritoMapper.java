package pe.com.punamba.backend_punamba.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import pe.com.punamba.backend_punamba.dto.CarritoItemResponseDTO;
import pe.com.punamba.backend_punamba.dto.CarritoResponseDTO;
import pe.com.punamba.backend_punamba.entity.Carrito;
import pe.com.punamba.backend_punamba.entity.CarritoItem;
import pe.com.punamba.backend_punamba.entity.Producto;
import pe.com.punamba.backend_punamba.entity.ProductoImagen;
import pe.com.punamba.backend_punamba.entity.VarianteAtributo;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface CarritoMapper {

    @Mapping(source = "usuario.idUsuario", target = "idUsuario")
    @Mapping(target = "totalCarrito", source = "items", qualifiedByName = "calcularTotal")
    CarritoResponseDTO toResponse(Carrito carrito);

    @Mapping(source = "idItem", target = "idItem")
    @Mapping(source = "variante.idVariante", target = "idVariante")
    @Mapping(source = "variante.sku", target = "sku")
    @Mapping(source = "variante.producto.idProducto", target = "idProducto")
    @Mapping(source = "variante.producto.nombre", target = "nombreProducto")
    @Mapping(source = "variante.producto", target = "imagen", qualifiedByName = "obtenerImagenPrincipal")
    @Mapping(source = "cantidad", target = "cantidad")
    @Mapping(source = "precioSnapshot", target = "precio")
    @Mapping(source = "variante.precioOferta", target = "precioOferta")
    @Mapping(source = "variante.stock", target = "stock")
    @Mapping(target = "subtotal", source = "item", qualifiedByName = "calcularSubtotal")
    @Mapping(source = "variante.atributos", target = "atributos", qualifiedByName = "mapearAtributos")
    CarritoItemResponseDTO toItemResponse(CarritoItem item);

    @Named("calcularSubtotal")
    default BigDecimal calcularSubtotal(CarritoItem item) {
        if (item == null || item.getPrecioSnapshot() == null || item.getCantidad() == null) {
            return BigDecimal.ZERO;
        }
        return item.getPrecioSnapshot().multiply(BigDecimal.valueOf(item.getCantidad()));
    }

    @Named("calcularTotal")
    default BigDecimal calcularTotal(List<CarritoItem> items) {
        if (items == null || items.isEmpty()) {
            return BigDecimal.ZERO;
        }
        return items.stream()
                .map(this::calcularSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @Named("obtenerImagenPrincipal")
    default String obtenerImagenPrincipal(Producto producto) {
        if (producto == null || producto.getImagenes() == null || producto.getImagenes().isEmpty()) {
            return null;
        }

        return producto.getImagenes().stream()
                .sorted(
                        Comparator
                                .comparing((ProductoImagen img) -> !(Boolean.TRUE.equals(img.getEsPrincipal())))
                                .thenComparing(img -> img.getOrden() != null ? img.getOrden() : Integer.MAX_VALUE)
                )
                .map(ProductoImagen::getUrlImagen)
                .findFirst()
                .orElse(null);
    }

    @Named("mapearAtributos")
    default List<String> mapearAtributos(List<VarianteAtributo> atributos) {
        if (atributos == null || atributos.isEmpty()) {
            return List.of();
        }

        return atributos.stream()
                .map(a -> {
                    String nombreAtributo = a.getAtributo() != null ? a.getAtributo().getNombre() : "";
                    String valor = (a.getValor() != null && a.getValor().getValor() != null)
                            ? a.getValor().getValor()
                            : "";

                    if (!nombreAtributo.isBlank() && !valor.isBlank()) {
                        return nombreAtributo + ": " + valor;
                    }

                    if (!valor.isBlank()) {
                        return valor;
                    }

                    return "";
                })
                .filter(s -> s != null && !s.isBlank())
                .collect(Collectors.toList());
    }
}