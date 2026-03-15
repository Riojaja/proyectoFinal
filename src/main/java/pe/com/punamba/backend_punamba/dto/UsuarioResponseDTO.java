package pe.com.punamba.backend_punamba.dto;

import pe.com.punamba.backend_punamba.entity.Usuario.EstadoUsuario;
import pe.com.punamba.backend_punamba.entity.Usuario.TipoDocumento;

import java.time.LocalDate;
import java.util.Set;

public class UsuarioResponseDTO {
    
    private Integer idUsuario;
    private String nombre;
    private String apellido;
    private String email;
    private TipoDocumento tipoDocumento;
    private String numeroDocumento;
    private String telefono;
    private LocalDate fechaNacimiento;
    private EstadoUsuario estado;
    private Set<String> roles; 

    public UsuarioResponseDTO() {
    }

    public Integer getIdUsuario() { return idUsuario; }
    public void setIdUsuario(Integer idUsuario) { this.idUsuario = idUsuario; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getApellido() { return apellido; }
    public void setApellido(String apellido) { this.apellido = apellido; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public TipoDocumento getTipoDocumento() { return tipoDocumento; }
    public void setTipoDocumento(TipoDocumento tipoDocumento) { this.tipoDocumento = tipoDocumento; }

    public String getNumeroDocumento() { return numeroDocumento; }
    public void setNumeroDocumento(String numeroDocumento) { this.numeroDocumento = numeroDocumento; }

    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }

    public LocalDate getFechaNacimiento() { return fechaNacimiento; }
    public void setFechaNacimiento(LocalDate fechaNacimiento) { this.fechaNacimiento = fechaNacimiento; }

    public EstadoUsuario getEstado() { return estado; }
    public void setEstado(EstadoUsuario estado) { this.estado = estado; }

    public Set<String> getRoles() { return roles; }
    public void setRoles(Set<String> roles) { this.roles = roles; }
}