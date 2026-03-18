import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { HttpClient } from '@angular/common/http';
import Swal from 'sweetalert2';

import { environment } from '../../../../../environments/environment';

type Pedido = {
  idOrden: number;
  numeroOrden: string;
  total: number;
  subtotal?: number;
  impuesto?: number;
  fechaOrden?: string;
  metodoPago?: string;
  estadoOrden?: string;
  colorEstado?: string;
};

@Component({
  selector: 'app-pedidos',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './pedidos.component.html',
  styleUrls: ['./pedidos.component.scss']
})
export class PedidosComponent implements OnInit {
  private http = inject(HttpClient);
  private api = environment.apiUrl;

  cargando = true;
  pedidos: Pedido[] = [];

  ngOnInit(): void {
    this.cargarPedidos();
  }

  cargarPedidos() {
    this.cargando = true;

    this.http.get<Pedido[]>(`${this.api}/ordenes/mis-ordenes`).subscribe({
      next: (res) => {
        this.pedidos = Array.isArray(res) ? res : [];
        this.cargando = false;
      },
      error: (err) => {
        this.cargando = false;
        this.pedidos = [];

        const msg =
          typeof err?.error === 'string'
            ? err.error
            : err?.error?.message || 'No se pudieron cargar tus pedidos.';

        Swal.fire({
          icon: 'error',
          title: 'Error',
          text: msg
        });
      }
    });
  }

  estadoTexto(p: Pedido): string {
    return p?.estadoOrden || 'Pendiente';
  }

  estadoClase(p: Pedido): string {
    const estado = (p?.estadoOrden || '').toUpperCase();

    if (estado.includes('ENTREGADO')) return 'success';
    if (estado.includes('ENVIADO') || estado.includes('EN CAMINO')) return 'primary';
    if (estado.includes('PROCESANDO') || estado.includes('PREPARANDO')) return 'warning';
    if (estado.includes('PAGADO') || estado.includes('CONFIRMADO')) return 'info';
    if (estado.includes('CANCELADO')) return 'danger';

    return 'secondary';
  }

  fechaTexto(p: Pedido): string {
    const fecha = p?.fechaOrden;

    if (!fecha) return '-';

    const f = new Date(fecha);

    if (isNaN(f.getTime())) return fecha;

    return f.toLocaleString('es-PE', {
      year: 'numeric',
      month: '2-digit',
      day: '2-digit',
      hour: '2-digit',
      minute: '2-digit'
    });
  }

  metodoTexto(p: Pedido): string {
    return p?.metodoPago || 'No especificado';
  }

  totalTexto(p: Pedido): number {
    return Number(p?.total || 0);
  }

  trackByPedido(_: number, p: Pedido) {
    return p.idOrden;
  }

  totalAcumulado(): number {
    return this.pedidos.reduce((acc, p) => acc + this.totalTexto(p), 0);
  }
}