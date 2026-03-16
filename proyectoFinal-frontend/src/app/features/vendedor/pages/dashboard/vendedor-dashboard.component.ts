import { Component, OnInit, ChangeDetectorRef, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import Swal from 'sweetalert2';

type UltimoPedido = {
    id: number | string;
    producto: string;
    fecha: string;
};

type DashboardDto = {
    ventasMensuales: number;
    pedidosPendientes: number;
    stockBajo: number;
    calificacionPromedio: number;
    ultimosPedidos: UltimoPedido[];
    ventasPorMes: number[]; // 12 valores
};

@Component({
    standalone: true,
    selector: 'app-vendedor-dashboard',
    imports: [CommonModule],
    templateUrl: './vendedor-dashboard.component.html',
    styleUrls: ['./vendedor-dashboard.component.scss'],
})
export class VendedorDashboardComponent implements OnInit {
    private cdr = inject(ChangeDetectorRef);

    cargando = false;
    dashboard: DashboardDto = {
        ventasMensuales: 0,
        pedidosPendientes: 0,
        stockBajo: 0,
        calificacionPromedio: 0,
        ultimosPedidos: [],
        ventasPorMes: [0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0],
    };

    meses = ['Ene', 'Feb', 'Mar', 'Abr', 'May', 'Jun', 'Jul', 'Ago', 'Set', 'Oct', 'Nov', 'Dic'];

    ngOnInit(): void {
        this.cargarDashboard();
    }

    cargarDashboard(): void {
        if (this.cargando) return; // 🚫 Prevención de doble clic
        this.cargando = true;
        this.cdr.detectChanges(); // Forzar actualización para mostrar spinner

        // Simulación de carga de datos (reemplazar con llamada HTTP real)
        setTimeout(() => {
            try {
                // Aquí asignas los datos que vienen del backend
                this.dashboard = {
                    ventasMensuales: 12500.50,
                    pedidosPendientes: 3,
                    stockBajo: 2,
                    calificacionPromedio: 4.7,
                    ultimosPedidos: [
                        { id: 101, producto: 'Producto A', fecha: '2026-03-14' },
                        { id: 102, producto: 'Producto B', fecha: '2026-03-13' },
                        { id: 103, producto: 'Producto C', fecha: '2026-03-12' },
                    ],
                    ventasPorMes: [0, 0, 1200, 3400, 2800, 0, 0, 0, 0, 0, 0, 0],
                };
            } catch (error) {
                console.error('Error al cargar dashboard', error);
                Swal.fire({
                    icon: 'error',
                    title: 'Error',
                    text: 'No se pudo cargar el dashboard del vendedor.',
                    timer: 3000,
                    showConfirmButton: false,
                });
            } finally {
                this.cargando = false;
                this.cdr.detectChanges();
            }
        }, 1000); // Simula 1 segundo de carga
    }

    // Método para refrescar (llamado desde el botón)
    refrescar(): void {
        this.cargarDashboard();
    }

    get maxVenta(): number {
        return Math.max(...this.dashboard.ventasPorMes, 1);
    }

    get hayPedidos(): boolean {
        return this.dashboard.ultimosPedidos.length > 0;
    }

    get hayVentas(): boolean {
        return this.dashboard.ventasPorMes.some(v => v > 0);
    }

    barHeight(value: number): string {
        if (value <= 0) return '0%';
        const pct = Math.round((value / this.maxVenta) * 100);
        return Math.max(pct, 8) + '%';
    }
}