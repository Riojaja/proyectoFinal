package pe.com.punamba.backend_punamba.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pe.com.punamba.backend_punamba.dto.AdminDashboardDTO;
import pe.com.punamba.backend_punamba.service.AdminDashboardService;

@RestController
@RequestMapping("/api/admin/dashboard")
public class AdminDashboardController {

    private final AdminDashboardService adminDashboardService;

    public AdminDashboardController(AdminDashboardService adminDashboardService) {
        this.adminDashboardService = adminDashboardService;
    }

    @GetMapping
    public ResponseEntity<AdminDashboardDTO> obtenerDashboard() {
        return ResponseEntity.ok(adminDashboardService.obtenerDashboard());
    }
}