package pe.com.punamba.backend_punamba.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import pe.com.punamba.backend_punamba.dto.CarritoItemResponseDTO;
import pe.com.punamba.backend_punamba.dto.CarritoResponseDTO;
import pe.com.punamba.backend_punamba.entity.Carrito;
import pe.com.punamba.backend_punamba.entity.CarritoItem;

import java.math.BigDecimal;
import java.util.List;

@Mapper(componentModel = "spring")
public interface CarritoMapper {

    @Mapping(source = "usuario.idUsuario", target = "idUsuario")
    @Mapping(target = "totalCarrito", source = "items", qualifiedByName = "calcularTotal")
    CarritoResponseDTO toResponse(Carrito carrito);

    @Mapping(source = "variante.idVariante", target = "idVariante")
    @Mapping(source = "variante.producto.nombre", target = "nombreProducto")
    @Mapping(source = "precioSnapshot", target = "precioUnitario")
    @Mapping(target = "subtotal", source = "item", qualifiedByName = "calcularSubtotal")
    CarritoItemResponseDTO toItemResponse(CarritoItem item);

    @Named("calcularSubtotal")
    default BigDecimal calcularSubtotal(CarritoItem item) {
        if (item.getPrecioSnapshot() == null || item.getCantidad() == null) {
            return BigDecimal.ZERO;
        }
        return item.getPrecioSnapshot().multiply(new BigDecimal(item.getCantidad()));
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
}