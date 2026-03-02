package pe.com.punamba.backend_punamba.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import pe.com.punamba.backend_punamba.dto.FavoritoResponseDTO;
import pe.com.punamba.backend_punamba.entity.Favorito;

@Mapper(componentModel = "spring")
public interface FavoritoMapper {

    @Mapping(source = "usuario.idUsuario", target = "idUsuario")
    @Mapping(source = "producto.idProducto", target = "idProducto")
    @Mapping(source = "producto.nombre", target = "nombreProducto")
    FavoritoResponseDTO toResponse(Favorito favorito);
}