package pe.com.punamba.backend_punamba.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder; // Importación necesaria
import org.springframework.stereotype.Component;
import pe.com.punamba.backend_punamba.entity.Rol;
import pe.com.punamba.backend_punamba.entity.Usuario;
import pe.com.punamba.backend_punamba.repository.RolRepository;
import pe.com.punamba.backend_punamba.repository.UsuarioRepository;
import java.util.HashSet;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired private RolRepository rolRepository;
    @Autowired private UsuarioRepository usuarioRepository;
    @Autowired private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        if (!usuarioRepository.existsByEmail("admin@punamba.com")) {
            Usuario admin = new Usuario();
            admin.setNombre("Admin");
            admin.setApellido("Punamba");
            admin.setEmail("admin@punamba.com");
            admin.setNumeroDocumento("00000000");
            admin.setTipoDocumento(Usuario.TipoDocumento.DNI);
            
            admin.setPasswordHash(passwordEncoder.encode("admin123")); 
            
            admin.setEstado(Usuario.EstadoUsuario.activo);
            admin.setEmailVerificado(true);
            
            Rol rolAdmin = rolRepository.findByNombre("ADMIN")
                    .orElseThrow(() -> new RuntimeException("Error: El rol ADMIN no existe en la DB. Revisa tu SQL."));
            
            admin.setRoles(new HashSet<>());
            admin.getRoles().add(rolAdmin);
            
            usuarioRepository.save(admin);
            System.out.println("--> [OK] Usuario Admin creado con contraseña encriptada.");
        }
    }
}