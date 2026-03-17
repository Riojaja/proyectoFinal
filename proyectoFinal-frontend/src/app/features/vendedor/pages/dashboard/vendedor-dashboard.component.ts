import { Component, OnInit, ChangeDetectorRef, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../../../../environments/environment';
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
    ventasPorCategoria?: { categoria: string; total: number }[]; // Para gráfico circular
};

@Component({
    standalone: true,
    selector: 'app-vendedor-dashboard',
    imports: [CommonModule],
    templateUrl: './vendedor-dashboard.component.html',
    styleUrls: ['./vendedor-dashboard.component.scss'],
})
export class VendedorDashboardComponent implements OnInit {
    private http = inject(HttpClient);
    private cdr = inject(ChangeDetectorRef);
    private api = environment.apiUrl;

    cargando = false;
    dashboard: DashboardDto = {
        ventasMensuales: 0,
        pedidosPendientes: 0,
        stockBajo: 0,
        calificacionPromedio: 0,
        ultimosPedidos: [],
        ventasPorMes: [0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0],
        ventasPorCategoria: []
    };

    meses = ['Ene', 'Feb', 'Mar', 'Abr', 'May', 'Jun', 'Jul', 'Ago', 'Set', 'Oct', 'Nov', 'Dic'];

    // Para gráfico circular
    chartColors = ['#FF6B6B', '#4ECDC4', '#45B7D1', '#96CEB4', '#FFEAA7', '#D4A5A5', '#9B59B6', '#3498DB'];

    ngOnInit(): void {
        this.cargarDashboard();
    }

    cargarDashboard(): void {
        if (this.cargando) return;
        this.cargando = true;
        this.cdr.detectChanges();

        // 👇 LLAMADA REAL AL BACKEND
        this.http.get<DashboardDto>(`${this.api}/vendedores/dashboard`).subscribe({
            next: (data) => {
                this.dashboard = {
                    ventasMensuales: data.ventasMensuales || 0,
                    pedidosPendientes: data.pedidosPendientes || 0,
                    stockBajo: data.stockBajo || 0,
                    calificacionPromedio: data.calificacionPromedio || 0,
                    ultimosPedidos: data.ultimosPedidos || [],
                    ventasPorMes: data.ventasPorMes || [0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0],
                    ventasPorCategoria: data.ventasPorCategoria || []
                };
                this.cargando = false;
                this.cdr.detectChanges();
            },
            error: (err) => {
                console.error('Error cargando dashboard:', err);
                this.cargando = false;
                this.cdr.detectChanges();
                Swal.fire({
                    icon: 'error',
                    title: 'Error',
                    text: 'No se pudo cargar el dashboard del vendedor.',
                    timer: 3000,
                    showConfirmButton: false,
                });
            }
        });
    }

    refrescar(): void {
        this.cargarDashboard();
    }

    // Para gráfico de barras
    get maxVenta(): number {
        return Math.max(...this.dashboard.ventasPorMes, 1);
    }

    barHeight(value: number): string {
        if (value <= 0) return '0%';
        const pct = Math.round((value / this.maxVenta) * 100);
        return Math.max(pct, 8) + '%';
    }

    // Para gráfico de líneas
    get puntosLineas(): string {
        const ancho = 600;
        const alto = 200;
        const margen = 30;
        const max = this.maxVenta;
        
        if (max === 0) return '';
        
        const puntos = this.dashboard.ventasPorMes.map((valor, i) => {
            const x = margen + (i * (ancho - 2 * margen) / 11);
            const y = alto - margen - ((valor / max) * (alto - 2 * margen));
            return `${x},${y}`;
        }).join(' ');
        
        return puntos;
    }

    get hayVentas(): boolean {
        return this.dashboard.ventasPorMes.some(v => v > 0);
    }

    get hayPedidos(): boolean {
        return this.dashboard.ultimosPedidos.length > 0;
    }

    get hayVentasPorCategoria(): boolean {
        return (this.dashboard.ventasPorCategoria?.length || 0) > 0;
    }

    // Formateo de moneda
    formatMoney(valor: number): string {
        return new Intl.NumberFormat('es-PE', {
            style: 'currency',
            currency: 'PEN',
            minimumFractionDigits: 2
        }).format(valor);
    }

    // Obtener color para gráfico circular
    getColor(index: number): string {
        return this.chartColors[index % this.chartColors.length];
    }

    // Calcular total para porcentajes
    get totalVentasCategorias(): number {
        return this.dashboard.ventasPorCategoria?.reduce((acc, item) => acc + item.total, 0) || 0;
    }

    // Calcular porcentaje para gráfico circular
    porcentajeCategoria(total: number): number {
        if (this.totalVentasCategorias === 0) return 0;
        return (total / this.totalVentasCategorias) * 100;
    }
}