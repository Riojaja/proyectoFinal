package pe.com.punamba.backend_punamba.mapper;

import org.mapstruct.Mapper;
import pe.com.punamba.backend_punamba.dto.MetodoEnvioDTO;
import pe.com.punamba.backend_punamba.entity.MetodoEnvio;

@Mapper(componentModel = "spring")
public interface MetodoEnvioMapper {
    MetodoEnvioDTO toDto(MetodoEnvio entity);
    MetodoEnvio toEntity(MetodoEnvioDTO dto);
}