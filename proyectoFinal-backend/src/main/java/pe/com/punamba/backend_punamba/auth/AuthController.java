package pe.com.punamba.backend_punamba.auth;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pe.com.punamba.backend_punamba.dto.AuthResponse;
import pe.com.punamba.backend_punamba.dto.LoginRequest;
import pe.com.punamba.backend_punamba.dto.RegisterRequest;
import pe.com.punamba.backend_punamba.dto.RefreshTokenRequest;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@RequestBody RegisterRequest request) {
        return ResponseEntity.ok(authService.register(request));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refresh(@RequestBody RefreshTokenRequest request) {
        try {
            return ResponseEntity.ok(authService.refreshToken(request.getRefreshToken()));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(null);
        }
    }
}