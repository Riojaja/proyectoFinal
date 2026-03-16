import { Component, OnInit, OnDestroy, ChangeDetectorRef, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Subject, takeUntil } from 'rxjs';
import Swal from 'sweetalert2';
import { AdminService, AdminDashboardResponse } from '../../../../core/services/admin.service';

@Component({
  standalone: true,
  selector: 'app-admin-dashboard',
  imports: [CommonModule],
  templateUrl: './admin-dashboard.component.html',
  styleUrls: ['./admin-dashboard.component.scss'],
})
export class AdminDashboardComponent implements OnInit, OnDestroy {
  private adminService = inject(AdminService);
  private cdr = inject(ChangeDetectorRef);
  private destroy$ = new Subject<void>();

  cargando = true;
  error = '';

  gmv = 0;
  comisiones = 0;
  vendedoresPendientes = 0;
  categoriasActivas = 0;

  vendedores: { nombre: string; totalVentas: number; logoUrl?: string | null }[] = [];

  tipoGrafico: 'mensual' | 'semanal' = 'mensual';

  barras: number[] = [];
  etiquetas: string[] = [];

  ventasMensuales: { periodo: number; total: number }[] = [];
  ventasSemanales: { periodo: number; total: number }[] = [];

  ngOnInit(): void {
    console.log('Dashboard: ngOnInit ejecutado'); // Para depuración
    this.cargarDashboard();
  }

  cargarDashboard(): void {
    this.cargando = true;
    this.error = '';
    // Forzar detección para mostrar "Cargando..."
    this.cdr.detectChanges();

    this.adminService.obtenerDashboard()
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (resp: AdminDashboardResponse) => {
          console.log('Dashboard: datos recibidos', resp); // Depuración
          this.gmv = Number(resp.gmv ?? 0);
          this.comisiones = Number(resp.comisiones ?? 0);
          this.vendedoresPendientes = Number(resp.vendedoresPendientes ?? 0);
          this.categoriasActivas = Number(resp.categoriasActivas ?? 0);

          this.vendedores = (resp.vendedores ?? []).map(v => ({
            nombre: v.nombre,
            totalVentas: Number(v.totalVentas ?? 0),
            logoUrl: v.logoUrl
          }));

          this.ventasMensuales = (resp.ventasMensuales ?? []).map(x => ({
            periodo: Number(x.periodo),
            total: Number(x.total ?? 0)
          }));

          this.ventasSemanales = (resp.ventasSemanales ?? []).map(x => ({
            periodo: Number(x.periodo),
            total: Number(x.total ?? 0)
          }));

          this.aplicarGrafico();
          this.cargando = false;
          // Forzar detección para actualizar la vista con los datos
          this.cdr.detectChanges();
        },
        error: (err) => {
          console.error('Error cargando dashboard admin', err);
          this.error = 'No se pudo cargar el dashboard.';
          this.cargando = false;
          this.cdr.detectChanges(); // Actualizar vista con el error

          // Mostrar notificación de error con SweetAlert2 (opcional)
          Swal.fire({
            icon: 'error',
            title: 'Oops...',
            text: 'Ocurrió un error al cargar el dashboard',
            timer: 3000,
            showConfirmButton: false
          });
        }
      });
  }

  cambiarGrafico(tipo: 'mensual' | 'semanal'): void {
    this.tipoGrafico = tipo;
    this.aplicarGrafico();
    this.cdr.detectChanges(); // Forzar actualización del gráfico
  }

  aplicarGrafico(): void {
    if (this.tipoGrafico === 'mensual') {
      this.barras = this.ventasMensuales.map(x => x.total);
      this.etiquetas = this.ventasMensuales.map(x => `${x.periodo}m`);
    } else {
      this.barras = this.ventasSemanales.map(x => x.total);
      this.etiquetas = this.ventasSemanales.map(x => `S${x.periodo}`);
    }
  }

  get maxBarValue(): number {
    const max = Math.max(...this.barras, 0);
    return max > 0 ? max : 1;
  }

  getBarHeight(valor: number): number {
    return (valor / this.maxBarValue) * 100;
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }
}