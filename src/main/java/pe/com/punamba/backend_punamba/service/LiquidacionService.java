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
import java.util.Objects;
import java.util.Optional;

@Service
public class LiquidacionService {

    @Autowired
    private LiquidacionRepository liquidacionRepository;

    @Autowired
    private VendedorRepository vendedorRepository;

    @Transactional(readOnly = true)
    public List<Liquidacion> listarTodas() {
        return liquidacionRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Optional<Liquidacion> buscarPorId(Integer id) {
        Integer idSeguro = Objects.requireNonNull(id, "El id de la liquidación no puede ser null");
        return liquidacionRepository.findById(idSeguro);
    }

    @Transactional(readOnly = true)
    public List<Liquidacion> listarPorVendedor(Integer idVendedor) {
        Integer idVendedorSeguro = Objects.requireNonNull(idVendedor, "El id del vendedor no puede ser null");
        return liquidacionRepository.findByVendedorIdVendedorOrderByFechaCorteDesc(idVendedorSeguro);
    }

    @Transactional(readOnly = true)
    public List<Liquidacion> listarPorEstado(Liquidacion.EstadoLiquidacion estado) {
        Liquidacion.EstadoLiquidacion estadoSeguro = Objects.requireNonNull(estado, "El estado no puede ser null");
        return liquidacionRepository.findByEstado(estadoSeguro);
    }

    @Transactional
    public Liquidacion generarLiquidacion(Integer idVendedor, Liquidacion datosLiquidacion) {
        Integer idVendedorSeguro = Objects.requireNonNull(idVendedor, "El id del vendedor no puede ser null");
        Liquidacion datosLiquidacionSegura = Objects.requireNonNull(datosLiquidacion, "Los datos de liquidación no pueden ser null");

        Vendedor vendedor = vendedorRepository.findById(idVendedorSeguro)
                .orElseThrow(() -> new RuntimeException("Vendedor no encontrado"));

        datosLiquidacionSegura.setVendedor(vendedor);
        datosLiquidacionSegura.setEstado(Liquidacion.EstadoLiquidacion.pendiente);

        return liquidacionRepository.save(datosLiquidacionSegura);
    }

    @Transactional
    public Liquidacion registrarPago(Integer idLiquidacion, String urlComprobante) {
        Integer idLiquidacionSeguro = Objects.requireNonNull(idLiquidacion, "El id de la liquidación no puede ser null");
        String urlComprobanteSeguro = Objects.requireNonNull(urlComprobante, "La URL del comprobante no puede ser null");

        Liquidacion liquidacion = liquidacionRepository.findById(idLiquidacionSeguro)
                .orElseThrow(() -> new RuntimeException("Liquidación no encontrada"));

        if (liquidacion.getEstado() == Liquidacion.EstadoLiquidacion.pagado) {
            throw new RuntimeException("Esta liquidación ya ha sido pagada.");
        }

        liquidacion.setEstado(Liquidacion.EstadoLiquidacion.pagado);
        liquidacion.setFechaPago(LocalDateTime.now());
        liquidacion.setComprobanteTransferencia(urlComprobanteSeguro);

        return liquidacionRepository.save(liquidacion);
    }
}