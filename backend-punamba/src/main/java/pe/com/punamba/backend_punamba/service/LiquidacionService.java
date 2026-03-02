package pe.com.punamba.backend_punamba.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.com.punamba.backend_punamba.entity.Liquidacion;
import pe.com.punamba.backend_punamba.entity.Vendedor;
import pe.com.punamba.backend_punamba.repository.LiquidacionRepository;
import pe.com.punamba.backend_punamba.repository.VendedorRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class LiquidacionService {

    @Autowired private LiquidacionRepository liquidacionRepository;
    @Autowired private VendedorRepository vendedorRepository;

    @Transactional(readOnly = true)
    public List<Liquidacion> listarTodas() {
        return liquidacionRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Optional<Liquidacion> buscarPorId(Integer id) {
        return liquidacionRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public List<Liquidacion> listarPorVendedor(Integer idVendedor) {
        return liquidacionRepository.findByVendedorIdVendedorOrderByFechaCorteDesc(idVendedor);
    }

    @Transactional(readOnly = true)
    public List<Liquidacion> listarPorEstado(Liquidacion.EstadoLiquidacion estado) {
        return liquidacionRepository.findByEstado(estado);
    }

    @Transactional
    public Liquidacion generarLiquidacion(Integer idVendedor, Liquidacion datosLiquidacion) {
        Vendedor vendedor = vendedorRepository.findById(idVendedor)
                .orElseThrow(() -> new RuntimeException("Vendedor no encontrado"));
        
        datosLiquidacion.setVendedor(vendedor);
        datosLiquidacion.setEstado(Liquidacion.EstadoLiquidacion.pendiente);
        
        return liquidacionRepository.save(datosLiquidacion);
    }

    @Transactional
    public Liquidacion registrarPago(Integer idLiquidacion, String urlComprobante) {
        Liquidacion liquidacion = liquidacionRepository.findById(idLiquidacion)
                .orElseThrow(() -> new RuntimeException("Liquidación no encontrada"));
        
        if (liquidacion.getEstado() == Liquidacion.EstadoLiquidacion.pagado) {
            throw new RuntimeException("Esta liquidación ya ha sido pagada.");
        }

        liquidacion.setEstado(Liquidacion.EstadoLiquidacion.pagado);
        liquidacion.setFechaPago(LocalDateTime.now());
        liquidacion.setComprobanteTransferencia(urlComprobante);
        
        return liquidacionRepository.save(liquidacion);
    }
}