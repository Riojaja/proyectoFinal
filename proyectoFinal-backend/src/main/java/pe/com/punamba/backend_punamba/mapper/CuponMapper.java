package pe.com.punamba.backend_punamba.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import pe.com.punamba.backend_punamba.dto.CuponRequestDTO;
import pe.com.punamba.backend_punamba.dto.CuponResponseDTO;
import pe.com.punamba.backend_punamba.entity.Cupon;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Mapper(componentModel = "spring")
public interface CuponMapper {

    @Mapping(source = "vendedor.idVendedor", target = "idVendedor")
    @Mapping(target = "fechaInicio", expression = "java(toLocalDate(cupon.getFechaInicio()))")
    @Mapping(target = "fechaExpiracion", expression = "java(toLocalDate(cupon.getFechaExpiracion()))")
    CuponResponseDTO toResponse(Cupon cupon);

    @Mapping(target = "idCupon", ignore = true)
    @Mapping(target = "vendedor", ignore = true)
    @Mapping(target = "cantidadUsos", ignore = true)
    @Mapping(target = "fechaCreacion", ignore = true)
    @Mapping(target = "fechaInicio", expression = "java(toStartOfDay(dto.getFechaInicio()))")
    @Mapping(target = "fechaExpiracion", expression = "java(toEndOfDay(dto.getFechaExpiracion()))")
    Cupon toEntity(CuponRequestDTO dto);

    @Mapping(target = "idCupon", ignore = true)
    @Mapping(target = "vendedor", ignore = true)
    @Mapping(target = "cantidadUsos", ignore = true)
    @Mapping(target = "fechaCreacion", ignore = true)
    @Mapping(target = "fechaInicio", expression = "java(toStartOfDay(dto.getFechaInicio()))")
    @Mapping(target = "fechaExpiracion", expression = "java(toEndOfDay(dto.getFechaExpiracion()))")
    void updateEntityFromDto(CuponRequestDTO dto, @MappingTarget Cupon cupon);

    default LocalDate toLocalDate(LocalDateTime value) {
        return value != null ? value.toLocalDate() : null;
    }

    default LocalDateTime toStartOfDay(LocalDate value) {
        return value != null ? value.atStartOfDay() : null;
    }

    default LocalDateTime toEndOfDay(LocalDate value) {
        return value != null ? value.atTime(23, 59, 59) : null;
    }
}