package pe.com.punamba.backend_punamba.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.com.punamba.backend_punamba.entity.Tienda;
import pe.com.punamba.backend_punamba.entity.Vendedor;
import pe.com.punamba.backend_punamba.repository.TiendaRepository;
import pe.com.punamba.backend_punamba.repository.VendedorRepository;

import java.util.List;
import java.util.Optional;

@Service
public class TiendaService {

    @Autowired private TiendaRepository tiendaRepository;
    @Autowired private VendedorRepository vendedorRepository;

    @Transactional(readOnly = true)
    public List<Tienda> listarActivas() {
        return tiendaRepository.findByEstadoTrue();
    }

    @Transactional(readOnly = true)
    public List<Tienda> buscarPorVendedor(Integer idVendedor) {
        return tiendaRepository.findByVendedorIdVendedor(idVendedor);
    }

    @Transactional(readOnly = true)
    public Optional<Tienda> buscarPorId(Integer idTienda) {
        return tiendaRepository.findById(idTienda);
    }

    @Transactional
    public Tienda guardar(Integer idVendedor, Tienda datosTienda) {
        Vendedor vendedor = vendedorRepository.findById(idVendedor)
                .orElseThrow(() -> new RuntimeException("Vendedor no encontrado"));
        
        datosTienda.setVendedor(vendedor);
        return tiendaRepository.save(datosTienda);
    }

    @Transactional
    public void eliminar(Integer idTienda) {
        tiendaRepository.findById(idTienda).ifPresent(tienda -> {
            tienda.setEstado(false);
            tiendaRepository.save(tienda);
        });
    }

    public boolean existeNombre(String nombre) {
        return tiendaRepository.existsByNombre(nombre);
    }
}