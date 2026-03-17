package pe.com.punamba.backend_punamba.dto;

public class AuthResponse {
    private String token;
    private String refreshToken;
    private String email;
    private String nombreCompleto;

    public AuthResponse() {}

    public AuthResponse(String token, String refreshToken, String email, String nombreCompleto) {
        this.token = token;
        this.refreshToken = refreshToken;
        this.email = email;
        this.nombreCompleto = nombreCompleto;
    }

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }

    public String getRefreshToken() { return refreshToken; }
    public void setRefreshToken(String refreshToken) { this.refreshToken = refreshToken; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getNombreCompleto() { return nombreCompleto; }
    public void setNombreCompleto(String nombreCompleto) { this.nombreCompleto = nombreCompleto; }
}