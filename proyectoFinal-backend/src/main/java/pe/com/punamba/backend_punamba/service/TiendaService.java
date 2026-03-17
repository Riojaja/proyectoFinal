package pe.com.punamba.backend_punamba.service;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import pe.com.punamba.backend_punamba.entity.Tienda;
import pe.com.punamba.backend_punamba.entity.Vendedor;
import pe.com.punamba.backend_punamba.repository.TiendaRepository;
import pe.com.punamba.backend_punamba.repository.VendedorRepository;

@Service
public class TiendaService {

    @Autowired
    private TiendaRepository tiendaRepository;

    @Autowired
    private VendedorRepository vendedorRepository;

    @Transactional(readOnly = true)
    public List<Tienda> listarActivas() {
        return tiendaRepository.findByEstadoTrue();
    }

    @Transactional(readOnly = true)
    public Optional<Tienda> buscarPorVendedor(Integer idVendedor) {
        Integer idVendedorSeguro = Objects.requireNonNull(idVendedor, "El id del vendedor no puede ser null");
        return tiendaRepository.findByVendedorIdVendedor(idVendedorSeguro);
    }

    @Transactional(readOnly = true)
    public Optional<Tienda> buscarPorId(Integer idTienda) {
        Integer idTiendaSeguro = Objects.requireNonNull(idTienda, "El id de la tienda no puede ser null");
        return tiendaRepository.findById(idTiendaSeguro);
    }

    @Transactional
    public Tienda guardar(Integer idVendedor, Tienda datosTienda) {
        Integer idVendedorSeguro = Objects.requireNonNull(idVendedor, "El id del vendedor no puede ser null");
        Tienda datosTiendaSegura = Objects.requireNonNull(datosTienda, "Los datos de la tienda no pueden ser null");

        Vendedor vendedor = vendedorRepository.findById(idVendedorSeguro)
                .orElseThrow(() -> new RuntimeException("Vendedor no encontrado"));

        datosTiendaSegura.setVendedor(vendedor);
        return tiendaRepository.save(datosTiendaSegura);
    }

    @Transactional
    public void eliminar(Integer idTienda) {
        Integer idTiendaSeguro = Objects.requireNonNull(idTienda, "El id de la tienda no puede ser null");

        Optional<Tienda> tiendaOpt = tiendaRepository.findById(idTiendaSeguro);
        if (tiendaOpt.isPresent()) {
            Tienda tienda = Objects.requireNonNull(tiendaOpt.get(), "La tienda no puede ser null");
            tienda.setEstado(false);
            tiendaRepository.save(tienda);
        }
    }

    public boolean existeNombre(String nombre) {
        return tiendaRepository.existsByNombre(nombre);
    }
}