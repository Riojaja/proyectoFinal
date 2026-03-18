import { Component, OnInit, inject, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../../../../environments/environment';
import { forkJoin, of, switchMap } from 'rxjs';
import { catchError } from 'rxjs/operators';
import Swal from 'sweetalert2';

type Estado = 'pendiente' | 'procesando' | 'pagado';

type VendedorMini = {
  idVendedor: number;
  nombreTienda: string;
  ruc?: string;
  totalVentas?: number;
};

type LiquidacionApi = {
  idLiquidacion: number;
  idVendedor?: number;
  nombreTienda?: string;
  ruc?: string;
  montoTotalVentas: number;
  comisionRetenida: number;
  montoAPagar: number;
  estado: Estado;
  fechaCorte: string;
  fechaPago?: string | null;
  comprobanteTransferencia?: string | null;
};

type LiquidacionView = {
  id: number;
  vendedorId?: number;
  vendedorNombre: string;
  vendedorRuc: string;
  montoTotalVentas: number;
  comisionRetenida: number;
  montoAPagar: number;
  estado: Estado;
  fechaCorte: string;
  fechaPago?: string | null;
  comprobante?: string | null;
};

@Component({
  standalone: true,
  selector: 'app-admin-liquidaciones',
  imports: [CommonModule, FormsModule],
  templateUrl: './admin-liquidaciones.component.html',
  styleUrls: ['./admin-liquidaciones.component.scss'],
})
export class AdminLiquidacionesComponent implements OnInit {
  private http = inject(HttpClient);
  private cdr = inject(ChangeDetectorRef);
  private api = environment.apiUrl;

  loading = false;
  working = false; // Para prevenir doble clic

  vendedores: VendedorMini[] = [];
  liquidaciones: LiquidacionView[] = [];

  q = '';

  corte = {
    idVendedor: null as number | null,
    fechaCorte: this.hoyISO(),
    comisionPct: 0.10,
  };

  resumen = {
    vendedores: 0,
    totalVentas: 0,
    comision: 0,
    neto: 0,
  };

  drawerOpen = false;
  pago = {
    metodo: 'BCP Transferencia',
    fileName: '',
    file: null as File | null,
    comprobanteUrl: '',
    ids: [] as number[],
  };

  ngOnInit(): void {
    this.cargarVendedores();
    this.cargarLiquidaciones();
  }

  cargarVendedores() {
    this.http.get<any[]>(`${this.api}/vendedores`).pipe(
      catchError(() => of([]))
    ).subscribe((res: any[]) => {
      this.vendedores = (res || []).map(v => ({
        idVendedor: v.idVendedor,
        nombreTienda: v.nombreTienda || v.nombreEmpresa || 'Vendedor',
        ruc: v.ruc || '',
        totalVentas: Number(v.totalVentas || 0),
      }));
      this.recalcularResumen();
      this.cdr.detectChanges();
    });
  }

  cargarLiquidaciones() {
    this.loading = true;
    this.cdr.detectChanges(); // Forzar mostrar spinner

    this.http.get<LiquidacionApi[]>(`${this.api}/liquidaciones`).subscribe({
      next: (res) => {
        this.liquidaciones = (res || []).map(x => this.toView(x));
        this.loading = false;
        this.cdr.detectChanges(); // Forzar actualización de la tabla
      },
      error: (err) => {
        console.error('Error cargando liquidaciones', err);
        this.liquidaciones = [];
        this.loading = false;
        this.cdr.detectChanges();
        Swal.fire({
          icon: 'error',
          title: 'Error',
          text: 'No se pudieron cargar las liquidaciones.',
          timer: 3000,
          showConfirmButton: false,
        });
      }
    });
  }

  private toView(x: LiquidacionApi): LiquidacionView {
    return {
      id: x.idLiquidacion,
      vendedorId: x.idVendedor,
      vendedorNombre: x.nombreTienda || '—',
      vendedorRuc: x.ruc || '—',
      montoTotalVentas: Number(x.montoTotalVentas || 0),
      comisionRetenida: Number(x.comisionRetenida || 0),
      montoAPagar: Number(x.montoAPagar || 0),
      estado: (String(x.estado || 'pendiente').toLowerCase() as Estado),
      fechaCorte: x.fechaCorte || '',
      fechaPago: x.fechaPago || null,
      comprobante: x.comprobanteTransferencia || null,
    };
  }

  get pendientes(): LiquidacionView[] {
    return this.liquidaciones.filter(l => l.estado !== 'pagado');
  }

  get gmvTotalPorLiquidar(): number {
    return this.pendientes.reduce((a, x) => a + x.montoTotalVentas, 0);
  }

  get comisionesTotales(): number {
    return this.pendientes.reduce((a, x) => a + x.comisionRetenida, 0);
  }

  get montoPendientePago(): number {
    return this.pendientes.reduce((a, x) => a + x.montoAPagar, 0);
  }

  get cantidadSeleccionada(): number {
    return this.pago.ids.length;
  }

  get totalSeleccionado(): number {
    return this.liquidaciones
      .filter(x => this.pago.ids.includes(x.id))
      .reduce((acc, x) => acc + x.montoAPagar, 0);
  }

  get rows(): LiquidacionView[] {
    const s = this.q.trim().toLowerCase();
    if (!s) return this.liquidaciones;

    return this.liquidaciones.filter(r =>
      String(r.id).toLowerCase().includes(s) ||
      (r.vendedorNombre || '').toLowerCase().includes(s) ||
      (r.vendedorRuc || '').toLowerCase().includes(s) ||
      (r.estado || '').toLowerCase().includes(s)
    );
  }

  badgeEstado(e: Estado) {
    if (e === 'pagado') return 'badge bg-success-subtle text-success';
    if (e === 'procesando') return 'badge bg-warning-subtle text-warning';
    return 'badge bg-danger-subtle text-danger';
  }

  exportarCSV() {
    if (this.working) return;
    const data = this.rows;
    const header = [
      'ID',
      'Vendedor',
      'RUC',
      'MontoTotalVentas',
      'ComisionRetenida',
      'MontoAPagar',
      'Estado',
      'FechaCorte',
      'FechaPago',
      'Comprobante'
    ];

    const lines = [
      header.join(','),
      ...data.map(x => [
        x.id,
        this.safeCSV(x.vendedorNombre),
        this.safeCSV(x.vendedorRuc),
        x.montoTotalVentas,
        x.comisionRetenida,
        x.montoAPagar,
        x.estado,
        x.fechaCorte,
        x.fechaPago || '',
        this.safeCSV(x.comprobante || '')
      ].join(','))
    ];

    const blob = new Blob([lines.join('\n')], { type: 'text/csv;charset=utf-8;' });
    const url = URL.createObjectURL(blob);

    const a = document.createElement('a');
    a.href = url;
    a.download = `liquidaciones_${this.hoyISO()}.csv`;
    a.click();

    URL.revokeObjectURL(url);
  }

  private safeCSV(v: any) {
    const s = String(v ?? '').replaceAll('"', '""');
    return `"${s}"`;
  }

  onCorteChange() {
    this.recalcularResumen();
  }

  recalcularResumen() {
    const vendors = this.vendedoresSeleccionados();
    const ventas = vendors.reduce((a, v) => a + Number(v.totalVentas || 0), 0);
    const com = ventas * this.corte.comisionPct;
    const neto = ventas - com;

    this.resumen = {
      vendedores: vendors.length,
      totalVentas: ventas,
      comision: com,
      neto,
    };
    this.cdr.detectChanges();
  }

  private vendedoresSeleccionados(): VendedorMini[] {
    if (!this.vendedores.length) return [];
    if (this.corte.idVendedor) {
      return this.vendedores.filter(v => v.idVendedor === this.corte.idVendedor);
    }
    return this.vendedores;
  }

  generarCortes() {
    if (this.working) return;
    const vendors = this.vendedoresSeleccionados();
    if (!vendors.length) {
      Swal.fire({
        icon: 'warning',
        title: 'Sin vendedores',
        text: 'No hay vendedores para generar liquidaciones.',
        timer: 2000,
        showConfirmButton: false,
      });
      return;
    }

    this.working = true;
    this.cdr.detectChanges();

    const reqs = vendors.map(v => {
      const ventas = Number(v.totalVentas || 0);
      const com = ventas * this.corte.comisionPct;
      const neto = ventas - com;

      const body = {
        montoTotalVentas: ventas,
        comisionRetenida: com,
        montoAPagar: neto,
        fechaCorte: this.corte.fechaCorte,
      };

      return this.http.post(`${this.api}/liquidaciones/vendedor/${v.idVendedor}/generar`, body).pipe(
        catchError(err => of({ __err: true, err }))
      );
    });

    forkJoin(reqs).subscribe((results: any[]) => {
      const ok = results.filter(r => !r?.__err).length;
      const fail = results.filter(r => r?.__err).length;

      this.working = false;

      if (ok > 0) {
        Swal.fire({
          icon: 'success',
          title: 'Cortes generados',
          text: `Se generaron ${ok} liquidación(es) hasta ${this.corte.fechaCorte}.`,
          timer: 3000,
          showConfirmButton: false,
        });
      }

      if (fail > 0) {
        Swal.fire({
          icon: 'error',
          title: 'Error',
          text: `Fallaron ${fail} liquidación(es).`,
          timer: 3000,
          showConfirmButton: false,
        });
      }

      this.cargarLiquidaciones();
      this.cdr.detectChanges();
    });
  }

  abrirProcesarPagosMasivo() {
    if (this.working) return;
    this.pago.ids = this.pendientes.map(x => x.id);
    this.resetPagoForm();
    this.openDrawer();
  }

  abrirProcesarPagoUno(id: number) {
    if (this.working) return;
    this.pago.ids = [id];
    this.resetPagoForm();
    this.openDrawer();
  }

  private resetPagoForm() {
    this.pago.metodo = 'BCP Transferencia';
    this.pago.fileName = '';
    this.pago.file = null;
    this.pago.comprobanteUrl = '';
  }

  onFileSelected(ev: Event) {
    const input = ev.target as HTMLInputElement;
    const f = input?.files?.[0] || null;
    this.pago.file = f;
    this.pago.fileName = f ? f.name : '';
    this.cdr.detectChanges();
  }

  private subirComprobante() {
    if (!this.pago.file) {
      return of({ url: '' });
    }

    const fd = new FormData();
    fd.append('file', this.pago.file);

    return this.http.post<{ url: string; fileName: string }>(
      `${this.api}/liquidaciones/comprobante`,
      fd
    );
  }

  procesarPagos() {
    if (this.working) return;

    const ids = (this.pago.ids || []).filter(Boolean);
    if (!ids.length) {
      Swal.fire({
        icon: 'warning',
        title: 'Selección vacía',
        text: 'Selecciona al menos una liquidación.',
        timer: 2000,
        showConfirmButton: false,
      });
      return;
    }

    if (!this.pago.file) {
      Swal.fire({
        icon: 'warning',
        title: 'Comprobante requerido',
        text: 'Debes subir un comprobante antes de procesar el pago.',
        timer: 2000,
        showConfirmButton: false,
      });
      return;
    }

    this.working = true;
    this.cdr.detectChanges();

    this.subirComprobante().pipe(
      switchMap((uploadRes: any) => {
        const comprobanteUrl = uploadRes?.url || '';

        if (!comprobanteUrl) {
          throw new Error('No se pudo obtener la URL del comprobante.');
        }

        this.pago.comprobanteUrl = comprobanteUrl;

        const reqs = ids.map(id =>
          this.http.patch(`${this.api}/liquidaciones/${id}/pagar`, {
            comprobanteUrl: this.pago.comprobanteUrl
          }).pipe(
            catchError(err => of({ __err: true, id, err }))
          )
        );

        return forkJoin(reqs);
      })
    ).subscribe({
      next: (results: any[]) => {
        const ok = results.filter(r => !r?.__err).length;
        const fail = results.filter(r => r?.__err).length;

        this.working = false;

        if (ok > 0) {
          Swal.fire({
            icon: 'success',
            title: 'Pagos procesados',
            text: `Pagos procesados correctamente: ${ok}.`,
            timer: 3000,
            showConfirmButton: false,
          });
          this.closeDrawer();
          this.cargarLiquidaciones();
        }

        if (fail > 0) {
          Swal.fire({
            icon: 'error',
            title: 'Error',
            text: `Fallaron ${fail} pagos.`,
            timer: 3000,
            showConfirmButton: false,
          });
        }
        this.cdr.detectChanges();
      },
      error: (err) => {
        console.error(err);
        this.working = false;
        this.cdr.detectChanges();
        Swal.fire({
          icon: 'error',
          title: 'Error',
          text: 'No se pudo subir el comprobante o procesar los pagos.',
          timer: 3000,
          showConfirmButton: false,
        });
      }
    });
  }

  money(n: number) {
    const x = Number(n || 0);
    return x.toLocaleString('es-PE', {
      style: 'currency',
      currency: 'PEN'
    });
  }

  formatFecha(fecha?: string | null) {
    if (!fecha) return '—';
    return fecha;
  }

  private hoyISO() {
    const d = new Date();
    const y = d.getFullYear();
    const m = String(d.getMonth() + 1).padStart(2, '0');
    const dd = String(d.getDate()).padStart(2, '0');
    return `${y}-${m}-${dd}`;
  }

  private lockBodyScroll(lock: boolean) {
    if (typeof document === 'undefined') return;
    document.body.style.overflow = lock ? 'hidden' : '';
  }

  openDrawer() {
    this.drawerOpen = true;
    this.lockBodyScroll(true);
    this.cdr.detectChanges();
  }

  closeDrawer() {
    this.drawerOpen = false;
    this.lockBodyScroll(false);
    this.cdr.detectChanges();
  }

  verDetalle(r: LiquidacionView) {
    Swal.fire({
      title: `Liquidación #${r.id}`,
      html: `
        <div style="text-align: left;">
          <p><strong>Vendedor:</strong> ${r.vendedorNombre}</p>
          <p><strong>RUC:</strong> ${r.vendedorRuc}</p>
          <p><strong>Estado:</strong> ${r.estado}</p>
          <p><strong>Fecha corte:</strong> ${r.fechaCorte || '—'}</p>
          <p><strong>Fecha pago:</strong> ${r.fechaPago || '—'}</p>
          <p><strong>Comprobante:</strong> ${r.comprobante || '—'}</p>
        </div>
      `,
      icon: 'info',
      confirmButtonText: 'Cerrar',
    });
  }

  isPagoSelected(id: number): boolean {
    return this.pago.ids.includes(id);
  }

  togglePagoId(id: number, checked: boolean) {
    if (checked) {
      this.pago.ids = Array.from(new Set([...this.pago.ids, id]));
    } else {
      this.pago.ids = this.pago.ids.filter(x => x !== id);
    }
    this.cdr.detectChanges();
  }
}