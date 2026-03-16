import { Component, OnInit, inject, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, ReactiveFormsModule } from '@angular/forms';
import { HttpClient } from '@angular/common/http';
import Swal from 'sweetalert2';

type EstadoVendedor = 'pendiente' | 'activo' | 'inactivo';
type TabEstado = 'todas' | EstadoVendedor;

type VendedorRow = {
  idVendedor: number;
  nombreEmpresa: string;
  ruc: string;
  email: string;
  telefono: string;
  fechaSolicitud: string;
  calificacion: number;
  totalVentas: number;
  estado: EstadoVendedor;
  tiendas?: string;
};

@Component({
  standalone: true,
  selector: 'app-admin-vendedores',
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './admin-vendedores.component.html',
  styleUrls: ['./admin-vendedores.component.scss'],
})
export class AdminVendedoresComponent implements OnInit {
  private http = inject(HttpClient);
  private fb = inject(FormBuilder);
  private cdr = inject(ChangeDetectorRef);

  private api = 'http://localhost:8090/api';

  cargando = false;
  // Propiedades para prevenir doble clic en acciones
  procesando = false;

  tab: TabEstado = 'todas';

  vendedores: VendedorRow[] = [];
  seleccionado: VendedorRow | null = null;

  modalDetalle = false;

  detalleForm = this.fb.group({
    nombreEmpresa: [''],
    ruc: [''],
    email: [''],
    telefono: [''],
    fechaSolicitud: [''],
    calificacion: [''],
    totalVentas: [''],
    estado: [''],
    tiendas: [''],
  });

  ngOnInit(): void {
    this.cargar();
  }

  cargar(): void {
    this.cargando = true;
    this.cdr.detectChanges(); // Forzar actualización para mostrar spinner

    this.http.get<VendedorRow[]>(`${this.api}/vendedores`).subscribe({
      next: (res) => {
        this.vendedores = (res || []).map(v => ({
          ...v,
          estado: String(v.estado || '').toLowerCase() as EstadoVendedor
        }));
        this.cargando = false;
        this.cdr.detectChanges();
      },
      error: (e) => {
        console.error('Error cargando vendedores', e);
        this.vendedores = [];
        this.cargando = false;
        this.cdr.detectChanges();
        Swal.fire({
          icon: 'error',
          title: 'Error',
          text: 'No se pudo cargar las solicitudes de vendedores.',
          timer: 3000,
          showConfirmButton: false,
        });
      }
    });
  }

  setTab(t: TabEstado): void {
    this.tab = t;
  }

  get totalCount(): number {
    return this.vendedores.length;
  }

  get pendientesCount(): number {
    return this.vendedores.filter(v => v.estado === 'pendiente').length;
  }

  get activosCount(): number {
    return this.vendedores.filter(v => v.estado === 'activo').length;
  }

  get inactivosCount(): number {
    return this.vendedores.filter(v => v.estado === 'inactivo').length;
  }

  get vendedoresFiltrados(): VendedorRow[] {
    if (this.tab === 'todas') return this.vendedores;
    return this.vendedores.filter(v => v.estado === this.tab);
  }

  verDetalle(v: VendedorRow): void {
    this.seleccionado = v;

    this.detalleForm.patchValue({
      nombreEmpresa: v.nombreEmpresa,
      ruc: v.ruc,
      email: v.email,
      telefono: v.telefono,
      fechaSolicitud: v.fechaSolicitud,
      calificacion: `${this.estrellas(v.calificacion)} ★`,
      totalVentas: `S/. ${Number(v.totalVentas || 0).toFixed(2)}`,
      estado: v.estado,
      tiendas: v.tiendas || 'Sin tienda',
    });

    this.modalDetalle = true;
  }

  cerrarDetalle(): void {
    this.modalDetalle = false;
  }

  badgeEstado(e: EstadoVendedor): string {
    if (e === 'activo') return 'badge bg-success-subtle text-success';
    if (e === 'pendiente') return 'badge bg-warning-subtle text-warning';
    return 'badge bg-secondary-subtle text-secondary';
  }

  estrellas(n: number): string {
    const x = Math.round((n || 0) * 10) / 10;
    return x.toFixed(1);
  }

  puedeAprobar(v: VendedorRow | null): boolean {
    if (!v) return false;
    return v.estado === 'pendiente' || v.estado === 'inactivo';
  }

  puedeRechazar(v: VendedorRow | null): boolean {
    if (!v) return false;
    return v.estado === 'pendiente' || v.estado === 'activo';
  }

  aprobar(v: VendedorRow): void {
    if (this.procesando) return;
    this.procesando = true;

    Swal.fire({
      title: '¿Aprobar vendedor?',
      text: `¿Estás seguro de aprobar a ${v.nombreEmpresa}?`,
      icon: 'question',
      showCancelButton: true,
      confirmButtonColor: '#28a745',
      cancelButtonColor: '#6c757d',
      confirmButtonText: 'Sí, aprobar',
      cancelButtonText: 'Cancelar',
    }).then((result) => {
      if (result.isConfirmed) {
        this.http.put(`${this.api}/vendedores/${v.idVendedor}/aprobar`, {}).subscribe({
          next: () => {
            v.estado = 'activo';
            if (this.seleccionado && this.seleccionado.idVendedor === v.idVendedor) {
              this.detalleForm.patchValue({ estado: 'activo' });
            }
            this.procesando = false;
            Swal.fire({
              icon: 'success',
              title: 'Aprobado',
              text: `Vendedor #${v.idVendedor} aprobado correctamente.`,
              timer: 2000,
              showConfirmButton: false,
            });
          },
          error: (e) => {
            console.error(e);
            this.procesando = false;
            Swal.fire({
              icon: 'error',
              title: 'Error',
              text: 'No se pudo aprobar la solicitud.',
              timer: 3000,
              showConfirmButton: false,
            });
          }
        });
      } else {
        this.procesando = false;
      }
    });
  }

  rechazar(v: VendedorRow): void {
    if (this.procesando) return;
    this.procesando = true;

    Swal.fire({
      title: '¿Rechazar vendedor?',
      text: `¿Estás seguro de rechazar a ${v.nombreEmpresa}?`,
      icon: 'warning',
      showCancelButton: true,
      confirmButtonColor: '#dc3545',
      cancelButtonColor: '#6c757d',
      confirmButtonText: 'Sí, rechazar',
      cancelButtonText: 'Cancelar',
    }).then((result) => {
      if (result.isConfirmed) {
        this.http.put(`${this.api}/vendedores/${v.idVendedor}/rechazar`, {}).subscribe({
          next: () => {
            v.estado = 'inactivo';
            if (this.seleccionado && this.seleccionado.idVendedor === v.idVendedor) {
              this.detalleForm.patchValue({ estado: 'inactivo' });
            }
            this.procesando = false;
            Swal.fire({
              icon: 'success',
              title: 'Rechazado',
              text: `Vendedor #${v.idVendedor} rechazado.`,
              timer: 2000,
              showConfirmButton: false,
            });
          },
          error: (e) => {
            console.error(e);
            this.procesando = false;
            Swal.fire({
              icon: 'error',
              title: 'Error',
              text: 'No se pudo rechazar la solicitud.',
              timer: 3000,
              showConfirmButton: false,
            });
          }
        });
      } else {
        this.procesando = false;
      }
    });
  }

  aprobarSeleccionado(): void {
    if (!this.seleccionado) return;
    this.aprobar(this.seleccionado);
  }

  rechazarSeleccionado(): void {
    if (!this.seleccionado) return;
    this.rechazar(this.seleccionado);
  }
}