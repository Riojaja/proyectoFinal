package pe.com.punamba.backend_punamba.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import pe.com.punamba.backend_punamba.dto.CategoriaRequestDTO;
import pe.com.punamba.backend_punamba.dto.CategoriaResponseDTO;
import pe.com.punamba.backend_punamba.entity.Categoria;

@Mapper(componentModel = "spring")
public interface CategoriaMapper {

    @Mapping(source = "categoriaPadre.idCategoria", target = "idCategoriaPadre")
    @Mapping(source = "categoriaPadre.nombre", target = "nombreCategoriaPadre")
    CategoriaResponseDTO toResponse(Categoria categoria);

    @Mapping(target = "idCategoria", ignore = true)
    @Mapping(target = "fechaCreacion", ignore = true)
    @Mapping(target = "subcategorias", ignore = true)
    @Mapping(target = "categoriaPadre", ignore = true)
    Categoria toEntity(CategoriaRequestDTO request);

    @Mapping(target = "idCategoria", ignore = true)
    @Mapping(target = "fechaCreacion", ignore = true)
    @Mapping(target = "subcategorias", ignore = true)
    @Mapping(target = "categoriaPadre", ignore = true)
    void updateEntityFromDto(CategoriaRequestDTO request, @MappingTarget Categoria entidadExistente);
}