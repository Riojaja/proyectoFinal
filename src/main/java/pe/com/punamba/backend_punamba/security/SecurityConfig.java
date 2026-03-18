package pe.com.punamba.backend_punamba.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthFilter;
    private final AuthenticationProvider authenticationProvider;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthFilter, AuthenticationProvider authenticationProvider) {
        this.jwtAuthFilter = jwtAuthFilter;
        this.authenticationProvider = authenticationProvider;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(Customizer.withDefaults())
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth

                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                        .requestMatchers("/api/auth/**").permitAll()

                        .requestMatchers(HttpMethod.GET, "/uploads/**").permitAll()

                        .requestMatchers(HttpMethod.GET, "/api/productos/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/categorias/**").permitAll()

                        .requestMatchers(HttpMethod.GET, "/api/tiendas").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/tiendas/{id}").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/tiendas/vendedor/*").permitAll()

                        .requestMatchers("/api/usuarios/me").authenticated()
                        .requestMatchers("/api/carrito/**").authenticated()

                        .requestMatchers("/api/tiendas/perfil").hasAnyRole("VENDEDOR", "ADMIN")
                        .requestMatchers("/api/tiendas/perfil/banner").hasAnyRole("VENDEDOR", "ADMIN")

                        .requestMatchers(HttpMethod.GET, "/api/admin/dashboard").hasRole("ADMIN")

                        .requestMatchers("/api/usuarios/**").hasRole("ADMIN")

                        .requestMatchers(HttpMethod.POST, "/api/categorias/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/categorias/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/categorias/**").hasRole("ADMIN")

                        .requestMatchers(HttpMethod.GET, "/api/vendedores").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/vendedores/*").hasAnyRole("ADMIN", "VENDEDOR")
                        .requestMatchers(HttpMethod.PUT, "/api/vendedores/*/aprobar").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/vendedores/*/rechazar").hasRole("ADMIN")
                        .requestMatchers("/api/vendedores/perfil").hasAnyRole("VENDEDOR", "ADMIN")
                        .requestMatchers("/api/vendedores/perfil/logo").hasAnyRole("VENDEDOR", "ADMIN")

                        .requestMatchers(HttpMethod.GET, "/api/vendedores/pedidos").hasAnyRole("VENDEDOR", "ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/vendedores/pedidos/*/envio")
                        .hasAnyRole("VENDEDOR", "ADMIN")

                        .requestMatchers(HttpMethod.GET, "/api/marcas/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/atributos/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/atributo-valores/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/categorias/*/marcas").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/categorias/*/atributos").permitAll()

                        .requestMatchers(HttpMethod.POST, "/api/marcas/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/marcas/**").hasRole("ADMIN")

                        .requestMatchers(HttpMethod.POST, "/api/atributos/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/atributos/**").hasRole("ADMIN")

                        .requestMatchers(HttpMethod.POST, "/api/atributo-valores/**").hasRole("ADMIN")

                        .requestMatchers(HttpMethod.POST, "/api/categorias/marcas").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/categorias/*/marcas/*").hasRole("ADMIN")

                        .requestMatchers(HttpMethod.POST, "/api/categorias/atributos").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/categorias/atributos").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/categorias/*/atributos/*").hasRole("ADMIN")

                        .requestMatchers(HttpMethod.POST, "/api/productos/**").hasAnyRole("VENDEDOR", "ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/productos/**").hasAnyRole("VENDEDOR", "ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/productos/**").hasAnyRole("VENDEDOR", "ADMIN")

                        .requestMatchers(HttpMethod.GET, "/api/cupones/mis-cupones").hasAnyRole("VENDEDOR", "ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/cupones/validar").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/cupones").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/cupones/*").hasAnyRole("VENDEDOR", "ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/cupones/vendedor/*").hasAnyRole("VENDEDOR", "ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/cupones").hasAnyRole("VENDEDOR", "ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/cupones/*").hasAnyRole("VENDEDOR", "ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/cupones/*").hasAnyRole("VENDEDOR", "ADMIN")

                        .requestMatchers(HttpMethod.GET, "/api/metodos-envio/activos").hasAnyRole("VENDEDOR", "ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/metodos-envio").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/metodos-envio/*").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/metodos-envio").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/metodos-envio/*").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/metodos-envio/*").hasRole("ADMIN")

                        .requestMatchers(HttpMethod.GET, "/api/liquidaciones/mis-liquidaciones")
                        .hasAnyRole("VENDEDOR", "ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/liquidaciones").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/liquidaciones/vendedor/*/generar").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/liquidaciones/comprobante").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PATCH, "/api/liquidaciones/*/pagar").hasRole("ADMIN")

                        .requestMatchers(HttpMethod.GET, "/api/iconos-categoria/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/iconos-categoria/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/iconos-categoria/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/iconos-categoria/**").hasRole("ADMIN")

                        .requestMatchers(HttpMethod.GET, "/api/disputas/mis-disputas")
                        .hasAnyRole("CLIENTE", "VENDEDOR", "ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/disputas/admin/todas").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/disputas/estado").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/disputas/*").hasAnyRole("CLIENTE", "VENDEDOR", "ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/disputas/*/mensajes")
                        .hasAnyRole("CLIENTE", "VENDEDOR", "ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/disputas/*/mensajes")
                        .hasAnyRole("CLIENTE", "VENDEDOR", "ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/disputas/abrir").hasAnyRole("CLIENTE", "ADMIN")
                        .requestMatchers(HttpMethod.PATCH, "/api/disputas/*/resolver").hasRole("ADMIN")

                        .requestMatchers("/api/cliente/**").hasRole("CLIENTE")

                        .anyRequest().authenticated())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authenticationProvider(authenticationProvider)
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}