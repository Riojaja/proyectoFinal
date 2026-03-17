package pe.com.punamba.backend_punamba.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import pe.com.punamba.backend_punamba.dto.VendedorRequestDTO;
import pe.com.punamba.backend_punamba.dto.VendedorResponseDTO;
import pe.com.punamba.backend_punamba.entity.Vendedor;

@Mapper(componentModel = "spring")
public interface VendedorMapper {

    @Mapping(source = "usuario.idUsuario", target = "idUsuario")
    @Mapping(source = "usuario.nombre", target = "nombreUsuario")
    VendedorResponseDTO toResponse(Vendedor vendedor);

    @Mapping(target = "idVendedor", ignore = true)
    @Mapping(target = "usuario", ignore = true)
    @Mapping(target = "calificacion", ignore = true)
    @Mapping(target = "totalVentas", ignore = true)
    @Mapping(target = "estado", ignore = true)
    @Mapping(target = "fechaRegistro", ignore = true)
    Vendedor toEntity(VendedorRequestDTO request);

    @Mapping(target = "idVendedor", ignore = true)
    @Mapping(target = "usuario", ignore = true)
    @Mapping(target = "calificacion", ignore = true)
    @Mapping(target = "totalVentas", ignore = true)
    @Mapping(target = "estado", ignore = true)
    @Mapping(target = "fechaRegistro", ignore = true)
    @Mapping(target = "ruc", ignore = true)
    void updateEntityFromDto(VendedorRequestDTO request, @MappingTarget Vendedor vendedorExistente);
}