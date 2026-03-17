package pe.com.punamba.backend_punamba.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import pe.com.punamba.backend_punamba.dto.FavoritoResponseDTO;
import pe.com.punamba.backend_punamba.entity.Favorito;
import pe.com.punamba.backend_punamba.entity.ProductoImagen;

@Mapper(componentModel = "spring")
public interface FavoritoMapper {

    @Mapping(source = "usuario.idUsuario", target = "idUsuario")
    @Mapping(source = "producto.idProducto", target = "idProducto")
    @Mapping(source = "producto.nombre", target = "nombreProducto")
    @Mapping(source = "producto.precioBase", target = "precioProducto")
    @Mapping(source = "producto", target = "imagenProducto", qualifiedByName = "obtenerImagenPrincipal")
    @Mapping(source = "fechaAgregado", target = "fechaAgregado")
    FavoritoResponseDTO toResponse(Favorito favorito);

    @Named("obtenerImagenPrincipal")
    default String obtenerImagenPrincipal(pe.com.punamba.backend_punamba.entity.Producto producto) {
        if (producto == null || producto.getImagenes() == null || producto.getImagenes().isEmpty()) {
            return null;
        }
        
        String imagen = producto.getImagenes().stream()
            .filter(img -> Boolean.TRUE.equals(img.getEsPrincipal()))
            .findFirst()
            .map(ProductoImagen::getUrlImagen)
            .orElse(producto.getImagenes().get(0).getUrlImagen());
        
        // 👇 CONSTRUIR URL COMPLETA
        if (imagen != null && !imagen.startsWith("http")) {
            // Si la imagen ya tiene /uploads/, mantenerlo
            if (imagen.startsWith("/uploads/")) {
                return imagen;
            }
            return "/uploads/" + imagen;
        }
        return imagen;
    }
}