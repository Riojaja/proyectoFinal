package pe.com.punamba.backend_punamba.auth;

import java.util.HashSet;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import pe.com.punamba.backend_punamba.dto.AuthResponse;
import pe.com.punamba.backend_punamba.dto.LoginRequest;
import pe.com.punamba.backend_punamba.dto.RegisterRequest;
import pe.com.punamba.backend_punamba.entity.Rol;
import pe.com.punamba.backend_punamba.entity.Usuario;
import pe.com.punamba.backend_punamba.entity.Vendedor;
import pe.com.punamba.backend_punamba.mapper.UsuarioMapper;
import pe.com.punamba.backend_punamba.repository.RolRepository;
import pe.com.punamba.backend_punamba.repository.UsuarioRepository;
import pe.com.punamba.backend_punamba.repository.VendedorRepository;
import pe.com.punamba.backend_punamba.security.JwtService;

@Service
public class AuthService {

    private final UsuarioRepository repository;
    private final RolRepository rolRepository;
    private final VendedorRepository vendedorRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final UsuarioMapper usuarioMapper;

    public AuthService(
            UsuarioRepository repository,
            RolRepository rolRepository,
            VendedorRepository vendedorRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService,
            AuthenticationManager authenticationManager,
            UsuarioMapper usuarioMapper
    ) {
        this.repository = repository;
        this.rolRepository = rolRepository;
        this.vendedorRepository = vendedorRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
        this.usuarioMapper = usuarioMapper;
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        Usuario usuario = usuarioMapper.toEntity(request);

        usuario.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        usuario.setEstado(Usuario.EstadoUsuario.activo);
        usuario.setEmailVerificado(false);

        if (usuario.getRoles() == null) {
            usuario.setRoles(new HashSet<>());
        }

        String rolSolicitado = (request.getRol() != null && !request.getRol().trim().isEmpty())
                ? request.getRol().trim().toUpperCase()
                : "CLIENTE";

        if ("CLIENTE".equals(rolSolicitado)) {
            Rol rolCliente = rolRepository.findByNombre("CLIENTE")
                    .orElseThrow(() -> new RuntimeException("Error: El rol CLIENTE no existe."));
            usuario.getRoles().add(rolCliente);
        }

        if ("VENDEDOR".equals(rolSolicitado)) {
            if (request.getNombreTienda() == null || request.getNombreTienda().trim().isEmpty()) {
                throw new RuntimeException("El nombre de la tienda es obligatorio.");
            }

            if (request.getRuc() == null || request.getRuc().trim().isEmpty()) {
                throw new RuntimeException("El RUC es obligatorio.");
            }

            if (vendedorRepository.existsByRuc(request.getRuc().trim())) {
                throw new RuntimeException("El RUC ya se encuentra registrado.");
            }

            if (vendedorRepository.existsByNombreTiendaIgnoreCase(request.getNombreTienda().trim())) {
                throw new RuntimeException("El nombre de la tienda ya está en uso.");
            }
        }

        Usuario usuarioGuardado = repository.saveAndFlush(usuario);

        if ("VENDEDOR".equals(rolSolicitado)) {
            Vendedor vendedor = new Vendedor();
            vendedor.setUsuario(usuarioGuardado);
            vendedor.setNombreTienda(request.getNombreTienda().trim());
            vendedor.setRuc(request.getRuc().trim());
            vendedor.setEstado(Vendedor.EstadoVendedor.pendiente);
            vendedorRepository.save(vendedor);
        }

        String jwtToken = jwtService.generateToken(usuarioGuardado);
        String refreshToken = jwtService.generateRefreshToken(usuarioGuardado);

        return new AuthResponse(
                jwtToken,
                refreshToken,
                usuarioGuardado.getEmail(),
                usuarioGuardado.getNombre() + " " + usuarioGuardado.getApellido()
        );
    }

    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        Usuario user = repository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        String jwtToken = jwtService.generateToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);

        return new AuthResponse(
                jwtToken,
                refreshToken,
                user.getEmail(),
                user.getNombre() + " " + user.getApellido()
        );
    }

    public AuthResponse refreshToken(String refreshToken) {
        String userEmail = jwtService.extractUsername(refreshToken);
        if (userEmail != null) {
            Usuario user = repository.findByEmail(userEmail)
                    .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

            if (jwtService.isTokenValid(refreshToken, user)) {
                String newAccessToken = jwtService.generateToken(user);

                return new AuthResponse(
                        newAccessToken,
                        refreshToken,
                        user.getEmail(),
                        user.getNombre() + " " + user.getApellido()
                );
            }
        }
        throw new RuntimeException("Refresh token inválido o expirado");
    }
}