import { Component, OnDestroy, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { forkJoin, of } from 'rxjs';
import { catchError } from 'rxjs/operators';
import Swal from 'sweetalert2';

import {
  Disputa,
  DisputaMensaje,
  DisputaService,
  EstadoDisputa,
  RolEmisor,
} from '../../../../core/services/disputa.service';

@Component({
  standalone: true,
  selector: 'app-admin-disputas',
  imports: [CommonModule, FormsModule],
  templateUrl: './admin-disputas.component.html',
  styleUrls: ['./admin-disputas.component.scss'],
})
export class AdminDisputasComponent implements OnInit, OnDestroy {
  private disputaService = inject(DisputaService);

  loading = false;
  loadingDrawer = false;
  working = false;
  sendingMessage = false;

  q = '';
  page = 1;
  pageSize = 6;

  disputas: Disputa[] = [];
  mensajes: DisputaMensaje[] = [];

  drawerOpen = false;
  selected: Disputa | null = null;

  readonly estadosDisponibles: EstadoDisputa[] = [
    'abierta',
    'en_revision',
    'en_negociacion',
    'resuelta_reembolso_total',
    'resuelta_reembolso_parcial',
    'resuelta_rechazada',
    'cerrada',
  ];

  draft = {
    nuevoEstado: null as EstadoDisputa | null,
    mensaje: '',
    adjuntoUrl: '',
    observacionAdmin: '',
    resolucionFinal: '',
    montoReembolso: null as number | null,
  };

  ngOnInit(): void {
    this.cargar();
  }

  ngOnDestroy(): void {
    this.lockBodyScroll(false);
  }

  cargar() {
    if (this.loading) return;

    this.loading = true;

    this.disputaService.todasAdmin().subscribe({
      next: (data) => {
        this.disputas = (data || []).sort(
          (a, b) => Number(b.idDisputa) - Number(a.idDisputa)
        );
        this.page = 1;
        this.loading = false;
      },
      error: (err) => {
        this.loading = false;
        const msg =
          typeof err?.error === 'string'
            ? err.error
            : 'No se pudieron cargar las disputas.';
        Swal.fire('Error', msg, 'error');
      },
    });
  }

  get abiertasCount() {
    return this.disputas.filter((x) => x.estado === 'abierta').length;
  }

  get revisionCount() {
    return this.disputas.filter((x) => x.estado === 'en_revision').length;
  }

  get negociacionCount() {
    return this.disputas.filter((x) => x.estado === 'en_negociacion').length;
  }

  get reembolsoCount() {
    return this.disputas.filter(
      (x) =>
        x.estado === 'resuelta_reembolso_total' ||
        x.estado === 'resuelta_reembolso_parcial'
    ).length;
  }

  get promedioDiasResolucion() {
    const resueltas = this.disputas.filter((x) => !!x.fechaResolucion);
    if (!resueltas.length) return 0;

    let totalMs = 0;
    let validas = 0;

    resueltas.forEach((x) => {
      const a = x.fechaApertura ? new Date(x.fechaApertura).getTime() : 0;
      const r = x.fechaResolucion ? new Date(x.fechaResolucion).getTime() : 0;

      if (a && r && r >= a) {
        totalMs += r - a;
        validas++;
      }
    });

    if (!validas) return 0;

    const promedio = totalMs / validas;
    return Math.round((promedio / (1000 * 60 * 60 * 24)) * 10) / 10;
  }

  get filtered(): Disputa[] {
    const s = this.q.trim().toLowerCase();
    if (!s) return this.disputas;

    return this.disputas.filter((r) => {
      const campos = [
        r.idDisputa,
        r.idOrden,
        r.numeroOrden,
        r.idOrdenDetalle,
        r.sku,
        r.usuarioEmail,
        r.clienteNombre,
        r.vendedorNombre,
        r.vendedorRuc,
        r.nombreProducto,
        r.motivo,
        r.descripcion,
        r.estado,
      ];

      return campos.some((v) => String(v ?? '').toLowerCase().includes(s));
    });
  }

  get totalPages() {
    return Math.max(1, Math.ceil(this.filtered.length / this.pageSize));
  }

  get rows(): Disputa[] {
    const start = (this.page - 1) * this.pageSize;
    return this.filtered.slice(start, start + this.pageSize);
  }

  get canSave() {
    if (!this.selected || !this.draft.nuevoEstado || this.working) return false;

    if (this.draft.nuevoEstado === 'resuelta_reembolso_parcial') {
      return Number(this.draft.montoReembolso ?? 0) > 0;
    }

    return true;
  }

  prevPage() {
    if (this.page > 1) this.page--;
  }

  nextPage() {
    if (this.page < this.totalPages) this.page++;
  }

  badgeClass(e: EstadoDisputa) {
    switch (e) {
      case 'abierta':
        return 'badge bg-danger-subtle text-danger';
      case 'en_revision':
        return 'badge bg-warning-subtle text-warning';
      case 'en_negociacion':
        return 'badge bg-info-subtle text-info';
      case 'resuelta_reembolso_total':
      case 'resuelta_reembolso_parcial':
        return 'badge bg-success-subtle text-success';
      case 'resuelta_rechazada':
        return 'badge bg-secondary-subtle text-secondary';
      case 'cerrada':
        return 'badge bg-dark-subtle text-dark';
      default:
        return 'badge bg-light text-dark';
    }
  }

  estadoLabel(e: EstadoDisputa) {
    switch (e) {
      case 'abierta':
        return 'Abierta';
      case 'en_revision':
        return 'En revisión';
      case 'en_negociacion':
        return 'En negociación';
      case 'resuelta_reembolso_total':
        return 'Reembolso total';
      case 'resuelta_reembolso_parcial':
        return 'Reembolso parcial';
      case 'resuelta_rechazada':
        return 'Rechazada';
      case 'cerrada':
        return 'Cerrada';
      default:
        return e;
    }
  }

  nombreRol(rol: RolEmisor) {
    switch (rol) {
      case 'admin':
        return 'Admin';
      case 'cliente':
        return 'Cliente';
      case 'vendedor':
        return 'Vendedor';
      default:
        return rol;
    }
  }

  openDrawer(row: Disputa) {
    this.drawerOpen = true;
    this.lockBodyScroll(true);

    this.selected = { ...row };
    this.mensajes = [];
    this.loadingDrawer = true;

    this.llenarDraftDesdeDisputa(row);

    forkJoin({
      detalle: this.disputaService.obtener(row.idDisputa).pipe(
        catchError(() => of(row))
      ),
      mensajes: this.disputaService.listarMensajes(row.idDisputa).pipe(
        catchError(() => of([] as DisputaMensaje[]))
      ),
    }).subscribe({
      next: ({ detalle, mensajes }) => {
        this.selected = detalle;
        this.mensajes = mensajes || [];
        this.llenarDraftDesdeDisputa(detalle);
        this.loadingDrawer = false;
      },
      error: () => {
        this.loadingDrawer = false;
      },
    });
  }

  closeDrawer() {
    this.drawerOpen = false;
    this.selected = null;
    this.mensajes = [];
    this.loadingDrawer = false;
    this.resetDraft();
    this.lockBodyScroll(false);
  }

  private lockBodyScroll(lock: boolean) {
    if (typeof document === 'undefined') return;
    document.body.style.overflow = lock ? 'hidden' : '';
  }

  private resetDraft() {
    this.draft = {
      nuevoEstado: null,
      mensaje: '',
      adjuntoUrl: '',
      observacionAdmin: '',
      resolucionFinal: '',
      montoReembolso: null,
    };
  }

  private llenarDraftDesdeDisputa(disputa: Disputa) {
    this.draft.nuevoEstado = disputa.estado || null;
    this.draft.observacionAdmin = disputa.observacionAdmin || '';
    this.draft.resolucionFinal = disputa.resolucionFinal || '';
    this.draft.montoReembolso =
      disputa.estado === 'resuelta_reembolso_parcial'
        ? Number(disputa.montoReembolso ?? 0)
        : null;
    this.draft.mensaje = '';
    this.draft.adjuntoUrl = '';
  }

  evidenciaItems(raw?: string | null): string[] {
    const valor = String(raw || '').trim();
    if (!valor) return [];

    try {
      const parsed = JSON.parse(valor);

      if (Array.isArray(parsed)) {
        return parsed.map((x) => String(x)).filter(Boolean);
      }

      if (parsed && typeof parsed === 'object') {
        if (Array.isArray((parsed as any).files)) {
          return (parsed as any).files.map((x: any) => String(x)).filter(Boolean);
        }
        if (Array.isArray((parsed as any).urls)) {
          return (parsed as any).urls.map((x: any) => String(x)).filter(Boolean);
        }
      }

      return [];
    } catch {
      return [];
    }
  }

  elegirEstado(e: EstadoDisputa) {
    this.draft.nuevoEstado = e;

    if (e !== 'resuelta_reembolso_parcial') {
      this.draft.montoReembolso = null;
    }
  }

  enviarMensaje() {
    if (!this.selected || this.sendingMessage) return;

    const texto = this.draft.mensaje.trim();
    if (!texto) return;

    const payload: { mensaje: string; adjuntoUrl?: string } = {
      mensaje: texto,
    };

    const adjunto = this.draft.adjuntoUrl.trim();
    if (adjunto) {
      payload.adjuntoUrl = adjunto;
    }

    this.sendingMessage = true;

    this.disputaService.enviarMensaje(this.selected.idDisputa, payload).subscribe({
      next: (msg) => {
        this.mensajes = [...this.mensajes, msg];
        this.draft.mensaje = '';
        this.draft.adjuntoUrl = '';
        this.sendingMessage = false;
      },
      error: (err) => {
        this.sendingMessage = false;
        const msg =
          typeof err?.error === 'string'
            ? err.error
            : 'No se pudo enviar el mensaje.';
        Swal.fire('Error', msg, 'error');
      },
    });
  }

  guardarYCerrar() {
    if (!this.selected || !this.draft.nuevoEstado || this.working) return;

    if (
      this.draft.nuevoEstado === 'resuelta_reembolso_parcial' &&
      Number(this.draft.montoReembolso ?? 0) <= 0
    ) {
      Swal.fire('Atención', 'Ingresa un monto válido para el reembolso parcial.', 'warning');
      return;
    }

    this.working = true;

    this.disputaService
      .resolver(this.selected.idDisputa, {
        nuevoEstado: this.draft.nuevoEstado,
        montoReembolso:
          this.draft.nuevoEstado === 'resuelta_reembolso_parcial'
            ? Number(this.draft.montoReembolso)
            : null,
        observacionAdmin: this.draft.observacionAdmin.trim(),
        resolucionFinal: this.draft.resolucionFinal.trim(),
      })
      .subscribe({
        next: () => {
          this.working = false;
          this.closeDrawer();
          this.cargar();
          Swal.fire('Actualizada', 'La disputa fue actualizada correctamente.', 'success');
        },
        error: (err) => {
          this.working = false;
          const msg =
            typeof err?.error === 'string'
              ? err.error
              : 'No se pudo actualizar la disputa.';
          Swal.fire('Error', msg, 'error');
        },
      });
  }
}