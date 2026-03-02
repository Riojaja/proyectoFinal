package pe.com.punamba.backend_punamba.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import pe.com.punamba.backend_punamba.dto.*;
import pe.com.punamba.backend_punamba.entity.Producto;
import pe.com.punamba.backend_punamba.entity.ProductoVariante;
import pe.com.punamba.backend_punamba.entity.VarianteAtributo;

@Mapper(componentModel = "spring")
public interface ProductoMapper {

    @Mapping(source = "categoria.idCategoria", target = "idCategoria")
    @Mapping(source = "categoria.nombre", target = "nombreCategoria")
    @Mapping(source = "vendedor.idVendedor", target = "idVendedor")
    @Mapping(source = "vendedor.nombreTienda", target = "nombreTienda")
    ProductoResponseDTO toResponse(Producto producto);

    @Mapping(target = "idProducto", ignore = true)
    @Mapping(target = "estado", ignore = true)
    @Mapping(target = "fechaCreacion", ignore = true)
    @Mapping(target = "fechaActualizacion", ignore = true)
    @Mapping(target = "vendedor", ignore = true) 
    @Mapping(target = "categoria", ignore = true) 
    @Mapping(target = "imagenes", ignore = true)
    Producto toEntity(ProductoRequestDTO request);

    @Mapping(target = "idProducto", ignore = true)
    @Mapping(target = "estado", ignore = true)
    @Mapping(target = "fechaCreacion", ignore = true)
    @Mapping(target = "fechaActualizacion", ignore = true)
    @Mapping(target = "vendedor", ignore = true)
    @Mapping(target = "categoria", ignore = true)
    @Mapping(target = "imagenes", ignore = true)
    void updateEntityFromDto(ProductoRequestDTO request, @MappingTarget Producto productoExistente);

    @Mapping(target = "idVariante", ignore = true)
    @Mapping(target = "producto", ignore = true)
    @Mapping(target = "stockReservado", ignore = true)
    @Mapping(target = "activo", ignore = true)
    @Mapping(target = "fechaCreacion", ignore = true)
    @Mapping(target = "fechaActualizacion", ignore = true)
    ProductoVariante toVarianteEntity(VarianteRequestDTO request);

    VarianteResponseDTO toVarianteResponse(ProductoVariante variante);

    @Mapping(source = "idAtributo", target = "atributo.idAtributo")
    @Mapping(source = "idValor", target = "valor.idValor")
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "variante", ignore = true)
    VarianteAtributo toAtributoEntity(VarianteAtributoRequestDTO request);

    @Mapping(source = "atributo.idAtributo", target = "idAtributo")
    @Mapping(source = "atributo.nombre", target = "nombreAtributo")
    @Mapping(source = "valor.idValor", target = "idValor")
    @Mapping(source = "valor.valor", target = "valor")
    VarianteAtributoResponseDTO toAtributoResponse(VarianteAtributo atributo);
}