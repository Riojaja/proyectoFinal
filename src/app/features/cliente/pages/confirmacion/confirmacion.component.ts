import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router, RouterModule } from '@angular/router';
import { HttpClient } from '@angular/common/http';

import { environment } from '../../../../../environments/environment';

type Orden = {
  idOrden: number;
  numeroOrden: string;
  total: number;
  fechaOrden?: string;
  nombreDestinatario?: string;
  direccionEntrega?: string;
  detalles?: Array<{ cantidad?: number }>;
  items?: Array<{ cantidad?: number }>;
};

@Component({
  selector: 'app-confirmacion',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './confirmacion.component.html',
  styleUrls: ['./confirmacion.component.scss']
})
export class ConfirmacionComponent implements OnInit {
  private router = inject(Router);
  private route = inject(ActivatedRoute);
  private http = inject(HttpClient);
  private api = environment.apiUrl;

  pedido: Orden | null = null;
  direccion: any = null;
  metodo = 'Pago';
  fecha = '';
  numeroPedido = '';
  total = 0;
  cantidadArticulos = 0;

  ngOnInit(): void {
    const state = history.state;
    const id = Number(this.route.snapshot.paramMap.get('id'));

    this.metodo = state?.metodo || this.leerMetodoTemporal() || 'Pago';

    if (state?.orden) {
      this.pedido = state.orden;
      this.direccion = state?.direccion || this.mapearDireccionDesdeOrden(state.orden);
      this.aplicarDatos();
      return;
    }

    if (id && !isNaN(id)) {
      this.http.get<Orden>(`${this.api}/ordenes/${id}`).subscribe({
        next: (orden) => {
          this.pedido = orden;
          this.direccion = this.mapearDireccionDesdeOrden(orden);
          this.aplicarDatos();
        },
        error: () => {
          this.router.navigateByUrl('/cliente/pedidos');
        }
      });
      return;
    }

    this.router.navigateByUrl('/cliente');
  }

  private leerMetodoTemporal(): string {
    try {
      return sessionStorage.getItem('checkout_metodo_pago') || '';
    } catch {
      return '';
    }
  }

  private mapearDireccionDesdeOrden(orden: Orden | null) {
    if (!orden) return null;

    return {
      nombreDestinatario: orden.nombreDestinatario || '',
      telefono: '',
      direccionLinea1: orden.direccionEntrega || '',
      direccionLinea2: ''
    };
  }

  private aplicarDatos() {
    this.numeroPedido = this.pedido?.numeroOrden || 'PNB-000000';
    this.total = Number(this.pedido?.total || 0);

    const items = Array.isArray(this.pedido?.items)
      ? this.pedido?.items || []
      : this.pedido?.detalles || [];

    this.cantidadArticulos = items.reduce((acc: number, it: any) => acc + Number(it?.cantidad || 0), 0);

    if (this.pedido?.fechaOrden) {
      const f = new Date(this.pedido.fechaOrden);
      this.fecha = isNaN(f.getTime())
        ? this.pedido.fechaOrden
        : f.toLocaleString('es-PE', {
            year: 'numeric',
            month: '2-digit',
            day: '2-digit',
            hour: '2-digit',
            minute: '2-digit'
          });
    } else {
      const ahora = new Date();
      this.fecha = ahora.toLocaleString('es-PE', {
        year: 'numeric',
        month: '2-digit',
        day: '2-digit',
        hour: '2-digit',
        minute: '2-digit'
      });
    }
  }
}